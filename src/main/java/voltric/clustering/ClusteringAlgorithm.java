package voltric.clustering;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.model.AbstractBayesNet;

/**
 * Created by fernando on 25/04/17.
 */
public interface ClusteringAlgorithm<M extends AbstractBayesNet> {

    LearningResult<M> learnModel(DiscreteData dataSet);
}
