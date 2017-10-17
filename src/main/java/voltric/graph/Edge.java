/**
 * Edge.java
 * Copyright (C) 2006 Tao Chen, Kin Man Poon, Yi Wang, and Nevin L. Zhang
 */
package voltric.graph;

/**
 * This class provides an implementation for edges in graphs. This implementation is more suited for Directed edges.
 * However, it can work for undirected graphs too.
 * 
 * @author Yi Wang
 * @author ferjorosa
 */
public final class Edge<T> {

	/** The edge's head. */
	private AbstractNode<T> head;

	/** The edge's tail. */
	private AbstractNode<T> tail;

	/**
	 * Constructs an edge to connect the specified head and tail nodes.
	 * 
	 * <p>
	 * <b>Note: The constructor visibility is set to "package" because only <code>DirectedAcyclicGraph.addEdge(AbstractNode, AbstractNode)</code>
	 * and <code>UndirectedGraph.addEdge(AbstractNode, AbstractNode)</code> are supposed to call the constructor. </b>
	 * </p>
	 * 
	 * @param head head of this edge.
	 * @param tail tail of this edge.
	 * @see DirectedAcyclicGraph#addEdge(AbstractNode, AbstractNode)
	 * @see UndirectedGraph#addEdge(AbstractNode, AbstractNode)
	 */
	Edge(AbstractNode<T> head, AbstractNode<T> tail) {
		this.head = head;
		this.tail = tail;
	}

	/**
	 * Returns the head of this edge.
	 * 
	 * @return the head of this edge.
	 */
	public final AbstractNode<T> getHead() {
		return head;
	}

	/**
	 * Returns the opposite to the specified end.
	 * 
	 * @param end end to which the opposite is at request.
	 * @return the opposite to the specified end.
	 */
	public final AbstractNode<T> getOpposite(AbstractNode<T> end) {
		// The argument node must belong to this edge
		if(!end.equals(head) && !end.equals(tail))
			throw new IllegalArgumentException("The argument node must belong to this edge");

		return end == head ? tail : head;
	}

	/**
	 * Returns the tail of this edge.
	 * 
	 * @return the tail of this edge.
	 */
	public final AbstractNode<T> getTail() {
		return tail;
	}

	/**
	 * Returns {@code true} if the object is an {@code Edge} with equal fields.
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

		Edge edge = (Edge) object;
		return this.head.equals(edge.head)
				&& this.tail.equals(edge.tail);
	}

	/**
	 * Returns the object's hashcode.
	 *
	 * @return the object's hashcode.
	 */
	@Override
	public int hashCode() {
		int result = head.hashCode();
		result = 31 * result + tail.hashCode();
		return result;
	}

	/**
	 * Returns a string representation of this edge. This implementation returns <code>toString(0)</code>.
	 * 
	 * @return a string representation of this edge.
	 * @see #toString(int)
	 */
	@Override
	public final String toString() {
		return toString(0);
	}

	/**
	 * Returns a string representation of this edge. The string representation will be indented by the specified amount.
	 * 
	 * @param amount amount by which the string representation is to be indented.
	 * @return a string representation of this edge.
	 */
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
		stringBuffer.append("edge {\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("\thead = \"" + head.getContent().toString() + "\";\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("\ttail = \"" + tail.getContent().toString() + "\";\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("};\n");

		return stringBuffer.toString();
	}

}