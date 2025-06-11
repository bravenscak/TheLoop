package hr.algebra.theloop.engine;

import hr.algebra.theloop.missions.EnergySurgeMission;
import hr.algebra.theloop.missions.HuntDuplicatesMission;
import hr.algebra.theloop.missions.Mission;
import hr.algebra.theloop.missions.StabilizeEraMission;
import hr.algebra.theloop.model.*;
import hr.algebra.theloop.utils.GameLogger;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class MissionManager {

    private final Random random;

    public void initializeMissions(GameState gameState) {
        if (!gameState.getActiveMissions().isEmpty()) {
            GameLogger.warning("Missions already initialized, skipping");
            return;
        }

        List<Era> availableEras = Arrays.asList(Era.values());

        Era firstEra = availableEras.get(random.nextInt(availableEras.size()));
        Era secondEra = availableEras.get(random.nextInt(availableEras.size()));

        Mission firstMission = createRandomMission(firstEra);
        Mission secondMission = createRandomMission(secondEra);

        gameState.getActiveMissions().add(firstMission);
        gameState.getActiveMissions().add(secondMission);

        GameLogger.missionCreated(firstMission);
        GameLogger.missionCreated(secondMission);
    }

    public void checkAllMissions(GameState gameState, Player player, String actionType) {
        if (gameState.getActiveMissions().isEmpty()) {
            GameLogger.gameFlow("No missions to check (waiting for sync)");
            return;
        }

        List<Mission> missionsToCheck = gameState.getActiveMissions().stream()
                .filter(mission -> !mission.isCompleted())
                .toList();

        for (Mission mission : missionsToCheck) {
            boolean wasCompleted = mission.isCompleted();

            mission.checkProgress(gameState, player, actionType);

            if (!wasCompleted && mission.isCompleted()) {
                gameState.getActiveMissions().remove(mission);
                gameState.getCompletedMissions().add(mission);
                gameState.incrementMissionsCompleted();

                GameLogger.missionCompleted(mission, player.getName());

                Era newEra = Era.values()[random.nextInt(Era.values().length)];
                Mission newMission = createRandomMission(newEra);
                gameState.getActiveMissions().add(newMission);

                GameLogger.missionCreated(newMission);
                break;
            }
        }
    }

    public boolean needsMissionSync(GameState gameState) {
        return gameState.getActiveMissions().isEmpty() && gameState.getCompletedMissions().isEmpty();
    }

    private Mission createRandomMission(Era era) {
        int missionType = random.nextInt(3);

        return switch (missionType) {
            case 0 -> new StabilizeEraMission(era);
            case 1 -> new EnergySurgeMission(era);
            default -> new HuntDuplicatesMission();
        };
    }
}