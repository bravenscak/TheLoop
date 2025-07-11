package hr.algebra.theloop.model;

import hr.algebra.theloop.config.ConfigurationManager;
import hr.algebra.theloop.missions.Mission;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;

@Data
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MAX_DUPLICATES_IN_BAG = 28;

    @NonNull private Era drFooPosition;
    private int drFooMovesThisCycle;
    private int currentCycle;

    private List<Mission> activeMissions;
    private List<Mission> completedMissions;
    private int totalMissionsCompleted;

    private int turnNumber;
    private boolean gameOver;
    private GameResult gameResult;

    private GameResources resources;

    private List<PlayerData> playerStates;
    private int currentPlayerIndex;

    private long randomSeed = System.currentTimeMillis();

    public GameState() {
        this.drFooPosition = Era.DAWN_OF_TIME;
        this.drFooMovesThisCycle = 0;
        this.currentCycle = 1;
        this.turnNumber = 1;
        this.gameOver = false;

        this.activeMissions = new ArrayList<>();
        this.completedMissions = new ArrayList<>();
        this.totalMissionsCompleted = 0;

        this.resources = new GameResources();

        this.playerStates = new ArrayList<>();
        this.currentPlayerIndex = 0;
    }

    public int recalculateDuplicatesInBag() {
        int duplicatesOnBoard = 0;
        for (Era era : Era.values()) {
            duplicatesOnBoard += getDuplicateCount(era);
        }
        return Math.max(0, MAX_DUPLICATES_IN_BAG - duplicatesOnBoard);
    }

    public void savePlayerState(Player player) {
        PlayerData playerData = new PlayerData(player);
        playerStates.removeIf(ps -> ps.getName().equals(player.getName()));
        playerStates.add(playerData);
    }

    public void saveAllPlayerStates(List<Player> players, int currentPlayerIdx) {
        this.currentPlayerIndex = currentPlayerIdx;
        this.playerStates.clear();
        for (Player player : players) {
            savePlayerState(player);
        }
    }

    public void restorePlayerState(Player player) {
        PlayerData playerData = playerStates.stream()
                .filter(ps -> ps.getName().equals(player.getName()))
                .findFirst()
                .orElse(null);

        if (playerData != null) {
            playerData.restoreToPlayer(player);
        }
    }

    public boolean hasPlayerStates() {
        return playerStates != null && !playerStates.isEmpty();
    }

    public List<String> getSavedPlayerNames() {
        return playerStates.stream().map(PlayerData::getName).toList();
    }

    public int getRifts(Era era) { return resources.getRifts(era); }
    public void addRifts(Era era, int amount) {
        resources.addRifts(era, amount);
        if (resources.getRifts(era) >= 3 && !resources.hasVortex(era)) {
            createVortex(era);
        }
    }
    public void removeRifts(Era era, int amount) { resources.removeRifts(era, amount); }

    public int getEnergy(Era era) { return resources.getEnergy(era); }
    public void addEnergy(Era era, int amount) { resources.addEnergy(era, amount); }
    public void removeEnergy(Era era, int amount) { resources.removeEnergy(era, amount); }

    public boolean hasVortex(Era era) { return resources.hasVortex(era); }
    public void createVortex(Era era) {
        resources.createVortex(era);
        activeMissions.removeIf(mission -> era.equals(mission.getAssignedEra()));

        if (getVortexCount() >= ConfigurationManager.INSTANCE.getMaxVortexes()) {
            endGame(GameResult.DEFEAT_VORTEXES);
        }
    }
    public int getVortexCount() { return resources.getVortexCount(); }

    public List<Duplicate> getDuplicatesAt(Era era) { return resources.getDuplicatesAt(era); }
    public void addDuplicate(Era era, Duplicate duplicate) { resources.addDuplicate(era, duplicate); }
    public boolean removeDuplicate(Era era, Duplicate duplicate) { return resources.removeDuplicate(era, duplicate); }
    public int getDuplicateCount(Era era) { return resources.getDuplicateCount(era); }

    public void clearDuplicatesAt(Era era) {
        if (resources != null && resources.getDuplicates() != null) {
            resources.getDuplicates().get(era).clear();
        }
    }

    public void moveDrFoo() {
        drFooPosition = drFooPosition.getNext();
        drFooMovesThisCycle++;

        if (drFooMovesThisCycle >= 7) {
            completeCycle();
        }
    }

    private void completeCycle() {
        currentCycle++;
        drFooMovesThisCycle = 0;

        if (currentCycle > 3) {
            endGame(GameResult.DEFEAT_CYCLES);
        }
    }

    public void addMission(Mission mission) {
        activeMissions.add(mission);
    }

    public void completeMission(Mission mission) {
        if (activeMissions.remove(mission)) {
            completedMissions.add(mission);
            totalMissionsCompleted++;

            if (totalMissionsCompleted >= 4) {
                endGame(GameResult.VICTORY);
            }
        }
    }

    public boolean isGameWon() { return totalMissionsCompleted >= 4; }
    public boolean isGameLost() { return resources.getVortexCount() >= 3 || currentCycle > 3; }

    public void endGame(GameResult result) {
        this.gameOver = true;
        this.gameResult = result;
    }

    public void nextTurn() { turnNumber++; }

    public int getTotalRifts() { return resources.getTotalRifts(); }
    public int getTotalEnergy() { return resources.getTotalEnergy(); }
    public Era getEraWithMostRifts() { return resources.getEraWithMostRifts(); }
    public Era getEraWithMostEnergy() { return resources.getEraWithMostEnergy(); }
    public long getRandomSeed() { return randomSeed; }
    public void setRandomSeed(long seed) { this.randomSeed = seed; }

    @Override
    public String toString() {
        return String.format("GameState[Turn: %d, Dr.Foo: %s, Cycle: %d, Missions: %d/4, Vortexes: %d/3]",
                turnNumber, drFooPosition.getDisplayName(), currentCycle,
                totalMissionsCompleted, resources.getVortexCount());
    }

    public void incrementMissionsCompleted() {
        this.totalMissionsCompleted++;
    }
}