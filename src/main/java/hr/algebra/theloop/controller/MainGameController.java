package hr.algebra.theloop.controller;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.input.PlayerInputHandler;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.thread.ThreadingManager;
import hr.algebra.theloop.ui.GameUIManager;
import hr.algebra.theloop.ui.MultiplayerUIHelper;
import hr.algebra.theloop.ui.PlayerHandManager;
import hr.algebra.theloop.utils.DocumentationUtils;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    @FXML private CircularBoardView circularBoard;
    @FXML private CardController card1Controller, card2Controller, card3Controller;
    @FXML private Label turnLabel, drFooLocationLabel, cycleLabel, missionsLabel, vortexLabel;
    @FXML private Label playerNameLabel, playerLocationLabel;
    @FXML private Label completedMissionsLabel, duplicatesLabel, availableCardsLabel;
    @FXML private Label multiplayerInfoLabel;
    @FXML private Button endTurnButton, loopButton;
    @FXML private ListView<String> activeMissionsList;
    @FXML private TextArea multiplayerInfoTextArea;

    private GameEngine gameEngine;
    private GameUIManager uiManager;
    private PlayerInputHandler inputHandler;
    private PlayerHandManager handManager;
    private ThreadingManager threadingManager;
    private GameActionsHandler actionsHandler;
    private boolean gameRunning;
    private MultiplayerUIHelper multiplayerHelper;

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

        String playerModeStr = System.getProperty("playerMode", "SINGLE_PLAYER");
        PlayerMode playerMode = PlayerMode.valueOf(playerModeStr);

        System.out.println("🎮 Initializing game in " + playerMode + " mode");

        gameEngine.setPlayerMode(playerMode);
        gameEngine.setupMultiplayerPlayers(playerMode);

        multiplayerHelper = new MultiplayerUIHelper(gameEngine);

        gameEngine.startGame();
        gameRunning = true;

        System.out.println("✅ Game initialized successfully");

        if (playerMode != PlayerMode.SINGLE_PLAYER) {
            Player localPlayer = gameEngine.getLocalPlayer();
            System.out.println("🎯 You are controlling: " + localPlayer.getName() +
                    " (at " + localPlayer.getCurrentEra().getDisplayName() + ")");
        }
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
                    Player localPlayer = gameEngine.getLocalPlayer();
                    boolean success = gameEngine.movePlayer(localPlayer, era);

                    if (success) {
                        updateUI();
                        checkGameEnd();
                    }
                    event.consume();
                });
            }
        }
    }

    @FXML
    private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        gameEngine.endPlayerTurn();
        updateUI();
        checkGameEnd();
    }

    @FXML
    private void performLoop() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        gameEngine.processTurn();
        updateUI();
    }

    @FXML
    private void acquireCard() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        Player localPlayer = gameEngine.getLocalPlayer();
        boolean success = gameEngine.acquireCard(localPlayer);

        if (success) {
            updateUI();
        }
    }

    @FXML
    private void saveGame() {
        if (gameRunning) {
            gameEngine.saveGame();
            actionsHandler.handleSaveGame();
        }
    }

    @FXML
    private void loadGame() {
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

    @FXML
    private void newGame() {
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

        if (multiplayerHelper != null) {
            multiplayerHelper.updateMultiplayerInfoLabel(multiplayerInfoTextArea);
        }

        uiManager.updateAll(
                gameEngine.getGameState(),
                multiplayerHelper != null ? multiplayerHelper.getDisplayPlayer() : gameEngine.getCurrentPlayer(),
                gameEngine.isGameOver(),
                gameEngine.isWaitingForPlayerInput(),
                gameEngine.getDuplicatesInBag(),
                gameEngine.getTotalDuplicatesOnBoard()
        );

        Player displayPlayer = multiplayerHelper != null ? multiplayerHelper.getDisplayPlayer() : gameEngine.getCurrentPlayer();
        handManager.updateHand(displayPlayer);

        updatePlayerDisplay(displayPlayer);
        updatePlayerPositions(); // DODAJ
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
        if (gameEngine != null) {
            gameEngine.shutdown();
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

    private void updatePlayerDisplay(Player playerToDisplay) {
        if (gameEngine == null || playerToDisplay == null) return;

        Player currentPlayer = gameEngine.getCurrentPlayer();
        boolean isLocalPlayerTurn = multiplayerHelper != null ? multiplayerHelper.isLocalPlayerTurn() : true;

        playerLocationLabel.setText(playerToDisplay.getCurrentEra().getDisplayName());

        if (multiplayerHelper != null && gameEngine.isMultiplayer()) {
            updateMultiplayerInfo();
        }
    }

    private void updateMultiplayerInfo() {
        if (multiplayerHelper != null) {
            String info = multiplayerHelper.generateMultiplayerInfo();
            if (multiplayerInfoLabel != null) {
                multiplayerInfoLabel.setText(info);
            }
        }
    }

    private void updatePlayerPositions() {
        if (gameEngine == null) return;

        clearPlayerIndicators();

        for (int i = 0; i < gameEngine.getPlayerManager().getPlayers().size(); i++) {
            Player player = gameEngine.getPlayerManager().getPlayers().get(i);
            boolean isLocal = (i == gameEngine.getLocalPlayerIndex());

            addPlayerIndicatorToEra(player.getCurrentEra(), player.getName(), isLocal);
        }
    }

    private void clearPlayerIndicators() {
        for (Era era : Era.values()) {
            var eraView = circularBoard.getEraView(era);
            if (eraView != null) {
                eraView.getStyleClass().removeAll("era-has-bruno", "era-has-alice", "era-has-local-player");
            }
        }
    }

    private void addPlayerIndicatorToEra(Era era, String playerName, boolean isLocal) {
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

    @FXML
    private void onCardPlay() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        Player localPlayer = gameEngine.getLocalPlayer();

        if (!localPlayer.getHand().isEmpty()) {
            Era targetEra = localPlayer.getCurrentEra();
            boolean success = gameEngine.playCard(localPlayer, 0, targetEra);

            if (success) {
                updateUI();
                checkGameEnd();
            }
        }
    }
}