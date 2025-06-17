package hr.algebra.theloop.rmi;

import hr.algebra.theloop.jndi.ConfigurationKey;
import hr.algebra.theloop.jndi.ConfigurationReader;
import hr.algebra.theloop.utils.GameLogger;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ChatServer {

    private static final int RANDOM_PORT_HINT = 0;
    private static final int rmiPort = ConfigurationReader.getIntegerValueForKey(ConfigurationKey.RMI_PORT);

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(rmiPort);

            ChatRemoteService chatRemoteService = new ChatRemoteServiceImpl();

            ChatRemoteService skeleton = (ChatRemoteService) UnicastRemoteObject.exportObject(
                    chatRemoteService, RANDOM_PORT_HINT);

            registry.rebind(ChatRemoteService.CHAT_REMOTE_OBJECT_NAME, skeleton);

            GameLogger.success("🎯 Chat Server started on port " + rmiPort);
            GameLogger.gameFlow("Chat service registered as: " + ChatRemoteService.CHAT_REMOTE_OBJECT_NAME);

            while (true) {
                Thread.sleep(1000);
            }

        } catch (RemoteException e) {
            GameLogger.error("Failed to start Chat Server: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            GameLogger.gameFlow("Chat Server shutting down...");
        }
    }
}