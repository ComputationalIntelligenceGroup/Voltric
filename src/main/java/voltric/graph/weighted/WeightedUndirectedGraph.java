package voltric.graph.weighted;

import voltric.graph.AbstractNode;
import voltric.graph.Edge;
import voltric.graph.UndirectedGraph;
import voltric.graph.UndirectedNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides an implementation of an undirected graph where its edges have an associated weight. It inherits
 * {@link UndirectedGraph}. It provides a specific set of methods and overrides some of the parent, to provide an
 * adequate interface for the user.
 *
 * This approach, instead of adding weight to all kinds of graphs by default, allows more flexibility.
 * TODO: removeEdge y check removeNode
 *
 * @param <T> the node's content type.
 */
public class WeightedUndirectedGraph<T> extends UndirectedGraph<T> implements WeightedGraph<T>{

    /** The Map containing all the corresponding edge weights */
    protected Map<Edge<T>, Double> edgeWeights;

    /**
     * Default constructor. It will create an empty graph.
     */
    public WeightedUndirectedGraph(){
        super();
        this.edgeWeights = new HashMap<>();
    }

    /**
     * Copy constructor. The nodes, edges and edge weights of this graph are added according to those of the specified graph.
     * However, the graph elements being added are not the same instances as those of the argument graph.
     *
     * @param graph the graph being copied.
     */
    public WeightedUndirectedGraph(WeightedUndirectedGraph<T> graph){

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
     * @param graph the undirected graph being used as base (that will be copied).
     */
    public WeightedUndirectedGraph(UndirectedGraph<T> graph){
        // Calling the parent copy constructor
        super(graph);

        this.edgeWeights = new HashMap<>();

        // Each edge is assigned a default weight
        for(Edge<T> edge: this.getEdges())
            this.setEdgeWeight(edge, DEFAULT_EDGE_WEIGHT);
    }

    /**
     * Adds an edge with default weight that connects the two specified nodes to this graph and returns the edge.
     * Given that the implementation of the Edge class is "directed", only one edge from head to tail or viceversa is allowed.
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
     * Given that the implementation of the Edge class is "directed", only one edge from head to tail or viceversa is allowed.
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
     * Generates the maximum weight spanning tree from current graph.
     *
     * @return the maximum weight spanning tree of current graph.
     */
    // Utiliza el algoritmo de Prim, por eso pide un start. Suele ser aleatorio
    // El grafo deberia ser completo porque contiene todos los costes
    public WeightedUndirectedGraph<T> maximumWeightSpanningTree(UndirectedNode<T> start){

        if(!this.containsNode(start))
            throw new IllegalArgumentException("The node doesn't belong to the graph");

        if(!this.isCompleteGraph())
            throw new IllegalArgumentException("Graph has to be complete to obtain the MWST");

        // The Maximum Weight Spanning Tree object is created and the start node is added
        WeightedUndirectedGraph<T> maxWST = new WeightedUndirectedGraph<>();
        maxWST.addNode(start.getContent());

        Set<AbstractNode<T>> unvisitedNodes = new HashSet<>(this.getNumberOfNodes());
        unvisitedNodes.addAll(this.getUndirectedNodes());
        unvisitedNodes.remove(start);

        AbstractNode<T> vertex = start;

        while(!unvisitedNodes.isEmpty()){

            double maxEdgeWeight = -Double.MAX_VALUE; // initialized with minimum possible value
            AbstractNode<T> bestNeighbour = null;

            for(AbstractNode<T> neighbour : vertex.getNeighbors()){
                if(unvisitedNodes.contains(neighbour)){

                    Edge<T> edge;

                    //TODO: Tengo que hacer esto ya que los Edges son siempre dirigidos
                    Optional<Edge<T>> possibleEdge = this.getEdge(vertex, neighbour);
                    Optional<Edge<T>> possibleInverseEdge = this.getEdge(neighbour, vertex);

                    // If the graph is complete there should exist one of these edges
                    if(possibleEdge.isPresent())
                        edge = possibleEdge.get();
                    else
                        edge = possibleInverseEdge.get();

                    // Evaluamos su peso
                    if(this.edgeWeights.get(edge) > maxEdgeWeight) {
                        bestNeighbour = neighbour;
                        maxEdgeWeight = this.edgeWeights.get(edge);
                    }
                }
            }

            // Una vez escogido el mejor edge, lo añadimos al mwst con su respectivo peso
            UndirectedNode<T> maxWSTneighbourNode = maxWST.addNode(bestNeighbour.getContent());
            UndirectedNode<T> maxWSTvertexNode = maxWST.getNode(vertex.getContent());
            maxWST.addEdge(maxWSTvertexNode, maxWSTneighbourNode, maxEdgeWeight);

            vertex = bestNeighbour;
            unvisitedNodes.remove(vertex);
        }

        return maxWST;
    }

    /**
     * Generates the minimum weight spanning tree from current graph.
     *
     * @return the minimum weight spanning tree of current graph.
     */
    public WeightedUndirectedGraph<T> minimumWeightSpanningTree(UndirectedNode<T> start){

        if(!this.containsNode(start))
            throw new IllegalArgumentException("The node doesn't belong to the graph");

        if(!this.isCompleteGraph())
            throw new IllegalArgumentException("Graph has to be complete to obtain the MWST");

        // The Minimum Weight Spanning Tree object is created and the start node is added
        WeightedUndirectedGraph<T> minWST = new WeightedUndirectedGraph<>();
        minWST.addNode(start.getContent());

        Set<AbstractNode<T>> unvisitedNodes = new HashSet<>(this.getNumberOfNodes());
        unvisitedNodes.addAll(this.getUndirectedNodes());
        unvisitedNodes.remove(start);

        AbstractNode<T> vertex = start;

        while(!unvisitedNodes.isEmpty()){

            double minEdgeWeight = Double.MAX_VALUE;
            AbstractNode<T> bestNeighbour = null;

            for(AbstractNode<T> neighbour : vertex.getNeighbors()){
                if(unvisitedNodes.contains(neighbour)){

                    Edge<T> edge;

                    //TODO: Tengo que hacer esto ya que los Edges son siempre dirigidos
                    Optional<Edge<T>> possibleEdge = this.getEdge(vertex, neighbour);
                    Optional<Edge<T>> possibleInverseEdge = this.getEdge(neighbour, vertex);

                    // If the graph is complete there should exist one of these edges
                    if(possibleEdge.isPresent())
                        edge = possibleEdge.get();
                    else
                        edge = possibleInverseEdge.get();

                    // Evaluamos su peso
                    if(this.edgeWeights.get(edge) < minEdgeWeight) {
                        bestNeighbour = neighbour;
                        minEdgeWeight = this.edgeWeights.get(edge);
                    }
                }
            }

            // Una vez escogido el mejor edge, lo añadimos al mwst con su respectivo peso
            UndirectedNode<T> maxWSTneighbourNode = minWST.addNode(bestNeighbour.getContent());
            UndirectedNode<T> maxWSTvertexNode = minWST.getNode(vertex.getContent());
            minWST.addEdge(maxWSTvertexNode, maxWSTneighbourNode, minEdgeWeight);

            vertex = bestNeighbour;
            unvisitedNodes.remove(vertex);
        }

        return minWST;
    }

    /**
     * Returns {@code true} if the object is a {@code WeightedUndirectedGraph} with equal fields (inherited ones included).
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

        WeightedUndirectedGraph graph = (WeightedUndirectedGraph) object;
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
    public WeightedUndirectedGraph<T> clone(){
        return new WeightedUndirectedGraph<>(this);
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
        stringBuffer.append("weighted undirected graph {\n");

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
