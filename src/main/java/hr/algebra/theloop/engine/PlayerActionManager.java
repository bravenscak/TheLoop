package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.model.PlayerMode;
import hr.algebra.theloop.utils.GameLogger;
import javafx.application.Platform;

import java.util.List;

public class PlayerActionManager {

    private final GameState gameState;
    private final MissionManager missionManager;
    private Runnable uiUpdateCallback;

    public PlayerActionManager(GameState gameState, MissionManager missionManager,
                               CardAcquisitionManager cardAcquisitionManager) {
        this.gameState = gameState;
        this.missionManager = missionManager;
    }

    public void setUIUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    public void setPlayerMode(PlayerMode mode, int localIndex) {
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (gameState.isGameOver()) {
            return false;
        }

        if (!isValidCardIndex(player, cardIndex)) {
            return false;
        }

        List<ArtifactCard> hand = player.getHand();
        ArtifactCard card = hand.get(cardIndex);

        if (!card.canExecute(gameState, player)) {
            return false;
        }

        int totalDuplicatesBefore = getTotalDuplicates();

        card.execute(gameState, player);
        card.exhaust();

        int totalDuplicatesAfter = getTotalDuplicates();
        boolean duplicatesChanged = (totalDuplicatesBefore != totalDuplicatesAfter);

        GameLogger.playerAction(player.getName(), "Played " + card.getName() +
                (duplicatesChanged ? " (duplicates: " + totalDuplicatesBefore + " → " + totalDuplicatesAfter + ")" : ""));

        if (uiUpdateCallback != null) {
            Platform.runLater(uiUpdateCallback);
        }

        missionManager.checkAllMissions(gameState, player, card.getClass().getSimpleName());

        return true;
    }

    public boolean movePlayer(Player player, Era targetEra) {
        if (gameState.isGameOver()) {
            return false;
        }

        Era currentEra = player.getCurrentEra();
        if (!currentEra.isAdjacentTo(targetEra)) {
            return false;
        }

        if (player.canUseFreeBattery()) {
            return executeFreeBatteryMovement(player, targetEra);
        }

        return executeEnergyMovement(player, currentEra, targetEra);
    }

    private boolean executeFreeBatteryMovement(Player player, Era targetEra) {
        player.useFreeBattery();
        player.moveToEra(targetEra);
        GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName() + " (battery)");

        triggerUIUpdate();
        missionManager.checkAllMissions(gameState, player, "Movement");
        return true;
    }

    private boolean executeEnergyMovement(Player player, Era currentEra, Era targetEra) {
        int availableEnergy = gameState.getEnergy(currentEra);
        if (availableEnergy <= 0) {
            return false;
        }

        gameState.removeEnergy(currentEra, 1);
        player.moveToEra(targetEra);
        GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName() + " (1 energy)");

        triggerUIUpdate();
        missionManager.checkAllMissions(gameState, player, "Movement");
        return true;
    }

    public boolean spawnDuplicate(Era era, int duplicatesInBag) {
        if (duplicatesInBag <= 0) return false;

        hr.algebra.theloop.model.Duplicate newDuplicate = new hr.algebra.theloop.model.Duplicate(era);
        gameState.addDuplicate(era, newDuplicate);

        return true;
    }

    private boolean isValidCardIndex(Player player, int cardIndex) {
        List<ArtifactCard> hand = player.getHand();
        return cardIndex >= 0 && cardIndex < hand.size();
    }

    private void triggerUIUpdate() {
        if (uiUpdateCallback != null) {
            Platform.runLater(uiUpdateCallback);
        }
    }

    private int getTotalDuplicates() {
        int total = 0;
        for (Era era : Era.values()) {
            total += gameState.getDuplicateCount(era);
        }
        return total;
    }
}