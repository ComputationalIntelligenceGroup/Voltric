package voltric.util.information.mi;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.util.frequencycount.FrequencyCounter;
import voltric.util.frequencycount.ParallelFrequencyCounter;
import voltric.util.frequencycount.SequentialFrequencyCounter;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cambiar de DiscreteData a Data[DiscreteVariable], como parte de la lenta eliminación de DiscreteData
 *
 */
class FrequencyCountedMI {

    // Frequency-counted
    // TODO: Check if it works
    public static double computePairwise(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet){
        List<DiscreteVariable> variables = new ArrayList<>();
        variables.add(x);
        variables.add(y);

        if(!dataSet.getVariables().containsAll(variables) || !x.isManifestVariable() || !y.isManifestVariable())
            throw new IllegalArgumentException("Variables need to be of manifest type and belong to the DataSet");

        return computePairwise(dataSet, variables, new SequentialFrequencyCounter()) // returns Map<Map<DiscreteVariable,Double>>
                .get(x).get(y);
    }

    // Frequency-counted
    // TODO: Check if it works
    public static double computePairwiseParallel(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet){
        List<DiscreteVariable> variables = new ArrayList<>();
        variables.add(x);
        variables.add(y);

        if(!dataSet.getVariables().containsAll(variables) || !x.isManifestVariable() || !y.isManifestVariable())
            throw new IllegalArgumentException("Variables need to be of manifest type and belong to the DataSet");

        return computePairwise(dataSet, variables, new ParallelFrequencyCounter())
                .get(x).get(y);
    }

    // Frequency-counted
    /** Este es el caso múltiple donde se generan joint variables */
    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet){

        // La informacion mutua entre algo y el conjunto vacio es 0
        if(x.isEmpty() || y.isEmpty())
            return 0;

        // joint variables
        ArrayList<DiscreteVariable> xy = new ArrayList<>();
        xy.addAll(x);
        xy.addAll(y);

        // The dataSet is projected to the joint dimension of the variables
        DiscreteData xyProjectedDataSet = dataSet.project(xy);
        DiscreteData xProjectedDataSet = xyProjectedDataSet.project(x);
        DiscreteData yProjectedDataSet = xyProjectedDataSet.project(y);

        // initialization
        double mi = 0.0;
        double pxy, px, py;

        for (DiscreteDataInstance xyInstance : xyProjectedDataSet.getInstances()) {
            // Compute P(x,y)
            pxy = xyProjectedDataSet.getWeight(xyInstance) / xyProjectedDataSet.getTotalWeight();

            // Compute P(x)
            DiscreteDataInstance xInstance = xyInstance.project(x);
            xInstance.setData(xProjectedDataSet); //TODO: temporal, no se deberia asignar setData fuera del paquete de data
            px = xProjectedDataSet.getWeight(xInstance) /  xProjectedDataSet.getTotalWeight();

            // Compute P(y)
            DiscreteDataInstance yInstance = xyInstance.project(y);
            yInstance.setData(yProjectedDataSet); //TODO: temporal, no se deberia asignar setData fuera del paquete de data
            py = yProjectedDataSet.getWeight(yInstance) /  yProjectedDataSet.getTotalWeight();

            // Pair-wise Mutual Information formula (Single value)
            double localMI = pxy * Math.log(pxy / (px * py));
            mi += localMI;
        }

        return mi;
    }

    // Frequency-counted
    // TODO: Modificar processMI y/o mi frequencyCounter de ltm-learning
    public static Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwise(List<DiscreteVariable> variables, DiscreteData dataSet){
        if(!dataSet.getVariables().containsAll(variables))
            throw new IllegalArgumentException("Variables need to belong to the DataSet");

        if(dataSet.getVariables().stream().filter(x->!x.isManifestVariable()).count() > 0)
            throw new IllegalArgumentException("All the variables must be of type Manifest");

        return computePairwise(dataSet, variables, new SequentialFrequencyCounter());
    }

    // Frequency-counted
    // TODO: Modificar processMI y/o mi frequencyCounter de ltm-learning
    public static Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwiseParallel(List<DiscreteVariable> variables, DiscreteData dataSet){
        if(!dataSet.getVariables().containsAll(variables))
            throw new IllegalArgumentException("Variables need to belong to the DataSet");

        if(dataSet.getVariables().stream().filter(x->!x.isManifestVariable()).count() > 0)
            throw new IllegalArgumentException("All the variables must be of type Manifest");

        return computePairwise(dataSet, variables, new ParallelFrequencyCounter());
    }

    private static  Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwise(DiscreteData data, List<DiscreteVariable> variables, FrequencyCounter frequencyCounter){
        int numberOfVariables = variables.size();
        double totalWeight = data.getTotalWeight();
        ArrayList<double[]> f = frequencyCounter.compute(data, variables);
        ArrayList<double[]> results = new ArrayList<double[]>(numberOfVariables);


        for(int i = 0; i<numberOfVariables;i++){
            results.add(new double[numberOfVariables]);
        }

        for (int i = 0; i < numberOfVariables; i++) {
            for (int j = i + 1; j < numberOfVariables; j++) {
                double[] pi = getMarginal(f.get(i)[i] / totalWeight);
                double[] pj = getMarginal(f.get(j)[j]/ totalWeight);

                double[][] pij = new double[2][2];
                pij[1][1] = f.get(i)[j] / totalWeight;
                pij[1][0] = pi[1] - pij[1][1];
                pij[0][1] = pj[1] - pij[1][1];
                pij[0][0] = 1 - pi[1] - pj[1] + pij[1][1];

                double mi = 0;
                for (int xi = 0; xi < 2; xi++) {
                    for (int xj = 0; xj < 2; xj++) {
                        if (pij[xi][xj] > 0) {
                            mi +=
                                    pij[xi][xj]
                                            * Math.log(pij[xi][xj]
                                            / (pi[xi] * pj[xj]));
                        }
                    }
                }


                results.get(i)[j] = mi;
                results.get(j)[i] = mi;
                assert !Double.isNaN(mi);
            }
        }
        return processMi(results, variables);
    }

    // Used in computePairwise
    private static double[] getMarginal(double p_1) {
        double[] result = { 1 - p_1, p_1 };
        return result;
    }

    // Modified version of IslandFinder
    private static Map<DiscreteVariable, Map<DiscreteVariable, Double>> processMi(List<double[]> miArray, List<DiscreteVariable> vars) {
        // convert the array to map

        // initialize the data structure for pairwise MI
        Map<DiscreteVariable, Map<DiscreteVariable, Double>> mis = new HashMap<>(vars.size());

        for (int i = 0; i < vars.size(); i++) {
            double[] row = miArray.get(i);

            Map<DiscreteVariable, Double> map = new HashMap<DiscreteVariable, Double>(vars.size());
            for (int j = 0; j < vars.size(); j++) {
                map.put(vars.get(j), row[j]);
            }

            mis.put(vars.get(i), map);

            // to allow garbage collection
            miArray.set(i, null);
        }

        return mis;

    }
}
