package hr.algebra.theloop.ui;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.missions.Mission;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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
    private final ListView<String> missionsList;
    private final CircularBoardView circularBoard;

    public GameUIManager(Label turnLabel, Label drFooLocationLabel, Label cycleLabel,
                         Label missionsLabel, Label vortexLabel, Label playerNameLabel,
                         Label playerLocationLabel, Button endTurnButton, Button loopButton,
                         ListView<String> missionsList, CircularBoardView circularBoard) {
        this.turnLabel = turnLabel;
        this.drFooLocationLabel = drFooLocationLabel;
        this.cycleLabel = cycleLabel;
        this.missionsLabel = missionsLabel;
        this.vortexLabel = vortexLabel;
        this.playerNameLabel = playerNameLabel;
        this.playerLocationLabel = playerLocationLabel;
        this.endTurnButton = endTurnButton;
        this.loopButton = loopButton;
        this.missionsList = missionsList;
        this.circularBoard = circularBoard;
    }

    public void updateStatusLabels(GameState state) {
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
            vortexLabel.setText("Vortexes: " + state.getVortexCount() + "/4");
        }
    }

    public void updateBoard(GameState state, Player currentPlayer) {
        if (circularBoard == null) {
            return;
        }

        for (Era era : Era.values()) {
            boolean playerHere = era.equals(currentPlayer.getCurrentEra());
            circularBoard.updateEra(era,
                    state.getRifts(era),
                    state.getEnergy(era),
                    state.hasVortex(era),
                    playerHere);
        }

        circularBoard.pointDrFooAt(state.getDrFooPosition());
    }

    public void updatePlayerInfo(Player currentPlayer) {
        if (playerNameLabel != null) {
            playerNameLabel.setText(currentPlayer.getName());
        }
        if (playerLocationLabel != null) {
            playerLocationLabel.setText("@ " + currentPlayer.getCurrentEra().getDisplayName());
        }
    }

    public void updateMissions(GameState state) {
        if (missionsList == null) return;

        missionsList.getItems().clear();
        List<Mission> missions = state.getActiveMissions();

        if (missions.isEmpty()) {
            missionsList.getItems().add("No active missions");
        } else {
            for (Mission mission : missions) {
                missionsList.getItems().add(mission.toString());
            }
        }
    }

    public void updateButtons(boolean gameOver, boolean waitingForPlayerInput) {
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

    public void updateAll(GameState state, Player currentPlayer, boolean gameOver, boolean waitingForPlayerInput) {
        updateStatusLabels(state);
        updateBoard(state, currentPlayer);
        updatePlayerInfo(currentPlayer);
        updateMissions(state);
        updateButtons(gameOver, waitingForPlayerInput);
    }
}