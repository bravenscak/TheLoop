package hr.algebra.theloop.controller;

import hr.algebra.theloop.chat.ChatManager;
import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.input.PlayerInputHandler;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.rmi.ChatRemoteService;
import hr.algebra.theloop.thread.ThreadingManager;
import hr.algebra.theloop.ui.GameUIManager;
import hr.algebra.theloop.ui.MultiplayerUIHelper;
import hr.algebra.theloop.ui.PlayerHandManager;
import hr.algebra.theloop.utils.DocumentationUtils;
import hr.algebra.theloop.utils.GameLogger;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    @FXML private CircularBoardView circularBoard;
    @FXML private CardController card1Controller, card2Controller, card3Controller;
    @FXML private Label turnLabel, drFooLocationLabel, cycleLabel, missionsLabel, vortexLabel;
    @FXML private Label playerNameLabel, playerLocationLabel;
    @FXML private Label completedMissionsLabel, duplicatesLabel, availableCardsLabel;
    @FXML private Button endTurnButton, loopButton;
    @FXML private ListView<String> activeMissionsList;
    @FXML private TextArea multiplayerInfoTextArea;

    @FXML private VBox chatContainer;
    @FXML private TextArea chatArea;
    @FXML private TextField chatTextField;
    @FXML private Button sendChatButton;

    private GameEngine gameEngine;
    private GameUIManager uiManager;
    private PlayerInputHandler inputHandler;
    private PlayerHandManager handManager;
    private ThreadingManager threadingManager;
    private GameActionsHandler actionsHandler;
    private boolean gameRunning;
    private MultiplayerUIHelper multiplayerHelper;
    private ChatRemoteService chatRemoteService;

    private ConfigurationController configController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeConfiguration();
        initializeGame();
        initializeManagers();
        initializeThreading();
        initializeChat();
        setupEventHandlers();
        updateUI();
    }

    private void initializeConfiguration() {
        configController = new ConfigurationController();
        GameLogger.gameFlow("🔧 Configuration system initialized");
    }

    private void initializeGame() {
        gameEngine = new GameEngine();

        String playerModeStr = System.getProperty("playerMode", "SINGLE_PLAYER");
        PlayerMode playerMode = PlayerMode.valueOf(playerModeStr);

        gameEngine.setPlayerMode(playerMode);
        gameEngine.setupMultiplayerPlayers(playerMode);

        gameEngine.setUIUpdateCallback(this::updateUI);

        multiplayerHelper = new MultiplayerUIHelper(gameEngine);

        gameEngine.startGame();
        gameRunning = true;

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

    private void initializeChat() {
        if (gameEngine.getPlayerMode() != PlayerMode.SINGLE_PLAYER) {
            try {
                chatRemoteService = ChatManager.connectToChatService();
                ChatManager.createAndRunChatTimeline(chatRemoteService, chatArea);
                chatTextField.setOnAction(e -> sendChatMessage());

            } catch (RemoteException | NotBoundException e) {
                GameLogger.warning("Chat service not available: " + e.getMessage());
                hideChatUI();
            }
        } else {
            hideChatUI();
        }
    }

    private void hideChatUI() {
        if (chatContainer != null) {
            chatContainer.setVisible(false);
            chatContainer.setManaged(false);
        }
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

    @FXML private void setEasyMode() {
        configController.setEasyMode();
        refreshGameWithNewConfig();
    }

    @FXML private void setNormalMode() {
        configController.setNormalMode();
        refreshGameWithNewConfig();
    }

    @FXML private void setHardMode() {
        configController.setHardMode();
        refreshGameWithNewConfig();
    }

    @FXML private void adjustMaxCycles() {
        configController.adjustMaxCycles();
        refreshGameWithNewConfig();
    }

    @FXML private void adjustMissionsToWin() {
        configController.adjustMissionsToWin();
        refreshGameWithNewConfig();
    }

    @FXML private void adjustMaxVortexes() {
        configController.adjustMaxVortexes();
        refreshGameWithNewConfig();
    }

    @FXML private void adjustServerPort() {
        configController.adjustServerPort();
    }

    @FXML private void showConfiguration() {
        configController.showCurrentConfiguration();
    }

    @FXML private void resetConfiguration() {
        configController.resetToDefaults();
        refreshGameWithNewConfig();
    }

    private void refreshGameWithNewConfig() {
        if (gameEngine != null) {
            gameEngine.refreshConfiguration();
            updateUI();
            GameLogger.gameFlow("🔄 Game refreshed with new configuration");
        }
    }

    @FXML
    private void sendChatMessage() {
        if (chatRemoteService != null && gameEngine.getPlayerMode() != PlayerMode.SINGLE_PLAYER) {
            String playerName = gameEngine.getLocalPlayer().getName();
            ChatManager.sendChatMessage(chatTextField, chatArea, chatRemoteService,
                    gameEngine.getPlayerMode(), playerName);
        }
    }

    @FXML
    private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        if (gameEngine.isMultiplayer() && gameEngine.getLocalPlayerIndex() != 0) {
            System.out.println("❌ Only Player 1 can end turn!");
            return;
        }

        boolean success = gameEngine.isWaitingForPlayerInput() ?
                inputHandler.endPlayerTurn() : inputHandler.processNextTurn();

        if (success) {
            updateUI();
            checkGameEnd();
        }
    }

    @FXML
    private void performLoop() {
        if (gameRunning && !gameEngine.isGameOver() && inputHandler.performLoop()) {
            updateUI();
        }
    }

    @FXML
    private void acquireCard() {
        if (gameRunning && !gameEngine.isGameOver()) {
            Player playerToAcquire = multiplayerHelper != null ? multiplayerHelper.getDisplayPlayer() : gameEngine.getCurrentPlayer();
            if (gameEngine.acquireCard(playerToAcquire)) {
                updateUI();
            }
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
        if (threadingManager != null) threadingManager.stop();
        if (gameEngine != null) gameEngine.shutdown();

        gameEngine = actionsHandler.handleNewGame();
        multiplayerHelper = new MultiplayerUIHelper(gameEngine);
        gameEngine.setUIUpdateCallback(this::updateUI);
        inputHandler = new PlayerInputHandler(gameEngine);
        actionsHandler.updateGameEngine(gameEngine);
        handManager.clearAllSelections();

        initializeThreading();
        setupEventHandlers();

        gameRunning = true;
        endTurnButton.setDisable(false);
        updateUI();

        if (gameEngine.isMultiplayer()) {
            gameEngine.broadcastCompleteGameState("New Game Started", "System");
        }
    }

    private void updateUI() {
        if (gameEngine == null) return;

        if (multiplayerHelper != null) {
            multiplayerHelper.updateMultiplayerInfoLabel(multiplayerInfoTextArea);
        }

        Player displayPlayer = multiplayerHelper != null ? multiplayerHelper.getDisplayPlayer() : gameEngine.getCurrentPlayer();

        uiManager.updateAll(
                gameEngine.getGameState(),
                displayPlayer,
                gameEngine.isGameOver(),
                gameEngine.isWaitingForPlayerInput(),
                gameEngine.getDuplicatesInBag(),
                gameEngine.getTotalDuplicatesOnBoard()
        );

        handManager.updateHand(displayPlayer);
        updatePlayerPositions();
    }

    private void updatePlayerPositions() {
        if (gameEngine == null || !gameEngine.isMultiplayer()) return;

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
}