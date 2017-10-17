package voltric.util.information.mi;

import voltric.data.DiscreteData;
import voltric.inference.CliqueTreePropagation;
import voltric.potential.Function;
import voltric.util.information.mi.normalization.NormalizationFactor;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Esta clase actua como fachada de los metodos
 *
 * TODO: La normalized conditional information no se ha implementado aun porque requiere revisar la teoria
 * Antes de meterme en la normalized CMI, seria mejor terminar la CMI
 */
public class NMI {

    public static double computePairwise(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        double mi = MI.computePairwise(x, y, dataSet);

        List<DiscreteVariable> xList = new ArrayList<>();
        List<DiscreteVariable> yList = new ArrayList<>();
        xList.add(x);
        yList.add(y);

        return normalizationFactor.normalizeMI(mi, xList, yList , dataSet);
    }

    public static double computePairwiseParallel(DiscreteVariable x, DiscreteVariable y, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        double mi = MI.computePairwiseParallel(x, y, dataSet);

        List<DiscreteVariable> xList = new ArrayList<>();
        List<DiscreteVariable> yList = new ArrayList<>();
        xList.add(x);
        yList.add(y);

        return normalizationFactor.normalizeMI(mi, xList, yList , dataSet);
    }

    public static double computePairwise(DiscreteVariable x, List<DiscreteVariable> y, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        List<DiscreteVariable> oneVariableList = new ArrayList<>();
        oneVariableList.add(x);
        return NMI.computePairwise(oneVariableList, y, dataSet, normalizationFactor);
    }

    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        double mi = MI.computePairwise(x, y, dataSet);

        return normalizationFactor.normalizeMI(mi, x, y , dataSet);
    }

    public static Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwise(List<DiscreteVariable> variables, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        Map<DiscreteVariable, Map<DiscreteVariable, Double>> miMap = MI.computePairwise(variables, dataSet);

        // Each value is normalized
        for(DiscreteVariable firstVar: miMap.keySet())
            for(DiscreteVariable secondVar: miMap.get(firstVar).keySet()) {
                // The "normalizeMI" method needs to receive a List<DiscreteVariable> as argument
                List<DiscreteVariable> firstVarList = new ArrayList<>();
                List<DiscreteVariable> secondVarList = new ArrayList<>();
                firstVarList.add(firstVar);
                secondVarList.add(secondVar);

                miMap.get(firstVar).put(secondVar, normalizationFactor.normalizeMI(miMap.get(firstVar).get(secondVar), firstVarList, secondVarList, dataSet));
            }

        // the normalized map is returned
        return miMap;
    }

    public static Map<DiscreteVariable, Map<DiscreteVariable, Double>> computePairwiseParallel(List<DiscreteVariable> variables, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        Map<DiscreteVariable, Map<DiscreteVariable, Double>> miMap = MI.computePairwiseParallel(variables, dataSet);

        // Each value is normalized
        for(DiscreteVariable firstVar: miMap.keySet())
            for(DiscreteVariable secondVar: miMap.get(firstVar).keySet()) {
                // The "normalizeMI" method needs to receive a List<DiscreteVariable> as argument
                List<DiscreteVariable> firstVarList = new ArrayList<>();
                List<DiscreteVariable> secondVarList = new ArrayList<>();
                firstVarList.add(firstVar);
                secondVarList.add(secondVar);

                miMap.get(firstVar).put(secondVar, normalizationFactor.normalizeMI(miMap.get(firstVar).get(secondVar), firstVarList, secondVarList, dataSet));
            }

        // the normalized map is returned
        return miMap;
    }

    public static double computePairwise(Function dist, NormalizationFactor normalizationFactor){
        double mi = MI.computePairwise(dist);
        return normalizationFactor.normalizeMI(mi, dist);
    }

    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, Function dist, NormalizationFactor normalizationFactor){
        double mi = MI.computePairwise(x, y, dist);
        return normalizationFactor.normalizeMI(mi, x, y, dist);
    }

    /** La unica manera actual de hacer estos calculos es mediante la JPD, ya que no se como calcularlo de forma frequentista para el caso de N variables condicionantes */
    public static double computeConditional(Function dist, DiscreteVariable condVar, NormalizationFactor normalizationFactor){
        double cmi = MI.computeConditional(dist, condVar);
        return normalizationFactor.normalizeCMI(cmi, dist, condVar);
    }

    public static double computeConditional(Function dist, List<DiscreteVariable> condVars, NormalizationFactor normalizationFactor){
        double cmi = MI.computeConditional(dist, condVars);
        return normalizationFactor.normalizeCMI(cmi, dist, condVars);
    }

    /** BN FACTORIZATION */

    public static double computePairwise(DiscreteVariable x, DiscreteVariable y, CliqueTreePropagation ctp, DiscreteData data){
        double mi = BnFactorizationMI.computePairwise(x, y, ctp, data);
    }

    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, CliqueTreePropagation ctp, DiscreteData data){
        double mi = BnFactorizationMI.computePairwise(x, y, ctp, data);
    }

    public static double computeConditional(DiscreteVariable x, DiscreteVariable y, DiscreteVariable condVar, CliqueTreePropagation ctp, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        double cmi = MI.computeConditional(x, y, condVar, ctp, dataSet);
    }

    public static double computeConditional(DiscreteVariable x, DiscreteVariable y, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        double cmi = BnFactorizationMI.computeConditional(x, y, condVars, ctp, dataSet);
    }

    public static double computeConditional(List<DiscreteVariable> x, List<DiscreteVariable> y, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData dataSet, NormalizationFactor normalizationFactor){
        double cmi = BnFactorizationMI.computeConditional(x, y, condVars, ctp, dataSet);
    }

}
