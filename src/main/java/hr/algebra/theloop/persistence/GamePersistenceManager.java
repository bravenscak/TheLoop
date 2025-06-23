package hr.algebra.theloop.persistence;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.utils.GameLogger;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GamePersistenceManager {

    private static final String SAVES_DIRECTORY = "saves";
    private static final String MANUAL_SAVE_PREFIX = "manual_save_";
    private static final String AUTO_SAVE_PREFIX = "autosave_";
    private static final String FILE_EXTENSION = ".dat";

    private GamePersistenceManager() {
    }

    public static boolean saveGameManually(GameState gameState, String saveName) {
        try {
            createSavesDirectory();

            String fileName = MANUAL_SAVE_PREFIX + saveName + FILE_EXTENSION;
            Path filePath = Paths.get(SAVES_DIRECTORY, fileName);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                oos.writeObject(gameState);
            }

            GameLogger.success("Manual save completed: " + fileName);
            return true;

        } catch (IOException e) {
            GameLogger.error("Manual save failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveGameWithTimestamp(GameState gameState) {
        try {
            createSavesDirectory();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            String fileName = MANUAL_SAVE_PREFIX + LocalDateTime.now().format(formatter) + FILE_EXTENSION;
            Path filePath = Paths.get(SAVES_DIRECTORY, fileName);

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                oos.writeObject(gameState);
            }

            GameLogger.success("Manual save completed: " + fileName);
            return true;

        } catch (IOException e) {
            GameLogger.error("Manual save failed: " + e.getMessage());
            return false;
        }
    }

    public static GameState loadGameState(String fileName) {
        try {
            Path filePath = Paths.get(SAVES_DIRECTORY, fileName);

            if (!Files.exists(filePath)) {
                GameLogger.error("Save file not found: " + fileName);
                return null;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
                GameState gameState = (GameState) ois.readObject();
                GameLogger.success("Game loaded successfully: " + fileName);
                return gameState;
            }

        } catch (IOException | ClassNotFoundException e) {
            GameLogger.error("Failed to load game: " + e.getMessage());
            return null;
        }
    }

    public static GameState loadGameFromDialog(Stage parentStage) {
        List<String> availableSaves = getAvailableSaveFiles();

        if (availableSaves.isEmpty()) {
            GameLogger.warning("No save files found");
            return null;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Game");
        fileChooser.setInitialDirectory(Paths.get(SAVES_DIRECTORY).toFile());

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Save files (*.dat)", "*.dat");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showOpenDialog(parentStage);

        if (selectedFile != null) {
            return loadGameState(selectedFile.getName());
        }

        return null;
    }

    public static List<String> getAvailableSaveFiles() {
        try {
            Path savesDir = Paths.get(SAVES_DIRECTORY);

            if (!Files.exists(savesDir)) {
                return new ArrayList<>();
            }

            return Files.list(savesDir)
                    .filter(path -> path.toString().endsWith(FILE_EXTENSION))
                    .map(path -> path.getFileName().toString())
                    .sorted((a, b) -> {
                        boolean aIsManual = a.startsWith(MANUAL_SAVE_PREFIX);
                        boolean bIsManual = b.startsWith(MANUAL_SAVE_PREFIX);

                        if (aIsManual && !bIsManual) return -1;
                        if (!aIsManual && bIsManual) return 1;

                        return b.compareTo(a);
                    })
                    .toList();

        } catch (IOException e) {
            GameLogger.error("Failed to list save files: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static boolean deleteSaveFile(String fileName) {
        try {
            Path filePath = Paths.get(SAVES_DIRECTORY, fileName);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                GameLogger.success("Save file deleted: " + fileName);
            } else {
                GameLogger.warning("Save file not found: " + fileName);
            }

            return deleted;

        } catch (IOException e) {
            GameLogger.error("Failed to delete save file: " + e.getMessage());
            return false;
        }
    }

    private static void createSavesDirectory() throws IOException {
        Path savesDir = Paths.get(SAVES_DIRECTORY);
        if (!Files.exists(savesDir)) {
            Files.createDirectories(savesDir);
        }
    }

    public static String formatSaveFileInfo(String fileName) {
        try {
            Path filePath = Paths.get(SAVES_DIRECTORY, fileName);
            if (!Files.exists(filePath)) {
                return fileName + " (file not found)";
            }

            long sizeBytes = Files.size(filePath);
            String sizeStr = sizeBytes > 1024 ? (sizeBytes / 1024) + " KB" : sizeBytes + " B";

            String type = fileName.startsWith(AUTO_SAVE_PREFIX) ? "Auto" : "Manual";

            return fileName + " (" + type + ", " + sizeStr + ")";

        } catch (IOException e) {
            return fileName + " (error reading info)";
        }
    }
}