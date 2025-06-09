package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.*;
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
    private final CardAcquisitionManager cardAcquisitionManager;

    public GameEngine() {
        this.gameState = new GameState();
        this.players = new ArrayList<>();
        this.random = new Random();
        this.currentPlayerIndex = 0;
        this.waitingForPlayerInput = false;

        this.drFooAI = new DrFooAI(random);
        this.missionManager = new MissionManager(random);
        this.cardAcquisitionManager = new CardAcquisitionManager(random);
    }

    public void addPlayer(String name, Era startingEra) {
        Player player = new Player(name, startingEra);
        players.add(player);
        giveStartingCards(player);
    }

    private void giveStartingCards(Player player) {
        List<ArtifactCard> startingCards = CardFactory.createRandomStartingDeck();
        for (ArtifactCard card : startingCards) {
            player.addCardToHand(card);
        }
        System.out.println("📋 " + player.getName() + " starting cards: " +
                startingCards.stream().map(ArtifactCard::getName).toList());
    }

    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        cardAcquisitionManager.initializeAvailableCards(gameState);
        missionManager.initializeMissions(gameState);
        players.get(0).setCurrentPlayer(true);
        waitingForPlayerInput = true;

        System.out.println("🎮 THE LOOP GAME STARTED!");
        printGameStatus();
        System.out.println("⏳ Waiting for player actions...");
    }

    public void processTurn() {
        if (gameState.isGameOver()) return;

        drFooAI.executeDrFooPhase(gameState);
        cardAcquisitionManager.addRandomCardsToEras(gameState, 1);
        waitingForPlayerInput = true;
        System.out.println("⏳ Waiting for player actions...");
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) return false;

        List<ArtifactCard> hand = player.getHand();
        if (cardIndex < 0 || cardIndex >= hand.size()) return false;

        ArtifactCard card = hand.get(cardIndex);
        if (!card.canExecute(gameState, player)) {
            System.out.println("❌ Cannot play " + card.getName());
            return false;
        }

        card.execute(gameState, player);

        System.out.println("✅ Played: " + card.getName() + " (" + card.getDimension().getDisplayName() + ") [EXHAUSTED]");
        missionManager.checkAllMissions(gameState, player, card.getClass().getSimpleName());
        return true;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) return false;

        Era currentEra = player.getCurrentEra();
        if (!currentEra.isAdjacentTo(targetEra)) {
            System.out.println("❌ Cannot move to " + targetEra.getDisplayName());
            return false;
        }

        if (player.canUseFreeBattery()) {
            player.useFreeBattery();
            player.moveToEra(targetEra);
            System.out.println("🚶 " + player.getName() + " moved to " + targetEra.getDisplayName() + " (free)");
        } else if (gameState.getEnergy(currentEra) > 0) {
            gameState.removeEnergy(currentEra, 1);
            player.moveToEra(targetEra);
            System.out.println("🚶 " + player.getName() + " moved to " + targetEra.getDisplayName() + " (1 energy)");
        } else {
            System.out.println("❌ No energy to move");
            return false;
        }

        missionManager.checkAllMissions(gameState, player, "Movement");
        return true;
    }

    public boolean acquireCard(Player player) {
        Era playerEra = player.getCurrentEra();
        ArtifactCard acquiredCard = cardAcquisitionManager.acquireCard(playerEra, player);

        if (acquiredCard != null) {
            System.out.println("🎴 " + player.getName() + " acquired: " + acquiredCard.getName());
            return true;
        } else {
            System.out.println("❌ No cards available at " + playerEra.getDisplayName());
            return false;
        }
    }

    public boolean performLoop(Player player, CardDimension dimension) {
        if (!player.canPerformLoop(dimension, gameState)) {
            System.out.println("❌ Cannot perform LOOP on " + dimension.getDisplayName() + " cards");
            return false;
        }

        Era playerEra = player.getCurrentEra();
        int loopCost = player.getLoopsPerformedThisTurn() + 1;

        gameState.removeEnergy(playerEra, loopCost);

        int readiedCards = 0;
        for (ArtifactCard card : player.getHand()) {
            if (card.getDimension() == dimension && card.isExhausted()) {
                card.ready();
                readiedCards++;
            }
        }

        player.setLoopsPerformedThisTurn(player.getLoopsPerformedThisTurn() + 1);
        System.out.println("🔄 LOOP: Readied " + readiedCards + " " + dimension.getDisplayName() + " cards");
        return true;
    }

    public void endPlayerTurn() {
        if (!waitingForPlayerInput) return;

        waitingForPlayerInput = false;
        for (Player player : players) {
            for (ArtifactCard card : player.getHand()) {
                player.discardCard(card);
            }
            player.getHand().clear();

            player.rechargeBatteries();
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
            System.out.println("    Hand: " + player.getHand().stream()
                    .map(card -> card.getName() + "(" + card.getDimension().getIcon() +
                            (card.isExhausted() ? "💤" : "✨") + ")")
                    .toList());
        }

        System.out.println("Vortexes: " + gameState.getVortexCount() + "/4");
        System.out.println("Active Missions: " + gameState.getActiveMissions().size());

        int totalDuplicates = 0;
        for (Era era : Era.values()) {
            int duplicateCount = gameState.getDuplicateCount(era);
            totalDuplicates += duplicateCount;
        }
        if (totalDuplicates > 0) {
            System.out.println("Total Duplicates: " + totalDuplicates);
        }

        if (gameState.isGameOver()) {
            System.out.println("\n" + gameState.getGameResult().getMessage());
        }
    }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public boolean isGameOver() { return gameState.isGameOver(); }
    public boolean isWaitingForPlayerInput() { return waitingForPlayerInput; }
    public CardAcquisitionManager getCardAcquisitionManager() { return cardAcquisitionManager; }
}