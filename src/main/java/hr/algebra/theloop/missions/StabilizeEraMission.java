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
        System.out.println("🐛 Stabilize mission check: actionType=" + actionType +
                ", player@" + player.getCurrentEra() +
                ", mission@" + assignedEra +
                ", currentRifts=" + gameState.getRifts(assignedEra));

        if (!actionType.contains("Rift")) {
            System.out.println("🐛 Not a rift action, skipping");
            return false;
        }

        if (!player.getCurrentEra().equals(assignedEra)) {
            System.out.println("🐛 Player not on mission era, skipping");
            return false;
        }

        riftsRemovedByPlayer++;
        playerHasWorked = true;

        System.out.println("🎯 Mission progress: " + riftsRemovedByPlayer + " rifts removed by player from " + assignedEra.getDisplayName());

        if (gameState.getRifts(assignedEra) == 0 && playerHasWorked && riftsRemovedByPlayer > 0) {
            System.out.println("🎯 Mission completed through player action at " + assignedEra.getDisplayName() + "!");
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