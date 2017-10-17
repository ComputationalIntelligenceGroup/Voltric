package voltric.learning.structure.hillclimbing.operator;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.model.DiscreteBayesNet;

/**
 * Created by equipo on 02/08/2017.
 */
public interface HcOperator {

    /**
     * Applies the operator to the Bayesian network and returns a new network. In  case there are various possible
     * result models, the best one will be returned (i.e Arc Operators).
     *
     * @param seedNet the initial BN.
     * @param data the DataSet being used to learn the BN after the operator is applied.
     * @param parameterLearning the parameter learning algorithm used to learn the BN.
     * @return the new BN. If the learning score equals {@code Double.MIN_VALUE} it measn the model hasn't been modified.
     */
    LearningResult<DiscreteBayesNet> apply(DiscreteBayesNet seedNet, DiscreteData data, DiscreteParameterLearning parameterLearning);
}
