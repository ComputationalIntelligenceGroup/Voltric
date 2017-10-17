package voltric.learning.structure.type;

import voltric.model.AbstractBayesNet;

/**
 * Created by fernando on 22/08/17.
 */
public class TreeStructure implements StructureType {

    @Override
    public boolean allows(AbstractBayesNet bayesNet) {
        return bayesNet.isTree();
    }
}
