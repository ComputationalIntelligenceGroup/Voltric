package voltric.learning.parameter.mle;

import voltric.data.DiscreteData;
import voltric.graph.DirectedNode;
import voltric.model.DiscreteBayesNet;
import voltric.model.DiscreteBeliefNode;
import voltric.potential.Function;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Maximum Likelihood Estimation (StaticMLE) is method used to estimate the parameters of a statisical model given a collection
 * of data observations. For a fixed set of data and underlying statistical model, the method of maximum likelihood
 * selects the set of values of the model parameters that maximizes the likelihood function. Intuitively, this maximizes
 * the "agreement" of the selected model with the observed data.
 *
 * Unlike other methods like the Expectation-Maximization (EM), its purpose resides in learning models without latent nodes.
 */
public class StaticMLE {

    /**
     * Optimizes parameters in the specified BN with respect to the specified data set. The argument BN will be modified directly.
     *
     * @param bayesNet BN whose parameters are going to be learned.
     * @param dataSet data set to be used.
     */
    public static void computeMle(DiscreteBayesNet bayesNet, DiscreteData dataSet) {

        if (bayesNet.getLatentNodes().size() > 0)
            throw new IllegalArgumentException("Models with latent nodes are not accepted because they cannot be estimated using StaticMLE");

        if(!dataSet.getVariables().containsAll(bayesNet.getVariables()))
            throw new IllegalArgumentException("The Data set must contain all the variables present in the Bayes net");

        for (DiscreteBeliefNode node : bayesNet.getNodes()) {

            // retrieve variables in family
            ArrayList<DiscreteVariable> family = new ArrayList<>();
            family.add(node.getVariable());

            for (DirectedNode parent : node.getParents()) {
                family.add(((DiscreteBeliefNode) parent).getVariable());
            }

            // derives CPT from sufficient statistics
            Function cpt = Function.createFunction(dataSet.project(family));
            cpt.normalize(node.getVariable());

            // sets CPT
            node.setCpt(cpt);
        }
    }

    /**
     * Optimizes parameters in the specified BN with respect to the specified data set. A uniform prior can be incorporated
     * by setting the pseudo count. The argument BN will be modified directly.
     *
     * @param bayesNet BN whose parameters are going to be learned.
     * @param dataSet data set to be used.
     * @param alpha smoothing factor to incorporate.
     */
    public static void computeMle(DiscreteBayesNet bayesNet, DiscreteData dataSet, double alpha) {

        if (bayesNet.getLatentNodes().size() > 0)
            throw new IllegalArgumentException("Models with latent nodes are not accepted because they cannot be estimated using StaticMLE");

        if(!dataSet.getVariables().containsAll(bayesNet.getVariables()))
            throw new IllegalArgumentException("The Data set must contain all the variables present in the Bayes net");

        for (DiscreteBeliefNode node : bayesNet.getNodes()) {

            // retrieve variables in family
            ArrayList<DiscreteVariable> family = new ArrayList<>();
            family.add(node.getVariable());

            for (DirectedNode parent : node.getParents()) {
                family.add(((DiscreteBeliefNode) parent).getVariable());
            }

            // derives CPT from sufficient statistics
            Function cpt = Function.createFunction(dataSet.project(family));

            // incorporate prior
            double[] cells = cpt.getCells();
            for (int i = 0; i < cells.length; i++) {
                cells[i] += alpha;
            }

            cpt.normalize(node.getVariable());

            // sets CPT
            node.setCpt(cpt);
        }
    }

    /**
     * Optimizes parameters in the specified BN with respect to the specified data set. A marginal prior can be
     * incorporated by setting the pseudo-count. The argument BN will be modified directly.
     *
     * @param bayesNet BN whose parameters are going to be learned.
     * @param dataSet data set to be used.
     * @param pseudoCount  marginal prior to incorporate.
     */
    public static void computeMleMargPrior(DiscreteBayesNet bayesNet, DiscreteData dataSet, double pseudoCount) {

        if (bayesNet.getLatentNodes().size() > 0)
            throw new IllegalArgumentException("Models with latent nodes are not accepted because they cannot be estimated using StaticMLE");

        if(!dataSet.getVariables().containsAll(bayesNet.getVariables()))
            throw new IllegalArgumentException("The Data set must contain all the variables present in the Bayes net");

        for (DiscreteBeliefNode node : bayesNet.getNodes()) {

            // retrieve variables in family
            ArrayList<DiscreteVariable> family = new ArrayList<>();
            family.add(node.getVariable());

            for (DirectedNode parent : node.getParents()) {
                family.add(((DiscreteBeliefNode) parent).getVariable());
            }

            // sufficient statistics Nijk
            Function cpt = Function.createFunction(dataSet.project(family));

            // use marginal distribution with the given pseudo count as prior
            ArrayList<DiscreteVariable> var = new ArrayList<>(1);
            var.add(node.getVariable());

            // sufficient statistics Nik
            Function marg = Function.createFunction(dataSet.project(var));

            // scaled by N0 / N
            Function prior = Function.createFunction(family);
            Arrays.fill(prior.getCells(), pseudoCount
                    / dataSet.getTotalWeight());
            prior = prior.times(marg);

            // incorporate prior and normalizeMI:
            // (Nijk + Nik * N0 / N) / (Nij + N0)
            cpt.plus(prior);
            cpt.normalize(node.getVariable());

            // sets CPT
            node.setCpt(cpt);
        }
    }
}
