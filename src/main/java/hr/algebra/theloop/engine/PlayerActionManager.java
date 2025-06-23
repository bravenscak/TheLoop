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
    private final CardAcquisitionManager cardAcquisitionManager;
    private Runnable uiUpdateCallback;
    private PlayerMode playerMode = PlayerMode.SINGLE_PLAYER;
    private int localPlayerIndex = 0;

    public PlayerActionManager(GameState gameState, MissionManager missionManager,
                               CardAcquisitionManager cardAcquisitionManager) {
        this.gameState = gameState;
        this.missionManager = missionManager;
        this.cardAcquisitionManager = cardAcquisitionManager;
    }

    public void setUIUpdateCallback(Runnable callback) {
        this.uiUpdateCallback = callback;
    }

    public void setPlayerMode(PlayerMode mode, int localIndex) {
        this.playerMode = mode;
        this.localPlayerIndex = localIndex;
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (gameState.isGameOver()) {
            return false;
        }

        if (!isLocalPlayer(player)) {
            return false;
        }

        List<ArtifactCard> hand = player.getHand();
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            return false;
        }

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

        if (!isLocalPlayer(player)) {
            return false;
        }

        Era currentEra = player.getCurrentEra();
        if (!currentEra.isAdjacentTo(targetEra)) {
            return false;
        }

        if (player.canUseFreeBattery()) {
            player.useFreeBattery();
            player.moveToEra(targetEra);
            GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName() + " (battery)");

            if (uiUpdateCallback != null) {
                Platform.runLater(uiUpdateCallback);
            }

            missionManager.checkAllMissions(gameState, player, "Movement");
            return true;
        }

        int availableEnergy = gameState.getEnergy(currentEra);
        if (availableEnergy > 0) {
            gameState.removeEnergy(currentEra, 1);
            player.moveToEra(targetEra);
            GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName() + " (1 energy)");

            if (uiUpdateCallback != null) {
                Platform.runLater(uiUpdateCallback);
            }

            missionManager.checkAllMissions(gameState, player, "Movement");
            return true;
        }

        return false;
    }

    public boolean spawnDuplicate(Era era, int duplicatesInBag) {
        if (duplicatesInBag <= 0) return false;

        hr.algebra.theloop.model.Duplicate newDuplicate = new hr.algebra.theloop.model.Duplicate(era);
        gameState.addDuplicate(era, newDuplicate);

        return true;
    }

    private boolean isLocalPlayer(Player player) {
        if (playerMode == PlayerMode.SINGLE_PLAYER) {
            return true;
        }
        return true;
    }

    private int getTotalDuplicates() {
        int total = 0;
        for (Era era : Era.values()) {
            total += gameState.getDuplicateCount(era);
        }
        return total;
    }
}