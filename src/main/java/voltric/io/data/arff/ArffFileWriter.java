package voltric.io.data.arff;

import voltric.data.Data;
import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.io.data.DataFileWriter;
import voltric.variables.DiscreteVariable;
import voltric.variables.IVariable;
import voltric.variables.StateSpaceType;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Transforms a {@link Data} object into an ARFF file.
 *
 * @author ferjorosa
 */
public class ArffFileWriter implements DataFileWriter {

    /**
     * Transforms a {@link Data} object into an ARFF file.
     *
     * @param data the {@link Data} object that is going to be used to write the ARFF file.
     * @param filePathString the file's path.
     * @throws IOException if there is a problem while writing the file.
     */
    public void writeToFile(DiscreteData data, String filePathString) throws IOException{
        FileWriter fw = new FileWriter(filePathString);

        // Writes the ARFF @relation line that identifies the Data
        fw.write("@relation " + data.getName() + "\n\n");

        // Writes the ARFF attributes
        for (DiscreteVariable att : data.getVariables()) {
            fw.write(attributeToArffString(att) + "\n");
        }

        // Writes the ARFF data instances
        fw.write("\n\n@data\n\n");

        for (DiscreteDataInstance instance : data.getInstances())
            fw.write(discreteDataInstanceToARFFString(data.getVariables(), instance, ","));

        // Closes the file
        fw.close();
    }

    /**
     * Transforms a variable into an equivalent ARFF @attribute line.
     *
     * @param attribute the variable that is going to be transformed.
     * @param <V> the specific type of the variable ({@code DiscreteVariable}, {@code AbstractContinuousVariable}, {@code Variable}, etc.)
     * @return the equivalent @attribute line.
     */
    private <V extends IVariable> String attributeToArffString(V attribute){
        if(attribute.getStateSpaceType() == StateSpaceType.REAL)
            return "@attribute " + attribute.getName() + " real";
        else if(attribute.getStateSpaceType() == StateSpaceType.FINITE) {
            StringBuilder stringBuilder = new StringBuilder("@attribute " + attribute.getName() + " {");
            DiscreteVariable discreteAttribute = (DiscreteVariable) attribute;
            List<String> attributeStates = discreteAttribute.getStates();

            // Append all the variable states minus the last one
            attributeStates
                    .stream()
                    .limit(discreteAttribute.getStates().size() - 1)
                    .forEach(e -> stringBuilder.append(e + ", "));

            // Append the last state
            stringBuilder.append(attributeStates.get(attributeStates.size() - 1) + "}");

            return stringBuilder.toString();
        }
        else
            throw new IllegalArgumentException("Unknown SateSapaceType");
    }

    /**
     * Transforms the {@link DiscreteDataInstance} into an equivalent ARFF @data line.
     *
     * @param dataInstance the instance to be transformed into an ARFF string.
     * @return the ARFF @data line equivalent of the dataInstance.
     */
    private String discreteDataInstanceToARFFString(List<DiscreteVariable> attributes, DiscreteDataInstance dataInstance, String separator){
        StringBuilder builder = new StringBuilder();

        // Append all the columns of the DataInstance with  the separator except the last one
        for(int i=0; i<dataInstance.getTextualValues().size();i++)
            builder.append(discreteDataInstanceToARFFString(attributes.get(i), dataInstance, attributes, ","));

        // Append the last column of the data instance
        DiscreteVariable att = attributes.get(attributes.size() - 1);
        builder.append(discreteDataInstanceToARFFString(att,dataInstance, attributes, ""));

        return builder.toString();
    }

    /**
     * This method returns the string equivalent of an specific value of the instance
     *
     * @param att the variable that indicates the values that is going to be transformed.
     * @param dataInstance the instance being converted into an ARFF string.
     * @param separator the separator being used
     * @return the string equivalent of the numeric value.
     */
    // TODO: Maybe is better not to modularize this part and add it to the "dataInstanceToARFFString" method? (different strategy)
    private String discreteDataInstanceToARFFString(DiscreteVariable att, DiscreteDataInstance dataInstance, List<DiscreteVariable> variables, String separator) {
        StringBuilder builder = new StringBuilder();
        if(dataInstance.getNumericValue(variables.indexOf(att)) == Double.NaN) // Value is MISSING
            builder.append("?" + separator);
        else if (att.getStateSpaceType() == StateSpaceType.FINITE || att.getStateSpaceType() == StateSpaceType.REAL){
            builder.append(dataInstance.getTextualValue(variables.indexOf(att)) + separator);
        } else
            throw new IllegalArgumentException("Illegal state space type of Attribute: " + att.getStateSpaceType());

        return builder.toString();
    }

}
