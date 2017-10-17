package voltric.variables;

import voltric.util.counter.Counter;
import voltric.util.counter.CounterInstance;
import voltric.variables.modelTypes.VariableType;
import voltric.variables.visitor.VariableVisitor;

/**
 * This is the parent class for the discrete and continuous variable classes. It implements their common methods.
 *
 * @author kmpoon
 * @author ferjorosa
 */
public abstract class Variable implements IVariable, Comparable<Variable>{

    /** The variable counter */
    private static Counter variableCounter = new Counter("variable");

    /** The variable's identifier. It is an index that represents its creation order. */
    protected int index;

    /** The name of the variable */
    protected String name;

    /** The type of the variable (hidden or manifest) */
    protected VariableType type;

    /**
     * Constructs a variable with a default name that has been given by its <code>variableCounter</code>.
     */
    protected Variable(VariableType type) {
        CounterInstance instance = variableCounter.next();
        this.name = instance.name;
        this.index = instance.index;
        this.type = type;
    }

    /**
     * Constructs a variable with a given name, trimming its white spaces.
     *
     * @param name name of the variable.
     */
    protected Variable(String name, VariableType type) {
        this.name = name.trim();

        // _name cannot be blank
        if(this.name.length() <= 0)
            throw new IllegalArgumentException("name cannot be blank");

        // if the name follows the 'variableCounter' pattern, its internal count will be correctly updated
        variableCounter.encounterName(name);

        this.index = variableCounter.nextIndex();
        this.type = type;
    }

    /**
     * The accept method for the Variable visitor (see 'Visitor Pattern')
     *
     * @param variableVisitor the visitor.
     * @param <T> the return type of the variableVisitor.visit().
     * @return the result of the visit.
     */
    public abstract <T> T accept(VariableVisitor<T> variableVisitor);

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name){
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public VariableType getType(){
        return this.type;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isManifestVariable() {
        return this.type == VariableType.MANIFEST_VARIABLE;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLatentVariable() {
        return this.type == VariableType.LATENT_VARIABLE;
    }

    /**
     * Compares this variable with the specified variable for ordering.
     *
     * <p>
     * Note: <code>compareTo(Object)</code> is inconsistent with <code>equals(Object)</code>.
     * </p>
     *
     * @param another the variable to be compared.
     * @return a negative or a positive integer if this variable was created
     *         earlier than or later than the specified variable; zero if they
     *         refers to the same variable.
     */
    public int compareTo(final Variable another) {
        return this.index - another.index;
    }

    /**
     * Returns true if <code>object</code> is a <code>Variable</code> instance with the same index and name.
     *
     * @param object the object to test equality against.
     * @return true if object equals this.
     */
    @Override
    public boolean equals(Object object){
        if(this == object)
            return true;

        if(object.getClass() != this.getClass())
            return  false;

        Variable variable = (Variable) object;
        return this.name.equals(variable.name)
                && this.index == variable.index
                && this.type.equals(variable.type);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the variable.
     *
     * @return a string representation of the variable.
     */
    @Override
    public String toString(){
        return this.name + " [index = " + this.index + "]";
    }
}