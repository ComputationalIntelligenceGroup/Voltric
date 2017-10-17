package voltric.graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides an implementation for undirected graphs.
 *
 * @author Yi Wang
 * @author ferjorosa
 */
public class UndirectedGraph<T> extends AbstractGraph<T> {

    /**
     * Default constructor.
     */
    public UndirectedGraph() {
        super();
    }

    /**
     * Copy constructor. The nodes and edges of this graph are added according to those of the specified graph.
     * However, the graph elements being added are not the same instances as those of the argument graph.
     *
     * @param graph the graph being copied.
     */
    public UndirectedGraph(UndirectedGraph<T> graph){
        super(graph);
    }

    /** {@inheritDoc} */
    @Override
    public UndirectedNode<T> addNode(AbstractNode<T> node) {
        if(!(node instanceof UndirectedNode))
            throw new IllegalArgumentException("Only undirected nodes are allowed");

        // adds the node to the list of nodes in this graph
        this.nodes.add(node);

        // maps name to node
        this.contents.put(node.getContent(), node);

        return (UndirectedNode<T>) node;
    }

    /** {@inheritDoc} */
    @Override
    public UndirectedNode<T> addNode(T content) {

        // name must be unique in this graph
        if(this.containsNode(content))
            throw new IllegalArgumentException("Node names must be unique.");

        // creates node
        UndirectedNode<T> node = new UndirectedNode<>(this, content);

        // adds node to the list of nodes in this graph
        this.nodes.add(node);

        // maps name to node
        this.contents.put(content, node);

        return node;
    }

    /**
     * Adds an edge that connects the two specified nodes to this graph and returns the edge. Given that the implementation
     * of the Edge class is "directed", only one edge from head to tail or viceversa is allowed.
     *
     * @param head head of the edge.
     * @param tail tail of the edge.
     * @return the edge that was added to this graph.
     */
    @Override
    public Edge<T> addEdge(AbstractNode<T> head, AbstractNode<T> tail) {

        // this graph must contain both nodes
        if(!containsNode(head) || !containsNode(tail))
            throw new IllegalArgumentException("The graph must contain both nodes (" + head.getContent() +" && "+tail.getContent()+").");

        // nodes must be distinct; otherwise, self loop will be introduced.
        if(head.equals(tail))
            throw new IllegalArgumentException("Both nodes must be distinct; otherwise, a self loop will be introduced");

        // nodes cannot be neighbors; otherwise, a duplicated edge will be introduced (remember this case is undirected,
        // but the Edge class has been implemented for directed edges)
        if(head.hasNeighbor(tail) || tail.hasNeighbor(head))
            throw new IllegalArgumentException("Nodes cannot be neighbours; otherwise a duplicated edge will be introduced");

        // creates edge
        Edge<T> edge = new Edge<>(head, tail);

        // adds edge to the list of edges in this graph
        edges.add(edge);

        // attaches edge to both ends
        head.attachEdge(edge);
        tail.attachEdge(edge);

        return edge;
    }

