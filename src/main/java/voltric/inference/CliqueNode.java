/**
 * CliqueNode.java 
 * Copyright (C) 2006 Tao Chen, Kin Man Poon, Yi Wang, and Nevin L. Zhang
 */
package voltric.inference;


import voltric.graph.*;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.*;

/**
 * This class provides an implementation for cliques in CTs.
 * 
 * @author Yi Wang
 * 
 */
public class CliqueNode extends UndirectedNode<String> {

	/**
	 * the prefix of names of cliques.
	 */
	private final static String CLIQUE_PREFIX = "Clique";

	/**
	 * Then during propogation, every time the msaage is sent to this node, the
	 * _msgProd will be updated by product of the massage and itself(if the
	 * meaage contains no zero cell).
	 * 
	 * Note that there is no need to calculate the _alphaProd
	 */
	protected Function _msgsProd;

	public void setMsgsProd(Function function) {
		_msgsProd = function;
	}

	/**
	 * Modify the filed _msgsProd by timing function. After judging whether the
	 * current _msgsProd is superior to the argument function, the method
	 * multiply or times will be called.
	 * 
	 * @param function
	 */
	public void modifyMsgProd(Function function) {
		if (_msgsProd.superiorTo(function)) {
			_msgsProd.multiply(function);
		} else {
			_msgsProd = _msgsProd.times(function);
		}
	}

	protected Set<CliqueNode> _qualifiedNeiMsgs;

	public void clearQualifiedNeiMsgs() {
		_qualifiedNeiMsgs.clear();
	}

	/**
	 * the collection of Variables attached to this clique. We specify the set
	 * to be a LinkedHashSet since we want to use the orders of the variables.
	 */
	private LinkedHashSet<DiscreteVariable> _variables;

	/**
	 * the colletion of functions attached to this clique.
	 */
	private List<Function> _funcs;

	/**
	 * the messages sent to neighbors.
	 */
	private Map<AbstractNode, Function> _msgs;

	/**
	 * the normalizing constants sent to neighbors.
	 */
	private Map<AbstractNode, Double> _alphas;

	private Map<AbstractNode, Double> logAlphas;

	/**
	 * the difference in belief nodes to neighbors.
	 */
	private Map<AbstractNode, Set<DiscreteVariable>> _diffs;

	/**
	 * the cardinality of this clique, namely, the product of cardinalities of
	 * Variables attached to this clique.
	 */
	private int _cardinality;

	/**
	 * Creates a clique with the specified graph to contain it and the specified
	 * collection of belief nodes attached.
	 * 
	 * @param graph
	 *            graph to contain this clique.
	 * @param variables colletion of Variables to be attached to this clique.
	 */
	CliqueNode(AbstractGraph<String> graph, LinkedHashSet<DiscreteVariable> variables) {
		super(graph, CLIQUE_PREFIX + graph.getNumberOfNodes());

		_variables = variables;
		_funcs = new LinkedList<Function>();
		_msgs = new HashMap<AbstractNode, Function>();
		_alphas = new HashMap<AbstractNode, Double>();
		logAlphas = new HashMap<AbstractNode, Double>();
		_diffs = new HashMap<AbstractNode, Set<DiscreteVariable>>();

		_msgsProd = Function.createIdentityFunction();
		_qualifiedNeiMsgs = new LinkedHashSet<CliqueNode>();

		// computes cardinality
		_cardinality = 1;
		for (DiscreteVariable var : _variables) {
			_cardinality *= var.getCardinality();
		}
	}

	/**
	 * Creates a clique with the specified graph to contain it and the specified
	 * collection of belief nodes attached.
	 * 
	 * @param graph
	 *            graph to contain this clique.
	 * @param variables colletion of Variables to be attached to this clique.
	 * @param name pecify the name of this node.
	 */
	CliqueNode(AbstractGraph<String> graph, LinkedHashSet<DiscreteVariable> variables, String name) {
		super(graph, name);

		_variables = variables;
		_funcs = new LinkedList<Function>();
		_msgs = new HashMap<AbstractNode, Function>();
		_alphas = new HashMap<AbstractNode, Double>();
		logAlphas = new HashMap<AbstractNode, Double>();
		_diffs = new HashMap<AbstractNode, Set<DiscreteVariable>>();

		_msgsProd = Function.createIdentityFunction();
		_qualifiedNeiMsgs = new LinkedHashSet<CliqueNode>();

		// computes cardinality
		_cardinality = 1;
		for (DiscreteVariable var : _variables) {
			_cardinality *= var.getCardinality();
		}
	}

