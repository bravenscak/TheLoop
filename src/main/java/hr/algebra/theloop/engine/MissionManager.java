package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.GameResult;
import hr.algebra.theloop.missions.Mission;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.missions.EnergySurgeMission;
import hr.algebra.theloop.missions.StabilizeEraMission;
import hr.algebra.theloop.missions.HuntDuplicatesMission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MissionManager {

    private final Random random;
    private final List<Era> availableEras;

    public MissionManager(Random random) {
        this.random = random;
        this.availableEras = new ArrayList<>();
        Collections.addAll(availableEras, Era.values());
    }

    public void initializeMissions(GameState gameState) {
        Collections.shuffle(availableEras, random);

        Era firstEra = availableEras.get(0);
        Era secondEra = availableEras.get(1);

        Mission firstMission = createRandomMission(firstEra, gameState);
        Mission secondMission = createDifferentTypeMission(secondEra, gameState, firstMission.getClass());

        gameState.addMission(firstMission);
        gameState.addMission(secondMission);

        System.out.println("🎯 Random missions initialized:");
        for (Mission mission : gameState.getActiveMissions()) {
            System.out.println("  - " + mission.toString());
        }
    }

    public void checkAllMissions(GameState gameState, Player player, String actionType) {
        List<Mission> missionsToComplete = new ArrayList<>();

        for (Mission mission : gameState.getActiveMissions()) {
            if (!mission.isCompleted() && mission.checkProgress(gameState, player, actionType)) {
                if (mission.isCompleted()) {
                    missionsToComplete.add(mission);
                }
            }
        }

        for (Mission mission : missionsToComplete) {
            gameState.completeMission(mission);
            if (gameState.getActiveMissions().size() < 2) {
                addRandomMission(gameState);
            }
        }

        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            System.out.println("🎉 VICTORY: 4 missions completed!");
        }
    }

    private void addRandomMission(GameState gameState) {
        Era randomEra = getRandomAvailableEra(gameState);
        Mission newMission = createRandomMission(randomEra, gameState);

        gameState.addMission(newMission);
        System.out.println("🎯 New random mission added: " + newMission.toString());
    }

    private Era getRandomAvailableEra(GameState gameState) {
        List<Era> nonVortexEras = new ArrayList<>();
        for (Era era : Era.values()) {
            if (!gameState.hasVortex(era)) {
                nonVortexEras.add(era);
            }
        }

        if (!nonVortexEras.isEmpty()) {
            return nonVortexEras.get(random.nextInt(nonVortexEras.size()));
        } else {
            Era[] eras = Era.values();
            return eras[random.nextInt(eras.length)];
        }
    }

    private Mission createRandomMission(Era era, GameState gameState) {
        int missionType = random.nextInt(3);

        return switch (missionType) {
            case 0 -> new StabilizeEraMission(era);
            case 1 -> new EnergySurgeMission(era);
            case 2 -> new HuntDuplicatesMission();
            default -> new StabilizeEraMission(era);
        };
    }

    public void printMissionStats(GameState gameState) {
        System.out.println("\n=== MISSION STATS ===");
        System.out.println("Active missions: " + gameState.getActiveMissions().size());
        System.out.println("Completed missions: " + gameState.getTotalMissionsCompleted() + "/4");

        int stabilize = 0, energy = 0, hunt = 0;
        for (Mission mission : gameState.getActiveMissions()) {
            if (mission instanceof StabilizeEraMission) stabilize++;
            else if (mission instanceof EnergySurgeMission) energy++;
            else if (mission instanceof HuntDuplicatesMission) hunt++;
        }

        System.out.println("Mission types - Stabilize: " + stabilize +
                ", Energy: " + energy + ", Hunt: " + hunt);
    }

    private boolean hasTooManyOfSameType(GameState gameState, Class<? extends Mission> missionClass) {
        long count = gameState.getActiveMissions().stream()
                .filter(mission -> mission.getClass().equals(missionClass))
                .count();
        return count >= 2;
    }

    private Mission createDifferentTypeMission(Era era, GameState gameState, Class<? extends Mission> existingType) {
        List<Integer> availableTypes = new ArrayList<>();

        if (!existingType.equals(StabilizeEraMission.class)) {
            availableTypes.add(0);
        }
        if (!existingType.equals(EnergySurgeMission.class)) {
            availableTypes.add(1);
        }
        if (!existingType.equals(HuntDuplicatesMission.class)) {
            availableTypes.add(2);
        }

        if (availableTypes.isEmpty()) {
            return createRandomMission(era, gameState);
        }

        int chosenType = availableTypes.get(random.nextInt(availableTypes.size()));

        return switch (chosenType) {
            case 0 -> new StabilizeEraMission(era);
            case 1 -> new EnergySurgeMission(era);
            case 2 -> new HuntDuplicatesMission();
            default -> new StabilizeEraMission(era);
        };
    }
    private Mission createBalancedMission(Era era, GameState gameState) {
        List<Integer> availableTypes = new ArrayList<>();

        if (!hasTooManyOfSameType(gameState, StabilizeEraMission.class)) {
            availableTypes.add(0);
        }
        if (!hasTooManyOfSameType(gameState, EnergySurgeMission.class)) {
            availableTypes.add(1);
        }
        if (!hasTooManyOfSameType(gameState, HuntDuplicatesMission.class)) {
            availableTypes.add(2);
        }

        if (availableTypes.isEmpty()) {
            availableTypes.add(random.nextInt(3));
        }

        int chosenType = availableTypes.get(random.nextInt(availableTypes.size()));

        return switch (chosenType) {
            case 0 -> new StabilizeEraMission(era);
            case 1 -> new EnergySurgeMission(era);
            case 2 -> new HuntDuplicatesMission();
            default -> new StabilizeEraMission(era);
        };
    }
}