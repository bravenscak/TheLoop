package hr.algebra.theloop.model.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Mission;
import hr.algebra.theloop.model.Player;

public class HuntDuplicatesMission extends Mission {

    public HuntDuplicatesMission() {
        super("Hunt Duplicates",
                "Destroy 3 duplicates on Dr. Foo's era",
                null, // No specific era - follows Dr. Foo
                3); // Need to destroy 3 duplicates
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {
        // This would trigger when duplicates are destroyed on Dr. Foo's era
        // Implementation depends on duplicate destruction mechanics
        // For now, just a placeholder
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d) - follows Dr. Foo",
                name, currentProgress, requiredProgress);
    }
}