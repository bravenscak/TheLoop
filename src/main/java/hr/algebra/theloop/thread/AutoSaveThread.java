package hr.algebra.theloop.thread;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.utils.GameLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoSaveThread implements Runnable {

    private static final AtomicBoolean SAVE_IN_PROGRESS = new AtomicBoolean(false);

    private final GameState gameState;

    public AutoSaveThread(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void run() {
        if (!SAVE_IN_PROGRESS.compareAndSet(false, true)) {
            return;
        }

        try {
            // DEBUG: Print current directory
            String currentDir = System.getProperty("user.dir");
            System.out.println("🔍 Current directory: " + currentDir);

            Path savesPath = createSavesDirectory();
            String fileName = generateFileName();
            String fullPath = saveGameState(savesPath, fileName);

            GameLogger.success("Auto-save completed: " + fullPath);
            System.out.println("💾 Saved to: " + fullPath);

        } catch (Exception e) {
            GameLogger.error("Auto-save failed: " + e.getMessage());
            e.printStackTrace(); // DEBUG: See full error
        } finally {
            SAVE_IN_PROGRESS.set(false);
        }
    }

    private Path createSavesDirectory() throws IOException {
        Path projectRoot = Paths.get(System.getProperty("user.dir"));
        Path savesPath = projectRoot.resolve("saves");

        System.out.println("🔍 Trying to create: " + savesPath.toAbsolutePath());

        if (!Files.exists(savesPath)) {
            Files.createDirectories(savesPath);
            System.out.println("✅ Created saves directory: " + savesPath.toAbsolutePath());
        } else {
            System.out.println("📁 Saves directory already exists: " + savesPath.toAbsolutePath());
        }

        return savesPath;
    }

    private String generateFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return "autosave_" + LocalDateTime.now().format(formatter) + ".dat";
    }

    private String saveGameState(Path savesPath, String fileName) throws IOException {
        Path filePath = savesPath.resolve(fileName);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(gameState);
        }

        return filePath.toAbsolutePath().toString();
    }

    public static boolean isSaveInProgress() {
        return SAVE_IN_PROGRESS.get();
    }
}