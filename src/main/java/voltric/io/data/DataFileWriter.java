package voltric.io.data;

import voltric.data.DiscreteData;

import java.io.IOException;

/**
 * Interface that defines the methods that all file writers should have.
 *
 * @author ferjorosa
 */
public interface DataFileWriter {

    /**
     * Transforms a {@link DiscreteData} object into a file.
     *
     * @param data the {@link DiscreteData} object that is going to be used to write the file.
     * @param filePathString the file's path.
     * @throws IOException if there is a problem while writing the file.
     */
    void writeToFile(DiscreteData data, String filePathString) throws IOException;
}
