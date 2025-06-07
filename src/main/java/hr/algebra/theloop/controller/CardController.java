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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setEmpty();
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
        } else if (card != null) {
            CardDimension dimension = card.getDimension();
            cardBackground.getStyleClass().add("card-" + dimension.name().toLowerCase().replace("_", "-"));
        }

        if (isSelected) {
            cardBackground.getStyleClass().add("card-selected");
        }
    }

    private void setExhausted(boolean exhausted) {
        if (exhaustedOverlay != null) {
            exhaustedOverlay.setVisible(exhausted);
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateCardStyle();
    }

    @FXML
    private void onCardClicked(MouseEvent event) {
        if (isEmpty || card == null) {
            System.out.println("Empty card slot clicked");
            return;
        }

        System.out.println("Card clicked: " + card.getName() +
                " (" + card.getDimension().getDisplayName() + ")");

        setSelected(!isSelected);

        // TODO: Implement card play logic

        if (isSelected) {
            System.out.println("Card selected for play");
            // Notify parent controller that this card is selected
        } else {
            System.out.println("Card deselected");
        }
    }

    public boolean canPlayCard() {
        return !isEmpty && card != null && !card.isExhausted();
    }

    public void playCard() {
        if (!canPlayCard()) {
            System.out.println("Cannot play card: " +
                    (isEmpty ? "empty slot" :
                            card.isExhausted() ? "exhausted" : "unknown reason"));
            return;
        }

        setExhausted(true);
        setSelected(false);

        System.out.println("Card played: " + card.getName());
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

    public String getCardInfo() {
        if (isEmpty) {
            return "Empty slot";
        }

        return String.format("%s (%s) - %s %s",
                card.getName(),
                card.getDimension().getDisplayName(),
                card.isExhausted() ? "Exhausted" : "Ready",
                isSelected ? "[Selected]" : "");
    }
}