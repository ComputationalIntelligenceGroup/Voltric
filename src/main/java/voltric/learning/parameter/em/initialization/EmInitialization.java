package voltric.learning.parameter.em.initialization;

/**
 * This abstract class groups the s initialization strategies common code. The EM's initialization techniques allow it
 * to choose a good starting point for its subsequent steps.
 */
public abstract class EmInitialization {

    /** When using this initialization method, a random of restarts are executed. Then before eliminating the bad ones,
     * 'numInitIterations' EM steps are executed (i.e., 3 EM steps) for all the random restarts. */
    protected int numInitIterations;

    /**
     * Main constructor. An instance is created with the passed number of init iterations.
     *
     * @param numInitIterations the number of EM steps executed for each random restart, before choosing the best ones.
     */
    public EmInitialization(int numInitIterations){
        this.numInitIterations = numInitIterations;
    }

    /**
     * Returns the number of EM steps executed for each random restart, before choosing the best ones.
     *
     * @return the number of EM steps executed for each random restart, before choosing the best ones.
     */
    public int getNumInitIterations() {
        return numInitIterations;
    }
}
