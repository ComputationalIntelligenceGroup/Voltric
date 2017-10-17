package voltric.learning.parameter.em;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.graph.AbstractNode;
import voltric.inference.CliqueTreePropagation;
import voltric.inference.CliqueTreePropagationGroup;
import voltric.learning.LearningResult;
import voltric.learning.parameter.em.config.EmConfig;
import voltric.learning.parameter.em.initialization.ChickeringHeckerman;
import voltric.learning.parameter.em.initialization.MultipleRestarts;
import voltric.learning.score.LearningScore;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by fernando on 1/04/17.
 */
public class ParallelEM extends AbstractParallelEM {

    private static ForkJoinPool threadPool = null;

    public ParallelEM(){
        super();
    }

    public ParallelEM(EmConfig config, ScoreType scoreType) {
        super(config, scoreType);
    }

    /** {@inheritDoc} */
    @Override
    public LearningResult<DiscreteBayesNet> learnModel(DiscreteBayesNet bayesNet, DiscreteData dataSet) {

        if(!dataSet.getVariables().containsAll(bayesNet.getManifestVariables()))
            throw new IllegalArgumentException("The Data set must contain all the manifest variables present in the Bayes net");

        // resets the number of EM steps
        this.nSteps = 0;

        // selects a good starting point
        CliqueTreePropagationGroup ctps = emStart(bayesNet, dataSet);

        double previousScore = emStep(ctps, dataSet);
        this.nSteps++;

        // runs EM steps until convergence
        //TODO: Esta mal implementado. El loop no coincide con el EM
        double score;
        do {
            score = emStep(ctps, dataSet);
            this.nSteps++;
        } while (score - previousScore > this.threshold
                && this.nSteps < this.nMaxSteps);

        return new LearningResult<>(ctps.model, score, this.scoreType);
    }

    /** {@inheritDoc} */
    @Override
    protected double emStep(CliqueTreePropagationGroup ctps, DiscreteData dataSet) {
        ForkComputation.Context context = new ForkComputation.Context(dataSet, ctps, this.dontUpdateNodes);

        ForkComputation computation = new ForkComputation(context, 0, dataSet.getInstances().size());
        getForkJoinPool().invoke(computation);

        // updates parameters
        for (AbstractNode node : ctps.model.getNodes()) {
            DiscreteBeliefNode bNode = (DiscreteBeliefNode) node;

            if (this.dontUpdateNodes != null
                    && this.dontUpdateNodes.contains(bNode.getName()))
                continue;

            Function cpt = computation.suffStats.get(bNode.getVariable());
            // Add 1 to each entry to avoid 0 probability By Peixian Chen
            for(int i=0; i<cpt.getDomainSize(); i++)
            {
                cpt.getCells()[i] = cpt.getCells()[i]+1;
            }
            cpt.normalize(bNode.getVariable());
            bNode.setCpt(cpt);
        }


        if (Math.abs(computation.loglikelihood
                - computation.loglikelihoodAlternative) > 1e-6) {
            System.out.printf(
                    "Loglikelihood and Alternative loglikelihood do not match: "
                            + "%e vs %e\n", computation.loglikelihood,
                    computation.loglikelihoodAlternative);
            System.out.printf(
                    "Now it is using Alternative loglikelihood (%f).\n",
                    computation.loglikelihoodAlternative);
        }

        return LearningScore.calculateScore(dataSet, ctps.model, computation.loglikelihoodAlternative, this.scoreType);
    }
    
    /** {@inheritDoc} */
    @Override
    protected CliqueTreePropagationGroup chickeringHeckermanRestart(DiscreteBayesNet bayesNet, DiscreteData dataSet, ChickeringHeckerman chickeringHeckermanConfig) {
        // generates random starting points and CTPs for them
        CliqueTreePropagationGroup[] ctps = new CliqueTreePropagationGroup[this.nRestarts];
        double[] lastStepScore = new double[this.nRestarts];
        double[] currentScore = new double[this.nRestarts];

        for (int i = 0; i < this.nRestarts; i++) {
            DiscreteBayesNet copy = bayesNet.clone();

            // in case we reuse the parameters of the input BN as a starting
            // point, we put it at the first place.
            if (!this.reuse || i != 0) {
                if (this.dontUpdateNodes == null) {
                    copy.randomlyParameterize();
                } else {
                    for (DiscreteBeliefNode node : copy.getNodes()) {
                        if (!this.dontUpdateNodes.contains(node.getName())) {
                            Function cpt = node.getCpt();
                            cpt.randomlyDistribute(node.getVariable());
                            node.setCpt(cpt);
                        }
                    }
                }
            }

            ctps[i] = CliqueTreePropagationGroup.constructFromModel(copy, getForkJoinPool().getParallelism());
        }

        // We run several steps of emStep before killing starting points for two reasons:
        // 1. the loglikelihood computed is always that of previous model.
        // 2. When reuse, the reused model is kind of dominant because maybe it has already EMed.
        this.nSteps += chickeringHeckermanConfig.getNumInitIterations();
        for (int i = 0; i < this.nRestarts; i++) {
            double score = 0;
            for (int j = 0; j < chickeringHeckermanConfig.getNumInitIterations(); j++)
                score = emStep(ctps[i], dataSet);

            currentScore[i] = score;
        }

        // game starts, half ppl die in each round :-)
        int nCandidates = this.nRestarts;
        int nStepsPerRound = 1;

        while (nCandidates > 1 && this.nSteps < this.nMaxSteps) {
            // runs EM on all starting points for several steps
            for (int j = 0; j < nStepsPerRound; j++) {
                boolean noImprovements = true;
                for (int i = 0; i < nCandidates; i++) {
                    lastStepScore[i] = currentScore[i];
                    currentScore[i] = emStep(ctps[i], dataSet);

                    if(currentScore[i] - lastStepScore[i] >this.threshold || lastStepScore[i] == Double.NEGATIVE_INFINITY)
                        noImprovements = false;
                }
                this.nSteps++;

                if (noImprovements) {
                    return ctps[0];
                }
            }

            // sorts BNs in descending order with respect to loglikelihoods
            for (int i = 0; i < nCandidates - 1; i++) {
                for (int j = i + 1; j < nCandidates; j++) {
                    if (currentScore[i] < currentScore[j]) {
                        CliqueTreePropagationGroup tempCtp = ctps[i];
                        ctps[i] = ctps[j];
                        ctps[j] = tempCtp;

                    }
                }
            }

            // retains top half
            nCandidates /= 2;

            // doubles EM steps subject to maximum step constraint
            nStepsPerRound = Math.min(nStepsPerRound * 2, this.nMaxSteps - this.nSteps);
        }

        // returns the CTP for the best starting point
        return ctps[0];
    }

