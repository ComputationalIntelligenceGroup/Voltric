package voltric.learning.parameter.em.config;

import voltric.data.DiscreteDataInstance;
import voltric.inference.CliqueTreePropagation;
import voltric.learning.parameter.em.initialization.EmInitialization;
import voltric.learning.parameter.em.util.MessagesForLocalEM;
import voltric.variables.DiscreteVariable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by fernando on 5/04/17.
 */
public class LocalEmConfig extends EmConfig {

    /**
     * A repository of messages. In the implementation, this must be prepared beforehand.
     */
    private Map<DiscreteDataInstance, Set<MessagesForLocalEM>> repository;

    /**
     * We control termination of localEM by number of continued steps.
     */
    protected int nContinuedSteps = 10;

    /**
     * Specify that in M-step, whose Cpt will be updated.
     */
    protected DiscreteVariable[] mutableVars;

    /**
     * A template Ctp. The useful information conveyed is the cliquetree,
     * especially the foucused subtree contained.
     */
    protected CliqueTreePropagation templateCtp;

    public LocalEmConfig(int nRestarts,
                         double threshold,
                         int nMaxSteps,
                         EmInitialization escapeMethod,
                         boolean reuse,
                         HashSet<String> dontUpdateNodes,
                         Map<DiscreteDataInstance, Set<MessagesForLocalEM>> repository,
                         int nContinuedSteps,
                         DiscreteVariable[] mutableVars,
                         CliqueTreePropagation templateCtp) {

        super(nRestarts, threshold, nMaxSteps, escapeMethod, reuse, dontUpdateNodes);

        this.repository = repository;
        this.nContinuedSteps = nContinuedSteps;
        this.mutableVars = mutableVars;
        this.templateCtp = templateCtp;
    }

    public LocalEmConfig(boolean reuse,
                         HashSet<String> dontUpdateNodes,
                         Map<DiscreteDataInstance, Set<MessagesForLocalEM>> repository,
                         int nContinuedSteps,
                         DiscreteVariable[] mutableVars,
                         CliqueTreePropagation templateCtp) {

        super(reuse, dontUpdateNodes);

        this.repository = repository;
        this.nContinuedSteps = nContinuedSteps;
        this.mutableVars = mutableVars;
        this.templateCtp = templateCtp;
    }

    public LocalEmConfig(EmConfig emConfig,
                         Map<DiscreteDataInstance, Set<MessagesForLocalEM>> repository,
                         int nContinuedSteps,
                         DiscreteVariable[] mutableVars,
                         CliqueTreePropagation templateCtp){

        super(emConfig.nRestarts, emConfig.threshold, emConfig.nMaxSteps,
                emConfig.initializationMethod, emConfig.reuse, emConfig.dontUpdateNodes);

        this.repository = repository;
        this.nContinuedSteps = nContinuedSteps;
        this.mutableVars = mutableVars;
        this.templateCtp = templateCtp;
    }

    public Map<DiscreteDataInstance, Set<MessagesForLocalEM>> getRepository() {
        return repository;
    }

    public void setRepository(Map<DiscreteDataInstance, Set<MessagesForLocalEM>> repository) {
        this.repository = repository;
    }

    public int getnContinuedSteps() {
        return nContinuedSteps;
    }

    public void setnContinuedSteps(int nContinuedSteps) {
        this.nContinuedSteps = nContinuedSteps;
    }

    public DiscreteVariable[] getMutableVars() {
        return mutableVars;
    }

    public void setMutableVars(DiscreteVariable[] mutableVars) {
        this.mutableVars = mutableVars;
    }

    public CliqueTreePropagation getTemplateCtp() {
        return templateCtp;
    }

    public void setTemplateCtp(CliqueTreePropagation templateCtp) {
        this.templateCtp = templateCtp;
    }
}
