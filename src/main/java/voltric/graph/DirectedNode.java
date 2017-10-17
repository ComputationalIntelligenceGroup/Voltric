package voltric.graph;

import voltric.model.AbstractBeliefNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * This class provides an implementation for the {@link DirectedGraph} nodes.
 *
 * Each node has an unique ID because we cannot test equality with all the fields or there would be an infinite equality loop.
 * And because this class and its subclasses are not Clonable, there would not be two nodes with the same ID. This is why
 * its {@code equals} and {@code hashcode} methods do not take on consideration the {@code graph} field (circular dependencies).
 *
 * @author Yi Wang
 * @author ferjorosa
 */
public class DirectedNode<T> extends AbstractNode<T> {

    /** The map from parents of this node to incoming edges. we use {@code LinkedHashMap} for predictable iteration order. */
    protected LinkedHashMap<DirectedNode<T>, Edge<T>> parents;

    /** The map from children of this node to outgoing edges. we use {@code LinkedHashMap} for predictable iteration order. */
    protected LinkedHashMap<DirectedNode<T>, Edge<T>> children;

    /**
     * Constructs a node with a specific name to belong to a the specified graph.
     *
     * <p>
     * <b>Note: Besides constructors of subclasses, only {@code DirectedGraph.addNode(String)} is supposed to call
     * this method. </b>
     * </p>
     *
     * @param graph the graph it belongs to.
     * @param content content of this node
     * @see DirectedAcyclicGraph#addNode(Object)
     */
    protected DirectedNode(AbstractGraph<T> graph, T content) {
        super(graph, content);

        this.parents = new LinkedHashMap<>();
        this.children = new LinkedHashMap<>();
    }

    /** {@inheritDoc} */
    public DirectedGraph<T> getGraph(){
        return (DirectedGraph<T>) this.graph;
    }

    /**
     * Returns the edges that come from this node. The edges that coincide its children.
     */
    @Override
    public Collection<Edge<T>> getAdjacentEdges() {
        return getChildEdges();
    }

    /**
     * Attaches the specified incoming edge to this node by updating the node's map from parents to incoming edges.
     *
     * <p>
     * <b>Note: Only <code>DirectedGraph.addEdge(AbstractNode, AbstractNode)</code> and <code>AbstractBeliefNode.attachInEdge(Edge)</code>
     * are supposed to call this  method. </b>
     * </p>
     *
     * @param edge incoming edge to be attached to this node.
     * @see DirectedGraph#addEdge(AbstractNode, AbstractNode)
     * @see AbstractBeliefNode#attachInEdge(Edge)
     */
    protected void attachInEdge(Edge<T> edge) {
        // maps tail, namely, the new parent, to edge
        parents.put((DirectedNode<T>) edge.getTail(), edge);
    }

    /**
     * Attaches the specified outgoing edge to this node by updating the map from children to outgoing edges of this node.
     *
     * <p>
     * <b>Note: Only <code>DirectedGraph.addEdge(AbstractNode, AbstractNode)</code> is supposed to call this method. </b>
     * </p>
     *
     * @param edge outgoing edge to be attached to this node.
     * @see DirectedGraph#addEdge(AbstractNode, AbstractNode)
     */
    protected final void attachOutEdge(Edge<T> edge) {
        // maps head, namely, the new child, to edge
        children.put((DirectedNode<T>) edge.getHead(), edge);
    }

    /**
     * Detaches the specified incoming edge from this node by updating the map from parents to incoming edges of this node.
     *
     * <p>
     * <b>Note: Only <code>DirectedGraph.removeEdge(Edge)</code> and  <code>AbstractBeliefNode.detachInEdge(Edge)</code> are
     * supposed to call this method. </b>
     * </p>
     *
     * @param edge incoming edge to be detached from this node.
     * @see DirectedGraph#removeEdge(Edge)
     * @see AbstractBeliefNode#detachInEdge(Edge)
     */
    protected void detachInEdge(Edge<T> edge) {
        // removes tail from the collection of parents of this node
        parents.remove(edge.getTail());
    }

    /**
     * Detaches the specified outgoing edge from this node by updating the map from children to outgoing edges of this node.
     *
     * <p>
     * <b>Note: Only <code>DirectedGraph.removeEdge(Edge)</code> is
     * supposed to call this method. </b>
     * </p>
     *
     * @param edge outgoing edge to be detached from this node.
     * @see DirectedGraph#removeEdge(Edge)
     */
    protected final void detachOutEdge(Edge<T> edge) {
        // removes head from the collection of children of this node
        children.remove(edge.getHead());
    }