    /** {@inheritDoc} */
    @Override
    protected CliqueTreePropagationGroup multipleRestarts(DiscreteBayesNet bayesNet, DiscreteData dataSet, MultipleRestarts multipleRestarts) {
        throw new NotImplementedException();
    }

    @SuppressWarnings("serial")
    private static class ForkComputation extends RecursiveAction {
        public static class Context {
            // input
            public final DiscreteData data;
            public final CliqueTreePropagationGroup ctps;
            public final HashSet<String> nonUpdateNodes;
            public final int splitThreshold;

            public Context(DiscreteData data, CliqueTreePropagationGroup ctps,
                           HashSet<String> nonUpdateNodes) {
                this.data = data;
                this.ctps = ctps;
                this.nonUpdateNodes = nonUpdateNodes;
                splitThreshold = (int) Math.ceil(data.getInstances().size() / (double) ctps.capacity);
            }
        }

        private final Context context;
        private final int start;
        private final int length;

        // the result object is assumed to be accessed by a single thread only.

        // sufficient statistics for each node
        public final HashMap<DiscreteVariable, Function> suffStats =
                new HashMap<DiscreteVariable, Function>();
        private double loglikelihood = 0;

        // loglikelihood that is computed in an alternative way. In particular,
        // log is applied during the propagation rather than after propagation
        // to avoid zero likelihood.
        private double loglikelihoodAlternative = 0;

        public ForkComputation(Context context, int start, int length) {
            this.context = context;
            this.start = start;
            this.length = length;
        }

        @Override
        protected void compute() {
            if (length <= context.splitThreshold) {
                computeDirectly();
                return;
            }

            int split = length / 2;
            ForkComputation c1 = new ForkComputation(context, start, split);
            ForkComputation c2 =
                    new ForkComputation(context, start + split, length - split);
            invokeAll(c1, c2);

            loglikelihood = c1.loglikelihood + c2.loglikelihood;
            loglikelihoodAlternative =
                    c1.loglikelihoodAlternative + c2.loglikelihoodAlternative;

            for (DiscreteVariable v : context.ctps.model.getVariables()) {
                if (context.nonUpdateNodes != null
                        && context.nonUpdateNodes.contains(v.getName()))
                    continue;

                addToSufficientStatistics(suffStats, v, c1.suffStats.get(v));
                addToSufficientStatistics(suffStats, v, c2.suffStats.get(v));
            }
        }

        private void computeDirectly() {
            CliqueTreePropagation ctp = context.ctps.take();

            // computes datum by datum
            for (int i = start; i < start + length; i++) {
                DiscreteDataInstance dataCase = context.data.getInstances().get(i);
                double weight = context.data.getWeight(dataCase);

                // sets evidences
                ctp.setEvidence(context.data.getVariables(), dataCase.getNumericValues());

                // propagates
                double likelihoodDataCase = ctp.propagate();
                double loglikelihoodAlternativeDataCase =
                        ctp.getLastLogLikelihood();

                if(likelihoodDataCase <= Double.MIN_NORMAL)
                    throw new InternalError("likelihoodDataCase should be > Double.MIN_NORMAL");

                // updates sufficient statistics for each node
                for (DiscreteVariable var : context.ctps.model.getVariables()) {

                    if (context.nonUpdateNodes != null
                            && context.nonUpdateNodes.contains(var.getName()))
                        continue;

                    Function fracWeight = ctp.computeFamilyBelief(var);

                    fracWeight.multiply(weight);

                    addToSufficientStatistics(suffStats, var, fracWeight);
                }

                loglikelihood += Math.log(likelihoodDataCase) * weight;
                loglikelihoodAlternative +=
                        loglikelihoodAlternativeDataCase * weight;
            }

            context.ctps.put(ctp);
        }

        private static void addToSufficientStatistics(
                HashMap<DiscreteVariable, Function> stats, DiscreteVariable variable, Function f) {
            if (stats.containsKey(variable)) {
                stats.get(variable).plus(f);
            } else {
                stats.put(variable, f);
            }
        }
    }

    protected static ForkJoinPool getForkJoinPool() {
        if (threadPool == null)
            threadPool = new ForkJoinPool();

        return threadPool;
    }
}
