package hr.algebra.theloop.input;

import hr.algebra.theloop.controller.CardController;
import hr.algebra.theloop.engine.GameEngine;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Player;


public class PlayerInputHandler {

    private final GameEngine gameEngine;
    private CardController selectedCard = null;
    private int selectedCardIndex = -1;

    public PlayerInputHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    public boolean selectCard(CardController cardController, int cardIndex) {
        System.out.println("🔧 DEBUG: selectCard called for index " + cardIndex);

        if (!canAcceptInput()) {
            System.out.println("❌ DEBUG: Cannot accept input in selectCard!");
            return false;
        }

        if (cardController.isEmpty() || !cardController.canPlayCard()) {
            System.out.println("❌ DEBUG: Card is empty or cannot play!");
            return false;
        }

        if (selectedCard != null) {
            System.out.println("🔧 DEBUG: Clearing previous selection: " + selectedCard);
            selectedCard.setSelected(false);
        }

        if (selectedCard == cardController) {
            System.out.println("🔧 DEBUG: Deselecting same card");
            clearSelection();
        } else {
            selectedCard = cardController;
            selectedCardIndex = cardIndex;
            cardController.setSelected(true);
            System.out.println("🔧 DEBUG: Selected card " + cardIndex + ", controller: " + cardController);
        }

        return true;
    }

    public boolean handleEraClick(Era era) {
        System.out.println("🔧 DEBUG: handleEraClick called for " + era);

        if (!canAcceptInput()) {
            System.out.println("❌ DEBUG: Cannot accept input!");
            return false;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        System.out.println("🔧 DEBUG: hasSelectedCard() = " + hasSelectedCard());
        System.out.println("🔧 DEBUG: selectedCard = " + selectedCard);
        System.out.println("🔧 DEBUG: selectedCardIndex = " + selectedCardIndex);

        if (hasSelectedCard()) {
            System.out.println("🔧 DEBUG: Attempting to play card on era");
            return playCardOnEra(currentPlayer, selectedCardIndex, era);
        } else {
            System.out.println("🔧 DEBUG: Attempting movement");
            return attemptMovement(currentPlayer, era);
        }
    }

    private boolean playCardOnEra(Player player, int cardIndex, Era targetEra) {
        boolean success = gameEngine.playCard(player, cardIndex, targetEra);

        if (success) {
            clearSelection();
        }

        return success;
    }

    private boolean attemptMovement(Player player, Era targetEra) {
        if (player.getCurrentEra().equals(targetEra)) {
            return false; // Already there
        }

        return gameEngine.movePlayer(player, targetEra);
    }

    public boolean performLoop() {
        if (!canAcceptInput()) {
            return false;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();

        boolean hasExhaustedCards = currentPlayer.getHand().stream()
                .anyMatch(card -> card.isExhausted());

        if (!hasExhaustedCards) {
            System.out.println("❌ No exhausted cards to LOOP");
            return false;
        }

        int loopCost = currentPlayer.getLoopsPerformedThisTurn() + 1;
        Era playerEra = currentPlayer.getCurrentEra();
        int availableEnergy = gameEngine.getGameState().getEnergy(playerEra);

        if (availableEnergy < loopCost) {
            System.out.println("❌ Not enough energy for LOOP! Need " + loopCost + ", have " + availableEnergy);
            return false;
        }

        gameEngine.getGameState().removeEnergy(playerEra, loopCost);

        int readiedCards = 0;
        for (var card : currentPlayer.getHand()) {
            if (card.isExhausted()) {
                card.ready();
                readiedCards++;
            }
        }

        currentPlayer.setLoopsPerformedThisTurn(currentPlayer.getLoopsPerformedThisTurn() + 1);

        System.out.println("🔄 LOOP performed! Readied " + readiedCards + " cards for " + loopCost + " energy");
        System.out.println("   Energy remaining: " + gameEngine.getGameState().getEnergy(playerEra));

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