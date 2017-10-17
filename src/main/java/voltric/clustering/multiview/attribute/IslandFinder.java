package voltric.clustering.multiview.attribute;

import voltric.clustering.multiview.attribute.stopcondition.StopCondition;
import voltric.data.DiscreteData;
import voltric.learning.parameter.em.ParallelEM;
import voltric.model.HLCM;
import voltric.model.creator.HlcmCreator;
import voltric.util.SymmetricPair;
import voltric.util.stattest.discrete.DiscreteStatisticalTest;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Es importante recordar que los scores no tienen porque ser simetricos como la MI, puede ser por ejemplo
 * la KL divergencia, asi que el codigo debe estar programado con ello en mente, ergo eliminar los SymmetricPairs
 * (en los scores) a menos que se utilice unicamente la MI.
 */
public class IslandFinder implements AttributeGrouping{

    private ParallelEM parameterLearning;

    private StopCondition stopCondition;

    private DiscreteStatisticalTest statisticalTest;

    private int maxNumberOfGroups;

    // TODO: No me queda aun clara la jerarquia de Parameter learning algorithms, asi que fuerzo a que sea un ParallelEM
    public IslandFinder(ParallelEM parameterLearning, StopCondition stopCondition, DiscreteStatisticalTest statisticalTest, int maxNumberOfGroups){
        this.parameterLearning = parameterLearning;
        this.stopCondition = stopCondition;
        this.statisticalTest = statisticalTest;
        this.maxNumberOfGroups = maxNumberOfGroups;
    }

    @Override
    public ParallelEM getParameterLearning() {
        return parameterLearning;
    }

    @Override
    public DiscreteStatisticalTest getStatisticalTest() {
        return statisticalTest;
    }

    public List<HLCM> find(DiscreteData dataSet){
        if(dataSet.getVariables().size() < 3)
            throw new IllegalArgumentException("Not enough attributes in the dataSet.");

        // Initialize the list of HLCMs that will be returned
        List<HLCM> islands = new ArrayList<>();

        // Create the set of variables that will be grouped in subsets
        List<DiscreteVariable> variableSet = new ArrayList<>(dataSet.getVariables());

        // The set of pair scores uset to make the groups
        Map<DiscreteVariable, Map<DiscreteVariable, Double>> pairScores = new HashMap<>();

        // Mientras siga habiendo variables en variableSet
        while(variableSet.size() > 0){
            // Si variableSet == 3 formamos un LCM y palante
            if(variableSet.size() == 3){
                // Creates a LCM with cardinality 2 and adds it to the collection of islands
                HLCM lcm = HlcmCreator.createLCM(variableSet, 2);
                islands.add(lcm);
                return islands;
                //TODO: si es menor que 3 deberian repartirse entre las demas islas o algo asi
            //
            }else if (variableSet.size() < 3){
                // Aplicamos un paso del algoritmo de Keivani
            }
            //
            if(pairScores.isEmpty())
                pairScores = statisticalTest.computePairwise(variableSet, dataSet);

            //
            SymmetricPair<DiscreteVariable, DiscreteVariable> bestPair = findBestPair(variableSet, pairScores);

            // Once we have found the best pair, a cluster of variables is made, representing the new island
            List<DiscreteVariable> clusterSet = new ArrayList<>();
            // The best pair's variables are added to the cluster set and removed from the variable set
            clusterSet.add(bestPair.getFirst());
            clusterSet.add(bestPair.getSecond());
            variableSet.remove(bestPair.getFirst());
            variableSet.remove(bestPair.getSecond());

            // Ahora seguimos añadiendo hasta que se deje de cumplir la condicion de ejecucion (si se deja de añadir mas variables al cluster)
            do{
                // After this, the closest variable to clusterSet is added. Then it is removed from variableSet.
                DiscreteVariable closestVariableToClusterSet = findClosestVariableToCluster(variableSet, clusterSet, pairScores);
                clusterSet.add(closestVariableToClusterSet);
                variableSet.remove(closestVariableToClusterSet);

            }while (stopCondition.isTrue() && variableSet.size() > 0); // Se cumple el UD_test y siguen quedando variables que añadir
            //Se puede sustituir por test de parada y en vez de llamar a isTrue, crear una boolean cuyo valor cambia segun el resultado del test y algun otro factor

            // Once the loop has ended, a new island is created with the clusterSet's variables
            HLCM lcm = HlcmCreator.createLCM(variableSet, 2);
            islands.add(lcm);
        }

        return islands;
    }

    private SymmetricPair<DiscreteVariable, DiscreteVariable> findBestPair(List<DiscreteVariable> variableSet, Map<DiscreteVariable, Map<DiscreteVariable, Double>> pairScores){

        double maxScore = Double.NEGATIVE_INFINITY;
        DiscreteVariable first = null;
        DiscreteVariable second = null;

        // Enumerate all pairs of variables
        for (int i = 0; i < variableSet.size(); i++) {
            DiscreteVariable vi = variableSet.get(i);
            for (int j = i + 1; j < variableSet.size(); j++) {
                DiscreteVariable vj = variableSet.get(j);
                double score = pairScores.get(vi).get(vj);

                if(score > maxScore){
                    maxScore = score;
                    first = vi;
                    second = vj;
                }
            }
        }

        return new SymmetricPair<>(first, second);
    }

    // TODO: Comprobar como funciona el algoritmo de Liu => compara con cada MV o con la LV
    private DiscreteVariable findClosestVariableToCluster(List<DiscreteVariable> variableSet, List<DiscreteVariable> clusterSet, Map<DiscreteVariable, Map<DiscreteVariable, Double>> pairScores){
        double maxScore = Double.NEGATIVE_INFINITY;
        DiscreteVariable closestVariableToCluster = null;

        for(DiscreteVariable inCluster: clusterSet)
            for(DiscreteVariable outCluster: variableSet){
                double score = pairScores.get(inCluster).get(outCluster);
                if(score > maxScore){
                    maxScore = score;
                    closestVariableToCluster = outCluster;
                }
            }

        return closestVariableToCluster;
    }
}
