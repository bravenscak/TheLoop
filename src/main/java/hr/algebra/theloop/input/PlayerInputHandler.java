package hr.algebra.theloop.input;

import hr.algebra.theloop.cards.*;
import hr.algebra.theloop.controller.CardController;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;

public class PlayerInputHandler {

    private final GameEngine gameEngine;
    private final CardActionHandler cardActionHandler;

    private CardController selectedCard = null;
    private int selectedCardIndex = -1;

    public PlayerInputHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.cardActionHandler = new CardActionHandler(gameEngine);
    }

    public boolean selectCard(CardController cardController, int cardIndex) {
        if (!canAcceptInput()) return false;

        if (cardController.isEmpty() || !cardController.canPlayCard()) {
            return false;
        }

        if (selectedCard != null) {
            selectedCard.setSelected(false);
        }

        if (selectedCard == cardController) {
            clearSelection();
        } else {
            selectedCard = cardController;
            selectedCardIndex = cardIndex;
            cardController.setSelected(true);
        }

        return true;
    }

    public boolean handleEraClick(Era era) {
        if (!canAcceptInput()) return false;

        Player playerToControl = getPlayerToControl();

        if (hasSelectedCard()) {
            boolean success = false;

            if (selectedCard.getCard() instanceof MovementCard) {
                success = handleMovementCardFixed(playerToControl, selectedCardIndex, era);
            } else if (selectedCard.getCard() instanceof EnergyCard) {
                success = gameEngine.playCard(playerToControl, selectedCardIndex, era);
            } else if (selectedCard.getCard() instanceof RiftCard) {
                success = gameEngine.playCard(playerToControl, selectedCardIndex, era);
            } else if (cardActionHandler.isDuplicateCard(selectedCard.getCard())) {
                success = cardActionHandler.handleDuplicateCard(playerToControl, selectedCardIndex, era, selectedCard);
            } else {
                success = cardActionHandler.handleRegularCard(playerToControl, selectedCardIndex, era);
            }

            if (success) {
                clearSelection();
            }

            return success;
        } else {
            return attemptRegularMovement(playerToControl, era);
        }
    }

    private boolean attemptRegularMovement(Player player, Era targetEra) {
        if (player.getCurrentEra().equals(targetEra)) {
            return false;
        }

        return gameEngine.movePlayer(player, targetEra);
    }

    public boolean performLoop() {
        if (!canAcceptInput()) return false;

        Player playerToControl = getPlayerToControl();

        boolean hasExhaustedCards = playerToControl.getHand().stream()
                .anyMatch(ArtifactCard::isExhausted);

        if (!hasExhaustedCards) {
            return false;
        }

        int loopCost = playerToControl.getLoopsPerformedThisTurn() + 1;
        Era playerEra = playerToControl.getCurrentEra();
        int availableEnergy = gameEngine.getGameState().getEnergy(playerEra);

        if (availableEnergy < loopCost) {
            return false;
        }

        gameEngine.getGameState().removeEnergy(playerEra, loopCost);

        for (ArtifactCard card : playerToControl.getHand()) {
            if (card.isExhausted()) {
                card.ready();
            }
        }

        playerToControl.setLoopsPerformedThisTurn(playerToControl.getLoopsPerformedThisTurn() + 1);
        return true;
    }

    public boolean endPlayerTurn() {
        if (!gameEngine.isWaitingForPlayerInput()) {
            return false;
        }

        clearSelection();
        gameEngine.endPlayerTurn();
        return true;
    }

    public boolean processNextTurn() {
        if (gameEngine.isWaitingForPlayerInput()) {
            return false;
        }

        gameEngine.processTurn();
        return true;
    }

    public void clearSelection() {
        if (selectedCard != null) {
            selectedCard.setSelected(false);
            selectedCard = null;
            selectedCardIndex = -1;
        }
    }

    private boolean handleMovementCardFixed(Player player, int cardIndex, Era targetEra) {
        MovementCard movementCard = (MovementCard) selectedCard.getCard();
        Era currentEra = player.getCurrentEra();

        if (!movementCard.isValidTarget(currentEra, targetEra)) {
            return false;
        }

        if (!movementCard.canExecute(gameEngine.getGameState(), player)) {
            return false;
        }

        selectedCard.playCard();

        movementCard.executeMovement(gameEngine.getGameState(), player, targetEra);

        gameEngine.getMissionManager().checkAllMissions(
                gameEngine.getGameState(), player, "MovementCard");
        gameEngine.broadcastGameState("Used " + movementCard.getName(), player.getName());

        return true;
    }

    private boolean canAcceptInput() {
        return !gameEngine.isGameOver() && gameEngine.isWaitingForPlayerInput();
    }

    private Player getPlayerToControl() {
        return gameEngine.isMultiplayer() ? gameEngine.getLocalPlayer() : gameEngine.getCurrentPlayer();
    }

    public boolean hasSelectedCard() {
        return selectedCard != null && selectedCardIndex >= 0;
    }

    public CardController getSelectedCard() {
        return selectedCard;
    }

    public int getSelectedCardIndex() {
        return selectedCardIndex;
    }
}