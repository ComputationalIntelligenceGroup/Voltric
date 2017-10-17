package voltric.graph;

import java.util.Collection;

/**
 * This class provides an implementation for {@link UndirectedGraph} nodes.
 *
 * Each node has an unique ID because we cannot test equality with all the fields or there would be an infinite equality loop.
 * And because this class and its subclasses are not Clonable, there would not be two nodes with the same ID. This is why
 * its {@code equals} and @code hashcode methods do not take on consideration the {@code graph} field (circular dependencies).
 *
 * @author Yi Wang
 * @author ferjorosa
 */
public class UndirectedNode<T> extends AbstractNode<T> {

    /**
     * Constructs a node with the specified name and the specified graph to contain it.
     *
     * <p>
     * <b>Note: Besides constructors of subclasses, only {@code UndirectedGraph.addNode(String)} is supposed to call
     * this method. </b>
     * </p>
     *
     * @param graph graph to contain this node.
     * @param content content of this node.
     * @see UndirectedGraph#addNode(Object)
     */
    protected UndirectedNode(AbstractGraph<T> graph, T content) {
        super(graph, content);
    }

    /**
     * Returns the deficiency of this node. The deficiency of a node in an {@code UndirectedGraph} is the number
     * of broken pairs of neighbors.
     *
     * @return the deficiency of this node.
     */
    public final int computeDeficiency() {
        int deficiency = 0;

        // tests each pair of neighbors twice
        for (AbstractNode neighbor1 : getNeighbors()) {
            for (AbstractNode neighbor2 : getNeighbors()) {
                if (!neighbor1.hasNeighbor(neighbor2)) {
                    deficiency++;
                }
            }
        }

        // divides by 2 due to double counting
        return deficiency / 2;
    }

    /** {@inheritDoc} */
    public UndirectedGraph<T> getGraph(){
        return (UndirectedGraph<T>) this.graph;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Edge<T>> getAdjacentEdges() {
        return super.getEdges();
    }

    /**
     * Returns {@code true} if the object is an {@code UndirectedNode} with equal fields (inherited ones included).
     * It avoids circular dependencies by using the graph's unique ID.
     *
     * @param object the object to test equality against.
     * @return true if {@code object} equals this.
     */
    @Override
    public boolean equals(Object object){
        if(this == object)
            return true;

        if(object.getClass() != this.getClass())
            return  false;

        AbstractNode node = (AbstractNode) object;
        return this.graph.uniqueID.equals(node.graph.uniqueID)
                && this.content.equals(node.content);
    }

    /**
     * Returns the object's hashcode. It avoids circular dependencies by using the graph's unique ID.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 29 * result + this.graph.uniqueID.hashCode();
        return result;
    }


    /** {@inheritDoc} */
    @Override
    public String toString(int amount) {
        // amount must be non-negative
        if(amount <= 0)
            throw new IllegalArgumentException("amount must be positive");

        // prepares white space for indent
        StringBuffer whiteSpace = new StringBuffer();
        for (int i = 0; i < amount; i++) {
            whiteSpace.append("\t");
        }

        // builds string representation
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(whiteSpace);
        stringBuffer.append("undirected node {\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tcontent = \"" + this.content.toString() + "\";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tdegree = " + getDegree() + ";\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("\tneighbors = { ");

        for (AbstractNode neighbor : getNeighbors()) {
            stringBuffer.append("\"" + neighbor.content.toString()  + "\" ");
        }

        stringBuffer.append("};\n");

        stringBuffer.append(whiteSpace);
        stringBuffer.append("};\n");

        return stringBuffer.toString();
    }

}
