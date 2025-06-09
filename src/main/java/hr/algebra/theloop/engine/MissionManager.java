package hr.algebra.theloop.engine;

import hr.algebra.theloop.missions.*;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.GameResult;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.utils.GameLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MissionManager {

    private final MissionFactory missionFactory;

    public MissionManager(Random random) {
        this.missionFactory = new MissionFactory(random);
    }

    public void initializeMissions(GameState gameState) {
        List<Mission> initialMissions = missionFactory.createInitialMissions(gameState);

        for (Mission mission : initialMissions) {
            gameState.addMission(mission);
            GameLogger.mission("Initialized: " + mission.getName());
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
            GameLogger.mission("COMPLETED: " + mission.getName());

            if (gameState.getActiveMissions().size() < 2) {
                addNewMission(gameState);
            }
        }

        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            GameLogger.gameEnd("VICTORY: 4 missions completed through player effort!");
        }
    }

    private void addNewMission(GameState gameState) {
        Set<String> existingMissionTypes = new HashSet<>();
        for (Mission mission : gameState.getActiveMissions()) {
            if (mission instanceof StabilizeEraMission) {
                existingMissionTypes.add("STABILIZE");
            } else if (mission instanceof EnergySurgeMission) {
                existingMissionTypes.add("ENERGY");
            } else if (mission instanceof HuntDuplicatesMission) {
                existingMissionTypes.add("HUNT");
            }
        }

        Mission newMission = missionFactory.createNewMission(gameState, new ArrayList<>(existingMissionTypes));

        if (newMission != null) {
            gameState.addMission(newMission);
            GameLogger.mission("New: " + newMission.getName());
        }
    }
}