package voltric.model.creator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import voltric.data.DiscreteData;
import voltric.graph.weighted.WeightedUndirectedGraph;
import voltric.model.DiscreteBeliefNode;
import voltric.model.HLCM;
import voltric.util.stattest.discrete.DiscreteStatisticalTest;
import voltric.variables.DiscreteVariable;
import voltric.variables.modelTypes.VariableType;
import voltric.learning.structure.chowliu.ChowLiu;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by equipo on 20/04/2017.
 */
public class HlcmCreator {

    // createFlatLTM

    /**
     *
     *
     * @param clusters
     * @param dataSet
     * @param root
     * @return
     */
    // TODO: La root debe pertenecer a los clusters, tiene que ser uno de ellos
    public static HLCM createFlatLTM(List<HLCM> clusters, DiscreteVariable root, DiscreteData dataSet, DiscreteStatisticalTest statisticalTest){
        HLCM unconnectedClusters = LfmCreator.createLFM(clusters);
        List<DiscreteVariable> clusterRoots = clusters.stream().map(x->x.getRoot().getVariable()).collect(Collectors.toList());
        WeightedUndirectedGraph<DiscreteVariable> chowLiuTree = ChowLiu.learnChowLiuTree(clusterRoots, unconnectedClusters, dataSet, statisticalTest);

        throw new NotImplementedException();

    }

    /**
     *
     *
     * @param clusters
     * @param dataSet
     * @return
     */
    // A random cluster is chosen, whose root variable will act as the new tree's root
    public static HLCM createFlatLtmRandomRoot(List<HLCM> clusters, DiscreteData dataSet, DiscreteStatisticalTest statisticalTest){
        Random random = new Random();
        int randomRootIndex = random.nextInt(clusters.size() - 1);
        return createFlatLTM(clusters, clusters.get(randomRootIndex).getRoot().getVariable(), dataSet, statisticalTest);
    }

    // createMultilevel

    /**
     *
     * @return
     */
    public static HLCM createOneLevelLTM(){
        return null;
    }

    // createLCM

    /**
     * Create an LCM from a collection of manifest Vars. The parameters are randomly set.
     *
     * @param manifestVariables manifest variables.
     * @param rootCardinality The cardinality of the new latent variable.
     * @param name The new LCM's name
     * @return The resulting LCM.
     */
    public static HLCM createLCM(List<DiscreteVariable> manifestVariables, int rootCardinality, String name){

        HLCM lcm = new HLCM(name);
        DiscreteBeliefNode root = lcm.addNode(new DiscreteVariable(rootCardinality, VariableType.LATENT_VARIABLE));

        for(DiscreteVariable variable: manifestVariables)
            lcm.addEdge(lcm.addNode(variable), root);

        lcm.randomlyParameterize();

        return lcm;
    }

    /**
     * Create an LCM from a collection of manifest Vars. The parameters are randomly set.
     *
     * @param manifestVariables manifest variables.
     * @param rootCardinality The cardinality of the new latent variable.
     * @return The resulting LCM.
     */
    public static HLCM createLCM(List<DiscreteVariable> manifestVariables, int rootCardinality){
        return createLCM(manifestVariables, rootCardinality, "LCM");
    }
}
