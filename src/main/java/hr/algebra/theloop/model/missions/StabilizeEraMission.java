package hr.algebra.theloop.model.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Mission;
import hr.algebra.theloop.model.Player;

public class StabilizeEraMission extends Mission {

    public StabilizeEraMission(Era assignedEra) {
        super("Stabilize " + assignedEra.getDisplayName(),
                "Remove all rifts from " + assignedEra.getDisplayName(),
                assignedEra,
                1); // Just need 1 success (era with 0 rifts)
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {
        // Check if this era has been stabilized (0 rifts)
        if (gameState.getRifts(assignedEra) == 0 && !completed) {
            // Also check if player is on this era (optional rule)
            if (player.getCurrentEra().equals(assignedEra)) {
                addProgress(1);
                return true;
            }
        }
        return false;
    }
}
