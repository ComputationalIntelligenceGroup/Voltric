package voltric.learning.parameter.em;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.inference.CliqueTreePropagation;
import voltric.learning.LearningResult;
import voltric.learning.parameter.em.config.EmConfig;
import voltric.learning.parameter.em.initialization.ChickeringHeckerman;
import voltric.learning.parameter.em.initialization.MultipleRestarts;
import voltric.learning.score.LearningScore;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.model.HLCM;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.HashMap;

/**
 * Created by fernando on 4/04/17.
 */
public class EM extends AbstractSequentialEM {

    public EM(){
        super();
    }

    public EM(EmConfig config, ScoreType scoreType) {
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
        CliqueTreePropagation ctp = emStart(bayesNet, dataSet);

        double previousScore = emStep(ctp, dataSet);
        this.nSteps++;

        // runs EM steps until convergence or if the max number of steps is reached.
        // It is necessary to do at least one iteration
        double score;
        do {
            score = emStep(ctp, dataSet);
            this.nSteps++;
        } while (score - previousScore > this.threshold
                && this.nSteps < this.nMaxSteps);

        return new LearningResult<>(bayesNet, score, this.scoreType);
    }

    /** {@inheritDoc} */
    @Override
    protected double emStep(CliqueTreePropagation ctp, DiscreteData dataSet){
        // gets the BN to be optimized
        DiscreteBayesNet bayesNet = ctp.getBayesNet();

        // sufficient statistics for each node
        HashMap<DiscreteVariable, Function> suffStats = new HashMap<DiscreteVariable, Function>();

        double loglikelihood = 0.0;

        for (DiscreteDataInstance dataInstance : dataSet.getInstances()) {
            double weight = dataSet.getWeight(dataInstance);

            // sets evidences
            ctp.setEvidence(dataSet.getVariables(), dataInstance.getNumericValues());

            // propagates
            double likelihoodDataCase = ctp.propagate();

            // updates sufficient statistics for each node
            for (DiscreteVariable var : bayesNet.getVariables()) {

                if(this.dontUpdateNodes != null && this.dontUpdateNodes.contains(var.getName()))
                    continue;

                Function fracWeight = ctp.computeFamilyBelief(var);

                fracWeight.multiply(weight);

                if (suffStats.containsKey(var)) {
                    suffStats.get(var).plus(fracWeight);
                } else {
                    suffStats.put(var, fracWeight);
                }
            }

            loglikelihood += Math.log(likelihoodDataCase) * weight;

        }

        // updates parameters
        for (DiscreteBeliefNode node : bayesNet.getNodes()) {

            if(this.dontUpdateNodes != null && this.dontUpdateNodes.contains(node.getVariable().getName()))
                continue;

            Function cpt = suffStats.get(node.getVariable());
            cpt.normalize(node.getVariable());
            node.setCpt(cpt);
        }

        return LearningScore.calculateScore(dataSet, bayesNet, loglikelihood, this.scoreType);
    }

    /** {@inheritDoc} */
    @Override
    protected CliqueTreePropagation chickeringHeckermanRestart(DiscreteBayesNet bayesNet, DiscreteData dataSet, ChickeringHeckerman chickeringHeckermanConfig) {
        // generates random starting points and CTPs for them
        CliqueTreePropagation[] ctps = new CliqueTreePropagation[this.nRestarts];
        double[] lastStepScore = new double[this.nRestarts];
        double[] currentScore = new double[this.nRestarts];

        for (int i = 0; i < this.nRestarts; i++) {
            DiscreteBayesNet copy = bayesNet.clone();

            // in case we reuse the parameters of the input BN as a starting
            // point, we put it at the first place.
            if (!this.reuse || i != 0)
            {
                if(this.dontUpdateNodes == null)
                {
                    copy.randomlyParameterize();
                }else
                {
                    for(DiscreteBeliefNode node : copy.getNodes())
                    {
                        if(!this.dontUpdateNodes.contains(node.getVariable().getName()))
                        {
                            Function cpt = node.getCpt();
                            cpt.randomlyDistribute(node.getVariable());
                            node.setCpt(cpt);
                        }
                    }
                }
            }

            if (copy instanceof HLCM) {
                ctps[i] = new CliqueTreePropagation((HLCM) copy);
            } else {
                ctps[i] = new CliqueTreePropagation(copy);
            }
        }

        // We run several steps of emStep before killing starting points for two reasons:
        // 1. the loglikelihood-related score being computed is always greater that of previous model.
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

        while (nCandidates > 1 && this.nSteps < this.nMaxSteps)
        {
            // runs EM on all starting points for several steps
            for (int j = 0; j < nStepsPerRound; j++)
            {
                boolean noImprovements = true;
                for (int i = 0; i < nCandidates; i++)
                {
                    lastStepScore[i] = currentScore[i];
                    currentScore[i] = emStep(ctps[i], dataSet);

                    if(currentScore[i] - lastStepScore[i] > this.threshold || lastStepScore[i] == Double.NEGATIVE_INFINITY)
                        noImprovements = false;
                }
                this.nSteps++;

                if(noImprovements)
                    return ctps[0];

            }

            // sorts BNs in descending order with respect to the score
            for (int i = 0; i < nCandidates - 1; i++) {
                for (int j = i + 1; j < nCandidates; j++) {
                    if (currentScore[i] < currentScore[j]) {
                        CliqueTreePropagation tempCtp = ctps[i];
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
    protected CliqueTreePropagation multipleRestarts(DiscreteBayesNet bayesNet, DiscreteData dataSet, MultipleRestarts multipleRestarts){
        throw new NotImplementedException();
    }
}
