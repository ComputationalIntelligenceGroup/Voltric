package voltric.io.data.arff;

import voltric.data.Data;
import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstanceFactory;
import voltric.io.data.DataFileReader;
import voltric.variables.ContinuousVariable;
import voltric.variables.DiscreteVariable;
import voltric.variables.IVariable;
import voltric.variables.modelTypes.VariableType;
import voltric.variables.util.IllegalVariableCastException;
import voltric.variables.util.VariableUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Reads a file that is in Weka's ARFF format and returns it with its specific Data type. It asks the
 * dataType for a better type safety in compilation time, essential with some algorithms.
 *
 * @author ferjorosa
 */
public class ArffFileReader implements DataFileReader{

    /**
     * Reads a file that is in Weka's ARFF format and returns a {@link DiscreteData} object.
     *
     * <p>
     *     It will need to open the same file 4 times, each one reading a subset of it. This has been done for a better
     *     modularization of the class and doesn't affect its efficiency much
     * </p>
     *
     * @param filePathString the file's path.
     * @return a new DataSet of type {@code V}.
     * @throws IOException if there is a problem while reading the file.
     */
    public DiscreteData readDiscreteData(String filePathString) throws IOException{
        // Get the Path object from the provided string
        Path pathFile = Paths.get(filePathString);

        try {
            // Reads the @relation line
            String relationName = getRelationName(pathFile);
            // Reads the @attribute lines
            List<DiscreteVariable> attributes = getAttributes(pathFile, DiscreteVariable.class);
            // Creates a Data object
            DiscreteData newData = new DiscreteData(relationName, attributes);
            // Reads the @data lines
            newData = addDiscreteDataInstances(pathFile, newData);
            // Returns the new data object
            return newData;

        } catch (IllegalVariableCastException illegalVariableCast){
            throw new IllegalArgumentException("the specified type doesn't coincide with the types of the ARFF attributes.");
        }
    }

    /**
     * Returns the ARFF file's @relation name.
     *
     * @param pathFile the file's path.
     * @return the @relation name without spaces.
     * @throws IOException if there is a problem while reading the file.
     */
    private String getRelationName(Path pathFile) throws IOException{

        // Read the relation getName
        Optional<String> atRelation = Files.lines(pathFile)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .limit(1)
                .filter(line -> line.startsWith("@relation"))
                .findFirst();

        if (!atRelation.isPresent())
            throw new IllegalArgumentException("ARFF file does not start with a @relation line.");

        // Returns the relation name after removing the '@relation' substring
        return atRelation.get().split(" ")[1];
    }

    /**
     * Returns the {@code VariableCollection} that corresponds to the ARFF attributes.
     *
     *
     * @param pathFile the file's path.
     * @param dataType the type of the variables, for type safety in compilation time.
     * @param <V> == dataType.
     * @return the collection of Variables that correspond to the ARFF attributes.
     * @throws IOException if there is a problem while reading the file.
     * @throws IllegalVariableCastException there is a variable in the file whose type doesn't correspond with the dataType.
     */
    private <V extends IVariable> List<V> getAttributes(Path pathFile, Class<V> dataType) throws IOException, IllegalVariableCastException {

        int dataLineCount = getDataLineCount(pathFile);

        List<String> attLines = Files.lines(pathFile)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .limit(dataLineCount)
                .filter(line -> line.startsWith("@attribute"))
                .collect(Collectors.toList());

        // First we create a list of the Variables using its supertype IVariable
        List<IVariable> atts = IntStream.range(0,attLines.size())
                .mapToObj( i -> createAttributeFromLine(i, attLines.get(i)))
                .collect(Collectors.toList());

        // Then we try to cast them to the dataType that has been specified by the user. This information will be lost at runtime (Java's way)
        // but it will give a better type safety to the user on compilation time.
        List<V> castedAttributes = VariableUtil.castVariables(atts, dataType);

        return new ArrayList<>(castedAttributes);
    }

    /**
     * This method adds the data instances that are present in the ARFF file's data.
     *
     * @param pathFile the file's path.
     * @param data the data object being completed.
     * @return a new {@link Data} object with data instances.
     * @throws IOException if there is a problem while reading the file.
     */
    private DiscreteData addDiscreteDataInstances(Path pathFile, DiscreteData data) throws IOException{

        int dataLineCount = getDataLineCount(pathFile);

        Stream<String> dataLines =  Files.lines(pathFile)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .skip(dataLineCount)
                .filter(w -> !w.isEmpty());

        int dataLineIndex = 1;
        Iterator<String> dataLinesIterator = dataLines.iterator();
        while(dataLinesIterator.hasNext()){
            data.add(DiscreteDataInstanceFactory.fromArffDataLine(dataLinesIterator.next(), data.getVariables(), dataLineIndex));
            dataLineIndex++;
        }
        return data;
    }

    /**
     * Returns the number of the @data line.
     *
     * @param pathFile the file's path.
     * @return the number of the @data line.
     * @throws IOException if there is a problem while reading the file.
     */
    private int getDataLineCount(Path pathFile) throws IOException{

        // Find the @data line
        final int[] count = {0};
        Optional<String> atData = Files.lines(pathFile)
                .map(String::trim)
                .filter(w -> !w.isEmpty())
                .filter(w -> !w.startsWith("%"))
                .peek(line -> count[0]++)
                .filter(line -> line.startsWith("@data"))
                .findFirst();

        if (!atData.isPresent())
            throw new IllegalArgumentException("ARFF file does not contain @data line.");

        return count[0];
    }

    /**
     * Creates a IVariable from an ARFF's @attribute line.
     *
     * @param attributeIndex the attribute's index.
     * @param line the ARFF's @attribute line.
     * @return the associated IVariable.
     */
    private IVariable createAttributeFromLine(int attributeIndex, String line){
        String[] parts = line.split("\\s+|\t+");

        if (!parts[0].trim().startsWith("@attribute"))
            throw new IllegalArgumentException("Attribute line"+ attributeIndex +" does not start with @attribute");

        String name = parts[1].trim();

        name = name.replaceAll("^'+", "");
        name = name.replaceAll("'+$", "");

        parts[2]=parts[2].trim();

        if (parts[2].equals("real") || parts[2].equals("numeric")){
            if(parts.length>3 && parts[3].startsWith("[")){
                parts[3]=line.substring(line.indexOf("[")).replaceAll("\t", "");
                double min = Double.parseDouble(parts[3].substring(parts[3].indexOf("[")+1,parts[3].indexOf(",")));
                double max = Double.parseDouble(parts[3].substring(parts[3].indexOf(",")+1,parts[3].indexOf("]")));
                return new ContinuousVariable(name, VariableType.MANIFEST_VARIABLE);
            }else
                return new ContinuousVariable(name, VariableType.MANIFEST_VARIABLE);
        }else if (parts[2].startsWith("{")){
            parts[2]=line.substring(line.indexOf("{")).replaceAll("\t", "");
            String[] states = parts[2].substring(1,parts[2].length()-1).split(",");

            List<String> statesNames = Arrays.stream(states).map(String::trim).collect(Collectors.toList());

            return new DiscreteVariable(name, statesNames, VariableType.MANIFEST_VARIABLE);
        }else{
            throw new UnsupportedOperationException("We can not create an attribute from this line: "+line);
        }

    }
}