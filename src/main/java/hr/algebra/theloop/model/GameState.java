package hr.algebra.theloop.model;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Data
public class GameState implements Serializable {

    @NonNull private Era drFooPosition;
    private int drFooMovesThisCycle;
    private int currentCycle;

    private Map<Era, Integer> rifts;
    private Map<Era, Integer> energy;
    private Set<Era> vortexes;

    private Map<Era, List<Duplicate>> duplicates;

    private List<Mission> activeMissions;
    private List<Mission> completedMissions;
    private int totalMissionsCompleted;

    private int turnNumber;
    private boolean gameOver;
    private GameResult gameResult;

    public GameState() {
        this.drFooPosition = Era.DAWN_OF_TIME;
        this.drFooMovesThisCycle = 0;
        this.currentCycle = 1;
        this.turnNumber = 1;
        this.gameOver = false;

        this.rifts = new ConcurrentHashMap<>();
        this.energy = new ConcurrentHashMap<>();
        this.vortexes = ConcurrentHashMap.newKeySet();
        this.duplicates = new ConcurrentHashMap<>();

        this.activeMissions = new ArrayList<>();
        this.completedMissions = new ArrayList<>();
        this.totalMissionsCompleted = 0;

        for (Era era : Era.values()) {
            rifts.put(era, 0);
            energy.put(era, 1); // Each era starts with 1 energy
            duplicates.put(era, new ArrayList<>());
        }
    }

    public int getRifts(Era era) {
        return rifts.getOrDefault(era, 0);
    }

    public void addRifts(Era era, int amount) {
        int current = getRifts(era);
        rifts.put(era, current + amount);

        if (current + amount >= 3 && !vortexes.contains(era)) {
            createVortex(era);
        }
    }

    public void removeRifts(Era era, int amount) {
        int current = getRifts(era);
        int newAmount = Math.max(0, current - amount);
        rifts.put(era, newAmount);
    }

    public int getEnergy(Era era) {
        return energy.getOrDefault(era, 0);
    }

    public void addEnergy(Era era, int amount) {
        int current = getEnergy(era);
        energy.put(era, current + amount);
    }

    public void removeEnergy(Era era, int amount) {
        int current = getEnergy(era);
        int newAmount = Math.max(0, current - amount);
        energy.put(era, newAmount);
    }

    public boolean hasVortex(Era era) {
        return vortexes.contains(era);
    }

    public void createVortex(Era era) {
        vortexes.add(era);
        rifts.put(era, 0);
        activeMissions.removeIf(mission -> era.equals(mission.getAssignedEra()));

        System.out.println("⚠️ VORTEX created at " + era.getDisplayName() + "!");
    }

    public int getVortexCount() {
        return vortexes.size();
    }

    public List<Duplicate> getDuplicatesAt(Era era) {
        return new ArrayList<>(duplicates.getOrDefault(era, new ArrayList<>()));
    }

    public void addDuplicate(Era era, Duplicate duplicate) {
        duplicates.computeIfAbsent(era, k -> new ArrayList<>()).add(duplicate);
    }

    public boolean removeDuplicate(Era era, Duplicate duplicate) {
        List<Duplicate> eraDuplicates = duplicates.get(era);
        return eraDuplicates != null && eraDuplicates.remove(duplicate);
    }

    public int getDuplicateCount(Era era) {
        return duplicates.getOrDefault(era, new ArrayList<>()).size();
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

        System.out.println("🔄 Dr. Foo completed cycle " + (currentCycle - 1));
    }

    public void addMission(Mission mission) {
        activeMissions.add(mission);
    }

    public void completeMission(Mission mission) {
        if (activeMissions.remove(mission)) {
            completedMissions.add(mission);
            totalMissionsCompleted++;

            System.out.println("🎯 MISSION COMPLETED: " + mission.getName());

            if (totalMissionsCompleted >= 4) {
                endGame(GameResult.VICTORY);
            }
        }
    }

    public boolean isGameWon() {
        return totalMissionsCompleted >= 4;
    }

    public boolean isGameLost() {
        return vortexes.size() >= 3 || currentCycle > 3;
    }

    public void endGame(GameResult result) {
        this.gameOver = true;
        this.gameResult = result;
    }

    public void nextTurn() {
        turnNumber++;
    }

    public int getTotalRifts() {
        return rifts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getTotalEnergy() {
        return energy.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Era getEraWithMostRifts() {
        return rifts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Era.DAWN_OF_TIME);
    }

    public Era getEraWithMostEnergy() {
        return energy.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Era.DAWN_OF_TIME);
    }

    @Override
    public String toString() {
        return String.format("GameState[Turn: %d, Dr.Foo: %s, Cycle: %d, Missions: %d/4, Vortexes: %d/3]",
                turnNumber, drFooPosition.getDisplayName(), currentCycle,
                totalMissionsCompleted, vortexes.size());
    }
}