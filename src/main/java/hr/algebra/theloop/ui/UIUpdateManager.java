package hr.algebra.theloop.ui;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class UIUpdateManager {

    private final Label turnLabel;
    private final Label drFooLocationLabel;
    private final Label cycleLabel;
    private final Label missionsLabel;
    private final Label vortexLabel;
    private final Label playerNameLabel;
    private final Label playerLocationLabel;
    private final Button endTurnButton;
    private final Button loopButton;
    private final ListView<String> activeMissionsList;
    private final CircularBoardView circularBoard;
    private final Label completedMissionsLabel;
    private final Label duplicatesLabel;
    private final Label availableCardsLabel;

    public UIUpdateManager(Label turnLabel, Label drFooLocationLabel, Label cycleLabel,
                           Label missionsLabel, Label vortexLabel, Label playerNameLabel,
                           Label playerLocationLabel, Button endTurnButton, Button loopButton,
                           ListView<String> activeMissionsList, CircularBoardView circularBoard,
                           Label completedMissionsLabel, Label duplicatesLabel, Label availableCardsLabel) {
        this.turnLabel = turnLabel;
        this.drFooLocationLabel = drFooLocationLabel;
        this.cycleLabel = cycleLabel;
        this.missionsLabel = missionsLabel;
        this.vortexLabel = vortexLabel;
        this.playerNameLabel = playerNameLabel;
        this.playerLocationLabel = playerLocationLabel;
        this.endTurnButton = endTurnButton;
        this.loopButton = loopButton;
        this.activeMissionsList = activeMissionsList;
        this.circularBoard = circularBoard;
        this.completedMissionsLabel = completedMissionsLabel;
        this.duplicatesLabel = duplicatesLabel;
        this.availableCardsLabel = availableCardsLabel;
    }

    public void updateAll(GameState state, Player currentPlayer, boolean gameOver,
                          boolean waitingForPlayerInput, int duplicatesInBag, int duplicatesOnBoard) {
        updateStatusLabels(state);
        updateBoard(state, currentPlayer);
        updatePlayerInfo(currentPlayer);
        updateMissions(state);
        updateGameStatus(duplicatesInBag, duplicatesOnBoard);
        updateButtons(gameOver, waitingForPlayerInput);
    }

    private void updateStatusLabels(GameState state) {
        if (turnLabel != null) {
            turnLabel.setText("Turn: " + state.getTurnNumber());
        }
        if (drFooLocationLabel != null) {
            drFooLocationLabel.setText("Dr. Foo @ " + state.getDrFooPosition().getDisplayName());
        }
        if (cycleLabel != null) {
            cycleLabel.setText("Cycle: " + state.getCurrentCycle() + "/3");
        }
        if (missionsLabel != null) {
            missionsLabel.setText("Missions: " + state.getTotalMissionsCompleted() + "/4");
        }
        if (vortexLabel != null) {
            vortexLabel.setText("Vortexes: " + state.getVortexCount() + "/3");
        }
    }

    private void updateBoard(GameState state, Player currentPlayer) {
        if (circularBoard == null) return;

        for (Era era : Era.values()) {
            boolean playerHere = era.equals(currentPlayer.getCurrentEra());
            circularBoard.updateEra(era,
                    state.getRifts(era),
                    state.getEnergy(era),
                    state.getDuplicatesAt(era),
                    state.hasVortex(era),
                    playerHere);
        }

        circularBoard.pointDrFooAt(state.getDrFooPosition());
    }

    private void updatePlayerInfo(Player currentPlayer) {
        if (playerNameLabel != null) {
            playerNameLabel.setText(currentPlayer.getName());
        }
        if (playerLocationLabel != null) {
            playerLocationLabel.setText("@ " + currentPlayer.getCurrentEra().getDisplayName());
        }
    }

    private void updateMissions(GameState state) {
        if (activeMissionsList == null) return;

        activeMissionsList.getItems().clear();
        state.getActiveMissions().forEach(mission ->
                activeMissionsList.getItems().add(mission.toString()));

        if (completedMissionsLabel != null) {
            completedMissionsLabel.setText("Completed: " + state.getTotalMissionsCompleted() + "/4");
        }
    }

    private void updateGameStatus(int duplicatesInBag, int duplicatesOnBoard) {
        if (duplicatesLabel != null) {
            duplicatesLabel.setText("Duplicates: " + duplicatesOnBoard + " (Bag: " + duplicatesInBag + "/28)");
        }

        if (availableCardsLabel != null) {
            availableCardsLabel.setText("Available Cards: 7");
        }
    }

    private void updateButtons(boolean gameOver, boolean waitingForPlayerInput) {
        if (gameOver) {
            if (endTurnButton != null) endTurnButton.setDisable(true);
            if (loopButton != null) loopButton.setDisable(true);
        } else {
            if (endTurnButton != null) {
                endTurnButton.setDisable(false);
                endTurnButton.setText(waitingForPlayerInput ? "End Player Turn" : "Next Turn");
            }
            if (loopButton != null) {
                loopButton.setDisable(!waitingForPlayerInput);
            }
        }
    }
}