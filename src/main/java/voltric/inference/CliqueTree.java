/**
 * CliqueTree.java 
 * Copyright (C) 2006 Tao Chen, Kin Man Poon, Yi Wang, and Nevin L. Zhang
 */
package voltric.inference;


import voltric.graph.AbstractNode;
import voltric.graph.DirectedNode;
import voltric.graph.Edge;
import voltric.graph.UndirectedGraph;
import voltric.learning.parameter.em.util.MessagesForLocalEM;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.variables.DiscreteVariable;
import voltric.variables.Variable;

import java.util.*;

/**
 * This class provides an implementation for clique trees (CTs).
 * 
 * @author Yi Wang
 * 
 */
public class CliqueTree extends UndirectedGraph<String> {

	/**
	 * the pivot of this CT. likelihood will be computed at the pivot.
	 */
	private CliqueNode _pivot;

	/**
	 * the collection of cliques in the subtree in which full propagation will
	 * be carried out. All the nodes must be continuous.
	 */
	protected Set<CliqueNode> _focusedSubtree;

	/**
	 * Manually set the _focusedSubtree
	 * 
	 * @param focusedSubtree
	 */
	public void setFocusedSubtree(Set<CliqueNode> focusedSubtree) {
		_focusedSubtree = focusedSubtree;
	}

	/**
	 * The map from belief nodes to family covering cliques. belief for each
	 * node and its family will be computed at its family covering clique.
	 */
	protected Map<DiscreteVariable, CliqueNode> _familyCliques;

	/**
	 * <p>
	 * Constructs an empty CT. We have NOT construct the _familyCliques and
	 * _focusedSubtree fields.
	 * </p>
	 * 
	 * <p>
	 * <b>Note: Only <code>clone()</code> is supposed to call this method. </b>
	 * </p>
	 */
	private CliqueTree() {
		super();
	}

	/**
	 * Constructs a CT for the specified BN. TODO I have Checked this method.
	 * However, for current work, this method is not used since we are dealing
	 * with the HLCM for now. We may delete this method or revise it in the
	 * present of new demand.
	 * 
	 * @param DiscreteBayesNet
	 *            BN to be associated with this CT.
	 */
	public CliqueTree(DiscreteBayesNet DiscreteBayesNet) {

		// I know this can be implicit. However, I am inclined to add it.
		super();

		// computes minimum deficiency order
		UndirectedGraph<Variable> moralGraph = DiscreteBayesNet.computeMoralGraph();
		LinkedList<Variable> order = moralGraph.minimumDeficiencySearch();

		// builds this CT
//		buildCliqueTree(moralGraph, order.iterator(), DiscreteBayesNet);
		buildCliqueTreeNotRecursively(moralGraph, order.iterator(), DiscreteBayesNet);

		// finds family covering cliques for belief nodes
		_familyCliques = new HashMap<DiscreteVariable, CliqueNode>();

		for (DiscreteVariable var : DiscreteBayesNet.getVariables()) {
			// finds smallest clique that covers this family
			int minCard = Integer.MAX_VALUE;
			CliqueNode familyClique = null;

			ArrayList<DiscreteVariable> vParents = new ArrayList<DiscreteVariable>();
			for (DirectedNode node : DiscreteBayesNet.getNode(var).getParents()) {
				vParents.add(((DiscreteBeliefNode) node).getVariable());
			}

			for (AbstractNode clique : this.nodes) {
				CliqueNode cliqueNode = (CliqueNode) clique;
				int card = cliqueNode.getCardinality();

				if (card < minCard && cliqueNode.contains(var)
						&& cliqueNode.containsAll(vParents)) {
					minCard = card;
					familyClique = cliqueNode;
				}
			}

			_familyCliques.put(var, familyClique);
		}

		// sets smallest clique as pivot for fast likelihood computation
		int minCard = Integer.MAX_VALUE;
		for (AbstractNode node : this.nodes) {
			CliqueNode clique = (CliqueNode) node;
			int card = clique.getCardinality();

			if (card < minCard) {
				minCard = card;
				_pivot = clique;
			}
		}
	}

	/**
	 * Adds a clique that covers the specified collection of Variables to this
	 * clique tree.
	 * 
	 * @param vars
	 *            Collection of Variables to be attached to the clique.
	 * @return The clique that was added to this CT.
	 */
	private CliqueNode addNode(LinkedHashSet<DiscreteVariable> vars, String name) {
		// creates clique
		CliqueNode node = null;
		if (name == null)
			node = new CliqueNode(this, vars);
		else
			node = new CliqueNode(this, vars, name);
		// adds clique to cliques in CTs
		this.nodes.add(node);
		// maps name to clique
		this.contents.put(node.getContent(), node);
		return node;
	}

