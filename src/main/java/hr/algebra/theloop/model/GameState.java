package hr.algebra.theloop.model;

import hr.algebra.theloop.missions.Mission;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;

@Data
public class GameState implements Serializable {

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
    }

    public int getRifts(Era era) {
        return resources.getRifts(era);
    }

    public void addRifts(Era era, int amount) {
        resources.addRifts(era, amount);

        if (resources.getRifts(era) >= 3 && !resources.hasVortex(era)) {
            createVortex(era);
        }
    }

    public void removeRifts(Era era, int amount) {
        resources.removeRifts(era, amount);
    }

    public int getEnergy(Era era) {
        return resources.getEnergy(era);
    }

    public void addEnergy(Era era, int amount) {
        resources.addEnergy(era, amount);
    }

    public void removeEnergy(Era era, int amount) {
        resources.removeEnergy(era, amount);
    }

    public boolean hasVortex(Era era) {
        return resources.hasVortex(era);
    }

    public void createVortex(Era era) {
        resources.createVortex(era);
        activeMissions.removeIf(mission -> era.equals(mission.getAssignedEra()));

        System.out.println("⚠️ VORTEX created at " + era.getDisplayName() + "!");
    }

    public int getVortexCount() {
        return resources.getVortexCount();
    }

    public List<Duplicate> getDuplicatesAt(Era era) {
        return resources.getDuplicatesAt(era);
    }

    public void addDuplicate(Era era, Duplicate duplicate) {
        resources.addDuplicate(era, duplicate);
    }

    public boolean removeDuplicate(Era era, Duplicate duplicate) {
        return resources.removeDuplicate(era, duplicate);
    }

    public int getDuplicateCount(Era era) {
        return resources.getDuplicateCount(era);
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
        return resources.getVortexCount() >= 3 || currentCycle > 3;
    }

    public void endGame(GameResult result) {
        this.gameOver = true;
        this.gameResult = result;
    }

    public void nextTurn() {
        turnNumber++;
    }

    public int getTotalRifts() {
        return resources.getTotalRifts();
    }

    public int getTotalEnergy() {
        return resources.getTotalEnergy();
    }

    public Era getEraWithMostRifts() {
        return resources.getEraWithMostRifts();
    }

    public Era getEraWithMostEnergy() {
        return resources.getEraWithMostEnergy();
    }

    @Override
    public String toString() {
        return String.format("GameState[Turn: %d, Dr.Foo: %s, Cycle: %d, Missions: %d/4, Vortexes: %d/3]",
                turnNumber, drFooPosition.getDisplayName(), currentCycle,
                totalMissionsCompleted, resources.getVortexCount());
    }
}