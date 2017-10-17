package voltric.model;

import voltric.graph.AbstractNode;
import voltric.graph.DirectedAcyclicGraph;
import voltric.graph.Edge;
import voltric.variables.Variable;

import java.util.HashMap;
import java.util.List;

/**
 * This class implements a base class for Bayesian networks. It provides the general BN methods and fields.
 * IT will be extended by other class that represents more specific models (i.e., A Discrete Bayesian network where
 * both the manifest and the latent nodes are discrete).
 *
 * @param <L> type of latent belief nodes.
 * @param <M> type of manifest belief nodes.
 */
public abstract class AbstractBayesNet<L extends AbstractBeliefNode,M extends AbstractBeliefNode>{

    /** Bayesian network's name */
    protected String name;

    /** The network's DAG */
    protected DirectedAcyclicGraph<Variable> dag;

    /**
     * Default constructor. It creates an empty Bayesian network with a default name.
     */
    public AbstractBayesNet(){
        this.name = "Abstract Bayesian network";
        this.dag = new DirectedAcyclicGraph<>();
    }

    /**
     * Constructs an empty bayesian network with a specific name.
     *
     * @param name name of the new network.
     */
    public AbstractBayesNet(String name){
        // name cannot be blank
        if(name.length() <= 0)
            throw new IllegalArgumentException("Bayesian network name cannot be blank");

        this.name = name;
        this.dag = new DirectedAcyclicGraph<>();
    }

    /**
     * Returns the collection of manifest nodes.
     *
     * @return the collection of manifest nodes.
     */
    public abstract List<M> getManifestNodes();

    /**
     * Returns the collection of latent nodes.
     *
     * @return the collection of latent nodes.
     */
    public abstract List<L> getLatentNodes();

    /**
     * Returns the name of the Bayesian network.
     *
     * @return the name of the Bayesian network.
     */
    public String getName(){
        return this.name;
    }

    /**
     * Returns the DAG object that represents the structure of this Bayesian network.
     *
     * Note: It is marked as 'protected' to hide the implementation of the class. Not all the graph methods are applicable
     * in a Bayesian network. Therefore, only he ones that make sense are visible to the user.
     *
     * @return the DAG object that represents the structure of this Bayesian network
     */
    protected DirectedAcyclicGraph<Variable> getDag(){
        return this.dag;
    }

    /**
     * Returns the DAG's edges.
     *
     * @return the DAG's edges.
     */
    public List<Edge<Variable>> getEdges(){
        return this.dag.getEdges();
    }

    /**
     * Returns the node with the specified name, or null if not found
     *
     * @param name name of the target node
     * @return the node with the specified name, or null if not found
     */
    public AbstractBeliefNode getNode(String name) {
        HashMap<Variable, AbstractNode<Variable>> nodeContents = this.dag.getContents();
        for(Variable nodeVar: nodeContents.keySet()){
            if(name.equals(nodeVar.getName()))
                return (AbstractBeliefNode) nodeContents.get(nodeVar);
        }
        throw new IllegalArgumentException("The argument name doesn't correspond to any Belief node");
    }

    /**
     * Returns the node to which the specified variable is attached in this BN.
     *
     * @param variable variable attached to the node.
     * @return the node to which the specified variable is attached,
     *         or {@code null} if none uses this variable.
     */
    public AbstractBeliefNode getNode(Variable variable){
        return (AbstractBeliefNode) this.dag.getNode(variable);
    }

    /**
     * Returns the number of nodes.
     *
     * @return the number of nodes.
     */
    public int getNumberOfNodes(){
        return this.dag.getNumberOfNodes();
    }

    /**
     * Returns the number of edges.
     *
     * @return the number of edges.
     */
    public int getNumberOfEdges(){
        return this.dag.getNumberOfEdges();
    }

    /**
     * Adds an edge that connects the two specified nodes to this BN and returns
     * the edge. This implementation extends <code>AbstractGraph.addEdge(AbstractNode, AbstractNode)</code> such
     * that all loglikelihoods will be expired.
     *
     * <p>The resulting edge is {@code head <- tail}.</p>
     *
     * @param head head of the edge.
     * @param tail tail of the edge.
     * @return the edge that was added to this BN.
     */
    public Edge<Variable> addEdge(AbstractBeliefNode head, AbstractBeliefNode tail) {
        return this.dag.addEdge(head, tail);
    }

    /**
     * Removes the specified edge from this BN.
     *
     * @param edge edge to be removed from this BN.
     */
    public void removeEdge(Edge<Variable> edge) {
        this.dag.removeEdge(edge);
    }

    /**
     * Reverses the specified edge.
     *
     * @param edge the BN's edge to be reversed.
     */
    public Edge<Variable> reverseEdge(Edge<Variable> edge) {
        return this.dag.reverseEdge(edge);
    }

    /**
     * Checks if the graph contains an edge between the head and the tail nodes.
     *
     * @param head the head node.
     * @param tail the tail node.
     * @return {@code true} if the edge is present adn {@code false}
     */
    public boolean containsEdge(AbstractBeliefNode head, AbstractBeliefNode tail){
        return this.dag.containsEdge(head, tail);
    }

    /**
     * Returns whether the Bayesian network is a Tree or not.
     *
     * @return {@code true} if it posses a tree-structure or {@code false} otherwise.
     */
    public boolean isTree(){
        return this.dag.isTree();
    }

    /**
     * Returns {@code true} if the object is an {@code AbstractBayesNet} with equal fields.
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

        AbstractBayesNet bayesNet = (AbstractBayesNet) object;
        return this.name.equals(bayesNet.name)
                && this.dag.equals(bayesNet.dag);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + dag.hashCode();
        return result;
    }

    /**
     * Returns a string representation of this Bayesian network. The string representation will be indented by the specified amount.
     *
     * @param amount amount by which the string representation is to be indented.
     * @return a string representation of this Bayesian network.
     */
    public abstract String toString(int amount);

    /**
     * Returns a string representation of this Bayesian network. By default returns a string representation indented by 1.
     *
     * @return a string representation of this Bayesian network.
     * @see #toString(int)
     */
    public final String toString(){
        return this.toString(1);
    }
}
