package hr.algebra.theloop.controller;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    @FXML private Pane gameBoardPane;
    @FXML private Polygon drFooMachine;
    @FXML private HBox playerHandBox;

    @FXML private EraController dawnEraController;
    @FXML private EraController medievalEraController;
    @FXML private EraController renaissanceEraController;
    @FXML private EraController industryEraController;
    @FXML private EraController globalizationEraController;
    @FXML private EraController robotsEraController;
    @FXML private EraController endTimesEraController;

    @FXML private Label turnLabel;
    @FXML private Label drFooLocationLabel;
    @FXML private Label cycleLabel;
    @FXML private Label missionsLabel;
    @FXML private Label vortexLabel;
    @FXML private Label playerNameLabel;
    @FXML private Label playerLocationLabel;

    @FXML private Button endTurnButton;
    @FXML private Button loopButton;
    @FXML private ListView<String> missionsList;

    private GameEngine gameEngine;
    private Map<Era, EraController> eraControllers;
    private boolean gameRunning;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGame();
        setupEraControllers();
        updateUI();
    }

    private void setupGame() {
        gameEngine = new GameEngine();
        gameEngine.addPlayer("Time Agent Bruno", Era.DAWN_OF_TIME);
        gameEngine.startGame();
        gameRunning = true;
    }

    private void setupEraControllers() {
        eraControllers = new HashMap<>();

        if (dawnEraController != null) {
            dawnEraController.setEra(Era.DAWN_OF_TIME);
            eraControllers.put(Era.DAWN_OF_TIME, dawnEraController);
        }

        if (medievalEraController != null) {
            medievalEraController.setEra(Era.MEDIEVAL);
            eraControllers.put(Era.MEDIEVAL, medievalEraController);
        }

        if (renaissanceEraController != null) {
            renaissanceEraController.setEra(Era.RENAISSANCE);
            eraControllers.put(Era.RENAISSANCE, renaissanceEraController);
        }

        if (industryEraController != null) {
            industryEraController.setEra(Era.INDUSTRY);
            eraControllers.put(Era.INDUSTRY, industryEraController);
        }

        if (globalizationEraController != null) {
            globalizationEraController.setEra(Era.GLOBALIZATION);
            eraControllers.put(Era.GLOBALIZATION, globalizationEraController);
        }

        if (robotsEraController != null) {
            robotsEraController.setEra(Era.ROBOTS);
            eraControllers.put(Era.ROBOTS, robotsEraController);
        }

        if (endTimesEraController != null) {
            endTimesEraController.setEra(Era.END_OF_TIMES);
            eraControllers.put(Era.END_OF_TIMES, endTimesEraController);
        }
    }

    private double getAngleForEra(Era era) {
        double baseAngle = 360.0 / 7.0;
        return era.ordinal() * baseAngle;
    }

    @FXML
    private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) {
            return;
        }

        gameEngine.processTurn();
        updateUI();

        if (gameEngine.isGameOver()) {
            gameRunning = false;
            endTurnButton.setDisable(true);
        }
    }

    @FXML
    private void performLoop() {
        // TODO: Implement LOOP dialog
        System.out.println("LOOP button clicked - not implemented yet");
    }

    @FXML
    private void saveGame() {
        // TODO: Implement save functionality
        System.out.println("Save game - not implemented yet");
    }

    @FXML
    private void loadGame() {
        // TODO: Implement load functionality
        System.out.println("Load game - not implemented yet");
    }

    @FXML
    private void newGame() {
        setupGame();
        updateUI();
        endTurnButton.setDisable(false);
        gameRunning = true;
    }

    private void updateUI() {
        updateStatusLabels();
        updateEras();
        updatePlayerInfo();
    }

    private void updateStatusLabels() {
        GameState state = gameEngine.getGameState();

        turnLabel.setText("Turn: " + state.getTurnNumber());
        drFooLocationLabel.setText("Dr. Foo @ " + state.getDrFooPosition().getDisplayName());
        cycleLabel.setText("Cycle: " + state.getCurrentCycle() + "/3");
        missionsLabel.setText("Missions: " + state.getTotalMissionsCompleted() + "/4");
        vortexLabel.setText("Vortexes: " + state.getVortexCount() + "/3");
    }

    private void updateEras() {
        GameState state = gameEngine.getGameState();

        for (Map.Entry<Era, EraController> entry : eraControllers.entrySet()) {
            Era era = entry.getKey();
            EraController controller = entry.getValue();

            if (controller != null) {
                controller.updateDisplay(
                        state.getRifts(era),
                        state.getEnergy(era),
                        state.hasVortex(era)
                );
            }
        }
    }

    private void updatePlayerInfo() {
        Player currentPlayer = gameEngine.getCurrentPlayer();

        playerNameLabel.setText(currentPlayer.getName());
        playerLocationLabel.setText("@ " + currentPlayer.getCurrentEra().getDisplayName());

        updatePlayerPositions();
    }

    private void updatePlayerPositions() {
        for (EraController controller : eraControllers.values()) {
            if (controller != null) {
                controller.setPlayerPresent(false);
            }
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        EraController playerEraController = eraControllers.get(currentPlayer.getCurrentEra());
        if (playerEraController != null) {
            playerEraController.setPlayerPresent(true);
        }
    }
}