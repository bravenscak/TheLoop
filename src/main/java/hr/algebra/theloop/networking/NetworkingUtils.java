package hr.algebra.theloop.networking;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.NetworkGameState;
import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.utils.GameLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class NetworkingUtils {

    private NetworkingUtils() {}

    public static final String DEFAULT_HOST = "localhost";
    public static final int PLAYER_ONE_PORT = 12345;
    public static final int PLAYER_TWO_PORT = 12346;

    public static boolean sendGameState(GameState gameState, PlayerMode currentPlayerMode,
                                        String lastAction, String lastPlayerName) {
        try {
            int targetPort = getTargetPort(currentPlayerMode);
            if (targetPort == -1) {
                return false;
            }

            NetworkGameState networkState = NetworkGameState.fromGameState(
                    gameState, currentPlayerMode, lastAction, lastPlayerName);

            return sendNetworkState(networkState, DEFAULT_HOST, targetPort);

        } catch (Exception e) {
            GameLogger.error("Failed to send game state: " + e.getMessage());
            return false;
        }
    }

    private static boolean sendNetworkState(NetworkGameState networkState, String host, int port) {
        try (Socket clientSocket = new Socket(host, port)) {
            GameLogger.gameFlow("Connecting to " + host + ":" + port);

            try (ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream())) {

                oos.writeObject(networkState);
                oos.flush();

                String response = (String) ois.readObject();
                GameLogger.success("Network response: " + response);
                return true;
            }

        } catch (IOException | ClassNotFoundException e) {
            GameLogger.warning("Network send failed: " + e.getMessage());
            return false;
        }
    }

    private static int getTargetPort(PlayerMode currentMode) {
        return switch (currentMode) {
            case PLAYER_ONE -> PLAYER_TWO_PORT;
            case PLAYER_TWO -> PLAYER_ONE_PORT;
            case SINGLE_PLAYER -> -1;
        };
    }

    public static int getListeningPort(PlayerMode playerMode) {
        return switch (playerMode) {
            case PLAYER_ONE -> PLAYER_ONE_PORT;
            case PLAYER_TWO -> PLAYER_TWO_PORT;
            case SINGLE_PLAYER -> -1;
        };
    }
}