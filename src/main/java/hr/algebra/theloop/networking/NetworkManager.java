package hr.algebra.theloop.networking;

import hr.algebra.theloop.model.*;
import hr.algebra.theloop.utils.GameLogger;

import java.util.function.Consumer;

public class NetworkManager {

    private PlayerMode playerMode = PlayerMode.SINGLE_PLAYER;
    private GameServer gameServer;
    private Thread serverThread;
    private volatile boolean enabled = false;
    private final Consumer<NetworkGameState> updateCallback;

    public NetworkManager(Consumer<NetworkGameState> updateCallback) {
        this.updateCallback = updateCallback;
    }

    public void setPlayerMode(PlayerMode playerMode) {
        this.playerMode = playerMode;

        if (playerMode != PlayerMode.SINGLE_PLAYER) {
            start();
        } else {
            stop();
        }
    }

    public void start() {
        if (enabled || playerMode == PlayerMode.SINGLE_PLAYER) {
            return;
        }

        gameServer = new GameServer(playerMode, updateCallback);
        serverThread = new Thread(gameServer, "GameServer-" + playerMode);
        serverThread.setDaemon(true);
        serverThread.start();

        enabled = true;
        GameLogger.gameFlow("NetworkManager started for " + playerMode);
    }

    public void stop() {
        if (!enabled) {
            return;
        }

        enabled = false;

        if (gameServer != null) {
            gameServer.stop();
        }

        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
        }

        GameLogger.gameFlow("NetworkManager stopped");
    }

    public boolean sendGameState(GameState gameState, String lastAction, String playerName) {
        if (!enabled || playerMode == PlayerMode.SINGLE_PLAYER) {
            return false;
        }

        return NetworkingUtils.sendGameState(gameState, playerMode, lastAction, playerName);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMultiplayer() {
        return playerMode != PlayerMode.SINGLE_PLAYER;
    }

    public PlayerMode getPlayerMode() {
        return playerMode;
    }
}