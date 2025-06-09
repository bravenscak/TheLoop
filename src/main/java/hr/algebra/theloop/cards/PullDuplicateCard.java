package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

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
            System.out.println("❌ " + sourceEra.getDisplayName() + " is not adjacent to player");
            return false;
        }

        List<Duplicate> duplicatesAtSource = gameState.getDuplicatesAt(sourceEra);
        if (!duplicatesAtSource.contains(selectedDuplicate)) {
            System.out.println("❌ Selected duplicate not found at " + sourceEra.getDisplayName());
            return false;
        }

        gameState.removeDuplicate(sourceEra, selectedDuplicate);
        selectedDuplicate.moveTo(playerEra);

        if (selectedDuplicate.isAtDestructionEra()) {
            System.out.println("💥 " + selectedDuplicate.getDisplayName() +
                    " destroyed by temporal paradox at " + playerEra.getDisplayName() + "!");
        } else {
            gameState.addDuplicate(playerEra, selectedDuplicate);
            System.out.println("🔄 Pulled " + selectedDuplicate.getDisplayName() + " from " +
                    sourceEra.getDisplayName() + " to " + playerEra.getDisplayName());
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