package voltric.variables.visitor;

import voltric.variables.DiscreteVariable;
import voltric.variables.JointContinuousVariable;
import voltric.variables.ContinuousVariable;

/**
 * Visitor that checks if a <code>Variable</code> is continuous (to avoid the use of 'instanceof').
 */
public class CheckContinuousVariableVisitor implements VariableVisitor<Boolean> {

    /**
     * Returns true because <code>ContinuousVariable</code> is a subtype of <code>AbstractContinuousVariable</code>.
     *
     * @param variable the singular continuous variable to check.
     * @return true.
     */
    @Override
    public Boolean visit(ContinuousVariable variable) {
        return true;
    }

    /**
     * Returns true because <code>JointContinuousVariable</code> is a subtype of <code>AbstractContinuousVariable</code>.
     *
     * @param variable the joint continuous variable to check.
     * @return true.
     */
    @Override
    public Boolean visit(JointContinuousVariable variable) {
        return true;
    }

    /**
     * Returns false because <code>DiscreteVariable</code> is NOT a subtype of <code>AbstractContinuousVariable</code>.
     *
     * @param variable the discrete variable to check.
     * @return false.
     */
    @Override
    public Boolean visit(DiscreteVariable variable) {
        return false;
    }
}
