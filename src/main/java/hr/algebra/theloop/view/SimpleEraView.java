package hr.algebra.theloop.view;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.Duplicate;
import java.util.List;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.List;

public class SimpleEraView extends Pane {
    private final Era era;
    private final Rectangle background;
    private final Text nameText;
    private final Text riftsText;
    private final Text energyText;
    private final Text duplicatesText;
    private List<Duplicate> currentDuplicates;

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
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        nameText.setLayoutX(8);
        nameText.setLayoutY(20);

        riftsText = new Text("🔴0");
        riftsText.setLayoutX(8);
        riftsText.setLayoutY(45);
        riftsText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        energyText = new Text("🟢1");
        energyText.setLayoutX(50);
        energyText.setLayoutY(45);
        energyText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));

        duplicatesText = new Text("👤0");
        duplicatesText.setLayoutX(8);
        duplicatesText.setLayoutY(70);
        duplicatesText.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        duplicatesText.setFill(Color.LIGHTBLUE);

        getChildren().addAll(background, nameText, riftsText, energyText, duplicatesText);

        setOnMouseClicked(event -> {
            System.out.println("🖱️ " + era.getDisplayName() + " clicked");
            event.consume();
        });
    }

    public void updateResources(int rifts, int energy, int duplicates,
                                List<Duplicate> duplicateList, boolean hasVortex, boolean playerPresent) {
        riftsText.setText("🔴" + rifts);
        energyText.setText("🟢" + energy);

        this.currentDuplicates = duplicateList;

        if (duplicates > 0 && duplicateList != null && !duplicateList.isEmpty()) {
            duplicatesText.setText("👤" + duplicates);
            duplicatesText.setVisible(true);

            setupDuplicateTooltip(duplicateList);

        } else {
            duplicatesText.setVisible(false);
            Tooltip.uninstall(duplicatesText, null);
        }

        updateBorderEffects(hasVortex, playerPresent, duplicates);
    }

    private void setupDuplicateTooltip(List<Duplicate> duplicates) {
        StringBuilder tooltipText = new StringBuilder("Duplicates (destroy at):\n");

        for (int i = 0; i < Math.min(duplicates.size(), 4); i++) {
            Duplicate dup = duplicates.get(i);
            tooltipText.append("• Dr. Foo → ").append(dup.getDestroyEra().getDisplayName()).append("\n");
        }

        if (duplicates.size() > 4) {
            tooltipText.append("... and ").append(duplicates.size() - 4).append(" more");
        }

        Tooltip tooltip = new Tooltip(tooltipText.toString());
        tooltip.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 10px;");
        tooltip.setShowDelay(javafx.util.Duration.millis(750)); // Show after 750ms
        Tooltip.install(duplicatesText, tooltip);
    }

    private void updateBorderEffects(boolean hasVortex, boolean playerPresent, int duplicates) {
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

    public List<Duplicate> getCurrentDuplicates() {
        return currentDuplicates;
    }

    public Era getEra() {
        return era;
    }
}