package hr.algebra.theloop.controller;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.input.PlayerInputHandler;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.persistence.GamePersistenceManager;
import hr.algebra.theloop.thread.ThreadingManager;
import hr.algebra.theloop.ui.GameUIManager;
import hr.algebra.theloop.ui.MultiplayerUIHelper;
import hr.algebra.theloop.ui.PlayerHandManager;
import hr.algebra.theloop.utils.GameLogger;
import hr.algebra.theloop.view.CircularBoardView;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class GameActionsHandler {

    private GameEngine gameEngine;
    private ThreadingManager threadingManager;
    private final Runnable uiUpdateCallback;
    private final Runnable gameEndCallback;

    private PlayerInputHandler inputHandler;
    private PlayerHandManager handManager;
    private MultiplayerUIHelper multiplayerHelper;
    private GameUIManager uiManager;
    private CircularBoardView circularBoard;

    public GameActionsHandler(GameEngine gameEngine, ThreadingManager threadingManager,
                              Runnable uiUpdateCallback, Runnable gameEndCallback) {
        this.gameEngine = gameEngine;
        this.threadingManager = threadingManager;
        this.uiUpdateCallback = uiUpdateCallback;
        this.gameEndCallback = gameEndCallback;
    }

    public void setComponents(PlayerInputHandler inputHandler, PlayerHandManager handManager,
                              MultiplayerUIHelper multiplayerHelper, GameUIManager uiManager,
                              CircularBoardView circularBoard) {
        this.inputHandler = inputHandler;
        this.handManager = handManager;
        this.multiplayerHelper = multiplayerHelper;
        this.uiManager = uiManager;
        this.circularBoard = circularBoard;
    }

    public void handleSaveGame() {
        if (gameEngine.isGameOver()) {
            GameLogger.warning("Cannot save - game is over");
            return;
        }

        Optional<String> saveName = promptForSaveName();

        if (saveName.isPresent() && !saveName.get().trim().isEmpty()) {
            GamePersistenceManager.saveGameManually(gameEngine.getGameState(), saveName.get().trim());
        } else {
            GamePersistenceManager.saveGameWithTimestamp(gameEngine.getGameState());
        }

        forceAutoSaveIfAvailable();
    }

    private Optional<String> promptForSaveName() {
        TextInputDialog dialog = new TextInputDialog("quicksave");
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Enter save name:");
        dialog.setContentText("Save name:");
        return dialog.showAndWait();
    }

    private void forceAutoSaveIfAvailable() {
        if (threadingManager != null) {
            threadingManager.forceAutoSave();
        }
    }

    public GameEngine handleLoadGame(Button referenceButton) {
        try {
            GameState loadedState = loadGameStateFromDialog(referenceButton);
            if (loadedState != null) {
                shutdownCurrentGameSafely();

                GameEngine newGameEngine = new GameEngine();
                newGameEngine.restoreFromGameState(loadedState);

                setupAfterLoadGame(newGameEngine);

                GameLogger.gameFlow("Game loaded successfully");
                return newGameEngine;
            }
        } catch (Exception e) {
            GameLogger.error("Failed to load game: " + e.getMessage());
        }
        return null;
    }

    private void setupAfterLoadGame(GameEngine newGameEngine) {
        updateGameEngine(newGameEngine);
        recreateMultiplayerComponents(newGameEngine);
        recreateInputHandler();
        clearHandManagerSelections();
        setupUIManagerComponents();
        setupEventHandlers();
        startThreading();


        if (newGameEngine.isMultiplayer()) {
            newGameEngine.broadcastCompleteGameState("Game Loaded", "System");
        }
        triggerUIUpdate();
    }

    private GameState loadGameStateFromDialog(Button referenceButton) {
        Stage stage = (Stage) referenceButton.getScene().getWindow();
        return GamePersistenceManager.loadGameFromDialog(stage);
    }

    public GameEngine handleNewGame() {
        shutdownCurrentGameSafely();

        GameEngine newGameEngine = createFreshGameEngine();
        setupAfterNewGame(newGameEngine);
        startNewGame(newGameEngine);

        GameLogger.gameFlow("New game started in " + newGameEngine.getPlayerMode() + " mode");
        return newGameEngine;
    }

    private void shutdownCurrentGameSafely() {
        stopThreading();
        shutdownCurrentGame();
    }

    private GameEngine createFreshGameEngine() {
        String playerModeStr = System.getProperty("playerMode", "SINGLE_PLAYER");
        PlayerMode playerMode = PlayerMode.valueOf(playerModeStr);

        GameEngine newGameEngine = new GameEngine();
        newGameEngine.setPlayerMode(playerMode);
        newGameEngine.setupMultiplayerPlayers(playerMode);
        newGameEngine.setUIUpdateCallback(uiUpdateCallback);

        return newGameEngine;
    }

    private void startNewGame(GameEngine newGameEngine) {
        newGameEngine.startGame();

        if (newGameEngine.isMultiplayer()) {
            newGameEngine.broadcastCompleteGameState("New Game Started", "System");
        }
    }

    private void setupAfterLoad(GameEngine newGameEngine) {
        setupAfterNewGame(newGameEngine);
    }


    private void setupAfterNewGame(GameEngine newGameEngine) {
        updateGameEngine(newGameEngine);
        recreateMultiplayerComponents(newGameEngine);
        recreateInputHandler();
        clearHandManagerSelections();
        setupUIManagerComponents();
        setupEventHandlers();
        startThreading();
        triggerUIUpdate();
    }

    private void recreateMultiplayerComponents(GameEngine newGameEngine) {
        if (multiplayerHelper != null) {
            this.multiplayerHelper = new MultiplayerUIHelper(newGameEngine);
        }
    }

    private void recreateInputHandler() {
        if (inputHandler != null) {
            this.inputHandler = new PlayerInputHandler(gameEngine);
        }
    }

    private void clearHandManagerSelections() {
        if (handManager != null) {
            handManager.clearAllSelections();
        }
    }

    private void setupUIManagerComponents() {
        if (uiManager != null && multiplayerHelper != null && handManager != null) {
            uiManager.setMultiplayerComponents(multiplayerHelper, handManager, null);
        }
    }

    private void triggerUIUpdate() {
        if (uiUpdateCallback != null) {
            uiUpdateCallback.run();
        }
    }

    public void setupEventHandlers() {
        setupCardClickHandlers();
        setupEraClickHandlers();
    }

    private void setupCardClickHandlers() {
        if (handManager != null && inputHandler != null) {
            handManager.setupCardClickHandlers(inputHandler);
        }
    }

    private void setupEraClickHandlers() {
        if (circularBoard != null && inputHandler != null) {
            for (Era era : Era.values()) {
                var eraView = circularBoard.getEraView(era);
                if (eraView != null) {
                    eraView.setOnMouseClicked(event -> {
                        handleEraClick(era);
                        event.consume();
                    });
                }
            }
        }
    }

    private void handleEraClick(Era era) {
        if (inputHandler.handleEraClick(era)) {
            triggerUIUpdate();

            if (gameEngine.isGameOver() && gameEndCallback != null) {
                gameEndCallback.run();
            }
        }
    }

    private void startThreading() {
        if (gameEngine != null) {
            threadingManager = new ThreadingManager(gameEngine);
            threadingManager.start();
        }
    }

    private void stopThreading() {
        if (threadingManager != null) {
            threadingManager.stop();
        }
    }

    private void shutdownCurrentGame() {
        if (gameEngine != null) {
            gameEngine.shutdown();
        }
    }

    public void updateGameEngine(GameEngine newGameEngine) {
        this.gameEngine = newGameEngine;
    }

    public void updateThreadingManager(ThreadingManager newThreadingManager) {
        this.threadingManager = newThreadingManager;
    }

    public PlayerInputHandler getInputHandler() {
        return inputHandler;
    }

    public MultiplayerUIHelper getMultiplayerHelper() {
        return multiplayerHelper;
    }
}