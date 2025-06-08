package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.energy.EnergyCard;
import hr.algebra.theloop.cards.movement.MovementCard;
import hr.algebra.theloop.cards.rift.RiftCard;
import hr.algebra.theloop.model.*;
import hr.algebra.theloop.model.missions.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class GameEngine {
    private GameState gameState;
    private List<Player> players;
    private Random random;
    private int currentPlayerIndex;
    private boolean waitingForPlayerInput;

    public GameEngine() {
        this.gameState = new GameState();
        this.players = new ArrayList<>();
        this.random = new Random();
        this.currentPlayerIndex = 0;
        this.waitingForPlayerInput = false;
    }

    public void addPlayer(String name, Era startingEra) {
        Player player = new Player(name, startingEra);
        players.add(player);
        giveStartingCards(player);
    }

    private void giveStartingCards(Player player) {
        player.addCardToHand(new EnergyCard("Basic Energy", Era.DAWN_OF_TIME, 1));
        player.addCardToHand(new MovementCard("Basic Move", Era.MEDIEVAL, 1));
        player.addCardToHand(new RiftCard("Basic Repair", Era.RENAISSANCE, 1));
    }

    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        initializeMissions();

        players.get(0).setCurrentPlayer(true);

        waitingForPlayerInput = true;

        System.out.println("🎮 THE LOOP GAME STARTED!");
        System.out.println("Players: " + players.size());
        System.out.println("Missions: " + gameState.getActiveMissions().size() + " active");
        printGameStatus();

        System.out.println("⏳ Waiting for player actions... Click cards or use buttons!");
    }

    private void initializeMissions() {
        gameState.addMission(new StabilizeEraMission(Era.MEDIEVAL));
        gameState.addMission(new EnergySurgeMission(Era.RENAISSANCE));

        System.out.println("🎯 Missions initialized:");
        for (Mission mission : gameState.getActiveMissions()) {
            System.out.println("  - " + mission.toString());
        }
    }

    public void processTurn() {
        if (gameState.isGameOver()) {
            return;
        }

        drFooPhase();
        waitingForPlayerInput = true;

        System.out.println("⏳ Waiting for player actions... Click cards or use buttons!");
    }

    private void drFooPhase() {
        System.out.println("\n--- DR. FOO PHASE ---");

        Era oldPosition = gameState.getDrFooPosition();
        gameState.moveDrFoo();
        Era newPosition = gameState.getDrFooPosition();

        System.out.println("Dr. Foo moves: " + oldPosition.getDisplayName() + " → " + newPosition.getDisplayName());

        int duplicatesHere = gameState.getDuplicateCount(newPosition);
        int totalRifts = 2 + duplicatesHere;

        System.out.println("Dropping " + totalRifts + " rifts into cube tower...");

        simulateCubeTower(newPosition, totalRifts);

        checkDefeatConditions();
    }

    private void simulateCubeTower(Era drFooEra, int riftsToAdd) {
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
            addRiftToEra(targetEra);
        }
    }

    private void addRiftToEra(Era era) {
        int currentRifts = gameState.getRifts(era);

        if (currentRifts >= 3) {
            System.out.println("⚠️ VORTEX created at " + era.getDisplayName() + "!");
            gameState.createVortex(era);
        } else {
            gameState.addRifts(era, 1);
            System.out.println("🔴 Added 1 rift to " + era.getDisplayName() + " (" + (currentRifts + 1) + "/3)");
        }
    }

    private void checkDefeatConditions() {
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

        // Rule 3: 2 vortexes in same era = defeat (TODO: implement later)
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) {
            return false;
        }

        List<hr.algebra.theloop.cards.ArtifactCard> hand = player.getHand();
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            return false;
        }

        hr.algebra.theloop.cards.ArtifactCard card = hand.get(cardIndex);
        if (!card.canExecute(gameState, player)) {
            System.out.println("❌ Cannot play " + card.getName());
            return false;
        }

        card.execute(gameState, player);
        System.out.println("✅ Played: " + card.getName());

        checkAllMissions(player, card.getClass().getSimpleName());

        return true;
    }

    private void checkMissionProgress(Player player, hr.algebra.theloop.cards.ArtifactCard card) {
        for (Mission mission : new ArrayList<>(gameState.getActiveMissions())) {
            if (mission.checkProgress(gameState, player, card.getClass().getSimpleName())) {
                if (mission.isCompleted()) {
                    gameState.completeMission(mission);

                    if (gameState.getActiveMissions().size() < 2) {
                        addRandomMission();
                    }
                }
            }
        }

        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            System.out.println("🎉 VICTORY: 4 missions completed!");
        }
    }

    private void addRandomMission() {
        Era[] eras = Era.values();
        Era randomEra = eras[random.nextInt(eras.length)];

        if (random.nextBoolean()) {
            gameState.addMission(new StabilizeEraMission(randomEra));
        } else {
            gameState.addMission(new EnergySurgeMission(randomEra));
        }

        System.out.println("🎯 New mission added!");
    }

    public boolean movePlayer(Player player, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) {
            return false;
        }

        Era currentEra = player.getCurrentEra();

        if (currentEra.isAdjacentTo(targetEra)) {
            if (player.canUseFreeBattery()) {
                player.useFreeBattery();
                player.moveToEra(targetEra);
                System.out.println("🚶 " + player.getName() + " moved to " + targetEra.getDisplayName() + " (free)");

                checkAllMissions(player, "Movement");

                return true;
            } else if (gameState.getEnergy(currentEra) > 0) {
                gameState.removeEnergy(currentEra, 1);
                player.moveToEra(targetEra);
                System.out.println("🚶 " + player.getName() + " moved to " + targetEra.getDisplayName() + " (1 energy)");

                checkAllMissions(player, "Movement");

                return true;
            }
        }

        System.out.println("❌ Cannot move to " + targetEra.getDisplayName());
        return false;
    }

    public void endPlayerTurn() {
        if (!waitingForPlayerInput) {
            return;
        }

        waitingForPlayerInput = false;

        for (Player player : players) {
            player.rechargeBatteries();

            for (ArtifactCard card : player.getHand()) {
                card.ready();
            }

            player.fillHandToThree();
        }

        gameState.nextTurn();
        printGameStatus();

        System.out.println("🔄 Turn ended. Click 'End Turn' for next Dr. Foo phase.");
    }

    public void printGameStatus() {
        System.out.println("\n=== GAME STATUS ===");
        System.out.println(gameState.toString());

        for (Player player : players) {
            System.out.println("  " + player.toString());
        }

        System.out.println("Vortexes: " + gameState.getVortexCount() + "/4");
        System.out.println("Active Missions: " + gameState.getActiveMissions().size());

        if (gameState.isGameOver()) {
            System.out.println("\n" + gameState.getGameResult().getMessage());
        }
    }

    private void checkAllMissions(Player player, String actionType) {
        List<Mission> missionsToComplete = new ArrayList<>();

        for (Mission mission : gameState.getActiveMissions()) {
            if (!mission.isCompleted() && mission.checkProgress(gameState, player, actionType)) {
                if (mission.isCompleted()) {
                    missionsToComplete.add(mission);
                }
            }
        }

        for (Mission mission : missionsToComplete) {
            gameState.completeMission(mission);

            if (gameState.getActiveMissions().size() < 2) {
                addRandomMission();
            }
        }

        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            System.out.println("🎉 VICTORY: 4 missions completed!");
        }
    }
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public boolean isWaitingForPlayerInput() {
        return waitingForPlayerInput;
    }
}