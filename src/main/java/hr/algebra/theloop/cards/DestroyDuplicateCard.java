package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.utils.GameLogger;

import java.util.List;

public class DestroyDuplicateCard extends ArtifactCard {

    public DestroyDuplicateCard(String name) {
        super(name, "Destroy 1 duplicate on current era", CardDimension.STRIPE);
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        List<Duplicate> duplicatesHere = gameState.getDuplicatesAt(playerEra);

        if (!duplicatesHere.isEmpty()) {
            Duplicate duplicateToDestroy = duplicatesHere.get(0);
            gameState.removeDuplicate(playerEra, duplicateToDestroy);
            GameLogger.playerAction(player.getName(), "Destroyed duplicate at " + playerEra.getDisplayName());
        }

        exhaust();
    }

    public boolean executeWithDuplicate(GameState gameState, Player player, Era targetEra, Duplicate selectedDuplicate) {
        Era playerEra = player.getCurrentEra();

        if (!playerEra.equals(targetEra)) {
            GameLogger.warning("Cannot destroy duplicate - must be on same era as duplicate!");
            return false;
        }

        List<Duplicate> duplicatesHere = gameState.getDuplicatesAt(targetEra);

        if (!duplicatesHere.contains(selectedDuplicate)) {
            return false;
        }

        boolean removed = gameState.removeDuplicate(targetEra, selectedDuplicate);

        if (removed) {
            GameLogger.playerAction(player.getName(), "Destroyed duplicate at " + targetEra.getDisplayName());
            return true;
        }

        return false;
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        return !isExhausted() && !gameState.getDuplicatesAt(playerEra).isEmpty();
    }

    public static DestroyDuplicateCard createBasicDestroy() {
        return new DestroyDuplicateCard("Destroy Duplicate");
    }
}