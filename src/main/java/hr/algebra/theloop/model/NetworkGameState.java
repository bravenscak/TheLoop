package hr.algebra.theloop.model;

import hr.algebra.theloop.missions.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
public class NetworkGameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private int turnNumber;
    private Era drFooPosition;
    private int currentCycle;
    private boolean gameOver;
    private GameResult gameResult;

    private int[] riftsPerEra;
    private int[] energyPerEra;
    private boolean[] vortexPerEra;
    private int[] duplicateCountPerEra;

    private List<List<DuplicateInfo>> duplicateDetails;

    private List<PlayerData> playerStates;
    private int currentPlayerIndex;
    private PlayerMode activePlayerMode;

    private String lastAction;
    private String lastPlayerName;

    private List<MissionData> activeMissions;
    private List<MissionData> completedMissions;
    private int totalMissionsCompleted;

    @Data
    @AllArgsConstructor
    public static class DuplicateInfo implements Serializable {
        private Era spawnEra;
        private Era destroyEra;
        private Era currentEra;
        private int turnsActive;

        public static DuplicateInfo fromDuplicate(Duplicate dup) {
            return new DuplicateInfo(
                    dup.getSpawnEra(),
                    dup.getDestroyEra(),
                    dup.getCurrentEra(),
                    dup.getTurnsActive()
            );
        }

        public Duplicate toDuplicate() {
            return new Duplicate(spawnEra, currentEra, turnsActive);
        }
    }

    @Data
    @AllArgsConstructor
    public static class MissionData implements Serializable {
        private String name;
        private String description;
        private Era assignedEra;
        private int currentProgress;
        private int requiredProgress;
        private boolean completed;
        private String missionType;

        public static MissionData fromMission(Mission mission) {
            String type = determineMissionType(mission);

            return new MissionData(
                    mission.getName(),
                    mission.getDescription(),
                    mission.getAssignedEra(),
                    mission.getCurrentProgress(),
                    mission.getRequiredProgress(),
                    mission.isCompleted(),
                    type
            );
        }

        private static String determineMissionType(Mission mission) {
            if (mission instanceof StabilizeEraMission) {
                return "STABILIZE";
            } else if (mission instanceof EnergySurgeMission) {
                return "ENERGY";
            }
            return "HUNT";
        }

        public Mission toMission() {
            Mission mission = createMissionByType();
            mission.setCurrentProgress(currentProgress);
            mission.setCompleted(completed);
            return mission;
        }

        private Mission createMissionByType() {
            return switch (missionType) {
                case "STABILIZE" -> new StabilizeEraMission(assignedEra);
                case "ENERGY" -> new EnergySurgeMission(assignedEra);
                default -> new HuntDuplicatesMission();
            };
        }
    }

    public static NetworkGameState fromGameState(GameState gameState, PlayerMode playerMode,
                                                 String lastAction, String lastPlayerName) {
        Era[] eras = Era.values();
        int eraCount = eras.length;

        NetworkStateBuilder builder = new NetworkStateBuilder(eraCount);

        populateEraData(builder, gameState, eras);
        List<MissionData> activeMissionsData = convertMissions(gameState.getActiveMissions());
        List<MissionData> completedMissionsData = convertMissions(gameState.getCompletedMissions());

        return new NetworkGameState(
                gameState.getTurnNumber(),
                gameState.getDrFooPosition(),
                gameState.getCurrentCycle(),
                gameState.isGameOver(),
                gameState.getGameResult(),
                builder.rifts,
                builder.energy,
                builder.vortex,
                builder.duplicates,
                builder.duplicateDetails,
                gameState.getPlayerStates(),
                gameState.getCurrentPlayerIndex(),
                playerMode,
                lastAction,
                lastPlayerName,
                activeMissionsData,
                completedMissionsData,
                gameState.getTotalMissionsCompleted()
        );
    }

    private static void populateEraData(NetworkStateBuilder builder, GameState gameState, Era[] eras) {
        for (int i = 0; i < eras.length; i++) {
            Era era = eras[i];

            builder.rifts[i] = gameState.getRifts(era);
            builder.energy[i] = gameState.getEnergy(era);
            builder.vortex[i] = gameState.hasVortex(era);
            builder.duplicates[i] = gameState.getDuplicateCount(era);

            List<DuplicateInfo> eraDetails = convertDuplicates(gameState.getDuplicatesAt(era));
            builder.duplicateDetails.add(eraDetails);
        }
    }

    private static List<DuplicateInfo> convertDuplicates(List<Duplicate> duplicates) {
        List<DuplicateInfo> eraDetails = new ArrayList<>();
        for (Duplicate dup : duplicates) {
            eraDetails.add(DuplicateInfo.fromDuplicate(dup));
        }
        return eraDetails;
    }

    private static List<MissionData> convertMissions(List<Mission> missions) {
        return missions.stream()
                .map(MissionData::fromMission)
                .toList();
    }

    public void applyToGameState(GameState gameState) {
        updateBasicGameState(gameState);
        updateErasData(gameState);
        updateMissions(gameState);
        updatePlayerStates(gameState);
    }

    private void updateBasicGameState(GameState gameState) {
        gameState.setTurnNumber(turnNumber);
        gameState.setDrFooPosition(drFooPosition);
        gameState.setCurrentCycle(currentCycle);
        gameState.setGameOver(gameOver);
        gameState.setGameResult(gameResult);
        gameState.setCurrentPlayerIndex(currentPlayerIndex);
        gameState.setTotalMissionsCompleted(totalMissionsCompleted);
    }

    private void updateErasData(GameState gameState) {
        Era[] eras = Era.values();
        for (int i = 0; i < eras.length; i++) {
            Era era = eras[i];
            updateSingleEra(gameState, era, i);
        }
    }

    private void updateSingleEra(GameState gameState, Era era, int index) {
        gameState.getResources().getRifts().put(era, riftsPerEra[index]);
        gameState.getResources().getEnergy().put(era, energyPerEra[index]);

        updateVortexState(gameState, era, index);
        updateDuplicatesForEra(gameState, era, index);
    }

    private void updateVortexState(GameState gameState, Era era, int index) {
        if (vortexPerEra[index]) {
            gameState.getResources().getVortexes().add(era);
        } else {
            gameState.getResources().getVortexes().remove(era);
        }
    }

    private void updateDuplicatesForEra(GameState gameState, Era era, int index) {
        gameState.getResources().getDuplicates().get(era).clear();

        if (duplicateDetails != null && index < duplicateDetails.size()) {
            for (DuplicateInfo dupInfo : duplicateDetails.get(index)) {
                Duplicate newDup = dupInfo.toDuplicate();
                gameState.getResources().getDuplicates().get(era).add(newDup);
            }
        }
    }

    private void updateMissions(GameState gameState) {
        if (activeMissions != null) {
            gameState.getActiveMissions().clear();
            for (MissionData missionData : activeMissions) {
                gameState.getActiveMissions().add(missionData.toMission());
            }
        }

        if (completedMissions != null) {
            gameState.getCompletedMissions().clear();
            for (MissionData missionData : completedMissions) {
                gameState.getCompletedMissions().add(missionData.toMission());
            }
        }
    }

    private void updatePlayerStates(GameState gameState) {
        if (playerStates != null) {
            gameState.setPlayerStates(playerStates);
        }
    }

    private static class NetworkStateBuilder {
        final int[] rifts;
        final int[] energy;
        final boolean[] vortex;
        final int[] duplicates;
        final List<List<DuplicateInfo>> duplicateDetails;

        NetworkStateBuilder(int eraCount) {
            this.rifts = new int[eraCount];
            this.energy = new int[eraCount];
            this.vortex = new boolean[eraCount];
            this.duplicates = new int[eraCount];
            this.duplicateDetails = new ArrayList<>();
        }
    }
}