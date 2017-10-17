package voltric.learning.score;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.inference.CliqueTree;
import voltric.inference.CliqueTreePropagation;
import voltric.model.DiscreteBayesNet;

/**
 * Created by fernando on 8/08/17.
 */
public class LearningScore {

    public static double calculateScore(DiscreteData data, DiscreteBayesNet bayesNet, double logLikelihood, ScoreType scoreType){
        switch (scoreType){
            case LogLikelihood: return logLikelihood;
            case BIC: return calculateBIC(data, bayesNet, logLikelihood);
            case AIC: return calculateAIC(bayesNet, logLikelihood);
            default: throw new IllegalArgumentException("Illegal Score type");
        }
    }

    public static double calculateLogLikelihood(DiscreteData dataSet, DiscreteBayesNet bayesNet){
        double loglikelihood = 0.0;
        CliqueTreePropagation ctp = new CliqueTreePropagation(bayesNet);

        for (DiscreteDataInstance dataInstance : dataSet.getInstances()) {
            double weight = dataSet.getWeight(dataInstance);
            // sets evidences
            ctp.setEvidence(dataSet.getVariables(), dataInstance.getNumericValues());
            // propagates evidence
            double likelihoodDataInstance = ctp.propagate();
            // LogLikelihood & weight
            loglikelihood += Math.log(likelihoodDataInstance) * weight;
        }
        return loglikelihood;
    }

    public static double calculateBIC(DiscreteData data, DiscreteBayesNet bayesNet){
        double logL = calculateLogLikelihood(data, bayesNet);
        return logL - bayesNet.computeDimension() * Math.log(data.getTotalWeight()) / 2.0;
    }

    public static double calculateBIC(DiscreteData data, DiscreteBayesNet bayesNet, double logLikelihood){
        return logLikelihood - bayesNet.computeDimension() * Math.log(data.getTotalWeight()) / 2.0;
    }

    public static double calculateAIC(DiscreteData data, DiscreteBayesNet bayesNet){
        double logL = calculateLogLikelihood(data, bayesNet);
        return logL - bayesNet.computeDimension();
    }

    public static double calculateAIC(DiscreteBayesNet bayesNet, double logLikelihood){
        return logLikelihood - bayesNet.computeDimension();
    }
}
