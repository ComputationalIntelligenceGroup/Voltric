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
 * Created by equipo on 02/08/2017.
 */
public class AddArc implements HcOperator{

    /** The set of nodes that need to be avoided in the structure search process. */
    private List<Variable> blackList;

    /** The set of edges that need to be avoided in the structure search process. */
    private Map<Variable, List<Variable>> edgeBlackList;

    /** The structure type that is searched. It may constraint the resulting model. */
    private StructureType structureType;

    /** Maximum number of parent nodes. */
    private int maxNumberOfParents;

    /**
     * Main constructor.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process. All the edges that contain
     *                  a black-listed node will be avoided.
     * @param edgeBlackList The set of edges that need to be avoided in the structure search process. The key of the map
     *                      is the tail node and the List contains all the head nodes.
     * @param structureType The structure type that is searched. It may constraint the resulting model.
     */
    public AddArc(List<Variable> blackList, Map<Variable, List<Variable>> edgeBlackList, StructureType structureType, int maxNumberOfParents){
        this.blackList = blackList;
        this.edgeBlackList = edgeBlackList;
        this.structureType = structureType;
        this.maxNumberOfParents = maxNumberOfParents;
    }

    public AddArc(List<Variable> blackList, StructureType structureType, int maxNumberOfParents){
        this.blackList = blackList;
        this.structureType = structureType;
        this.maxNumberOfParents = maxNumberOfParents;
        this.edgeBlackList = new HashMap<>();
        for(Variable key: this.edgeBlackList.keySet())
            this.edgeBlackList.put(key, new ArrayList<>());

    }

    public AddArc(StructureType structureType, int maxNumberOfParents){
        this.blackList = new ArrayList<>();
        this.structureType = structureType;
        this.maxNumberOfParents = maxNumberOfParents;
        this.edgeBlackList = new HashMap<>();
        for(Variable key: this.edgeBlackList.keySet())
            this.edgeBlackList.put(key, new ArrayList<>());
    }

    /** {@inheritDoc} */
    public LearningResult<DiscreteBayesNet> apply(DiscreteBayesNet seedNet, DiscreteData data, DiscreteParameterLearning parameterLearning){

        // The BN is copied to avoid modifying current object.
        DiscreteBayesNet clonedNet = seedNet.clone();
        DiscreteBayesNet bestEdgeModel = clonedNet;
        double bestEdgeScore = -Double.MAX_VALUE; // Log-likelihood related scores are negative

        // The BN nodes are filtered using the blacklist.
        List<DiscreteBeliefNode> whiteList = clonedNet.getVariables().stream()
                .filter(x -> !this.blackList.contains(x))
                .map(var -> clonedNet.getNode(var))
                .collect(Collectors.toList());

        // Iteration through all the white-listed BN nodes
        for(DiscreteBeliefNode fromNode : whiteList) {
            for (DiscreteBeliefNode toNode : whiteList) {

                //First it checks they are not the same node
                if (!fromNode.equals(toNode)) {

                    // Then it checks the edge to be added is not forbidden
                    if ((!edgeBlackList.containsKey(fromNode.getVariable()) || !edgeBlackList.get(fromNode.getVariable()).contains(toNode.getVariable())) &&

                        // Then it checks the edge is not already present in the BN
                        !clonedNet.containsEdge(toNode, fromNode) &&

                        // Then it checks this new edge wouldn't surpass the allowed number of parent edges of the receiving node
                        toNode.getParentNodes().size() < this.maxNumberOfParents) {

                            //Then it tries to add the edge. An exception could be thrown if the edge is not permitted (i.e., if it creates a cycle)
                            try {

                                Edge<Variable> newEdge = clonedNet.addEdge(fromNode, toNode);

                                LearningResult<DiscreteBayesNet> newEdgeResult = parameterLearning.learnModel(clonedNet, data);
                                if (newEdgeResult.getScoreValue() > bestEdgeScore) {
                                    bestEdgeScore = newEdgeResult.getScoreValue();
                                    bestEdgeModel = newEdgeResult.getBayesianNetwork();
                                }

                                // The edge is removed for the next iteration to have the initial BN
                                clonedNet.removeEdge(newEdge);

                             // The exception catch does nothing because we are just following a brute force approach to add new edges
                            }catch (IllegalArgumentException e){}
                    }
                }
            }
        }

        return new LearningResult<>(bestEdgeModel, bestEdgeScore, parameterLearning.getScoreType());
    }
}
