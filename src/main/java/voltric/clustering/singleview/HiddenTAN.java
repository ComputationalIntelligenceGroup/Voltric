package voltric.clustering.singleview;

import voltric.data.DiscreteData;
import voltric.graph.AbstractNode;
import voltric.graph.DirectedAcyclicGraph;
import voltric.graph.DirectedNode;
import voltric.graph.Edge;
import voltric.graph.weighted.WeightedUndirectedGraph;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.learning.structure.chowliu.ChowLiu;
import voltric.model.DiscreteBayesNet;
import voltric.model.HLCM;
import voltric.model.creator.HlcmCreator;
import voltric.util.stattest.discrete.DiscreteStatisticalTest;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * TODO: El Chow-Liu debe permitir escoger que tipo de Mutual information quieres utilizar
 */
public class HiddenTAN {

    // Esto es la parte SEM del Hidden TAN
    public static LearningResult<DiscreteBayesNet> learnModel(int maxCardinality,
                                                              DiscreteData dataSet,
                                                              DiscreteParameterLearning parameterLearning,
                                                              double threshold,
                                                              boolean randomChowLiuRoot,
                                                              DiscreteStatisticalTest statisticalTest){

        int currentCardinality = 1;

        HLCM previousModel = HlcmCreator.createLCM(dataSet.getVariables(), 2); // dumb Model
        double previousScore = -Double.MAX_VALUE;
        HLCM iterationModel = previousModel.clone(); // copied dumb Model
        double iterationScore = -Double.MAX_VALUE;

        while(currentCardinality <= maxCardinality){

            // cardinality is increased
            currentCardinality = currentCardinality + 1;

            // Initially a NB model with the updated cardinality is learned to estimate the Chow-Liu Tree
            iterationModel = (HLCM) parameterLearning
                    .learnModel(HlcmCreator.createLCM(dataSet.getVariables(), currentCardinality), dataSet)
                    .getBayesianNetwork();

            // Estimate the Chow-Liu tree of its manifest variables conditioned on the root of the NB model
            WeightedUndirectedGraph<DiscreteVariable> clTree = ChowLiu.learnChowLiuTree(iterationModel.getManifestVariables(),
                    iterationModel.getRoot().getVariable(), iterationModel, dataSet, statisticalTest);

            // The TAN model resulted from this iteration
            LearningResult<DiscreteBayesNet> iterationResult;
            if(randomChowLiuRoot)
                iterationResult = learnRandomRootTAN(iterationModel, parameterLearning, dataSet, clTree);
            else
                iterationResult = learnBestRootTAN(iterationModel, parameterLearning, dataSet, clTree);

            iterationModel = (HLCM) iterationResult.getBayesianNetwork();
            iterationScore = iterationResult.getScoreValue();

            // Current iteration didn't improve previous one, so we return the previous model
            if(previousScore > iterationScore)
                return new LearningResult<>(previousModel, previousScore, parameterLearning.getScoreType());

            // Current iteration improved previous one but didn't surpassed the threshold
            if(Math.abs(iterationScore - previousScore) > threshold)
                return iterationResult;

            // Current iteration is now the previous one
            previousModel = iterationModel;
            previousScore = iterationScore;
        }

        // If the model wasn't returned before reached the maximum cardinality, the last iteration's model is returned
        // It is learned in case there wasn't even one iteration. A NB model with cardinality 2 would then be returned (the dumb model)
        return parameterLearning.learnModel(iterationModel, dataSet);
    }

    private static LearningResult<DiscreteBayesNet> learnRandomRootTAN(HLCM initialModel,
                                                                       DiscreteParameterLearning parameterLearning,
                                                                       DiscreteData dataSet,
                                                                       WeightedUndirectedGraph<DiscreteVariable> chowLiuTree){

        // A random node is chosen to be the root of the Chow-Liu tree
        // Note: Do not confuse this root with the root of the Naive Bayes model.
        Random random = new Random();
        int rootIndex = random.nextInt(initialModel.getManifestNodes().size() - 1);

        return learnTAN(initialModel, parameterLearning, dataSet, chowLiuTree, rootIndex);
    }

    private static LearningResult<DiscreteBayesNet> learnBestRootTAN(HLCM initialModel,
                                                                     DiscreteParameterLearning parameterLearning,
                                                                     DiscreteData dataSet,
                                                                     WeightedUndirectedGraph<DiscreteVariable> chowLiuTree){

        LearningResult<DiscreteBayesNet> bestLearningResult = null;

        for(int i = 0; i < initialModel.getManifestNodes().size(); i++){

            // The model is cloned so it wont interfere with subsequent iterations
            HLCM clonedModel = initialModel.clone();

            LearningResult<DiscreteBayesNet> result = learnTAN(clonedModel, parameterLearning, dataSet, chowLiuTree, i);
            if(bestLearningResult == null || bestLearningResult.getScoreValue() < result.getScoreValue())
                bestLearningResult = result;
        }
        return  bestLearningResult;
    }

    private static LearningResult<DiscreteBayesNet> learnTAN(HLCM initialModel,
                                                             DiscreteParameterLearning parameterLearning,
                                                             DiscreteData dataSet,
                                                             WeightedUndirectedGraph<DiscreteVariable> chowLiuTree,
                                                             int rootIndex){

        // After that we have an undirected tree. To make it directed, a root node is used.
        AbstractNode<DiscreteVariable> root = chowLiuTree.getNodes().get(rootIndex);

        // Once the CL tree root has been chosen, a directed graph is created by recursively iterating through the graph
        List<AbstractNode<DiscreteVariable>> visitedNodes = new ArrayList<>();
        visitedNodes.add(root);
        DirectedAcyclicGraph<DiscreteVariable> directedClTree = new DirectedAcyclicGraph<>();
        directedClTree.addNode(root.getContent());
        iterateChildNodes(directedClTree, visitedNodes, root);

        // Now that the DAG has been filled, its edges are added to the Naive Bayes,
        // generating a Tree-agumented Naive Bayes model (TAN)
        for(Edge<DiscreteVariable> edge: directedClTree.getEdges()){
            initialModel.addEdge(initialModel.getNode(edge.getTail().getContent()),
                    initialModel.getNode(edge.getHead().getContent()));
        }

        // The TAN parameters are learned and the model is returned
        return parameterLearning.learnModel(initialModel, dataSet);
    }

    // tailRecursive method
    private static void iterateChildNodes(DirectedAcyclicGraph<DiscreteVariable> resultingGraph,
                                   List<AbstractNode<DiscreteVariable>> visitedNodes,
                                   AbstractNode<DiscreteVariable> node){

        for (AbstractNode<DiscreteVariable> neighbour: node.getNeighbors()){
            if(!visitedNodes.contains(neighbour)){

                // The neighbour node is set as visited
                visitedNodes.add(neighbour);

                // The new graph's nodes are required for adding the new edge between them
                DirectedNode<DiscreteVariable> neighbourNode = resultingGraph.addNode(neighbour.getContent()); // new -> added
                DirectedNode<DiscreteVariable> fromNode = resultingGraph.getNode(node.getContent()); // old -> retrieved

                resultingGraph.addEdge(fromNode, neighbourNode);
                iterateChildNodes(resultingGraph, visitedNodes, neighbour);
            }
        }
    }

}