	/**
	 * Recursively builds this CT.
	 * 
	 * @param moralGraph
	 *            Moral graph of the remainder of the associated BN.
	 * @param nameIter
	 *            Name of the next node to be eliminated from the moral graph.
	 */
	private void buildCliqueTree(UndirectedGraph<String> moralGraph,
			Iterator<String> nameIter, DiscreteBayesNet DiscreteBayesNet) {
		// node to be eliminated
		AbstractNode<String> elimNode = moralGraph.getNode(nameIter.next());

		// belief nodes to be attached to new clique
		LinkedHashSet<DiscreteVariable> vars = new LinkedHashSet<>();

		// node to be eliminated will be attached to new clique
		DiscreteBeliefNode bNode =  DiscreteBayesNet.getNode(elimNode.getContent());
		vars.add(bNode.getVariable());

		// separator (neighbors of eliminated node) will be attached to clique
		LinkedList<DiscreteVariable> separator = new LinkedList<DiscreteVariable>();
		for (AbstractNode<String> neighbor : elimNode.getNeighbors()) {
			DiscreteBeliefNode bNeighbor = DiscreteBayesNet.getNode(neighbor.getContent());

			separator.add(bNeighbor.getVariable());
			vars.add(bNeighbor.getVariable());
		}

		if (vars.size() == moralGraph.getNumberOfNodes()) {
			// ends recursion and adds clique to this CT
			addNode(vars, null);
		} else {
			// eliminates node
			moralGraph.eliminateNode(elimNode);

			// recursively builds rest of this CT
			buildCliqueTree(moralGraph, nameIter, DiscreteBayesNet);

			// finds another clique in rest of CT that contains separator
			CliqueNode clique2 = null;
			for (AbstractNode node : this.nodes) {
				clique2 = (CliqueNode) node;

				if (clique2.containsAll(separator)) {
					break;
				}
			}

			// adds clique to clique tree
			CliqueNode clique = addNode(vars, null);

			if (clique.containsAll(clique2.getVariables())) {
				// subsumes non-maximal clique
				for (AbstractNode neighbor : clique2.getNeighbors()) {
					addEdge(clique, neighbor);
				}

				removeNode(clique2);
			} else {
				// connects two cliques
				addEdge(clique, clique2);
			}
		}
	}
	
	/**
	 * builds this CT.
	 * 
	 * The recursive method will cause "stack over flow" when the tree is large.
	 * 
	 * @param moralGraph
	 *            Moral graph of the remainder of the associated BN.
	 * @param variableIterator Variable of the next node to be eliminated from the moral graph.
	 */
	private void buildCliqueTreeNotRecursively(UndirectedGraph<Variable> moralGraph, Iterator<Variable> variableIterator, DiscreteBayesNet DiscreteBayesNet)
	{
		
		ArrayList<CliqueNode> cliqueList = new ArrayList<CliqueNode>();
		ArrayList<LinkedList<DiscreteVariable>> seperatorList = new ArrayList<LinkedList<DiscreteVariable>>();
		
		while(variableIterator.hasNext())
		{
			// node to be eliminated
			AbstractNode<Variable> elimNode = moralGraph.getNode(variableIterator.next());
			
			// belief nodes to be attached to new clique
			LinkedHashSet<DiscreteVariable> vars = new LinkedHashSet<DiscreteVariable>();

			// node to be eliminated will be attached to new clique
			DiscreteBeliefNode bNode = DiscreteBayesNet.getNode(elimNode.getContent());
			vars.add(bNode.getVariable());
			
			// separator (neighbors of eliminated node) will be attached to clique
			LinkedList<DiscreteVariable> separator = new LinkedList<DiscreteVariable>();
			for (AbstractNode<Variable> neighbor : elimNode.getNeighbors()) {
				DiscreteBeliefNode bNeighbor = DiscreteBayesNet.getNode(neighbor.getContent());

				separator.add(bNeighbor.getVariable());
				vars.add(bNeighbor.getVariable());
			}
			
			// adds clique to clique tree
			CliqueNode clique = addNode(vars, null);
			
			cliqueList.add(clique);
			seperatorList.add(separator);
			
			if (vars.size() == moralGraph.getNumberOfNodes()) {
				// ends recursion and adds clique to this CT			
				break;
			} 
			
			// eliminates node
			moralGraph.eliminateNode(elimNode);
		}
		
		int size = cliqueList.size();
		
		for(int i=size-2; i>-1; i--)
		{

			CliqueNode clique = cliqueList.get(i);
			LinkedList<DiscreteVariable> separator = seperatorList.get(i);
			
			// finds another clique in rest of CT that contains separator
			CliqueNode clique2 = null;
			for(int j=i+1; j<size; j++)
			{
				clique2 = cliqueList.get(j);
				
				if (clique2.containsAll(separator)) {
					break;
				}	
			}

			if (clique.containsAll(clique2.getVariables())) {
				// subsumes non-maximal clique
				for (AbstractNode<String> neighbor : clique2.getNeighbors()) {
					addEdge(clique, neighbor);
				}

				removeNode(clique2);
			} else {
				// connects two cliques
				addEdge(clique, clique2);
			}
		}
	} 

