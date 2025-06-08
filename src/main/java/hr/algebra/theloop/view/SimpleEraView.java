package hr.algebra.theloop.view;

import hr.algebra.theloop.model.Era;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class SimpleEraView extends Pane {
    private final Era era;
    private final Rectangle background;
    private final Text nameText;
    private final Text riftsText;
    private final Text energyText;
    private final Text duplicatesText; // NEW: duplicate display

    public SimpleEraView(Era era, double x, double y, double width, double height) {
        this.era = era;

        setLayoutX(x);
        setLayoutY(y);
        setPrefSize(width, height);

        background = new Rectangle(width, height);
        background.setFill(Color.web(era.getColorCode()));
        background.setStroke(Color.WHITE);
        background.setStrokeWidth(2);
        background.setArcWidth(10);
        background.setArcHeight(10);

        nameText = new Text(era.getDisplayName());
        nameText.setFill(Color.WHITE);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        nameText.setLayoutX(5);
        nameText.setLayoutY(15);

        riftsText = new Text("🔴0");
        riftsText.setLayoutX(5);
        riftsText.setLayoutY(35);

        energyText = new Text("🟢1");
        energyText.setLayoutX(35);
        energyText.setLayoutY(35);

        duplicatesText = new Text("👤0");
        duplicatesText.setLayoutX(5);
        duplicatesText.setLayoutY(50);
        duplicatesText.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
        duplicatesText.setFill(Color.LIGHTBLUE);

        getChildren().addAll(background, nameText, riftsText, energyText, duplicatesText);

        setOnMouseClicked(event -> {
            System.out.println("🖱️ " + era.getDisplayName() + " clicked");
            event.consume();
        });
    }

    public void updateResources(int rifts, int energy, boolean hasVortex, boolean playerPresent) {
        updateResources(rifts, energy, 0, hasVortex, playerPresent); // Default 0 duplicates
    }

    public void updateResources(int rifts, int energy, int duplicates, boolean hasVortex, boolean playerPresent) {
        riftsText.setText("🔴" + rifts);
        energyText.setText("🟢" + energy);

        if (duplicates > 0) {
            duplicatesText.setText("👤" + duplicates);
            duplicatesText.setVisible(true);
        } else {
            duplicatesText.setVisible(false);
        }

        if (hasVortex) {
            background.setStroke(Color.RED);
            background.setStrokeWidth(4);
        } else if (playerPresent) {
            background.setStroke(Color.YELLOW);
            background.setStrokeWidth(3);
        } else if (duplicates > 0) {
            background.setStroke(Color.LIGHTBLUE);
            background.setStrokeWidth(3);
        } else {
            background.setStroke(Color.WHITE);
            background.setStrokeWidth(2);
        }
    }

    public Era getEra() {
        return era;
    }
}