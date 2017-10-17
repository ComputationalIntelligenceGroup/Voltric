package voltric.util.execution;

import voltric.learning.score.ScoreType;
import voltric.model.AbstractBayesNet;
import voltric.util.Utils;

import java.util.UUID;

/**
 * TODO: In the future, it could be applied to several ML techniques, like classification or clustering.
 *
 */
public class ExecutionResult<M extends AbstractBayesNet> {

    /** The resulting model of the learning algorithm's execution. */
    private M model;

    /** The model's score. */
    double score;

    /** The score type. */
    ScoreType scoreType;

    /** The ExecutionResult's uniqueID. */
    private UUID uniqueID;

    /** Index of the Execution result. Used when there is an ongoing execution sequence, like a stream. */
    private int index;

    /** Number of nanoseconds at the start of the execution. */
    private double nanoStart;

    /** Number of nanoseconds at the end of the execution. */
    private double nanoFinish;

    public ExecutionResult(M model, double score, ScoreType scoreType, int index, double nanoStart, double nanoFinish){
        this.model = model;
        this.score = score;
        this.scoreType = scoreType;
        this.uniqueID = UUID.randomUUID();
        this.index = index;
        this.nanoStart = nanoStart;
        this.nanoFinish = nanoFinish;
    }


    /**
     * Returns the resulting model of the learning algorithm's execution.
     *
     * @return the resulting model of the learning algorithm's execution.
     */
    public M getModel() {
        return model;
    }

    /**
     * Returns its uniqueID, in the form of an UUID.
     *
     * @return its uniqueID, in the form of an UUID.
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    /**
     * Returns the index of the result when generated in sequence.
     *
     * @return the index of the result when generated in sequence.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the number of nanoseconds at the start of the execution.
     *
     * @return the number of nanoseconds at the start of the execution.
     */
    public double getNanoStart() {
        return nanoStart;
    }

    /**
     * Returns the number of nanoseconds at the end of the execution.
     *
     * @return the number of nanoseconds at the end of the execution.
     */
    public double getNanoFinish() {
        return nanoFinish;
    }

    /**
     * Returns the execution's time in nanoseconds.
     *
     * @return the execution's time in nanoseconds.
     */
    public double getNanoExecutionTime(){
        return this.nanoFinish - this.nanoStart;
    }

    /**
     * Returns the associated score.
     *
     * @return the associated score.
     */
    public double getScore() {
        return score;
    }

    /**
     * Returns the score type.
     *
     * @return the score type.
     */
    public ScoreType getScoreType() {
        return scoreType;
    }

    /**
     * Returns {@code true} if the object is an {@code ExecutionResult} with the same {@code uniqueID}, {@code model},
     * {@code score}, {@code scoreType}, {@code index}, {@code nanoStart} and {@code nanoFinish}.
     *
     * @param object the object to test equality against.
     * @return true if {@code object} equals this.
     */
    public boolean equals(Object object){
        if(this == object)
            return true;

        if(object instanceof ExecutionResult == false)
            return  false;

        ExecutionResult result = (ExecutionResult) object;
        return this.uniqueID.equals(result.uniqueID)
                && this.model.equals(result.model)
                && Utils.eqDouble(this.score, result.score)
                && this.scoreType.equals(result.scoreType)
                && this.index == result.index
                && this.nanoStart == result.nanoStart
                && this.nanoFinish == result.nanoFinish;
    }

    /**
     * Returns the object's hashcode.
     *
     * @return the object's hashcode.
     */
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = model.hashCode();
        result = 31 * result + uniqueID.hashCode();
        result = 31 * result + index;
        temp = Double.doubleToLongBits(nanoStart);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(nanoFinish);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Returns a textual representation of this {@code ExecutionResult}.
     *
     * @return a String description of this {@code ExecutionResult}.
     */
    public String toString(){
        StringBuilder str = new StringBuilder();
        str.append("Execution ID: "+this.getUniqueID()+"\n");
        str.append(this.getModel().toString()+"\n");
        str.append("Score type: " + this.getScoreType()+"\n");
        str.append("Score value: "+this.getScore()+"\n");
        str.append("Learning time:" + this.getNanoExecutionTime()+" ms");

        return str.toString();
    }
}
