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
 * Created by fernando on 25/08/17.
 */
public class ReverseArc implements HcOperator{

    /** The set of nodes that need to be avoided in the structure search process. */
    private List<Variable> blackList;

    /** The set of edges that need to be avoided in the structure search process. */
    private Map<Variable, List<Variable>> edgeBlackList;

    /** The structure type that is searched. It may constraint the resulting model. */
    private StructureType structureType;

    /** Maximum number of parents nodes. */
    private int maxNumberOfParents;

    /**
     * Main constructor.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process. All the edges that contain
     *                  a black-listed node will be avoided.
     * @param edgeBlackList the set of edges that need to be avoided in the structure search process. The key of the map
     *                      is the tail node and the List contains all the head nodes.
     * @param structureType The structure type that is searched. It may constraint the resulting model.
     * @param maxNumberOfParents The maximum number of parent nodes.
     */
    public ReverseArc(List<Variable> blackList, Map<Variable, List<Variable>> edgeBlackList, StructureType structureType, int maxNumberOfParents){
        this.blackList = blackList;
        this.edgeBlackList = edgeBlackList;
        this.structureType = structureType;
        this.maxNumberOfParents = maxNumberOfParents;
    }

    /**
     * Alternative constructor where there is no limit in the allowed number of node parents.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process. All the edges that contain
     *                  a black-listed node will be avoided.
     * @param edgeBlackList The set of edges that need to be avoided in the structure search process. The key of the map
     *                      is the tail node and the List contains all the head nodes.
     * @param structureType The structure type that is searched. It may constraint the resulting model.
     */
    public ReverseArc(List<Variable> blackList, Map<Variable, List<Variable>> edgeBlackList, StructureType structureType) {
        this(blackList, edgeBlackList, structureType, Integer.MAX_VALUE);
    }

    /**
     * This constructor accepts a Set of edges (to avoid repeated ones) as the black list.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process. All the edges that contain
     *                  a black-listed node will be avoided.
     * @param edgeBlackList The set of edges that need to be avoided in the structure search process.
     * @param structureType The structure type that is searched. It may constraint the resulting model.
     * @param maxNumberOfParents The maximum number of parent nodes.
     */
    public ReverseArc(List<Variable> blackList, List<Edge<Variable>> edgeBlackList, StructureType structureType, int maxNumberOfParents){
        this.blackList = blackList;
        this.edgeBlackList = new HashMap<>();
        this.structureType = structureType;
        this.maxNumberOfParents = maxNumberOfParents;

        for(Edge<Variable> edge: edgeBlackList)
            this.edgeBlackList.put(edge.getTail().getContent(), new ArrayList<>());

        for(Edge<Variable> edge: edgeBlackList)
            this.edgeBlackList.get(edge.getTail().getContent()).add(edge.getHead().getContent());
    }

    /**
     * Alternative constructor where there is no limit in the allowed number of node parents.This constructor accepts a
     * Set of edges (to avoid repeated ones) as the black list.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process. All the edges that contain
     *                  a black-listed node will be avoided.
     * @param edgeBlackList The set of edges that need to be avoided in the structure search process. The key of the map
     *                      is the tail node and the List contains all the head nodes.
     * @param structureType The structure type that is searched. It may constraint the resulting model.
     */
    public ReverseArc(List<Variable> blackList, List<Edge<Variable>> edgeBlackList, StructureType structureType){
        this(blackList, edgeBlackList, structureType, Integer.MAX_VALUE);
    }

    /** {@inheritDoc} */
    @Override
    public LearningResult<DiscreteBayesNet> apply(DiscreteBayesNet seedNet, DiscreteData data, DiscreteParameterLearning parameterLearning) {

        // The BN is copied to avoid modifying current object.
        DiscreteBayesNet clonedNet = seedNet.clone();
        DiscreteBayesNet bestEdgeModel = clonedNet;
        double bestEdgeScore = -Double.MAX_VALUE; // Log-likelihood related scores are negative

        // Iteration through all the BN's edges
        for(Edge<Variable> edge: clonedNet.getEdges()) {

            Variable edgeTail = edge.getTail().getContent();
            Variable edgeHead = edge.getHead().getContent();

            Variable reversedHead = edgeTail;

            // Checks that none of the edge's nodes is black-listed
            if (!blackList.contains(edgeTail) && !blackList.contains(edgeHead) &&

                // Check that the edge to be reversed is not forbidden
                (!edgeBlackList.containsKey(edgeTail) || !edgeBlackList.get(edgeTail).contains(edgeHead)) &&

                // Then it checks that this reversed edge wouldn't surpass the allowed number of parent nodes
                clonedNet.getNode(reversedHead).getParentNodes().size() < this.maxNumberOfParents) {

                //Then it tries to add the reversed edge. An exception could be thrown if the edge is not permitted (i.e., if it creates a cycle)
                try {
                    Edge<Variable> reversedEdge = clonedNet.reverseEdge(edge);

                    LearningResult<DiscreteBayesNet> newEdgeResult = parameterLearning.learnModel(clonedNet, data);
                    if (newEdgeResult.getScoreValue() > bestEdgeScore) {
                        bestEdgeScore = newEdgeResult.getScoreValue();
                        bestEdgeModel = newEdgeResult.getBayesianNetwork();
                    }

                    // Independently, the edge is once again reversed for the next iteration to have the initial BN
                    clonedNet.reverseEdge(reversedEdge);

                // The exception catch does nothing because we are just following a brute force approach to add new edges
                } catch (IllegalArgumentException e){}
            }
        }

        return new LearningResult<>(bestEdgeModel, bestEdgeScore, parameterLearning.getScoreType());
    }
}
