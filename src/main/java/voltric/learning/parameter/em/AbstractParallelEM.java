package voltric.learning.parameter.em;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagationGroup;
import voltric.learning.parameter.em.config.EmConfig;
import voltric.learning.parameter.em.initialization.ChickeringHeckerman;
import voltric.learning.parameter.em.initialization.MultipleRestarts;
import voltric.learning.score.ScoreType;
import voltric.model.DiscreteBayesNet;

/**
 * Created by fernando on 4/04/17.
 */
public abstract class AbstractParallelEM extends AbstractEM{

    public AbstractParallelEM(){
        super();
    }

    public AbstractParallelEM(EmConfig config, ScoreType scoreType) {
        super(config, scoreType);
    }

    protected abstract double emStep(CliqueTreePropagationGroup ctps, DiscreteData dataSet);

    protected abstract CliqueTreePropagationGroup chickeringHeckermanRestart(DiscreteBayesNet bayesNet, DiscreteData dataSet, ChickeringHeckerman chickeringHeckermanConfig);

    protected abstract CliqueTreePropagationGroup multipleRestarts(DiscreteBayesNet bayesNet, DiscreteData dataSet, MultipleRestarts multipleRestarts);

    protected CliqueTreePropagationGroup emStart(DiscreteBayesNet bayesNet, DiscreteData dataSet){
        if(this.initializationMethod instanceof ChickeringHeckerman)
            return chickeringHeckermanRestart(bayesNet, dataSet, (ChickeringHeckerman) this.initializationMethod);
        if(this.initializationMethod instanceof MultipleRestarts)
            return multipleRestarts(bayesNet, dataSet, (MultipleRestarts) this.initializationMethod);
        else
            throw new IllegalArgumentException("Invalid escape method");
    }
}
