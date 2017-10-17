package voltric.variables;

import voltric.variables.modelTypes.VariableType;
import voltric.variables.visitor.VariableVisitor;

import java.util.Collections;
import java.util.Set;

/**
 * A single dimensional continuous variable.
 *
 * @author kmpoon
 * @author ferjorosa
 */
public class ContinuousVariable extends AbstractContinuousVariable {

    /**
     * Constructs a singular continuous variable with a default name.
     */
    public ContinuousVariable(VariableType type) {
        super(type);
    }

    /**
     * Constructs a singular continuous variable with the given {@code getName}.
     *
     * @param name name of this variable.
     */
    public ContinuousVariable(String name, VariableType type) {
        super(name, type);
    }

    /**
     * Returns an unmodifiable set that contains only this variable.
     *
     * @return an unmodifiable set that contains only this variable.
     */
    @Override
    public Set<ContinuousVariable> getVariables() {
        return Collections.singleton(this);
    }

    /** {@inheritDoc} */
    public <T> T accept(VariableVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Returns true if <code>object</code> is a <code>ContinuousVariable</code> instance where its index and name
     * coincide.
     *
     * @param object the object to test equality against.
     * @return true if object equals this.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if(object instanceof ContinuousVariable == false)
            return false;

        ContinuousVariable variable = (ContinuousVariable) object;
        return this.name.equals(variable.getName())
                && this.index == variable.index;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "Singular continuous variable (" + this.name + ")";
    }
}