	/**
	 * <p>
	 * Attachs the specified edge to this node. This implementation extends
	 * <code>AbstractNode.attachEdge(Edge edge)</code> such that the difference
	 * in attached belief nodes between this clique and the oppsite will be
	 * updated as well.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only
	 * <code>UndirectedGraph.addEdge(AbstractNode, AbstractNode)</code> is
	 * supposed to call this method. </b>
	 * </p>
	 * 
	 * @param edge
	 *            edge to be attached to this node.
	 * @see UndirectedGraph#addEdge(AbstractNode, AbstractNode)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected final void attachEdge(Edge<String> edge) {
		super.attachEdge(edge);

		// retrieves belief nodes different to those attached to opposite
		CliqueNode neighbor = (CliqueNode) edge.getOpposite(this);

		Set<DiscreteVariable> difference = (Set<DiscreteVariable>) _variables.clone();
		difference.removeAll(neighbor._variables);

		_diffs.put(neighbor, difference);
	}

	/**
	 * <p>
	 * Detachs the specified edge to this node. This implementation extends
	 * <code>AbstractNode.detachEdge(Edge edge)</code> such that the key of the
	 * neighbor CliqueNode is removed from the difference mapping.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only
	 * <code>UndirectedGraph.addEdge(AbstractNode, AbstractNode)</code> is
	 * supposed to call this method. </b>
	 * </p>
	 * 
	 * @param edge
	 *            edge to be detached to this node.
	 * @see UndirectedGraph#removeEdge(AbstractNode, AbstractNode)
	 */
	@Override
	protected final void detachEdge(Edge<String> edge) {
		super.detachEdge(edge);

		// retrieves belief nodes different to those attached to opposite
		CliqueNode neighbor = (CliqueNode) edge.getOpposite(this);
		_diffs.remove(neighbor);
	}

	/**
	 * <p>
	 * Attaches the specified function to this clique.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only <code>CliqueTreePropagation.absorbEvidence()</code> is
	 * supposed to call this method. </b>
	 * </p>
	 * 
	 * @param function
	 *            function to be attached to this clique.
	 * @see CliqueTreePropagation#absorbEvidence
	 */
	void attachFunction(Function function) {
		_funcs.add(function);
	}

	/**
	 * <p>
	 * Removes all functions attached to this clique.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only <code>CliqueTreePropagation.absorbEvidence()</code> is
	 * supposed to call this method. </b>
	 * </p>
	 * 
	 * @see CliqueTreePropagation#absorbEvidence
	 */
	public void clearFunctions() {
		_funcs.clear();
	}

	/**
	 * Returns <code>true</code> if the specified Variable is attached to this
	 * clique.
	 * 
	 * @param var
	 *            Variable whose presence in this clique is to be tested.
	 * @return <code>true</code> if the specified Variable is attached to this
	 *         clique.
	 */
	public boolean contains(DiscreteVariable var) {
		return _variables.contains(var);
	}

	/**
	 * Returns <code>true</code> if the specified collection of Variables are
	 * attached to this clique.
	 * 
	 * @param vars
	 *            collection of Variables whose presence in this clique is to be
	 *            tested.
	 * @return <code>true</code> if the specified collection of Variables are
	 *         attached to this clique.
	 */
	public boolean containsAll(Collection<DiscreteVariable> vars) {
		return _variables.containsAll(vars);
	}

	/**
	 * Returns the cardinality of this clique.
	 * 
	 * @return the cardinality of this clique.
	 */
	public int getCardinality() {
		return _cardinality;
	}

	/**
	 * <p>
	 * Returns the set of Variables attached to this clique but not to the
	 * specified neighbor.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only
	 * <code>CliqueTree.computeMinimalSubtree(java.util.ArrayList)</code> and
	 * <code>CliqueTreePropagation.sendMessage(CliqueNode, CliqueNode)</code>
	 * are supposed to call this method. </b>
	 * </p>
	 * 
	 * @param neighbor
	 *            neighbor to be compared with this clique.
	 * @return the collection of Variables attached to this clique but not to
	 *         the specified neighbor.
	 */
	Set<DiscreteVariable> getDifferenceTo(CliqueNode neighbor) {
		return _diffs.get(neighbor);
	}

