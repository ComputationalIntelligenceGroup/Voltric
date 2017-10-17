package voltric.util.information.mi;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagation;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Esta clase actua como fachada de los metodos
 *
 * TODO: Revisar los metodos
 * TODO: IMPORTANTE, todavia queda todo el tema del apartado de analisis y las CMI y combinaciones de MI
 *
 * TODO: No puedo utilizar symmetric porque los edges son siempre dirigidos, ncluso en UndirectedGraph
 */
public class MI {

    /** Using frequency-counted PDF, doesnt take into consideration conditional dependencies */

    public static double computePairwise(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet){
        return FrequencyCountedMI.computePairwise(x, y, dataSet);
    }

    public static double computePairwiseParallel(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet){
        return FrequencyCountedMI.computePairwiseParallel(x, y, dataSet);
    }

    public static double computePairwise(DiscreteVariable x, List<DiscreteVariable> y, DiscreteData dataSet){
        List<DiscreteVariable> oneVariableList = new ArrayList<>();
        oneVariableList.add(x);
        return MI.computePairwise(oneVariableList, y, dataSet);
    }

    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet){
        return FrequencyCountedMI.computePairwise(x, y, dataSet);
    }

    public static Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwise(List<DiscreteVariable> variables, DiscreteData dataSet){
        return FrequencyCountedMI.computePairwise(variables, dataSet);
    }

    public static Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwiseParallel(List<DiscreteVariable> variables, DiscreteData dataSet){
        return FrequencyCountedMI.computePairwiseParallel(variables, dataSet);
    }

    /** Using the BN factorization for the Joint probability distribution */
    /** The most efficient one, but requires to learn a BN */

    public static double computePairwise(DiscreteVariable x, DiscreteVariable y, CliqueTreePropagation ctp, DiscreteData data){
       return BnFactorizationMI.computePairwise(x, y, ctp, data);
    }

    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, CliqueTreePropagation ctp, DiscreteData data){
        return BnFactorizationMI.computePairwise(x, y, ctp, data);
    }

    public static double computeConditional(DiscreteVariable x, DiscreteVariable y, DiscreteVariable condVar, CliqueTreePropagation ctp, DiscreteData data){
        return BnFactorizationMI.computeConditional(x, y, condVar, ctp, data);
    }

    public static double computeConditional(DiscreteVariable x, DiscreteVariable y, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData data){
        return BnFactorizationMI.computeConditional(x, y, condVars, ctp, data);
    }

    public static double computeConditional(List<DiscreteVariable> x, List<DiscreteVariable> y, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData data){
        return BnFactorizationMI.computeConditional(x, y, condVars, ctp, data);
    }

    /** Using the Joint Probability Distribution. This is worse than the frequency-counted case or the BN factorization, because returns the same results
     as the frequency counted, but it is impossible to compute when the number of variables gets too high due to its number of parameters. */
    /** Careful, this may take not be useful because the JPD could not be created due to the exponential number of parameters*/

    public static double computePairwise(Function dist){
        return JointDistributionMI.computePairwise(dist);
    }

    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, Function dist){
        return JointDistributionMI.computePairwise(x, y, dist);
    }

    public static double computeConditional(Function dist, DiscreteVariable condVar){
        return JointDistributionMI.computeConditional(dist, condVar);
    }

    // TODO: Hay que probar que el resultado es correcto
    public static double computeConditional(Function dist, List<DiscreteVariable> condVars){
        return JointDistributionMI.computeConditional(dist, condVars);
    }

    // TODO: Revisar teoria (https://en.wikipedia.org/wiki/Pointwise_mutual_information)
    // TODO: Falta un argumento: los puntos X=x e Y=y que vamos a tomar para calcularla ???
    public static double computePointwise(Function dist){
        return JointDistributionMI.computePointwise(dist);
    }

}
