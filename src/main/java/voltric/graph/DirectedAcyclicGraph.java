package voltric.graph;

/**
 * This class provides an implementation for directed acyclic graphs (DAGs).
 *
 * @author Yi Wang
 * @author ferjorosa
 */
public class DirectedAcyclicGraph<T> extends DirectedGraph<T>{

    /**
     * Default constructor.
     */
    public DirectedAcyclicGraph(){
        super();
    }

    /**
     * Copy constructor. The nodes and edges of this graph are added according to those of the specified graph.
     * However, the graph elements being added are not the same instances as those of the argument graph.
     *
     * @param graph the graph being copied.
     */
    public DirectedAcyclicGraph(DirectedAcyclicGraph<T> graph){
        super(graph);
    }

    /**
     * Adds an edge that connects the two specified nodes to this graph and returns the edge. T
     * here is going to be a run time exception if the resulting graph contains a cycle.
     *
     * @param head head of the edge.
     * @param tail tail of the edge.
     * @return the edge that was added to this graph.
     */
    @Override
    public Edge<T> addEdge(AbstractNode<T> head, AbstractNode<T> tail) {
        // this graph must contain both nodes
        if(!containsNode(head) || !containsNode(tail))
            throw new IllegalArgumentException("The graph must contain both nodes");

        // nodes must be distinct; otherwise, self loop will be introduced.
        if(head.equals(tail))
            throw new IllegalArgumentException("Both nodes must be distinct; otherwise, a self loop will be introduced");

        // nodes cannot be neighbors; otherwise, a duplicated edge will be introduced
        if(head.hasNeighbor(tail))
            throw new IllegalArgumentException("Nodes cannot be neighbours; otherwise either a duplicated edge will be introduced");

        // This graph cannot contain a directed path from the head to the tail; otherwise a directed cycle will be introduced
        if(this.containsPath(head, tail))
            throw new IllegalArgumentException("This graph cannot contain a directed path from the head to the tail; otherwise a directed cycle will be introduced");

        // creates the edge
        Edge<T> edge = new Edge<>(head, tail);

        // adds the edge to the list of edges in this graph
        this.edges.add(edge);

        // attaches the edge to both ends
        head.attachEdge(edge);
        tail.attachEdge(edge);

        ((DirectedNode<T>) head).attachInEdge(edge);
        ((DirectedNode<T>) tail).attachOutEdge(edge);

        return edge;
    }

    /**
     * Returns the moral graph of this graph. I assume that you know what is a
     * moral graph. Otherwise, you are not supposed to use this method :-)
     *
     * @return the moral graph of this graph.
     */
    public final UndirectedGraph<T> computeMoralGraph() {
        UndirectedGraph<T> moralGraph = new UndirectedGraph<>();

        // copies nodes in this graph
        for (AbstractNode<T> node : this.nodes) {
            moralGraph.addNode(node.getContent());
        }

        // copies edges in this graph with directions dropped
        for (Edge<T> edge : this.edges) {
            moralGraph.addEdge(moralGraph.getNode(edge.getHead().getContent()),
                    moralGraph.getNode(edge.getTail().getContent()));
        }

        // connects nodes that are divorced parents of some node in this DAG.
        for (AbstractNode<T> node : this.nodes) {
            DirectedNode<T> dNode = (DirectedNode<T>) node;

            for (DirectedNode<T> parent1 : dNode.getParents()) {
                AbstractNode<T> neighbor1 = moralGraph.getNode(parent1.getContent());

                for (DirectedNode<T> parent2 : dNode.getParents()) {
                    AbstractNode<T> neighbor2 = moralGraph.getNode(parent2.getContent());

                    if (neighbor1 != neighbor2 && !neighbor1.hasNeighbor(neighbor2)) {
                        moralGraph.addEdge(neighbor1, neighbor2);
                    }
                }
            }
        }

        return moralGraph;
    }

    /**
     * Returns {@code true} if the object is a {@code DirectedAcyclicGraph} with equal fields (inherited ones included).
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

        DirectedAcyclicGraph graph = (DirectedAcyclicGraph) object;
        return this.nodes.equals(graph.nodes)
                && this.edges.equals(graph.edges)
                && this.contents.equals(graph.contents);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = nodes.hashCode();
        result = 23 * result + edges.hashCode();
        result = 23 * result + contents.hashCode();
        return result;
    }

    /**
     * Creates and returns a deep copy of this graph. This implementation copies everything in this graph. Consequently,
     * it is safe to do anything you want to the deep copy.
     *
     * @return a deep copy of this graph.
     */
    @Override
    public DirectedAcyclicGraph<T> clone() {
        return new DirectedAcyclicGraph<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString(int amount) {
        // amount must be non-negative
        if(amount <= 0)
            throw new IllegalArgumentException("The amount must be positive");

        // prepares white space for indent
        StringBuffer whiteSpace = new StringBuffer();
        for (int i = 0; i < amount; i++) {
            whiteSpace.append("\t");
        }

        // builds string representation
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(whiteSpace);
        stringBuffer.append("directed acyclic graph {\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tnumber of nodes = " + getNumberOfNodes() + ";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tnodes = {\n");

        for (AbstractNode node : this.nodes) {
            stringBuffer.append(node.toString(amount + 2));
        }

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\t};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer
                .append("\tnumber of edges = " + getNumberOfEdges() + ";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tedges = {\n");

        for (Edge<T> edge : this.edges) {
            stringBuffer.append(edge.toString(amount + 2));
        }

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\t};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("};");

        return stringBuffer.toString();
    }
}