    /**
     * Returns the set of children of this node. For the sake of efficiency, this implementation returns the reference to
     * a private field. Make sure you understand this before using this method.
     *
     * @return the set of children of this node.
     */
    public final Set<DirectedNode<T>> getChildren() {
        return children.keySet();
    }

    /**
     * Returns a collection of the edges containing between this node and its children.
     *
     * @return collection of edges to its children
     */
    public Collection<Edge<T>> getChildEdges() {
        return children.values();
    }

    /**
     * Returns the set of descendants of this node. This method differs from {@code getChildren} in that it does
     * a depth first search, it will not just return the immediate children.
     *
     * @return the set of descendants of this node.
     */
    public final Set<AbstractNode<T>> getDescendants() {
        // discovering and finishing time
        HashMap<AbstractNode<T>, Integer> d = new HashMap<>();
        HashMap<AbstractNode<T>, Integer> f = new HashMap<>();

        // starting with this node, DFS
        this.graph.depthFirstSearch(this, 0, d, f);

        // except this node, all discovered nodes are descendants
        Set<AbstractNode<T>> descendants = d.keySet();
        descendants.remove(this);

        return descendants;
    }

    /**
     * Returns the incoming degree of this node.
     *
     * @return the incoming degree of this node.
     */
    public final int getInDegree() {
        return parents.size();
    }

    /**
     * Returns the outgoing degree of this node.
     *
     * @return the outgoing degree of this node.
     */
    public final int getOutDegree() {
        return children.size();
    }

    /**
     * Returns the set of parents of this node. For the sake of efficiency, this
     * implementation returns the reference to a private field. Make sure you
     * understand this before using this method.
     *
     * @return the set of parents of this node.
     */
    public final Set<DirectedNode<T>> getParents() {
        return parents.keySet();
    }

    /**
     * Returns a collection of the edges incident to this node.
     *
     * @return collection of edges from its parents
     */
    public Collection<Edge<T>> getParentEdges() {
        return parents.values();
    }

    /**
     * Returns <code>true</code> if the specified node is a child of this node.
     *
     * @param node node whose relationship to this node is to be tested.
     * @return <code>true</code> if the specified node is a child of this node.
     */
    public final boolean hasChild(AbstractNode<T> node) {
        // Both nodes must be in same graph
        if(!this.graph.containsNode(node))
            throw new IllegalArgumentException("Both nodes must be on the same graph");

        return children.containsKey(node);
    }

    /**
     * Returns <code>true</code> if the specified node is a parent of this node.
     *
     * @param node node whose relationship to this node is to be tested.
     * @return <code>true</code> if the specified node is a parent of this node.
     */
    public final boolean hasParent(AbstractNode<T> node) {
        // Both nodes must be in same graph
        if(!this.graph.containsNode(node))
            throw new IllegalArgumentException("Both nodes must be on the same graph");

        return parents.containsKey(node);
    }

    /**
     * Returns <code>true</code> if this node is a leaf.
     *
     * @return <code>true</code> if this node is a leaf.
     */
    public final boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * Returns <code>true</code> if this node is a root.
     *
     * @return <code>true</code> if this node is a root.
     */
    public final boolean isRoot() {
        return parents.isEmpty();
    }

    /**
     * Returns {@code true} if the object is an {@code DirectedNode} with equal fields (inherited ones included).
     * It avoids circular dependencies by using the graph's unique ID.
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

        DirectedNode node = (DirectedNode) object;
        return this.graph.uniqueID.equals(node.graph.uniqueID)
                && this.content.equals(node.content);
    }

    /**
     * Returns the object's hashcode. It avoids circular dependencies by using the graph's unique ID.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 11 * result + graph.uniqueID.hashCode();
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString(int amount) {
        // amount must be non-negative
        if(amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        // prepares white space for indent
        StringBuffer whiteSpace = new StringBuffer();
        for (int i = 0; i < amount; i++) {
            whiteSpace.append("\t");
        }

        // builds string representation
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(whiteSpace);
        stringBuffer.append("directed node {\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tcontent = \"" + this.content.toString() + "\";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tincoming degree = " + getInDegree() + ";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tparents = { ");
        for (AbstractNode parent : getParents()) {
            stringBuffer.append("\"" + parent.content.toString() + "\" ");
        }
        stringBuffer.append("};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\toutgoing degree = " + getOutDegree() + ";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tchildren = { ");

        for (AbstractNode child : getChildren()) {
            stringBuffer.append("\"" + child.content.toString() + "\" ");
        }

        stringBuffer.append("};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("};\n");

        return stringBuffer.toString();
    }

}