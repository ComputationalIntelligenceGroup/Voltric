package voltric.data;

/**
 * Created by equipo on 07/03/2017.
 */
class UnassignedDataSetException extends RuntimeException {

    /**
     * Default constructor
     */
    public UnassignedDataSetException() { super(); }

    /**
     * Constructs an {@code UnassignedDataSetException} with a specific message.
     *
     * @param message the argument message
     */
    public UnassignedDataSetException(String message) { super(message); }

    /**
     * Constructs an {@code UnassignedDataSetException} with a specific message and a cause.
     *
     * @param message the argument message.
     * @param cause the cause of the exception.
     */
    public UnassignedDataSetException(String message, Throwable cause) { super(message, cause); }

    /**
     * Constructs an {@code UnassignedDataSetException} with a cause.
     *
     * @param cause the cause of the exception.
     */
    public UnassignedDataSetException(Throwable cause) { super(cause); }
}
