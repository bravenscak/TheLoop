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

    public MissionManager(Random random) {
        this.random = random;
    }

    public void initializeMissions(GameState gameState) {
        List<Era> availableEras = new ArrayList<>();
        Collections.addAll(availableEras, Era.values());
        Collections.shuffle(availableEras, random);

        Era firstEra = availableEras.get(0);
        Era secondEra = availableEras.get(1);

        Mission firstMission = createMissionOfType(0, firstEra);
        Mission secondMission = createMissionOfType(1, secondEra);

        gameState.addMission(firstMission);
        gameState.addMission(secondMission);

        System.out.println("🎯 Missions initialized (different types):");
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
        Mission newMission = createRandomMission(randomEra);

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

    private Mission createRandomMission(Era era) {
        int missionType = random.nextInt(3);
        return createMissionOfType(missionType, era);
    }

    private Mission createMissionOfType(int type, Era era) {
        return switch (type) {
            case 0 -> new StabilizeEraMission(era);
            case 1 -> new EnergySurgeMission(era);
            case 2 -> new HuntDuplicatesMission();
            default -> new StabilizeEraMission(era);
        };
    }
}