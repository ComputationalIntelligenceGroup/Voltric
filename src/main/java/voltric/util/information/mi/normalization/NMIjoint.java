package voltric.util.information.mi.normalization;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagation;
import voltric.potential.Function;
import voltric.util.Utils;
import voltric.util.information.entropy.BnFactorizationEntropy;
import voltric.util.information.entropy.Entropy;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by equipo on 25/07/2017.
 */
public class NMIjoint implements NormalizationFactor {

    @Override
    public double partialNormalizeMI(double mi, double px, double py, double pxy) {
        double partialEntropy = Entropy.computePartialValue(pxy);
        return mi / partialEntropy;
    }

    @Override
    public double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet) {
        // joint variables
        ArrayList<DiscreteVariable> xy = new ArrayList<>();
        xy.addAll(x);
        xy.addAll(y);

        DiscreteData data = dataSet.project(xy);
        double jointEntropy = Entropy.compute(data);
        return mi / jointEntropy;
    }

    /******************************************************************************************************************/

    @Override
    public double normalizeMI(double mi, List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet, CliqueTreePropagation ctp) {

        // joint variables
        ArrayList<DiscreteVariable> xy = new ArrayList<>();
        xy.addAll(x);
        xy.addAll(y);

        double jointEntropy = BnFactorizationEntropy.compute(xy, ctp, dataSet);
        return mi / jointEntropy;
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

        double jointEntropy = Entropy.compute(dist);
        return mi / jointEntropy;
    }

    // Este caso, al normalizar con la entropia conjunta, no necesita de que le indiquen que variables componen X e Y
    // pero se mantiene por cohesion con las demas clases que heredan del interfaz
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

        double jointEntropy = Entropy.compute(dist);
        return mi / jointEntropy;
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

        double jointCondEntropy = Entropy.computeConditional(dist, condVar);
        return cmi / jointCondEntropy;
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

        double jointCondEntropy = Entropy.computeConditional(dist, condVars);
        return cmi / jointCondEntropy;
    }
}
