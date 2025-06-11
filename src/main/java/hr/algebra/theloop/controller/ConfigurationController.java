package hr.algebra.theloop.controller;

import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.model.GameConfiguration;
import hr.algebra.theloop.utils.GameLogger;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class ConfigurationController {

    private final ConfigurationManager configManager;

    public ConfigurationController() {
        this.configManager = ConfigurationManager.getInstance();
    }

    public void setEasyMode() {
        GameConfiguration config = configManager.getConfig();
        config.setMaxCycles(5);
        config.setMissionsToWin(3);
        config.setMaxVortexes(4);
        configManager.updateConfig(config);

        showConfigAlert("Easy Mode Set",
                "Cycles: 5, Missions: 3, Vortexes: 4");
    }

    public void setNormalMode() {
        GameConfiguration config = configManager.getConfig();
        config.setMaxCycles(3);
        config.setMissionsToWin(4);
        config.setMaxVortexes(3);
        configManager.updateConfig(config);

        showConfigAlert("Normal Mode Set",
                "Cycles: 3, Missions: 4, Vortexes: 3");
    }

    public void setHardMode() {
        GameConfiguration config = configManager.getConfig();
        config.setMaxCycles(2);
        config.setMissionsToWin(5);
        config.setMaxVortexes(2);
        configManager.updateConfig(config);

        showConfigAlert("Hard Mode Set",
                "Cycles: 2, Missions: 5, Vortexes: 2");
    }

    public void adjustMaxCycles() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getMaxCycles()));
        dialog.setTitle("Adjust Max Cycles");
        dialog.setHeaderText("Dr. Foo Cycles Configuration");
        dialog.setContentText("Enter max cycles (1-10):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            try {
                int cycles = Integer.parseInt(value);
                if (cycles >= 1 && cycles <= 10) {
                    configManager.setMaxCycles(cycles);
                    showConfigAlert("Max Cycles Updated", "New value: " + cycles);
                } else {
                    showErrorAlert("Invalid value. Please enter 1-10.");
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid number format.");
            }
        });
    }

    public void adjustMissionsToWin() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getMissionsToWin()));
        dialog.setTitle("Adjust Missions to Win");
        dialog.setHeaderText("Victory Condition Configuration");
        dialog.setContentText("Enter missions needed for victory (1-10):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            try {
                int missions = Integer.parseInt(value);
                if (missions >= 1 && missions <= 10) {
                    configManager.setMissionsToWin(missions);
                    showConfigAlert("Missions to Win Updated", "New value: " + missions);
                } else {
                    showErrorAlert("Invalid value. Please enter 1-10.");
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid number format.");
            }
        });
    }

    public void adjustMaxVortexes() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getMaxVortexes()));
        dialog.setTitle("Adjust Max Vortexes");
        dialog.setHeaderText("Defeat Condition Configuration");
        dialog.setContentText("Enter max vortexes before defeat (1-10):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            try {
                int vortexes = Integer.parseInt(value);
                if (vortexes >= 1 && vortexes <= 10) {
                    configManager.setMaxVortexes(vortexes);
                    showConfigAlert("Max Vortexes Updated", "New value: " + vortexes);
                } else {
                    showErrorAlert("Invalid value. Please enter 1-10.");
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid number format.");
            }
        });
    }

    public void adjustServerPort() {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(configManager.getServerPort()));
        dialog.setTitle("Adjust Server Port");
        dialog.setHeaderText("Multiplayer Network Configuration");
        dialog.setContentText("Enter server port (1024-65535):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            try {
                int port = Integer.parseInt(value);
                if (port >= 1024 && port <= 65535) {
                    GameConfiguration config = configManager.getConfig();
                    config.setServerPort(port);
                    configManager.updateConfig(config);
                    showConfigAlert("Server Port Updated", "New port: " + port);
                } else {
                    showErrorAlert("Invalid port. Please enter 1024-65535.");
                }
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid number format.");
            }
        });
    }

    public void showCurrentConfiguration() {
        GameConfiguration config = configManager.getConfig();

        String configText = String.format(
                "🎮 GAME SETTINGS:\n" +
                        "Max Cycles: %d\n" +
                        "Missions to Win: %d\n" +
                        "Max Vortexes: %d\n\n" +
                        "🌐 NETWORK SETTINGS:\n" +
                        "Server Port: %d\n" +
                        "Chat Port: %d\n" +
                        "Connection Timeout: %d ms\n\n" +
                        "👤 PLAYER SETTINGS:\n" +
                        "Starting Era: %s\n" +
                        "Starting Energy: %d\n" +
                        "Free Battery Uses: %d",
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