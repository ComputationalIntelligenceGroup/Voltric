package voltric.util.information.mi;

import voltric.potential.Function;
import voltric.util.Utils;
import voltric.util.information.entropy.Entropy;
import voltric.variables.DiscreteVariable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Esto es mas eficiente que FrequencyCountered porque las probabilidades solo se calculan en el momento
 * de crear el potencial, a diferencia del otro caso, donde se calculan en cada llamada de computePairwise()
 */
// TODO: Revisar todos los metodos que permitan variables repetidas en X e Y
class JointDistributionMI {

    // Calculada a partir de las marginales de la function
    public static double computePairwise(Function dist){

        // ensure the distribution contains a pair of variables
        if(dist.getDimension() != 2)
            throw new IllegalArgumentException("The argument function's dimension must be 2");
        // ensure the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // cells of joint and two marginal distributions
        double[] cells = dist.getCells();
        double[] cells1 = dist.sumOut(dist.getVariables().get(1)).getCells();
        double[] cells2 = dist.sumOut(dist.getVariables().get(0)).getCells();

        // I(X;Y) = sum_X,Y P(X,Y) log P(X,Y)/P(X)P(Y)
        double mi = 0.0;
        int index = 0;
        for (double cell1 : cells1) {
            for (double cell2 : cells2) {
                double cell = cells[index++];

                // if P(x, y) = 0, skip this term
                if (cell != 0.0) {
                    mi += cell * Math.log(cell / (cell1 * cell2));
                }
            }
        }

        return mi;
    }

