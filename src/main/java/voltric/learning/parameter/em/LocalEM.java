package voltric.learning.parameter.em;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.inference.CliqueTreePropagation;
import voltric.learning.LearningResult;
import voltric.learning.parameter.em.config.LocalEmConfig;
import voltric.learning.parameter.em.initialization.ChickeringHeckerman;
import voltric.learning.parameter.em.initialization.MultipleRestarts;
import voltric.learning.parameter.em.util.MessagesForLocalEM;
import voltric.learning.score.LearningScore;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by fernando on 3/04/17.
 */
public class LocalEM extends AbstractSequentialEM{

    /**
     * A repository of messages. In thie implementation, this must be prepared
     * beforehand.
     */
    private Map<DiscreteDataInstance, Set<MessagesForLocalEM>> repository;

    /**
     * We control termination of localEM by number of continued steps.
     */
    protected int nContinuedSteps;

    /**
     * Specify that in M-step, whose Cpt will be updated.
     */
    protected DiscreteVariable[] mutableVars;

    /**
     * A template Ctp. The useful information conveyed is the cliquetree,
     * especially the foucused subtree contained.
     */
    protected CliqueTreePropagation templateCtp;

    public LocalEM(LocalEmConfig config, ScoreType scoreType) {
        super(config, scoreType);
        this.repository = config.getRepository();
        this.nContinuedSteps = config.getnContinuedSteps();
        this.mutableVars = config.getMutableVars();
        this.templateCtp = config.getTemplateCtp();
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

        // runs EM steps until convergence
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
    protected double emStep(CliqueTreePropagation ctp, DiscreteData dataSet) {
        DiscreteBayesNet bayesNet = ctp.getBayesNet();
        HashMap<DiscreteVariable, Function> suffStats = new HashMap<DiscreteVariable, Function>();
        double loglikelihood = 0.0;

        // computes datum by datum
        for (DiscreteDataInstance dataCase : dataSet.getInstances()) {
            double weight = dataSet.getWeight(dataCase);

            Set<MessagesForLocalEM> msgs = this.repository.get(dataCase);
            ctp.getCliqueTree().copyInMsgsFrom(msgs);

            ctp.setEvidence(dataSet.getVariables(), dataCase.getNumericValues());

            // propagates
            double likelihood = ctp.propagate();

            // updates sufficient statistics for each mutable node
            for (DiscreteVariable var : this.mutableVars) {
                Function fracWeight = ctp.computeFamilyBelief(var);
                fracWeight.multiply(weight);

                if (suffStats.containsKey(var)) {
                    suffStats.get(var).plus(fracWeight);
                } else {
                    suffStats.put(var, fracWeight);
                }
            }

            // updates loglikelihood
            loglikelihood += Math.log(likelihood) * weight;
        }

        // updates parameters
        for (DiscreteVariable var : this.mutableVars) {
            Function cpt = suffStats.get(var);
            cpt.normalize(var);
            bayesNet.getNode(var).setCpt(cpt);
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
            DiscreteBayesNet bayesNetCopy = bayesNet.clone();

            // finds mutable nodes in new BN
            ArrayList<DiscreteBeliefNode> mutableNodesCopy = new ArrayList<>();
            for (DiscreteVariable var : this.mutableVars) {
                mutableNodesCopy.add(bayesNetCopy.getNode(var));
            }

            // in case we reuse the parameters of the input BN as a starting
            // point, we put it at the first place.
            if (!this.reuse || i != 0) {
                bayesNetCopy.randomlyParameterize(mutableNodesCopy);
            }

            ctps[i] = this.templateCtp.clone();
            ctps[i].setBayesNet(bayesNetCopy);
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

                    if(currentScore[i] - lastStepScore[i] >this.threshold || lastStepScore[i] == Double.NEGATIVE_INFINITY)
                        noImprovements = false;
                }
                this.nSteps++;

                if(noImprovements)
                {
                    return ctps[0];
                }
            }

            // sorts BNs in descending order with respect to the scores
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
    protected CliqueTreePropagation multipleRestarts(DiscreteBayesNet bayesNet, DiscreteData dataSet, MultipleRestarts multipleRestarts) {
        CliqueTreePropagation[] ctps = new CliqueTreePropagation[this.nRestarts];
        double[] currentScore = new double[this.nRestarts];

        for (int i = 0; i < this.nRestarts; i++) {
            DiscreteBayesNet bayesNetCopy = bayesNet.clone();

            // finds mutable nodes in new BN
            ArrayList<DiscreteBeliefNode> mutableNodesCopy = new ArrayList<>();
            for (DiscreteVariable var : this.mutableVars) {
                mutableNodesCopy.add(bayesNetCopy.getNode(var));
            }

            // in case we reuse the parameters of the input BN as a starting
            // point, we put it at the first place.
            if (!this.reuse || i != 0) {
                bayesNetCopy.randomlyParameterize(mutableNodesCopy);
            }

            ctps[i] = this.templateCtp.clone();
            ctps[i].setBayesNet(bayesNetCopy);
        }

        this.nSteps += multipleRestarts.getNumInitIterations();
        for (int i = 0; i < this.nRestarts; i++) {
            double score = 0;
            for (int j = 0; j < multipleRestarts.getNumInitIterations(); j++)
                score = emStep(ctps[i], dataSet);

            currentScore[i] = score;
        }
        this.nSteps += multipleRestarts.getnPreSteps();
        for (int i = 0; i < this.nRestarts; i++) {
            double score = 0;
            for (int j = 0; j < multipleRestarts.getnPreSteps(); j++)
                score = emStep(ctps[i], dataSet);

            currentScore[i] = score;
        }


        CliqueTreePropagation bCtp = null;
        double bScore = -Double.MAX_VALUE;
        for (int i = 0; i < this.nRestarts; i++) {
            double score = currentScore[i];
            if (score > bScore) {
                bCtp = ctps[i];
                bScore = score;
            }
        }
        // returns the CTPs for the best starting point
        return bCtp;
    }
}
