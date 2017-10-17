package voltric.graph.weighted;

import voltric.graph.Edge;

import java.util.Map;

/**
 * Interface that provides the basic elements of a WeightedGraph
 *
 * @param <T> the node's content type.
 */
public interface WeightedGraph<T> {

    /** The default weight for an edge. */
    double DEFAULT_EDGE_WEIGHT = 1.0;

    /**
     * Sets the weight of an specific edge. An exception will be thrown if the edge doesn't belong to the graph.
     *
     * @param edge the specific edge.
     * @param weight the new edge weight.
     */
    void setEdgeWeight(Edge<T> edge, double weight);

    /**
     * Returns the weight associated to an edge. An exception will be thrown if the edge doesn't belong to the graph.
     *
     * @param edge the specific edge.
     * @return the weight associated to the edge.
     */
    double getEdgeWeight(Edge<T> edge);

    /**
     * Returns the collection of edge weights.
     *
     * @return the collection of edge weights.
     */
    Map<Edge<T>, Double> getEdgeWeights();
}
