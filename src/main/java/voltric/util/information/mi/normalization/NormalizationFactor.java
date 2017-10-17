package voltric.util.information.mi.normalization;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagation;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.List;

/**
 * Created by equipo on 25/07/2017.
 */
public interface NormalizationFactor {

    double partialNormalizeMI(double mi, double px, double py, double pxy);

    // frequency-countered normalization (according to FrequencyCounteredMI)
    double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet);
    /*******************************************************************************/

    double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet, CliqueTreePropagation ctp);

    double normalizeCMI(double cmi, List<DiscreteVariable> x, List<DiscreteVariable> y, List<DiscreteVariable> condVars, DiscreteData dataSet, CliqueTreePropagation ctp);

    /*******************************************************************************/
    // empirical-distribution normalization (according to EmpiricalDistributionMI)
    double normalizeMI(double mi, Function dist);

    double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, Function dist);

    double normalizeCMI(double cmi, Function dist, DiscreteVariable condVar); // Este método deberia ser un caso especial del List<condVars>

    double normalizeCMI(double cmi, Function dist, List<DiscreteVariable> condVars);


}
