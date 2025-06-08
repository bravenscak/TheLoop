package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class RiftCard extends ArtifactCard {
    private final int riftAmount;

    public RiftCard(String name, Era originalEra, int riftAmount) {
        super(name, "Remove " + riftAmount + " rifts from current era", originalEra, CardDimension.STRIPE);
        this.riftAmount = riftAmount;
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        int riftsHere = gameState.getRifts(playerEra);
        int actualRemoved = Math.min(riftAmount, riftsHere);

        gameState.removeRifts(playerEra, actualRemoved);
        exhaust();
        System.out.println("🔧 " + getName() + ": Removed " + actualRemoved + " rifts from " + playerEra.getDisplayName());
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();
        return !isExhausted() && gameState.getRifts(playerEra) > 0;
    }
}