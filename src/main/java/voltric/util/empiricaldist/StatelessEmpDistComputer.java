package voltric.util.empiricaldist;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.inference.CliqueTreePropagation;
import voltric.model.DiscreteBayesNet;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO: He eliminado Arrays.binarySearch ya que no estoy seguro de que los arrays esten ordenados (lo cual es condicion necesaria)
 * para su correcto funcionamiento
 */
public class StatelessEmpDistComputer {

    /** TODO: revisar si es realmente la conjunta la distribucion que devuelve
     * This method returns the joint distribution of the argument variables. This method allows both latent and manifest variables.
     * All latent variables must belong to the bayesNet and all the manifest variables must belong to the bayesNet and to
     * the dataSet.
     *
     * @param vFirst the first discrete variable.
     * @param vSecond the second discrete variable.
     * @param bayesNet the bayesNet where the latent variables reside.
     * @param dataSet the dataSet used to obtain the posterior probabilities of the latent variables.
     * @return a Function that represents the joint distribution between the argument variables.
     */
    public static Function computeEmpDist(DiscreteVariable vFirst, DiscreteVariable vSecond, DiscreteBayesNet bayesNet, DiscreteData dataSet){

        // Both variables must belong to the bayesNet
        if(!bayesNet.containsVar(vFirst) || !bayesNet.containsVar(vSecond))
            throw new IllegalArgumentException("Both variables must belong to the bayesian network");

        // The latent variables are separated
        List<DiscreteVariable> latentVariables = new ArrayList<>();
        if(vFirst.isLatentVariable()) latentVariables.add(vFirst);
        if(vSecond.isLatentVariable()) latentVariables.add(vSecond);

        // The manifest variables that belong to the dataSet are identified
        List<DiscreteVariable> manifestVariables = bayesNet.getManifestVariables();

        // To store the LV's posterior probabilities, a Map is created, where a function is assigned to each data case
        Map<DiscreteVariable, Map<DiscreteDataInstance, Function>> latentPosts = new HashMap<>();
        for(DiscreteVariable latentVar: latentVariables)
            latentPosts.put(latentVar, new HashMap<>());
        // The Map is then properly filled
        createLatentPosts(latentPosts, dataSet, bayesNet);

        // An 'empty' function is created using both variables
        List<DiscreteVariable> variablePairList = new ArrayList<>();
        variablePairList.add(vFirst);
        variablePairList.add(vSecond);
        Function empDist = Function.createFunction(variablePairList);

        // The empirical distribution is created:
        for (DiscreteDataInstance dataCase : dataSet.getInstances()) {
            int[] states = dataCase.getNumericValues();

            // If there is missing data, continue;
            // TODO: Revisar, caso para datos incompletos donde se ignora la instancia
            /*if ((viIdx != -1 && states[viIdx] == -1)
                    || (vjIdx != -1 && states[vjIdx] == -1)) {
                continue;
            }*/

            // P(vFirst, vSecond|d) = P(vFirst|d) * P(vSecond|d)
            Function freq;

            if(vFirst.isManifestVariable())
                freq = Function.createIndicatorFunction(vFirst, states[manifestVariables.indexOf(vFirst)]);
            else // vFirst is a latent variable
                freq = latentPosts.get(vFirst).get(dataCase);

            if(vSecond.isManifestVariable())
                freq = freq.times(Function.createIndicatorFunction(vSecond, states[manifestVariables.indexOf(vSecond)]));
            else // vSecond is a latent variable
                freq = freq.times(latentPosts.get(vSecond).get(dataCase));

            freq = freq.times(dataSet.getWeight(dataCase));
            empDist.plus(freq);
        }

        empDist.normalize();

        return empDist;
    }

