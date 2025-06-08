package hr.algebra.theloop.model;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardDimension;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Data
public class Player implements Serializable {

    @NonNull private String name;
    @NonNull private Era currentEra;
    private String agentIcon; // For visual representation

    private List<ArtifactCard> hand;
    private List<ArtifactCard> deck;
    private List<ArtifactCard> discardPile;

    private boolean batteriesFull; // Free movement once per turn
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

        this.hand = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.discardPile = new ArrayList<>();

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
        this.loopsPerformedThisTurn = 0; // Reset LOOP counter
    }

    public void addCardToHand(ArtifactCard card) {
        if (hand.size() < 3) {
            hand.add(card);
        } else {
            deck.add(card);
        }
    }

    public void addCardToDeck(ArtifactCard card) {
        deck.add(card);
    }

    public void playCard(ArtifactCard card) {
        if (hand.remove(card)) {
            discardPile.add(card);
            cardsPlayed++;
        }
    }

    public void discardCard(ArtifactCard card) {
        hand.remove(card);
        discardPile.add(card);
    }

    public void drawCard() {
        if (deck.isEmpty() && !discardPile.isEmpty()) {
            deck.addAll(discardPile);
            discardPile.clear();
            java.util.Collections.shuffle(deck);
        }

        if (!deck.isEmpty() && hand.size() < 3) {
            ArtifactCard card = deck.remove(0);
            hand.add(card);
        }
    }

    public void fillHandToThree() {
        while (hand.size() < 3 && (!deck.isEmpty() || !discardPile.isEmpty())) {
            drawCard();
        }
    }

    public boolean canPerformLoop(CardDimension dimension, GameState gameState) {
        if (!dimension.canLoop()) {
            return false;
        }

        boolean hasExhaustedCards = hand.stream()
                .anyMatch(card -> card.getDimension() == dimension && card.isExhausted());

        if (!hasExhaustedCards) {
            return false;
        }

        int loopCost = loopsPerformedThisTurn + 1;
        return gameState.getEnergy(currentEra) >= loopCost;
    }

    public void performLoop(CardDimension dimension, GameState gameState) {
        if (!canPerformLoop(dimension, gameState)) {
            System.out.println("❌ Cannot perform LOOP!");
            return;
        }

        int loopCost = loopsPerformedThisTurn + 1;

        gameState.removeEnergy(currentEra, loopCost);
        energySpent += loopCost;

        int readiedCards = 0;
        for (ArtifactCard card : hand) {
            if (card.getDimension() == dimension && card.isExhausted()) {
                card.ready();
                readiedCards++;
            }
        }

        loopsPerformedThisTurn++;

        System.out.println("🔄 LOOP performed! Readied " + readiedCards +
                " " + dimension.getDisplayName() + " cards for " + loopCost + " energy");
    }

    public int getLoopsPerformedThisTurn() {
        return loopsPerformedThisTurn;
    }

    public void setLoopsPerformedThisTurn(int loops) {
        this.loopsPerformedThisTurn = loops;
    }

    public List<ArtifactCard> getReadyCards() {
        return hand.stream()
                .filter(card -> !card.isExhausted())
                .collect(Collectors.toList());
    }

    public List<ArtifactCard> getExhaustedCards() {
        return hand.stream()
                .filter(ArtifactCard::isExhausted)
                .collect(Collectors.toList());
    }

    public List<ArtifactCard> getCardsByDimension(CardDimension dimension) {
        return hand.stream()
                .filter(card -> card.getDimension() == dimension)
                .collect(Collectors.toList());
    }

    public boolean hasPlayableCards(GameState gameState) {
        return hand.stream()
                .anyMatch(card -> !card.isExhausted() && card.canExecute(gameState, this));
    }

    public int getHandSize() {
        return hand.size();
    }

    public int getDeckSize() {
        return deck.size();
    }

    public int getDiscardPileSize() {
        return discardPile.size();
    }

    public int getTotalCards() {
        return hand.size() + deck.size() + discardPile.size();
    }

    public void incrementRiftsRemoved(int amount) {
        this.riftsRemoved += amount;
    }

    public void incrementMissionsContributed() {
        this.missionsContributed++;
    }

    public double getEfficiencyRatio() {
        return cardsPlayed > 0 ? (double) missionsContributed / cardsPlayed : 0.0;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s [Hand: %d, Deck: %d, Battery: %s]",
                name, currentEra.getDisplayName(), hand.size(), deck.size(),
                batteriesFull ? "🔋 Full" : "🪫 Used");
    }
}