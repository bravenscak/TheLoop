package hr.algebra.theloop.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


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

    private List<PlayerData> playerStates;
    private int currentPlayerIndex;
    private PlayerMode activePlayerMode;

    private String lastAction;
    private String lastPlayerName;

    public static NetworkGameState fromGameState(GameState gameState, PlayerMode playerMode,
                                                 String lastAction, String lastPlayerName) {
        Era[] eras = Era.values();
        int eraCount = eras.length;

        int[] rifts = new int[eraCount];
        int[] energy = new int[eraCount];
        boolean[] vortex = new boolean[eraCount];
        int[] duplicates = new int[eraCount];

        for (int i = 0; i < eraCount; i++) {
            Era era = eras[i];
            rifts[i] = gameState.getRifts(era);
            energy[i] = gameState.getEnergy(era);
            vortex[i] = gameState.hasVortex(era);
            duplicates[i] = gameState.getDuplicateCount(era);
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
        }

        if (playerStates != null) {
            gameState.setPlayerStates(playerStates);
        }
    }
}