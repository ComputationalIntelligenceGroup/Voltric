package voltric.variables;

import voltric.variables.modelTypes.VariableType;

import java.util.Set;

/**
 * Base class for the continuous variable classes. Default implementations are provided for some of the methods that do not
 * vary in its subtypes.
 *
 * @author kmpoon
 * @author ferjorosa
 */
public abstract class AbstractContinuousVariable extends Variable {

    /**
     * Constructs a continuous variable with the given getName.
     *
     * @param name name of the variable
     */
    protected AbstractContinuousVariable(String name, VariableType type) {
        super(name, type);
    }

    /**
     * Default constructor. Its name will be provided by default.
     */
    protected AbstractContinuousVariable(VariableType type) {
        super(type);
    }

    /** {@inheritDoc} */
    public StateSpaceType getStateSpaceType(){
        return StateSpaceType.REAL;
    }

    /**
     * Returns a set of the underlying singular continuous variable.
     *
     * @return a set of the underlying singular continuous variable
     */
    public abstract Set<ContinuousVariable> getVariables();

    /** {@inheritDoc} */
    public String toString() {
        return "Continuous variable (" + this.name + ")";
    }

    /**
     * Checks if the provided value belongs to the state space or not. Given that the continuous state space is infinite,
     * all the double values are permitted.
     *
     * @param value the value to check.
     * @return always true.
     */
    public boolean isValuePermitted(double value){
        return true;
    }
}