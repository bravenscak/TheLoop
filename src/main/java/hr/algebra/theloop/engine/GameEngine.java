package hr.algebra.theloop.engine;

import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.model.*;
import hr.algebra.theloop.networking.NetworkManager;
import hr.algebra.theloop.utils.GameLogger;
import javafx.application.Platform;

import java.util.Random;

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
    private final ConfigurationManager configManager;
    private Runnable uiUpdateCallback;

    private int localPlayerIndex = 0;

    public GameEngine() {
        this.configManager = ConfigurationManager.getInstance();
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
            localPlayerIndex = (playerMode == PlayerMode.PLAYER_ONE) ? 0 : 1;
        }
    }

    public void startGame() {
        if (playerManager.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }

        cardAcquisitionManager.initializeAvailableCards(gameState);

        if (!networkManager.isMultiplayer() || localPlayerIndex == 0) {
            missionManager.initializeMissions(gameState);
            GameLogger.gameFlow("🎮 Player 1: Initialized missions");
        }

        playerManager.setupInitialPlayer();

        if (networkManager.isMultiplayer()) {
            turnManager.setWaitingForPlayerInput(true);

            if (localPlayerIndex == 0) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        broadcastCompleteGameState("Missions Initialized", "System");
                        GameLogger.gameFlow("🎮 Player 1: Broadcasted initial missions to all players");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        } else {
            turnManager.startPlayerTurn();
        }
    }

    public void processTurn() {
        if (gameState.isGameOver()) return;

        if (!networkManager.isMultiplayer() || localPlayerIndex == 0) {
            turnManager.processDrFooTurn(drFooAI, gameState, cardAcquisitionManager);
            GameLogger.gameFlow("🎮 Player 1: Processed Dr. Foo turn");
        }

        broadcastGameState("Dr. Foo Turn", "Dr. Foo");
    }

    public void endPlayerTurn() {
        if (!isWaitingForPlayerInput()) return;

        if (networkManager.isMultiplayer()) {
            if (localPlayerIndex != 0) {
                GameLogger.warning("Only Player 1 can end turn in multiplayer!");
                return;
            }

            playerManager.endPlayerTurns();
            saveGame();
            processTurn();
            broadcastCompleteGameState("Dr. Foo Turn Complete", "System");
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

        if (networkManager.isEnabled()) {
            broadcastGameState("Duplicate spawned at " + era.getDisplayName(), "Dr. Foo");
        }

        return true;
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (!isLocalPlayer(player)) {
            return false;
        }

        boolean success = playerActionManager.playCard(player, cardIndex, targetEra);

        if (success) {
            if (uiUpdateCallback != null) {
                Platform.runLater(uiUpdateCallback);
            }

            if (missionManager.needsMissionSync(gameState)) {
                GameLogger.warning("🎮 Player 2: No missions available, requesting sync");
                requestMissionSync("Need mission sync for card play");
                return success;
            }

            checkGameEndConditions();

            if (!networkManager.isMultiplayer() || localPlayerIndex == 0) {
                broadcastGameState("Played card", player.getName());
            } else {
                requestMissionSync("Card played by " + player.getName());
            }
        }

        return success;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        if (!isLocalPlayer(player)) {
            return false;
        }

        boolean success = playerActionManager.movePlayer(player, targetEra);

        if (success) {
            if (uiUpdateCallback != null) {
                Platform.runLater(uiUpdateCallback);
            }

            if (missionManager.needsMissionSync(gameState)) {
                GameLogger.warning("🎮 Player 2: No missions available, requesting sync");
                requestMissionSync("Need mission sync for movement");
                return success;
            }

            checkGameEndConditions();

            if (!networkManager.isMultiplayer() || localPlayerIndex == 0) {
                broadcastGameState("Moved to " + targetEra.getDisplayName(), player.getName());
            } else {
                requestMissionSync("Movement by " + player.getName());
            }
        }

        return success;
    }

    private void requestMissionSync(String reason) {
        if (networkManager.isEnabled() && localPlayerIndex != 0) {
            GameLogger.gameFlow("🎮 Player 2: Requesting mission sync - " + reason);
            broadcastGameState(reason, getLocalPlayer().getName());
        }
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
            playerManager.addPlayer("Time Agent Bruno", loadedState.getDrFooPosition().getPrevious());
        }

        if (networkManager.isMultiplayer()) {
            turnManager.setWaitingForPlayerInput(true);
        } else {
            turnManager.setWaitingForPlayerInput(!loadedState.isGameOver());
        }

        this.duplicatesInBag = gameState.recalculateDuplicatesInBag();
    }

    public void broadcastGameState(String lastAction, String playerName) {
        if (networkManager.isEnabled()) {
            gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
            networkManager.sendGameState(gameState, lastAction, playerName);
        }
    }

    public void broadcastCompleteGameState(String action, String playerName) {
        if (networkManager.isEnabled()) {
            for (Player player : playerManager.getPlayers()) {
                gameState.savePlayerState(player);
            }
            gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
            networkManager.sendGameState(gameState, action, playerName);
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

            this.duplicatesInBag = gameState.recalculateDuplicatesInBag();

            if (uiUpdateCallback != null) {
                Platform.runLater(uiUpdateCallback);
            }

            GameLogger.gameFlow("🔄 Network update applied - " + networkState.getLastAction());

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

    private void checkGameEndConditions() {
        missionManager.checkAllMissions(gameState, getCurrentPlayer(), "EndConditionsCheck");

        if (gameState.getTotalMissionsCompleted() >= configManager.getMissionsToWin()) {
            gameState.endGame(GameResult.VICTORY);
            GameLogger.success("🎉 VICTORY! Completed " + configManager.getMissionsToWin() + " missions!");
        } else if (gameState.getVortexCount() >= configManager.getMaxVortexes()) {
            gameState.endGame(GameResult.DEFEAT_VORTEXES);
            GameLogger.error("💀 DEFEAT! " + configManager.getMaxVortexes() + " vortexes opened!");
        } else if (gameState.getCurrentCycle() > configManager.getMaxCycles()) {
            gameState.endGame(GameResult.DEFEAT_CYCLES);
            GameLogger.error("💀 DEFEAT! Dr. Foo completed " + configManager.getMaxCycles() + " cycles!");
        }
    }

    public void shutdown() {
        networkManager.stop();
    }

    public void setUIUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
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
    public boolean isMultiplayer() { return networkManager.isMultiplayer(); }
    public PlayerMode getPlayerMode() { return networkManager.getPlayerMode(); }
    public int getLocalPlayerIndex() { return localPlayerIndex; }

    public int getTotalDuplicatesOnBoard() {
        return (int) java.util.Arrays.stream(Era.values())
                .mapToLong(era -> gameState.getDuplicateCount(era)).sum();
    }

    public GameState getGameState() { return gameState; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public int getDuplicatesInBag() { return duplicatesInBag; }
    public MissionManager getMissionManager() { return missionManager; }
    public ConfigurationManager getConfigManager() { return configManager; }
}