    /** {@inheritDoc} */
    @Override
    public void removeEdge(Edge<T> edge) {
        // this graph must contains the edge
        if(!this.containsEdge(edge))
            throw new IllegalArgumentException("the graph must contain the argument edge");

        // removes edge from the list of edges in this graph
        edges.remove(edge);

        // detachs edge from both ends
        edge.getHead().detachEdge(edge);
        edge.getTail().detachEdge(edge);
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsEdge(AbstractNode<T> head, AbstractNode<T> tail) {
        // this graph must contain both nodes
        if(!containsNode(head) || !containsNode(tail))
            throw new IllegalArgumentException("The graph must contain both nodes");

        // creates the edges
        Edge<T> edge = new Edge<>(head, tail);
        Edge<T> inverseEdge = new Edge<>(tail, head);

        return this.edges.contains(edge) || this.edges.contains(inverseEdge);
    }

    /** {@inheritDoc} */
    @Override
    public UndirectedNode<T> getNode(T content) {
        return (UndirectedNode<T>) super.getNode(content);
    }

    /**
     * Returns the list of nodes in this graph. For the sake of efficiency, this implementation returns the reference to
     * the protected field. Make sure you understand this before using this method.
     *
     * <p><b>Note:</b> Given the lack of COVARIANCE in the Java's collection library, this method cannot be properly
     * overridden. That is why {@code getUndirectedNodes()} exists.</p>
     *
     * @return the list of nodes in this graph.
     *
     * @see #getUndirectedNodes()
     */
    @Override
    public List<AbstractNode<T>> getNodes() {
        return super.getNodes();
    }

    /**
     * Returns a List containing the graph's casted nodes.
     *
     * @return a List containing the graph's casted nodes.
     */
    public List<UndirectedNode<T>> getUndirectedNodes(){
        return this.getNodes().stream().map(x-> (UndirectedNode<T>) x).collect(Collectors.toList());
    }

    /**
     * Traverses this graph in a depth first manner. This implementation discovers the specified node and then recursively
     * explores its unvisited neighbors. Given the directed nature of the {@code Edge} class implementation, the process
     * is not as straightforward as would be if {@code Edge} was undirected, but works correctly.
     *
     * @param node node to start with.
     * @param visitTimes The begining number of visits.
     * @param d map from nodes to their discovering time.
     * @param f map from nodes to their finishing time.
     * @return the elapsed time.
     */
    @Override
    public final int depthFirstSearch(AbstractNode<T> node, int visitTimes, Map<AbstractNode<T>, Integer> d, Map<AbstractNode<T>, Integer> f) {
        // this graph must contain node
        if(!this.containsNode(node))
            throw new IllegalArgumentException("The graph must contain the node "+ node.getContent());

        // discovers node
        d.put(node, visitTimes++);

        // explores unvisited neighbors (sort of an undirected approach)
        for (AbstractNode<T> neighbor : node.getNeighbors()) {
            if (!d.containsKey(neighbor)) {
                visitTimes = depthFirstSearch(neighbor, visitTimes, d, f);
            }
        }

        // finishes node
        f.put(node, visitTimes++);

        return visitTimes;
    }

    /** {@inheritDoc} */
    @Override
    //TODO: Test
    public boolean isTree(){
        // 1 - The number of edges in a tree must be number of nodes minus 1
        if (this.getNumberOfEdges() != this.getNumberOfNodes() - 1)
            return false;

        // a Depth First Search is performed from a random node
        HashMap<AbstractNode<T>, Integer> d = new HashMap<>();
        HashMap<AbstractNode<T>, Integer> f = new HashMap<>();
        this.depthFirstSearch(this.getNodes().get(0),0, d,f);

        // 3 - All nodes in a tree must be connected to the root
        if(d.size() != this.getNumberOfNodes())
            return false;

        // 4 - Each node can only be visited once
        for(int i: f.values())
            if(i > 1)
                return false;

        return true;
    }

    /**
     * Eliminates the specified node from this graph. To eliminate a node from an {@code UnidrectedGraph} is to marry
     * its broken neighbors and remove it from the graph.
     *
     * @param node node to be eliminated.
     */
    public final void eliminateNode(AbstractNode<T> node) {
        // this graph must contain node
        if(!this.containsNode(node))
            throw new IllegalArgumentException("The graph must contain the node "+ node.getContent());

        // marries broken neighbors
        for (AbstractNode<T> neighbor1 : node.getNeighbors()) {
            for (AbstractNode<T> neighbor2 : node.getNeighbors()) {
                if (neighbor1 != neighbor2 && !neighbor1.hasNeighbor(neighbor2)) {
                    addEdge(neighbor1, neighbor2);
                }
            }
        }

        // removes node from graph
        removeNode(node);
    }

    /**
     * Returns the contents of the nodes in this graph in a minimum deficiency order.
     *
     * @return the contents of the nodes in this graph in a minimum deficiency order.
     */
    public final LinkedList<T> minimumDeficiencySearch() {
        // we use LinkedList for fast iteration
        LinkedList<T> order = new LinkedList<>();

        // works on a deep copy of this graph
        UndirectedGraph<T> copy = this.clone();

        // successively eliminates node with minimum deficiency
        while (copy.getNumberOfNodes() > 0) {
            int minDef = Integer.MAX_VALUE;
            AbstractNode<T> elimNode = null;

            for (AbstractNode<T> node : copy.nodes) {
                int deficiency = ((UndirectedNode<T>) node).computeDeficiency();

                if (deficiency < minDef) {
                    minDef = deficiency;
                    elimNode = node;
                }
            }

            // eliminates selected node
            copy.eliminateNode(elimNode);

            // appends node's content to the contents list
            order.add(elimNode.getContent());
        }

        return order;
    }

    public boolean isCompleteGraph(){
        // A complete graph with n vertices has n*(n-1)/2 edges
        int n = this.getNumberOfNodes();
        return this.getNumberOfEdges() == n*(n-1)/2;
    }

    /**
     * Returns {@code true} if the object is a {@code UndirectedGraph} with equal fields (inherited ones included).
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

        UndirectedGraph graph = (UndirectedGraph) object;
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
        result = 5 * result + edges.hashCode();
        result = 5 * result + contents.hashCode();
        return result;
    }

    /**
     * Creates and returns a deep copy of this graph. This implementation copies everything in this graph. Consequently,
     * it is safe to do anything you want to the deep copy.
     *
     * @return a deep copy of this graph.
     */
    @Override
    public UndirectedGraph<T> clone() {
        return new UndirectedGraph<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public String toString(int amount) {
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
        stringBuffer.append("undirected graph {\n");

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
        }

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\t};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("};\n");

        return stringBuffer.toString();
    }

}
