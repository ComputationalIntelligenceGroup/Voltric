package voltric.data;

import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a data row composed of discrete values.
 */
public class DiscreteDataInstance {

    /**
     * The dataSet the instance has access to. Null if the instance doesn't have access to any dataset. The only way an
     * instance is able to know its variables' types is with an assigned DataSet.
     */
    private DiscreteData data;

    /** The stored values. */
    private int[] values;

    /**
     * Constructs a DataInstance from a collection of values.
     *
     * @param values the instance's values.
     */
    public DiscreteDataInstance(int[] values){
        this.values = values;
        this.data = null;
    }

    /**
     * Returns the DataSet it belongs to.
     *
     * @return the DataSet it belongs to.
     */
    public DiscreteData getData(){
        return this.data;
    }

    /**
     * Sets the instance's DataSet, the one it belongs to.
     *
     * @param data the DataSet the instance belongs to.
     */
    public void setData(DiscreteData data){
        if(values.length != data.getVariables().size())
            throw new IllegalArgumentException("The number of columns must coincide");

        this.data = data;
    }

    /**
     * Returns the collection of variables associated to the data instance.
     *
     * @return the collection of variables associated to the data instance.
     */
    public List<DiscreteVariable> getVariables(){
        if(this.data == null)
            throw new UnassignedDataSetException("Instance hasn't yet been assigned to a Data object");

        return this.data.getVariables();
    }

    /**
     * Returns the collection of numeric values of the data instance.
     *
     * @return the collection of numeric values of the data instance.
     */
    public final int[] getNumericValues() {
        return values;
    }

    /**
     * Returns the numeric value associated to the specified index.
     *
     * @param index the argument index.
     * @return the numeric value associated to the specified index.
     */
    public int getNumericValue(int index) {
        return values[index];
    }

    /**
     * Returns the numeric value associated to the specified variable.
     *
     * @param variable the argument variable.
     * @return the numeric value associated to the specified variable.
     */
    public int getNumericValue(DiscreteVariable variable){
        return getNumericValue(this.getVariables().indexOf(variable));
    }

    /**
     * Returns the textual equivalents of the data instance's values.
     *
     * @return the textual equivalents of the data instance's values.
     */
    public final List<String> getTextualValues() {
        List<String> result = new ArrayList<String>(this.getVariables().size());

        for (int i = 0; i < values.length; i++)
            result.add(this.getTextualValue(i));

        return result;
    }

    /**
     * Returns the textual equivalent of the data instance's value.
     *
     * @param index the index of the variable.
     * @return the textual equivalent of the data instance's value.
     */
    public String getTextualValue(int index){
        DiscreteVariable discreteVariable = this.getVariables().get(index);
        return discreteVariable.getState(values[index]);
    }

    /**
     * Returns the textual value associated to the specified variable.
     *
     * @param variable the argument variable.
     * @return the textual value associated to the specified variable.
     */
    public String getTextualValue(DiscreteVariable variable){
        return getTextualValue(this.getVariables().indexOf(variable));
    }

    /**
     * Returns the data instance's number of columns.
     *
     * @return the data instance's number of columns.
     */
    public int size() {
        return values.length;
    }

    public DiscreteDataInstance project(List<DiscreteVariable> variableList){
        if(!this.data.getVariables().containsAll(variableList))
            throw new IllegalArgumentException("All the argument variables must be involved in this Data object");

        int[] projectedValues = new int[variableList.size()];

        for(DiscreteVariable variable: variableList)
            projectedValues[variableList.indexOf(variable)] = this.getNumericValue(variable);

        return new DiscreteDataInstance(projectedValues);
    }

    /**
     * Compares this data instance with the argument one based on their values.
     *
     * @param instance the instance being compared.
     * @return -1 if this is less than the {@code instance}, +1 if greater and 0 if they are equal.
     */
    public int compareTo(DiscreteDataInstance instance) {
        if(this.values.length != instance.values.length)
            throw new IllegalArgumentException("The number of columns of the data instances does not coincide");

        if(!this.getVariables().equals(instance.getVariables()))
            throw new IllegalArgumentException("The 'variables' object of both data instances does not coincide");

        for (int i = 0; i < values.length; i++) {
            if (values[i] != instance.values[i])
                if(values[i] < instance.values[i])
                    return -1;
                else if(instance.values[i] < values[i])
                    return 1;
        }

        return 0;
    }

    /**
     * Returns true if the <code>object</code> is a {@code DataInstance} with the same values and columns.
     *
     * @param object the object to test equality against.
     * @return true if {@code object} equals this.
     */
    @Override
    public boolean equals(Object object){
        if(this == object)
            return true;

        if(object instanceof DiscreteDataInstance == false)
            return  false;

        DiscreteDataInstance dataInstance = (DiscreteDataInstance) object;
        return this.data.equals(dataInstance.getData())
                && Arrays.equals(this.values,dataInstance.values);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }

    /**
     * Returns a string representation of the data instance.
     *
     * @return a string representation of the data instance.
     */
    @Override
    public String toString(){
        String s = "";
        for(int index = 0; index < values.length; index++) {
            if(index < values.length - 1)
                s += this.getTextualValue(index);
            else
                s += this.getTextualValue(index) + ", ";
        }

        return s;
    }
}
