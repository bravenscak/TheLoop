package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.GameResult;

import java.util.Random;

public class DrFooAI {

    private final Random random;

    public DrFooAI(Random random) {
        this.random = random;
    }

    public void executeDrFooPhase(GameState gameState) {
        System.out.println("\n--- DR. FOO PHASE ---");

        Era oldPosition = gameState.getDrFooPosition();
        gameState.moveDrFoo();
        Era newPosition = gameState.getDrFooPosition();

        System.out.println("Dr. Foo moves: " + oldPosition.getDisplayName() + " → " + newPosition.getDisplayName());

        int duplicatesHere = gameState.getDuplicateCount(newPosition);
        int totalRifts = 2 + duplicatesHere;

        System.out.println("Dropping " + totalRifts + " rifts into cube tower...");

        simulateCubeTower(gameState, newPosition, totalRifts);
        checkDefeatConditions(gameState);
    }

    private void simulateCubeTower(GameState gameState, Era drFooEra, int riftsToAdd) {
        Era[] possibleTargets = {
                drFooEra.getPrevious(),
                drFooEra,
                drFooEra.getNext()
        };

        System.out.println("🎲 Cube tower targets: " +
                possibleTargets[0].getDisplayName() + ", " +
                possibleTargets[1].getDisplayName() + ", " +
                possibleTargets[2].getDisplayName());

        for (int i = 0; i < riftsToAdd; i++) {
            Era targetEra = possibleTargets[random.nextInt(3)];
            addRiftToEra(gameState, targetEra);
        }
    }

    private void addRiftToEra(GameState gameState, Era era) {
        int currentRifts = gameState.getRifts(era);

        if (currentRifts >= 3) {
            System.out.println("⚠️ VORTEX created at " + era.getDisplayName() + "!");
            gameState.createVortex(era);
        } else {
            gameState.addRifts(era, 1);
            System.out.println("🔴 Added 1 rift to " + era.getDisplayName() + " (" + (currentRifts + 1) + "/3)");
        }
    }

    private void checkDefeatConditions(GameState gameState) {
        if (gameState.getVortexCount() >= 4) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
            System.out.println("💀 DEFEAT: 4 vortexes created!");
            return;
        }

        if (gameState.getCurrentCycle() > 3) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
            System.out.println("💀 DEFEAT: Dr. Foo completed 3 cycles!");
            return;
        }
    }
}