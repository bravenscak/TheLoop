package hr.algebra.theloop.engine;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;
import hr.algebra.theloop.utils.GameLogger;

import java.util.List;

public class PlayerActionManager {

    private final GameState gameState;
    private final MissionManager missionManager;
    private final CardAcquisitionManager cardAcquisitionManager;

    public PlayerActionManager(GameState gameState, MissionManager missionManager,
                               CardAcquisitionManager cardAcquisitionManager) {
        this.gameState = gameState;
        this.missionManager = missionManager;
        this.cardAcquisitionManager = cardAcquisitionManager;
    }

    public boolean playCard(Player player, int cardIndex, Era targetEra) {
        if (gameState.isGameOver()) {
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

        card.execute(gameState, player);
        card.exhaust();

        GameLogger.playerAction(player.getName(), "Played " + card.getName());
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
            player.useFreeBattery();
            player.moveToEra(targetEra);
            GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName() + " (battery)");
        } else if (gameState.getEnergy(currentEra) > 0) {
            gameState.removeEnergy(currentEra, 1);
            player.moveToEra(targetEra);
            GameLogger.playerAction(player.getName(), "Moved to " + targetEra.getDisplayName() + " (1 energy)");
        } else {
            return false;
        }

        missionManager.checkAllMissions(gameState, player, "Movement");
        return true;
    }

    public boolean acquireCard(Player player) {
        Era playerEra = player.getCurrentEra();
        ArtifactCard acquiredCard = cardAcquisitionManager.acquireCard(playerEra, player);

        if (acquiredCard != null) {
            GameLogger.playerAction(player.getName(), "Acquired " + acquiredCard.getName());
            return true;
        }
        return false;
    }
}