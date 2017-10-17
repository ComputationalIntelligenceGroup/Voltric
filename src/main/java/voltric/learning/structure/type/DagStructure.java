package voltric.learning.structure.type;

import voltric.model.AbstractBayesNet;

/**
 * Created by fernando on 22/08/17.
 */
public class DagStructure implements StructureType {

    @Override
    public boolean allows(AbstractBayesNet bayesNet) {
        // Dado que siempre que se añade un arco se comprueba que no se formen ciclos,
        // el ser DAG siempre deberia cumplirse
        return true;
    }
}