	/**
	 * Creates and returns a deep copy of this CT. Variables are not deep copied
	 * but the reference are used.
	 * 
	 * @return A deep copy of this CT.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public CliqueTree clone() {
		CliqueTree copy = new CliqueTree();

		// copies nodes
		for (AbstractNode<String> node : this.nodes) {
			copy.addNode((LinkedHashSet<DiscreteVariable>) ((CliqueNode) node)
					.getVariables().clone(), node.getContent());
		}

		// copies edges
		for (Edge<String> edge : this.edges) {
			copy.addEdge(copy.getNode(edge.getHead().getContent()), copy
					.getNode(edge.getTail().getContent()));
		}

		copy._familyCliques = new HashMap<DiscreteVariable, CliqueNode>();
		for (DiscreteVariable var : _familyCliques.keySet()) {
			copy._familyCliques.put(var, (CliqueNode) copy
					.getNode(getFamilyClique(var).getContent()));
		}

		if (_focusedSubtree != null) {
			copy._focusedSubtree = new LinkedHashSet<CliqueNode>();
			for (CliqueNode clique : _focusedSubtree) {
				copy._focusedSubtree.add((CliqueNode) copy.getNode(clique.getContent()));
			}
		}

		copy._pivot = (CliqueNode) copy.getNode(_pivot.getContent());

		return copy;
	}

	/**
	 * Returns the set of cliques in the minimal subtree that covers the
	 * specified collection of Variables.
	 * 
	 * @param vars
	 *            Collection of Variables whose minimal covering subtree is to
	 *            be computed.
	 * @return The set of cliques in the minimal subtree that covers the
	 *         specified collection of Variables.
	 */
	Set<CliqueNode> computeMinimalSubtree(Collection<DiscreteVariable> vars) {
		// we deploy the following strategy: keep "removing" redundant leaves
		// until a minimal subtree that covers the specified collection of
		// belief node is left.
		HashMap<CliqueNode, Integer> degrees = new HashMap<CliqueNode, Integer>();
		LinkedList<CliqueNode> leaves = new LinkedList<CliqueNode>();

		// initializes degrees and leaves
		for (AbstractNode<String> node : this.nodes) {
			CliqueNode clique = (CliqueNode) node;
			int degree = clique.getDegree();

			degrees.put(clique, degree);
			if (degree == 1) {
				leaves.add(clique);
			}
		}

		// keep removing redundant leaves until list is empty
		while (!leaves.isEmpty()) {
			// pops head
			CliqueNode leaf = leaves.removeFirst();

			// finds the only alive neighbor
			CliqueNode aliveNeighbor = null;
			for (AbstractNode<String> neighbor : leaf.getNeighbors()) {
				if (degrees.containsKey(neighbor)) {
					aliveNeighbor = (CliqueNode) neighbor;
					break;
				}
			}

			// System.out.println(" " + (aliveNeighbor==null) );
			Set<?> difference = leaf.getDifferenceTo(aliveNeighbor);
			boolean isRedudant = true;
			for (DiscreteVariable var : vars) {
				if (difference.contains(var)) {
					isRedudant = false;
					break;
				}
			}

			if (isRedudant) {
				// removes leaf from degrees
				degrees.remove(leaf);

				// updates degree for alive neighbor
				int degree = degrees.get(aliveNeighbor) - 1;
				degrees.put(aliveNeighbor, degree);

				// update leaves
				if (degree == 0) {
					// only one clique left, terminate
					break;
				} else if (degree == 1) {
					leaves.add(aliveNeighbor);
				}
			}
		}

		return degrees.keySet();
	}

	/**
	 * Returns the family covering clique for the specified variable.
	 * 
	 * @param var
	 *            variable whose family covering clique is at request.
	 * @return The family covering clique for the specified variable.
	 */
	public CliqueNode getFamilyClique(DiscreteVariable var) {
		return _familyCliques.get(var);
	}

	/**
	 * Returns the pivot of this CT.
	 * 
	 * @return The pivot of this CT.
	 */
	public CliqueNode getPivot() {
		return _pivot;
	}

	/**
	 * Returns <code>true</code> if the specified clique is in the focused
	 * subtree.
	 * 
	 * @param clique
	 * @return
	 */
	public boolean inFocusedSubtree(CliqueNode clique) {
		return _focusedSubtree == null || _focusedSubtree.contains(clique);
	}
	
	public void copyInMsgsFrom(Set<MessagesForLocalEM> msgs) {
		
		
		for (MessagesForLocalEM message : msgs)
		{
			for (CliqueNode head : _focusedSubtree)
			{
				if(head.getContent().equals(message.getHead()))
				{
					for (AbstractNode node : head.getNeighbors())
					{
						if(node.getContent().equals(message.getTail()))
						{
							((CliqueNode) node).setMessageTo(head, message.getFunction());

							if (message.getNormalization() != null)
								((CliqueNode) node).setNormalizationTo((CliqueNode)head, message.getNormalization().doubleValue());
						}
					}
				}else
				{
					continue;
				}
			}
		}
	}

	/**
	 * Find the familiy cliquenode of the argument variable. Copy(Refer) the
	 * attached functions.
	 * 
	 * @param cliqueTree
	 * @param var
	 */
	public void copyFuncsFrom(CliqueTree cliqueTree, DiscreteVariable var) {
		CliqueNode cNode = cliqueTree.getFamilyClique(var);
		_familyCliques.get(var).setFunctions(cNode.getFunctions());
	}
}


