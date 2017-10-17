package voltric.model.visitor;

import voltric.model.ContinuousBeliefNode;
import voltric.model.DiscreteBeliefNode;

/**
 * This visitor returns the variable's cardinality. Given that it is a characteristic of {@code DiscreteBeliefNode} only,
 * it will allow also allow us to distinguish between continuous and discrete nodes without using {@code instanceof}.
 *
 * <p><b>Note:</b> By using a visitor for this kind of behaviour, a useless polymorphic method is avoided ({@code getCardinality()}),
 * providing a cleaner API of methods for the {@code ContinuousBeliefNode} class.</p>
 */
public class GetCardinalityVisitor implements BeliefNodeVisitor<Integer> {

    /**
     * Returns the discrete node's cardinality.
     *
     * @param node the node to visit.
     * @return the discrete node's cardinality.
     */
    @Override
    public Integer visit(DiscreteBeliefNode node) {
        return node.getVariable().getCardinality();
    }

    /**
     * Returns -1 because a continuous node doesn't have cardinality.
     *
     * @param node the node to visit.
     * @return -1 because a continuous node doesn't have cardinality.
     */
    @Override
    public Integer visit(ContinuousBeliefNode node) {
        return -1;
    }
}
