package voltric.util.frequencycount;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fernando on 15/04/17.
 */
public abstract class FrequencyCounter {

    public abstract ArrayList<double[]> compute(DiscreteData data, List<DiscreteVariable> variables);

    // Este método realmente es una marginalizacion del secuencial, ya que te permite calcular las frecuencias de un
    // intervalo de las variables
    protected ArrayList<double[]> computeFrequencies(DiscreteData data, List<DiscreteVariable> variables, int start, int end) {
        // the diagonal entries contain the frequencies of a single variable
        ArrayList<double[]> frequencies =
                new ArrayList<double[]>(variables.size());

        for(int i = 0; i<variables.size();i++){
            frequencies.add(new double[variables.size()]);
        }


        List<DiscreteDataInstance> cases = data.getInstances();
        for (int caseIndex = start; caseIndex < end; caseIndex++) {
            DiscreteDataInstance c = cases.get(caseIndex);
            int[] states = c.getNumericValues();
            double weight = data.getWeight(c);

            // find the indices of states that are greater than zero
            List<Integer> entries = new ArrayList<>(states.length);
            for (int s = 0; s < states.length; s++) {
                if (states[s] > 0) {
                    entries.add(s);
                }
            }

            // update the single and joint counts
            for (int i : entries) {
                //	int iInVariables = idMappingFromDataToVariables[i];
                int iInVariables = i;
                if (iInVariables < 0)
                    continue;

                for (int j : entries) {
                    //int jInVariables = idMappingFromDataToVariables[j];
                    int jInVariables = j;
                    if (jInVariables < 0)
                        continue;


                    double freq = frequencies.get(iInVariables)[jInVariables];
                    freq += weight;
                    frequencies.get(iInVariables)[jInVariables]=freq;

                }
            }
        }

        return frequencies;
    }
}
