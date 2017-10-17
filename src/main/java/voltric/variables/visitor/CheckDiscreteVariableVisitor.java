package voltric.variables.visitor;

import voltric.variables.ContinuousVariable;
import voltric.variables.DiscreteVariable;
import voltric.variables.JointContinuousVariable;

/**
 * Visitor that checks if a <code>Variable</code> is discrete (to avoid the use of 'instanceof').
 */
public class CheckDiscreteVariableVisitor implements VariableVisitor<Boolean> {

    /**
     * Returns false because <code>ContinuousVariable</code> is NOT a <code>DiscreteVariable</code>.
     *
     * @param variable the singular continuous variable to check.
     * @return false.
     */
    @Override
    public Boolean visit(ContinuousVariable variable) {
        return false;
    }

    /**
     * Returns false because <code>JointContinuousVariable</code> is NOT a <code>DiscreteVariable</code>.
     *
     * @param variable the joint continuous variable to check.
     * @return false.
     */
    @Override
    public Boolean visit(JointContinuousVariable variable) {
        return true;
    }

    /**
     * Returns true because <code>DiscreteVariable</code> is a <code>DiscreteVariable</code> (duh!).
     *
     * @param variable the discrete variable to check.
     * @return true.
     */
    @Override
    public Boolean visit(DiscreteVariable variable) {
        return true;
    }
}
