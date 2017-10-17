package voltric.learning.parameter.em.config;

import voltric.learning.parameter.em.initialization.ChickeringHeckerman;
import voltric.learning.parameter.em.initialization.EmInitialization;

import java.util.HashSet;

/**
 * Created by fernando on 3/04/17.
 */
public class EmConfig {

    /** The number of restarts */
    protected int nRestarts;

    /** The threshold to control the algorithm's convergence */
    protected double threshold;

    /** The maximum number of EM steps to control its convergence */
    protected int nMaxSteps;

    /** The escape method is used to choose a good starting point for the EM algorithm */
    protected EmInitialization initializationMethod;

    /** The flag indicates whether we reuse the parameters of the input BN as a candidate starting point. */
    protected boolean reuse = true;

    /** The collection of nodes that shouldnt be updated by the EM algorithm */
    protected HashSet<String> dontUpdateNodes;

    public EmConfig(){
        this.nRestarts = 64;
        this.threshold = 1e-4;
        this.nMaxSteps = 500;
        this.initializationMethod = new ChickeringHeckerman();
        this.reuse = true;
        this.dontUpdateNodes = new HashSet<>();
    }

    public EmConfig(int nRestarts, double threshold, int nMaxSteps, EmInitialization initializationMethod,
                    boolean reuse, HashSet<String> dontUpdateNodes){

        this.nRestarts = nRestarts;
        this.threshold = threshold;
        this.nMaxSteps = nMaxSteps;
        this.initializationMethod = initializationMethod;
        this.reuse = reuse;
        this.dontUpdateNodes = dontUpdateNodes;
    }

    public EmConfig(boolean reuse, HashSet<String> dontUpdateNodes){
        this();
        this.reuse = reuse;
        this.dontUpdateNodes = dontUpdateNodes;
    }

    public int getnRestarts() {
        return nRestarts;
    }

    public void setnRestarts(int nRestarts) {
        this.nRestarts = nRestarts;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getnMaxSteps() {
        return nMaxSteps;
    }

    public void setnMaxSteps(int nMaxSteps) {
        this.nMaxSteps = nMaxSteps;
    }

    public EmInitialization getInitializationMethod() {
        return initializationMethod;
    }

    public void setInitializationMethod(EmInitialization initializationMethod) {
        this.initializationMethod = initializationMethod;
    }

    public boolean isReuse() {
        return reuse;
    }

    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }

    public HashSet<String> getDontUpdateNodes() {
        return dontUpdateNodes;
    }

    public void setDontUpdateNodes(HashSet<String> dontUpdateNodes) {
        this.dontUpdateNodes = dontUpdateNodes;
    }
}
