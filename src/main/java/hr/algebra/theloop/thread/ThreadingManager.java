package hr.algebra.theloop.thread;

import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.utils.GameLogger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class ThreadingManager {

    private final GameEngine gameEngine;
    private Timeline autoSaveTimeline;
    private boolean isRunning = false;

    private static final int AUTO_SAVE_INTERVAL = 30;

    public ThreadingManager(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        setupAutoSaveTimeline();
    }

    private void setupAutoSaveTimeline() {
        autoSaveTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> performAutoSave()),
                new KeyFrame(Duration.seconds(AUTO_SAVE_INTERVAL))
        );
        autoSaveTimeline.setCycleCount(Animation.INDEFINITE);
    }

    private void performAutoSave() {
        if (gameEngine.isGameOver()) {
            return;
        }

        AutoSaveThread saveThread = new AutoSaveThread(gameEngine.getGameState());
        Thread thread = new Thread(saveThread, "AutoSave-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    public void start() {
        if (!isRunning) {
            autoSaveTimeline.play();
            isRunning = true;
            GameLogger.gameFlow("Background threading started (auto-save every " + AUTO_SAVE_INTERVAL + "s)");
        }
    }

    public void stop() {
        if (isRunning) {
            autoSaveTimeline.stop();
            isRunning = false;
            GameLogger.gameFlow("Background threading stopped");
        }
    }

    public void pause() {
        if (isRunning) {
            autoSaveTimeline.pause();
        }
    }

    public void resume() {
        if (isRunning) {
            autoSaveTimeline.play();
        }
    }

    public void forceAutoSave() {
        GameLogger.gameFlow("Manual save requested");
        performAutoSave();
    }

    public boolean isRunning() {
        return isRunning;
    }
}