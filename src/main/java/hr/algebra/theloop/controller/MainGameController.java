package hr.algebra.theloop.controller;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.input.PlayerInputHandler;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.ui.PlayerHandManager;
import hr.algebra.theloop.ui.UIUpdateManager;
import hr.algebra.theloop.utils.GameLogger;
import hr.algebra.theloop.view.CircularBoardView;
import hr.algebra.theloop.view.SimpleEraView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import hr.algebra.theloop.thread.ThreadingManager;

import java.net.URL;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    @FXML private CircularBoardView circularBoard;
    @FXML private HBox playerHandBox;
    @FXML private CardController card1Controller;
    @FXML private CardController card2Controller;
    @FXML private CardController card3Controller;

    @FXML private Label turnLabel;
    @FXML private Label drFooLocationLabel;
    @FXML private Label cycleLabel;
    @FXML private Label missionsLabel;
    @FXML private Label vortexLabel;
    @FXML private Label playerNameLabel;
    @FXML private Label playerLocationLabel;

    @FXML private Button endTurnButton;
    @FXML private Button loopButton;

    @FXML private ListView<String> activeMissionsList;
    @FXML private Label completedMissionsLabel;
    @FXML private Label duplicatesLabel;
    @FXML private Label availableCardsLabel;

    private GameEngine gameEngine;
    private UIUpdateManager uiUpdateManager;
    private PlayerInputHandler inputHandler;
    private PlayerHandManager handManager;
    private boolean gameRunning;
    private ThreadingManager threadingManager;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGame();
        setupManagers();
        setupThreading();
        setupEventHandlers();
        updateUI();
    }

    private void setupGame() {
        gameEngine = new GameEngine();
        gameEngine.addPlayer("Time Agent Bruno", Era.DAWN_OF_TIME);
        gameEngine.startGame();
        gameRunning = true;
    }

    private void setupManagers() {
        uiUpdateManager = new UIUpdateManager(
                turnLabel, drFooLocationLabel, cycleLabel, missionsLabel, vortexLabel,
                playerNameLabel, playerLocationLabel, endTurnButton, loopButton,
                activeMissionsList, circularBoard,
                completedMissionsLabel, duplicatesLabel, availableCardsLabel
        );

        inputHandler = new PlayerInputHandler(gameEngine);
        handManager = new PlayerHandManager(card1Controller, card2Controller, card3Controller);
    }

    private void setupThreading() {
        threadingManager = new ThreadingManager(gameEngine);
        threadingManager.start();
    }

    private void setupEventHandlers() {
        handManager.setupCardClickHandlers(inputHandler);
        setupEraClickHandlers();
    }

    private void setupEraClickHandlers() {
        if (circularBoard != null) {
            for (Era era : Era.values()) {
                SimpleEraView eraView = circularBoard.getEraView(era);
                if (eraView != null) {
                    eraView.setOnMouseClicked(event -> {
                        boolean success = inputHandler.handleEraClick(era);
                        if (success) {
                            updateUI();
                            checkGameEnd();
                        }
                        event.consume();
                    });
                }
            }
        }
    }

    @FXML
    private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        boolean success;
        if (gameEngine.isWaitingForPlayerInput()) {
            success = inputHandler.endPlayerTurn();
        } else {
            success = inputHandler.processNextTurn();
        }

        if (success) {
            updateUI();
            checkGameEnd();
        }
    }

    @FXML
    private void performLoop() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        boolean success = inputHandler.performLoop();
        if (success) {
            updateUI();
        }
    }

    @FXML
    private void acquireCard() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        boolean success = gameEngine.acquireCard(gameEngine.getCurrentPlayer());
        if (success) {
            updateUI();
        }
    }

    @FXML
    private void saveGame() {
        if (threadingManager != null) {
            threadingManager.forceAutoSave();
            GameLogger.gameFlow("Manual save triggered");
        }
    }

    @FXML
    private void loadGame() {
        System.out.println("📁 Load game - TODO: Implement deserialization");
    }

    @FXML
    private void newGame() {
        if (threadingManager != null) {
            threadingManager.stop();
        }

        if (inputHandler != null) {
            inputHandler.clearSelection();
        }
        if (handManager != null) {
            handManager.clearAllSelections();
        }

        setupGame();
        inputHandler = new PlayerInputHandler(gameEngine);
        handManager.setupCardClickHandlers(inputHandler);
        setupThreading();
        setupEraClickHandlers();

        updateUI();
        endTurnButton.setDisable(false);
        gameRunning = true;
    }

    private void updateUI() {
        if (gameEngine == null) return;

        uiUpdateManager.updateAll(
                gameEngine.getGameState(),
                gameEngine.getCurrentPlayer(),
                gameEngine.isGameOver(),
                gameEngine.isWaitingForPlayerInput(),
                gameEngine.getDuplicatesInBag(),
                gameEngine.getTotalDuplicatesOnBoard()
        );

        handManager.updateHand(gameEngine.getCurrentPlayer());
    }

    private void checkGameEnd() {
        if (gameEngine.isGameOver()) {
            gameRunning = false;
            endTurnButton.setDisable(true);
            loopButton.setDisable(true);

            if (threadingManager != null) {
                threadingManager.stop();
            }
        }
    }

    public void shutdown() {
        if (threadingManager != null) {
            threadingManager.stop();
        }
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }
}