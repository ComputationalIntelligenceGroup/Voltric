/**
 * CliqueTreePropagation.java 
 * Copyright (C) 2006 Tao Chen, Kin Man Poon, Yi Wang, and Nevin L. Zhang
 */
package voltric.inference;

import voltric.data.DiscreteData;
import voltric.graph.AbstractNode;
import voltric.graph.DirectedNode;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.*;

/**
 * This class provides an implementation for clique tree propagation (CTP)
 * algorithm.
 * 
 * @author Yi Wang
 * 
 *         If you are working with non-HLCM bayesnet, set the model as class
 *         "DiscreteBayesNet"; otherwise, you can set it as class "HLCM".
 * 
 * @author LIU Tengfei
 * 
 * 
 */
public final class CliqueTreePropagation implements Cloneable {

	/**
	 * The BN under query.
	 */
	private DiscreteBayesNet bayesNet;

	/**
	 * The CT used by this CTP.
	 */
	private CliqueTree cliqueTree;

	/**
	 * 
	 */
	private Map<DiscreteVariable, Integer> _evidence = new HashMap<DiscreteVariable, Integer>();

	private double lastLogLikelihood = Double.NaN;

	/**
	 * Dummy constructor. It is supposed that only
	 * <code>CliqueTreePropagation.clone()</code> will invoke it.
	 */
	private CliqueTreePropagation() {
	}

	/**
	 * Constructs a CTP for the specified BN.
	 * 
	 * @param bayesNet
	 *            BN under query.
	 */
	public CliqueTreePropagation(DiscreteBayesNet bayesNet) {
		this.bayesNet = bayesNet;
		cliqueTree = new CliqueTree(this.bayesNet);
		_evidence = new HashMap<DiscreteVariable, Integer>();
	}

	/**
	 * Clears the evidence entered into this inference engine.
	 */
	public void clearEvidence() {
		_evidence.clear();
	}

	/**
	 * Creates and returns a deep copy of this CTP.
	 * 
	 * @return A deep copy of this CTP.
	 */
	public CliqueTreePropagation clone() {
		CliqueTreePropagation copy = new CliqueTreePropagation();
		copy.bayesNet = bayesNet;
		copy.cliqueTree = cliqueTree.clone();
		// abandon eveidence
		return copy;
	}

	/**
	 * Prepares functions attached to cliques by <b>copying</b> CPTs and
	 * absorbing evidences.
	 * <p>
	 * Note: When an attached function is the same as one cpt rather than a
	 * funciont through projction of a cpt, we use the reference directly.
	 * Therefore be careful of updating the cpts of a DiscreteBayesNet.
	 * </p>
	 */
	public void absorbEvidence() {

		for (AbstractNode<String> node : cliqueTree.getNodes()) {
			CliqueNode cNode = (CliqueNode) node;
			cNode.clearFunctions();
			cNode.clearQualifiedNeiMsgs();
			cNode.setMsgsProd(Function.createIdentityFunction());
		}

		LinkedHashMap<DiscreteVariable, Function> functions =
				new LinkedHashMap<DiscreteVariable, Function>();

		for (DiscreteBeliefNode node : bayesNet.getNodes()) {
			DiscreteVariable var = node.getVariable();

			CliqueNode familiyClique = cliqueTree.getFamilyClique(var);
			if (familiyClique != null
					&& cliqueTree.inFocusedSubtree(familiyClique))
				// initializes function as CPT
				functions.put(var, node.getCpt());
		}

		Set<DiscreteVariable> mutableVars = functions.keySet();

		for (DiscreteVariable var : _evidence.keySet()) {
			int value = _evidence.get(var);

			DiscreteBeliefNode bNode = bayesNet.getNode(var.getName());

			if (mutableVars.contains(var)) {
				functions.put(var, functions.get(var).project(var, value));

			}
			for (DirectedNode child : bNode.getChildren()) {
				DiscreteBeliefNode bChild = (DiscreteBeliefNode) child;
				DiscreteVariable varChild = bChild.getVariable();
				if (mutableVars.contains(varChild))
					functions.put(varChild,
							functions.get(varChild).project(var, value));
			}
		}

		for (DiscreteVariable var : mutableVars) {
			// attaches function to family covering clique
			cliqueTree.getFamilyClique(var).attachFunction(functions.get(var));
		}
	}

