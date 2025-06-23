package hr.algebra.theloop.engine;

import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.networking.NetworkManager;
import hr.algebra.theloop.utils.GameLogger;

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

        endAllPlayerTurns();
        startPlayerTurn();
    }

    public void processMultiplayerTurn(PlayerManager playerManager, GameState gameState,
                                       DrFooAI drFooAI, CardAcquisitionManager cardManager,
                                       NetworkManager networkManager, int localPlayerIndex) {
        if (!networkManager.isMultiplayer() || localPlayerIndex == 0) {
            processDrFooTurn(drFooAI, gameState, cardManager);
            GameLogger.gameFlow("🎮 Player 1: Processed Dr. Foo turn");
        }
    }

    public void endMultiplayerTurn(PlayerManager playerManager, GameState gameState,
                                   NetworkManager networkManager, int localPlayerIndex) {
        if (!isWaitingForPlayerInput()) return;

        if (networkManager.isMultiplayer()) {
            if (localPlayerIndex != 0) {
                GameLogger.warning("Only Player 1 can end turn in multiplayer!");
                return;
            }

            playerManager.endPlayerTurns();
            gameState.saveAllPlayerStates(playerManager.getPlayers(), playerManager.getCurrentPlayerIndex());
        } else {
            String currentPlayerName = playerManager.getCurrentPlayer().getName();
            endPlayerTurn(playerManager.getPlayers(), gameState);
            GameLogger.gameFlow("Turn ended by " + currentPlayerName);
        }
    }

    public void setupMultiplayerMode(NetworkManager networkManager, int localPlayerIndex) {
        if (networkManager.isMultiplayer()) {
            setWaitingForPlayerInput(true);

            if (localPlayerIndex == 0) {
                Thread.ofVirtual().start(() -> {
                    try {
                        Thread.sleep(2000);
                        GameLogger.gameFlow("🎮 Player 1: Ready for mission broadcast");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } else {
            startPlayerTurn();
        }
    }

    public boolean isWaitingForPlayerInput() {
        return waitingForPlayerInput;
    }

    public void setWaitingForPlayerInput(boolean waiting) {
        this.waitingForPlayerInput = waiting;
    }

    private void endAllPlayerTurns() {
    }
}