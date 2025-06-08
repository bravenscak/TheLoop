package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

import java.util.List;

public class DestroyDuplicateCard extends ArtifactCard {

    public DestroyDuplicateCard(String name, Era originalEra) {
        super(name, "Destroy 1 duplicate on current era", originalEra, CardDimension.STRIPE);
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        List<Duplicate> duplicatesHere = gameState.getDuplicatesAt(playerEra);

        if (!duplicatesHere.isEmpty()) {
            Duplicate duplicateToDestroy = duplicatesHere.get(0);
            gameState.removeDuplicate(playerEra, duplicateToDestroy);

            System.out.println("💥 Destroyed duplicate " + duplicateToDestroy.getDisplayName() +
                    " at " + playerEra.getDisplayName() + "!");
        }

        exhaust();
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        return !isExhausted() && !gameState.getDuplicatesAt(playerEra).isEmpty();
    }
}