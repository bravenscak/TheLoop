package hr.algebra.theloop.missions;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MissionFactory {

    private static final String STABILIZE_TYPE = "STABILIZE";
    private static final String ENERGY_TYPE = "ENERGY";
    private static final String HUNT_TYPE = "HUNT";

    private final Random random;

    public MissionFactory(Random random) {
        this.random = random;
    }

    public List<Mission> createInitialMissions(GameState gameState) {
        List<Mission> missions = new ArrayList<>();

        List<Era> erasNeedingStabilization = getErasWithRifts(gameState);
        List<Era> erasForEnergyGoals = getErasForEnergy(gameState);

        Mission firstMission;
        if (!erasNeedingStabilization.isEmpty()) {
            Era targetEra = erasNeedingStabilization.get(random.nextInt(erasNeedingStabilization.size()));
            firstMission = createStabilizeMission(targetEra, gameState);
        } else {
            Era targetEra = erasForEnergyGoals.get(random.nextInt(erasForEnergyGoals.size()));
            firstMission = new EnergySurgeMission(targetEra);
        }

        Mission secondMission;
        if (firstMission instanceof StabilizeEraMission) {
            Era targetEra = erasForEnergyGoals.get(random.nextInt(erasForEnergyGoals.size()));
            secondMission = new EnergySurgeMission(targetEra);
        } else {
            secondMission = new HuntDuplicatesMission();
        }

        missions.add(firstMission);
        missions.add(secondMission);

        return missions;
    }

    public Mission createNewMission(GameState gameState, List<String> existingTypes) {
        List<String> availableTypes = new ArrayList<>();

        if (!existingTypes.contains(STABILIZE_TYPE) && hasErasWithRifts(gameState)) {
            availableTypes.add(STABILIZE_TYPE);
        }
        if (!existingTypes.contains(ENERGY_TYPE) && hasErasForEnergy(gameState)) {
            availableTypes.add(ENERGY_TYPE);
        }
        if (!existingTypes.contains(HUNT_TYPE)) {
            availableTypes.add(HUNT_TYPE);
        }

        if (availableTypes.isEmpty()) {
            if (hasErasWithRifts(gameState)) availableTypes.add(STABILIZE_TYPE);
            if (hasErasForEnergy(gameState)) availableTypes.add(ENERGY_TYPE);
            availableTypes.add(HUNT_TYPE);
        }

        if (availableTypes.isEmpty()) return null;

        String chosenType = availableTypes.get(random.nextInt(availableTypes.size()));

        return switch (chosenType) {
            case STABILIZE_TYPE -> createStabilizeMissionForSpawn(gameState);
            case ENERGY_TYPE -> createEnergyMissionForSpawn(gameState);
            case HUNT_TYPE -> new HuntDuplicatesMission();
            default -> null;
        };
    }

    private List<Era> getErasWithRifts(GameState gameState) {
        List<Era> needsWork = new ArrayList<>();
        for (Era era : Era.values()) {
            if (gameState.getRifts(era) > 0 && !gameState.hasVortex(era)) {
                needsWork.add(era);
            }
        }
        return needsWork;
    }

    private List<Era> getErasForEnergy(GameState gameState) {
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

    private boolean hasErasWithRifts(GameState gameState) {
        return !getErasWithRifts(gameState).isEmpty();
    }

    private boolean hasErasForEnergy(GameState gameState) {
        return !getErasForEnergy(gameState).isEmpty();
    }

    private StabilizeEraMission createStabilizeMission(Era era, GameState gameState) {
        StabilizeEraMission mission = new StabilizeEraMission(era);
        mission.setInitialRiftCount(gameState.getRifts(era));
        return mission;
    }

    private StabilizeEraMission createStabilizeMissionForSpawn(GameState gameState) {
        List<Era> needsWork = getErasWithRifts(gameState);
        if (needsWork.isEmpty()) return null;

        Era targetEra = needsWork.get(random.nextInt(needsWork.size()));
        return createStabilizeMission(targetEra, gameState);
    }

    private EnergySurgeMission createEnergyMissionForSpawn(GameState gameState) {
        List<Era> suitable = getErasForEnergy(gameState);
        if (suitable.isEmpty()) return null;

        Era targetEra = suitable.get(random.nextInt(suitable.size()));
        return new EnergySurgeMission(targetEra);
    }
}