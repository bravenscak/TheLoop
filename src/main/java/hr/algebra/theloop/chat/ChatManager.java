package hr.algebra.theloop.chat;

import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.rmi.ChatRemoteService;
import hr.algebra.theloop.utils.GameLogger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.rmi.RemoteException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatManager {

    private ChatManager() {}

    public static void createAndRunChatTimeline(ChatRemoteService chatRemoteService,
                                                TextArea chatMessagesTextArea) {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            try {
                List<String> chatMessages = chatRemoteService.getAllChatMessages();

                chatMessagesTextArea.clear();

                for (String chatMessage : chatMessages) {
                    chatMessagesTextArea.appendText(chatMessage + "\n");
                }

                chatMessagesTextArea.setScrollTop(Double.MAX_VALUE);

            } catch (RemoteException ex) {
                GameLogger.error("Chat service connection failed: " + ex.getMessage());
            }
        }), new KeyFrame(Duration.seconds(1)));

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        GameLogger.gameFlow("🎯 Chat timeline started - polling every second");
    }

    public static void sendChatMessage(TextField chatMessagesTextField,
                                       TextArea chatMessagesTextArea,
                                       ChatRemoteService chatRemoteService,
                                       PlayerMode playerMode,
                                       String playerName) {
        String messageText = chatMessagesTextField.getText();

        if (messageText == null || messageText.trim().isEmpty()) {
            return;
        }

        try {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String formattedMessage = String.format("[%s] %s: %s",
                    timestamp, playerName, messageText.trim());

            chatRemoteService.sendChatMessage(formattedMessage);

            chatMessagesTextField.clear();

            List<String> chatMessages = chatRemoteService.getAllChatMessages();
            chatMessagesTextArea.clear();
            for (String chatMessage : chatMessages) {
                chatMessagesTextArea.appendText(chatMessage + "\n");
            }
            chatMessagesTextArea.setScrollTop(Double.MAX_VALUE);

            GameLogger.gameFlow("💬 " + playerName + " sent: " + messageText);

        } catch (RemoteException e) {
            GameLogger.error("Failed to send chat message: " + e.getMessage());
        }
    }
}