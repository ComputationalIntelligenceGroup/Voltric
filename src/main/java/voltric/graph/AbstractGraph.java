/**
 * AbstractGraph.java
 * Copyright (C) 2006 Tao Chen, Kin Man Poon, Yi Wang, and Nevin L. Zhang
 */
package voltric.graph;

import voltric.variables.DiscreteVariable;

import java.util.*;

/**
 * This class provides a skeletal implementation for graphs, to minimize the effort required to implement relevant classes.
 * 
 * @author Yi Wang
 * @author ferjorosa
 */
public abstract class AbstractGraph<T> implements Cloneable {

	/** The list of nodes in this graph. we use <code>LinkedList</code> for fast add iteration (array copies for example). */
	protected LinkedList<AbstractNode<T>> nodes;

	/** The list of edges in this graph. we use <code>LinkedList</code> for fast add  iteration (array copies for example). */
	protected LinkedList<Edge<T>> edges;

	/** The Map from names to node objects, for fast indexation */
	protected HashMap<T, AbstractNode<T>> contents;

	/** Unique ID used for testing equality in both graphs and nodes, avoiding circular dependencies' problems. */
	protected UUID uniqueID;

	/**
	 * Constructs an empty graph.
	 */
	public AbstractGraph() {
		this.nodes = new LinkedList<>();
		this.edges = new LinkedList<>();
		this.contents = new HashMap<>();
		this.uniqueID = UUID.randomUUID();
	}

	/**
	 * Copy constructor. The nodes and edges of this graph are added according to those of the specified graph.
	 * However, the graph elements being added are not the same instances as those of the argument graph.
	 *
	 * @param graph the graph being copied.
	 */
	public AbstractGraph(AbstractGraph<T> graph){
		this.nodes = new LinkedList<>();
		this.edges = new LinkedList<>();
		this.contents = new HashMap<>();

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
	}

	/**
	 * Returns the unique ID of the graph. It is primarily used in the equals and hashcode methods.
	 *
	 * @return the unique ID.
	 */
	public UUID getUniqueID(){
		return this.uniqueID;
	}

	/**
	 * This method allows child classes to add a node that belongs further in the inheritance tree and reuse
	 * the parents' {@code addNode} functionality. For example, in the {@link voltric.model.HLCM}
	 * class, when calling its addNode(String).
	 *
	 * <p>This method should not be called on its own. That is why it is protected. The public addNode method
	 * receives a String as parameter and executes various checks.</p>
	 *
	 * @param node the specific node, created by hand because it is a child class with more specific fields.
	 * @return the node that has been added to this graph.
	 *
	 * @see voltric.model.HLCM#addNode(DiscreteVariable)
	 */
	protected abstract AbstractNode<T> addNode(AbstractNode<T> node);

	/**
	 * Adds a node with the specified name to this graph and returns the node.
	 *
	 * @param content content of the node.
	 * @return the node that has been added to this graph.
	 */
	public abstract AbstractNode<T> addNode(T content);

	/**
	 * Adds an edge that connects the two specified nodes to this graph and returns the edge.
	 *
	 * @param head head of the edge.
	 * @param tail tail of the edge.
	 * @return the edge that was added to this graph.
	 */
	public abstract Edge<T> addEdge(AbstractNode<T> head, AbstractNode<T> tail);

	/**
	 * Removes the specified edge from this graph. <b></b>
	 *
	 * @param edge edge to be removed from this graph.
	 */
	public abstract void removeEdge(Edge<T> edge);

	/**
	 * Checks if the graph contains an edge between the head and the tail nodes.
	 *
	 * @param head the head node.
	 * @param tail the tail node.
	 * @return {@code true} if the edge is present adn {@code false}
	 */
	public abstract boolean containsEdge(AbstractNode<T> head, AbstractNode<T> tail);

	/**
	 * Traverses this graph in a depth first manner. This implementation discovers the specified node and then recursively
	 * explores its unvisited neighbors (children for DAG). This method is VERY important, and should be well
	 * implemented because its implementation will affect other methods like: {@code containsPath} or {@code isTree}.
	 *
	 * @param node node to start with.
	 * @param visitTimes The begining number of visits.
	 * @param d map from nodes to their discovering time.
	 * @param f map from nodes to their finishing time.
	 * @return the elapsed time.
	 */
	public abstract int depthFirstSearch(AbstractNode<T> node, int visitTimes, Map<AbstractNode<T>, Integer> d, Map<AbstractNode<T>, Integer> f);

	/**
	 * Returns whether the graph is a tree or not.
	 *
	 * @return whether the graph is a tree or not
	 */
	public abstract boolean isTree();

	/**
	 * Returns the list of nodes in this graph. For the sake of efficiency, this implementation returns the reference to
	 * the protected field. Make sure you understand this before using this method.
	 *
	 * @return the list of nodes in this graph.
	 */
	public List<AbstractNode<T>> getNodes() {
		return nodes;
	}

	/**
	 * Returns the node with the specified content.
	 *
	 * @param content content of the node.
	 * @return the node with the specified name in this graph; returns {@code null} if none uses this name.
	 */
	public AbstractNode<T> getNode(T content) {
		return this.contents.get(content);
	}

	/**
	 * Returns the list of edges in this graph. For the sake of efficiency, this implementation returns the reference to
	 * the protected field. Make sure you understand this before using this method.
	 *
	 * @return the list of edges in this graph.
	 */
	public final List<Edge<T>> getEdges() {
		return edges;
	}

	/**
	 * Returns the edge whose head and tail coincides.
	 *
	 * @param head the head node.
	 * @param tail the tail node.
	 * @return the matching edge or {@code null} if none matches.
	 */
	public final Optional<Edge<T>> getEdge(AbstractNode<T> head, AbstractNode<T> tail){
		return this.edges.stream()
				.filter(x-> x.getHead().equals(head) && x.getTail().equals(tail))
				.findFirst();
	}

