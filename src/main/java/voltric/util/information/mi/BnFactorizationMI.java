package voltric.util.information.mi;

import voltric.data.DiscreteData;
import voltric.data.DiscreteDataInstance;
import voltric.inference.CliqueTreePropagation;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: En un futuro habria que ver cual es la implementacion de la MI mas optima en tiempo de computacion
 */
public class BnFactorizationMI {

    public static double computePairwise(DiscreteVariable x, DiscreteVariable y, CliqueTreePropagation ctp, DiscreteData data){

        List<DiscreteVariable> xList = new ArrayList<>();
        xList.add(x);

        List<DiscreteVariable> yList = new ArrayList<>();
        yList.add(y);

        return computePairwise(xList, yList, ctp, data);
    }

    public static double computePairwise(List<DiscreteVariable> x, List<DiscreteVariable> y, CliqueTreePropagation ctp, DiscreteData data){

        // Projectamos los datos a la dimension XY
        List<DiscreteVariable> xy = new ArrayList<>();
        xy.addAll(x);
        xy.addAll(y);

        DiscreteData projectedData = data.project(xy);

        double mi = 0;

        for(DiscreteDataInstance xyInstance : projectedData.getInstances()){
            ctp.setEvidence(xy, xyInstance.getNumericValues());
            double Pxy = ctp.propagate();

            ctp.setEvidence(x, xyInstance.project(x).getNumericValues());
            double Px = ctp.propagate();

            ctp.setEvidence(y, xyInstance.project(y).getNumericValues());
            double Py = ctp.propagate();

            mi += Pxy * Math.log(Pxy / (Px * Py));
        }

        return mi;
    }

    public static double computeConditional(DiscreteVariable x, DiscreteVariable y, DiscreteVariable condVar, CliqueTreePropagation ctp, DiscreteData data){
        List<DiscreteVariable> xList = new ArrayList<>();
        xList.add(x);

        List<DiscreteVariable> yList = new ArrayList<>();
        yList.add(y);

        List<DiscreteVariable> zList = new ArrayList<>();
        yList.add(condVar);

        return computeConditional(xList, yList, zList, ctp, data);
    }

    public static double computeConditional(DiscreteVariable x, DiscreteVariable y, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData data){
        List<DiscreteVariable> xList = new ArrayList<>();
        xList.add(x);

        List<DiscreteVariable> yList = new ArrayList<>();
        yList.add(y);

        List<DiscreteVariable> zList = condVars;

        return computeConditional(xList, yList, zList, ctp, data);
    }

    public static double computeConditional(List<DiscreteVariable> x, List<DiscreteVariable> y, List<DiscreteVariable> condVars, CliqueTreePropagation ctp, DiscreteData data){

        List<DiscreteVariable> z = condVars; // Redundant variable that helps understanding the method

        List<DiscreteVariable> xz = new ArrayList<>();
        xz.addAll(x);
        xz.addAll(z);

        List<DiscreteVariable> yz = new ArrayList<>();
        yz.addAll(y);
        yz.addAll(z);

        List<DiscreteVariable> xyz = new ArrayList<>();
        xyz.addAll(x);
        xyz.addAll(y);
        xyz.addAll(z);

        DiscreteData xyzData = data.project(xyz);

        double cmi = 0;

        for(DiscreteDataInstance xyzInstance : xyzData.getInstances()){
            ctp.setEvidence(xyz, xyzInstance.getNumericValues());
            double Pxyz = ctp.propagate();

            ctp.setEvidence(xz, xyzInstance.project(xz).getNumericValues());
            double Pxz = ctp.propagate();

            ctp.setEvidence(yz, xyzInstance.project(yz).getNumericValues());
            double Pyz = ctp.propagate();

            ctp.setEvidence(z, xyzInstance.project(z).getNumericValues());
            double Pz = ctp.propagate();

            cmi += Pxyz * Math.log((Pz * Pxyz) / (Pxz * Pyz));
        }

        return cmi;
    }

}
