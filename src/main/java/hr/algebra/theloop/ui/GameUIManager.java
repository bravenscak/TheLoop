package hr.algebra.theloop.ui;

import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.missions.Mission;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.model.Duplicate;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.util.List;

public class GameUIManager {

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
    private final ConfigurationManager configManager;

    private MultiplayerUIHelper multiplayerHelper;
    private PlayerHandManager handManager;
    private TextArea multiplayerInfoTextArea;

    public GameUIManager(Label turnLabel, Label drFooLocationLabel, Label cycleLabel,
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
        this.configManager = ConfigurationManager.getInstance();
    }

    public void setMultiplayerComponents(MultiplayerUIHelper multiplayerHelper,
                                         PlayerHandManager handManager,
                                         TextArea multiplayerInfoTextArea) {
        this.multiplayerHelper = multiplayerHelper;
        this.handManager = handManager;
        this.multiplayerInfoTextArea = multiplayerInfoTextArea;
    }

    public void updateAll(GameState state, Player currentPlayer, boolean gameOver,
                          boolean waitingForPlayerInput, int duplicatesInBag, int duplicatesOnBoard) {
        updateStatusLabels(state);
        updateBoard(state, currentPlayer);
        updatePlayerInfo(currentPlayer);
        updateMissions(state);
        updateGameStatus(duplicatesInBag, duplicatesOnBoard, state);
        updateButtons(gameOver, waitingForPlayerInput);

        if (multiplayerHelper != null) {
            if (multiplayerInfoTextArea != null) {
                multiplayerHelper.updateMultiplayerInfoLabel(multiplayerInfoTextArea);
            }
            multiplayerHelper.updatePlayerPositions(circularBoard);
        }

        if (handManager != null) {
            Player displayPlayer = multiplayerHelper != null ? multiplayerHelper.getDisplayPlayer() : currentPlayer;
            handManager.updateHand(displayPlayer);
        }
    }

    private void updateStatusLabels(GameState state) {
        if (turnLabel != null) {
            turnLabel.setText("Turn: " + state.getTurnNumber());
        }
        if (drFooLocationLabel != null) {
            drFooLocationLabel.setText("Dr. Foo @ " + state.getDrFooPosition().getDisplayName());
        }
        if (cycleLabel != null) {
            cycleLabel.setText("Cycle: " + state.getCurrentCycle() + "/" + configManager.getMaxCycles());
        }
        if (missionsLabel != null) {
            missionsLabel.setText("Missions: " + state.getTotalMissionsCompleted() + "/" + configManager.getMissionsToWin());
        }
        if (vortexLabel != null) {
            vortexLabel.setText("Vortexes: " + state.getVortexCount() + "/" + configManager.getMaxVortexes());
        }
    }

    private void updateBoard(GameState state, Player currentPlayer) {
        if (circularBoard == null) {
            return;
        }

        for (Era era : Era.values()) {
            boolean playerHere = era.equals(currentPlayer.getCurrentEra());
            List<Duplicate> duplicatesHere = state.getDuplicatesAt(era);

            circularBoard.updateEra(era,
                    state.getRifts(era),
                    state.getEnergy(era),
                    duplicatesHere,
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
        List<Mission> missions = state.getActiveMissions();

        if (missions.isEmpty()) {
            activeMissionsList.getItems().add("No active missions");
        } else {
            for (Mission mission : missions) {
                String displayText = formatMissionText(mission);
                activeMissionsList.getItems().add(displayText);
            }
        }

        if (completedMissionsLabel != null) {
            completedMissionsLabel.setText("Completed: " + state.getTotalMissionsCompleted() + "/" + configManager.getMissionsToWin());
        }

        activeMissionsList.refresh();
    }

    private String formatMissionText(Mission mission) {
        String progress = "(" + mission.getCurrentProgress() + "/" + mission.getRequiredProgress() + ")";
        String location = mission.getAssignedEra() != null ?
                " @ " + mission.getAssignedEra().getDisplayName() : " (follows Dr. Foo)";

        return mission.getName() + " " + progress + location;
    }

    private void updateGameStatus(int duplicatesInBag, int duplicatesOnBoard, GameState state) {
        if (duplicatesLabel != null) {
            duplicatesLabel.setText("Duplicates: " + duplicatesOnBoard + " (Bag: " + duplicatesInBag + "/28)");
        }

        if (availableCardsLabel != null) {
            int availableCards = countAvailableCards(state);
            availableCardsLabel.setText("Available Cards: " + availableCards);
        }
    }

    private int countAvailableCards(GameState state) {
        int count = 0;
        for (Era era : Era.values()) {
            if (!state.hasVortex(era)) {
                count++;
            }
        }
        return count;
    }

    private void updateButtons(boolean gameOver, boolean waitingForPlayerInput) {
        if (gameOver) {
            if (endTurnButton != null) {
                endTurnButton.setDisable(true);
            }
            if (loopButton != null) {
                loopButton.setDisable(true);
            }
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