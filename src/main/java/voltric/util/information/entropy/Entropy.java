package voltric.util.information.entropy;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.potential.Function;
import voltric.util.Utils;
import voltric.variables.DiscreteVariable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by fernando on 15/04/17.
 */
public class Entropy {

    /**
     * Returns the entropy of the specified distribution.
     *
     * Note: works even for more than one-dimensional distributions.
     *
     * @param dist the distribution whose entropy is to be computed.
     * @return the entropy of the specified distribution.
     */
    public static double compute(Function dist) {

        // ensure the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // H(X) = - sum_X P(X) log P(X)
        double ent = 0.0;
        for (double cell : dist.getCells()) {
            // if P(x) = 0, skip this term
            if (cell != 0.0) {
                ent -= cell * Math.log(cell);
            }
        }

        return -ent;
    }

    /**
     * Returns the entropy of the specified discrete dataSet.
     *
     * @param data the discrete dataSet.
     * @return the entropy value.
     */
    public static double compute(DiscreteData data){
        double entropy = 0.0;
        for(DiscreteDataInstance instance: data.getInstances()){
            double instanceProbability = data.getWeight(instance) / data.getTotalWeight();
            entropy += instanceProbability * Math.log(instanceProbability);
        }
        return -entropy;
    }

    /**
     * Returns the conditional entropy for the specified distribution. This distribution may have more than 1 dimension.
     *
     * @param dist the distribution whose conditional entropy is to be computed.
     * @param condVar the conditioning variables.
     * @return the entropy of te specified distribution.
     */
    // Podriamos calcularlo mediante la formula H(X|Y) = - sum_XY P(X,Y) log P(X,Y)/ P(Y)
    // El problema es que no entiendo 100% como se organiza Function. Habria que hacer un producto cartesiano de los
    // valores de X e Y (creo)

    // Por ello, ante la duda utilizo la formula de la regla de la cadena H(X|Y) =  H(X,Y) - H(Y)
    public static double computeConditional(Function dist, DiscreteVariable condVar){

        // ensure the distribution contains the conditional variable
        if(!dist.contains(condVar))
            throw new IllegalArgumentException("The argument distribution does not contain the conditional variable");
        // ensure the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // First the condVar is filtered to select X
        // X == all the conditioned vars
        List<DiscreteVariable> x = dist.getVariables().stream()
                .filter(var -> condVar.equals(var))
                .collect(Collectors.toList());

        // Y == all the conditioning vars
        Function py = dist.sumOut(x);
        double Hxy = Entropy.compute(dist); // joint entropy
        double Hy = Entropy.compute(py);

        return Hxy - Hy;
    }

    /**
     * Returns the conditional entropy for the specified distribution. The conditioning variables are passed
     * in the {@code condVars} list
     *
     * @param dist the distribution whose conditional entropy is to be computed.
     * @param condVars the conditioning variables.
     * @return the entropy of the specified distribution.
     */
    // Podriamos calcularlo mediante la formula H(X|Y) = - sum_XY P(X,Y) log P(X,Y)/ P(Y)
    // El problema es que no entiendo 100% como se organiza Function. Habria que hacer un producto cartesiano de los
    // valores de X e Y (creo)

    // Por ello, ante la duda utilizo la formula de la regla de la cadena H(X|Y) =  H(X,Y) - H(Y) iendo Y el conjunto de variables condicionantes

    //condVars puede ser alguna de las variables condicionadas
    public static double computeConditional(Function dist, List<DiscreteVariable> condVars){

        // ensure the distribution contains the conditioning variables
        if(!dist.containsAll(condVars))
            throw new IllegalArgumentException("The argument distribution does not contain all the conditioning variables");
        // ensure the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // First the condVars are filtered to select X
        // X == all the conditioned vars
        List<DiscreteVariable> x = dist.getVariables().stream().filter(var -> !condVars.contains(var)).collect(Collectors.toList());

        // Y == all the conditioning vars
        Function py = dist.sumOut(x);
        double Hxy = Entropy.compute(dist); // joint entropy
        double Hy = Entropy.compute(py);

        return Hxy - Hy;
    }

    /**
     * Returns the sum of the individual entropies that belong to each data's variable.
     *
     * @param data the discrete dataSet.
     * @return the sum of individual entropies.
     */
    public static double computeSumOfIndividualEntropies(DiscreteData data){
        double sumOfEntropies = 0.0;
        for(DiscreteVariable variable: data.getVariables())
            // Computes the entropy of the data being projected to the variable's dimension and adds it to the sum
            sumOfEntropies += Entropy.compute(data.project(variable));
        return sumOfEntropies;
    }

    /**
     * Returns the entropy value for a specific probability.
     *
     * @param probability the probability value.
     * @return the partial entropy value.
     */
    public static double computePartialValue(double probability){
        return probability * Math.log(probability);
    }

}
