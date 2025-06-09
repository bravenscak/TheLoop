package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class HuntDuplicatesMission extends Mission {

    public HuntDuplicatesMission() {
        super("Hunt Duplicates",
                "Manipulate 3 duplicates (push/pull/destroy)",
                null,
                3);
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {
        if (actionType.contains("Duplicate")) {
            currentProgress++;

            if (currentProgress >= requiredProgress) {
                completed = true;
                return true;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d) - any duplicate action",
                name, currentProgress, requiredProgress);
    }
}