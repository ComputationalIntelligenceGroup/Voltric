package voltric.variables;

import voltric.variables.modelTypes.VariableType;
import voltric.variables.visitor.VariableVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class provides an implementation for nominal getVariables (a finite number of states, where the order doesn't affect
 * the <code>equals</code> method).  Although the states of a nominal variable  are not ordered, we index them
 * by [0, 1, 2, ..., cardinality - 1] in this implementation.
 *
 * @author Yi Wang
 * @author ferjorosa
 */
public class DiscreteVariable extends Variable {

    /** Common prefix for default state names */
    private final static String STATE_PREFIX = "state";

    /** The states list of this discrete variable */
    private List<String> states;

    /**
     * Constructs a variable given a specific name and a list of states.
     *
     * @param name the name of the variable being created.
     * @param states the variable's list of states.
     */
    public DiscreteVariable(String name, List<String> states, VariableType type) {
        super(name, type);

        // states cannot be empty
        if(states.isEmpty())
            throw new IllegalArgumentException("states cannot be empty");

        this.states = states;
    }

    /**
     * Constructs a variable with a default name and a specific number of states (its cardinality).
     *
     * @param cardinality the cardinality of the new variable.
     */
    public DiscreteVariable(int cardinality, VariableType type) {
        super(type);

        this.states = createDefaultStates(cardinality);
    }

    /**
     * Constructs a variable with a specfic name and a specific number of states (its cardinality).
     *
     * @param cardinality the cardinality of the new variable.
     * @param name the name of the new variable.
     *
     * @see voltric.model.DiscreteBayesNet#decreaseCardinality(DiscreteVariable, int)
     * @see voltric.model.DiscreteBayesNet#increaseCardinality(DiscreteVariable, int)
     */
    public DiscreteVariable(int cardinality, VariableType type, String name) {
        super(name, type);

        this.states = createDefaultStates(cardinality);
    }

    /** {@inheritDoc} */
    public StateSpaceType getStateSpaceType(){
        return StateSpaceType.FINITE;
    }

    /**
     * Checks if the provided value belongs to the state space or not. It will only belong if the number is one
     * of the state indexes.
     *
     * @param value the value to check.
     * @return if the provided value belongs to the state space or not.
     */
    public boolean isValuePermitted(double value){
        return (int) value < this.getCardinality();
    }

    /**
     * Returns the cardinality of the variable. The cardinality of a nominal  variable equals its number of states.
     *
     * @return the cardinality of the variable.
     */
    public final int getCardinality() {
        return states.size();
    }

    /**
     * Returns the list of states of this variable.
     *
     * @return the list of states of this variable.
     */
    public final List<String> getStates() {
        return states;
    }

    /**
     * Returns the state associated with the argument index.
     *
     * @param stateIndex the argument index.
     * @return the state associated with the argument index.
     */
    public final String getState(int stateIndex){
        return getStates().get(stateIndex);
    }

    /**
     * Returns the index of the specified state in the domain of this variable.
     *
     * @param state state whose index is to be returned.
     * @return the index of the specified state in the domain of this variable.
     */
    public final int indexOf(String state) {
        return states.indexOf(state);
    }

    /**
     * Returns true if <code>state</code> is a valid state index int he variable's domain.
     *
     * @param state index of state whose validity is to be tested.
     * @return true if <code>state</code> is a valid state index int he variable's domain.
     */
    public final boolean isStateIndexValid(int state) {
        return (state >= 0 && state < getCardinality());
    }

    /** {@inheritDoc} */
    public <T> T accept(VariableVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Returns true if <code>object</code> is a <code>DiscreteVariable</code> instance where
     * its index, name and list of states coincide.
     *
     * @param object the object to test equality against.
     * @return true if object equals this.
     */
    @Override
    public final boolean equals(Object object) {
        if (this == object)
            return true;

        if(object.getClass() != this.getClass())
            return false;

        DiscreteVariable variable = (DiscreteVariable) object;
        return this.name.equals(variable.name)
                && this.index == variable.index
                && this.type.equals(variable.type)
                && this.states.equals(variable.states);
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
        result = 31 * result + states.hashCode();
        return result;
    }

    /**
     * Returns a string representation of the variable.
     *
     * @return a string representation of the variable.
     */
    @Override
    public String toString() {
        String s= "";
        s += "Discrete variable (" + this.name + "): {";

        for(int i = 0; i < states.size() - 1; i++)
            s+= states.get(i) + ", ";

        s += states.get(states.size() - 1) + "}";
        return s;
    }

    /**
     * Returns a list of default state names for the argument cardinality.
     *
     * @param cardinality number of states.
     * @return the list of default names of states for the specified cardinality.
     */
    public static ArrayList<String> createDefaultStates(int cardinality) {
        if(cardinality <= 0)
            throw new IllegalArgumentException("The variable's cardinality must be > 0");

        ArrayList<String> states = new ArrayList<String>();

        for (int i = 0; i < cardinality; i++)
            states.add(STATE_PREFIX + i);

        return states;
    }

    /**
     * Returns the cardinality of a list of discrete getVariables, i.e., the number
     * of possible combinations of their states.
     *
     * @param variables list of discrete getVariables.
     * @return cardinality of the discrete getVariables.
     */
    public static int getCardinality(Collection<DiscreteVariable> variables) {
        int cardinality = 1;
        for (DiscreteVariable variable : variables)
            cardinality *= variable.getCardinality();

        return cardinality;
    }
}