	/**
	 * @param source
	 * @param destination
	 * @param subtree
	 * @param standingVars
	 * @return
	 */
	private Function collectMessage(CliqueNode source, CliqueNode destination,
                                    Set<CliqueNode> subtree, Collection<DiscreteVariable> standingVars) {
		Function msg = Function.createIdentityFunction();

		// collects messages from neighbors of source except destination
		for (AbstractNode<String> neighbor : source.getNeighbors()) {
			CliqueNode clique = (CliqueNode) neighbor;

			if (clique != destination) {
				if (subtree.contains(clique)) {
					msg = msg.times(collectMessage(clique, source, subtree, standingVars));
				} else {
					msg = msg.times(clique.getMessageTo(source));
				}
			}
		}

		// times up all functions in source
		for (Function func : source.getFunctions()) {
			msg = msg.times(func);
		}

		// sums out difference between source and destination but retain
		// standing nodes
		for (DiscreteVariable var : source.getDifferenceTo(destination)) {
			if (!_evidence.containsKey(var) && !standingVars.contains(var)) {
				msg = msg.sumOut(var);
			}
		}

		return msg;
	}

	/**
	 * Returns the posterior probability distribution of the specified variable
	 * 
	 * @param var
	 *            Variable under query.
	 * @return The posterior probability distribution of the specified variable.
	 */
	public Function computeBelief(DiscreteVariable var) {
		// associated BN must contain var
		DiscreteBeliefNode node = bayesNet.getNode(var);

		if (node == null)
			throw new IllegalArgumentException("The model does not contain a Belief node associated to the variable under query");

		Function belief = null;

		if (_evidence.containsKey(var)) {
			//likelihood must be positive
			// TODO: test with exception
			assert computeLikelihood() > 0.0;

			belief = Function.createIndicatorFunction(var, _evidence.get(var));
		} else {
			// initializationMethod
			belief = Function.createIdentityFunction();

			// computes potential at answer extraction clique
			CliqueNode answerClique = cliqueTree.getFamilyClique(var);

			// times up functions attached to answer extraction clique
			for (Function function : answerClique.getFunctions()) {
				belief = belief.times(function);
			}

			// times up messages to answer extraction clique
			for (AbstractNode<String> neighbor : answerClique.getNeighbors()) {
				belief = belief.times(((CliqueNode) neighbor).getMessageTo(answerClique));
			}

			// marginalizes potential
			belief = belief.marginalize(var);

			// normalizes potential
			belief.normalize();
		}

		return belief;
	}

	/**
	 * Returns the posterior probability distribution of the specified
	 * collection of variable. The difference bwteen this method and
	 * <code> computeBelief(Collection<Variable> var)</code> is that here the
	 * Clique Subtree used for computing suffucient statistics is specified.
	 * 
	 * @param vars
	 *            Collection of variables under query.
	 * @param subtree
	 *            The clique subTree used for inference.
	 * @return The posterior probability distribution of the specified
	 *         collection of variables.
	 */
	public Function computeBelief(Collection<DiscreteVariable> vars,
                                  Set<CliqueNode> subtree) {
		if(vars.isEmpty())
		    throw new IllegalArgumentException("The collection of variables under query in empty");

        if(!bayesNet.containsVars(vars))
            throw new IllegalArgumentException("Some of the variables under query are not present in the model");

		// collects hidden and observed variables in query nodes
		LinkedList<DiscreteVariable> hdnVars = new LinkedList<DiscreteVariable>();
		ArrayList<DiscreteVariable> obsVars = new ArrayList<DiscreteVariable>();
		ArrayList<Integer> obsVals = new ArrayList<Integer>();

		for (DiscreteVariable var : vars) {
			if (_evidence.containsKey(var)) {
				obsVars.add(var);
				obsVals.add(_evidence.get(var));
			} else {
				hdnVars.add(var);
			}
		}

		// belief over observed variables
		Function obsBel = Function.createIndicatorFunction(obsVars, obsVals);

		if (hdnVars.isEmpty()) {
			return obsBel;
		}

		// belief over hidden variables
		Function hdnBel = Function.createIdentityFunction();

		// constructs the minimal subtree that covers all query nodes
		// Set<CliqueNode> subtree = cliqueTree.computeMinimalSubtree(nodes);
		// Set<CliqueNode> subtree =
		// cliqueTree.computeMinimalSubtree(hdnNodes);

		// uses first clique in the subtree as the pivot
		CliqueNode pivot = subtree.iterator().next();

		// computes the local potential at the pivot clique
		for (Function func : pivot.getFunctions())
			hdnBel = hdnBel.times(func);

		// collects messages from all neighbors of pivot and times them up
		for (AbstractNode neighbor : pivot.getNeighbors()) {
			CliqueNode clique = (CliqueNode) neighbor;

			// message from neighbor
			if (subtree.contains(clique)) {
				// recollects message
				hdnBel = hdnBel.times(collectMessage(clique, pivot, subtree, vars));
			} else {
				// reuses original message
				hdnBel = hdnBel.times(clique.getMessageTo(pivot));
			}
		}

		if (!(hdnVars.size() == hdnBel.getDimension())) {
			// marginalizes potential
			hdnBel = hdnBel.marginalize(hdnVars);
		}

		// normalizes potential
		hdnBel.normalize();

		return hdnBel.times(obsBel);
	}

