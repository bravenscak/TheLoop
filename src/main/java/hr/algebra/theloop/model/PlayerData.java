package hr.algebra.theloop.model;

import hr.algebra.theloop.cards.ArtifactCard;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
public class PlayerData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Era currentEra;
    private String agentIcon;

    private List<ArtifactCard> hand;
    private List<ArtifactCard> deck;
    private List<ArtifactCard> discardPile;

    private boolean batteriesFull;
    private int loopsPerformedThisTurn;
    private boolean isCurrentPlayer;

    private int cardsPlayed;
    private int energySpent;
    private int riftsRemoved;
    private int missionsContributed;

    public PlayerData(Player player) {
        this.name = player.getName();
        this.currentEra = player.getCurrentEra();
        this.agentIcon = player.getAgentIcon();

        this.hand = new ArrayList<>(player.getHand());
        this.deck = new ArrayList<>(player.getDeckManager().getDeck());
        this.discardPile = new ArrayList<>(player.getDeckManager().getDiscardPile());

        this.batteriesFull = player.isBatteriesFull();
        this.loopsPerformedThisTurn = player.getLoopsPerformedThisTurn();
        this.isCurrentPlayer = player.isCurrentPlayer();

        this.cardsPlayed = player.getCardsPlayed();
        this.energySpent = player.getEnergySpent();
        this.riftsRemoved = player.getRiftsRemoved();
        this.missionsContributed = player.getMissionsContributed();
    }

    public void restoreToPlayer(Player player) {
        player.setCurrentEra(currentEra);
        player.setAgentIcon(agentIcon);

        PlayerDeckManager deckManager = player.getDeckManager();
        deckManager.getHand().clear();
        deckManager.getDeck().clear();
        deckManager.getDiscardPile().clear();

        deckManager.getHand().addAll(hand);
        deckManager.getDeck().addAll(deck);
        deckManager.getDiscardPile().addAll(discardPile);

        player.setBatteriesFull(batteriesFull);
        player.setLoopsPerformedThisTurn(loopsPerformedThisTurn);
        player.setCurrentPlayer(isCurrentPlayer);

        player.setCardsPlayed(cardsPlayed);
        player.setEnergySpent(energySpent);
        player.setRiftsRemoved(riftsRemoved);
        player.setMissionsContributed(missionsContributed);
    }

    @Override
    public String toString() {
        return String.format("PlayerData[%s @ %s, Hand: %d, Deck: %d, Discard: %d]",
                name, currentEra.getDisplayName(),
                hand.size(), deck.size(), discardPile.size());
    }
}