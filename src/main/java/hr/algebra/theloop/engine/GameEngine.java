package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.*;
import hr.algebra.theloop.model.*;
import hr.algebra.theloop.utils.GameLogger;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class GameEngine {
    private static final int MAX_DUPLICATES_IN_BAG = 28;

    private GameState gameState;
    private List<Player> players;
    private int currentPlayerIndex;
    private int duplicatesInBag;

    private final TurnManager turnManager;
    private final DrFooAI drFooAI;
    private final MissionManager missionManager;
    private final CardAcquisitionManager cardAcquisitionManager;
    private final PlayerActionManager playerActionManager;

    public GameEngine() {
        this.gameState = new GameState();
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.duplicatesInBag = MAX_DUPLICATES_IN_BAG;

        Random random = new Random();
        this.turnManager = new TurnManager();
        this.drFooAI = new DrFooAI(random, this);
        this.missionManager = new MissionManager(random);
        this.cardAcquisitionManager = new CardAcquisitionManager(random);
        this.playerActionManager = new PlayerActionManager(gameState, missionManager, cardAcquisitionManager);
    }

    public void addPlayer(String name, Era startingEra) {
        Player player = new Player(name, startingEra);
        players.add(player);
        giveStartingCards(player);
        GameLogger.gameFlow("Added player: " + name + " @ " + startingEra.getDisplayName());
    }

    private void giveStartingCards(Player player) {
        List<ArtifactCard> allStartingCards = CardFactory.createRandomStartingDeck();

        for (int i = 0; i < Math.min(3, allStartingCards.size()); i++) {
            ArtifactCard card = allStartingCards.get(i);
            card.ready();
            player.addCardToHand(card);
        }

        for (int i = 3; i < allStartingCards.size(); i++) {
            ArtifactCard card = allStartingCards.get(i);
            card.ready();
            player.addCardToDeck(card);
        }
    }

    public void startGame() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        cardAcquisitionManager.initializeAvailableCards(gameState);
        missionManager.initializeMissions(gameState);
        getCurrentPlayer().setCurrentPlayer(true);
        turnManager.startPlayerTurn();

        GameLogger.gameFlow("Game started with " + players.size() + " players");
    }

    public void processTurn() {
        if (gameState.isGameOver()) return;
        turnManager.processDrFooTurn(drFooAI, gameState, cardAcquisitionManager);
    }

    public void endPlayerTurn() {
        if (!isWaitingForPlayerInput()) return;

        GameLogger.gameFlow("Turn " + gameState.getTurnNumber() + " ended");
        turnManager.endPlayerTurn(players, gameState);
    }

    public boolean spawnDuplicate(Era era) {
        if (duplicatesInBag <= 0) return false;

        Duplicate newDuplicate = new Duplicate(era);
        gameState.addDuplicate(era, newDuplicate);
        duplicatesInBag--;
        return true;
    }

    public void destroyDuplicate(Duplicate duplicate) {
        duplicatesInBag++;
        GameLogger.debug("Duplicate returned to bag (" + duplicatesInBag + "/28)");
    }

    public void checkGameEndConditions() {
        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            GameLogger.gameEnd("VICTORY - 4 missions completed!");
        } else if (gameState.getVortexCount() >= 3) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
            GameLogger.gameEnd("DEFEAT - Too many vortexes!");
        } else if (gameState.getCurrentCycle() > 3) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
            GameLogger.gameEnd("DEFEAT - Dr. Foo completed his plan!");
        }
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        boolean success = playerActionManager.playCard(player, cardIndex, targetEra);
        if (success) checkGameEndConditions();
        return success;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        return playerActionManager.movePlayer(player, targetEra);
    }

    public boolean acquireCard(Player player) {
        return playerActionManager.acquireCard(player);
    }

    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public boolean isGameOver() { return gameState.isGameOver(); }
    public boolean isWaitingForPlayerInput() { return turnManager.isWaitingForPlayerInput(); }
    public int getDuplicatesInBag() { return duplicatesInBag; }
    public MissionManager getMissionManager() { return missionManager; }

    public int getTotalDuplicatesOnBoard() {
        return Era.values().length - (int) java.util.Arrays.stream(Era.values())
                .mapToLong(era -> gameState.getDuplicateCount(era)).sum();
    }

    public void restoreFromGameState(GameState loadedState) {
        if (loadedState == null) {
            throw new IllegalArgumentException("Cannot restore from null GameState");
        }

        GameLogger.debug("Restoring game state from save file...");

        this.gameState = loadedState;

        if (this.players.isEmpty()) {
            addPlayer("Time Agent Bruno", loadedState.getDrFooPosition().getPrevious());
            GameLogger.debug("Created default player for loaded game");
        }

        if (!players.isEmpty()) {
            getCurrentPlayer().setCurrentPlayer(true);
        }

        if (loadedState.isGameOver()) {
            turnManager.setWaitingForPlayerInput(false);
            GameLogger.debug("Game over state - not waiting for input");
        } else {
            turnManager.setWaitingForPlayerInput(true);
            GameLogger.debug("Game active - waiting for player input");
        }

        this.duplicatesInBag = MAX_DUPLICATES_IN_BAG;
        for (Era era : Era.values()) {
            this.duplicatesInBag -= loadedState.getDuplicateCount(era);
        }

        if (this.duplicatesInBag < 0) {
            this.duplicatesInBag = 0;
            GameLogger.warning("Duplicate count was negative, reset to 0");
        }

        cardAcquisitionManager.clearAllCards();

        for (Era era : Era.values()) {
            if (!loadedState.hasVortex(era)) {
                cardAcquisitionManager.addCardToEra(era, hr.algebra.theloop.cards.CardFactory.createRandomCard());
            }
        }

        GameLogger.gameFlow("Game state restored successfully:");
        GameLogger.gameFlow("  Turn: " + loadedState.getTurnNumber());
        GameLogger.gameFlow("  Dr. Foo: " + loadedState.getDrFooPosition().getDisplayName());
        GameLogger.gameFlow("  Cycle: " + loadedState.getCurrentCycle() + "/3");
        GameLogger.gameFlow("  Missions: " + loadedState.getTotalMissionsCompleted() + "/4");
        GameLogger.gameFlow("  Vortexes: " + loadedState.getVortexCount() + "/3");
        GameLogger.gameFlow("  Duplicates in bag: " + this.duplicatesInBag + "/28");

        if (loadedState.isGameOver()) {
            GameLogger.gameFlow("Loaded game was already finished: " + loadedState.getGameResult().getMessage());
        }
    }
}