	/**
	 * Returns the posterior probability distribution of the specified
	 * collection of variables.
	 * 
	 * @param vars
	 *            Collection of variables under query.
	 * @return The posterior probability distribution of the specified
	 *         collection of variables.
	 */
	public Function computeBelief(Collection<DiscreteVariable> vars) {

        if(vars.isEmpty())
            throw new IllegalArgumentException("The collection of variables under query in empty");

        if(!bayesNet.containsVars(vars))
            throw new IllegalArgumentException("Some of the variables under query are not present in the model");

		// collects hidden and observed variables in query nodes
		LinkedList<DiscreteVariable> hdnVars = new LinkedList<DiscreteVariable>();
		ArrayList<DiscreteVariable> obsVars = new ArrayList<DiscreteVariable>();
		ArrayList<Integer> obsVals = new ArrayList<Integer>();

		for (DiscreteVariable var : vars) {
			if (_evidence.containsKey(var)) {
				obsVars.add(var);
				obsVals.add(_evidence.get(var));
			} else {
				hdnVars.add(var);
			}
		}

		// belief over observed variables
		Function obsBel = Function.createIndicatorFunction(obsVars, obsVals);

		if (hdnVars.isEmpty()) {
			return obsBel;
		}

		// belief over hidden variables
		Function hdnBel = Function.createIdentityFunction();

		// constructs the minimal subtree that covers all query nodes
		// Set<CliqueNode> subtree = cliqueTree.computeMinimalSubtree(nodes);
		Set<CliqueNode> subtree = cliqueTree.computeMinimalSubtree(hdnVars);

		// uses first clique in the subtree as the pivot
		CliqueNode pivot = subtree.iterator().next();

		// computes the local potential at the pivot clique
		for (Function func : pivot.getFunctions()) {
			hdnBel = hdnBel.times(func);
		}

		// collects messages from all neighbors of pivot and times them up
		for (AbstractNode<String> neighbor : pivot.getNeighbors()) {
			CliqueNode clique = (CliqueNode) neighbor;

			// message from neighbor
			if (subtree.contains(clique)) {
				// recollects message
				hdnBel =
						hdnBel.times(collectMessage(clique, pivot, subtree,
								vars));
			} else {
				// reuses original message
				hdnBel = hdnBel.times(clique.getMessageTo(pivot));
			}
		}

		if (!(hdnVars.size() == hdnBel.getDimension())) {
			// marginalizes potential
			hdnBel = hdnBel.marginalize(hdnVars);
		}

		// normalizes potential
		hdnBel.normalize();

		return hdnBel.times(obsBel);
	}