	/**
	 * Removes the specified edge from this graph.
	 *
	 * @param head the head of the edge.
	 * @param tail the tail of the edge.
	 */
	public void removeEdge(AbstractNode<T> head, AbstractNode<T> tail) {
		Optional<Edge<T>> possibleEdge = this.getEdge(head, tail);
		if(!possibleEdge.isPresent())
			throw new IllegalArgumentException("Edge doesn't exist");
		else
			this.removeEdge(possibleEdge.get());
	}

	/**
	 * Returns a HashMap that relates each content object with its graph node.
	 *
	 * @return a HashMap that relates each content object with its graph node.
	 */
	public final HashMap<T, AbstractNode<T>> getContents(){
		return this.contents;
	}

	/**
	 * Returns the number of nodes in this graph.
	 *
	 * @return the number of nodes in this graph.
	 */
	public final int getNumberOfNodes() {
		return nodes.size();
	}

	/**
	 * Returns the number of edges in this graph.
	 *
	 * @return the number of edges in this graph.
	 */
	public final int getNumberOfEdges() {
		return edges.size();
	}

	/**
	 * Returns {@code true} if this graph contains the specified edge.
	 * 
	 * @param edge edge whose presence in this graph is to be tested.
	 * @return {@code true} if the specified edge is present.
	 */
	public final boolean containsEdge(Edge<T> edge) {
		return this.edges.stream()
				.anyMatch(x-> x.getHead().equals(edge.getHead()) && x.getTail().equals(edge.getTail()));
	}

	/**
	 * Returns{@code true} if this graph contains the specified node.
	 * 
	 * @param node node whose presence in this graph is to be tested.
	 * @return {@code true} if the specified node is present.
	 */
	public final boolean containsNode(AbstractNode<T> node) {
		return node.getGraph() == this;
	}

	/**
	 * Returns {@code true} if this graph contains a node with the specified name.
	 * 
	 * @param content node's content object, whose presence in this graph is to be tested.
	 * @return {@code true} if this graph contains a node with the specified name.
	 */
	public final boolean containsNode(T content) {
		return contents.containsKey(content);
	}

	/**
	 * Returns {@code true} if this graph contains the specified collection of nodes.
	 * 
	 * @param nodes collection of nodes whose presence in this graph are to be tested.
	 * @return {@code true} if this graph contains the specified collection of nodes.
	 */
	public final boolean containsNodes(Collection<? extends AbstractNode<T>> nodes) {
		return this.nodes.containsAll(nodes);
	}

	/**
	 * Returns {@code true} if there is a path (directed path for DAG) between the two specified nodes.
	 *
	 * @param start start of a path whose presence in this graph is to be tested.
	 * @param end end of a path whose presence in this graph is to be tested.
	 * @return {@code true} if a path is present.
	 * @see #depthFirstSearch(AbstractNode, int, Map, Map)
	 */
	public final boolean containsPath(AbstractNode<T> start, AbstractNode<T> end) {
		// this graph must contain both start and end
		if(!this.containsNode(start) || !this.containsNode(end))
			throw new IllegalArgumentException("The graph must contain both the start and the end nodes that are passed as arguments");

		// discovering and finishing time
		HashMap<AbstractNode<T>, Integer> d = new HashMap<>();
		HashMap<AbstractNode<T>, Integer> f = new HashMap<>();

		// DFS
		depthFirstSearch(start, 0, d, f);

		// returns true if the end has been discovered
		return d.containsKey(end);
	}

	/**
	 * Removes the specified node from this graph. <b>A node is removed from
	 * this graph means two things: First, this graph no longer contains this
	 * node, Second, this node has no place(graph) to live. Therefore, we need
	 * call AbstraceNode.dispose() to fulfill the second meaning.</b>
	 *
	 * @param node node to be removed from this graph.
	 */
	public void removeNode(AbstractNode<T> node) {
		// this graph must contain the argument node
		if(!this.containsNode(node))
			throw new IllegalArgumentException("The graph must contain the argument node");

		// removes incident edges.
		// Needed to make a copy, otherwise it modifies the
		// edges list during iteration and will throw exception
		Collection<Edge<T>> incidentEdges = new ArrayList<>(node.getEdges());
		for (Edge<T> edge : incidentEdges) {
			this.removeEdge(edge);
		}

		// removes node from the list of nodes in this graph
		nodes.remove(node);

		// removes name from the map for indexing
		contents.remove(node.getContent());

		// the node is useless, dispose it.
		node.dispose();
	}

	/**
	 * Returns {@code true} if the object is an {@code AbstractGraph} with equal fields.
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

		AbstractGraph graph = (AbstractGraph) object;
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
		result = 31 * result + edges.hashCode();
		result = 31 * result + contents.hashCode();
		return result;
	}

	/**
	 * Returns a string representation of this graph. This implementation returns <code>toString(0)</code>.
	 * 
	 * @return a string representation of this graph.
	 * @see #toString(int)
	 */
	@Override
	public final String toString() {
		return toString(0);
	}

	/**
	 * Returns a string representation of this graph. The string representation will be indented by the specified amount.
	 * 
	 * @param amount amount by which the string representation is to be indented.
	 * @return a string representation of this graph.
	 */
	public abstract String toString(int amount);
    
	/**
	 * Move the denoted node in nodes to the head of the LinkedList
	 * @param content the object that identifies the node being moved
	 * the String of the node you want to move
	 */
	public void move2First(T content){
	   AbstractNode n = contents.get(content);
	   nodes.remove(n);
	   nodes.addFirst(n);
	}
}