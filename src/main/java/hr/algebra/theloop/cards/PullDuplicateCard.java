package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

import java.util.List;

public class PullDuplicateCard extends ArtifactCard {

    public PullDuplicateCard(String name, Era originalEra) {
        super(name, "Pull 1 duplicate from adjacent era", originalEra, CardDimension.STRIPE);
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
            gameState.removeDuplicate(sourceEra, duplicateToMove);
            duplicateToMove.moveTo(playerEra);

            if (duplicateToMove.isAtDestructionEra()) {
                System.out.println("💥 Duplicate destroyed by temporal paradox at " + playerEra.getDisplayName() + "!");
            } else {
                gameState.addDuplicate(playerEra, duplicateToMove);
                System.out.println("🔄 Pulled duplicate from " + sourceEra.getDisplayName() + " to " + playerEra.getDisplayName());
            }
        }

        exhaust();
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        if (isExhausted()) return false;

        Era playerEra = player.getCurrentEra();
        return !gameState.getDuplicatesAt(playerEra.getNext()).isEmpty() ||
                !gameState.getDuplicatesAt(playerEra.getPrevious()).isEmpty();
    }
}