    // Como permite que X contenga variables de Y, existe un caso especial incluido (no sportado por los calculos normales)
    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, Function dist){

        Set<DiscreteVariable> nonRepeatedSetOfVariables = new HashSet<>();
        nonRepeatedSetOfVariables.addAll(x);
        nonRepeatedSetOfVariables.addAll(y);

        // Ensure that the distribution contains all the variables that compose X & Y
        if(dist.getDimension() != (nonRepeatedSetOfVariables.size()))
            throw new IllegalArgumentException("The argument function's dimension must be equal to the size of the  set of non-repeated variables (" + nonRepeatedSetOfVariables.size() + ")");
        // Ensure that the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        /** Given that X & Y may have crossed variables, a filter needs to be made where the variables not present in them are summed out */
        List<DiscreteVariable> notPresentInY = dist.getVariables().stream().filter(var -> !y.contains(var)).collect(Collectors.toList());
        List<DiscreteVariable> notPresentInX = dist.getVariables().stream().filter(var -> !x.contains(var)).collect(Collectors.toList());

        Function px = dist.sumOut(notPresentInX);
        Function py = dist.sumOut(notPresentInY);

        /** Special cases (X contains all of Y or viceversa) */
        // I(X;Y) = H(X,Y) = H(X) OR I(X;Y) = H(X,Y) = H(Y)
        if(x.containsAll(y))
            return Entropy.compute(py);
        if(y.containsAll(x))
            return Entropy.compute(px);

        /** Normal case*/
        // TODO: Hastq que entienda como funciona por dentro Function, es mejor que utilice la formula de la MI utilizando entropias, ya que estoy mas seguro con ella
        // I(X;Y) = H(X,Y) - H(X|Y) - H(Y|X)

        double Hxy = Entropy.compute(dist);
        double HcondXY = Entropy.computeConditional(dist, y);
        double HcondYX = Entropy.computeConditional(dist, x);

        return Hxy - HcondXY - HcondYX;
    }

    // TODO: Revisar que signifca a nivel teorico y añadir  documentacion
    // TODO: Es muy importante que las dos primeras variables del potencial "dist" sean X e Y
    // Para evitar que se requiera un orden especifico en la funcion (ya que no sabemos de donde viene) he cambiado el codigo
    public static double computeConditional(Function dist, DiscreteVariable condVar){
        // ensure the distribution contains three variables
        if(dist.getDimension() != 3)
            throw new IllegalArgumentException("the argument function's dimension must be 3");
        // ensure the distribution contains the conditional variable
        if(!dist.contains(condVar))
            throw new IllegalArgumentException("The argument distribution does not contain the conditional variable");
        // ensure the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // First the condVar is filtered to select x & y
        List<DiscreteVariable> xy = dist.getVariables().stream()
                .filter(var -> !var.equals(condVar))
                .collect(Collectors.toList());

        DiscreteVariable x = xy.get(0);
        DiscreteVariable y = xy.get(1);

        Function pxz = dist.sumOut(y);
        Function pyz = dist.sumOut(x);
        Function pz = pxz.sumOut(x);

        // I(X;Y|Z) = sum_X,Y,Z P(X,Y,Z) log P(X,Y|Z)/P(X|Z)P(Y|Z) = sum_X,Y,Z P(X,Y,Z) log P(X,Y,Z)P(Z)/P(X,Z)P(Y,Z)

        // cells of joint, numerator, and denominator
        double[] distCells = dist.getCells();
        double[] numCells = dist.times(pz).getCells();
        double[] denomCells = pxz.times(pyz).getCells();
        int size = dist.getDomainSize();

        double cmi = 0.0;
        for (int i = 0; i < size; i++) {
            // skip if either denominator or numerator equals to zero
            if (denomCells[i] > 0.0 && numCells[i] > 0.0) {
                cmi += distCells[i] * Math.log(numCells[i] / denomCells[i]);
            }
        }

        return cmi;
    }

    // TODO: Habria que comprobar que está bien
    public static double computeConditional(Function dist, List<DiscreteVariable> condVars){
        // ensure the distribution contains (2 + condVars.size) variables
        if(dist.getDimension() != 2 + condVars.size())
            throw new IllegalArgumentException("the argument function's dimension must be " + (2 + condVars.size() + " (X,Y and the conditioning variables)"));
        // ensure the distribution contains the conditioning variables
        if(!dist.containsAll(condVars))
            throw new IllegalArgumentException("The argument distribution does not contain all the conditioning variables");
        // ensure the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // I(X;Y|Z) = sum_X,Y,Z P(X,Y,Z) log P(X,Y|Z)/P(X|Z)P(Y|Z) = sum_X,Y,Z P(X,Y,Z) log P(X,Y,Z)P(Z)/P(X,Z)P(Y,Z)

        // First the condVar is filtered to select x & y
        List<DiscreteVariable> xy = dist.getVariables().stream()
                .filter(var -> !condVars.contains(var))
                .collect(Collectors.toList());

        DiscreteVariable x = xy.get(0);
        DiscreteVariable y = xy.get(1);

        // Z represents all the conditioning variables
        Function pxz = dist.sumOut(y);
        Function pyz = dist.sumOut(x);
        Function pz = pxz.sumOut(x);

        // I(X;Y|Z) = sum_X,Y,Z P(X,Y,Z) log P(X,Y|Z)/P(X|Z)P(Y|Z) = sum_X,Y,Z P(X,Y,Z) log P(X,Y,Z)P(Z)/P(X,Z)P(Y,Z)

        // cells of joint, numerator, and denominator
        double[] distCells = dist.getCells();
        double[] numCells = dist.times(pz).getCells();
        double[] denomCells = pxz.times(pyz).getCells();
        int size = dist.getDomainSize();

        double cmi = 0.0;
        for (int i = 0; i < size; i++) {
            // skip if either denominator or numerator equals to zero
            if (denomCells[i] > 0.0 && numCells[i] > 0.0) {
                cmi += distCells[i] * Math.log(numCells[i] / denomCells[i]);
            }
        }

        return cmi;
    }

    // TODO: Revisar teoria (https://en.wikipedia.org/wiki/Pointwise_mutual_information)
    // TODO: Falta un argumento: los puntos X=x e Y=y que vamos a tomar para calcularla ???
    public static double computePointwise(Function dist){
        // ensure the distribution contains a pair of variables
        if(dist.getDimension() != 2)
            throw new IllegalArgumentException("the argument function's dimension must be 2");
        // ensure the distribution probabilities sum up to one
        if(!Utils.eqDouble(dist.sumUp(), 1.0, 0.0001))
            throw new IllegalArgumentException("The argument distribution's probabilities must sum up to 1.0");

        // cells of joint and two marginal distributions
        double[] cells = dist.getCells();
        double[] cells1 = dist.sumOut(dist.getVariables().get(1)).getCells();
        double[] cells2 = dist.sumOut(dist.getVariables().get(0)).getCells();

        // PMI(X;Y) = P(X=1,Y=1) log P(X=1,Y=1)/P(X=1)P(Y=1)
        double pmi = Math.log(cells[3]/(cells1[1]*cells2[1]));

        return pmi;
    }
}
