package hr.algebra.theloop.controller;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.input.PlayerInputHandler;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.thread.ThreadingManager;
import hr.algebra.theloop.ui.GameUIManager;
import hr.algebra.theloop.ui.PlayerHandManager;
import hr.algebra.theloop.utils.DocumentationUtils;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    @FXML private CircularBoardView circularBoard;
    @FXML private CardController card1Controller, card2Controller, card3Controller;
    @FXML private Label turnLabel, drFooLocationLabel, cycleLabel, missionsLabel, vortexLabel;
    @FXML private Label playerNameLabel, playerLocationLabel;
    @FXML private Label completedMissionsLabel, duplicatesLabel, availableCardsLabel;
    @FXML private Button endTurnButton, loopButton;
    @FXML private ListView<String> activeMissionsList;

    private GameEngine gameEngine;
    private GameUIManager uiManager;
    private PlayerInputHandler inputHandler;
    private PlayerHandManager handManager;
    private ThreadingManager threadingManager;
    private GameActionsHandler actionsHandler;
    private boolean gameRunning;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeGame();
        initializeManagers();
        initializeThreading();
        setupEventHandlers();
        updateUI();
    }

    private void initializeGame() {
        gameEngine = new GameEngine();
        gameEngine.addPlayer("Time Agent Bruno", Era.DAWN_OF_TIME);
        gameEngine.startGame();
        gameRunning = true;
    }

    private void initializeManagers() {
        uiManager = new GameUIManager(
                turnLabel, drFooLocationLabel, cycleLabel, missionsLabel, vortexLabel,
                playerNameLabel, playerLocationLabel, endTurnButton, loopButton,
                activeMissionsList, circularBoard,
                completedMissionsLabel, duplicatesLabel, availableCardsLabel
        );

        inputHandler = new PlayerInputHandler(gameEngine);
        handManager = new PlayerHandManager(card1Controller, card2Controller, card3Controller);
        actionsHandler = new GameActionsHandler(gameEngine, threadingManager, this::updateUI, this::endGame);
    }

    private void initializeThreading() {
        threadingManager = new ThreadingManager(gameEngine);
        threadingManager.start();
        actionsHandler.updateThreadingManager(threadingManager);
    }

    private void setupEventHandlers() {
        handManager.setupCardClickHandlers(inputHandler);

        for (Era era : Era.values()) {
            var eraView = circularBoard.getEraView(era);
            if (eraView != null) {
                eraView.setOnMouseClicked(event -> {
                    if (inputHandler.handleEraClick(era)) {
                        updateUI();
                        checkGameEnd();
                    }
                    event.consume();
                });
            }
        }
    }

    @FXML private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        boolean success = gameEngine.isWaitingForPlayerInput() ?
                inputHandler.endPlayerTurn() : inputHandler.processNextTurn();

        if (success) {
            updateUI();
            checkGameEnd();
        }
    }

    @FXML private void performLoop() {
        if (gameRunning && !gameEngine.isGameOver() && inputHandler.performLoop()) {
            updateUI();
        }
    }

    @FXML private void acquireCard() {
        if (gameRunning && !gameEngine.isGameOver() && gameEngine.acquireCard(gameEngine.getCurrentPlayer())) {
            updateUI();
        }
    }

    @FXML private void saveGame() {
        if (gameRunning) {
            gameEngine.saveGame();
            actionsHandler.handleSaveGame();
        }
    }

    @FXML private void loadGame() {
        if (!gameRunning) return;

        GameEngine newEngine = actionsHandler.handleLoadGame(endTurnButton);
        if (newEngine != null) {
            gameEngine = newEngine;
            inputHandler = new PlayerInputHandler(gameEngine);
            actionsHandler.updateGameEngine(gameEngine);
            handManager.clearAllSelections();

            initializeThreading();
            setupEventHandlers();
            updateUI();
        }
    }

    @FXML private void newGame() {
        gameEngine = actionsHandler.handleNewGame();
        inputHandler = new PlayerInputHandler(gameEngine);
        actionsHandler.updateGameEngine(gameEngine);
        handManager.clearAllSelections();

        initializeThreading();
        setupEventHandlers();
        updateUI();

        endTurnButton.setDisable(false);
        gameRunning = true;
    }

    private void updateUI() {
        if (gameEngine == null) return;

        uiManager.updateAll(
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
            endGame();
        }
    }

    private void endGame() {
        gameRunning = false;
        endTurnButton.setDisable(true);
        loopButton.setDisable(true);
        if (threadingManager != null) {
            threadingManager.stop();
        }
    }

    public void shutdown() {
        if (threadingManager != null) {
            threadingManager.stop();
        }
    }

    @FXML
    private void generateDocumentation() {
        try {
            DocumentationUtils.generateDocumentation();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Documentation Generated");
            alert.setHeaderText("Success!");
            alert.setContentText("HTML documentation generated successfully!\nLocation: doc/documentation.html");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Documentation generation failed");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();

            e.printStackTrace();
        }
    }
}