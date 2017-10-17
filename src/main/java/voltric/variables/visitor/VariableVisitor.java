package voltric.variables.visitor;

import voltric.variables.DiscreteVariable;
import voltric.variables.JointContinuousVariable;
import voltric.variables.ContinuousVariable;

/**
 * The visitor pattern interface for the subclasses of the {@link voltric.variables.Variable} class.
 *
 * @param <T> the return type parameter.
 *
 * @author ferjorosa
 */
public interface VariableVisitor<T> {

    /**
     * Generic visit method for the <code>ContinuousVariable</code>.
     *
     * @param variable the variable to visit.
     * @return
     */
    T visit(ContinuousVariable variable);

    /**
     * Generic visit method for the <code>JointContinuousVariable</code>.
     *
     * @param variable the variable to visit.
     * @return
     */
    T visit(JointContinuousVariable variable);

    /**
     * Generic visit method for the <code>DiscreteVariable</code>.
     *
     * @param variable the variable to visit.
     * @return
     */
    T visit(DiscreteVariable variable);
}
