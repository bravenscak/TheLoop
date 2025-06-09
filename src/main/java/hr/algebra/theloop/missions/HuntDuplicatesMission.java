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
        System.out.println("🐛 Hunt mission check: actionType=" + actionType +
                ", player@" + player.getCurrentEra() +
                ", progress=" + currentProgress);

        if (actionType.contains("Duplicate")) {
            currentProgress++;
            System.out.println("🎯 Duplicate manipulated! Progress: " + currentProgress + "/" + requiredProgress);

            if (currentProgress >= requiredProgress) {
                System.out.println("🎯 Hunt Duplicates mission completed!");
                completed = true;
                return true;
            }

            return true;
        }

        System.out.println("🐛 Not a duplicate action, skipping");
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d) - any duplicate action",
                name, currentProgress, requiredProgress);
    }
}