    // TODO: Comprobar que funciona correctamente y que genera la distribucion conjunta
    public static Function computeEmpDist(List<DiscreteVariable> variables, DiscreteBayesNet bayesNet, DiscreteData dataSet){

        if(variables.size() == 0)
            throw new IllegalArgumentException("variables cannot be empty");

        if(!bayesNet.containsVars(variables))
            throw new IllegalArgumentException("All variables must belong to the Bayesian network");

        // The latent variables are filtered
        List<DiscreteVariable> latentVariables = variables.stream().filter(x->x.isLatentVariable()).collect(Collectors.toList());

        // The manifest variables that belong to the dataSet are identified and a new Array is created
        List<DiscreteVariable> manifestVariables = bayesNet.getManifestVariables();

        // To store the LV's posterior probabilities, a Map is created, where a function is assigned to each data case
        Map<DiscreteVariable, Map<DiscreteDataInstance, Function>> latentPosts = new HashMap<>();
        for(DiscreteVariable latentVar: latentVariables)
            latentPosts.put(latentVar, new HashMap<>());
        // The Map is then properly filled
        createLatentPosts(latentPosts, dataSet, bayesNet);

        // An 'empty' function is created
        Function empDist = Function.createFunction(variables);

        // The empirical distribution is created:
        for (DiscreteDataInstance dataCase : dataSet.getInstances()) {
            int[] states = dataCase.getNumericValues();

            // If there is missing data, continue;
            // TODO: Revisar, caso para datos incompletos donde se ignora la instancia
            /*if ((viIdx != -1 && states[viIdx] == -1)
                    || (vjIdx != -1 && states[vjIdx] == -1)) {
                continue;
            }*/

            // P(vFirst, vSecond|d) = P(vFirst|d) * P(vSecond|d)
            Function freq = null;

            for(DiscreteVariable variable: variables){
                // Special case for the first variable
                if(freq == null){
                    if(variable.isManifestVariable())
                        freq = Function.createIndicatorFunction(variable, states[manifestVariables.indexOf(variable)]);
                    else // vFirst is a latent variable
                        freq = latentPosts.get(variable).get(dataCase);

                    // All the subsequent variables
                }else{
                    if(variable.isManifestVariable())
                        freq = freq.times(Function.createIndicatorFunction(variable, states[manifestVariables.indexOf(variable)]));
                    else // vSecond is a latent variable
                        freq = freq.times(latentPosts.get(variable).get(dataCase));
                }
            }
            freq = freq.times(dataSet.getWeight(dataCase));
            empDist.plus(freq);
        }
        empDist.normalize();

        return empDist;
    }

    // Este metodo sirve por si tenemos varias BNs con LVs y queremos crear un latent Posts con datos de todas ellas, una a una
    public static Map<DiscreteVariable, Map<DiscreteDataInstance, Function>> createLatentPosts(DiscreteBayesNet bayesNet, DiscreteData dataSet){

        // To store the LV's posterior probabilities, a Map is created, where a function is assigned to each data case
        Map<DiscreteVariable, Map<DiscreteDataInstance, Function>> latentPosts = new HashMap<>();
        for(DiscreteVariable latentVar: bayesNet.getLatentVariables())
            latentPosts.put(latentVar, new HashMap<>());

        CliqueTreePropagation ctp = new CliqueTreePropagation(bayesNet);

        List<DiscreteVariable> manifestVars = bayesNet.getManifestVariables();

        for(DiscreteDataInstance dataCase : dataSet.getInstances()){
            // Project dataCase to the manifest variables space
            DiscreteDataInstance projectedDataCase = dataCase.project(manifestVars);

            // set evidence and propagate
            ctp.setEvidence(manifestVars, projectedDataCase.getNumericValues());
            ctp.propagate();

            // for each of the chosen latent variables
            for(DiscreteVariable latentVar : latentPosts.keySet()){
                // compute P(Y|d)
                Function post = ctp.computeBelief(latentVar);
                Map<DiscreteDataInstance, Function> localLatentPosts = latentPosts.get(latentVar);
                localLatentPosts.put(dataCase, post);
            }
        }

        return latentPosts;
    }

