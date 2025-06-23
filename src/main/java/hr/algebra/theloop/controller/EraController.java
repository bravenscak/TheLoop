package hr.algebra.theloop.controller;

import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.utils.GameLogger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class EraController implements Initializable {

    private static final String VORTEX_STYLE_CLASS = "vortex";
    private static final String HIGH_RIFTS_STYLE_CLASS = "high-rifts";

    @FXML private Polygon eraShape;
    @FXML private Text eraNameText;
    @FXML private Text riftsText;
    @FXML private Text energyText;
    @FXML private Text vortexIndicator;
    @FXML private Text playerIndicator;

    private Era era;
    private int currentRifts = 0;
    private int currentEnergy = 1;
    private boolean hasVortex = false;
    private boolean playerPresent = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setEra(Era era) {
        this.era = era;
        if (era != null) {
            eraNameText.setText(era.getDisplayName());
            updateEraStyle();
        }
    }

    private void updateEraStyle() {
        if (era != null && eraShape != null) {
            eraShape.getStyleClass().clear();
            eraShape.getStyleClass().addAll("era-shape", "era-" + era.name().toLowerCase().replace("_", "-"));
        }
    }

    public void updateDisplay(int rifts, int energy, boolean vortex) {
        this.currentRifts = rifts;
        this.currentEnergy = energy;
        this.hasVortex = vortex;

        if (riftsText != null) {
            riftsText.setText(String.valueOf(rifts));
        }

        if (energyText != null) {
            energyText.setText(String.valueOf(energy));
        }

        if (vortexIndicator != null) {
            vortexIndicator.setVisible(vortex);
        }

        updateEraEffects();
    }

    private void updateEraEffects() {
        if (eraShape == null) return;

        if (hasVortex) {
            if (!eraShape.getStyleClass().contains(VORTEX_STYLE_CLASS)) {
                eraShape.getStyleClass().add(VORTEX_STYLE_CLASS);
            }
        } else {
            eraShape.getStyleClass().remove(VORTEX_STYLE_CLASS);
        }

        if (currentRifts >= 2) {
            if (!eraShape.getStyleClass().contains(HIGH_RIFTS_STYLE_CLASS)) {
                eraShape.getStyleClass().add(HIGH_RIFTS_STYLE_CLASS);
            }
        } else {
            eraShape.getStyleClass().remove(HIGH_RIFTS_STYLE_CLASS);
        }
    }

    public void setPlayerPresent(boolean present) {
        this.playerPresent = present;
        if (playerIndicator != null) {
            playerIndicator.setVisible(present);
        }
    }

    @FXML
    private void onEraClicked(MouseEvent event) {
        if (era != null) {
            GameLogger.gameFlow("Era clicked: " + era.getDisplayName() +
                    " (Rifts: " + currentRifts + ", Energy: " + currentEnergy + ")");
        }
    }

    public Era getEra() {
        return era;
    }

    public int getCurrentRifts() {
        return currentRifts;
    }

    public int getCurrentEnergy() {
        return currentEnergy;
    }

    public boolean hasVortex() {
        return hasVortex;
    }

    public boolean isPlayerPresent() {
        return playerPresent;
    }
}