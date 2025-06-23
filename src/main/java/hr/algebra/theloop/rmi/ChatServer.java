package hr.algebra.theloop.rmi;

import hr.algebra.theloop.jndi.ConfigurationKey;
import hr.algebra.theloop.jndi.ConfigurationReader;
import hr.algebra.theloop.utils.GameLogger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatServer {

    private static final int RANDOM_PORT_HINT = 0;
    private static final int RMI_PORT = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.RMI_PORT);
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);

    private ChatServer() {
    }

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            ChatRemoteService chatRemoteService = new ChatRemoteServiceImpl();

            ChatRemoteService skeleton = (ChatRemoteService) UnicastRemoteObject.exportObject(
                    chatRemoteService, RANDOM_PORT_HINT);

            registry.rebind(ChatRemoteService.CHAT_REMOTE_OBJECT_NAME, skeleton);

            GameLogger.success("🎯 Chat Server started on port " + RMI_PORT);
            GameLogger.gameFlow("Chat service registered as: " + ChatRemoteService.CHAT_REMOTE_OBJECT_NAME);

            while (RUNNING.get()) {
                Thread.sleep(1000);
            }

            GameLogger.gameFlow("💤 Chat Server shutting down gracefully...");

        } catch (RemoteException e) {
            GameLogger.error("Failed to start Chat Server: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            GameLogger.gameFlow("Chat Server interrupted - shutting down...");
        }
    }

    public static void shutdown() {
        RUNNING.set(false);
        GameLogger.gameFlow("🛑 Chat Server shutdown requested");
    }

    public static boolean isRunning() {
        return RUNNING.get();
    }
}