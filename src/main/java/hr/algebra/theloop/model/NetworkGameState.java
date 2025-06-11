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
            String type = "HUNT"; // default
            if (mission instanceof StabilizeEraMission) {
                type = "STABILIZE";
            } else if (mission instanceof EnergySurgeMission) {
                type = "ENERGY";
            }

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

        public Mission toMission() {
            Mission mission = switch (missionType) {
                case "STABILIZE" -> new StabilizeEraMission(assignedEra);
                case "ENERGY" -> new EnergySurgeMission(assignedEra);
                default -> new HuntDuplicatesMission();
            };

            mission.setCurrentProgress(currentProgress);
            mission.setCompleted(completed);
            return mission;
        }
    }

    public static NetworkGameState fromGameState(GameState gameState, PlayerMode playerMode,
                                                 String lastAction, String lastPlayerName) {
        Era[] eras = Era.values();
        int eraCount = eras.length;

        int[] rifts = new int[eraCount];
        int[] energy = new int[eraCount];
        boolean[] vortex = new boolean[eraCount];
        int[] duplicates = new int[eraCount];
        List<List<DuplicateInfo>> duplicateDetails = new ArrayList<>();

        for (int i = 0; i < eraCount; i++) {
            Era era = eras[i];
            rifts[i] = gameState.getRifts(era);
            energy[i] = gameState.getEnergy(era);
            vortex[i] = gameState.hasVortex(era);
            duplicates[i] = gameState.getDuplicateCount(era);

            List<DuplicateInfo> eraDetails = new ArrayList<>();
            for (Duplicate dup : gameState.getDuplicatesAt(era)) {
                eraDetails.add(DuplicateInfo.fromDuplicate(dup));
            }
            duplicateDetails.add(eraDetails);
        }

        List<MissionData> activeMissionsData = gameState.getActiveMissions().stream()
                .map(MissionData::fromMission)
                .toList();

        List<MissionData> completedMissionsData = gameState.getCompletedMissions().stream()
                .map(MissionData::fromMission)
                .toList();

        return new NetworkGameState(
                gameState.getTurnNumber(),
                gameState.getDrFooPosition(),
                gameState.getCurrentCycle(),
                gameState.isGameOver(),
                gameState.getGameResult(),
                rifts,
                energy,
                vortex,
                duplicates,
                duplicateDetails,
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

    public void applyToGameState(GameState gameState) {
        gameState.setTurnNumber(turnNumber);
        gameState.setDrFooPosition(drFooPosition);
        gameState.setCurrentCycle(currentCycle);
        gameState.setGameOver(gameOver);
        gameState.setGameResult(gameResult);
        gameState.setCurrentPlayerIndex(currentPlayerIndex);

        Era[] eras = Era.values();
        for (int i = 0; i < eras.length; i++) {
            Era era = eras[i];

            gameState.getResources().getRifts().put(era, riftsPerEra[i]);
            gameState.getResources().getEnergy().put(era, energyPerEra[i]);

            if (vortexPerEra[i]) {
                gameState.getResources().getVortexes().add(era);
            } else {
                gameState.getResources().getVortexes().remove(era);
            }

            gameState.getResources().getDuplicates().get(era).clear();

            if (duplicateDetails != null && i < duplicateDetails.size()) {
                for (DuplicateInfo dupInfo : duplicateDetails.get(i)) {
                    Duplicate newDup = dupInfo.toDuplicate();
                    gameState.getResources().getDuplicates().get(era).add(newDup);
                }
            }
        }

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

        gameState.setTotalMissionsCompleted(totalMissionsCompleted);

        if (playerStates != null) {
            gameState.setPlayerStates(playerStates);
        }
    }
}