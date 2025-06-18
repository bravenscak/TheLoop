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
    private Runnable uiUpdateCallback;
    private Runnable gameEndCallback;

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

        TextInputDialog dialog = new TextInputDialog("quicksave");
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Enter save name:");
        dialog.setContentText("Save name:");

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String saveName = result.get().trim();
            GamePersistenceManager.saveGameManually(gameEngine.getGameState(), saveName);
        } else {
            GamePersistenceManager.saveGameWithTimestamp(gameEngine.getGameState());
        }

        if (threadingManager != null) {
            threadingManager.forceAutoSave();
        }
    }

    public GameEngine handleLoadGame(Button referenceButton) {
        try {
            Stage stage = (Stage) referenceButton.getScene().getWindow();
            GameState loadedState = GamePersistenceManager.loadGameFromDialog(stage);

            if (loadedState != null) {
                stopThreading();

                GameEngine newGameEngine = new GameEngine();
                newGameEngine.restoreFromGameState(loadedState);

                setupAfterLoad(newGameEngine);

                GameLogger.gameFlow("Game loaded successfully");
                return newGameEngine;
            }

        } catch (Exception e) {
            GameLogger.error("Failed to load game: " + e.getMessage());
        }

        return null;
    }

    public GameEngine handleNewGame() {
        stopThreading();
        shutdownCurrentGame();

        String playerModeStr = System.getProperty("playerMode", "SINGLE_PLAYER");
        PlayerMode playerMode = PlayerMode.valueOf(playerModeStr);

        GameEngine newGameEngine = new GameEngine();
        newGameEngine.setPlayerMode(playerMode);
        newGameEngine.setupMultiplayerPlayers(playerMode);
        newGameEngine.setUIUpdateCallback(uiUpdateCallback);

        setupAfterNewGame(newGameEngine);

        newGameEngine.startGame();

        if (newGameEngine.isMultiplayer()) {
            newGameEngine.broadcastCompleteGameState("New Game Started", "System");
        }

        GameLogger.gameFlow("New game started in " + playerMode + " mode");
        return newGameEngine;
    }

    private void setupAfterLoad(GameEngine newGameEngine) {
        updateGameEngine(newGameEngine);

        if (inputHandler != null) {
            this.inputHandler = new PlayerInputHandler(newGameEngine);
        }

        if (handManager != null) {
            handManager.clearAllSelections();
        }

        startThreading();

        if (uiUpdateCallback != null) {
            uiUpdateCallback.run();
        }
    }

    private void setupAfterNewGame(GameEngine newGameEngine) {
        updateGameEngine(newGameEngine);

        if (multiplayerHelper != null) {
            this.multiplayerHelper = new MultiplayerUIHelper(newGameEngine);
        }

        if (inputHandler != null) {
            this.inputHandler = new PlayerInputHandler(newGameEngine);
        }

        if (handManager != null) {
            handManager.clearAllSelections();
        }

        if (uiManager != null && multiplayerHelper != null && handManager != null) {
            uiManager.setMultiplayerComponents(multiplayerHelper, handManager, null);
        }

        startThreading();

        if (uiUpdateCallback != null) {
            uiUpdateCallback.run();
        }
    }

    public void setupEventHandlers() {
        if (handManager != null && inputHandler != null) {
            handManager.setupCardClickHandlers(inputHandler);
        }

        if (circularBoard != null && inputHandler != null) {
            for (Era era : Era.values()) {
                var eraView = circularBoard.getEraView(era);
                if (eraView != null) {
                    eraView.setOnMouseClicked(event -> {
                        if (inputHandler.handleEraClick(era)) {
                            if (uiUpdateCallback != null) {
                                uiUpdateCallback.run();
                            }
                            if (gameEngine.isGameOver() && gameEndCallback != null) {
                                gameEndCallback.run();
                            }
                        }
                        event.consume();
                    });
                }
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