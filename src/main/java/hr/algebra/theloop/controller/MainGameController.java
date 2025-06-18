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
        threadingManager = new ThreadingManager(gameEngine);
        threadingManager.start();

        actionsHandler = new GameActionsHandler(gameEngine, threadingManager, this::updateUI, this::endGame);
        actionsHandler.setComponents(inputHandler, handManager, multiplayerHelper, uiManager, circularBoard);

        uiManager.setMultiplayerComponents(multiplayerHelper, handManager, multiplayerInfoTextArea);
        actionsHandler.setupEventHandlers();
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
            gameEngine.getConfigManager().refreshConfiguration(this::updateUI);
            GameLogger.gameFlow("🔄 Game refreshed with new configuration");
        }
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
            gameEngine = newEngine;
            inputHandler = actionsHandler.getInputHandler();
            multiplayerHelper = actionsHandler.getMultiplayerHelper();
            uiManager.setMultiplayerComponents(multiplayerHelper, handManager, multiplayerInfoTextArea);
            updateUI();
        }
    }

    @FXML private void newGame() {
        gameEngine = actionsHandler.handleNewGame();
        inputHandler = actionsHandler.getInputHandler();
        multiplayerHelper = actionsHandler.getMultiplayerHelper();
        gameRunning = true;
        endTurnButton.setDisable(false);
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