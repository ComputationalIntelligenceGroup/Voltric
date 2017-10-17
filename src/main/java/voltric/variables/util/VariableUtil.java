package voltric.variables.util;

import voltric.variables.IVariable;
import voltric.variables.Variable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO: Properly allocate this class' methods.
 */
public class VariableUtil {

    /**
     * Returns a string representation of a collection of <code>variable</code>, with a delimiter between each of them.
     *
     * @param variables the collection of <code>variable</code>.
     * @param delimiter the delimiter.
     * @return the joint name.
     */
    //TODO: This method used to belong to {@link voltric.variables.Variable}
    public static String getJointName(Collection<? extends Variable> variables, String delimiter) {
        boolean first = true;

        StringBuilder builder = new StringBuilder();
        for (Variable variable : variables) {
            if (!first) {
                builder.append(delimiter);
            }

            builder.append(variable.getName());
            first = false;
        }

        return builder.toString();
    }

    /**
     * Checks that the castType is valid. Returns true if the variable is an instance of the CastType class, false otherwise.
     *
     * @param variable the variable being checked.
     * @param castType the cast type.
     * @param <V> == castType
     * @return true if the variable is an instance of the CastType class, false otherwise.
     */
    public static <V extends IVariable> boolean checkCastType(IVariable variable, Class<V> castType) {
        return castType.isInstance(variable);
    }

    /**
     * Checks that the castType of the Variables is valid. Returns true if the variable is an instance of the CastType class, false otherwise.
     *
     * @param variables the variables being checked
     * @param castType the cast type.
     * @param <V> == castType
     * @return true if all the variables are an instance of the CastType class, false otherwise.
     */
    // True if OK
    public static <V extends IVariable> boolean checkCastTypes(Collection<IVariable> variables, Class<V> castType) {
        for(IVariable variable: variables)
            if(!checkCastType(variable, castType))
                return false;

        return true;
    }

    /**
     * Casts a List of {@code Ivariable} objects to a list of more specific types like {@code DiscreteVariable}
     *
     * @param variables the list of variables being casted.
     * @param castType the cast type.
     * @param <V> == castType.
     * @return a new list of variables with a more specific type.
     * @throws IllegalVariableCastException if variable casting is illegal.
     */
    // I know about type erasure on runtime...
    public static <V extends IVariable> List<V> castVariables(List<IVariable> variables, Class<V> castType) throws IllegalVariableCastException {

        if(!checkCastTypes(variables, castType))
            throw new IllegalVariableCastException("Illegal IVariable casting.");
        else
            // Cast the variables (just for compiler, type is erased at runtime)
            return variables.stream().map(x -> (V) x).collect(Collectors.toList());

    }
}
