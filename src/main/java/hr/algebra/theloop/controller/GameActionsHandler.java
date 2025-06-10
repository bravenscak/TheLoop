package hr.algebra.theloop.controller;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.persistence.GamePersistenceManager;
import hr.algebra.theloop.thread.ThreadingManager;
import hr.algebra.theloop.utils.GameLogger;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class GameActionsHandler {

    private GameEngine gameEngine;
    private ThreadingManager threadingManager;
    private Runnable uiUpdateCallback;
    private Runnable gameEndCallback;

    public GameActionsHandler(GameEngine gameEngine, ThreadingManager threadingManager,
                              Runnable uiUpdateCallback, Runnable gameEndCallback) {
        this.gameEngine = gameEngine;
        this.threadingManager = threadingManager;
        this.uiUpdateCallback = uiUpdateCallback;
        this.gameEndCallback = gameEndCallback;
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
                if (threadingManager != null) {
                    threadingManager.stop();
                }

                GameEngine newGameEngine = new GameEngine();
                newGameEngine.restoreFromGameState(loadedState);

                GameLogger.gameFlow("Game loaded successfully");
                return newGameEngine;
            }

        } catch (Exception e) {
            GameLogger.error("Failed to load game: " + e.getMessage());
        }

        return null;
    }

    public GameEngine handleNewGame() {
        if (threadingManager != null) {
            threadingManager.stop();
        }

        String playerModeStr = System.getProperty("playerMode", "SINGLE_PLAYER");
        PlayerMode playerMode = PlayerMode.valueOf(playerModeStr);

        GameEngine newGameEngine = new GameEngine();

        newGameEngine.setPlayerMode(playerMode);
        newGameEngine.setupMultiplayerPlayers(playerMode);

        newGameEngine.startGame();

        GameLogger.gameFlow("New game started in " + playerMode + " mode");
        return newGameEngine;
    }

    public void updateGameEngine(GameEngine newGameEngine) {
        this.gameEngine = newGameEngine;
    }

    public void updateThreadingManager(ThreadingManager newThreadingManager) {
        this.threadingManager = newThreadingManager;
    }
}