	/**
	 * Returns the posterior probability distribution of the family of the
	 * specified variable. It is a function of all Variables in the family no
	 * matter it is observed or hidden.
	 * 
	 * @param var
	 *            variable under query.
	 * @return the posterior probability distribution of the family of the
	 *         specified variable.
	 */
	public Function computeFamilyBelief(DiscreteVariable var) {
		// associated BN must contain var
        if(var == null)
            throw new IllegalArgumentException("Variable cannot be null");

        if(!bayesNet.containsVar(var))
            throw new IllegalArgumentException("The variable under query is not present in the model");

		// collects hidden and observed variables in family
		LinkedList<DiscreteVariable> hdnVars = new LinkedList<DiscreteVariable>();
		ArrayList<DiscreteVariable> obsVars = new ArrayList<DiscreteVariable>();
		ArrayList<Integer> obsVals = new ArrayList<Integer>();

		if (_evidence.containsKey(var)) {
			obsVars.add(var);
			obsVals.add(_evidence.get(var));
		} else {
			hdnVars.add(var);
		}

		DiscreteBeliefNode node = bayesNet.getNode(var);
		for (AbstractNode parent : node.getParents()) {
			DiscreteBeliefNode bParent = (DiscreteBeliefNode) parent;
			DiscreteVariable vParent = bParent.getVariable();

			if (_evidence.containsKey(vParent)) {
				obsVars.add(vParent);
				obsVals.add(_evidence.get(vParent));
			} else {
				hdnVars.add(vParent);
			}
		}

		// belief over observed variables
		Function obsBel = Function.createIndicatorFunction(obsVars, obsVals);

		if (hdnVars.isEmpty()) {
			return obsBel;
		}

		// belief over hidden variables
		Function hdnBel = Function.createIdentityFunction();

		// computes potential at family covering clique
		CliqueNode familyClique = cliqueTree.getFamilyClique(var);

        // times up functions attached to family covering clique
        for (Function function : familyClique.getFunctions()) {
            hdnBel = hdnBel.times(function);
        }

		// (In the HLCM propogation case)After this, the hdnBel is superior to
		// any funtion multiplied.
		for (AbstractNode neighbor : familyClique.getNeighbors()) {
			hdnBel = hdnBel.times(((CliqueNode) neighbor).getMessageTo(familyClique));
		}

		if (!(hdnVars.size() == hdnBel.getDimension())) {
			// marginalizes potential
			hdnBel = hdnBel.marginalize(hdnVars);
		}

		// normalizes potential
		hdnBel.normalize();

		return hdnBel.times(obsBel);
	}

	/**
	 * Returns the likelihood of the evidences on the associated BN. Make sure
	 * that propogation has been conducted when calling this method.
	 */
	public double computeLikelihood() {
		CliqueNode pivot = cliqueTree.getPivot();

		// times up functions attached to pivot
		Function potential = Function.createIdentityFunction();
		for (Function function : pivot.getFunctions()) {
			potential = potential.times(function);
		}

		// times up messages to pivot
		double normalization = 1.0;
		double logNormalization = 0;
		for (AbstractNode<String> neighbor : pivot.getNeighbors()) {
			CliqueNode clique = (CliqueNode) neighbor;
			potential = potential.times(clique.getMessageTo(pivot));
			normalization *= clique.getNormalizationTo(pivot);
			logNormalization += clique.getLogNormalizationTo(pivot);
		}

		double n = potential.sumUp();
		lastLogLikelihood = logNormalization + Math.log(n);
		return n * normalization;
	}

	/**
	 * Returns the last log-likelihood computed. It is updated after each call
	 * of {@link}.
	 * 
	 * @return last log-likelihood computed
	 */
	//TODO: computeLogLikelihood ????
	public double getLastLogLikelihood() {
		return lastLogLikelihood;
	}

	/**
	 * Collects messages around the source and sends an aggregated message to
	 * the destination.
	 * 
	 * @param source
	 *            source around which messages are to be collected.
	 * @param destination
	 *            destination to which an aggregated message is to be sent.
	 */
	public void collectMessage(CliqueNode source, CliqueNode destination) {
		if (source.getMessageTo(destination) == null
				|| cliqueTree.inFocusedSubtree(source)) {
			// collects messages from neighbors of source except destination
			for (AbstractNode<String> neighbor : source.getNeighbors()) {
				if (neighbor != destination) {
					collectMessage((CliqueNode) neighbor, source);
				}
			}

			sendMessage(source, destination);
		}
	}

	/**
	 * Sends an aggregated message from the source to the destination and
	 * distributes the message around the destination.
	 * 
	 * @param source
	 * @param destination
	 */
	public void distributeMessage(CliqueNode source, CliqueNode destination) {
		if (cliqueTree.inFocusedSubtree(destination)) {

			sendMessage(source, destination);

			// distributes messages to neighbors of destination except source
			for (AbstractNode<String> neighbor : destination.getNeighbors()) {
				if (neighbor != source) {
					distributeMessage(destination, (CliqueNode) neighbor);
				}
			}
		}
	}

	/**
	 * Returns the BN that is associated with this CTP.
	 * 
	 * @return the BN that is associated with this CTP.
	 */
	public DiscreteBayesNet getBayesNet() {
		return bayesNet;
	}

