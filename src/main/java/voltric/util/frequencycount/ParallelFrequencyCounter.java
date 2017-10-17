package voltric.util.frequencycount;

import voltric.data.DiscreteData;
import voltric.variables.DiscreteVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by fernando on 15/04/17.
 */
public class ParallelFrequencyCounter extends FrequencyCounter{

    /**
     * Metodo interfaz, es decir, aquel que llama el usuario
     *
     * @param data
     * @param variables
     * @return
     */
    public ArrayList<double[]> compute(DiscreteData data, List<DiscreteVariable> variables) {
        ParallelComputation c =
                new ParallelComputation(data, variables, 0, data.getInstances().size());
        ForkJoinPool pool = new ForkJoinPool();
        // Intuyo que aqui
        pool.invoke(c);
        return c.frequencies;
    }

    @SuppressWarnings("serial")
    private class ParallelComputation extends RecursiveAction {

        private final int start;
        private final int end;
        private DiscreteData data; // Esto seria una val en Scala (final en java solo sirve para las referencias, deberia ser immutable tmb)
        private List<DiscreteVariable> variables; // Esto seria una val en Scala (final en java solo sirve para las referencias, deberia ser immutable tmb)
        private static final int THRESHOLD = 500;
        private ArrayList<double[]> frequencies;

        private ParallelComputation(DiscreteData data, List<DiscreteVariable> variables, int start, int end) {
            this.start = start;
            this.end = end;
            this.data = data;
            this.variables = variables;
        }

        private void computeDirectly() {
            frequencies = computeFrequencies(data, variables, start, end);
        }

        @Override
        protected void compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                computeDirectly();
                return;
            }

            //TODO: Dual core?
            int split = length / 2;
            ParallelComputation c1 =
                    new ParallelComputation(data, variables, start, start + split);
            ParallelComputation c2 =
                    new ParallelComputation(data, variables, start + split, end);
            invokeAll(c1, c2);

            // This is not very efficient for combining the results
            // from subtasks.
            frequencies = c1.frequencies;
            for (int i = 0; i < frequencies.size();i++) {
                for (int j = 0; j < frequencies.get(i).length; j++) {
                    double t1 = frequencies.get(i)[j];
                    double t2 = c2.frequencies.get(i)[j];
                    frequencies.get(i)[j] = t1+t2;
                }
            }
        }
    }
}
