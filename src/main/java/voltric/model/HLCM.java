package voltric.model;

import voltric.graph.Edge;
import voltric.variables.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class HLCM extends DiscreteBayesNet implements LatentTreeModel {

    /**
     * Default constructor. It creates an empty HLCM with a default name.
     */
    public HLCM(){
        super("Hierarchical Latent Cluster Model");
    }

    /**
     * Constructs a discrete Bayesian network with the associated name
     *
     * @param name name of the new HLCM
     */
    public HLCM(String name){
        super(name.trim());
    }

    /**
     * Copy constructor. The name, DAG and log-likelihoods are added to this new HLCM. However, they are not added as
     * references, but as new instances.
     *
     * @param hlcm the HLCM being copied.
     */
    public HLCM(HLCM hlcm){
        super(hlcm);
    }

    /** {@inheritDoc} */
    @Override
    public DiscreteBeliefNode getRoot() {

        List<DiscreteBeliefNode> allNodes = this.getNodes().stream().map(x -> (DiscreteBeliefNode) x).collect(Collectors.toList());

        List<DiscreteBeliefNode> roots = allNodes.stream().filter(x -> x.isRoot()).collect(Collectors.toList());

        // TODO: Do checks with Tests and remove this type of error handling
        if (roots.size() > 1)
            throw new InternalError("BUG: the number of roots of this tree is " + roots.size() + ". " +
                    "Please report this problem to the library developers.");

        return roots.get(0);
    }

    /** {@inheritDoc} */
    @Override
    public List<DiscreteBeliefNode> getInternalNodes() {
        List<DiscreteBeliefNode> allNodes = this.getNodes().stream().map(x -> (DiscreteBeliefNode) x).collect(Collectors.toList());

        return allNodes.stream().filter(node -> !node.isRoot() && !node.isLeaf())
                .collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public List<DiscreteBeliefNode> getLeafNodes() {
        List<DiscreteBeliefNode> allNodes = this.getNodes().stream().map(x -> (DiscreteBeliefNode) x).collect(Collectors.toList());

        return allNodes.stream().filter(node -> node.isLeaf())
                .collect(Collectors.toList());
    }

    /**
     * Adds an edge that connects the two specified nodes to this BN and returns
     * the edge. This implementation extends <code>AbstractGraph.addEdge(AbstractNode, AbstractNode)</code> such
     * that all loglikelihoods will be expired.
     * <p>
     * <p>The resulting edge is {@code head <- tail}.</p>
     * <p>
     * <p>While this method does no require the user to specify if the nodes are manifest or latent, there is an
     * important restriction in HLCM models: <b>Manifest nodes cannot have edges to latent nodes</b>. </p>
     *
     * @param head head of the edge.
     * @param tail tail of the edge.
     * @return the edge that was added to this BN.
     */
    //TODO: PAra evitar el casting de AbstractNode a DiscreteBeliefNode es necesario implementar una clase "interfaz" de BN
    @Override
    public Edge<Variable> addEdge(AbstractBeliefNode head, AbstractBeliefNode tail) {

        DiscreteBeliefNode headBeliefNode = (DiscreteBeliefNode) head;
        DiscreteBeliefNode tailBeliefNode = (DiscreteBeliefNode) tail;

        if (headBeliefNode.isLatent() && tailBeliefNode.isManifest())
            throw new IllegalArgumentException("A latent node cannot have a manifest node as parent");

        return super.addEdge(head, tail);
    }

    /**
     * Returns {@code true} if the object is a {@code HLCM} with equal fields (inherited ones included).
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

        HLCM hlcm = (HLCM) object;
        return this.name.equals(hlcm.name)
                && this.dag.equals(hlcm.dag);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 3 * result + dag.hashCode();
        return result;
    }

    /**
     * Creates and returns a deep copy of this Bayesian network. This implementation copies everything in it.
     * Consequently, it is safe to do anything you want to the deep copy.
     *
     * @return a deep copy of this network.
     */
    @Override
    public HLCM clone(){
        return new HLCM(this);
    }


}