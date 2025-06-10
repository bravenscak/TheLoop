package hr.algebra.theloop.input;

import hr.algebra.theloop.cards.MovementCard;
import hr.algebra.theloop.cards.ArtifactCard;
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
            if (selectedCard.getCard() instanceof MovementCard) {
                return cardActionHandler.handleMovementCard(playerToControl, selectedCardIndex, era, selectedCard);
            } else if (cardActionHandler.isDuplicateCard(selectedCard.getCard())) {
                return cardActionHandler.handleDuplicateCard(playerToControl, selectedCardIndex, era, selectedCard);
            } else {
                return cardActionHandler.handleRegularCard(playerToControl, selectedCardIndex, era);
            }
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

        int readiedCards = 0;
        for (ArtifactCard card : playerToControl.getHand()) {
            if (card.isExhausted()) {
                card.ready();
                readiedCards++;
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