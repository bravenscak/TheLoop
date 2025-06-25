package hr.algebra.theloop.engine;

import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.GameResult;
import hr.algebra.theloop.utils.GameLogger;

import java.util.Random;

public class DrFooAI {

    private final Random random;
    private final GameEngine gameEngine;
    private final ConfigurationManager configManager;

    public DrFooAI(Random random, GameEngine gameEngine) {
        this.random = random;
        this.gameEngine = gameEngine;
        this.configManager = ConfigurationManager.getInstance();

    }

    public void executeDrFooPhase(GameState gameState) {
        GameLogger.drFoo("Phase begins");

        spawnDuplicates(gameState);
        moveDrFoo(gameState);
        dropRifts(gameState);
        ageDuplicates(gameState);
        checkDefeatConditions(gameState);
    }

    private void spawnDuplicates(GameState gameState) {
        int duplicatesToSpawn = calculateDuplicatesToSpawn(gameState);

        for (int i = 0; i < duplicatesToSpawn; i++) {
            Era spawnEra = getRandomEra();

            boolean spawned = gameEngine.spawnDuplicate(spawnEra);
            if (!spawned) {
                GameLogger.warning("Cannot spawn duplicate - bag empty");
                break;
            } else {
                GameLogger.drFoo("Spawned duplicate at " + spawnEra.getDisplayName());
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

        GameLogger.drFoo("Moves: " + oldPosition.getDisplayName() + " → " + newPosition.getDisplayName());
    }

    private void dropRifts(GameState gameState) {
        Era drFooEra = gameState.getDrFooPosition();
        int duplicatesHere = gameState.getDuplicateCount(drFooEra);
        int totalRifts = 2 + duplicatesHere;

        GameLogger.drFoo("Dropping " + totalRifts + " rifts (2 base + " + duplicatesHere + " duplicates)");
        simulateCubeTower(gameState, drFooEra, totalRifts);
    }

    private void simulateCubeTower(GameState gameState, Era drFooEra, int riftsToAdd) {
        Era[] possibleTargets = {
                drFooEra.getPrevious(),
                drFooEra,
                drFooEra.getNext()
        };

        for (int i = 0; i < riftsToAdd; i++) {
            Era targetEra = possibleTargets[random.nextInt(3)];
            addRiftToEra(gameState, targetEra);
        }
    }

    private void addRiftToEra(GameState gameState, Era era) {
        int currentRifts = gameState.getRifts(era);

        if (currentRifts >= 3) {
            GameLogger.warning("VORTEX created at " + era.getDisplayName());
            gameState.createVortex(era);
        } else {
            gameState.addRifts(era, 1);
            GameLogger.drFoo("Added 1 rift to " + era.getDisplayName() + " (" + (currentRifts + 1) + "/3)");
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
        if (gameState.getVortexCount() >= configManager.getMaxVortexes()) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
            GameLogger.gameEnd("DEFEAT: 3+ vortexes created!");
            return;
        }

        if (gameState.getCurrentCycle() > configManager.getMaxCycles()) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
            GameLogger.gameEnd("DEFEAT: Dr. Foo completed 3 cycles!");
        }
    }

    private Era getRandomEra() {
        Era[] eras = Era.values();
        return eras[random.nextInt(eras.length)];
    }
}