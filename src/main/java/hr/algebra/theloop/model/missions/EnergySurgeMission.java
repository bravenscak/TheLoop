package hr.algebra.theloop.model.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Mission;
import hr.algebra.theloop.model.Player;

public class EnergySurgeMission extends Mission {

    public EnergySurgeMission(Era assignedEra) {
        super("Energy Surge at " + assignedEra.getDisplayName(),
                "Accumulate 5+ energy at " + assignedEra.getDisplayName(),
                assignedEra,
                5); // Need 5 energy
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {
        // Check current energy level at assigned era
        int currentEnergy = gameState.getEnergy(assignedEra);

        if (currentEnergy > currentProgress) {
            // Energy increased
            addProgress(currentEnergy - currentProgress);
            return true;
        }

        return false;
    }
}