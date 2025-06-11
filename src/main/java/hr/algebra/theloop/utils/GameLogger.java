package hr.algebra.theloop.utils;


import hr.algebra.theloop.missions.Mission;

public class GameLogger {

    public static final boolean DEBUG_MODE = false;
    public static final boolean VERBOSE_MODE = false;
    public static final boolean PRODUCTION_MODE = true;

    public static void gameFlow(String message) {
        if (!PRODUCTION_MODE) {
            System.out.println("🎮 " + message);
        }
    }

    public static void playerAction(String playerName, String action) {
        if (!PRODUCTION_MODE) {
            System.out.println("👤 " + playerName + ": " + action);
        }
    }

    public static void mission(String message) {
        System.out.println("🎯 " + message);
    }

    public static void drFoo(String message) {
        if (!PRODUCTION_MODE) {
            System.out.println("🤖 Dr. Foo: " + message);
        }
    }

    public static void error(String message) {
        System.err.println("❌ ERROR: " + message);
    }

    public static void success(String message) {
        System.out.println("✅ " + message);
    }

    public static void warning(String message) {
        System.out.println("⚠️ " + message);
    }

    public static void gameEnd(String result) {
        System.out.println("\n🏁 GAME END: " + result + "\n");
    }

    public static void missionCreated(Mission mission) {
        System.out.println("[MISSION CREATED] " + mission.getName() +
                " - " + mission.getDescription());
    }

    public static void missionCompleted(Mission mission, String playerName) {
        System.out.println("[MISSION COMPLETED] " + mission.getName() +
                " by " + playerName + " - " + mission.getDescription());
    }
}