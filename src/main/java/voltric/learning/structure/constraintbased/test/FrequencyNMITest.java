package voltric.learning.structure.constraintbased.test;

import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import voltric.data.DiscreteData;
import voltric.potential.Function;
import voltric.util.empiricaldist.StatelessEmpDistComputer;
import voltric.util.information.mi.NMI;
import voltric.util.information.mi.normalization.NormalizationFactor;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Requiere optimizacion, ya que vamos a llamar a FreqNMI, pero podriamos almacenar las probabilidades marginales para
 * reutilizarlas en varios tests.
 */
public class FrequencyNMITest {

    private NormalizationFactor nmiNormalizationFactor;

    public FrequencyNMITest(NormalizationFactor nmiNormalizationFactor){
        this.nmiNormalizationFactor = nmiNormalizationFactor;
    }

    public double test(DiscreteVariable a, DiscreteVariable b, List<DiscreteVariable> conditionalVars, DiscreteData data){

        // First we need to generate the JPD composed of a, b & conditionalVars
        // Given that all of them are data attributes, its is easier (no latentPosts)
        List<DiscreteVariable> allVars = new ArrayList<>();
        allVars.add(a);
        allVars.add(b);
        allVars.addAll(conditionalVars);

        Function jointDist = StatelessEmpDistComputer.computeEmpDist(allVars, data);

        // First compute the NCMI
        double ncmi = NMI.computeConditional(jointDist, conditionalVars,this.nmiNormalizationFactor);

        // Now, we do a hypothesis test using the chi-square distribution, given that 2NMI -> chi-square dist with parameter k = (|a| - 1) * (|b| - 1)
        double degreesOfFreedom = (a.getCardinality() - 1) * (b.getCardinality() - 1);
        ChiSquaredDistribution chi2dist = new ChiSquaredDistribution(degreesOfFreedom);

        return chi2dist.density(2 * ncmi);
    }
}
