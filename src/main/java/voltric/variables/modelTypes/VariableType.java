package voltric.variables.modelTypes;

/**
 * This distinction is very useful for parameter learning algorithms like the {@code Expectation-Maximization}
 * and many structure learning algorithms.
 */
public enum VariableType {
    MANIFEST_VARIABLE,
    LATENT_VARIABLE
}
