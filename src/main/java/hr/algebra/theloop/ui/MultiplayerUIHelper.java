package hr.algebra.theloop.ui;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Player;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;

import java.util.Map;

public class MultiplayerUIHelper {

    private final GameEngine gameEngine;

    public MultiplayerUIHelper(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public boolean isLocalPlayerTurn() {
        return true;
    }

    public boolean checkAndAlertIfNotPlayerTurn(String action) {
        return true;
    }

    public void updateButtonStates(Button endTurnButton, Pane playerHandContainer, Map<?, Button> eraButtons) {
        boolean enableControls = !gameEngine.isGameOver();

        if (endTurnButton != null) {
            endTurnButton.setDisable(!enableControls);
        }

        if (playerHandContainer != null) {
            for (Node node : playerHandContainer.getChildren()) {
                if (node instanceof Button) {
                    node.setDisable(!enableControls);
                }
            }
        }

        if (eraButtons != null) {
            eraButtons.values().forEach(button -> button.setDisable(!enableControls));
        }
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

    public String generateGameStatus() {
        StringBuilder status = new StringBuilder();
        status.append("🎯 Missions: ")
                .append(gameEngine.getGameState().getTotalMissionsCompleted())
                .append("/4\n");

        status.append("⚡ Dr. Foo @ ")
                .append(gameEngine.getGameState().getDrFooPosition().getDisplayName())
                .append("\n");

        status.append("🌀 Vortexes: ")
                .append(gameEngine.getGameState().getVortexCount())
                .append("/3\n");

        status.append("🔄 Cycle: ")
                .append(gameEngine.getGameState().getCurrentCycle())
                .append("/3");

        return status.toString();
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

    public Player getDisplayPlayer() {
        return gameEngine.isMultiplayer() ? gameEngine.getLocalPlayer() : gameEngine.getCurrentPlayer();
    }
}