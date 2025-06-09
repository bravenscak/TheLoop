package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.*;
import hr.algebra.theloop.model.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class GameEngine {
    private static final int MAX_DUPLICATES_IN_BAG = 28;

    private GameState gameState;
    private List<Player> players;
    private Random random;
    private int currentPlayerIndex;
    private boolean waitingForPlayerInput;
    private int duplicatesInBag;

    private final DrFooAI drFooAI;
    private final MissionManager missionManager;
    private final CardAcquisitionManager cardAcquisitionManager;

    public GameEngine() {
        this.gameState = new GameState();
        this.players = new ArrayList<>();
        this.random = new Random();
        this.currentPlayerIndex = 0;
        this.waitingForPlayerInput = false;
        this.duplicatesInBag = MAX_DUPLICATES_IN_BAG;

        this.drFooAI = new DrFooAI(random, this);
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
    }

    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        cardAcquisitionManager.initializeAvailableCards(gameState);
        missionManager.initializeMissions(gameState);
        getCurrentPlayer().setCurrentPlayer(true);
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
        card.exhaust();
        System.out.println("✅ Played: " + card.getName() + " [EXHAUSTED]");

        missionManager.checkAllMissions(gameState, player, card.getClass().getSimpleName());
        checkGameEndConditions();

        return true;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        if (gameState.isGameOver() || !waitingForPlayerInput) {
            return false;
        }

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

        System.out.println("🔧 DEBUG: endPlayerTurn() started");
        waitingForPlayerInput = false;

        for (Player player : players) {
            System.out.println("🔧 DEBUG: Player hand before: " + player.getHand().stream().map(ArtifactCard::getName).toList());
            System.out.println("🔧 DEBUG: Player deck size: " + player.getDeckSize());
            System.out.println("🔧 DEBUG: Player discard size: " + player.getDiscardPileSize());

            // Move all cards to discard
            List<ArtifactCard> handCopy = new ArrayList<>(player.getHand());
            for (ArtifactCard card : handCopy) {
                player.getDiscardPile().add(card);
                System.out.println("🔧 DEBUG: Discarded " + card.getName());
            }
            player.getHand().clear();

            System.out.println("🔧 DEBUG: Hand cleared, discard size now: " + player.getDiscardPileSize());

            player.rechargeBatteries();

            // Draw 3 new cards
            for (int i = 0; i < 3; i++) {
                System.out.println("🔧 DEBUG: Attempting to draw card " + (i+1));
                player.drawCard();
            }

            System.out.println("🔧 DEBUG: Player hand after: " + player.getHand().stream().map(ArtifactCard::getName).toList());
        }

        gameState.nextTurn();
        printGameStatus();
        System.out.println("🔄 Player turn ended. Click 'End Turn' for Dr. Foo phase.");
    }

    public boolean spawnDuplicate(Era era) {
        if (duplicatesInBag <= 0) {
            System.out.println("🚫 Cannot spawn duplicate - bag is empty!");
            return false;
        }

        Duplicate newDuplicate = new Duplicate(era);
        gameState.addDuplicate(era, newDuplicate);
        duplicatesInBag--;

        System.out.println("🔵 Duplicate spawned at " + era.getDisplayName() +
                " (destroy at " + newDuplicate.getDestroyEra().getDisplayName() +
                ") [Bag: " + duplicatesInBag + "/" + MAX_DUPLICATES_IN_BAG + "]");
        return true;
    }

    public void destroyDuplicate(Duplicate duplicate) {
        duplicatesInBag++;
        System.out.println("💥 Duplicate destroyed - returned to bag [Bag: " + duplicatesInBag + "/" + MAX_DUPLICATES_IN_BAG + "]");
    }

    private void checkGameEndConditions() {
        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            return;
        }

        if (gameState.getVortexCount() >= 3) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
            return;
        }

        if (gameState.getCurrentCycle() > 3) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
            return;
        }
    }

    public void printGameStatus() {
        System.out.println("\n=== GAME STATUS ===");
        System.out.println(gameState.toString());
        System.out.println("🎒 Duplicates in bag: " + duplicatesInBag + "/" + MAX_DUPLICATES_IN_BAG);

        for (Player player : players) {
            System.out.println("  " + player.toString());
        }

        System.out.println("Vortexes: " + gameState.getVortexCount() + "/3");
        System.out.println("Active Missions: " + gameState.getActiveMissions().size());

        int totalDuplicates = getTotalDuplicatesOnBoard();
        if (totalDuplicates > 0) {
            System.out.println("Total Duplicates: " + totalDuplicates);
        }

        if (gameState.isGameOver()) {
            System.out.println("\n" + gameState.getGameResult().getMessage());
        }
    }

    public int getTotalDuplicatesOnBoard() {
        int total = 0;
        for (Era era : Era.values()) {
            total += gameState.getDuplicateCount(era);
        }
        return total;
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

    public int getDuplicatesInBag() {
        return duplicatesInBag;
    }
}