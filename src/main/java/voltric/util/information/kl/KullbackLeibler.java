package voltric.util.information.kl;

import voltric.potential.Function;

/**
 * Created by fernando on 15/04/17.
 */
public class KullbackLeibler {

    /**
     * Returns the KL divergence D(P||Q) between the distributions P and Q. The
     * convention we use: (1) 0 * log(0) = 0; (2) x * log(0) = 0 (!!!)
     *
     * @param p distribution P.
     * @param q distribution Q.
     * @return the KL divergence D(P||Q).
     */
    public static double compute(Function p, Function q) {
        // ensure two functions over same domain
        if(!p.getVariables().equals(q.getVariables()))
            throw new IllegalArgumentException("The set of variables from both function must coincide");

        double kl = 0.0;
        double[] pCells = p.getCells();
        double[] qCells = q.getCells();
        for (int i = 0; i < pCells.length; i++) {
            // skip cells where either P or Q instantiate to 0
            if (pCells[i] != 0.0 && qCells[i] != 0.0) {
                kl += pCells[i] * Math.log(pCells[i] / qCells[i]);
            }
        }

        return kl;
    }
}
