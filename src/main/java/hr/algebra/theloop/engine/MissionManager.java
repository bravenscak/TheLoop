package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.GameResult;
import hr.algebra.theloop.missions.Mission;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.missions.EnergySurgeMission;
import hr.algebra.theloop.missions.StabilizeEraMission;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MissionManager {

    private final Random random;

    public MissionManager(Random random) {
        this.random = random;
    }

    public void initializeMissions(GameState gameState) {
        gameState.addMission(new StabilizeEraMission(Era.MEDIEVAL));
        gameState.addMission(new EnergySurgeMission(Era.RENAISSANCE));

        System.out.println("🎯 Missions initialized:");
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
        Era[] eras = Era.values();
        Era randomEra = eras[random.nextInt(eras.length)];

        if (random.nextBoolean()) {
            gameState.addMission(new StabilizeEraMission(randomEra));
        } else {
            gameState.addMission(new EnergySurgeMission(randomEra));
        }

        System.out.println("🎯 New mission added!");
    }
}