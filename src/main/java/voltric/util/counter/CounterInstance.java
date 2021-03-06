package voltric.util.counter;

/**
 * This class represents an instance generated by the {@link Counter} class.
 *
 * @author kmpoon
 * @author ferjorosa
 *
 * @see Counter#next()
 */
public class CounterInstance {

    /** The index of the instance. */
    public final int index;

    /** The name of the instance. */
    public final String name;

    /**
     * Main constructor. A counter instance is create by passing both its index and name.
     *
     * @param index the index of the
     * @param name
     */
    CounterInstance(int index, String name) {
        this.index = index;
        this.name = name;
    }
}
