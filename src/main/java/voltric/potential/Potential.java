package voltric.potential;

import voltric.variables.DiscreteVariable;
import voltric.variables.Variable;

public interface Potential {
    /**
     * Adds a variable and returns the modified Potential. This Potential may be
     * used as the returned value.
     *
     * @param variable
     *            variable to add
     * @return the modified Potential
     */
    Potential addParentVariable(Variable variable);

    /**
     * Removes a variable and returns the modified Potential. This Potential may
     * be used as the returned value.
     *
     * @param variable
     *            variable to remove
     * @return the modified Potential
     */
    Potential removeParentVariable(Variable variable);

    /**
     * Normalizes this Potential and returns the normalization constant. If the
     * normalization constant is given, it is used. Otherwise if it is {@code
     * Double.NaN}, the constant is computed.
     *
     * @param constant
     *            normalization constant or {@code Double.NaN}
     * @return normalization constant used
     */
    double normalize(double constant);

    /**
     * It times a indicator function with the only {@code variable} and value 1
     * at the {@state}.
     *
     * @param variable
     *            variable of the indicator function
     * @param state
     *            state which has value 1, and other unspecified states 0
     */
    void timesIndicator(DiscreteVariable variable, int state);

    /**
     * Returns a copy of this Potential. The copy and the this Potential shares
     * the same instances of variables.
     *
     * @return a copy of this Potential
     */
    Potential clone();

    /**
     * Returns a function holding only the discrete part.
     *
     * @return function of the discrete part
     */
    //TODO: Wut? Avoid by using a visitor? (Understand this method better)
    Function function();

    /**
     * Marginalizes this function to have only the given {@code variable}.
     *
     * @param variable
     *            variable left in the marginalized function
     * @return function marginalized to the given {@code variable}
     */
    Function marginalize(DiscreteVariable variable);

    /**
     * Reorders the states according to the given order by adjusting the
     * positions of the probability entries in this function.
     *
     * @param variable
     *            variable states of which is being reordered
     * @param order
     *            the new state order
     */
    //TODO: Este metodo no tiene sentido que sea polimorfico
    void reorderStates(DiscreteVariable variable, int[] order);
}