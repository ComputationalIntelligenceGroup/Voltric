package voltric.graph;

import java.util.List;

/**
 * Created by equipo on 16/10/2017.
 */
public class GraphUtils {

    public static <T> UndirectedGraph<T> createCompleteGraph(List<T> nodeContentList){

        UndirectedGraph<T> completeGraph = new UndirectedGraph<T>();

        for(T content: nodeContentList)
            completeGraph.addNode(content);

        // TODO: Inefficient
        for(UndirectedNode<T> node1: completeGraph.getUndirectedNodes())
            for(UndirectedNode<T> node2: completeGraph.getUndirectedNodes()){
                if(!completeGraph.containsEdge(node2, node1))
                    completeGraph.addEdge(node2, node1);
            }

        return completeGraph;
    }
}
