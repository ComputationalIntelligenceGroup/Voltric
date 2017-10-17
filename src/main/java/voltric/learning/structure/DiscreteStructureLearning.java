package voltric.learning.structure;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.model.DiscreteBayesNet;

/**
 * Created by fernando on 18/08/17.
 */
public interface DiscreteStructureLearning{

    LearningResult<DiscreteBayesNet> learnModel(DiscreteBayesNet seedNet, DiscreteData data, DiscreteParameterLearning parameterLearning);
}
