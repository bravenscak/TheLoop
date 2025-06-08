package hr.algebra.theloop.ui;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.controller.CardController;
import hr.algebra.theloop.input.PlayerInputHandler;
import hr.algebra.theloop.model.Player;

import java.util.List;


public class PlayerHandManager {

    private final CardController card1Controller;
    private final CardController card2Controller;
    private final CardController card3Controller;

    public PlayerHandManager(CardController card1Controller,
                             CardController card2Controller,
                             CardController card3Controller) {
        this.card1Controller = card1Controller;
        this.card2Controller = card2Controller;
        this.card3Controller = card3Controller;
    }

    public void updateHand(Player currentPlayer) {
        List<ArtifactCard> hand = currentPlayer.getHand();

        updateCardController(card1Controller, hand, 0);
        updateCardController(card2Controller, hand, 1);
        updateCardController(card3Controller, hand, 2);
    }

    private void updateCardController(CardController controller, List<ArtifactCard> hand, int index) {
        if (controller == null) return;

        if (hand.size() > index) {
            controller.setCard(hand.get(index));
        } else {
            controller.setEmpty();
        }
    }

    public void setupCardClickHandlers(PlayerInputHandler inputHandler) {
        if (card1Controller != null) {
            card1Controller.setClickHandler(() -> inputHandler.selectCard(card1Controller, 0));
        }
        if (card2Controller != null) {
            card2Controller.setClickHandler(() -> inputHandler.selectCard(card2Controller, 1));
        }
        if (card3Controller != null) {
            card3Controller.setClickHandler(() -> inputHandler.selectCard(card3Controller, 2));
        }
    }

    public void clearAllSelections() {
        if (card1Controller != null) {
            card1Controller.setSelected(false);
        }
        if (card2Controller != null) {
            card2Controller.setSelected(false);
        }
        if (card3Controller != null) {
            card3Controller.setSelected(false);
        }
    }

    public CardController getCardController(int index) {
        return switch (index) {
            case 0 -> card1Controller;
            case 1 -> card2Controller;
            case 2 -> card3Controller;
            default -> null;
        };
    }

    public boolean hasSelectedCard() {
        return (card1Controller != null && card1Controller.isSelected()) ||
                (card2Controller != null && card2Controller.isSelected()) ||
                (card3Controller != null && card3Controller.isSelected());
    }

    public CardController getSelectedCardController() {
        if (card1Controller != null && card1Controller.isSelected()) {
            return card1Controller;
        }
        if (card2Controller != null && card2Controller.isSelected()) {
            return card2Controller;
        }
        if (card3Controller != null && card3Controller.isSelected()) {
            return card3Controller;
        }
        return null;
    }

    public int getSelectedCardIndex() {
        if (card1Controller != null && card1Controller.isSelected()) {
            return 0;
        }
        if (card2Controller != null && card2Controller.isSelected()) {
            return 1;
        }
        if (card3Controller != null && card3Controller.isSelected()) {
            return 2;
        }
        return -1;
    }

    public void refreshAllCards() {
        if (card1Controller != null && !card1Controller.isEmpty()) {
            card1Controller.setCard(card1Controller.getCard());
        }
        if (card2Controller != null && !card2Controller.isEmpty()) {
            card2Controller.setCard(card2Controller.getCard());
        }
        if (card3Controller != null && !card3Controller.isEmpty()) {
            card3Controller.setCard(card3Controller.getCard());
        }
    }
}