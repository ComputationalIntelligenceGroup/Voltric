/**
 * AbstractNode.java
 * Copyright (C) 2006 Tao Chen, Kin Man Poon, Yi Wang, and Nevin L. Zhang
 */
package voltric.graph;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;

/**
 * This class provides a skeletal implementation for nodes in graphs to minimize the effort required to implement such classes.
 *
 * Each node has an unique ID because we cannot test equality with all the fields or there would be an infinite equality loop.
 * And because this class and its subclasses are not Clonable, there would not be two nodes with the same ID. This is why
 * its {@code equals} and {@code hashcode} methods do not take on consideration the {@code graph} field (circular dependencies).
 * 
 * @author Yi Wang
 * @author ferjorosa
 */
// TODO: Revisar la importancia del uniqueID, dado que la key combinada de graph y name creo que ya es suficiente, al menos en el metodo equals()
// La principal razon por la que se incluyó uniqueID era porque la "key" identificativa podia cambiar, es decir, el nombre.
public abstract class AbstractNode<T> {

	/** Its associated graph. The one this node belongs to. */
	protected AbstractGraph<T> graph;

	/** The content object that is being wrapped by the node */
	protected T content;

	/** The map from neighbors of this node to incident edges. We use a {@code LinkedHashMap} for predictable iteration order.
	 *
	 * Mainly use for UNDIRECTED purposes.
	 */
	protected LinkedHashMap<AbstractNode<T>, Edge<T>> neighbors;

	/**
	 * Constructs a node with the specified name and the specified graph to contain it.
	 * 
	 * @param graph graph to contain this node.
	 * @param content the node's content.
	 */
	protected AbstractNode(AbstractGraph<T> graph, T content) {
		this.graph = graph;
		this.content = content;
		this.neighbors = new LinkedHashMap<>();
	}

	/**
	 * Returns the content of this node.
	 *
	 * @return the content of this node.
	 */
	public final T getContent() {
		return this.content;
	}

	/**
	 * Returns the graph that contains this node.
	 *
	 * @return the graph that contains this node.
	 */
	public AbstractGraph<T> getGraph() {
		return graph;
	}

	/**
	 * Attaches the specified edge to this node by updating the map from neighbors to incident edges of this node.
	 * 
	 * <p>
	 * <b>Note: Only <code>DirectedAcyclicGraph.addEdge(AbstractNode, AbstractNode)</code> and
	 * <code>UndirectedGraph.addEdge(AbstractNode, AbstractNode)</code> are supposed to call this method. </b>
	 * </p>
	 * 
	 * @param edge edge to be attached to this node.
	 * @see DirectedAcyclicGraph#addEdge(AbstractNode, AbstractNode)
	 * @see UndirectedGraph#addEdge(AbstractNode, AbstractNode)
	 */
	protected void attachEdge(Edge<T> edge) {
		// maps opposite, namely, the new neighbor, to edge
		neighbors.put(edge.getOpposite(this), edge);
	}

	/**
	 * Detaches the specified edge from this node by updating the map from neighbors to incident edges of this node.
	 * 
	 * <p>
	 * <b>Note: Only <code>DirectedAcyclicGraph.removeEdge(Edge)</code> and <code>UndirectedGraph.removeEdge(Edge)</code>
	 * are supposed to call this method. </b>
	 * </p>
	 * 
	 * @param edge edge to be detached from this node.
	 * @see DirectedAcyclicGraph#removeEdge(Edge)
	 * @see UndirectedGraph#removeEdge(Edge)
	 */
	protected void detachEdge(Edge<T> edge) {
		// removes opposite node from the collection of this node's neighbours list
		neighbors.remove(edge.getOpposite(this));
	}

	/**
	 * Disposes this node. This implementation sets the graph that contains this node to <code>null</code>
	 * such that the node can be used nowhere.
	 * 
	 * <p>
	 * <b>Note: Only <code>AbstractGraph.removeNode(AbstractNode)</code> is supposed to call this method. </b>
	 * </p>
	 */
	protected final void dispose() {
		graph = null;
	}

	/**
	 * Returns the degree, namely, the number of neighbors, of this node.
	 * 
	 * @return the degree of this node.
	 */
	public final int getDegree() {
		return neighbors.size();
	}

	/**
	 * Returns the collection of edges incident to this node. For the sake of
	 * efficiency, this implementation returns the reference that is backed by a
	 * private field. Make sure you understand this before using this method.
	 * 
	 * @return the collection of edges incident to this node.
	 */
	public final Collection<Edge<T>> getEdges() {
		return neighbors.values();
	}

	/**
	 * Returns a collection of all edges that leads this to its adjacent nodes.
	 * 
	 * @return a collection of all edges that leads this to its adjacent nodes.
	 */
	public abstract Collection<Edge<T>> getAdjacentEdges();

	/**
	 * Return the edge between this node and the argument node. Return {@code null} if there is no edge between them.
	 * 
	 * @param node the argument node.
	 * @return the edge between this node and the argument node. Return {@code null} if there is no edge between them.
	 */
	public Edge<T> getEdge(AbstractNode<T> node) {

		// Both nodes must be in same graph
		if(!graph.containsNode(node))
			throw new IllegalArgumentException("Both nodes must be in the same graph");

		return neighbors.get(node);
	}

	/**
	 * Returns the set of neighbors of this node. For the sake of efficiency, this implementation returns the reference
	 * that is backed by a private field. Make sure you understand this before using this method.
	 * 
	 * @return the set of neighbors of this node.
	 */
	public final Set<AbstractNode<T>> getNeighbors() {
		return neighbors.keySet();
	}

	/**
	 * Returns {@code true} if the specified node is a neighbor of this node.
	 * 
	 * @param node node whose neighborship to this node is to be tested.
	 * @return <code>true</code> if the specified node is a neighbor of this node.
	 */
	public final boolean hasNeighbor(AbstractNode<T> node) {

		// Both nodes must be in same graph
		if(!graph.containsNode(node))
			throw new IllegalArgumentException("Both nodes must be on the same graph");

		return neighbors.containsKey(node);
	}

	/**
	 * Replaces the content of this node.
	 * 
	 * @param content new content of this node.
	 */
	public void setName(T content) {
		// name must be unique in graph
		if(graph.containsNode(content))
			throw new IllegalArgumentException("Node names must be unique in the graph");

		// Remove old contents object from the graph's map
		graph.contents.remove(this.content);
		// Update node's content
		this.content = content;
		graph.contents.put(this.content, this);
	}

	/**
	 * Returns {@code true} if the object is an {@code AbstractNode} with equal {@code graph.uniqueID} & {@code content}.
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

		AbstractNode node = (AbstractNode) object;
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
		result = 31 * result + this.graph.uniqueID.hashCode();
		return result;
	}

	/**
	 * Returns a string representation of this node. This implementation returns <code>toString(0)</code>.
	 * 
	 * @return a string representation of this node.
	 * @see #toString(int)
	 */
	@Override
	public String toString() {
		return toString(1);
	}

	/**
	 * Returns a string representation of this node. The string representation will be indented by the specified amount.
	 * 
	 * @param amount amount by which the string representation is to be indented.
	 * @return a string representation of this node.
	 */
	public abstract String toString(int amount);
}