	/**
	 * <p>
	 * Returns the collection of functions attached to this clique.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only methods of <code>CliqueTreePropagation</code> are supposed
	 * to call this method. </b>
	 * </p>
	 * 
	 * @return the collection of functions attached to this clique.
	 */
	List<Function> getFunctions() {
		return _funcs;
	}

	/**
	 * Set the attached functions.
	 * 
	 * @param funcs
	 */
	void setFunctions(List<Function> funcs) {
		_funcs = funcs;
	}

	/**
	 * <p>
	 * Returns the message this clique sends to the specified neighbor.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only methods of <code>CliqueTreePropagation</code> are supposed
	 * to call this method. </b>
	 * </p>
	 * 
	 * @param neighbor
	 *            destination of the message.
	 * @return the message this clique sends to the specified neighbor.
	 */
	public Function getMessageTo(CliqueNode neighbor) {
		return _msgs.get(neighbor);
	}

	/**
	 * Returns the collection of Variables attached to this clique.
	 * 
	 * @return the collection of Variables attached to this clique.
	 */
	public LinkedHashSet<DiscreteVariable> getVariables() {
		return _variables;
	}

	/**
	 * <p>
	 * Returns the normalizing constant this clique sends to the specified
	 * neighbor.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only methods of <code>CliqueTreePropagation</code> are supposed
	 * to call this method. </b>
	 * </p>
	 * 
	 * @param neighbor
	 *            destination of the normalizing constant.
	 * @return the normalizing constant this clique sends to the specified
	 *         neighbor.
	 */
	public Double getNormalizationTo(CliqueNode neighbor) {
		return _alphas.get(neighbor);
	}

	public Double getLogNormalizationTo(CliqueNode neighbor) {
		return logAlphas.get(neighbor);
	}

	/**
	 * <p>
	 * Replaces the message this clique sends to the specified neighbor.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only methods of <code>CliqueTreePropagation</code> are supposed
	 * to call this method. </b>
	 * </p>
	 * 
	 * @param neighbor
	 *            destination of the message.
	 * @param message
	 *            message this clique sends to the specified neighbor.
	 */
	public void setMessageTo(CliqueNode neighbor, Function message) {
		_msgs.put(neighbor, message);
	}

	/**
	 * <p>
	 * Replaces the normalizing constant this clique sends to the specified
	 * neighbor.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only methods of <code>CliqueTreePropagation</code> are supposed
	 * to call this method. </b>
	 * </p>
	 * 
	 * @param neighbor
	 *            destination of the normalizing constant.
	 * @param normalization
	 *            normalizing constant this clique sends to the specified
	 *            neighbor.
	 */
	public void setNormalizationTo(CliqueNode neighbor, double normalization) {
		_alphas.put(neighbor, normalization);
	}

	public void setLogNormalizationTo(CliqueNode neighbor, double normalization) {
		logAlphas.put(neighbor, normalization);
	}

	/**
	 * Returns a string representation of this clique. The string representation
	 * will be indented by the specified amount.
	 * 
	 * @param amount
	 *            amount by which the string representation is to be indented.
	 * @return a string representation of this clique.
	 */
	@Override
	public String toString(int amount) {
		// amount must be non-negative
		if(amount <= 0)
			throw new IllegalArgumentException("amount must be positive");

		// prepares white space for indent
		StringBuffer whiteSpace = new StringBuffer();
		for (int i = 0; i < amount; i++) {
			whiteSpace.append("\t");
		}

		// builds string representation
		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(whiteSpace);
		stringBuffer.append("undirected node {\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("\tname = \"" + this.getContent() + "\";\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("\tdegree = " + getDegree() + ";\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("\tneighbors = { ");

		for (AbstractNode neighbor : getNeighbors()) {
			stringBuffer.append("\"" + neighbor.getContent() + "\" ");
		}

		stringBuffer.append("};\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("\tbelief nodes = { ");

		for (DiscreteVariable var : getVariables()) {
			stringBuffer.append("\"" + var.getName() + "\" ");
		}

		stringBuffer.append("};\n");

		stringBuffer.append(whiteSpace);
		stringBuffer.append("};\n");

		return stringBuffer.toString();
	}

}
