package voltric.util.information.entropy;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.inference.CliqueTreePropagation;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculo de la entropia a partir de una distribucion conjunta factorizada utilizando una red bayesiana.
 */
public class BnFactorizationEntropy {

    public static double compute(DiscreteVariable x, CliqueTreePropagation ctp, DiscreteData data){
        if(!ctp.getBayesNet().getVariables().contains(x))
            throw new IllegalArgumentException("The variable x should exist in the ctp");

        DiscreteData projectedData = data.project(x);

        double entropy = 0;
        for (DiscreteDataInstance dataCase : projectedData.getInstances()) {
            ctp.setEvidence(projectedData.getVariables(), dataCase.getNumericValues());
            double instanceProbability = ctp.propagate();
            entropy += instanceProbability * Math.log(instanceProbability);
        }
        return -entropy;
    }

    public static double compute(List<DiscreteVariable> x, CliqueTreePropagation ctp, DiscreteData data){

        if(!ctp.getBayesNet().getVariables().containsAll(x))
            throw new IllegalArgumentException("All the variables inside x should exist in the ctp");

        DiscreteData projectedData = data.project(x);

        double entropy = 0;
        for (DiscreteDataInstance dataCase : projectedData.getInstances()) {
            ctp.setEvidence(projectedData.getVariables(), dataCase.getNumericValues());
            double instanceProbability = ctp.propagate();
            entropy += instanceProbability * Math.log(instanceProbability);
        }
        return -entropy;
    }

    public static double computeSumOfIndividualEntropies(List<DiscreteVariable> x, CliqueTreePropagation ctp, DiscreteData data){

        if(!ctp.getBayesNet().getVariables().containsAll(x))
            throw new IllegalArgumentException("All the variables inside x should exist in the ctp");

        DiscreteData projectedData = data.project(x);

        double sumOfEntropies = 0.0;
        for(DiscreteVariable variable: data.getVariables())
            // Computes the entropy of the data being projected to the variable's dimension and adds it to the sum
            sumOfEntropies += BnFactorizationEntropy.compute(variable, ctp, data);
        return sumOfEntropies;
    }

    // TODO: Ahora que sabemos como obtener las probabilidades es conveniente probar si es mas rapida la formula de forma directa (ver wikipedia)
    public static double computeConditional(List<DiscreteVariable> vars, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData data){

        // La BN debe contener todas las variables tanto condicionadas como condicionantes
        if(!ctp.getBayesNet().containsVars(vars) || !ctp.getBayesNet().containsVars(condVars))
            throw new IllegalArgumentException("The BN must contain all the argument variables");

        if(!data.getVariables().containsAll(vars) || !data.getVariables().containsAll(condVars))
            throw new IllegalArgumentException("The dataSet must contains all the argument variables");

        // XY represents all the argument variables (without repetitions)
        List<DiscreteVariable> nonRepeatedVars = new ArrayList<>();
        vars.stream().filter(var-> !nonRepeatedVars.contains(var)).forEach(nonRepeatedVars::add);
        condVars.stream().filter(var-> !nonRepeatedVars.contains(var)).forEach(nonRepeatedVars::add);

        double Hxy = BnFactorizationEntropy.compute(nonRepeatedVars, ctp, data); // H(X,Y)
        double Hy = BnFactorizationEntropy.compute(condVars, ctp, data); // H(Y)

        return Hxy - Hy;
    }

    public static double computeConditional(DiscreteVariable var, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData data){
        List<DiscreteVariable> variables = new ArrayList<>();
        variables.add(var);
        return computeConditional(variables, condVars, ctp, data);
    }
}
