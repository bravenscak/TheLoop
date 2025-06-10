package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.*;
import hr.algebra.theloop.networking.NetworkManager;
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
    private final NetworkManager networkManager;

    private int localPlayerIndex = 0;

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
        this.networkManager = new NetworkManager(this::handleNetworkUpdate);
    }

    public void setPlayerMode(PlayerMode playerMode) {
        networkManager.setPlayerMode(playerMode);
    }

    public void setupMultiplayerPlayers(PlayerMode playerMode) {
        if (playerMode == PlayerMode.SINGLE_PLAYER) {
            playerManager.addPlayer("Time Agent Bruno", Era.DAWN_OF_TIME);
            localPlayerIndex = 0;
        } else {
            playerManager.addPlayer("Time Agent Bruno", Era.DAWN_OF_TIME);
            playerManager.addPlayer("Time Agent Alice", Era.MEDIEVAL);

            if (playerMode == PlayerMode.PLAYER_ONE) {
                localPlayerIndex = 0;
                GameLogger.gameFlow("🎮 You control: Time Agent Bruno");
            } else {
                localPlayerIndex = 1;
                GameLogger.gameFlow("🎮 You control: Time Agent Alice");
            }
        }
    }

    public void startGame() {
        if (playerManager.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        cardAcquisitionManager.initializeAvailableCards(gameState);
        missionManager.initializeMissions(gameState);
        playerManager.setupInitialPlayer();

        if (networkManager.isMultiplayer()) {
            turnManager.setWaitingForPlayerInput(true);
            GameLogger.gameFlow("🎮 Multiplayer mode - both players can act anytime");
        } else {
            turnManager.startPlayerTurn();
        }

        GameLogger.gameFlow("Game started with " + playerManager.getPlayers().size() + " players");
    }

    public void processTurn() {
        if (gameState.isGameOver()) return;

        turnManager.processDrFooTurn(drFooAI, gameState, cardAcquisitionManager);
        broadcastGameState("Dr. Foo Turn", "Dr. Foo");
    }

    public void endPlayerTurn() {
        if (!isWaitingForPlayerInput()) return;

        if (networkManager.isMultiplayer()) {
            processTurn();
        } else {
            String currentPlayerName = getCurrentPlayer().getName();
            turnManager.endPlayerTurn(playerManager.getPlayers(), gameState);
            broadcastGameState("End Turn", currentPlayerName);
        }
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
        if (!isLocalPlayer(player)) {
            GameLogger.warning("Cannot control other player's actions!");
            return false;
        }

        boolean success = playerActionManager.playCard(player, cardIndex, targetEra);

        if (success) {
            checkGameEndConditions();
            String action = "Played card " + cardIndex + " at " + targetEra.getDisplayName();
            broadcastGameState(action, player.getName());
        }

        return success;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        if (!isLocalPlayer(player)) {
            GameLogger.warning("Cannot control other player's movement!");
            return false;
        }

        boolean success = playerActionManager.movePlayer(player, targetEra);

        if (success) {
            String action = "Moved to " + targetEra.getDisplayName();
            broadcastGameState(action, player.getName());
        }

        return success;
    }

    public boolean acquireCard(Player player) {
        if (!isLocalPlayer(player)) {
            GameLogger.warning("Cannot control other player's card acquisition!");
            return false;
        }

        boolean success = playerActionManager.acquireCard(player);

        if (success) {
            broadcastGameState("Acquired card", player.getName());
        }

        return success;
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

        if (networkManager.isMultiplayer()) {
            turnManager.setWaitingForPlayerInput(true);
        } else {
            turnManager.setWaitingForPlayerInput(!loadedState.isGameOver());
        }

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

    private void broadcastGameState(String lastAction, String playerName) {
        if (networkManager.isEnabled()) {
            gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
            networkManager.sendGameState(gameState, lastAction, playerName);
        }
    }

    private void handleNetworkUpdate(NetworkGameState networkState) {
        try {
            networkState.applyToGameState(this.gameState);

            if (networkState.getPlayerStates() != null && !networkState.getPlayerStates().isEmpty()) {
                playerManager.restorePlayersFromStates(
                        networkState.getPlayerStates(),
                        networkState.getCurrentPlayerIndex()
                );
            }

            recalculateDuplicatesInBag(this.gameState);

            GameLogger.gameFlow("🔄 Synced: " + networkState.getLastAction() + " by " + networkState.getLastPlayerName());

        } catch (Exception e) {
            GameLogger.error("Failed to apply network update: " + e.getMessage());
        }
    }

    private boolean isLocalPlayer(Player player) {
        if (networkManager.getPlayerMode() == PlayerMode.SINGLE_PLAYER) {
            return true;
        }

        int playerIndex = playerManager.getPlayers().indexOf(player);
        return playerIndex == localPlayerIndex;
    }

    public void addPlayer(String name, Era startingEra) {
        playerManager.addPlayer(name, startingEra);
    }

    public void shutdown() {
        networkManager.stop();
    }

    public Player getCurrentPlayer() { return playerManager.getCurrentPlayer(); }
    public Player getLocalPlayer() {
        if (playerManager.getPlayers().size() > localPlayerIndex) {
            return playerManager.getPlayers().get(localPlayerIndex);
        }
        return getCurrentPlayer();
    }
    public boolean isGameOver() { return gameState.isGameOver(); }
    public boolean isWaitingForPlayerInput() {
        return networkManager.isMultiplayer() ? !gameState.isGameOver() : turnManager.isWaitingForPlayerInput();
    }
    public int getDuplicatesInBag() { return duplicatesInBag; }
    public MissionManager getMissionManager() { return missionManager; }
    public boolean isMultiplayer() { return networkManager.isMultiplayer(); }
    public PlayerMode getPlayerMode() { return networkManager.getPlayerMode(); }
    public int getLocalPlayerIndex() { return localPlayerIndex; }

    public int getTotalDuplicatesOnBoard() {
        return (int) java.util.Arrays.stream(Era.values())
                .mapToLong(era -> gameState.getDuplicateCount(era)).sum();
    }
}