package hr.algebra.theloop.ui;

import hr.algebra.theloop.cards.ArtifactCard;

public class CardInteractionHandler {

    private ArtifactCard card;
    private boolean isEmpty = true;
    private boolean isSelected = false;
    private Runnable clickHandler;

    public void setClickHandler(Runnable handler) {
        this.clickHandler = handler;
    }

    public void setCard(ArtifactCard card) {
        this.card = card;
        this.isEmpty = (card == null);
        this.isSelected = false;
    }

    public void setEmpty() {
        this.card = null;
        this.isEmpty = true;
        this.isSelected = false;
    }

    public void handleClick() {
        if (clickHandler != null) {
            clickHandler.run();
        }
    }

    public boolean canPlayCard() {
        if (isEmpty || card == null) {
            return false;
        }

        if (card.isExhausted()) {
            return false;
        }

        return true;
    }

    public void playCard() {
        if (!canPlayCard()) {
            return;
        }

        card.exhaust();
        setSelected(false);
    }

    public void readyCard() {
        if (card != null) {
            card.ready();
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public ArtifactCard getCard() {
        return card;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isExhausted() {
        return card != null && card.isExhausted();
    }
}