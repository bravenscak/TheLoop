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
import java.util.Set;
import java.util.HashSet;

public class MissionManager {

    private final Random random;

    public MissionManager(Random random) {
        this.random = random;
    }

    public void initializeMissions(GameState gameState) {
        System.out.println("🎯 Initializing missions with smart selection...");

        List<Era> erasNeedingStabilization = getErasNeedingWork(gameState);
        List<Era> erasForEnergyGoals = getErasForEnergyMissions(gameState);

        Mission firstMission;
        if (!erasNeedingStabilization.isEmpty()) {
            Era targetEra = erasNeedingStabilization.get(random.nextInt(erasNeedingStabilization.size()));
            firstMission = createStabilizeMission(targetEra, gameState);
            System.out.println("🎯 Created URGENT stabilization mission for " + targetEra.getDisplayName() +
                    " (has " + gameState.getRifts(targetEra) + " rifts)");
        } else {
            Era targetEra = erasForEnergyGoals.get(random.nextInt(erasForEnergyGoals.size()));
            firstMission = new EnergySurgeMission(targetEra);
            System.out.println("🎯 Created energy surge mission for " + targetEra.getDisplayName());
        }

        Mission secondMission;
        if (firstMission instanceof StabilizeEraMission) {
            Era targetEra = erasForEnergyGoals.get(random.nextInt(erasForEnergyGoals.size()));
            secondMission = new EnergySurgeMission(targetEra);
        } else {
            secondMission = new HuntDuplicatesMission();
        }

        gameState.addMission(firstMission);
        gameState.addMission(secondMission);

        System.out.println("🎯 Missions initialized with meaningful challenges:");
        for (Mission mission : gameState.getActiveMissions()) {
            System.out.println("  - " + mission.toString());
        }
    }

    private List<Era> getErasNeedingWork(GameState gameState) {
        List<Era> needsWork = new ArrayList<>();
        for (Era era : Era.values()) {
            if (gameState.getRifts(era) > 0 && !gameState.hasVortex(era)) {
                needsWork.add(era);
            }
        }
        return needsWork;
    }

    private List<Era> getErasForEnergyMissions(GameState gameState) {
        List<Era> suitable = new ArrayList<>();
        for (Era era : Era.values()) {
            if (!gameState.hasVortex(era) && gameState.getEnergy(era) < 3) {
                suitable.add(era);
            }
        }
        if (suitable.isEmpty()) {
            for (Era era : Era.values()) {
                if (!gameState.hasVortex(era)) {
                    suitable.add(era);
                }
            }
        }
        return suitable;
    }

    private StabilizeEraMission createStabilizeMission(Era era, GameState gameState) {
        StabilizeEraMission mission = new StabilizeEraMission(era);
        mission.setInitialRiftCount(gameState.getRifts(era));
        return mission;
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
            System.out.println("🎯 Mission completed: " + mission.getName());

            if (gameState.getActiveMissions().size() < 2) {
                addSingleSmartMission(gameState);
            }
        }

        if (gameState.getTotalMissionsCompleted() >= 4) {
            gameState.endGame(GameResult.VICTORY);
            System.out.println("🎉 VICTORY: 4 missions completed through player effort!");
        }
    }

    private void addSingleSmartMission(GameState gameState) {
        System.out.println("🎯 Adding single smart mission...");

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

        System.out.println("🎯 Existing mission types: " + existingMissionTypes);

        Mission newMission = createDifferentMissionType(gameState, existingMissionTypes);

        if (newMission != null) {
            gameState.addMission(newMission);
            System.out.println("🎯 New mission added: " + newMission.toString());
        } else {
            System.out.println("⚠️ Could not create suitable mission - skipping");
        }
    }

    private Mission createDifferentMissionType(GameState gameState, Set<String> existingTypes) {
        List<String> availableTypes = new ArrayList<>();

        if (!existingTypes.contains("STABILIZE") && hasErasNeedingStabilization(gameState)) {
            availableTypes.add("STABILIZE");
        }
        if (!existingTypes.contains("ENERGY") && hasErasForEnergy(gameState)) {
            availableTypes.add("ENERGY");
        }
        if (!existingTypes.contains("HUNT")) {
            availableTypes.add("HUNT");
        }

        if (availableTypes.isEmpty()) {
            if (hasErasNeedingStabilization(gameState)) availableTypes.add("STABILIZE");
            if (hasErasForEnergy(gameState)) availableTypes.add("ENERGY");
            availableTypes.add("HUNT");
        }

        if (availableTypes.isEmpty()) {
            return null;
        }

        String chosenType = availableTypes.get(random.nextInt(availableTypes.size()));
        System.out.println("🎯 Creating mission of type: " + chosenType);

        return switch (chosenType) {
            case "STABILIZE" -> createStabilizeMissionForSpawn(gameState);
            case "ENERGY" -> createEnergyMissionForSpawn(gameState);
            case "HUNT" -> new HuntDuplicatesMission();
            default -> null;
        };
    }

    private boolean hasErasNeedingStabilization(GameState gameState) {
        for (Era era : Era.values()) {
            if (gameState.getRifts(era) > 0 && !gameState.hasVortex(era)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasErasForEnergy(GameState gameState) {
        for (Era era : Era.values()) {
            if (!gameState.hasVortex(era)) {
                return true;
            }
        }
        return false;
    }

    private StabilizeEraMission createStabilizeMissionForSpawn(GameState gameState) {
        List<Era> needsWork = new ArrayList<>();
        for (Era era : Era.values()) {
            if (gameState.getRifts(era) > 0 && !gameState.hasVortex(era)) {
                needsWork.add(era);
            }
        }

        if (needsWork.isEmpty()) return null;

        Era targetEra = needsWork.get(random.nextInt(needsWork.size()));
        StabilizeEraMission mission = new StabilizeEraMission(targetEra);
        mission.setInitialRiftCount(gameState.getRifts(targetEra));
        return mission;
    }

    private EnergySurgeMission createEnergyMissionForSpawn(GameState gameState) {
        List<Era> suitable = new ArrayList<>();
        for (Era era : Era.values()) {
            if (!gameState.hasVortex(era)) {
                suitable.add(era);
            }
        }

        if (suitable.isEmpty()) return null;

        Era targetEra = suitable.get(random.nextInt(suitable.size()));
        return new EnergySurgeMission(targetEra);
    }
}