package voltric.util.information.mi.normalization;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagation;
import voltric.potential.Function;
import voltric.util.Utils;
import voltric.util.information.entropy.Entropy;
import voltric.variables.DiscreteVariable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by equipo on 25/07/2017.
 */
public class NMImin implements NormalizationFactor {

    @Override
    public double partialNormalizeMI(double mi, double px, double py, double pxy) {
        double xPartialEntropy = Entropy.computePartialValue(px);
        double yPartialEntropy = Entropy.computePartialValue(py);
        return mi /(Math.min(xPartialEntropy, yPartialEntropy));
    }

    @Override
    public double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet) {

        DiscreteData dataX = dataSet.project(x);
        DiscreteData dataY = dataSet.project(y);

        double xEntropy = Entropy.compute(dataX);
        double yEntropy = Entropy.compute(dataY);
        return mi / (Math.min(xEntropy, yEntropy));
    }

    /******************************************************************************************************************/

    @Override
    public double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet, CliqueTreePropagation ctp) {
        return 0;
    }

    @Override
    public double normalizeCMI(double cmi, List<DiscreteVariable> x, List<DiscreteVariable> y, List<DiscreteVariable> condVars, DiscreteData dataSet, CliqueTreePropagation ctp) {
        return 0;
    }

    /******************************************************************************************************************/

    @Override
    public double normalizeMI(double mi, Function dist) {

        // Ensure that the distribution contains at least a pair of variables
        if(dist.getDimension() != 2)
            throw new IllegalArgumentException("The argument function's dimension must be 2");
        // Ensure that the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // The two marginal distributions
        Function marginal1 = dist.sumOut(dist.getVariables().get(1));
        Function marginal2 = dist.sumOut(dist.getVariables().get(0));

        double entropy1 = Entropy.compute(marginal1);
        double entropy2 = Entropy.compute(marginal2);

        return mi / (Math.min(entropy1, entropy2));
    }

    @Override
    public double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, Function dist) {

        Set<DiscreteVariable> nonRepeatedSetOfVariables = new HashSet<>();
        nonRepeatedSetOfVariables.addAll(x);
        nonRepeatedSetOfVariables.addAll(y);

        // Ensure that the distribution contains all the variables that compose X & Y
        if(dist.getDimension() != (nonRepeatedSetOfVariables.size()))
            throw new IllegalArgumentException("The argument function's dimension must be equal to the size of the  set of non-repeated variables (" + nonRepeatedSetOfVariables.size() + ")");
        // Ensure that the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        /** Given that X & Y may have crossed variables, a filter needs to be made where the variables not present in them are summed out */
        List<DiscreteVariable> notPresentInY = dist.getVariables().stream().filter(var -> !y.contains(var)).collect(Collectors.toList());
        List<DiscreteVariable> notPresentInX = dist.getVariables().stream().filter(var -> !x.contains(var)).collect(Collectors.toList());

        Function px = dist.sumOut(notPresentInX);
        Function py = dist.sumOut(notPresentInY);

        double Hx = Entropy.compute(px);
        double Hy = Entropy.compute(py);

        return mi / (Math.min(Hx, Hy));
    }

    @Override
    public double normalizeCMI(double cmi, Function dist, DiscreteVariable condVar) {

        // Ensure that the distribution contains three variables
        if(dist.getDimension() != 3)
            throw new IllegalArgumentException("the argument function's dimension must be 3");
        // Ensure that the distribution contains the conditional variable
        if(!dist.contains(condVar))
            throw new IllegalArgumentException("The argument distribution does not contain the conditional variable");
        // Ensure that the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // First the condVar is filtered to select x & y
        List<DiscreteVariable> xy = dist.getVariables().stream()
                .filter(var -> !condVar.equals(var))
                .collect(Collectors.toList());

        Function pxz = dist.sumOut(xy.get(1)); // Y is summed out of the dist
        Function pyz = dist.sumOut(xy.get(0)); // X is summed out of the dist

        double condEntropyX = Entropy.computeConditional(pxz, condVar);
        double condEntropyY = Entropy.computeConditional(pyz, condVar);

        return cmi / (Math.min(condEntropyX, condEntropyY));
    }

    @Override
    public double normalizeCMI(double cmi, Function dist, List<DiscreteVariable> condVars) {

        // Ensure that the distribution contains at least (2 + condVars.size) variables
        if(dist.getDimension() != 2 + condVars.size())
            throw new IllegalArgumentException("the argument function's dimension must be " + (2 + condVars.size() + " (X and the conditioning variables)"));
        // Ensure that the distribution contains the conditioning variables
        if(!dist.containsAll(condVars))
            throw new IllegalArgumentException("The argument distribution does not contain all the conditioning variables");
        // Ensure that the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // First the condVar is filtered to select x & y
        List<DiscreteVariable> xy = dist.getVariables().stream()
                .filter(var -> !condVars.contains(var))
                .collect(Collectors.toList());

        Function pxz = dist.sumOut(xy.get(1)); // Y is summed out of the dist
        Function pyz = dist.sumOut(xy.get(0)); // X is summed out of the dist

        double condEntropyX = Entropy.computeConditional(pxz, condVars);
        double condEntropyY = Entropy.computeConditional(pyz, condVars);

        return cmi / (Math.min(condEntropyX, condEntropyY));
    }
}