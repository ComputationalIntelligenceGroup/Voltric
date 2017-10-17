package voltric.clustering.multiview.attribute;

import voltric.data.DiscreteData;
import voltric.learning.parameter.em.ParallelEM;
import voltric.model.HLCM;
import voltric.model.creator.HlcmCreator;
import voltric.potential.Function;
import voltric.util.SymmetricPair;
import voltric.util.empiricaldist.StatelessEmpDistComputer;
import voltric.util.stattest.discrete.DiscreteStatisticalTest;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO: Es importante recordar que los scores no tienen porque ser simetricos como la MI, puede ser por ejemplo
 * la KL divergencia. Hay que cambiar el codigo entonces.
 *
 */
public class KeivaniFinder implements AttributeGrouping {

    private ParallelEM parameterLearning;

    private DiscreteStatisticalTest statisticalTest;

    public KeivaniFinder(ParallelEM parameterLearning, DiscreteStatisticalTest statisticalTest){
        this.parameterLearning = parameterLearning;
        this.statisticalTest = statisticalTest;
    }

    @Override
    public ParallelEM getParameterLearning() {
        return parameterLearning;
    }

    @Override
    public DiscreteStatisticalTest getStatisticalTest() {
        return statisticalTest;
    }

    @Override
    public List<HLCM> find(DiscreteData dataSet) {

        if(dataSet.getVariables().size() < 3)
            throw new IllegalArgumentException("Not enough attributes in the dataSet.");

        // Initialize the Map of HLCMs, where the their roots act as map's keys
        Map<DiscreteVariable, HLCM> islands = new HashMap<>();

        // Create the set of variables that will be grouped in subsets
        List<DiscreteVariable> variableSet = new ArrayList<>(dataSet.getVariables());

        // The set of pair scores used to make the groups
        Map<DiscreteVariable, Map<DiscreteVariable, Double>> pairScores = new HashMap<>();

        while(variableSet.stream().filter(x->x.isManifestVariable()).count() > 0){

            // Computes all the pairScores between the attributes of the dataSet
            if(pairScores.isEmpty())
                pairScores = statisticalTest.computePairwise(variableSet, dataSet);

            // The best pair will contain at least one Manifest variable (dataSet attribute)
            SymmetricPair<DiscreteVariable, DiscreteVariable> bestPair = findBestPair(variableSet, pairScores);

            // If Both variables are manifest a cluster is created
            if(bestPair.getFirst().isManifestVariable() && bestPair.getSecond().isManifestVariable()){

                List<DiscreteVariable> clusterSet = new ArrayList<>();

                // The best pair's variables are added to the cluster set and removed from the variable set
                clusterSet.add(bestPair.getFirst());
                clusterSet.add(bestPair.getSecond());
                variableSet.remove(bestPair.getFirst());
                variableSet.remove(bestPair.getSecond());

                // A new LCM is created with both MVs
                HLCM newCluster = HlcmCreator.createLCM(clusterSet, 2);
                newCluster = (HLCM) parameterLearning.learnModel(newCluster, dataSet).getBayesianNetwork(); // La cardinalidad "local" deberia ser estimada en este paso
                islands.put(newCluster.getRoot().getVariable(), newCluster);

                // The LV that acts as the LCM's root is added to the variableSet
                variableSet.add(newCluster.getRoot().getVariable());

                // The pair scores between the new LV and the remaining MVs in the variableSet are calculated
                // Note: scores between LVs will not be calculated
                updatePairScoresWithLV(
                        pairScores,
                        newCluster,
                        variableSet.stream().filter(x->x.isManifestVariable()).collect(Collectors.toList()),
                        dataSet);

            // Else if one of them is a LV, the MV will be added to its associated LCM
            }else{

                // The LV will be the root of one of the stored LCMs
                DiscreteVariable latentVariable = bestPair.getFirst().isLatentVariable() ? bestPair.getFirst() : bestPair.getSecond();
                // The MV will be added to the previous LCM
                DiscreteVariable manifestVariable = bestPair.getFirst().isManifestVariable() ? bestPair.getFirst() : bestPair.getSecond();

                // The collection of variables that will be part of the new LCM is created
                List<DiscreteVariable> clusterSet = new ArrayList<>();
                clusterSet.addAll(islands.get(latentVariable).getLeafNodes().stream().map(x->x.getVariable()).collect(Collectors.toList()));
                clusterSet.add(manifestVariable);
                // The variableSet is updated by removing the MV
                variableSet.remove(manifestVariable);

                // A new LCM is created containing all the previous MVs plus the new one
                HLCM newCluster = HlcmCreator.createLCM(clusterSet, 2);
                newCluster = (HLCM) parameterLearning.learnModel(newCluster, dataSet).getBayesianNetwork(); // La cardinalidad "local" deberia ser estimada en este paso
                // This new LCM replaces the old one
                islands.put(newCluster.getRoot().getVariable(), newCluster);
                islands.remove(latentVariable);
            }
        }

        return new ArrayList<>(islands.values());
    }

    private SymmetricPair<DiscreteVariable, DiscreteVariable> findBestPair(List<DiscreteVariable> variableSet,
                                                                           Map<DiscreteVariable, Map<DiscreteVariable, Double>> pairScores){

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

    // A filtered version of the variableSet is passed
    //TODO: Aqui habria que modificar para permitir statistical tests no simetricos
    private void updatePairScoresWithLV(Map<DiscreteVariable, Map<DiscreteVariable, Double>> pairScores,
                                        HLCM hlcm,
                                        List<DiscreteVariable> manifestVariableSet,
                                        DiscreteData dataSet){

        Map<DiscreteVariable, Double> hlcmRootScores = new HashMap<>();

        //Calculate the pairScores between the latentVariable and all the Manifest Variables that are still on the variableSet
        for(DiscreteVariable manifestVariable: manifestVariableSet){
            Function empDist = StatelessEmpDistComputer.computeEmpDist(hlcm.getRoot().getVariable(), manifestVariable, hlcm, dataSet);
            double scoreLM = statisticalTest.computePairwise(empDist);
            double scoreML = statisticalTest.computePairwise(empDist);
            hlcmRootScores.put(manifestVariable, scoreLM);
            pairScores.get(manifestVariable).put(hlcm.getRoot().getVariable(), scoreML);
        }

        pairScores.put(hlcm.getRoot().getVariable(), hlcmRootScores);
    }
}
