package voltric.model;

import voltric.model.visitor.BeliefNodeVisitor;
import voltric.potential.Potential;
import voltric.variables.JointContinuousVariable;
import voltric.variables.ContinuousVariable;
import voltric.variables.Variable;

/**
 * Created by fernando on 23/03/17.
 */
public class ContinuousBeliefNode extends AbstractBeliefNode {

    /**
     * Joint variable held by this node.
     */
    private JointContinuousVariable joint;

    /**
     * Constructs a continuous belief node.
     *
     * @param network
     *            Bayesian network containing this node
     * @param variable
     *            a single dimensional continuous variable for this node
     */
    public ContinuousBeliefNode(AbstractBayesNet network, ContinuousVariable variable) {
        //this(network, new JointContinuousVariable(variable));
        //super(network, variable);
        //TODO: No hecho, solo de momento para quitar errores de compilacion
        super(null, null);
    }

    @Override
    public Potential getPotential() {
        return null;
    }

    @Override
    public void setPotential(Potential potential) {

    }

    @Override
    public Variable getVariable() {
        return null;
    }

    @Override
    public int computeDimension() {
        return 0;
    }

    @Override
    public <T> T accept(BeliefNodeVisitor<T> visitor) {
        return null;
    }
}
