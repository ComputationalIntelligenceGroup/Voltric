package voltric.data;

import voltric.variables.DiscreteVariable;

import java.util.*;

/**
 * Created by fernando on 7/08/17.
 */
public class DiscreteData {

    public static int MISSING_VALUE = -1;

    /** The associated name. */
    private String name;

    /** The data rows. */
    private List<DiscreteDataInstance> instances = new ArrayList<>();

    /** The data columns. */
    private List<DiscreteVariable> variables = new ArrayList<>();

    /**
     * The weight associated to each data instance.
     *
     * Note: It is a double value for easier computations (i.e. Entropy, Mutual Information, etc.). However it can only
     * be set with integer values, given that it represents the number of repetitions.
     */
    private Map<DiscreteDataInstance, Integer> instanceWeights = new HashMap<>();

    /**
     * Constructs a new {@code Data} object by providing its collection of instances, variables and its name.
     *
     * @param name the name of the data.
     * @param variables the data columns.
     */
    public DiscreteData(String name, List<DiscreteVariable> variables) {
        this.name = name;
        this.variables = variables;
    }

    /**
     * Constructs a new {@code Data} object with a name by default.
     *
     * @param variables the data columns.
     */
    public DiscreteData(List<DiscreteVariable> variables) {
        this.name = "data";
        this.variables = variables;
    }

    /**
     * Returns the name of the {@code Data} object.
     *
     * @return the name of the {@code Data} object.
     */
    public String getName() {
        return name;
    }

    /**
     * Modifies the name of {@code Data} by assigning a new name.
     *
     * @param name Data's new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns its collection of variables.
     */
    public List<DiscreteVariable> getVariables() {
        return variables;
    }

    /**
     * @return Returns its collection of data instances.
     */
    public List<DiscreteDataInstance> getInstances() {
        return instances;
    }

    /**
     * Returns the weight of the instance, its number of repetitions.
     *
     * @return the weight of the instance, its number of repetitions.
     */
    public int getWeight(DiscreteDataInstance dataInstance){
        return instanceWeights.get(dataInstance);
    }

    /**
     * Modifies the weight of the instance by setting a new value.
     *
     * @param weight the new weight of the instance.
     */
    public void setWeight(DiscreteDataInstance dataInstance, int weight){
        this.instanceWeights.put(dataInstance, weight);
    }

    public double getTotalWeight(){
        Optional<Integer> sum = instanceWeights.values().stream().reduce((x, y) -> x +y);
        if(sum.isPresent())
            return sum.get();
        else
            return 0;
    }

    public boolean hasMissingValues(){
        return false;
    }

    public void add(DiscreteDataInstance dataInstance, int weight){

        dataInstance.setData(this);

        // finds the position for this data instance
        int index = instances.indexOf(dataInstance);

        if(index < 0) {
            // check if the instance is permitted
            if (!this.isInstancePermitted(dataInstance))
                throw new IllegalArgumentException("Data instance is not permitted");

            // adds unseen data case
            instances.add(dataInstance);
            instanceWeights.put(dataInstance, weight);
        }else{
            // increases weight for the existing data instance
            instanceWeights.put(dataInstance, instanceWeights.get(dataInstance) + weight);
        }
    }

    public void add(DiscreteDataInstance dataInstance){
        this.add(dataInstance, 1);
    }

    /**
     * Projects current data to a new dimension, thus generating a new {@code Data} object.
     *
     * @param variableList the subset of variables that conforms the new dimension of data.
     * @return the projected {@code Data} object.
     */
    public DiscreteData project(List<DiscreteVariable> variableList){
        // Note: The comprobation that the variableList is valid is made in the dataInstance.project method call
        DiscreteData projectedData = new DiscreteData(new ArrayList<>(variableList));

        for(DiscreteDataInstance dataInstance: this.instances)
            projectedData.add(dataInstance.project(variableList), this.getWeight(dataInstance));

        return projectedData;
    }

    /**
     * Projects current data to a new one-dimension, thus generating a new {@code Data} object.
     *
     * @param variable the variable that conforms the new dimension of data.
     * @return the projected {@code Data} object.
     * @see DiscreteData#project(List)
     */
    public DiscreteData project(DiscreteVariable variable){
        List<DiscreteVariable> variableList = new ArrayList<>();
        variableList.add(variable);
        return this.project(variableList);
    }

    /**
     * Checks if each of the instance's values belong to the state space of its associated variable.
     *
     * @param instance the instance to be checked
     * @return true if all the values belong to the associated state space.
     */
    public boolean isInstancePermitted(DiscreteDataInstance instance){
        if(instance.size() != this.variables.size())
            return false;

        for(int i=0; i < instance.getNumericValues().length; i++)
            if(!this.variables.get(i).isValuePermitted(instance.getNumericValue(i)))
                return false;

        return true;
    }
}
