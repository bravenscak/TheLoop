package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

import java.util.List;

public class PushDuplicateCard extends ArtifactCard {

    public PushDuplicateCard(String name, Era originalEra) {
        super(name, "Push 1 duplicate to adjacent era", originalEra, CardDimension.STRIPE);
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        List<Duplicate> duplicatesHere = gameState.getDuplicatesAt(playerEra);

        if (!duplicatesHere.isEmpty()) {
            Duplicate duplicateToMove = duplicatesHere.get(0);

            Era nextEra = playerEra.getNext();
            Era prevEra = playerEra.getPrevious();

            Era targetEra = gameState.getRifts(nextEra) <= gameState.getRifts(prevEra) ? nextEra : prevEra;

            gameState.removeDuplicate(playerEra, duplicateToMove);
            duplicateToMove.moveTo(targetEra);
            gameState.addDuplicate(targetEra, duplicateToMove);

            if (duplicateToMove.isAtDestructionEra()) {
                gameState.removeDuplicate(targetEra, duplicateToMove);
                System.out.println("💥 Duplicate destroyed by temporal paradox at " + targetEra.getDisplayName() + "!");
            } else {
                System.out.println("🔄 Pushed duplicate from " + playerEra.getDisplayName() + " to " + targetEra.getDisplayName());
            }
        }

        exhaust();
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        return !isExhausted() && !gameState.getDuplicatesAt(playerEra).isEmpty();
    }
}