	/**
	 * Get cliqueTree
	 * 
	 * @author csct
	 * @return cliqueTree
	 */
	public CliqueTree getCliqueTree() {
		return cliqueTree;
	}

	/**
	 * Propagates messages on the CT.
	 * 
	 * @return LL.
	 */
	public double propagate() {
		if (Thread.interrupted()) {
			throw new RuntimeException("Thread interrupted");
		}

		// absorbs evidences
		absorbEvidence();

		CliqueNode pivot = cliqueTree.getPivot();

		// collects messages from neighbors of pivot
		for (AbstractNode<String> neighbor : pivot.getNeighbors()) {
			collectMessage((CliqueNode) neighbor, pivot);
		}

		// distributes messages to neighbors of pivot
		for (AbstractNode<String> neighbor : pivot.getNeighbors()) {
			distributeMessage(pivot, (CliqueNode) neighbor);
		}

		return computeLikelihood();
	}

	/**
	 * Sends a message from the source to the destiation.
	 * 
	 * @param source
	 *            source of the message.
	 * @param destination
	 *            destination of the message.
	 */
	public void sendMessage(CliqueNode source, CliqueNode destination) {
		Function message = Function.createIdentityFunction();
		double normalization = 1.0;
		double logNormalization = 0;

		for (AbstractNode<String> neighbor : source.getNeighbors()) {
			if (neighbor != destination) {
				CliqueNode clique = (CliqueNode) neighbor;
				message = message.times(clique.getMessageTo(source));
				normalization *= clique.getNormalizationTo(source);
				logNormalization += clique.getLogNormalizationTo(source);
			}
		}

		for (Function function : source.getFunctions()) {
			message = message.times(function);
		}

		// sums out difference between source and destination
		for (DiscreteVariable var : source.getDifferenceTo(destination)) {
			if (!_evidence.containsKey(var)) {
				message = message.sumOut(var);
			}
		}

		// normalizes to alleviate round off error
		double n = message.normalize();
		normalization *= n;
		logNormalization += Math.log(n);

        //assert normalization >= Double.MIN_NORMAL;
        if(normalization < Double.MIN_NORMAL)
            throw new IllegalStateException("normalization value lower than Double.MIN_NORMAL");

		// saves message and normalization
		source.setMessageTo(destination, message);
		source.setNormalizationTo(destination, normalization);
		source.setLogNormalizationTo(destination, logNormalization);
	}

	public void setEvidence(List<DiscreteVariable> variables, int[] states) {

        if(variables.size() != states.length)
            throw new IllegalArgumentException("The variables and evidence sizes must coincide");

		_evidence.clear();

		for (int i = 0; i < variables.size(); i++) {
			// ignore this variable if its value is missing
			if (states[i] == DiscreteData.MISSING_VALUE)
				continue;

			DiscreteVariable var = variables.get(i);

			if(!bayesNet.containsVar(var))
			    throw new IllegalArgumentException("The Bayes net does not contain the variable: " + variables.get(i).getName());

            if(!variables.get(i).isValuePermitted(states[i]))
			    throw new IllegalArgumentException("the state with index [" + i + "] is not valid for the variable: " + variables.get(i).getName());

			_evidence.put(var, states[i]);
		}
	}

	public void setBayesNet(DiscreteBayesNet bayesNet) {
		this.bayesNet = bayesNet;
	}

	public void addEvidence(DiscreteVariable variable, int state) {
		DiscreteBeliefNode node = bayesNet.getNode(variable);

		if(node == null)
		    throw new IllegalArgumentException("The Bayes net does not contain a Belief node for the variable: " + variable.getName());

		if(!variable.isValuePermitted(state))
		    throw new IllegalArgumentException("Illegal state evidence for the variable: " + variable.getName());

		if (state == DiscreteData.MISSING_VALUE) {
			_evidence.remove(node);
		} else {
			_evidence.put(node.getVariable(), state);
		}
	}

	public int getEvidence(DiscreteVariable variable) {
		DiscreteBeliefNode node = bayesNet.getNode(variable);

        if(node == null)
            throw new IllegalArgumentException("The Bayes net does not contain a Belief node for the variable: " + variable.getName());

		if (_evidence.containsKey(node)) {
			return _evidence.get(node);
		} else {
			return DiscreteData.MISSING_VALUE;
		}
	}
}