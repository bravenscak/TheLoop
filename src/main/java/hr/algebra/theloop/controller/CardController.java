package hr.algebra.theloop.controller;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.ui.CardDisplayManager;
import hr.algebra.theloop.ui.CardInteractionHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class CardController implements Initializable {

    @FXML private Rectangle cardBackground;
    @FXML private Text cardNameText;
    @FXML private Text dimensionIcon;
    @FXML private Text dimensionText;
    @FXML private Text descriptionText;
    @FXML private Text originalEraText;
    @FXML private Rectangle exhaustedOverlay;

    private CardDisplayManager displayManager;
    private CardInteractionHandler interactionHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayManager = new CardDisplayManager(
                cardBackground, cardNameText, dimensionIcon, dimensionText,
                descriptionText, originalEraText, exhaustedOverlay
        );

        interactionHandler = new CardInteractionHandler();

        setEmpty();
    }

    public void setClickHandler(Runnable handler) {
        interactionHandler.setClickHandler(handler);
    }

    public void setCard(ArtifactCard card) {
        interactionHandler.setCard(card);

        if (card != null) {
            displayManager.updateCardDisplay(card);
        } else {
            setEmpty();
        }
    }

    public void setEmpty() {
        interactionHandler.setEmpty();
        displayManager.setEmpty();
    }

    public void setSelected(boolean selected) {
        interactionHandler.setSelected(selected);
        displayManager.setSelected(selected);
    }

    @FXML
    private void onCardClicked(MouseEvent event) {
        interactionHandler.handleClick();
        event.consume();
    }

    public boolean canPlayCard() {
        return interactionHandler.canPlayCard();
    }

    public void playCard() {
        interactionHandler.playCard();
        if (interactionHandler.getCard() != null) {
            displayManager.updateCardDisplay(interactionHandler.getCard());
        }
    }

    public void readyCard() {
        interactionHandler.readyCard();
        if (interactionHandler.getCard() != null) {
            displayManager.updateCardDisplay(interactionHandler.getCard());
        }
    }

    public ArtifactCard getCard() {
        return interactionHandler.getCard();
    }

    public boolean isEmpty() {
        return interactionHandler.isEmpty();
    }

    public boolean isSelected() {
        return interactionHandler.isSelected();
    }

    public boolean isExhausted() {
        return interactionHandler.isExhausted();
    }
}