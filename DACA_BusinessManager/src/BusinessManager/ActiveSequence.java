package BusinessManager;

import java.util.*;

/**
 * Created by Regateiro on 08-03-2014.
 */
public class ActiveSequence {
    /**
     * The list of BE that are still alive. The BEs on this list are allowed to execute methods.
     */
    private final Set<String> besalive;

    /**
     * The current possible branches for this sequence.
     */
    private final Set<Integer> currBranches;

    /**
     * The position in the sequence.
     */
    private int nextPosition;

    /**
     * Instantiates a new instance of this class.
     */
    public ActiveSequence(Collection<Integer> sequences) {
        this.currBranches = new HashSet<>(sequences);
        this.besalive = new HashSet<>();
        this.nextPosition = 0;
    }

    /**
     * Updates the aliveBEs list.
     *
     * @param beUrl The name of the next BE used.
     */
    public void updateAliveBEs(Map<Integer, List<SequenceEntry>> sequences, String beUrl) {
        for (Integer seq : currBranches) {
            if (sequences.get(seq).size() > nextPosition) {
                String key = sequences.get(seq).get(nextPosition).getAuthorizedBS();
                List<String> values = sequences.get(seq).get(nextPosition).getRevokeList();

                if (key.equals(beUrl)) {
                    besalive.removeAll(values);
                }
            }
        }

        besalive.add(beUrl);
    }

    /**
     * Checks if a certain BE is still alive (able to execute methods).
     *
     * @param beUrl The unique name of the BE to have the execution validated.
     * @return True if the BE is still alive (able to execute methods), false otherwise.
     */
    public boolean isBEAlive(String beUrl) {
        return besalive.contains(beUrl);
    }

    /**
     * Gets the value of the next position.
     *
     * @return The value of the next position.
     */
    public int getNextPosition() {
        return nextPosition;
    }

    /**
     * Gets the current possible branches. The Set is unmodifiable.
     *
     * @return A unmodifiable view of the list of current possible branches.
     */
    public Set<Integer> getCurrentBranches() {
        return Collections.unmodifiableSet(currBranches);
    }

    /**
     * Removes specific branches from the list of possible branches.
     *
     * @param toRemove The list with the branches to remove.
     */
    public void removeBranches(List<Integer> toRemove) {
        this.currBranches.removeAll(toRemove);
    }

    /**
     * Increments the value of the next position.
     */
    public void incrementNextPosition() {
        this.nextPosition++;
    }

    @Override
    public String toString() {
        return "SequenceEntry{" + "besalive=" + besalive + ", currSequences=" + currBranches + ", nextPosition=" + nextPosition + '}';
    }
}
