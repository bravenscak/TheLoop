package hr.algebra.theloop.ui;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardDimension;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class CardDisplayManager {

    private final Rectangle cardBackground;
    private final Text cardNameText;
    private final Text dimensionIcon;
    private final Text dimensionText;
    private final Text descriptionText;
    private final Text originalEraText;
    private final Rectangle exhaustedOverlay;

    public CardDisplayManager(Rectangle cardBackground, Text cardNameText, Text dimensionIcon,
                              Text dimensionText, Text descriptionText, Text originalEraText,
                              Rectangle exhaustedOverlay) {
        this.cardBackground = cardBackground;
        this.cardNameText = cardNameText;
        this.dimensionIcon = dimensionIcon;
        this.dimensionText = dimensionText;
        this.descriptionText = descriptionText;
        this.originalEraText = originalEraText;
        this.exhaustedOverlay = exhaustedOverlay;
    }

    public void updateCardDisplay(ArtifactCard card) {
        if (card == null) return;

        cardNameText.setText(card.getName());
        dimensionIcon.setText(card.getDimension().getIcon());
        dimensionText.setText(card.getDimension().getDisplayName());
        descriptionText.setText(card.getDescription());

        updateCardStyle(card);
        setExhausted(card.isExhausted());
    }

    public void setEmpty() {
        if (cardNameText != null) {
            cardNameText.setText("Empty Slot");
            dimensionIcon.setText("");
            dimensionText.setText("");
            descriptionText.setText("No card");
            originalEraText.setText("");
        }

        updateEmptyStyle();
        setExhausted(false);
    }

    private void updateCardStyle(ArtifactCard card) {
        if (cardBackground == null) return;

        cardBackground.getStyleClass().clear();
        cardBackground.getStyleClass().add("card-background");

        CardDimension dimension = card.getDimension();
        String dimensionClass = "card-" + dimension.name().toLowerCase().replace("_", "-");
        cardBackground.getStyleClass().add(dimensionClass);

        cardBackground.setStroke(javafx.scene.paint.Color.web("#333"));
        cardBackground.setStrokeWidth(1);
    }

    private void updateEmptyStyle() {
        if (cardBackground == null) return;

        cardBackground.getStyleClass().clear();
        cardBackground.getStyleClass().addAll("card-background", "card-empty");
        cardBackground.setStroke(javafx.scene.paint.Color.GRAY);
        cardBackground.setStrokeWidth(1);
    }

    public void setSelected(boolean selected) {
        if (cardBackground == null) return;

        cardBackground.getStyleClass().removeAll("card-selected");

        if (selected) {
            cardBackground.getStyleClass().add("card-selected");
            cardBackground.setStroke(javafx.scene.paint.Color.LIME);
            cardBackground.setStrokeWidth(4);
        } else {
            cardBackground.setStroke(javafx.scene.paint.Color.web("#333"));
            cardBackground.setStrokeWidth(1);
        }
    }

    private void setExhausted(boolean exhausted) {
        if (exhaustedOverlay != null) {
            exhaustedOverlay.setVisible(exhausted);
        }

        if (cardBackground != null) {
            if (exhausted) {
                cardBackground.setOpacity(0.5);
                cardBackground.getStyleClass().add("card-exhausted");
            } else {
                cardBackground.setOpacity(1.0);
                cardBackground.getStyleClass().remove("card-exhausted");
            }
        }

        if (cardNameText != null) {
            cardNameText.setOpacity(exhausted ? 0.6 : 1.0);
        }
        if (descriptionText != null) {
            descriptionText.setOpacity(exhausted ? 0.6 : 1.0);
        }
    }
}