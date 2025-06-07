package hr.algebra.theloop.model;


public enum GameResult {
    VICTORY("🎉 VICTORY! Time agents saved the universe!"),
    DEFEAT_VORTEXES("💀 DEFEAT! Too many vortexes destroyed the timeline!"),
    DEFEAT_CYCLES("💀 DEFEAT! Dr. Foo completed his master plan!"),
    ONGOING("🎮 Game in progress...");

    private final String message;

    GameResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public boolean isGameOver() {
        return this != ONGOING;
    }
}