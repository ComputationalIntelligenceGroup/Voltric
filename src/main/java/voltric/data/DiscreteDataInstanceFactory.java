package voltric.data;

import voltric.variables.DiscreteVariable;

import java.util.List;

/**
 * Created by fernando on 7/08/17.
 */
public class DiscreteDataInstanceFactory {

    /** The value assigned to the missing statement "?" */
    private static final int MISSING = -1;

    /**
     * Returns the data instance's equivalent to the argument data line.
     *
     * @param dataLine the ARFF data line.
     * @param variables the ARFF attributes.
     * @param dataLineIndex the index of the data line, to give more information when a failure appears.
     * @return the data instance's equivalent to the argument data line.
     */
    //TODO: This method does something similar to "Variable.isValuePermitted" when assigning the numeric value. However,
    // given that it doesn't check that the string value can be safely transformed, it could throw a RuntimeException without the dataLineIndex information.
    public static DiscreteDataInstance fromArffDataLine(String dataLine, final List<DiscreteVariable> variables, int dataLineIndex){
        String[] parts = dataLine.split(",");

        if(parts.length != variables.size())
            throw new IllegalArgumentException("DataRow ["+ dataLineIndex +"]: The number of columns does not match the number of ARFF attributes.");

        int[] values = new int[variables.size()];
        for(int i=0; i<parts.length; i++){

            DiscreteVariable variable = variables.get(i);

            // NOTE: It could be done with duck typing but it wouldn't be worth it (more confusion in th Variables package)
            if(parts[i] == null || parts[i].equals("?"))
                values[i] = MISSING;
            else
                values[i] = variable.indexOf(parts[i]);
        }
        return new DiscreteDataInstance(values);
    }
}
