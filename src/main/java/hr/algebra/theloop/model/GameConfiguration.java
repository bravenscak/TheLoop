package hr.algebra.theloop.model;

import lombok.Data;

@Data
public class GameConfiguration {

    private int maxCycles = 3;
    private int missionsToWin = 4;
    private int maxVortexes = 3;
    private int maxHandSize = 3;

    private int serverPort = 12345;
    private int chatPort = 1099;
    private int connectionTimeout = 5000;

    private String startingEra = "DAWN_OF_TIME";
    private int startingEnergy = 1;
    private int freeBatteryUses = 1;

    public GameConfiguration() {}

    public GameConfiguration(GameConfiguration other) {
        this.maxCycles = other.maxCycles;
        this.missionsToWin = other.missionsToWin;
        this.maxVortexes = other.maxVortexes;
        this.maxHandSize = other.maxHandSize;
        this.serverPort = other.serverPort;
        this.chatPort = other.chatPort;
        this.connectionTimeout = other.connectionTimeout;
        this.startingEra = other.startingEra;
        this.startingEnergy = other.startingEnergy;
        this.freeBatteryUses = other.freeBatteryUses;
    }

    @Override
    public String toString() {
        return String.format("GameConfiguration{maxCycles=%d, missionsToWin=%d, maxVortexes=%d, " +
                        "maxHandSize=%d, serverPort=%d, chatPort=%d, " +
                        "startingEra='%s', startingEnergy=%d, freeBatteryUses=%d}",
                maxCycles, missionsToWin, maxVortexes, maxHandSize,
                serverPort, chatPort, startingEra, startingEnergy, freeBatteryUses);
    }
}