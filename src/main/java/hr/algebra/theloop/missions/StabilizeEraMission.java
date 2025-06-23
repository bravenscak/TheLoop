package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class StabilizeEraMission extends Mission {

    private int riftsRemovedByPlayer = 0;
    private boolean playerHasWorked = false;

    public StabilizeEraMission(Era assignedEra) {
        super("Stabilize " + assignedEra.getDisplayName(),
                "Remove all rifts from " + assignedEra.getDisplayName(),
                assignedEra,
                1);
    }

    public void setInitialRiftCount(int initialCount) {
        // Method kept for API compatibility, but no longer stores the value
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

        if (gameState.getRifts(assignedEra) == 0 && riftsRemovedByPlayer > 0) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;

        StabilizeEraMission that = (StabilizeEraMission) obj;
        return riftsRemovedByPlayer == that.riftsRemovedByPlayer &&
                playerHasWorked == that.playerHasWorked;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), riftsRemovedByPlayer, playerHasWorked);
    }
}