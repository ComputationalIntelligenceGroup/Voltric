package voltric.variables;

import voltric.variables.modelTypes.VariableType;

/**
 * This interface establishes the main methods of the data getVariables. A variable is represents a column in the data matrix,
 * where its name and data column type is stored.
 *
 * @author ferjorosa
 */
public interface IVariable {
    /**
     * Returns the name of the variable.
     *
     * @return the name of the variable.
     */
    String getName();

    /**
     * Modifies the name of the variable.
     *
     * @param name the new name of the variable.
     */
    void setName(String name);

    /**
     * The space type of the variable (discrete, continuous, etc).
     *
     * @return the space type of the variable.
     */
    StateSpaceType getStateSpaceType();

    /**
     *
     *
     * @return
     */
    VariableType getType();

    /**
     * Checks if the provided value belongs to the state space or not.
     *
     * @param value the provided value.
     * @return {@code true} if the provided value belongs to the state space.
     */
    boolean isValuePermitted(double value);

    /**
     * Checks if the variable is manifest (observable). This will be indicated in its model type.
     *
     * @return {@code true} if the variable is manifest (observable).
     *
     * @see VariableType
     */
    boolean isManifestVariable();

    /**
     * Checks if the variable is latent (hidden). This will be indicated in its model type.
     *
     * @return {@code true} if the variable is latent (hidden).
     *
     * @see VariableType
     */
    boolean isLatentVariable();

}
