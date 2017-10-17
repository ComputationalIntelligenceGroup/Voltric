package voltric.model.visitor;

import voltric.model.AbstractBeliefNode;
import voltric.model.ContinuousBeliefNode;
import voltric.model.DiscreteBeliefNode;

/**
 * The visitor pattern interface for the subclasses of the {@link AbstractBeliefNode} class.
 *
 * @param <T> the return type parameter.
 *
 * @author ferjorosa
 */
public interface BeliefNodeVisitor<T> {

    /**
     * Generic visit method for the {@code DiscreteBeliefNode} class.
     *
     * @param node the node to visit.
     * @return the specific return of the visit.
     */
    T visit(DiscreteBeliefNode node);

    /**
     * Generic visit method for the {@code ContinuousBeliefNode} class.
     *
     * @param node the node to visit.
     * @return the specific return of the visit.
     */
    T visit(ContinuousBeliefNode node);
}
