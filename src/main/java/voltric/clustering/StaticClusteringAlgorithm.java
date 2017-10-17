package voltric.clustering;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.model.AbstractBayesNet;
import voltric.util.execution.ExecutionResult;
import voltric.util.execution.StaticExecution;

/**
 * Created by fernando on 27/04/17.
 */
public abstract class StaticClusteringAlgorithm<M extends AbstractBayesNet> implements ClusteringAlgorithm<M>, StaticExecution<M>{

    @Override
    public ExecutionResult<M> execute(DiscreteData dataSet) {
        return this.execute(dataSet, 0);
    }

    @Override
    public ExecutionResult<M> execute(DiscreteData dataSet, int executionIndex) {
        double nanoStart = System.currentTimeMillis();
        LearningResult<M> learningResult = this.learnModel(dataSet);
        double nanoFinish = System.currentTimeMillis();

        return new ExecutionResult<>(learningResult.getBayesianNetwork(),
                learningResult.getScoreValue(),
                learningResult.getScoreType(),
                executionIndex,
                nanoStart,
                nanoFinish);
    }
}
