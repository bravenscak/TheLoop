package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.utils.GameLogger;

import java.util.List;

public class PullDuplicateCard extends ArtifactCard {

    public PullDuplicateCard(String name) {
        super(name, "Pull 1 duplicate from adjacent era", CardDimension.STRIPE);
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        Era nextEra = playerEra.getNext();
        Era prevEra = playerEra.getPrevious();

        List<Duplicate> nextDuplicates = gameState.getDuplicatesAt(nextEra);
        List<Duplicate> prevDuplicates = gameState.getDuplicatesAt(prevEra);

        Duplicate duplicateToMove = null;
        Era sourceEra = null;

        if (!nextDuplicates.isEmpty()) {
            duplicateToMove = nextDuplicates.get(0);
            sourceEra = nextEra;
        } else if (!prevDuplicates.isEmpty()) {
            duplicateToMove = prevDuplicates.get(0);
            sourceEra = prevEra;
        }

        if (duplicateToMove != null && sourceEra != null) {
            executeWithDuplicate(gameState, player, sourceEra, duplicateToMove);
        }

        exhaust();
    }

    public boolean executeWithDuplicate(GameState gameState, Player player, Era sourceEra, Duplicate selectedDuplicate) {
        Era playerEra = player.getCurrentEra();

        if (!playerEra.isAdjacentTo(sourceEra)) {
            return false;
        }

        List<Duplicate> duplicatesAtSource = gameState.getDuplicatesAt(sourceEra);
        if (!duplicatesAtSource.contains(selectedDuplicate)) {
            return false;
        }

        gameState.removeDuplicate(sourceEra, selectedDuplicate);
        selectedDuplicate.moveTo(playerEra);

        if (selectedDuplicate.isAtDestructionEra()) {
            GameLogger.playerAction(player.getName(), "Duplicate destroyed by temporal paradox at " + playerEra.getDisplayName());
        } else {
            gameState.addDuplicate(playerEra, selectedDuplicate);
            GameLogger.playerAction(player.getName(), "Pulled duplicate from " + sourceEra.getDisplayName());
        }

        return true;
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        if (isExhausted()) return false;

        Era playerEra = player.getCurrentEra();
        return !gameState.getDuplicatesAt(playerEra.getNext()).isEmpty() ||
                !gameState.getDuplicatesAt(playerEra.getPrevious()).isEmpty();
    }

    public static PullDuplicateCard createBasicPull() {
        return new PullDuplicateCard("Pull Duplicate");
    }
}