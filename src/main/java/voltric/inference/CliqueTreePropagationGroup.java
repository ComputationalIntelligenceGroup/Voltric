package voltric.inference;

import voltric.model.DiscreteBayesNet;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by fernando on 4/04/17.
 */
public class CliqueTreePropagationGroup {

    private final BlockingQueue<CliqueTreePropagation> queue;
    public final DiscreteBayesNet model;
    public final int capacity;

    public static CliqueTreePropagationGroup constructFromTemplate(
            CliqueTreePropagation template, DiscreteBayesNet model, int capacity) {
        CliqueTreePropagationGroup group =
                new CliqueTreePropagationGroup(model, capacity);

        while (group.queue.size() < capacity) {
            CliqueTreePropagation ctp = template.clone();
            ctp.setBayesNet(model);
            group.queue.add(ctp);
        }

        return group;
    }

    public static CliqueTreePropagationGroup constructFromModel(DiscreteBayesNet model,
                                                                int capacity) {
        return new CliqueTreePropagationGroup(construct(model), capacity);
    }

    private CliqueTreePropagationGroup(DiscreteBayesNet model, int capacity) {
        this.capacity = capacity;
        this.model = model;
        queue = new ArrayBlockingQueue<CliqueTreePropagation>(capacity);
    }

    public CliqueTreePropagationGroup(CliqueTreePropagation ctp, int capacity) {
        this(ctp.getBayesNet(), capacity);

        queue.add(ctp);

        while (queue.size() < capacity)
            queue.add(construct(model));
    }

    private static CliqueTreePropagation construct(DiscreteBayesNet model) {
        return new CliqueTreePropagation(model);
    }

    /**
     * It constructs new clique tree propagation if necessary, otherwise reuses
     * the ones in reserve.
     *
     * @return
     */
    public CliqueTreePropagation take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Puts back a clique tree propagation in reserve after use.
     *
     * @param ctp
     */
    public void put(CliqueTreePropagation ctp) {
        try {
            queue.put(ctp);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
