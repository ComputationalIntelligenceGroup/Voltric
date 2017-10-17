package voltric.learning.structure.constraintbased.test;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagation;
import voltric.model.DiscreteBayesNet;
import voltric.util.information.mi.normalization.NormalizationFactor;
import voltric.variables.DiscreteVariable;

import java.util.List;

/**
 * Created by equipo on 17/10/2017.
 */
public class BnFactorizationNMITest implements CITest{

    private NormalizationFactor nmiNormalizationFactor;

    private CliqueTreePropagation ctp;

    public BnFactorizationNMITest(NormalizationFactor nmiNormalizationFactor, DiscreteBayesNet bayesNet){
        this.nmiNormalizationFactor = nmiNormalizationFactor;
        this.ctp = new CliqueTreePropagation(bayesNet);
    }

    @Override
    public double test(DiscreteVariable a, DiscreteVariable b, List<DiscreteVariable> conditionalVars, DiscreteData data) {
        return 0;
    }
}
