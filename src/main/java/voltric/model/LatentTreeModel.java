package voltric.model;

import java.util.List;

/**
 * Created by fernando on 30/03/17.
 */
public interface LatentTreeModel {

    /**
     * Returns the root node of this latent tree model, or {@code null} if this model does not contain any node.
     *
     * @return the root node of this latent tree model.
     */
    DiscreteBeliefNode getRoot();

    /**
     * Returns the collection of nodes that are neither the root nor leaf nodes.
     *
     * @return the collection of nodes that are neither the root nor leaf nodes.
     */
    List<DiscreteBeliefNode> getInternalNodes();

    /**
     * Returns the collection of nodes that have no children. No latent node should be returned.
     *
     * @return the collection of nodes that have no children.
     */
    List<DiscreteBeliefNode> getLeafNodes();
}
