package hr.algebra.theloop.utils;

import hr.algebra.theloop.missions.Mission;
import java.util.logging.Logger;

public class GameLogger {

    private static final Logger logger = Logger.getLogger(GameLogger.class.getName());

    public static final boolean DEBUG_MODE = false;
    public static final boolean VERBOSE_MODE = false;
    public static final boolean PRODUCTION_MODE = true;

    private GameLogger() {
    }

    public static void gameFlow(String message) {
        if (!PRODUCTION_MODE && DEBUG_MODE) {
            logger.info(() -> "🎮 " + message);
        }
    }

    public static void playerAction(String playerName, String action) {
        if (!PRODUCTION_MODE && VERBOSE_MODE) {
            logger.info(() -> "👤 " + playerName + ": " + action);
        }
    }

    public static void mission(String message) {
        if (!PRODUCTION_MODE) {
            logger.info(() -> "🎯 " + message);
        }
    }

    public static void drFoo(String message) {
        if (!PRODUCTION_MODE && DEBUG_MODE) {
            logger.info(() -> "🤖 Dr. Foo: " + message);
        }
    }

    public static void error(String message) {
        logger.severe(() -> "❌ ERROR: " + message);
    }

    public static void success(String message) {
        logger.info(() -> "✅ " + message);
    }

    public static void warning(String message) {
        logger.warning(() -> "⚠️ " + message);
    }

    public static void gameEnd(String result) {
        logger.info(() -> "\n🏁 GAME END: " + result + "\n");
    }

    public static void missionCreated(Mission mission) {
        if (!PRODUCTION_MODE) {
            logger.info(() -> "[MISSION CREATED] " + mission.getName() + " - " + mission.getDescription());
        }
    }

    public static void missionCompleted(Mission mission, String playerName) {
        logger.info(() -> "[MISSION COMPLETED] " + mission.getName() + " by " + playerName + " - " + mission.getDescription());
    }
}