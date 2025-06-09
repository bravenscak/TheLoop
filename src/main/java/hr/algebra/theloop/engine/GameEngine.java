package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.*;
import hr.algebra.theloop.utils.GameLogger;
import lombok.Data;

import java.util.Random;

@Data
public class GameEngine {
    private static final int MAX_DUPLICATES_IN_BAG = 28;

    private GameState gameState;
    private PlayerManager playerManager;
    private int duplicatesInBag;

    private final TurnManager turnManager;
    private final DrFooAI drFooAI;
    private final MissionManager missionManager;
    private final CardAcquisitionManager cardAcquisitionManager;
    private final PlayerActionManager playerActionManager;

    public GameEngine() {
        this.gameState = new GameState();
        this.playerManager = new PlayerManager();
        this.duplicatesInBag = MAX_DUPLICATES_IN_BAG;

        Random random = new Random();
        this.turnManager = new TurnManager();
        this.drFooAI = new DrFooAI(random, this);
        this.missionManager = new MissionManager(random);
        this.cardAcquisitionManager = new CardAcquisitionManager(random);
        this.playerActionManager = new PlayerActionManager(gameState, missionManager, cardAcquisitionManager);
    }

    public void addPlayer(String name, Era startingEra) {
        playerManager.addPlayer(name, startingEra);
    }

    public void startGame() {
        if (playerManager.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        cardAcquisitionManager.initializeAvailableCards(gameState);
        missionManager.initializeMissions(gameState);
        playerManager.setupInitialPlayer();
        turnManager.startPlayerTurn();

        GameLogger.gameFlow("Game started with " + playerManager.getPlayers().size() + " players");
    }

    public void processTurn() {
        if (gameState.isGameOver()) return;
        turnManager.processDrFooTurn(drFooAI, gameState, cardAcquisitionManager);
    }

    public void endPlayerTurn() {
        if (!isWaitingForPlayerInput()) return;
        turnManager.endPlayerTurn(playerManager.getPlayers(), gameState);
    }

    public void saveGame() {
        gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
    }

    public boolean spawnDuplicate(Era era) {
        if (duplicatesInBag <= 0) return false;

        Duplicate newDuplicate = new Duplicate(era);
        gameState.addDuplicate(era, newDuplicate);
        duplicatesInBag--;
        return true;
    }

    public void checkGameEndConditions() {
        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
        } else if (gameState.getVortexCount() >= 3) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
        } else if (gameState.getCurrentCycle() > 3) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
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

    public void restoreFromGameState(GameState loadedState) {
        if (loadedState == null) {
            throw new IllegalArgumentException("Cannot restore from null GameState");
        }

        this.gameState = loadedState;

        if (loadedState.hasPlayerStates()) {
            playerManager.restorePlayersFromStates(
                    loadedState.getPlayerStates(),
                    loadedState.getCurrentPlayerIndex()
            );
        } else if (playerManager.isEmpty()) {
            addPlayer("Time Agent Bruno", loadedState.getDrFooPosition().getPrevious());
        }

        turnManager.setWaitingForPlayerInput(!loadedState.isGameOver());

        recalculateDuplicatesInBag(loadedState);

        restoreAvailableCards();
    }

    private void recalculateDuplicatesInBag(GameState loadedState) {
        this.duplicatesInBag = MAX_DUPLICATES_IN_BAG;
        for (Era era : Era.values()) {
            this.duplicatesInBag -= loadedState.getDuplicateCount(era);
        }
        this.duplicatesInBag = Math.max(0, this.duplicatesInBag);
    }

    private void restoreAvailableCards() {
        cardAcquisitionManager.clearAllCards();
        for (Era era : Era.values()) {
            if (!gameState.hasVortex(era)) {
                cardAcquisitionManager.addCardToEra(era,
                        hr.algebra.theloop.cards.CardFactory.createRandomCard());
            }
        }
    }

    public Player getCurrentPlayer() { return playerManager.getCurrentPlayer(); }
    public boolean isGameOver() { return gameState.isGameOver(); }
    public boolean isWaitingForPlayerInput() { return turnManager.isWaitingForPlayerInput(); }
    public int getDuplicatesInBag() { return duplicatesInBag; }
    public MissionManager getMissionManager() { return missionManager; }

    public int getTotalDuplicatesOnBoard() {
        return (int) java.util.Arrays.stream(Era.values())
                .mapToLong(era -> gameState.getDuplicateCount(era)).sum();
    }
}