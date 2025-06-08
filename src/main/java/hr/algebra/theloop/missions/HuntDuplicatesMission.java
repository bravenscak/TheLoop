package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class HuntDuplicatesMission extends Mission {

    public HuntDuplicatesMission() {
        super("Hunt Duplicates",
                "Destroy 3 duplicates on Dr. Foo's era",
                null,
                3);
    }

    @Override
    public boolean checkProgress(GameState gameState, Player player, String actionType) {

        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d) - follows Dr. Foo",
                name, currentProgress, requiredProgress);
    }
}