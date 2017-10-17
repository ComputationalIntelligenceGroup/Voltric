package voltric.clustering.multiview.attribute.stopcondition;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by equipo on 20/04/2017.
 */
public class UnidimensionalityTest implements StopCondition{

    private double udThreshold;

    public UnidimensionalityTest(double udThreshold){
        this.udThreshold = udThreshold;
    }

    public boolean isTrue(){
        return true;
    }

    public boolean isFalse(){
        return false;
    }

    public boolean test(){
        throw new NotImplementedException();
    }
}
