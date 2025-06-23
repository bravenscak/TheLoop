package hr.algebra.theloop.networking;

import hr.algebra.theloop.model.NetworkGameState;
import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.utils.GameLogger;
import javafx.application.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class GameServer implements Runnable {

    private final int port;
    private final PlayerMode playerMode;
    private final Consumer<NetworkGameState> gameStateUpdateCallback;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public GameServer(PlayerMode playerMode, Consumer<NetworkGameState> gameStateUpdateCallback) {
        this.playerMode = playerMode;
        this.port = NetworkingUtils.getListeningPort(playerMode);
        this.gameStateUpdateCallback = gameStateUpdateCallback;
    }

    @Override
    public void run() {
        if (port == -1) {
            GameLogger.gameFlow("Single player mode - no server needed");
            return;
        }

        running = true;

        try (ServerSocket server = new ServerSocket(port)) {
            this.serverSocket = server;
            GameLogger.gameFlow("Game server listening on port: " + port + " (Mode: " + playerMode + ")");

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    GameLogger.gameFlow("Player connected from: " + clientSocket.getRemoteSocketAddress());

                    Thread clientHandler = new Thread(() -> processClient(clientSocket));
                    clientHandler.setDaemon(true);
                    clientHandler.start();

                } catch (IOException e) {
                    if (running) {
                        GameLogger.error("Error accepting client connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            GameLogger.error("Failed to start game server on port " + port + ": " + e.getMessage());
        }
    }

    private void processClient(Socket clientSocket) {
        try (Socket socket = clientSocket) {
            handleClientCommunication(socket);
        } catch (IOException e) {
            GameLogger.error("Error processing client: " + e.getMessage());
        }
    }

    private void handleClientCommunication(Socket clientSocket) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

            NetworkGameState receivedState = (NetworkGameState) ois.readObject();

            GameLogger.gameFlow("Received game state from " + receivedState.getActivePlayerMode() +
                    " - Last action: " + receivedState.getLastAction());

            Platform.runLater(() -> {
                if (gameStateUpdateCallback != null) {
                    gameStateUpdateCallback.accept(receivedState);
                }
            });

            oos.writeObject("Game state received successfully");
            oos.flush();

        } catch (ClassNotFoundException e) {
            GameLogger.error("Error deserializing game state: " + e.getMessage());
            throw new IOException("Failed to deserialize game state", e);
        }
    }

    public void stop() {
        running = false;

        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                GameLogger.warning("Error closing server socket: " + e.getMessage());
            }
        }

        GameLogger.gameFlow("Game server stopped");
    }

    public boolean isRunning() {
        return running;
    }
}