package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class RiftCard extends ArtifactCard {

    public enum RiftEffect {
        REMOVE_FROM_CURRENT,
        REMOVE_FROM_ADJACENT,
        REMOVE_FROM_DR_FOO
    }

    private final int riftAmount;
    private final RiftEffect effect;

    public RiftCard(String name, int riftAmount) {
        super(name, "Remove " + riftAmount + " rifts from current era", CardDimension.STRIPE);
        this.riftAmount = riftAmount;
        this.effect = RiftEffect.REMOVE_FROM_CURRENT;
    }

    public RiftCard(String name, int riftAmount, RiftEffect effect) {
        super(name, generateDescription(riftAmount, effect), CardDimension.STRIPE);
        this.riftAmount = riftAmount;
        this.effect = effect;
    }

    private static String generateDescription(int amount, RiftEffect effect) {
        return switch (effect) {
            case REMOVE_FROM_CURRENT -> "Remove " + amount + " rifts from current era";
            case REMOVE_FROM_ADJACENT -> "Remove " + amount + " rifts from adjacent eras";
            case REMOVE_FROM_DR_FOO -> "Remove " + amount + " rifts from Dr. Foo's era";
        };
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era playerEra = player.getCurrentEra();

        switch (effect) {
            case REMOVE_FROM_CURRENT -> {
                int riftsHere = gameState.getRifts(playerEra);
                int actualRemoved = Math.min(riftAmount, riftsHere);
                gameState.removeRifts(playerEra, actualRemoved);
                System.out.println("🔧 " + getName() + ": Removed " + actualRemoved + " rifts from " + playerEra.getDisplayName());
            }

            case REMOVE_FROM_ADJACENT -> {
                Era prevEra = playerEra.getPrevious();
                Era nextEra = playerEra.getNext();

                int prevRifts = gameState.getRifts(prevEra);
                int nextRifts = gameState.getRifts(nextEra);

                int prevRemoved = Math.min(riftAmount, prevRifts);
                int nextRemoved = Math.min(riftAmount, nextRifts);

                gameState.removeRifts(prevEra, prevRemoved);
                gameState.removeRifts(nextEra, nextRemoved);

                System.out.println("🔧 " + getName() + ": Removed " + prevRemoved + " rifts from " +
                        prevEra.getDisplayName() + " and " + nextRemoved + " rifts from " + nextEra.getDisplayName());
            }

            case REMOVE_FROM_DR_FOO -> {
                Era drFooEra = gameState.getDrFooPosition();
                int riftsHere = gameState.getRifts(drFooEra);
                int actualRemoved = Math.min(riftAmount, riftsHere);
                gameState.removeRifts(drFooEra, actualRemoved);
                System.out.println("🔧 " + getName() + ": Removed " + actualRemoved + " rifts from Dr. Foo's era (" + drFooEra.getDisplayName() + ")");
            }
        }

        exhaust();
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        if (isExhausted()) {
            return false;
        }

        return switch (effect) {
            case REMOVE_FROM_CURRENT -> gameState.getRifts(player.getCurrentEra()) > 0;
            case REMOVE_FROM_DR_FOO -> gameState.getRifts(gameState.getDrFooPosition()) > 0;
            case REMOVE_FROM_ADJACENT -> {
                Era playerEra = player.getCurrentEra();
                yield gameState.getRifts(playerEra.getPrevious()) > 0 ||
                        gameState.getRifts(playerEra.getNext()) > 0;
            }
        };
    }

    public static RiftCard createBasicRepair() {
        return new RiftCard("Basic Repair", 1);
    }

    public static RiftCard createQuantumEraser() {
        return new RiftCard("Quantum Eraser", 2);
    }

    public static RiftCard createAdjacentRepair() {
        return new RiftCard("Adjacent Repair", 1, RiftEffect.REMOVE_FROM_ADJACENT);
    }

    public static RiftCard createDrFooDisruptor() {
        return new RiftCard("Dr. Foo Disruptor", 2, RiftEffect.REMOVE_FROM_DR_FOO);
    }
}