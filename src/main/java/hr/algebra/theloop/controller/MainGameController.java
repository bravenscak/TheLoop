package hr.algebra.theloop.controller;

import hr.algebra.theloop.chat.ChatManager;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.input.PlayerInputHandler;
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
import java.util.ResourceBundle;

public class MainGameController implements Initializable {

    private static final String SINGLE_PLAYER_MODE = "SINGLE_PLAYER";
    private static final String PLAYER_MODE_PROPERTY = "playerMode";
    private static final String DOCUMENTATION_LOCATION = "doc/documentation.html";
    private static final String DOCUMENTATION_TITLE = "Documentation Generated";
    private static final String ERROR_TITLE = "Error";
    private static final String SUCCESS_HEADER = "Success!";

    @FXML private CircularBoardView circularBoard;
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
    @FXML private Label completedMissionsLabel;
    @FXML private Label duplicatesLabel;
    @FXML private Label availableCardsLabel;
    @FXML private Button endTurnButton;
    @FXML private Button loopButton;
    @FXML private ListView<String> activeMissionsList;
    @FXML private TextArea multiplayerInfoTextArea;

    @FXML private VBox chatContainer;
    @FXML private TextArea chatArea;
    @FXML private TextField chatTextField;

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
        configController = new ConfigurationController();
        initializeGame();
        initializeManagers();
        initializeChat();
        updateUI();
    }

    private void initializeGame() {
        gameEngine = new GameEngine();

        String playerModeStr = System.getProperty(PLAYER_MODE_PROPERTY, SINGLE_PLAYER_MODE);
        PlayerMode playerMode = PlayerMode.valueOf(playerModeStr);

        gameEngine.setPlayerMode(playerMode);
        gameEngine.setupMultiplayerPlayers(playerMode);
        gameEngine.setUIUpdateCallback(this::updateUI);

        multiplayerHelper = new MultiplayerUIHelper(gameEngine);
        gameEngine.startGame();
        gameRunning = true;

        logMultiplayerInfo(playerMode);
    }

    private void logMultiplayerInfo(PlayerMode playerMode) {
        if (playerMode != PlayerMode.SINGLE_PLAYER) {
            Player localPlayer = gameEngine.getLocalPlayer();
            GameLogger.gameFlow("🎯 You are controlling: " + localPlayer.getName() +
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
        threadingManager = new ThreadingManager(gameEngine);
        threadingManager.start();

        actionsHandler = new GameActionsHandler(gameEngine, threadingManager, this::updateUI, this::endGame);
        actionsHandler.setComponents(inputHandler, handManager, multiplayerHelper, uiManager, circularBoard);

        uiManager.setMultiplayerComponents(multiplayerHelper, handManager, multiplayerInfoTextArea);
        actionsHandler.setupEventHandlers();

        configController.setGameEngine(gameEngine);
        configController.setUIUpdateCallback(this::updateUI);
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

    @FXML private void setEasyMode() {
        configController.setEasyMode();
    }

    @FXML private void setNormalMode() {
        configController.setNormalMode();
    }

    @FXML private void setHardMode() {
        configController.setHardMode();
    }

    @FXML private void adjustMaxCycles() {
        configController.adjustMaxCycles();
    }

    @FXML private void adjustMissionsToWin() {
        configController.adjustMissionsToWin();
    }

    @FXML private void adjustMaxVortexes() {
        configController.adjustMaxVortexes();
    }

    @FXML private void adjustServerPort() {
        configController.adjustServerPort();
    }

    @FXML private void showConfiguration() {
        configController.showCurrentConfiguration();
    }

    @FXML private void resetConfiguration() {
        configController.resetToDefaults();
    }

    @FXML private void sendChatMessage() {
        if (chatRemoteService != null && gameEngine.getPlayerMode() != PlayerMode.SINGLE_PLAYER) {
            String playerName = gameEngine.getLocalPlayer().getName();
            ChatManager.sendChatMessage(chatTextField, chatArea, chatRemoteService,
                    gameEngine.getPlayerMode(), playerName);
        }
    }

    @FXML private void endTurn() {
        if (!gameRunning || gameEngine.isGameOver()) return;

        if (!canEndTurn()) {
            GameLogger.warning("❌ Only Player 1 can end turn!");
            return;
        }

        boolean success = gameEngine.isWaitingForPlayerInput() ?
                inputHandler.endPlayerTurn() : inputHandler.processNextTurn();

        if (success) {
            updateUI();
            checkGameEnd();
        }
    }

    private boolean canEndTurn() {
        return !gameEngine.isMultiplayer() || gameEngine.getLocalPlayerIndex() == 0;
    }

    @FXML private void performLoop() {
        if (gameRunning && !gameEngine.isGameOver() && inputHandler.performLoop()) {
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
            updateAfterLoad(newEngine);
        }
    }

    private void updateAfterLoad(GameEngine newEngine) {
        gameEngine = newEngine;
        inputHandler = actionsHandler.getInputHandler();
        multiplayerHelper = actionsHandler.getMultiplayerHelper();
        uiManager.setMultiplayerComponents(multiplayerHelper, handManager, multiplayerInfoTextArea);
        configController.setGameEngine(gameEngine);
        updateUI();
    }

    @FXML private void newGame() {
        gameEngine = actionsHandler.handleNewGame();
        inputHandler = actionsHandler.getInputHandler();
        multiplayerHelper = actionsHandler.getMultiplayerHelper();
        gameRunning = true;
        endTurnButton.setDisable(false);
        configController.setGameEngine(gameEngine);
        updateUI();
    }

    private void updateUI() {
        if (gameEngine == null) return;

        Player displayPlayer = multiplayerHelper != null ? multiplayerHelper.getDisplayPlayer() : gameEngine.getCurrentPlayer();

        uiManager.updateAll(
                gameEngine.getGameState(),
                displayPlayer,
                gameEngine.isGameOver(),
                gameEngine.isWaitingForPlayerInput(),
                gameEngine.getDuplicatesInBag(),
                gameEngine.getTotalDuplicatesOnBoard()
        );
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

    @FXML private void generateDocumentation() {
        try {
            DocumentationUtils.generateDocumentation();
            showDocumentationSuccess();
        } catch (Exception e) {
            showDocumentationError(e);
        }
    }

    private void showDocumentationSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(DOCUMENTATION_TITLE);
        alert.setHeaderText(SUCCESS_HEADER);
        alert.setContentText("HTML documentation generated successfully!\nLocation: " + DOCUMENTATION_LOCATION);
        alert.showAndWait();
    }

    private void showDocumentationError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(ERROR_TITLE);
        alert.setHeaderText("Documentation generation failed");
        alert.setContentText("Error: " + e.getMessage());
        alert.showAndWait();

        GameLogger.error("Documentation generation failed: " + e.getMessage());
    }
}