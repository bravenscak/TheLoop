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

    // Click handler for parent controller
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

        // FIXED: Clear all previous style classes
        cardBackground.getStyleClass().clear();
        cardBackground.getStyleClass().add("card-background");

        if (isEmpty) {
            cardBackground.getStyleClass().add("card-empty");
        } else if (card != null) {
            CardDimension dimension = card.getDimension();
            String dimensionClass = "card-" + dimension.name().toLowerCase().replace("_", "-");
            cardBackground.getStyleClass().add(dimensionClass);

            System.out.println("🎨 Applied card style: " + dimensionClass);
        }

        // FIXED: Apply selection style AFTER dimension style for priority
        if (isSelected) {
            cardBackground.getStyleClass().add("card-selected");
            System.out.println("🟢 Applied selection style");
        }

        // FORCE style update
        cardBackground.applyCss();
    }

    private void setExhausted(boolean exhausted) {
        if (exhaustedOverlay != null) {
            exhaustedOverlay.setVisible(exhausted);
            if (exhausted) {
                System.out.println("🔘 Card exhausted overlay shown");
            }
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        updateCardStyle();

        if (selected) {
            System.out.println("🟢 Card SELECTED: " + (card != null ? card.getName() : "Empty"));
        } else {
            System.out.println("⚪ Card deselected");
        }
    }

    @FXML
    private void onCardClicked(MouseEvent event) {
        System.out.println("🖱️ Card clicked: " + (isEmpty ? "Empty slot" : card.getName()));

        if (clickHandler != null) {
            clickHandler.run();
        }

        // Prevent event propagation
        event.consume();
    }

    // IMPROVED: Better validation logic
    public boolean canPlayCard() {
        if (isEmpty || card == null) {
            System.out.println("❌ Cannot play: empty slot");
            return false;
        }

        if (card.isExhausted()) {
            System.out.println("❌ Cannot play: card exhausted");
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

        System.out.println("✅ Card played and exhausted: " + card.getName());
    }

    public void readyCard() {
        if (card != null) {
            card.ready();
            setExhausted(false);
            System.out.println("🔄 Card readied: " + card.getName());
        }
    }

    // Getters
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