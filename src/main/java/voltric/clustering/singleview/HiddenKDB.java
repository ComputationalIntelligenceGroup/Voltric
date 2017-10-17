package voltric.clustering.singleview;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.structure.hillclimbing.GeneralHillClimbing;
import voltric.learning.structure.hillclimbing.operator.*;
import voltric.learning.structure.type.DagStructure;
import voltric.model.DiscreteBayesNet;
import voltric.model.HLCM;
import voltric.model.creator.HlcmCreator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fernando on 25/08/17.
 */
public class HiddenKDB {

    public static LearningResult<DiscreteBayesNet> learnModel(int maxCardinality,
                                                              DiscreteData dataSet,
                                                              DiscreteParameterLearning parameterLearning,
                                                              double threshold,
                                                              int maxParents,
                                                              int maxIterations){

        // First the initial model is created
        HLCM initialModel = HlcmCreator.createLCM(dataSet.getVariables(), 2);

        // A hill-climbing search process is applied where 5 operators are allowed
        IncreaseLatentCardinality ilcOperator = new IncreaseLatentCardinality(maxCardinality);
        DecreaseLatentCardinality dlcOperator = new DecreaseLatentCardinality();
        AddArc addArcOperator = new AddArc(new DagStructure(), maxParents);
        // The remove and reverse operators have a set of forbidden edges, those that go from the root to the manifest nodes.
        RemoveArc removeArcOperator = new RemoveArc(new ArrayList<>(), initialModel.getEdges(), new DagStructure());
        ReverseArc reverseArcOperator = new ReverseArc(new ArrayList<>(), initialModel.getEdges(), new DagStructure());

        Set<HcOperator> operatorSet = new HashSet<>();
        operatorSet.add(ilcOperator);
        operatorSet.add(dlcOperator);
        operatorSet.add(addArcOperator);
        operatorSet.add(removeArcOperator);
        operatorSet.add(reverseArcOperator);

        // The number of iterations isn't taken into consideration, that is why a fixed number like 10 is assigned
        GeneralHillClimbing hillClimbing = new GeneralHillClimbing(operatorSet, maxIterations, threshold);

        return hillClimbing.learnModel(initialModel, dataSet, parameterLearning);
    }
}
