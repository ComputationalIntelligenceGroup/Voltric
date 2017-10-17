package voltric.learning.parameter.em.initialization;

/**
 * TODO: Explain
 */
public class MultipleRestarts extends EmInitialization {

    /** The number of preSteps to go in order to choose a good starting point. */
    private int nPreSteps;

    /**
     * Default constructor. A {@code MultipleRestarts} instance is created with 1 init iteration and 10 pre-steps.
     */
    public MultipleRestarts(){
        super(1); // numInitIterations
        this.nPreSteps = 10;
    }

    /**
     * Constructs a {@code MultipleRestarts} instance is created with 1 init iteration and an argument number of pre-steps.
     *
     * @param nPreSteps the number of pre-steps.
     */
    public MultipleRestarts(int nPreSteps){
        super(1); // numInitIterations
        this.nPreSteps = nPreSteps;
    }

    /**
     * Constructs a {@code MultipleRestarts} instance is created with an argument number of init iteration and an
     * argument number of pre-steps.
     *
     * @param nPreSteps the number of pre-steps.
     * @param numInitIterations the number of init iterations.
     */
    public MultipleRestarts(int nPreSteps, int numInitIterations){
        super(numInitIterations);
        this.nPreSteps = nPreSteps;
    }

    /**
     * Returns the number of preSteps to go in order to choose a good starting point.
     * @return the number of preSteps to go in order to choose a good starting point.
     */
    public int getnPreSteps() {
        return nPreSteps;
    }
}
