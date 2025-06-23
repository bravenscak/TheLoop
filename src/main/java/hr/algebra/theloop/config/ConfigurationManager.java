package hr.algebra.theloop.config;

import hr.algebra.theloop.model.GameConfiguration;
import hr.algebra.theloop.utils.XmlUtils;
import hr.algebra.theloop.utils.GameLogger;

public class ConfigurationManager {

    private static volatile ConfigurationManager instance;
    private GameConfiguration currentConfig;

    private ConfigurationManager() {
        loadConfiguration();
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    public void loadConfiguration() {
        try {
            currentConfig = XmlUtils.loadGameConfiguration();
            GameLogger.success("✅ Configuration loaded from XML");
            GameLogger.gameFlow("📋 Config: " + currentConfig.toString());
        } catch (Exception e) {
            GameLogger.error("Failed to load configuration, using defaults: " + e.getMessage());
            currentConfig = createDefaultConfiguration();
        }
    }

    public void refreshConfiguration(Runnable uiUpdateCallback) {
        try {
            loadConfiguration();
            GameLogger.gameFlow("🔄 Configuration refreshed from XML");

            if (uiUpdateCallback != null) {
                uiUpdateCallback.run();
            }

        } catch (Exception e) {
            GameLogger.error("Failed to refresh configuration: " + e.getMessage());
        }
    }

    public void saveConfiguration() {
        try {
            XmlUtils.saveGameConfiguration(currentConfig);
            GameLogger.success("✅ Configuration saved to XML");
        } catch (Exception e) {
            GameLogger.error("Failed to save configuration: " + e.getMessage());
        }
    }

    public GameConfiguration getConfig() {
        return currentConfig;
    }

    public void updateConfig(GameConfiguration newConfig) {
        this.currentConfig = new GameConfiguration(newConfig);
        saveConfiguration();
        GameLogger.gameFlow("🔄 Configuration updated");
    }

    public int getMaxCycles() {
        return currentConfig.getMaxCycles();
    }

    public int getMissionsToWin() {
        return currentConfig.getMissionsToWin();
    }

    public int getMaxVortexes() {
        return currentConfig.getMaxVortexes();
    }

    public int getServerPort() {
        return currentConfig.getServerPort();
    }

    public int getChatPort() {
        return currentConfig.getChatPort();
    }

    public void setMaxCycles(int maxCycles) {
        currentConfig.setMaxCycles(maxCycles);
        saveConfiguration();
    }

    public void setMissionsToWin(int missionsToWin) {
        currentConfig.setMissionsToWin(missionsToWin);
        saveConfiguration();
    }

    public void setMaxVortexes(int maxVortexes) {
        currentConfig.setMaxVortexes(maxVortexes);
        saveConfiguration();
    }

    private GameConfiguration createDefaultConfiguration() {
        return new GameConfiguration();
    }
}