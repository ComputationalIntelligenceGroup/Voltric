package voltric.clustering.singleview;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.structure.hillclimbing.GeneralHillClimbing;
import voltric.learning.structure.hillclimbing.operator.HcOperator;
import voltric.learning.structure.hillclimbing.operator.IncreaseLatentCardinality;
import voltric.model.DiscreteBayesNet;
import voltric.model.HLCM;
import voltric.model.creator.HlcmCreator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by fernando on 24/08/17.
 */
public class HiddenNaiveBayes {

    /**
     *
     * @param maxCardinality
     * @param dataSet
     * @param parameterLearning
     * @param threshold
     * @return
     */
    // TODO: De momento he dejado de lado todo el tema de HLCM porque es un poco raro para el HC como type parameters
    public static LearningResult<DiscreteBayesNet> learnModel(int maxCardinality,
                                                              DiscreteData dataSet,
                                                              DiscreteParameterLearning parameterLearning,
                                                              double threshold){

        // First the initial model is created
        HLCM initialModel = HlcmCreator.createLCM(dataSet.getVariables(), 2);

        // A hill-climbing search process is applied where only the IncreaseLatentCardinality operator is used
        IncreaseLatentCardinality ilcOperator = new IncreaseLatentCardinality(maxCardinality);
        Set<HcOperator> operatorSet = new HashSet<>();
        operatorSet.add(ilcOperator);

        // The number of iterations isn't taken into consideration, that is why a fixed number like 10 is assigned
        GeneralHillClimbing hillClimbing = new GeneralHillClimbing(operatorSet, 10, threshold);

        return hillClimbing.learnModel(initialModel, dataSet, parameterLearning);
    }
}
