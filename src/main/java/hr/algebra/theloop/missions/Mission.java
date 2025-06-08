package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import lombok.Data;

import java.io.Serializable;

@Data
public abstract class Mission implements Serializable {

    protected String name;
    protected String description;
    protected Era assignedEra;
    protected int currentProgress;
    protected int requiredProgress;
    protected boolean completed;

    public Mission(String name, String description, Era assignedEra, int requiredProgress) {
        this.name = name;
        this.description = description;
        this.assignedEra = assignedEra;
        this.requiredProgress = requiredProgress;
        this.currentProgress = 0;
        this.completed = false;
    }

    public abstract boolean checkProgress(GameState gameState, Player player, String actionType);

    public void addProgress(int amount) {
        currentProgress = Math.min(currentProgress + amount, requiredProgress);
        if (currentProgress >= requiredProgress && !completed) {
            completed = true;
        }
    }

    public int getProgressPercentage() {
        return (int) ((double) currentProgress / requiredProgress * 100);
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d) at %s",
                name, currentProgress, requiredProgress,
                assignedEra != null ? assignedEra.getDisplayName() : "Any Era");
    }
}