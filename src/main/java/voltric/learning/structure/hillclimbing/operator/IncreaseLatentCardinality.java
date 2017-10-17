package voltric.learning.structure.hillclimbing.operator;

import voltric.data.DiscreteData;
import voltric.learning.LearningResult;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.model.DiscreteBayesNet;
import voltric.variables.DiscreteVariable;
import voltric.variables.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Incrementa en 1 la cardinalidad de todas las variables latentes del modelo y devuelve aquel que mejora mas el score.
 */
public class IncreaseLatentCardinality implements HcOperator{

    /** The set of nodes that need to be avoided in the structure search process. */
    private List<Variable> blackList;

    /** The maximum allowed cardinality value. */
    private int maxCardinality;

    /**
     * Main constructor.
     *
     * @param blackList The set of nodes that need to be avoided in the structure search process.
     * @param maxCardinality The maximum allowed cardinality value.
     */
    public IncreaseLatentCardinality(List<Variable> blackList, int maxCardinality){
        this.blackList = blackList;
        this.maxCardinality = maxCardinality;
    }

    /**
     * Alternative constructor without the black-list.
     *
     * @param maxCardinality The maximum allowed cardinality value.
     */
    public IncreaseLatentCardinality(int maxCardinality){
        this(new ArrayList<>(), maxCardinality);
    }

    /** {@inheritDoc} */
    @Override
    public LearningResult<DiscreteBayesNet> apply(DiscreteBayesNet seedNet, DiscreteData data, DiscreteParameterLearning parameterLearning) {

        // The BN is copied to avoid modifying current object.
        DiscreteBayesNet clonedNet = seedNet.clone();

        // The BN latent nodes are filtered using the blacklist
        List<DiscreteVariable> whiteList = clonedNet.getLatentVariables().stream()
                .filter(x -> !this.blackList.contains(x))
                .collect(Collectors.toList());

        double bestScore = -Double.MAX_VALUE; // Log-likelihood related scores are negative
        DiscreteVariable bestLatentVar = null;

        // Iteration through all the allowed BN nodes
        for(DiscreteVariable latentVar : whiteList) {

            // The cardinality of the LV must be lesser than the established maximum
            if(latentVar.getCardinality() < this.maxCardinality) {

                clonedNet = clonedNet.increaseCardinality(latentVar, 1);

                // After the LV has increased its cardinality, the resulting model is learned. If its score is improved, the LV is stored
                double newScore = parameterLearning.learnModel(clonedNet, data).getScoreValue();
                if (newScore > bestScore) {
                    bestLatentVar = latentVar;
                    bestScore = newScore;
                }

                // The cardinality is reversed for the next iteration to have the initial BN.
                // Given that we have previously increased the LV's cardinality, it is a new LV, so we have to access it by its name.
                clonedNet = clonedNet.decreaseCardinality(clonedNet.getLatentVariable(latentVar.getName()), 1);
            }
        }

        // If the model has been modified
        if(bestLatentVar != null)
            clonedNet = clonedNet.increaseCardinality(clonedNet.getLatentVariable(bestLatentVar.getName()), 1);

        return new LearningResult<>(clonedNet, bestScore, parameterLearning.getScoreType());
    }
}
