package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class StabilizeEraMission extends Mission {

    private int riftsRemovedByPlayer = 0;
    private int initialRiftCount = 0;
    private boolean playerHasWorked = false;

    public StabilizeEraMission(Era assignedEra) {
        super("Stabilize " + assignedEra.getDisplayName(),
                "Remove all rifts from " + assignedEra.getDisplayName(),
                assignedEra,
                1);
    }

    public void setInitialRiftCount(int initialCount) {
        this.initialRiftCount = initialCount;
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {
        if (!actionType.contains("Rift")) {
            return false;
        }

        if (!player.getCurrentEra().equals(assignedEra)) {
            return false;
        }

        riftsRemovedByPlayer++;
        playerHasWorked = true;

        if (gameState.getRifts(assignedEra) == 0 && playerHasWorked && riftsRemovedByPlayer > 0) {
            completed = true;
            currentProgress = 1;
            return true;
        }
        return false;
    }

    public int getRiftsRemovedByPlayer() {
        return riftsRemovedByPlayer;
    }

    public boolean hasPlayerWorked() {
        return playerHasWorked;
    }

    @Override
    public String toString() {
        String workStatus = playerHasWorked ?
                " (Player removed: " + riftsRemovedByPlayer + ")" :
                " (No player action yet)";

        return String.format("%s (%d/%d) at %s%s",
                name, currentProgress, requiredProgress,
                assignedEra != null ? assignedEra.getDisplayName() : "Any Era",
                workStatus);
    }
}