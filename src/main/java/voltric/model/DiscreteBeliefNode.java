package voltric.model;

import voltric.graph.AbstractNode;
import voltric.graph.DirectedNode;
import voltric.model.visitor.BeliefNodeVisitor;
import voltric.model.visitor.GetCardinalityVisitor;
import voltric.potential.Function;
import voltric.potential.Potential;
import voltric.variables.DiscreteVariable;

/**
 * This class provides an implementation for discrete belief nodes in a Bayesian network.
 *
 * Note:
 *
 * @author kmpoon
 * @author ferjorosa
 */
public class DiscreteBeliefNode extends AbstractBeliefNode {

    /** The Conditional Probability Table attached to this node, its Potential. */
    private Function cpt;

    /**
     * Constructs a node with the specified variable attached and the specified graph to contain it.
     * This node has the same name as the argument variable.
     *
     * <p><b>
     *     Note: Besides constructors of subclasses, only
     *     <code>BayesNet.addNode(Variable)</code> is supposed call this method.
     * </b></p>
     *
     * @param bayesianNetwork in which the belief node is added to
     * @param variable variable to be attached to this node.
     * @see DiscreteBayesNet#addNode(DiscreteVariable)
     */
    public DiscreteBeliefNode(AbstractBayesNet bayesianNetwork, DiscreteVariable variable) {
        super(bayesianNetwork, variable);

        // sets CPT as uniform distribution
        this.cpt = Function.createUniformDistribution(variable);
    }

    /**
     * Returns the CPT attached to this node.
     *
     * <p>For the sake of efficiency, this implementation returns the reference to a private field. Make sure you
     * understand this before using this method.</p>
     *
     * @return the CPT attached to this node.
     */
    public final Function getPotential() {
        return cpt;
    }

    /**
     * Sets the node's associated Potential.
     *
     * @param potential Potential to be used by this belief node.
     */
    @Override
    public void setPotential(Potential potential) {
        if(!(potential instanceof Function))
            throw new IllegalArgumentException("Invalid Potential. Only discrete potentials (Functions) are allowed.");

        setCpt((Function) potential);
    }

    /**
     * Returns the variable attached to this node. For the sake of efficiency, this implementation returns the reference
     * to a private field. Make sure you understand this before using this method.
     *
     * @return the variable attached to this node.
     */
    public final DiscreteVariable getVariable() {
        return (DiscreteVariable) this.content;
    }

    /**
     * Returns the standard dimension, namely, the number of free parameters in the CPT, of this node.
     *
     * @author csct
     * @return the standard dimension of this node.
     */
    public final int computeDimension() {
        // let X and pi(X) be variable attached to this node and joint variable
        // attached to parents, respectively. the standard dimension equals
        // (|X|-1)*|pi(X)|.
        int dimension = this.getVariable().getCardinality() - 1;

        dimension *= DiscreteVariable.getCardinality(getDiscreteParentVariables());

        return dimension;
    }

