package voltric.clustering.multiview.attribute;

import voltric.data.DiscreteData;
import voltric.learning.parameter.DiscreteParameterLearning;
import voltric.model.HLCM;
import voltric.util.stattest.discrete.DiscreteStatisticalTest;

import java.util.List;

/**
 * Created by equipo on 20/04/2017.
 */
public interface AttributeGrouping {

    List<HLCM> find(DiscreteData dataSet);

    DiscreteParameterLearning getParameterLearning();

    DiscreteStatisticalTest getStatisticalTest();
}
