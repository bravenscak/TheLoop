package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.*;
import hr.algebra.theloop.networking.NetworkManager;
import hr.algebra.theloop.utils.GameLogger;
import javafx.application.Platform;

public class NetworkCoordinator {

    private final NetworkManager networkManager;
    private final int localPlayerIndex;
    private Runnable uiUpdateCallback;

    public NetworkCoordinator(NetworkManager networkManager, int localPlayerIndex) {
        this.networkManager = networkManager;
        this.localPlayerIndex = localPlayerIndex;
    }

    public void setUIUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    public void broadcastGameState(GameState gameState, PlayerManager playerManager, String lastAction, String playerName) {
        if (networkManager.isEnabled()) {
            gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
            networkManager.sendGameState(gameState, lastAction, playerName);
        }
    }

    public void broadcastCompleteGameState(GameState gameState, PlayerManager playerManager, String action, String playerName) {
        if (networkManager.isEnabled()) {
            for (Player player : playerManager.getPlayers()) {
                gameState.savePlayerState(player);
            }
            gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
            networkManager.sendGameState(gameState, action, playerName);
        }
    }

    public void handleNetworkUpdate(GameState gameState, PlayerManager playerManager, NetworkGameState networkState) {
        try {
            networkState.applyToGameState(gameState);

            if (networkState.getPlayerStates() != null && !networkState.getPlayerStates().isEmpty()) {
                playerManager.restorePlayersFromStates(
                        networkState.getPlayerStates(),
                        networkState.getCurrentPlayerIndex()
                );
            }

            if (uiUpdateCallback != null) {
                Platform.runLater(uiUpdateCallback);
            }

            GameLogger.gameFlow("🔄 Network update applied - " + networkState.getLastAction());

        } catch (Exception e) {
            GameLogger.error("Failed to apply network update: " + e.getMessage());
        }
    }

    public void requestMissionSync(Player localPlayer, String reason) {
        if (networkManager.isEnabled() && localPlayerIndex != 0) {
            GameLogger.gameFlow("🎮 Player 2: Requesting mission sync - " + reason);
        }
    }

    public boolean shouldBroadcast() {
        return !networkManager.isMultiplayer() || localPlayerIndex == 0;
    }

    public boolean isMultiplayerHost() {
        return networkManager.isMultiplayer() && localPlayerIndex == 0;
    }

    public boolean isMultiplayerClient() {
        return networkManager.isMultiplayer() && localPlayerIndex != 0;
    }

    public void scheduleInitialBroadcast(Runnable broadcastAction) {
        if (isMultiplayerHost()) {
            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(2000);
                    broadcastAction.run();
                    GameLogger.gameFlow("🎮 Player 1: Broadcasted initial missions to all players");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }
}