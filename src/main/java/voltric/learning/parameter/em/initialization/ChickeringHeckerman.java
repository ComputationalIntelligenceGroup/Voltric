package voltric.learning.parameter.em.initialization;

/**
 *
 *
 * TODO: Explain
 */
public class ChickeringHeckerman extends EmInitialization {

    /**
     * Default constructor. A {@code ChickeringHeckerman} instance is created with 1 init iteration.
     */
    public ChickeringHeckerman(){
        super(1);
    }

    /**
     * Main constructor. A {@code ChickeringHeckerman} instance is created with the argument number of init iterations.
     *
     * @param numInitIterations the number of init iterations.
     */
    public ChickeringHeckerman(int numInitIterations){
        super(numInitIterations);
    }

}
