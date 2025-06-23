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
            hideMultiplayerInfo(multiplayerInfoControl);
            return;
        }

        String infoText = generateMultiplayerInfo();

        switch (multiplayerInfoControl) {
            case Label label -> {
                label.setText(infoText);
                label.setVisible(true);
            }
            case TextArea textArea -> {
                textArea.setText(infoText);
                textArea.setVisible(true);
            }
            default -> throw new IllegalArgumentException("Unsupported control type: " + multiplayerInfoControl.getClass());
        }
    }

    private void hideMultiplayerInfo(Object multiplayerInfoControl) {
        switch (multiplayerInfoControl) {
            case Label label -> label.setVisible(false);
            case TextArea textArea -> textArea.setVisible(false);
            default -> throw new IllegalArgumentException("Unsupported control type: " + multiplayerInfoControl.getClass());
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