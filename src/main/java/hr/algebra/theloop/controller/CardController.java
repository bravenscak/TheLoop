package hr.algebra.theloop.controller;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardDimension;
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

    private ArtifactCard card;
    private boolean isEmpty = true;
    private boolean isSelected = false;
    private Runnable clickHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setEmpty();
    }

    public void setClickHandler(Runnable handler) {
        this.clickHandler = handler;
    }

    public void setCard(ArtifactCard card) {
        this.card = card;
        this.isEmpty = false;

        if (card != null) {
            updateCardDisplay();
        } else {
            setEmpty();
        }
    }

    public void setEmpty() {
        this.card = null;
        this.isEmpty = true;
        this.isSelected = false;

        if (cardNameText != null) {
            cardNameText.setText("Empty Slot");
            dimensionIcon.setText("");
            dimensionText.setText("");
            descriptionText.setText("No card");
            originalEraText.setText("");
        }

        updateCardStyle();
        setExhausted(false);
    }

    private void updateCardDisplay() {
        if (card == null) return;

        cardNameText.setText(card.getName());
        dimensionIcon.setText(card.getDimension().getIcon());
        dimensionText.setText(card.getDimension().getDisplayName());
        descriptionText.setText(card.getDescription());
        originalEraText.setText(card.getOriginalEra().getDisplayName());

        updateCardStyle();
        setExhausted(card.isExhausted());
    }

    private void updateCardStyle() {
        if (cardBackground == null) return;

        cardBackground.getStyleClass().clear();
        cardBackground.getStyleClass().add("card-background");

        if (isEmpty) {
            cardBackground.getStyleClass().add("card-empty");
            cardBackground.setStroke(javafx.scene.paint.Color.GRAY);
            cardBackground.setStrokeWidth(1);
        } else if (card != null) {
            CardDimension dimension = card.getDimension();
            String dimensionClass = "card-" + dimension.name().toLowerCase().replace("_", "-");
            cardBackground.getStyleClass().add(dimensionClass);

            cardBackground.setStroke(javafx.scene.paint.Color.web("#333"));
            cardBackground.setStrokeWidth(1);
        }

        if (isSelected) {
            cardBackground.getStyleClass().add("card-selected");
            cardBackground.setStroke(javafx.scene.paint.Color.LIME);
            cardBackground.setStrokeWidth(4);
        }
    }

    private void setExhausted(boolean exhausted) {
        if (exhaustedOverlay != null) {
            exhaustedOverlay.setVisible(exhausted);
        }

        // DODANO: Dodatni visual feedback
        if (cardBackground != null) {
            if (exhausted) {
                cardBackground.setOpacity(0.5); // Zatamni pozadinu
                cardBackground.getStyleClass().add("card-exhausted");
            } else {
                cardBackground.setOpacity(1.0); // Puna opacity
                cardBackground.getStyleClass().remove("card-exhausted");
            }
        }

        // DODANO: Zatamni text kada je exhausted
        if (cardNameText != null) {
            cardNameText.setOpacity(exhausted ? 0.6 : 1.0);
        }
        if (descriptionText != null) {
            descriptionText.setOpacity(exhausted ? 0.6 : 1.0);
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;

        if (cardBackground != null) {
            cardBackground.getStyleClass().removeAll("card-selected");

            if (selected) {
                cardBackground.getStyleClass().add("card-selected");
                cardBackground.setStroke(javafx.scene.paint.Color.LIME);
                cardBackground.setStrokeWidth(4);
            } else {
                updateCardStyle();
            }

            cardBackground.autosize();
        }
    }

    @FXML
    private void onCardClicked(MouseEvent event) {
        if (clickHandler != null) {
            clickHandler.run();
        }
        event.consume();
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
        setExhausted(true);
        setSelected(false);
    }

    public void readyCard() {
        if (card != null) {
            card.ready();
            setExhausted(false);
        }
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