package hr.algebra.theloop.controller;

import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.GameConfiguration;
import hr.algebra.theloop.utils.GameLogger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import lombok.Setter;

import java.util.Optional;

public class ConfigurationController {

    private static final String NEW_VALUE_PREFIX = "New value: ";
    private static final String INVALID_VALUE_MESSAGE = "Invalid value. Please enter 1-10.";
    private static final String INVALID_NUMBER_FORMAT_MESSAGE = "Invalid number format.";

    private final ConfigurationManager configManager;
    @Setter
    private GameEngine gameEngine;
    private Runnable uiUpdateCallback;

    public ConfigurationController() {
        this.configManager = ConfigurationManager.getInstance();
    }

    public void setUIUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    public void setEasyMode() {
        GameConfiguration config = configManager.getConfig();
        config.setMaxCycles(5);
        config.setMissionsToWin(3);
        config.setMaxVortexes(4);
        configManager.updateConfig(config);

        showConfigAlert("Easy Mode Set", "Cycles: 5, Missions: 3, Vortexes: 4");
        refreshGameWithNewConfig();
    }

    public void setNormalMode() {
        GameConfiguration config = configManager.getConfig();
        config.setMaxCycles(3);
        config.setMissionsToWin(4);
        config.setMaxVortexes(3);
        configManager.updateConfig(config);

        showConfigAlert("Normal Mode Set", "Cycles: 3, Missions: 4, Vortexes: 3");
        refreshGameWithNewConfig();
    }

    public void setHardMode() {
        GameConfiguration config = configManager.getConfig();
        config.setMaxCycles(2);
        config.setMissionsToWin(5);
        config.setMaxVortexes(2);
        configManager.updateConfig(config);

        showConfigAlert("Hard Mode Set", "Cycles: 2, Missions: 5, Vortexes: 2");
        refreshGameWithNewConfig();
    }

    public void adjustMaxCycles() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getMaxCycles()));
        dialog.setTitle("Adjust Max Cycles");
        dialog.setHeaderText("Dr. Foo Cycles Configuration");
        dialog.setContentText("Enter max cycles (1-10):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> processIntegerInput(value, 1, 10,
                cycles -> {
                    configManager.setMaxCycles(cycles);
                    showConfigAlert("Max Cycles Updated", NEW_VALUE_PREFIX + cycles);
                    refreshGameWithNewConfig();
                }));
    }

    public void adjustMissionsToWin() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getMissionsToWin()));
        dialog.setTitle("Adjust Missions to Win");
        dialog.setHeaderText("Victory Condition Configuration");
        dialog.setContentText("Enter missions needed for victory (1-10):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> processIntegerInput(value, 1, 10,
                missions -> {
                    configManager.setMissionsToWin(missions);
                    showConfigAlert("Missions to Win Updated", NEW_VALUE_PREFIX + missions);
                    refreshGameWithNewConfig();
                }));
    }

    public void adjustMaxVortexes() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getMaxVortexes()));
        dialog.setTitle("Adjust Max Vortexes");
        dialog.setHeaderText("Defeat Condition Configuration");
        dialog.setContentText("Enter max vortexes before defeat (1-10):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> processIntegerInput(value, 1, 10,
                vortexes -> {
                    configManager.setMaxVortexes(vortexes);
                    showConfigAlert("Max Vortexes Updated", NEW_VALUE_PREFIX + vortexes);
                    refreshGameWithNewConfig();
                }));
    }

    public void adjustServerPort() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getServerPort()));
        dialog.setTitle("Adjust Server Port");
        dialog.setHeaderText("Multiplayer Network Configuration");
        dialog.setContentText("Enter server port (1024-65535):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> processIntegerInput(value, 1024, 65535,
                port -> {
                    GameConfiguration config = configManager.getConfig();
                    config.setServerPort(port);
                    configManager.updateConfig(config);
                    showConfigAlert("Server Port Updated", "New port: " + port);
                }));
    }

    public void showCurrentConfiguration() {
        GameConfiguration config = configManager.getConfig();

        String configText = String.format("🎮 GAME SETTINGS:\nMax Cycles: %d\nMissions to Win: %d\nMax Vortexes: %d\n\n🌐 NETWORK SETTINGS:\nServer Port: %d\nChat Port: %d\nConnection Timeout: %d ms\n\n👤 PLAYER SETTINGS:\nStarting Era: %s\nStarting Energy: %d\nFree Battery Uses: %d",
                config.getMaxCycles(), config.getMissionsToWin(), config.getMaxVortexes(),
                config.getServerPort(), config.getChatPort(), config.getConnectionTimeout(),
                config.getStartingEra(), config.getStartingEnergy(), config.getFreeBatteryUses()
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Current Configuration");
        alert.setHeaderText("📋 Game Configuration");
        alert.setContentText(configText);
        alert.showAndWait();
    }

    public void resetToDefaults() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reset Configuration");
        confirmation.setHeaderText("Reset to Default Settings");
        confirmation.setContentText("Are you sure you want to reset all settings to defaults?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            GameConfiguration defaultConfig = new GameConfiguration();
            configManager.updateConfig(defaultConfig);
            showConfigAlert("Configuration Reset", "All settings restored to defaults.");
            refreshGameWithNewConfig();
        }
    }

    private void processIntegerInput(String value, int min, int max, java.util.function.IntConsumer onSuccess) {
        try {
            int intValue = Integer.parseInt(value);
            if (intValue >= min && intValue <= max) {
                onSuccess.accept(intValue);
            } else {
                showErrorAlert(INVALID_VALUE_MESSAGE);
            }
        } catch (NumberFormatException e) {
            showErrorAlert(INVALID_NUMBER_FORMAT_MESSAGE);
        }
    }

    private void refreshGameWithNewConfig() {
        if (gameEngine != null) {
            gameEngine.getConfigManager().refreshConfiguration(uiUpdateCallback);
            GameLogger.gameFlow("🔄 Game refreshed with new configuration");
        }
        if (uiUpdateCallback != null) {
            uiUpdateCallback.run();
        }
    }

    private void showConfigAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("✅ Configuration Updated");
        alert.setContentText(message);
        alert.showAndWait();

        GameLogger.gameFlow("🔧 " + title + ": " + message);
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Configuration Error");
        alert.setHeaderText("❌ Invalid Input");
        alert.setContentText(message);
        alert.showAndWait();
    }
}