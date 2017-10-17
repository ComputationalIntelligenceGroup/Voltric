package voltric.util.stattest.discrete;

import voltric.data.DiscreteData;
import voltric.potential.Function;
import voltric.util.information.mi.normalization.NormalizationFactor;
import voltric.variables.DiscreteVariable;

import java.util.List;
import java.util.Map;

/**
 * Created by fernando on 26/08/17.
 */
public interface DiscreteStatisticalTest {

    double computePairwise(Function dist);

    double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, Function dist);

    double computePairwise(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet);

    double computePairwiseParallel(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet);

    double computePairwise(DiscreteVariable x, List<DiscreteVariable> y, DiscreteData dataSet);

    double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet);

    Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwise(List<DiscreteVariable> variables, DiscreteData dataSet);

    Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwiseParallel(List<DiscreteVariable> variables, DiscreteData dataSet);

    double computeConditional(Function dist, DiscreteVariable condVar);

    double computeConditional(Function dist, List<DiscreteVariable> condVars);
}
