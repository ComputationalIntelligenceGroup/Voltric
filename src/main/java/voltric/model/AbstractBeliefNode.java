package voltric.model;

import voltric.graph.DirectedNode;
import voltric.graph.Edge;
import voltric.model.visitor.BeliefNodeVisitor;
import voltric.potential.Potential;
import voltric.variables.DiscreteVariable;
import voltric.variables.Variable;
import voltric.variables.modelTypes.VariableType;
import voltric.variables.util.VariableUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides an abstract implementation for belief nodes. This class methods will be overriden when
 * necessary by children classes like {@link DiscreteBeliefNode} and {@link ContinuousBeliefNode}.
 *
 * Each node has an unique ID because we cannot test equality with all the fields or there would be an infinite equality loop.
 * And because this class and its subclasses are not Clonable, there would not be two nodes with the same ID. This is why
 * its {@code equals} and {@code hashcode} methods do not take on consideration the {@code graph} field or
 * the {@code bayesianNetwork} field (circular dependencies).
 *
 * @author kmpoon
 * @author ferjorosa
 */
public abstract class AbstractBeliefNode extends DirectedNode<Variable> {

    /** The Bayesian network the node belongs to. */
    protected AbstractBayesNet bayesianNetwork;

    /**
     * Constructs a belief node for {@code variable} in the {@code graph}.
     *
     * @param bayesianNetwork in which the belief node is added to.
     * @param variable variable of this node.
     */
    protected AbstractBeliefNode(AbstractBayesNet bayesianNetwork, Variable variable) {
        super(bayesianNetwork.dag,variable);
        this.bayesianNetwork = bayesianNetwork;
    }

    /**
     * Returns the Potential of this belief node.
     *
     * @return Potential of this belief node.
     */
    public abstract Potential getPotential();

    /**
     * Uses the given {@code Potential} to be the Potential of this belief node.
     * It assumes that this Potential is of the appropriate type.
     *
     * @param potential Potential to be used by this belief node.
     */
    public abstract void setPotential(Potential potential);

    /**
     * Returns the variable of this node.
     *
     * @return variable of this node.
     */
    public abstract Variable getVariable();

    /**
     * Returns the standard dimension, namely, the number of free parameters, corresponding to this node.
     *
     * @return the standard dimension of this node.
     */
    public abstract int computeDimension();

    /**
     * The accept method for the BeliefNodeVisitor (see 'Visitor pattern')
     *
     * @param visitor the visitor.
     * @param <T> the return type of the BeliefNodeVisitor.visit().
     * @return the result of the visit.
     */
    public abstract <T> T accept(BeliefNodeVisitor<T> visitor);

    /**
     * Attaches the specified incoming edge to this node. This implementation extends
     * <code>DirectedNode.attachInEdge(Edge edge)</code> such that the CPT of this node will be updated as well.
     *
     * <p>
     * <b>Note: Only <code>BayesNet.addEdge(AbstractNode, AbstractNode)</code>
     * is supposed to call this method. </b>
     * </p>
     *
     * @param edge incoming edge to be attached to this node.
     * @see DiscreteBayesNet#addEdge(AbstractBeliefNode, AbstractBeliefNode)
     */
    protected void attachInEdge(Edge<Variable> edge) {
        super.attachInEdge(edge);

        // new CPT should include variable attached to parent
        Variable parent = ((AbstractBeliefNode) edge.getTail()).getVariable();
        setPotential(getPotential().addParentVariable(parent));
    }

    /**
     * Detaches the specified incoming edge from this node. This implementation
     * extends <code>DirectedNode.detachInEdge(Edge)</code> such that the CPT will be updated as well.
     *
     * <p>
     * <b>Note: Only <code>BayesNet.removeEdge(Edge)</code> is supposed to call
     * this method. </b>
     * </p>
     *
     * @param edge incoming edge to be detached from this node.
     * @see DiscreteBayesNet#removeEdge(Edge)
     */
    protected void detachInEdge(Edge<Variable> edge) {
        super.detachInEdge(edge);

        // new CPT should exclude variable attached to old parent
        Variable oldParent = ((AbstractBeliefNode) edge.getTail()).getVariable();
        setPotential(getPotential().removeParentVariable(oldParent));
    }

