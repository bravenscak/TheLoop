// 🛠️ ENERGYSURGEMISSION FIX
// Fix mission progress tracking

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
        System.out.println("🐛 Energy mission check: actionType=" + actionType +
                ", player@" + player.getCurrentEra() +
                ", mission@" + assignedEra +
                ", currentEnergy=" + gameState.getEnergy(assignedEra));

        if (!actionType.contains("Energy")) {
            System.out.println("🐛 Not an energy action, skipping");
            return false;
        }

        int currentEnergy = gameState.getEnergy(assignedEra);

        if (currentEnergy > currentProgress) {
            int newProgress = Math.min(currentEnergy, requiredProgress);
            System.out.println("🎯 Energy progress: " + currentProgress + " → " + newProgress);
            currentProgress = newProgress;
        }

        if (currentEnergy >= 5 && !completed) {
            System.out.println("🎯 Energy surge achieved at " + assignedEra.getDisplayName() + "!");
            completed = true;
            return true;
        }

        return false;
    }
}