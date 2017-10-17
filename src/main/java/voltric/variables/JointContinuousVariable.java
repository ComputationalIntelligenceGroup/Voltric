package voltric.variables;

import voltric.variables.modelTypes.VariableType;
import voltric.variables.util.VariableUtil;
import voltric.variables.visitor.VariableVisitor;

import java.util.*;

/**
 * A joint continuous variable, which can possibly be a multidimensional variable. It stores a collection of at least one
 * <code>ContinuousVariable</code>.
 *
 * @author kmpoon
 * @author ferjorosa
 * @see ContinuousVariable
 */
//TODO: El problema de la combinación de variables latents con variables manifest. Queda por resolver con PyC.
public class JointContinuousVariable extends AbstractContinuousVariable {

    /**  Stores the collection of <code>ContinuousVariable</code> that are joint together. */
    private SortedSet<ContinuousVariable> variables;

    /**
     * Creates a <code>JointContinuousVariable</code> from a collection of <code>ContinuousVariable</code>.
     *
     * @param variables the collection of singular continuous variables used to create it.
     */
    public JointContinuousVariable(Collection<ContinuousVariable> variables) {
        super(JointContinuousVariable.constructName(variables), JointContinuousVariable.defineVariableType(variables));
        this.variables = new TreeSet<>(variables);
    }

    /**
     * Constructs a joint continuous variable from a singular continuous variable. It will call the public constructor
     * that expects a Collection of <code>ContinuousVariable</code> and create a new TreeSet from it.
     *
     * @param variable the singular continuous variable used to create it.
     */
    public JointContinuousVariable(ContinuousVariable variable) {
        this(Collections.singletonList(variable));
    }

    /**
     * Constructs a <code>JointContinuousVariable</code> by combining two of them. It will call the public constructor
     * that expects a Collection of <code>ContinuousVariable</code> and create a new TreeSet from it.
     *
     * @param variable1 the first variable used to combine.
     * @param variable2 the second variable used to combine.
     */
    public JointContinuousVariable(JointContinuousVariable variable1, JointContinuousVariable variable2) {
        this(JointContinuousVariable.combineJointContinuousVariables(variable1, variable2));
    }

    /**
     * This constructor is only used internally to avoid copying needlessly a collection that has been created internally.
     * For example in the <code>attach</code> method.
     *
     * @param variables
     */
    private JointContinuousVariable(TreeSet<ContinuousVariable> variables){
        super(JointContinuousVariable.constructName(variables), JointContinuousVariable.defineVariableType(variables));
        this.variables = variables;
    }

    /**
     * Creates a new <code>JointContinuousVariable</code> by attaching a new collection of <code>ContinuousVariable</code>.
     *
     * @param variables the collection of variables being attached.
     * @return the new joint continuous variable.
     */
    public JointContinuousVariable attach(Collection<ContinuousVariable> variables) {
        TreeSet<ContinuousVariable> newVariables = new TreeSet<>();
        newVariables.addAll(this.getVariables());
        newVariables.addAll(variables);
        return new JointContinuousVariable(newVariables);
    }

    /**
     * Creates a new <code>JointContinuousVariable</code> by attaching a new collection of <code>ContinuousVariable</code>.
     *
     * @param variable the joint continuous variable being attached.
     * @return the new joint continuous variable.
     */
    public JointContinuousVariable attach(JointContinuousVariable variable){
        return new JointContinuousVariable(this, variable);
    }

    /**
     * Returns an unmodifiable collection copy of  <code>ContinuousVariable</code> that is stored in the joint
     * variable.
     *
     * <p> Remember that even though the Set is immutable, the variables contained in it are not. </p>
     *
     * @return getVariables forming this joint variable
     */
    public Set<ContinuousVariable> getVariables() {
        return Collections.unmodifiableSortedSet(variables);
    }

    /** {@inheritDoc} */
    public <T> T accept(VariableVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * Returns true if <code>object</code> is a <code>JointContinuousVariable</code> instance where its index, name
     * and collection of internal <code>ContinuousVariable</code> coincide.
     *
     * @param object the object to test equality against.
     * @return true if object equals this.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if(object instanceof JointContinuousVariable == false)
            return false;

        JointContinuousVariable jointVariable = (JointContinuousVariable) object;
        return this.name.equals(jointVariable.getName())
                && this.index == jointVariable.index
                && this.variables.equals(jointVariable.variables);
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + variables.hashCode();
        return result;
    }

    /**
     * Generates a name for a new <code>JointContinuousVariable</code>, which is a comma-separated list of the names
     * of the <code>ContinuousVariable</code> that it will contain.
     *
     * @return a new name for this joint variable.
     */
    private static String constructName(Collection<ContinuousVariable> variables) {
        return VariableUtil.getJointName(variables, ", ");
    }

    /**
     * Combines the singular continuous variables contained in two <code>JointContinuousVariable</code>, generating a new
     * TreeSet from it.
     *
     * @param var1 the first joint variable.
     * @param var2 the second joint variable.
     * @return a new TreeSet containing the combination of both variables.
     */
    private static TreeSet<ContinuousVariable> combineJointContinuousVariables(JointContinuousVariable var1,
                                                                               JointContinuousVariable var2){
        TreeSet<ContinuousVariable> variables = new TreeSet<>();
        variables.addAll(var1.getVariables());
        variables.addAll(var2.getVariables());
        return variables;
    }

    /**
     * The variable's type (latent or manifest) is defined the following way: if there are only manifest variables, the
     * joint variable is manifest too; but if there is at least one latent variable, the joint variable will also
     * be latent.
     *
     * @param variables the collection of singular variables that will form the new joint variable
     * @return the joint variable's type.
     */
    private static VariableType defineVariableType(Collection<ContinuousVariable> variables){
        for(Variable var: variables)
            if(var.getType() == VariableType.LATENT_VARIABLE)
                return VariableType.LATENT_VARIABLE;

        return VariableType.MANIFEST_VARIABLE;
    }
}