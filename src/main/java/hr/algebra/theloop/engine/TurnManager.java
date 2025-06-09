package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

import java.util.List;

public class TurnManager {

    private boolean waitingForPlayerInput;

    public TurnManager() {
        this.waitingForPlayerInput = false;
    }

    public void startPlayerTurn() {
        waitingForPlayerInput = true;
    }

    public void endPlayerTurn(List<Player> players, GameState gameState) {
        waitingForPlayerInput = false;

        for (Player player : players) {
            player.discardHand();
            player.rechargeBatteries();
            player.drawToFullHand();
        }

        gameState.nextTurn();
    }

    public void processDrFooTurn(DrFooAI drFooAI, GameState gameState, CardAcquisitionManager cardManager) {
        drFooAI.executeDrFooPhase(gameState);
        cardManager.addRandomCardsToEras(gameState, 1);
        startPlayerTurn();
    }

    public boolean isWaitingForPlayerInput() {
        return waitingForPlayerInput;
    }

    public void setWaitingForPlayerInput(boolean waiting) {
        this.waitingForPlayerInput = waiting;
    }
}