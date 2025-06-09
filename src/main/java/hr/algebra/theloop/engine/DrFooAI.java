package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.GameResult;

import java.util.Random;

public class DrFooAI {

    private final Random random;
    private final GameEngine gameEngine;

    public DrFooAI(Random random, GameEngine gameEngine) {
        this.random = random;
        this.gameEngine = gameEngine;
    }

    public void executeDrFooPhase(GameState gameState) {
        System.out.println("\n--- DR. FOO PHASE ---");

        spawnDuplicates(gameState);
        moveDrFoo(gameState);
        dropRifts(gameState);
        ageDuplicates(gameState);
        checkDefeatConditions(gameState);
    }

    private void spawnDuplicates(GameState gameState) {
        int duplicatesToSpawn = calculateDuplicatesToSpawn(gameState);

        for (int i = 0; i < duplicatesToSpawn; i++) {
            Era randomEra = getRandomEra();

            boolean spawned = gameEngine.spawnDuplicate(randomEra);
            if (!spawned) {
                System.out.println("🚫 Cannot spawn more duplicates - bag is empty!");
                break;
            }
        }
    }

    private int calculateDuplicatesToSpawn(GameState gameState) {
        int baseSpawn = 1;

        if (gameState.getCurrentCycle() >= 2) {
            baseSpawn++;
        }
        if (gameState.getCurrentCycle() >= 3) {
            baseSpawn++;
        }

        if (random.nextDouble() < 0.2) {
            baseSpawn++;
        }

        return baseSpawn;
    }

    private void moveDrFoo(GameState gameState) {
        Era oldPosition = gameState.getDrFooPosition();
        gameState.moveDrFoo();
        Era newPosition = gameState.getDrFooPosition();

        System.out.println("🤖 Dr. Foo moves: " + oldPosition.getDisplayName() + " → " + newPosition.getDisplayName());
    }

    private void dropRifts(GameState gameState) {
        Era drFooEra = gameState.getDrFooPosition();
        int duplicatesHere = gameState.getDuplicateCount(drFooEra);
        int totalRifts = 2 + duplicatesHere;

        System.out.println("🎲 Dropping " + totalRifts + " rifts (2 base + " + duplicatesHere + " duplicates)");
        simulateCubeTower(gameState, drFooEra, totalRifts);
    }

    private void simulateCubeTower(GameState gameState, Era drFooEra, int riftsToAdd) {
        Era[] possibleTargets = {
                drFooEra.getPrevious(),
                drFooEra,
                drFooEra.getNext()
        };

        System.out.println("🎯 Cube tower targets: " +
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

    private void ageDuplicates(GameState gameState) {
        for (Era era : Era.values()) {
            for (Duplicate duplicate : gameState.getDuplicatesAt(era)) {
                duplicate.ageTurn();
            }
        }
    }

    private void checkDefeatConditions(GameState gameState) {
        if (gameState.getVortexCount() >= 3) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
            System.out.println("💀 DEFEAT: 3+ vortexes created!");
            return;
        }

        if (gameState.getCurrentCycle() > 3) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
            System.out.println("💀 DEFEAT: Dr. Foo completed 3 cycles!");
            return;
        }
    }

    private Era getRandomEra() {
        Era[] eras = Era.values();
        return eras[random.nextInt(eras.length)];
    }
}