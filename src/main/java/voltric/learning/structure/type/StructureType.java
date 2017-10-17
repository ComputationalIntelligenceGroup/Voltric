package voltric.learning.structure.type;

import voltric.model.AbstractBayesNet;

/**
 * Other possibility would be PolyTrees.
 */
public interface StructureType {

    boolean allows(AbstractBayesNet bayesNet);
}
