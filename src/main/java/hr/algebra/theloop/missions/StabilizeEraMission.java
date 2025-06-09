package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class StabilizeEraMission extends Mission {

    public StabilizeEraMission(Era assignedEra) {
        super("Stabilize " + assignedEra.getDisplayName(),
                "Remove all rifts from " + assignedEra.getDisplayName(),
                assignedEra,
                1);
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {
        if ("RiftCard".equals(actionType) && gameState.getRifts(assignedEra) == 0 && !completed) {
            System.out.println("🎯 Era " + assignedEra.getDisplayName() + " stabilized!");
            addProgress(1);
            return true;
        }
        return false;
    }
}
