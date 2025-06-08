package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
public class EnergySurgeMission extends Mission {

    public EnergySurgeMission(Era assignedEra) {
        super("Energy Surge at " + assignedEra.getDisplayName(),
                "Accumulate 5+ energy at " + assignedEra.getDisplayName(),
                assignedEra,
                5);
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {
        int currentEnergy = gameState.getEnergy(assignedEra);

        if (currentEnergy >= 5 && !completed) {
            System.out.println("🎯 Energy surge achieved at " + assignedEra.getDisplayName() + "!");
            addProgress(5);
            return true;
        }

        return false;
    }
}