package voltric.learning.structure.constraintbased.test;

import voltric.data.DiscreteData;
import voltric.variables.DiscreteVariable;

import java.util.List;

/**
 * Created by equipo on 16/10/2017.
 */
public interface CITest {

    double test(DiscreteVariable a, DiscreteVariable b, List<DiscreteVariable> conditionalVars, DiscreteData data);
}
