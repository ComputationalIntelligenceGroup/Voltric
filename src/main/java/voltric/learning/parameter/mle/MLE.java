package voltric.learning.parameter.mle;

import voltric.data.DiscreteData;
import voltric.graph.DirectedNode;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.score.LearningScore;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;

/**
 * Created by fernando on 17/08/17.
 */
public class MLE implements DiscreteParameterLearning {

    private ScoreType scoreType;

    public MLE(ScoreType scoreType) {
        this.scoreType = scoreType;
    }

    @Override
    public LearningResult<DiscreteBayesNet> learnModel(DiscreteBayesNet bayesNet, DiscreteData dataSet) {
        if (bayesNet.getLatentNodes().size() > 0)
            throw new IllegalArgumentException("Models with latent nodes are not accepted because their parameters cannot be estimated using StaticMLE");

        if (!dataSet.getVariables().containsAll(bayesNet.getVariables()))
            throw new IllegalArgumentException("The Data set must contain all the variables present in the Bayes net");

        for (DiscreteBeliefNode node : bayesNet.getNodes()) {

            // retrieve variables in family
            ArrayList<DiscreteVariable> family = new ArrayList<>();
            family.add(node.getVariable());

            for (DirectedNode parent : node.getParents()) {
                family.add(((DiscreteBeliefNode) parent).getVariable());
            }

            // derives CPT from sufficient statistics
            Function cpt = Function.createFunction(dataSet.project(family));
            cpt.normalize(node.getVariable());

            // sets CPT
            node.setCpt(cpt);
        }

        // Calculate the Log-Likelihood for the Data
        double logLikelihood = LearningScore.calculateLogLikelihood(dataSet, bayesNet);

        return new LearningResult<>(bayesNet, LearningScore.calculateScore(dataSet, bayesNet, logLikelihood, this.scoreType), this.scoreType);
    }

    @Override
    public ScoreType getScoreType () {
        return scoreType;
    }
}