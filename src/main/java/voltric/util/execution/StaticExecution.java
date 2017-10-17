package voltric.util.execution;

import voltric.data.DiscreteData;
import voltric.model.AbstractBayesNet;

/**
 * Created by fernando on 25/04/17.
 */
public interface StaticExecution<M extends AbstractBayesNet> {

    /**
     *
     *
     * @param dataSet
     * @return
     */
    ExecutionResult<M> execute(DiscreteData dataSet);

    /**
     *
     *
     * @param dataSet
     * @param executionIndex
     * @return
     */
    ExecutionResult<M> execute(DiscreteData dataSet, int executionIndex);
}
