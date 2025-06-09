package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.utils.GameLogger;

import java.util.List;

public class PushDuplicateCard extends ArtifactCard {

    public PushDuplicateCard(String name) {
        super(name, "Push 1 duplicate to adjacent era", CardDimension.STRIPE);
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        List<Duplicate> duplicatesHere = gameState.getDuplicatesAt(playerEra);

        if (duplicatesHere.isEmpty()) {
            return;
        }

        Duplicate duplicateToMove = duplicatesHere.get(0);
        executeWithDuplicate(gameState, player, playerEra, duplicateToMove);
        exhaust();
    }

    public boolean executeWithDuplicate(GameState gameState, Player player, Era sourceEra, Duplicate selectedDuplicate) {
        List<Duplicate> duplicatesHere = gameState.getDuplicatesAt(sourceEra);

        if (!duplicatesHere.contains(selectedDuplicate)) {
            return false;
        }

        Era nextEra = sourceEra.getNext();
        Era prevEra = sourceEra.getPrevious();
        Era targetEra = gameState.getRifts(nextEra) <= gameState.getRifts(prevEra) ? nextEra : prevEra;

        gameState.removeDuplicate(sourceEra, selectedDuplicate);
        selectedDuplicate.moveTo(targetEra);

        if (selectedDuplicate.isAtDestructionEra()) {
            GameLogger.playerAction(player.getName(), "Duplicate destroyed by temporal paradox at " + targetEra.getDisplayName());
        } else {
            gameState.addDuplicate(targetEra, selectedDuplicate);
            GameLogger.playerAction(player.getName(), "Pushed duplicate to " + targetEra.getDisplayName());
        }

        return true;
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        return !isExhausted() && !gameState.getDuplicatesAt(playerEra).isEmpty();
    }

    public static PushDuplicateCard createBasicPush() {
        return new PushDuplicateCard("Push Duplicate");
    }
}