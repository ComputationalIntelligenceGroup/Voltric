package voltric.io.data;

import voltric.data.Data;
import voltric.data.DiscreteData;
import voltric.io.data.arff.ArffFileReader;
import voltric.variables.IVariable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * This class allows to load {@link Data} from the disk in different formats (CSV, ARFF, etc.).
 *
 * @author ferjorosa
 */
public class DataFileLoader {

    /**
     * Loads a {@link DiscreteData} from a file. If the file contains variables that are not discrete, an exception
     * will be thrown.
     *
     * @param filePathString the file's path.
     * @return a new {@link Data} object containing the file's information.
     */
    //TODO: Maybe is better to use 'throws' instead of having an unchecked exception, given that it is an IO operation.
    public static DiscreteData loadDiscreteData(String filePathString){
        try {
            return selectDataFileReader(filePathString).readDiscreteData(filePathString);
        }catch(IOException ex){
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Selects the supported File reader or throws an Exception if none is available.
     *
     * @param filePathString the file's path.
     * @return  the supported file reader.
     */
    private static DataFileReader selectDataFileReader(String filePathString){
        if(new File(filePathString).isDirectory())
            throw new IllegalArgumentException("The path refers to a directory, which is not supported yet");

        // Simple way to check the file's format, just by looking at its file extension
        String[] parts = filePathString.split("\\.");
        String fileExtension = parts[parts.length - 1];

        // Check if file format is supported
        if(fileExtension.equals("arff"))
            return new ArffFileReader();
        else
            throw new IllegalArgumentException("File extension not supported");
    }
}
