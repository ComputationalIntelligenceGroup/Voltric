package voltric.util.frequencycount;

import voltric.data.DiscreteData;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fernando on 15/04/17.
 */
public class SequentialFrequencyCounter extends FrequencyCounter{

    /**
     * Metodo interfaz, es decir, aquel que llama el usuario
     *
     * @param data
     * @param variables
     * @return
     */
    public ArrayList<double[]> compute(DiscreteData data, List<DiscreteVariable> variables) {
        return computeFrequencies(data, variables, 0, data.getInstances().size());
    }
}
