package voltric.variables.util;

import java.util.List;

/**
 * Exception being thrown when a supertype {@code IVariable} is being casted to its wrong child equivalent
 * For example, trying to cast a {@code ContinuousVariable} to {@code DiscreteVariable}.
 *
 * @author ferjorosa
 *
 * @see VariableUtil#castVariables(List, Class)
 */
public class IllegalVariableCastException extends Exception {

    /**
     * Constructs an {@code IllegalVariableCastException} with a specific message.
     *
     * @param message the argument message.
     */
    public IllegalVariableCastException(String message){
        super(message);
    }
}
