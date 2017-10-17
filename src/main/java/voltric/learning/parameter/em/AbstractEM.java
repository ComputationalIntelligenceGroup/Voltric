package voltric.learning.parameter.em;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.parameter.em.config.EmConfig;
import voltric.learning.parameter.em.initialization.EmInitialization;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;

import java.util.HashSet;

/**
 * Created by fernando on 4/04/17.
 */
public abstract class AbstractEM implements DiscreteParameterLearning {

    /** The number of elapsed steps */
    protected int nSteps;

    /** The number of restarts */
    protected int nRestarts;

    /** The threshold to control the algorithm's convergence */
    protected double threshold;

    /** The maximum number of EM steps to control its convergence */
    protected int nMaxSteps;

    /** */
    protected EmInitialization initializationMethod;

    /** The flag indicates whether we reuse the parameters of the input BN as a candidate starting point. */
    protected boolean reuse;

    /** The collection of nodes that shouldnt be updated by the EM algorithm */
    protected HashSet<String> dontUpdateNodes;

    /** The type of score used when learning the Bayesian network */
    protected ScoreType scoreType;

    /**
     * Default constructor
     */
    public AbstractEM(){
        this(new EmConfig(), ScoreType.LogLikelihood);
    }

    /**
     *
     *
     * @param config
     */
    public AbstractEM(EmConfig config, ScoreType scoreType){
        this.nSteps = 0;
        this.nRestarts = config.getnRestarts();
        this.threshold = config.getThreshold();
        this.nMaxSteps = config.getnMaxSteps();
        this.initializationMethod = config.getInitializationMethod();
        this.reuse = config.isReuse();
        this.dontUpdateNodes = config.getDontUpdateNodes();
        this.scoreType = scoreType;
    }

    /** {@inheritDoc} */
    public abstract LearningResult<DiscreteBayesNet> learnModel(DiscreteBayesNet bayesNet, DiscreteData dataSet);

    @Override
    public ScoreType getScoreType() {
        return scoreType;
    }
}
