package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class MovementCard extends ArtifactCard {

    public enum MovementEffect {
        MOVE_ADJACENT,
        MOVE_TWO_ERAS,
        MOVE_AND_ADD_ENERGY
    }

    private final int moveDistance;
    private final MovementEffect effect;

    public MovementCard(String name, int moveDistance) {
        super(name, "Move up to " + moveDistance + " era(s)", CardDimension.SPIRAL);
        this.moveDistance = moveDistance;
        this.effect = MovementEffect.MOVE_ADJACENT;
    }

    public MovementCard(String name, MovementEffect effect) {
        super(name, generateDescription(effect), CardDimension.SPIRAL);
        this.moveDistance = effect == MovementEffect.MOVE_TWO_ERAS ? 2 : 1;
        this.effect = effect;
    }

    private static String generateDescription(MovementEffect effect) {
        return switch (effect) {
            case MOVE_ADJACENT -> "Move to adjacent era";
            case MOVE_TWO_ERAS -> "Move up to 2 eras";
            case MOVE_AND_ADD_ENERGY -> "Move to adjacent era + add 1 energy";
        };
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era currentEra = player.getCurrentEra();
        Era targetEra = null;

        switch (effect) {
            case MOVE_ADJACENT -> {
                Era nextEra = currentEra.getNext();
                Era prevEra = currentEra.getPrevious();

                int nextRifts = gameState.getRifts(nextEra);
                int prevRifts = gameState.getRifts(prevEra);

                targetEra = (nextRifts <= prevRifts) ? nextEra : prevEra;
                player.moveToEra(targetEra);
                System.out.println("🚶 " + getName() + ": " + player.getName() + " moved to " + targetEra.getDisplayName());
            }

            case MOVE_TWO_ERAS -> {
                targetEra = currentEra.getNext().getNext();
                player.moveToEra(targetEra);
                System.out.println("🚶 " + getName() + ": " + player.getName() + " moved 2 eras to " + targetEra.getDisplayName());
            }

            case MOVE_AND_ADD_ENERGY -> {
                Era nextEra = currentEra.getNext();
                Era prevEra = currentEra.getPrevious();

                int nextRifts = gameState.getRifts(nextEra);
                int prevRifts = gameState.getRifts(prevEra);

                targetEra = (nextRifts <= prevRifts) ? nextEra : prevEra;
                player.moveToEra(targetEra);
                gameState.addEnergy(targetEra, 1);
                System.out.println("🚶⚡ " + getName() + ": " + player.getName() + " moved to " +
                        targetEra.getDisplayName() + " and added 1 energy");
            }
        }

        exhaust();
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        return !isExhausted();
    }

    public static MovementCard createTimeWalk() {
        return new MovementCard("Time Walk", 1);
    }

    public static MovementCard createQuantumLeap() {
        return new MovementCard("Quantum Leap", MovementEffect.MOVE_TWO_ERAS);
    }

    public static MovementCard createEnergeticStep() {
        return new MovementCard("Energetic Step", MovementEffect.MOVE_AND_ADD_ENERGY);
    }
}