    /** {@inheritDoc} */
    @Override
    public <T> T accept(BeliefNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Suppose this is a Latent node in an HLCM. Then for the purpose of satisfying regularity, the cardinality of
     * this node is at most:
     * (CardOfNeighbor1 * CardOFNeighbor2 * ... * CardOfNeighborN) / The maximum cardinality among its N neighbors.
     *
     * <p>This method compute this quantity for check regularity.</p>
     *
     * <p>If any of its neighbors is a continuous node, then this regularity definition does not hold,
     * and it throws an {@code IllegalArgumentException}.</p>
     *
     * @return The maximum possible cardinality of this node in an HLCM
     */
    public final int computeMaxPossibleCardInHLCM() {

        int product = 1;
        int max = 1;

        // a visitor find the cardinality for discrete and continuous nodes
        // The visitor allows us to avoid the creation of a polymorphic method that wouldn't make much sense on continuous nodes
        GetCardinalityVisitor visitor = new GetCardinalityVisitor();

        for (AbstractNode neighbor : getNeighbors()) {
            int cardinality = ((AbstractBeliefNode) neighbor).accept(visitor);
            // if the neighbor is a continuous node
            if (cardinality < 0)
                throw new IllegalArgumentException("Continuous belief neighbors are not allowed.");

            product *= cardinality;
            max = Math.max(max, cardinality);
        }

        // the product has exceeded the largest possible value
        if (product < 0) {
            return Integer.MAX_VALUE;
        } else
            return product / max;
    }

    /**
     * Returns {@code true} if the specified function can be a valid CPT of this node. Here the meaning of <b>valid</b>
     * is kind of partial since we only check this function is a function of this node variable and the variables
     * in its parents. However <b>validity</b> do Not guarantee that this function is a CPT of {@code this.variable}.
     *
     * @param function function whose validity as a CPT is to be tested.
     * @return <code>true</code> if the specified function can be a valid CPT of this node.
     */
    public final boolean isValidCpt(Function function) {
        // A valid CPT should contain the node's variable
        if (!function.contains(this.getVariable())) {
            return false;
        }

        // A valid CPT must contain exactly variables in this family
        if (function.getDimension() != getInDegree() + 1) {
            return false;
        }

        for (DirectedNode parent : getParents()) {
            if (!function.contains(((DiscreteBeliefNode) parent).getVariable())) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>
     * Randomly sets the parameters of this node.
     * </p>
     *
     * <p>
     * <b>Note: Only <code>BayesNet.randomlyParameterize()</code> and
     * <code>BayesNet.randomlyParameterize(java.util.Collection)</code> are
     * supposed to call this method.
     * </p>
     *
     * <p>
     * Notes add by csct: We consider a more general situation of calling this
     * method, that is, when variables contained in "_ctp" mismatch with the
     * family Variables of the node, or maybe even the "_ctp" is just null. The
     * cases often occur when structure learning, e.g. in
     * HLCM.introduceState4Root().
     * </p>
     *
     * @see DiscreteBayesNet#randomlyParameterize()
     * @see DiscreteBayesNet#randomlyParameterize(java.util.Collection)
     */
    protected final void randomlyParameterize() {
        this.cpt.randomlyDistribute(this.getVariable());
    }

    /**
     * Replaces the CPT attached to this node. This implementation will check whether this Function cpt is a function
     * of this node and its parent nodes.
     *
     * <p>However, it is not guaranteed that cpt satisfies the probability constraint. Therefore, when use this method,
     * make sure Function cpt is in the form of a conditional probability of <code>this.variable</code></p>
     *
     * @param cpt new CPT to be attached to this node.
     */
    public final void setCpt(Function cpt) {
        if (!isValidCpt(cpt))
            throw new IllegalArgumentException("Invalid CPT");

        this.cpt = cpt;
    }

    /**
     * Returns the CPT attached to this node. This method exists to pair with setCPT (name purpose only).
     *
     * <p>For the sake of efficiency, this implementation returns the reference to a private field. Make sure you
     * understand this before using this method.</p>
     *
     * @return the CPT attached to this node.
     */
    public final Function getCpt(){
        return this.getPotential();
    }

    /**
     * Returns {@code true} if the object is a {@code DiscreteBeliefNode} with equal fields (inherited ones included).
     *
     * @param object the object to test equality against.
     * @return true if {@code object} equals this.
     */
    @Override
    public boolean equals(Object object){
        if(this == object)
            return true;

        if(object.getClass() != this.getClass())
            return  false;

        DiscreteBeliefNode node = (DiscreteBeliefNode) object;
        return this.graph.getUniqueID().equals(node.graph.getUniqueID())
                && this.content.equals(node.content)
                && this.cpt.equals(node.cpt);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 3 * result + graph.getUniqueID().hashCode();
        result = 3 * result + cpt.hashCode();
        return result;
    }
}
