package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.EnergyCard;
import hr.algebra.theloop.cards.MovementCard;
import hr.algebra.theloop.cards.RiftCard;
import hr.algebra.theloop.model.*;
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

    private final DrFooAI drFooAI;
    private final MissionManager missionManager;

    public GameEngine() {
        this.gameState = new GameState();
        this.players = new ArrayList<>();
        this.random = new Random();
        this.currentPlayerIndex = 0;
        this.waitingForPlayerInput = false;

        this.drFooAI = new DrFooAI(random);
        this.missionManager = new MissionManager(random);
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

        missionManager.initializeMissions(gameState);
        players.get(0).setCurrentPlayer(true);
        waitingForPlayerInput = true;

        System.out.println("🎮 THE LOOP GAME STARTED!");
        System.out.println("Players: " + players.size());
        System.out.println("Missions: " + gameState.getActiveMissions().size() + " active");
        printGameStatus();

        System.out.println("⏳ Waiting for player actions... Click cards or use buttons!");
    }

    public void processTurn() {
        if (gameState.isGameOver()) {
            return;
        }

        drFooAI.executeDrFooPhase(gameState);
        waitingForPlayerInput = true;

        System.out.println("⏳ Waiting for player actions... Click cards or use buttons!");
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) {
            return false;
        }

        List<ArtifactCard> hand = player.getHand();
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            return false;
        }

        ArtifactCard card = hand.get(cardIndex);
        if (!card.canExecute(gameState, player)) {
            System.out.println("❌ Cannot play " + card.getName());
            return false;
        }

        card.execute(gameState, player);
        System.out.println("✅ Played: " + card.getName());

        missionManager.checkAllMissions(gameState, player, card.getClass().getSimpleName());

        return true;
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

                missionManager.checkAllMissions(gameState, player, "Movement");

                return true;
            } else if (gameState.getEnergy(currentEra) > 0) {
                gameState.removeEnergy(currentEra, 1);
                player.moveToEra(targetEra);
                System.out.println("🚶 " + player.getName() + " moved to " + targetEra.getDisplayName() + " (1 energy)");

                missionManager.checkAllMissions(gameState, player, "Movement");

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