    /**
     * Returns the casted collection of children. This exists because BayesNets' methods only accept Belief nodes as parameters.
     * An given the lack of Java's collection covariance, a new method had to be created with a different name.
     *
     * @return the casted collection of children.
     */
    public Set<AbstractBeliefNode> getChildrenNodes() {
        return this.getChildren().stream().map(x->(AbstractBeliefNode) x).collect(Collectors.toSet());
    }

    /**
     * Returns the casted collection of parents. This exists because BayesNets' methods only accept Belief nodes as parameters.
     * An given the lack of Java's collection covariance, a new method had to be created with a different name.
     *
     * @return the casted collection of parents.
     */
    public Set<AbstractBeliefNode> getParentNodes(){
        return this.getParents().stream().map(x->(AbstractBeliefNode) x).collect(Collectors.toSet());
    }

    /**
     * Returns the belief node's name.
     *
     * @return the belief node's name.
     */
    public String getName(){
        return this.getVariable().getName();
    }

    /**
     * Returns a list containing the discrete variables of its parent nodes, excluding any continuous variables.
     *
     * @return list containing node variable and parent variables.
     */
    public List<DiscreteVariable> getDiscreteParentVariables() {
        final List<DiscreteVariable> list = new ArrayList<>(getParents().size());

        Collection<DirectedNode<Variable>> parentNodes = getParents();
        for (DirectedNode parentNode : parentNodes) {
            AbstractBeliefNode beliefNode = (AbstractBeliefNode) parentNode;

            if (parentNode instanceof DiscreteBeliefNode)
                list.add((DiscreteVariable) beliefNode.getVariable());
        }

        return list;
    }

    /**
     * Checks if the belief node is a manifest node (observable). This information is given by its associated variable.
     *
     * @return {@code true} if its variable's type == VariableType.LATENT_VARIABLE; {@code false} otherwise.
     */
    public final boolean isLatent(){
        return this.getVariable().getType() == VariableType.LATENT_VARIABLE;
    }

    /**
     * Checks if the belief node is a manifest node (observable). This information is given by its associated variable.
     *
     * @return {@code true} if its variable's type == VariableType.MANIFEST_VARIABLE; {@code false} otherwise.
     */
    public final boolean isManifest(){
        return this.getVariable().getType() == VariableType.MANIFEST_VARIABLE;
    }

    /**
     * Returns {@code true} if the object is a {@code AbstractBeliefNode} with equal fields (inherited ones included).
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

        AbstractBeliefNode node = (AbstractBeliefNode) object;
        return this.graph.getUniqueID().equals(node.graph.getUniqueID())
                && this.content.equals(node.content);
    }

    /**
     * Returns the object's hashcode. An {@code AbstractBeliefNode} extends {@code DirectedNode} so we can provide
     * a hashcode implemetation that avoids the use of the {@code bayesianNetwork} field (circular dependencies) whike
     * providing a unique code.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 17 * result + graph.getUniqueID().hashCode();
        return result;
    }

    /**
     * Returns a string representation of this Belief node. The string representation will be indented by the specified amount.
     *
     * @param amount amount by which the string representation is to be indented.
     * @return a string representation of this Belief node.
     */
    @Override
    public String toString(int amount) {
        // amount must be non-negative
        assert amount >= 0;

        // prepares white space for indent
        StringBuffer whiteSpace = new StringBuffer();
        for (int i = 0; i < amount; i++) {
            whiteSpace.append("\t");
        }

        // builds string representation
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(whiteSpace);

        List<DiscreteVariable> parents = getDiscreteParentVariables();

        if (parents.size() > 0) {
            stringBuffer.append(String.format("P(%s| %s) {\n", getVariable()
                    .toString(), VariableUtil.getJointName(parents, ", ")));
        } else {
            stringBuffer.append(String.format("P(%s) {\n", getVariable()
                    .toString()));
        }

        if (getPotential() != null) {
            stringBuffer.append(getPotential());
            stringBuffer.append("\n");
        } else
            stringBuffer.append("Potential: nil\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("};\n");

        return stringBuffer.toString();
    }

    /**
     * Returns a string representation of this Belief node. By default returns a string representation indented by 1.
     *
     * @return a string representation of this Belief node.
     * @see #toString(int)
     */
    public final String toString(){
        return this.toString(1);
    }
}