package hr.algebra.theloop.ui;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class MultiplayerUIHelper {

    private final GameEngine gameEngine;

    public MultiplayerUIHelper(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public String generateMultiplayerInfo() {
        if (!gameEngine.isMultiplayer()) {
            return "";
        }

        StringBuilder info = new StringBuilder();
        info.append("🎮 PLAYERS\n");
        info.append("━━━━━━━━━━━━━━━━━━━━\n");

        for (int i = 0; i < gameEngine.getPlayerManager().getPlayers().size(); i++) {
            Player player = gameEngine.getPlayerManager().getPlayers().get(i);
            boolean isLocal = (i == gameEngine.getLocalPlayerIndex());

            String icon = player.getName().contains("Bruno") ? "🔵" : "🔴";

            info.append(icon).append(" ")
                    .append(player.getName())
                    .append("\n   📍 ").append(player.getCurrentEra().getDisplayName())
                    .append("\n   🃏 ").append(player.getHand().size()).append(" cards");

            if (isLocal) {
                info.append(" ⭐ YOU");
            }

            if (i < gameEngine.getPlayerManager().getPlayers().size() - 1) {
                info.append("\n\n");
            }
        }

        return info.toString();
    }

    public void updateMultiplayerInfoLabel(Object multiplayerInfoControl) {
        if (!gameEngine.isMultiplayer()) {
            if (multiplayerInfoControl instanceof Label) {
                ((Label) multiplayerInfoControl).setVisible(false);
            } else if (multiplayerInfoControl instanceof TextArea) {
                ((TextArea) multiplayerInfoControl).setVisible(false);
            }
            return;
        }

        String infoText = generateMultiplayerInfo();

        if (multiplayerInfoControl instanceof Label) {
            Label label = (Label) multiplayerInfoControl;
            label.setText(infoText);
            label.setVisible(true);
        } else if (multiplayerInfoControl instanceof TextArea) {
            TextArea textArea = (TextArea) multiplayerInfoControl;
            textArea.setText(infoText);
            textArea.setVisible(true);
        }
    }

    public void updatePlayerPositions(CircularBoardView circularBoard) {
        if (gameEngine == null || !gameEngine.isMultiplayer() || circularBoard == null) {
            return;
        }

        clearPlayerIndicators(circularBoard);

        for (int i = 0; i < gameEngine.getPlayerManager().getPlayers().size(); i++) {
            Player player = gameEngine.getPlayerManager().getPlayers().get(i);
            boolean isLocal = (i == gameEngine.getLocalPlayerIndex());
            addPlayerIndicatorToEra(circularBoard, player.getCurrentEra(), player.getName(), isLocal);
        }
    }

    private void clearPlayerIndicators(CircularBoardView circularBoard) {
        for (Era era : Era.values()) {
            var eraView = circularBoard.getEraView(era);
            if (eraView != null) {
                eraView.getStyleClass().removeAll("era-has-bruno", "era-has-alice", "era-has-local-player");
            }
        }
    }

    private void addPlayerIndicatorToEra(CircularBoardView circularBoard, Era era, String playerName, boolean isLocal) {
        var eraView = circularBoard.getEraView(era);
        if (eraView != null) {
            if (playerName.contains("Bruno")) {
                eraView.getStyleClass().add("era-has-bruno");
            } else if (playerName.contains("Alice")) {
                eraView.getStyleClass().add("era-has-alice");
            }

            if (isLocal) {
                eraView.getStyleClass().add("era-has-local-player");
            }
        }
    }

    public Player getDisplayPlayer() {
        return gameEngine.isMultiplayer() ? gameEngine.getLocalPlayer() : gameEngine.getCurrentPlayer();
    }
}