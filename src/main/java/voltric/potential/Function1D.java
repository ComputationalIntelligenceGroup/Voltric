package voltric.potential;


import voltric.variables.DiscreteVariable;

/**
 * This class provides an implementation for one-dimensional tabular functions,
 * namely, vectors.
 *
 * @author Yi Wang
 *
 */
class Function1D extends Function {

    /** The shortcut to the only variable in this function. */
    protected DiscreteVariable variable;

    /**
     * <p>
     * Constructs a function of the specified array of variables.
     * </p>
     *
     * <p>
     * Note: Only function classes are supposed to call this method.
     * </p>
     *
     * @param variables array of variables to be involved. There is only one Variable.
     */
    protected Function1D(DiscreteVariable[] variables) {
        super(variables);

        variable = _variables[0];
    }

    /**
     * <p>
     * Constructs a function with all its internal data structures specified.
     * </p>
     *
     * <p>
     * Note: Only function classes are supposed to call this method.
     * </p>
     *
     * @param variables array of variables in new function. There is only one Variable.
     * @param cells array of cells in new function.
     * @param magnitudes array of magnitudes for variables in new function.
     */
    protected Function1D(DiscreteVariable[] variables, double[] cells, int[] magnitudes) {
        super(variables, cells, magnitudes);

        variable = _variables[0];
    }

    /**
     * Returns the product between this function and the specified
     * two-dimensional function.
     *
     * @param function two-dimensional multiplier function.
     * @return the product between this function and the specified two-dimensional function.
     */
    public final Function times(Function function) {

        if (function instanceof Function1D && function._variables[0] == variable) {
            Function result = this.clone();
            for (int i = 0; i < getDomainSize(); i++) {
                result._cells[i] *= function._cells[i];
            }
            //System.out.println("Function1DxFunction1D called");
            return result;
        } else if (function instanceof Function2D && function.contains(variable)) {
            //System.out.println("Function1DxFunction2D called");
            DiscreteVariable[] variables = function._variables.clone();
            double[] cells = new double[function.getDomainSize()];

            int index = 0;
            int xCard = ((Function2D) function).varX.getCardinality();
            int yCard = ((Function2D) function).varY.getCardinality();

            if (variable == ((Function2D) function).varX) {
                for (int i = 0; i < xCard; i++) {
                    for (int j = 0; j < yCard; j++) {
                        cells[index] = _cells[i] * function._cells[index];
                        index++;
                    }
                }
            } else {
                for (int i = 0; i < xCard; i++) {
                    for (int j = 0; j < yCard; j++) {
                        cells[index] = _cells[j] * function._cells[index];
                        index++;
                    }
                }
            }
            int[] magnitudes = function._magnitudes.clone();
            return (new Function2D(variables, cells, magnitudes));

        } else {
            return super.times(function);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.latlab.util.Function#normalizeMI(org.latlab.util.Variable)
     */
    public final boolean normalize(DiscreteVariable variable) {

        // argument variable must be the one involved in this function
        if(!variable.equals(this.variable))
            throw new IllegalArgumentException("Argument variable must be the one involved in this function");

        // reduces to normalization over all variable(s)
        return (normalize() == 0.0 ? true : false);
    }

    /*
     * Note: I guess this method never used.
     *
     * @see org.latlab.util.Function#project(org.latlab.util.Variable, int)
     */
    public final Function project(DiscreteVariable variable, int state) {

        // argument variable must be the one involved in this function
        if(!variable.equals(this.variable))
            throw new IllegalArgumentException("Argument variable must be the one involved in this function");

        // state must be valid
        if(!variable.isValuePermitted(state))
            throw new IllegalArgumentException("State is invalid");

        // result is a zero-dimensional function. the only cell is selected by
        // the argument state.
        DiscreteVariable[] variables = new DiscreteVariable[0];
        double[] cells = new double[] { _cells[state] };
        int[] magnitudes = new int[0];

        return (new Function(variables, cells, magnitudes));
    }

    /*
     * Note: I guess this method never called in HLCM clique tree propogation.
     *
     * @see org.latlab.util.Function#sumOut(org.latlab.util.Variable)
     */
    public final Function sumOut(DiscreteVariable variable) {

        // argument variable must be the one involved in this function
        if(!variable.equals(this.variable))
            throw new IllegalArgumentException("Argument variable must be the one involved in this function");

        // result is a zero-dimensional function. the only cell contains the sum
        // of the cells in this function.
        DiscreteVariable[] variables = new DiscreteVariable[0];
        double[] cells = new double[] { sumUp() };
        int[] magnitudes = new int[0];

        return (new Function(variables, cells, magnitudes));
    }
}