// 🛠️ PHASE 3: CARD CYCLING FIX
// Fix deck/discard pile management and shuffling

// 1. UPDATE Player.java - Better card cycling with debug
package hr.algebra.theloop.model;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardDimension;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Player implements Serializable {

    @NonNull private String name;
    @NonNull private Era currentEra;
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
        this.loopsPerformedThisTurn = 0;
    }

    public void addCardToHand(ArtifactCard card) {
        if (hand.size() < 3) {
            hand.add(card);
            System.out.println("🃏 Added to hand: " + card.getName() + " (Hand: " + hand.size() + "/3)");
        }
    }

    public void addCardToDeck(ArtifactCard card) {
        deck.add(card);
        System.out.println("🃏 Added to deck: " + card.getName() + " (Deck: " + deck.size() + ")");
    }

    public void drawToFullHand() {
        System.out.println("🃏 === DRAW TO FULL HAND ===");
        System.out.println("🃏 Before: Hand=" + hand.size() + ", Deck=" + deck.size() + ", Discard=" + discardPile.size());

        while (hand.size() < 3) {
            if (deck.isEmpty() && !discardPile.isEmpty()) {
                System.out.println("🔄 Deck empty! Shuffling discard pile into deck...");
                System.out.println("🃏 Discard pile contents:");
                for (ArtifactCard card : discardPile) {
                    System.out.println("  - " + card.getName() + " [" + (card.isExhausted() ? "Exhausted" : "Ready") + "]");
                    card.ready();
                }

                deck.addAll(discardPile);
                discardPile.clear();
                Collections.shuffle(deck);
                System.out.println("🔄 Shuffled! New deck size: " + deck.size());
            }

            if (deck.isEmpty()) {
                System.out.println("⚠️ No more cards to draw! Hand: " + hand.size() + "/3");
                break;
            }

            ArtifactCard card = deck.remove(0);
            card.ready();
            hand.add(card);
            System.out.println("🃏 Drew: " + card.getName() + " (Hand: " + hand.size() + "/3)");
        }

        System.out.println("🃏 After: Hand=" + hand.size() + ", Deck=" + deck.size() + ", Discard=" + discardPile.size());
    }

    public void discardHand() {
        System.out.println("🃏 === DISCARDING HAND ===");
        for (ArtifactCard card : hand) {
            System.out.println("🗑️ Discarding: " + card.getName() + " [" + (card.isExhausted() ? "Exhausted" : "Ready") + "]");
            discardPile.add(card);
        }
        hand.clear();
        System.out.println("🗑️ Hand cleared. Discard pile: " + discardPile.size() + " cards");
    }

    public void printDeckState() {
        System.out.println("🃏 === DECK STATE for " + name + " ===");
        System.out.println("Hand (" + hand.size() + "):");
        for (ArtifactCard card : hand) {
            System.out.println("  - " + card.getName() + " [" + (card.isExhausted() ? "Exhausted" : "Ready") + "]");
        }
        System.out.println("Deck (" + deck.size() + "):");
        for (int i = 0; i < Math.min(deck.size(), 5); i++) {
            System.out.println("  - " + deck.get(i).getName());
        }
        if (deck.size() > 5) {
            System.out.println("  ... and " + (deck.size() - 5) + " more");
        }
        System.out.println("Discard (" + discardPile.size() + "):");
        for (ArtifactCard card : discardPile) {
            System.out.println("  - " + card.getName() + " [" + (card.isExhausted() ? "Exhausted" : "Ready") + "]");
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

    public boolean hasPlayableCards(GameState gameState) {
        return hand.stream()
                .anyMatch(card -> !card.isExhausted() && card.canExecute(gameState, this));
    }

    public List<ArtifactCard> getPlayableCards(GameState gameState) {
        return hand.stream()
                .filter(card -> !card.isExhausted() && card.canExecute(gameState, this))
                .collect(Collectors.toList());
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

    public void incrementRiftsRemoved(int amount) {
        this.riftsRemoved += amount;
    }

    public void incrementMissionsContributed() {
        this.missionsContributed++;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s [Hand: %d, Deck: %d, Discard: %d, Battery: %s]",
                name, currentEra.getDisplayName(), hand.size(), deck.size(), discardPile.size(),
                batteriesFull ? "🔋 Full" : "🪫 Used");
    }
}