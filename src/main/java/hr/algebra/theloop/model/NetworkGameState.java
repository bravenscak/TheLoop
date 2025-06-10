package hr.algebra.theloop.model;

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
                lastPlayerName
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

        if (playerStates != null) {
            gameState.setPlayerStates(playerStates);
        }
    }
}