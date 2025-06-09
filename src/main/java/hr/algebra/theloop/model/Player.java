package hr.algebra.theloop.model;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardDimension;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.List;

@Data
public class Player implements Serializable {

    @NonNull private String name;
    @NonNull private Era currentEra;
    private String agentIcon;

    private PlayerDeckManager deckManager;

    private boolean batteriesFull;
    private int loopsPerformedThisTurn;
    private boolean isCurrentPlayer;

    private int cardsPlayed;
    private int energySpent;
    private int riftsRemoved;
    private int missionsContributed;

    public Player(String name, Era startingEra) {
        this.name = name;
        this.currentEra = startingEra;
        this.agentIcon = "🕵️";
        this.deckManager = new PlayerDeckManager();
        this.batteriesFull = true;
        this.loopsPerformedThisTurn = 0;
        this.isCurrentPlayer = false;
        this.cardsPlayed = 0;
        this.energySpent = 0;
        this.riftsRemoved = 0;
        this.missionsContributed = 0;
    }

    public boolean canMoveTo(Era targetEra) {
        return currentEra.isAdjacentTo(targetEra);
    }

    public void moveToEra(Era newEra) {
        this.currentEra = newEra;
    }

    public boolean canUseFreeBattery() {
        return batteriesFull;
    }

    public void useFreeBattery() {
        this.batteriesFull = false;
    }

    public void rechargeBatteries() {
        this.batteriesFull = true;
        this.loopsPerformedThisTurn = 0;
    }

    public boolean canPerformLoop(CardDimension dimension, GameState gameState) {
        boolean hasExhaustedCards = deckManager.getHand().stream().anyMatch(ArtifactCard::isExhausted);
        if (!hasExhaustedCards) return false;

        int loopCost = loopsPerformedThisTurn + 1;
        return gameState.getEnergy(currentEra) >= loopCost;
    }

    public List<ArtifactCard> getHand() {
        return deckManager.getHand();
    }

    public void addCardToHand(ArtifactCard card) {
        deckManager.addCardToHand(card);
    }

    public void addCardToDeck(ArtifactCard card) {
        deckManager.addCardToDeck(card);
    }

    public void drawToFullHand() {
        deckManager.drawToFullHand();
    }

    public void discardHand() {
        deckManager.discardHand();
    }

    public boolean hasPlayableCards(GameState gameState) {
        return deckManager.hasPlayableCards(gameState, this);
    }

    public List<ArtifactCard> getPlayableCards(GameState gameState) {
        return deckManager.getPlayableCards(gameState, this);
    }

    public List<ArtifactCard> getExhaustedCards() {
        return deckManager.getExhaustedCards();
    }

    public List<ArtifactCard> getReadyCards() {
        return deckManager.getReadyCards();
    }

    public int getHandSize() {
        return deckManager.getHandSize();
    }

    public int getDeckSize() {
        return deckManager.getDeckSize();
    }

    public int getDiscardPileSize() {
        return deckManager.getDiscardPileSize();
    }

    public void incrementRiftsRemoved(int amount) {
        this.riftsRemoved += amount;
    }

    public void incrementMissionsContributed() {
        this.missionsContributed++;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s [Hand: %d, Deck: %d, Battery: %s]",
                name, currentEra.getDisplayName(), getHandSize(), getDeckSize(),
                batteriesFull ? "🔋 Full" : "🪫 Used");
    }
}