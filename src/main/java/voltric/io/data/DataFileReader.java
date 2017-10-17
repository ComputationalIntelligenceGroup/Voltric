package voltric.io.data;

import voltric.data.DiscreteData;

import java.io.IOException;

/**
 * Interface that defines the methods that all file readers should have.
 *
 * @author ferjorosa
 */
public interface DataFileReader {

    /**
     * Reads a file and returns a {@link DiscreteData} object with its specific Data type. It asks the
     * dataType for a better type safety in compilation time, essential with some algorithms.
     *
     * @param filePathString the file's path.
     * @return a new DataSet of type {@code V}.
     * @throws IOException if there is a problem while reading the file.
     */
    DiscreteData readDiscreteData(String filePathString) throws IOException;
}
