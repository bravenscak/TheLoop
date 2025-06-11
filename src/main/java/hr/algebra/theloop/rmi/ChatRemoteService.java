package hr.algebra.theloop.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatRemoteService extends Remote {
    String CHAT_REMOTE_OBJECT_NAME = "hr.algebra.theloop.chat.service";

    void sendChatMessage(String message) throws RemoteException;
    List<String> getAllChatMessages() throws RemoteException;
}