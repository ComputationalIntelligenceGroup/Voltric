package voltric.clustering.multiview;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import voltric.clustering.StaticClusteringAlgorithm;
import voltric.clustering.multiview.attribute.AttributeGrouping;
import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.parameter.em.ParallelEM;
import voltric.model.HLCM;
import voltric.model.creator.HlcmCreator;
import voltric.util.stattest.discrete.DiscreteStatisticalTest;

import java.util.List;

/**
 * El BI cuenta con varios pasos extra ademas del IslandFinder
 * TODO: Hacer un Abstract class que implemente ambos interfaces y que de lo comun
 */
public class BridgedIslands extends StaticClusteringAlgorithm<HLCM>{

    /** */
    private AttributeGrouping attributeGroupingAlgorithm;

    /** */
    private DiscreteParameterLearning parameterLearning;

    /** */
    private DiscreteStatisticalTest statisticalTest;

    /**
     *
     *
     * @param attributeGroupingAlgorithm
     */
    public BridgedIslands(AttributeGrouping attributeGroupingAlgorithm){
        this.attributeGroupingAlgorithm = attributeGroupingAlgorithm;
        this.parameterLearning = attributeGroupingAlgorithm.getParameterLearning();
        this.statisticalTest = attributeGroupingAlgorithm.getStatisticalTest();
    }

    @Override
    public LearningResult<HLCM> learnModel(DiscreteData dataSet) {

        // 1- Calculate sibling clusters
        List<HLCM> siblingClusters = attributeGroupingAlgorithm.find(dataSet);

        // 2- Refine sibling clusters cardinality
        refineSiblingClustersCardinality(siblingClusters, dataSet);

        // 3- Form the tree
        HLCM flatLTM = HlcmCreator.createFlatLtmRandomRoot(siblingClusters, dataSet, attributeGroupingAlgorithm.getStatisticalTest());

        // 4- Refine the model
        return refineModel(flatLTM, dataSet);
    }

    private void refineSiblingClustersCardinality(List<HLCM> siblingClusters, DiscreteData dataSet){

    }

    private LearningResult<HLCM> refineModel(HLCM model, DiscreteData dataSet){
        throw new NotImplementedException();
    }
}
