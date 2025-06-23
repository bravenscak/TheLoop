package hr.algebra.theloop.engine;

import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.model.*;
import hr.algebra.theloop.networking.NetworkManager;
import hr.algebra.theloop.utils.GameLogger;

import java.util.Random;

public class GameEngine {
    private static final int MAX_DUPLICATES_IN_BAG = 28;
    private static final String TIME_AGENT_BRUNO = "Time Agent Bruno";
    private static final String TIME_AGENT_ALICE = "Time Agent Alice";

    private GameState gameState;
    private PlayerManager playerManager;
    private int duplicatesInBag;

    private final TurnManager turnManager;
    private final DrFooAI drFooAI;
    private final MissionManager missionManager;
    private final CardAcquisitionManager cardAcquisitionManager;
    private final PlayerActionManager playerActionManager;
    private final NetworkCoordinator networkCoordinator;
    private final ConfigurationManager configManager;

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

        NetworkManager networkManager = new NetworkManager(this::handleNetworkUpdate);
        this.networkCoordinator = new NetworkCoordinator(networkManager, localPlayerIndex);
    }

    public void setPlayerMode(PlayerMode playerMode) {
        networkCoordinator.getNetworkManager().setPlayerMode(playerMode);
        playerActionManager.setPlayerMode(playerMode, localPlayerIndex);
    }

    public void setupMultiplayerPlayers(PlayerMode playerMode) {
        if (playerMode == PlayerMode.SINGLE_PLAYER) {
            playerManager.addPlayer(TIME_AGENT_BRUNO, Era.DAWN_OF_TIME);
            localPlayerIndex = 0;
        } else {
            playerManager.addPlayer(TIME_AGENT_BRUNO, Era.DAWN_OF_TIME);
            playerManager.addPlayer(TIME_AGENT_ALICE, Era.MEDIEVAL);
            localPlayerIndex = (playerMode == PlayerMode.PLAYER_ONE) ? 0 : 1;
        }
    }

    public void startGame() {
        validatePlayersSetup();
        initializeGameComponents();
        setupMultiplayerMode();
    }

    private void validatePlayersSetup() {
        if (playerManager.isEmpty()) {
            throw new IllegalStateException("No players added!");
        }
    }

    private void initializeGameComponents() {
        cardAcquisitionManager.initializeAvailableCards(gameState);

        if (networkCoordinator.shouldBroadcast()) {
            missionManager.initializeMissions(gameState);
            GameLogger.gameFlow("🎮 Player 1: Initialized missions");
        }

        playerManager.setupInitialPlayer();
    }

    private void setupMultiplayerMode() {
        turnManager.setupMultiplayerMode(networkCoordinator.getNetworkManager(), localPlayerIndex);

        networkCoordinator.scheduleInitialBroadcast(() ->
                networkCoordinator.broadcastCompleteGameState(gameState, playerManager, "Missions Initialized", "System")
        );
    }

    public void processTurn() {
        if (gameState.isGameOver()) return;

        turnManager.processMultiplayerTurn(playerManager, gameState, drFooAI, cardAcquisitionManager,
                networkCoordinator.getNetworkManager(), localPlayerIndex);
        networkCoordinator.broadcastGameState(gameState, playerManager, "Dr. Foo Turn", "Dr. Foo");
    }

    public void endPlayerTurn() {
        turnManager.endMultiplayerTurn(playerManager, gameState, networkCoordinator.getNetworkManager(), localPlayerIndex);

        if (networkCoordinator.isMultiplayerHost()) {
            processTurn();
            networkCoordinator.broadcastCompleteGameState(gameState, playerManager, "Dr. Foo Turn Complete", "System");
        } else if (!isMultiplayer()) {
            networkCoordinator.broadcastGameState(gameState, playerManager, "End Turn", getCurrentPlayer().getName());
        }
    }

    public void saveGame() {
        gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
    }

    public boolean spawnDuplicate(Era era) {
        if (duplicatesInBag <= 0) return false;

        boolean success = playerActionManager.spawnDuplicate(era, duplicatesInBag);
        if (success) {
            duplicatesInBag--;
            if (networkCoordinator.getNetworkManager().isEnabled()) {
                networkCoordinator.broadcastGameState(gameState, playerManager, "Duplicate spawned at " + era.getDisplayName(), "Dr. Foo");
            }
        }
        return success;
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        boolean success = playerActionManager.playCard(player, cardIndex, targetEra);

        if (success) {
            handleMissionSync();
            checkGameEndConditions();
            broadcastCardAction(player);
        }

        return success;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        boolean success = playerActionManager.movePlayer(player, targetEra);

        if (success) {
            handleMissionSync();
            checkGameEndConditions();
            broadcastMovementAction(player, targetEra);
        }

        return success;
    }

    private void handleMissionSync() {
        if (missionManager.shouldRequestSync(gameState)) {
            GameLogger.warning("🎮 Player 2: No missions available, requesting sync");
            networkCoordinator.requestMissionSync(getLocalPlayer(), "Need mission sync");
        }
    }

    private void checkGameEndConditions() {
        missionManager.checkGameEndConditions(gameState, configManager);
    }

    private void broadcastCardAction(Player player) {
        if (networkCoordinator.shouldBroadcast()) {
            networkCoordinator.broadcastGameState(gameState, playerManager, "Played card", player.getName());
        } else {
            networkCoordinator.requestMissionSync(getLocalPlayer(), "Card played by " + player.getName());
        }
    }

    private void broadcastMovementAction(Player player, Era targetEra) {
        if (networkCoordinator.shouldBroadcast()) {
            networkCoordinator.broadcastGameState(gameState, playerManager, "Moved to " + targetEra.getDisplayName(), player.getName());
        } else {
            networkCoordinator.requestMissionSync(getLocalPlayer(), "Movement by " + player.getName());
        }
    }

    public void restoreFromGameState(GameState loadedState) {
        validateLoadedState(loadedState);

        this.gameState = loadedState;
        restorePlayerStates(loadedState);
        configureTurnManager();
        recalculateDuplicates();
    }

    private void validateLoadedState(GameState loadedState) {
        if (loadedState == null) {
            throw new IllegalArgumentException("Cannot restore from null GameState");
        }
    }

    private void restorePlayerStates(GameState loadedState) {
        if (loadedState.hasPlayerStates()) {
            playerManager.restorePlayersFromStates(
                    loadedState.getPlayerStates(),
                    loadedState.getCurrentPlayerIndex()
            );
        } else if (playerManager.isEmpty()) {
            playerManager.addPlayer(TIME_AGENT_BRUNO, loadedState.getDrFooPosition().getPrevious());
        }
    }

    private void configureTurnManager() {
        if (isMultiplayer()) {
            turnManager.setWaitingForPlayerInput(true);
        } else {
            turnManager.setWaitingForPlayerInput(!gameState.isGameOver());
        }
    }

    private void recalculateDuplicates() {
        this.duplicatesInBag = gameState.recalculateDuplicatesInBag();
    }

    public void broadcastGameState(String lastAction, String playerName) {
        networkCoordinator.broadcastGameState(gameState, playerManager, lastAction, playerName);
    }

    public void broadcastCompleteGameState(String action, String playerName) {
        networkCoordinator.broadcastCompleteGameState(gameState, playerManager, action, playerName);
    }

    private void handleNetworkUpdate(NetworkGameState networkState) {
        networkCoordinator.handleNetworkUpdate(gameState, playerManager, networkState);
        this.duplicatesInBag = gameState.recalculateDuplicatesInBag();
    }

    public void shutdown() {
        networkCoordinator.getNetworkManager().stop();
    }

    public void setUIUpdateCallback(Runnable callback) {
        playerActionManager.setUIUpdateCallback(callback);
        networkCoordinator.setUIUpdateCallback(callback);
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
        return isMultiplayer() ? !gameState.isGameOver() : turnManager.isWaitingForPlayerInput();
    }

    public boolean isMultiplayer() { return networkCoordinator.getNetworkManager().isMultiplayer(); }
    public PlayerMode getPlayerMode() { return networkCoordinator.getNetworkManager().getPlayerMode(); }
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