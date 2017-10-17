package voltric.graph.weighted;

import voltric.graph.AbstractNode;
import voltric.graph.DirectedAcyclicGraph;
import voltric.graph.Edge;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * This class provides an implementation of a directed acyclic graph where its edges have an associated weight. It inherits
 * {@link DirectedAcyclicGraph}. It provides a specific set of methods and overrides some of the parent, to provide an
 * adequate interface for the user.
 *
 * This approach, instead of adding weight to all kinds of graphs by default, allows more flexibility.
 * TODO: removeEdge y check removeNode
 *
 * @param <T> the node's content type.
 */
public class WeightedDirectedAcyclicGraph<T> extends DirectedAcyclicGraph<T> implements WeightedGraph<T>{

    /** The Map containing all the corresponding edge weights */
    protected Map<Edge<T>, Double> edgeWeights;

    /**
     * Default constructor. It will create an empty graph.
     */
    public WeightedDirectedAcyclicGraph(){
        super();
        this.edgeWeights = new HashMap<>();
    }

    /**
     * Copy constructor. The nodes, edges and edge weights of this graph are added according to those of the specified graph.
     * However, the graph elements being added are not the same instances as those of the argument graph.
     *
     * @param graph the graph being copied.
     */
    public WeightedDirectedAcyclicGraph(WeightedDirectedAcyclicGraph<T> graph){

        this.nodes = new LinkedList<>();
        this.edges = new LinkedList<>();
        this.contents = new HashMap<>();
        this.edgeWeights = new HashMap<>();

        // copies the uniqueID
        this.uniqueID = graph.uniqueID;

        // copies nodes
        for (AbstractNode<T> node : graph.nodes) {
            this.addNode(node.getContent());
        }

        // copies edges
        for (Edge<T> edge : graph.edges) {
            this.addEdge(this.getNode(edge.getHead().getContent()), this.getNode(edge.getTail().getContent()));
        }

        // Sets the edge weights
        for(Edge<T> edge: this.getEdges()) {
            AbstractNode<T> head = graph.getNode(edge.getHead().getContent());
            AbstractNode<T> tail = graph.getNode(edge.getTail().getContent());
            Edge<T> graphEdge = graph.getEdge(head, tail).get();
            this.setEdgeWeight(edge, graph.getEdgeWeight(graphEdge));
        }
    }

    /**
     * Constructs a weighted graph by copying its structure and adding default weights to its edges.
     *
     * @param graph the directed acyclic graph being used as base (that will be copied).
     */
    public WeightedDirectedAcyclicGraph(DirectedAcyclicGraph<T> graph){
        super(graph);

        this.edgeWeights = new HashMap<>();

        // Each edge is assigned a default weight
        for(Edge<T> edge: this.getEdges())
            this.setEdgeWeight(edge, DEFAULT_EDGE_WEIGHT);
    }

    /**
     * Adds an edge with default weight that connects the two specified nodes to this graph and returns the edge.
     *
     * @param head head of the edge.
     * @param tail tail of the edge.
     * @return the edge that was added to this graph.
     */
    @Override
    public Edge<T> addEdge(AbstractNode<T> head, AbstractNode<T> tail) {
        return this.addEdge(head, tail, DEFAULT_EDGE_WEIGHT);
    }
    /**
     * Adds an edge with a specific weight that connects the two specified nodes to this graph and returns the edge.
     *
     * @param head head of the edge.
     * @param tail tail of the edge.
     * @param weight the weight of the edge.
     * @return the edge that was added to this graph.
     */
    public Edge<T> addEdge(AbstractNode<T> head, AbstractNode<T> tail, double weight) {
        Edge<T> newEdge = super.addEdge(head,tail);
        this.setEdgeWeight(newEdge, weight);
        return newEdge;
    }

    /** {@inheritDoc} */
    @Override
    public void removeEdge(Edge<T> edge) {
        super.removeEdge(edge);
        this.edgeWeights.remove(edge);
    }

    /** {@inheritDoc} */
    @Override
    public void setEdgeWeight(Edge<T> edge, double weight) {
        if(!this.containsEdge(edge))
            throw new IllegalArgumentException("The provided edge doesn't belong to the graph");

        this.edgeWeights.put(edge, weight);
    }

    /** {@inheritDoc} */
    @Override
    public double getEdgeWeight(Edge<T> edge) {
        if(!this.containsEdge(edge))
            throw new IllegalArgumentException("The provided edge doesn't belong to the graph");

        return this.edgeWeights.get(edge);
    }

    /** {@inheritDoc} */
    @Override
    public Map<Edge<T>, Double> getEdgeWeights(){
        return this.edgeWeights;
    }

    /**
     * Returns {@code true} if the object is a {@code WeightedDirectedAcyclicGraph} with equal fields (inherited ones included).
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

        WeightedDirectedAcyclicGraph graph = (WeightedDirectedAcyclicGraph) object;
        return this.nodes.equals(graph.nodes)
                && this.edges.equals(graph.edges)
                && this.contents.equals(graph.contents)
                && this.edgeWeights.equals(graph.edgeWeights);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + edgeWeights.hashCode();
        return result;
    }

    /**
     * Creates and returns a deep copy of this graph. This implementation copies everything in this graph. Consequently,
     * it is safe to do anything you want to the deep copy.
     *
     * @return a deep copy of this graph.
     */
    @Override
    public WeightedDirectedAcyclicGraph<T> clone(){
        return new WeightedDirectedAcyclicGraph<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString(int amount){
        // amount cannot be non-negative
        if(amount <= 0)
            throw new IllegalArgumentException("The amount must be positive");

        // prepares white space for indent
        StringBuffer whiteSpace = new StringBuffer();
        for (int i = 0; i < amount; i++) {
            whiteSpace.append('\t');
        }

        // builds string representation
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(whiteSpace);
        stringBuffer.append("weighted directed acyclic graph {\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tnumber of nodes = " + getNumberOfNodes() + ";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tnodes = {\n");

        for (AbstractNode node : nodes) {
            stringBuffer.append(node.toString(amount + 2));
        }

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\t};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tnumber of edges = " + getNumberOfEdges() + ";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tedges = {\n");

        for (Edge<T> edge : edges) {
            stringBuffer.append(edge.toString(amount + 2));
            stringBuffer.append("edge weight = "+ this.edgeWeights.get(edge)+"\n");
        }

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\t};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("};\n");

        return stringBuffer.toString();
    }
}
