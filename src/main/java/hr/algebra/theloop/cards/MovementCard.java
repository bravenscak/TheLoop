package hr.algebra.theloop.cards;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.utils.GameLogger;

public class MovementCard extends ArtifactCard {

    public enum MovementEffect {
        MOVE_ADJACENT,
        MOVE_TWO_ERAS,
        MOVE_AND_ADD_ENERGY
    }

    private final int moveDistance;
    private final MovementEffect effect;

    public MovementCard(String name, int moveDistance) {
        super(name, "Move up to " + moveDistance + " era(s) - click target era", CardDimension.SPIRAL);
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
            case MOVE_ADJACENT -> "Move to adjacent era - click target";
            case MOVE_TWO_ERAS -> "Move up to 2 eras - click target";
            case MOVE_AND_ADD_ENERGY -> "Move to adjacent era + add 1 energy - click target";
        };
    }

    @Override
    public void execute(GameState gameState, Player player) {
        exhaust();
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        return !isExhausted();
    }

    public boolean executeMovement(GameState gameState, Player player, Era targetEra) {
        Era currentEra = player.getCurrentEra();

        if (!isValidTarget(currentEra, targetEra)) {
            return false;
        }

        switch (effect) {
            case MOVE_ADJACENT, MOVE_TWO_ERAS -> {
                player.moveToEra(targetEra);
                GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName());
            }
            case MOVE_AND_ADD_ENERGY -> {
                player.moveToEra(targetEra);
                gameState.addEnergy(targetEra, 1);
                GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName() + " + 1 energy");
            }
        }
        return true;
    }

    public boolean isValidTarget(Era currentEra, Era targetEra) {
        return switch (effect) {
            case MOVE_ADJACENT -> currentEra.isAdjacentTo(targetEra);
            case MOVE_TWO_ERAS -> currentEra.distanceTo(targetEra) <= 2;
            case MOVE_AND_ADD_ENERGY -> currentEra.isAdjacentTo(targetEra);
        };
    }

    public Era[] getValidTargets(Era currentEra) {
        return switch (effect) {
            case MOVE_ADJACENT, MOVE_AND_ADD_ENERGY -> new Era[]{currentEra.getNext(), currentEra.getPrevious()};
            case MOVE_TWO_ERAS -> {
                Era[] allEras = Era.values();
                yield java.util.Arrays.stream(allEras)
                        .filter(era -> currentEra.distanceTo(era) <= 2 && !era.equals(currentEra))
                        .toArray(Era[]::new);
            }
        };
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