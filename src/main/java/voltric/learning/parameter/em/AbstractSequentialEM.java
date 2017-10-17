package voltric.learning.parameter.em;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagation;
import voltric.learning.parameter.em.config.EmConfig;
import voltric.learning.parameter.em.initialization.ChickeringHeckerman;
import voltric.learning.parameter.em.initialization.MultipleRestarts;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;

/**
 * Created by fernando on 4/04/17.
 */
public abstract class AbstractSequentialEM extends AbstractEM{

    /**
     * Default constructor
     */
    public AbstractSequentialEM(){
        super();
    }

    /**
     *
     * @param config
     */
    public AbstractSequentialEM(EmConfig config, ScoreType scoreType) {
        super(config, scoreType);
    }

    /**
     * Devuelve la log-likelihood de aprender la BN con el dataSet.
     *
     * @param ctp
     * @param dataSet
     */
    protected abstract double emStep(CliqueTreePropagation ctp, DiscreteData dataSet);

    /**
     * Selects a good starting point using the Chickering & Heckerman's strategy.
     *
     * <p><b>Note:</b> that this restarting phase will terminate midway if the maximum number of steps is reached. However,
     * it will not terminate if the EM algorithm already converges on some starting point. That makes things complicated.</p>
     *
     * @param bayesNet the input Bayes Net.
     * @param dataSet the data set to be used.
     * @param chickeringHeckermanConfig the object that contains the parameters specific to the initializationMethod strategy.
     * @return the best starting point's CTP.
     * @see ChickeringHeckerman
     */
    protected abstract CliqueTreePropagation chickeringHeckermanRestart(DiscreteBayesNet bayesNet, DiscreteData dataSet, ChickeringHeckerman chickeringHeckermanConfig);

    /**
     *
     *
     * @param bayesNet
     * @param dataSet
     * @param multipleRestarts
     * @return
     */
    protected abstract CliqueTreePropagation multipleRestarts(DiscreteBayesNet bayesNet, DiscreteData dataSet, MultipleRestarts multipleRestarts);

    /**
     * The EM initializationMethod strategy
     *
     * @param bayesNet
     * @param dataSet
     * @return
     */
    protected CliqueTreePropagation emStart(DiscreteBayesNet bayesNet, DiscreteData dataSet){
        if(this.initializationMethod instanceof ChickeringHeckerman)
            return chickeringHeckermanRestart(bayesNet, dataSet, (ChickeringHeckerman) this.initializationMethod);
        if(this.initializationMethod instanceof MultipleRestarts)
            return multipleRestarts(bayesNet, dataSet, (MultipleRestarts) this.initializationMethod);
        else
            throw new IllegalArgumentException("Invalid escape method");
    }
}
