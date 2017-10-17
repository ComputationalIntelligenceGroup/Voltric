package voltric.learning.structure.hillclimbing.operator;

import voltric.data.DiscreteData;
import voltric.graph.Edge;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.structure.type.StructureType;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.variables.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Solo deberia poder eliminar arcos que enlacen nodos pertenecientes a la whitelist, si alguno lo de los nodos del arco
 * pertence a la whitelist
 */
public class RemoveArc implements HcOperator{

    /** The set of nodes that need to be avoided in the structure search process. All the edges containing black-listed
     * nodes will be avoided.
     */
    private List<Variable> blackList;

    /** The set of edges that need to be avoided in the structure search process. The key of the map is the tail node
     * and the List contains all the head nodes.
     */
    private Map<Variable, List<Variable>> edgeBlackList;

    /** The structure type that is searched. It may constraint the resulting model. */
    private StructureType structureType;

    /**
     * Main constructor.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process. All the edges that contain
     *                  a black-listed node will be avoided.
     * @param edgeBlackList the set of edges that need to be avoided in the structure search process. The key of the map
     *                      is the tail node and the List contains all the head nodes.
     * @param structureType The structure type that is searched. It may constraint the resulting model.
     */
    public RemoveArc(List<Variable> blackList, Map<Variable, List<Variable>> edgeBlackList, StructureType structureType){
        this.blackList = blackList;
        this.edgeBlackList = edgeBlackList;
        this.structureType = structureType;
    }

    /**
     * This constructor accepts a Set of edges (to avoid repeated ones) as the black list.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process. All the edges that contain
     *                  a black-listed node will be avoided.
     * @param edgeBlackList The set of edges that need to be avoided in the structure search process.
     * @param structureType The structure type that is searched. It may constraint the resulting model.
     */
    public RemoveArc(List<Variable> blackList, List<Edge<Variable>> edgeBlackList, StructureType structureType){
        this.blackList = blackList;
        this.edgeBlackList = new HashMap<>();
        this.structureType = structureType;

        for(Edge<Variable> edge: edgeBlackList)
            this.edgeBlackList.put(edge.getTail().getContent(), new ArrayList<>());

        for(Edge<Variable> edge: edgeBlackList)
            this.edgeBlackList.get(edge.getTail().getContent()).add(edge.getHead().getContent());
    }

    /** {@inheritDoc} */
    @Override
    public LearningResult<DiscreteBayesNet> apply(DiscreteBayesNet seedNet, DiscreteData data, DiscreteParameterLearning parameterLearning) {

        // The BN is copied to avoid modifying current object.
        DiscreteBayesNet clonedNet = seedNet.clone();
        DiscreteBayesNet bestEdgeModel = clonedNet;
        double bestEdgeScore = -Double.MAX_VALUE; // Log-likelihood related scores are negative

        // The BN nodes are filtered using the blacklist.
        List<DiscreteBeliefNode> whiteList = clonedNet.getVariables().stream()
                .filter(x -> !this.blackList.contains(x))
                .map(var -> clonedNet.getNode(var))
                .collect(Collectors.toList());

        // Iteration through all the BN's edges
        for(Edge<Variable> edge: clonedNet.getEdges()) {

            Variable edgeTail = edge.getTail().getContent();
            Variable edgeHead = edge.getHead().getContent();

            // Check the edge to be removed is not forbidden
            if (!blackList.contains(edgeTail) && !blackList.contains(edgeHead) &&

                // Check that the edge to be removed is not forbidden
                (!edgeBlackList.containsKey(edgeTail) || !edgeBlackList.get(edgeTail).contains(edgeHead))) {

                clonedNet.removeEdge(edge);

                LearningResult<DiscreteBayesNet> newEdgeResult = parameterLearning.learnModel(clonedNet, data);
                if (newEdgeResult.getScoreValue() > bestEdgeScore) {
                    bestEdgeScore = newEdgeResult.getScoreValue();
                    bestEdgeModel = newEdgeResult.getBayesianNetwork();
                }

                // The edge is reassigned for the next iteration to have the initial BN
                clonedNet.addEdge(clonedNet.getNode(edge.getTail().getContent()), clonedNet.getNode(edge.getHead().getContent()));
            }
        }

        return new LearningResult<>(bestEdgeModel, bestEdgeScore, parameterLearning.getScoreType());
    }
}
