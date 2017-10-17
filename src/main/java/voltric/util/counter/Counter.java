package voltric.util.counter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For generating indices and default names in some classes like in {@link voltric.variables.Variable}. It can also keep
 * track of the index and check for name clashes when manual names are being used thanks to the <code>encounterName</code> method.
 *
 * @author kmpoon
 * @author ferjorosa
 *
 * @see voltric.variables.Variable
 */
public class Counter {

    /** Current count. */
    private int current = 0;

    /** The prefix that is going to be used in the name */
    private final String prefix;

    /** The Patterne object that is going to be used to check if a name is valid. */
    private final Pattern pattern;

    /**
     * Constructs an Counter by passing its prefix.
     *
     * @param prefix the prefix of the counter that is going to be present in all its created instances.
     */
    public Counter(String prefix) {
        this.prefix = prefix;
        this.pattern = Pattern.compile(prefix + "(\\d*)");
    }

    /**
     * Creates a new {@link CounterInstance}.
     *
     * @return a new {@link CounterInstance}.
     */
    public synchronized CounterInstance next() {
        current++;
        return new CounterInstance(current, createName());
    }

    /**
     * The current count advances 1
     *
     * @return the new count.
     */
    public synchronized int nextIndex() {
        return current++;
    }

    /**
     * This method allows the counter to be updated with a bigger number than the current count.
     *
     * For example in the Variable class, if a variable has been created in other method with the name 'variable327' and the 'this.current'
     * value is '98', 'this.current' would be updated to '328' so the next random variable that was created would keep
     * following the creation order.
     *
     * @param name the name of the counter instance.
     *
     * @see voltric.variables.Variable
     */
    public synchronized void encounterName(String name) {
        Matcher match = pattern.matcher(name);
        if (match.matches()) {
            int number = Integer.parseInt(match.group(1));
            if (number >= current)
                current = number + 1;
        }
    }

    /**
     * Returns a default name using the prefix and the current count.
     *
     * @return a default name using the prefix and the current count.
     */
    private String createName() {
        return prefix + current;
    }
}
