package voltric.potential;

import voltric.variables.DiscreteVariable;

/**
 * This class provides an implementation for two-dimensional tabular functions,
 * namely, matrices.
 *
 * @author Yi Wang
 *
 */
class Function2D extends Function {

    /**
     * the shortcut to the only two variables in this function. There is a
     * requirement that variable>varY.
     */
    protected DiscreteVariable varX, varY;

    /**
     * <p>
     * Constructs a function of the specified array of variables.
     * </p>
     *
     * <p>
     * Note: Only function classes are supposed to call this method.
     * </p>
     *
     * @param variables array of variables to be involved. There are two Variables soorted in the Variable array.
     */
    protected Function2D(DiscreteVariable[] variables) {
        super(variables);

        varX = _variables[0];
        varY = _variables[1];
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
     * @param variables array of variables in new function. There are two Variables in the Variable array.
     * @param cells array of cells in new function.
     * @param magnitudes array of magnitudes for variables in new function.
     */
    protected Function2D(DiscreteVariable[] variables, double[] cells, int[] magnitudes) {
        super(variables, cells, magnitudes);

        varX = _variables[0];
        varY = _variables[1];
    }

    /**
     * Returns the product between this Function2D and another function. The
     * multiplication is delegated to <code>Function1D.times(Function)</code>
     * if the argument is a Function1D and they share a common Variable.
     *
     * @param function another factor
     * @return the product between this Function1D and another function.
     * @see Function1D#times(Function)
     */
    public final Function times(Function function) {
        if (function instanceof Function1D
                && this.contains(function._variables[0])) {
            return ((Function1D) function).times(this);
        } else if (function instanceof Function1D
                // '==' substituted by equals
                && varX.equals(function._variables[0]) && varY.equals(function._variables[1])) {
            Function result = this.clone();
            for (int i = 0; i < getDomainSize(); i++) {
                result._cells[i] *= function._cells[i];
            }
            //System.out.println("Function2DxFunction2D called");
            return result;
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

        // For Test
        //System.out.println("Function2D.normalizeMI(Variable) executed");

        // argument variable must be either of the variables in this function
        if(!variable.equals(varX) && !variable.equals(varY))
            throw new IllegalArgumentException("Argument variable must be either of the variables in this function");


        boolean hasZero = false;

        int xCard = varX.getCardinality();
        int yCard = varY.getCardinality();

        int index;
        double sum;

        if (variable == varX) {
            // uniform probability that may be used
            double uniform = 1.0 / xCard;

            for (int i = 0; i < yCard; i++) {
                // computes sum
                index = i;
                sum = 0.0;
                for (int j = 0; j < xCard; j++) {
                    sum += _cells[index];
                    index += yCard;
                }

                // normalizes
                index = i;
                if (sum != 0.0) {
                    for (int j = 0; j < xCard; j++) {
                        _cells[index] /= sum;
                        index += yCard;
                    }
                } else {
                    for (int j = 0; j < xCard; j++) {
                        _cells[index] = uniform;
                        index += yCard;
                    }

                    hasZero = true;
                }
            }
        } else {
            // uniform probability that may be used
            double uniform = 1.0 / yCard;

            index = 0;
            for (int i = 0; i < xCard; i++) {
                // computes sum
                sum = 0.0;
                for (int j = 0; j < yCard; j++) {
                    sum += _cells[index++];
                }

                // normalizes
                index -= yCard;
                if (sum != 0.0) {
                    for (int j = 0; j < yCard; j++) {
                        _cells[index++] /= sum;
                    }
                } else {
                    for (int j = 0; j < yCard; j++) {
                        _cells[index++] = uniform;
                    }

                    hasZero = true;
                }
            }
        }

        return hasZero;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.latlab.util.Function#project(org.latlab.util.Variable, int)
     */
    public Function project(DiscreteVariable variable, int state) {

        // For Test
        //System.out.println("Function2D.project(Variable, int) executed");

        // argument variable must be either of the variables in this function
        if(!variable.equals(varX) && !variable.equals(varY))
            throw new IllegalArgumentException("Argument variable must be either of the variables in this function");

        // state must be valid
        if(!variable.isValuePermitted(state))
            throw new IllegalArgumentException("state is invalid");

        // result is an one-dimensional function
        DiscreteVariable[] variables;
        double[] cells;
        int[] magnitudes = new int[] { 1 };

        if (variable == varX) {
            variables = new DiscreteVariable[] {varY};

            int yCard = varY.getCardinality();
            cells = new double[yCard];

            System.arraycopy(_cells, state * yCard, cells, 0, yCard);
        } else {
            variables = new DiscreteVariable[] {varX};

            int xCard = varX.getCardinality();
            int yCard = varY.getCardinality();
            cells = new double[xCard];

            int index = state;
            for (int i = 0; i < xCard; i++) {
                cells[i] = _cells[index];
                index += yCard;
            }
        }

        return (new Function1D(variables, cells, magnitudes));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.latlab.util.Function#sumOut(org.latlab.util.Variable)
     */
    public Function sumOut(DiscreteVariable variable) {

        // argument variable must be either of the variables in this function
        if(!variable.equals(varX) && !variable.equals(varY))
            throw new IllegalArgumentException("Argument variable must be either of the variables in this function");

        // result is an one-dimensional function
        DiscreteVariable[] variables;
        double[] cells;
        int[] magnitudes = new int[] { 1 };

        int xCard = varX.getCardinality();
        int yCard = varY.getCardinality();

        if (variable == varX) {
            variables = new DiscreteVariable[] {varY};

            cells = new double[yCard];

            int index = 0;
            for (int i = 0; i < xCard; i++) {
                for (int j = 0; j < yCard; j++) {
                    cells[j] += _cells[index++];
                }
            }
        } else {
            variables = new DiscreteVariable[] {varX};

            cells = new double[xCard];

            int index = 0;
            for (int i = 0; i < xCard; i++) {
                for (int j = 0; j < yCard; j++) {
                    cells[i] += _cells[index++];
                }
            }
        }

        return (new Function1D(variables, cells, magnitudes));
    }

    /**
     * <p>
     * Multiply this function by the argument function. Note that this function
     * must contains the argument function in terms of the variables.
     * </p>
     *
     * @param function
     *            multiplier function.
     * @return the product between this function and the specified function.
     */
    @Override
    public final void multiply(Function function) {
        if (function.getDimension() == 0) {
            multiply(function._cells[0]);
        } else if (function instanceof Function1D) {
            int xCard = varX.getCardinality();
            int yCard = varY.getCardinality();
            int index = 0;
            if (varX == ((Function1D) function).variable) {
                for (int i = 0; i < xCard; i++) {
                    for (int j = 0; j < yCard; j++) {
                        _cells[index] *= function._cells[i];
                        index++;
                    }
                }
            } else {
                for (int i = 0; i < xCard; i++) {
                    for (int j = 0; j < yCard; j++) {
                        _cells[index] *= function._cells[j];
                        index++;
                    }
                }
            }
        } else {
            for (int i = 0; i < getDomainSize(); i++) {
                _cells[i] *= function._cells[i];
            }
        }
    }

    /**
     * <p>
     * DIvide this function by the argument function. Note that this function
     * must contains the argument function in terms of the variables. Also note
     * that the argument function should contain NO zero cell at all.
     * </p>
     *
     * @param function
     * @return the division
     */
    @Override
    public final void divide(Function function) {
        if (function.getDimension() == 0) {
            divide(function._cells[0]);
        } else if (function instanceof Function1D) {
            int xCard = varX.getCardinality();
            int yCard = varY.getCardinality();
            int index = 0;
            if (varX == ((Function1D) function).variable) {
                for (int i = 0; i < xCard; i++) {
                    for (int j = 0; j < yCard; j++) {
                        _cells[index] /= function._cells[i];
                        index++;
                    }
                }
            } else {
                for (int i = 0; i < xCard; i++) {
                    for (int j = 0; j < yCard; j++) {
                        _cells[index] /= function._cells[j];
                        index++;
                    }
                }
            }
        } else {
            for (int i = 0; i < getDomainSize(); i++) {
                _cells[i] /= function._cells[i];
            }
        }
    }

}