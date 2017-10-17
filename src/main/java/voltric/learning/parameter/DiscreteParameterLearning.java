package voltric.learning.parameter;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;

/**
 * Created by fernando on 18/08/17.
 */
public interface DiscreteParameterLearning extends ParameterLearning{

    LearningResult<DiscreteBayesNet> learnModel(DiscreteBayesNet seedNet, DiscreteData data);

    ScoreType getScoreType();
}