    // Este metodo sirve para crear una conjunta  a partir de LVs de varias BNs (que se han generado con subsets del dataSet) y un subset del dataSet
    // su aplicacion actual es para clacular MI entre una LV y y un conjunto de MVs que pertenecen a diferentes particiones sin generar un modelo
    // multidimensional
    public static Function computeEmpDist(List<DiscreteVariable> variables, DiscreteData dataSet, Map<DiscreteVariable, Map<DiscreteDataInstance, Function>> latentPosts){

        List<DiscreteVariable> manifestVars = variables.stream().filter(var -> var.isManifestVariable()).collect(Collectors.toList());
        List<DiscreteVariable> latentVars = variables.stream().filter(var -> var.isLatentVariable()).collect(Collectors.toList());

        if(!dataSet.getVariables().containsAll(manifestVars))
            throw new IllegalArgumentException("All the manifest variables must be present in the DataSet");

        if(!latentPosts.keySet().containsAll(latentVars))
            throw new IllegalArgumentException("All the latent variables must have a set of posterior probabilities in 'latentPosts'");

        // An 'empty' function is created
        Function empDist = Function.createFunction(variables);

        // The empirical distribution is created:
        for (DiscreteDataInstance dataCase : dataSet.getInstances()) {
            int[] states = dataCase.getNumericValues();

            // P(vFirst, vSecond|d) = P(vFirst|d) * P(vSecond|d)
            Function freq = null;

            for(DiscreteVariable variable: variables){
                // Special case for the first variable
                if(freq == null){
                    if(variable.isManifestVariable())
                        freq = Function.createIndicatorFunction(variable, states[manifestVars.indexOf(variable)]);
                    else // vFirst is a latent variable
                        freq = latentPosts.get(variable).get(dataCase);

                    // All the subsequent variables
                }else{
                    if(variable.isManifestVariable())
                        freq = freq.times(Function.createIndicatorFunction(variable, states[manifestVars.indexOf(variable)]));
                    else // vSecond is a latent variable
                        freq = freq.times(latentPosts.get(variable).get(dataCase));
                }
            }
            freq = freq.times(dataSet.getWeight(dataCase));
            empDist.plus(freq);
        }
        empDist.normalize();

        return empDist;
    }

    //Solo vale para variables MV
    public static Function computeEmpDist(List<DiscreteVariable> manifestVariables, DiscreteData dataSet){

        if(manifestVariables.stream().filter(x->x.isLatentVariable()).collect(Collectors.toList()).size() > 0)
            throw new IllegalArgumentException("Only Manifest variables are allowed");

        // An 'empty' function is created
        Function empDist = Function.createFunction(manifestVariables);

        // The empirical distribution is created:
        for (DiscreteDataInstance dataCase : dataSet.getInstances()) {
            int[] states = dataCase.getNumericValues();

            Function freq = null;

            for(DiscreteVariable variable: manifestVariables){
                // Special case for the first variable
                if(freq == null)
                        freq = Function.createIndicatorFunction(variable, states[manifestVariables.indexOf(variable)]);
                // All the subsequent variables
                else
                    freq = freq.times(Function.createIndicatorFunction(variable, states[manifestVariables.indexOf(variable)]));

            }
            freq = freq.times(dataSet.getWeight(dataCase));
            empDist.plus(freq);
        }
        empDist.normalize();

        return empDist;
    }

    /**
     * This method fills the {@code latentPosts} Map with the posterior probabilities of each latent variable
     * associated to each data case.
     *
     * Note: This Map needs to be properly initialized. Only the latent variables that appear in the Map as keys will be calculated.
     *
     * @param latentPosts the Map that is going to be filled.
     * @param dataSet the dataSet.
     * @param bayesNet the bayesNet that contains the latent variables.
     */
    private static void createLatentPosts(Map<DiscreteVariable, Map<DiscreteDataInstance, Function>> latentPosts, DiscreteData dataSet, DiscreteBayesNet bayesNet){

        CliqueTreePropagation ctp = new CliqueTreePropagation(bayesNet);

        List<DiscreteVariable> manifestVars = bayesNet.getManifestVariables();

        for(DiscreteDataInstance dataCase : dataSet.getInstances()){
            // Project dataCase to the manifest variables space
            DiscreteDataInstance projectedDataCase = dataCase.project(manifestVars);

            // set evidence and propagate
            ctp.setEvidence(manifestVars, projectedDataCase.getNumericValues());
            ctp.propagate();

            // for each of the chosen latent variables
            for(DiscreteVariable latentVar : latentPosts.keySet()){
                // compute P(Y|d)
                Function post = ctp.computeBelief(latentVar);
                Map<DiscreteDataInstance, Function> localLatentPosts = latentPosts.get(latentVar);
                localLatentPosts.put(dataCase, post);
            }
        }
    }
}
