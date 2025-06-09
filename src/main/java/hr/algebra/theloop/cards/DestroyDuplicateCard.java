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
            executeWithDuplicate(gameState, player, playerEra, duplicateToDestroy);
        }

        exhaust();
    }

    public boolean executeWithDuplicate(GameState gameState, Player player, Era sourceEra, Duplicate selectedDuplicate) {
        List<Duplicate> duplicatesHere = gameState.getDuplicatesAt(sourceEra);

        if (!duplicatesHere.contains(selectedDuplicate)) {
            return false;
        }

        gameState.removeDuplicate(sourceEra, selectedDuplicate);

        // TODO: Need to fix duplicate bag management
        GameLogger.playerAction(player.getName(), "Destroyed duplicate at " + sourceEra.getDisplayName());

        return true;
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