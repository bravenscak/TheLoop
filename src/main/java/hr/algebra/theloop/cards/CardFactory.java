package hr.algebra.theloop.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardFactory {

    private static final Random random = new Random();

    public static List<ArtifactCard> createRandomStartingDeck() {
        List<ArtifactCard> startingCards = new ArrayList<>();

        startingCards.add(createRandomEnergyCard());     // STAR
        startingCards.add(createRandomRiftCard());       // STRIPE
        startingCards.add(createRandomMovementCard());   // SPIRAL

        System.out.println("🎲 Random starting deck generated:");
        for (ArtifactCard card : startingCards) {
            System.out.println("  - " + card.getName() + " (" + card.getDimension().getDisplayName() + ")");
        }

        return startingCards;
    }

    public static List<ArtifactCard> createBasicStartingDeck() {
        List<ArtifactCard> startingCards = new ArrayList<>();

        startingCards.add(EnergyCard.createBasicEnergy());
        startingCards.add(RiftCard.createBasicRepair());
        startingCards.add(MovementCard.createTimeWalk());

        return startingCards;
    }

    public static List<ArtifactCard> createBalancedStartingDeck() {
        List<ArtifactCard> startingCards = new ArrayList<>();

        if (random.nextBoolean()) {
            startingCards.add(EnergyCard.createBasicEnergy());
        } else {
            startingCards.add(EnergyCard.createEnergyBoost());
        }

        if (random.nextBoolean()) {
            startingCards.add(RiftCard.createBasicRepair());
        } else {
            startingCards.add(RiftCard.createQuantumEraser());
        }

        if (random.nextBoolean()) {
            startingCards.add(MovementCard.createTimeWalk());
        } else {
            startingCards.add(MovementCard.createQuantumLeap());
        }

        return startingCards;
    }

    public static ArtifactCard createRandomCard() {
        int cardType = random.nextInt(4);

        return switch (cardType) {
            case 0 -> createRandomEnergyCard();
            case 1 -> createRandomRiftCard();
            case 2 -> createRandomMovementCard();
            case 3 -> createRandomDuplicateCard();
            default -> EnergyCard.createBasicEnergy();
        };
    }

    private static ArtifactCard createRandomEnergyCard() {
        int variant = random.nextInt(4);

        return switch (variant) {
            case 0 -> EnergyCard.createBasicEnergy();
            case 1 -> EnergyCard.createEnergyBoost();
            case 2 -> EnergyCard.createAdjacentEnergy();
            case 3 -> EnergyCard.createEnergySiphon();
            default -> EnergyCard.createBasicEnergy();
        };
    }

    private static ArtifactCard createRandomRiftCard() {
        int variant = random.nextInt(4);

        return switch (variant) {
            case 0 -> RiftCard.createBasicRepair();
            case 1 -> RiftCard.createQuantumEraser();
            case 2 -> RiftCard.createAdjacentRepair();
            case 3 -> RiftCard.createDrFooDisruptor();
            default -> RiftCard.createBasicRepair();
        };
    }

    private static ArtifactCard createRandomMovementCard() {
        int variant = random.nextInt(3);

        return switch (variant) {
            case 0 -> MovementCard.createTimeWalk();
            case 1 -> MovementCard.createQuantumLeap();
            case 2 -> MovementCard.createEnergeticStep();
            default -> MovementCard.createTimeWalk();
        };
    }

    private static ArtifactCard createRandomDuplicateCard() {
        int variant = random.nextInt(3);

        return switch (variant) {
            case 0 -> PushDuplicateCard.createBasicPush();
            case 1 -> PullDuplicateCard.createBasicPull();
            case 2 -> DestroyDuplicateCard.createBasicDestroy();
            default -> PushDuplicateCard.createBasicPush();
        };
    }

    public static List<ArtifactCard> getAllCardTypes() {
        List<ArtifactCard> allCards = new ArrayList<>();

        allCards.add(EnergyCard.createBasicEnergy());
        allCards.add(EnergyCard.createEnergyBoost());
        allCards.add(EnergyCard.createAdjacentEnergy());
        allCards.add(EnergyCard.createEnergySiphon());

        allCards.add(RiftCard.createBasicRepair());
        allCards.add(RiftCard.createQuantumEraser());
        allCards.add(RiftCard.createAdjacentRepair());
        allCards.add(RiftCard.createDrFooDisruptor());

        allCards.add(MovementCard.createTimeWalk());
        allCards.add(MovementCard.createQuantumLeap());
        allCards.add(MovementCard.createEnergeticStep());

        allCards.add(PushDuplicateCard.createBasicPush());
        allCards.add(PullDuplicateCard.createBasicPull());
        allCards.add(DestroyDuplicateCard.createBasicDestroy());

        return allCards;
    }

    public static List<ArtifactCard> getRandomSelection(List<ArtifactCard> cards, int count) {
        List<ArtifactCard> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled, random);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    public static List<ArtifactCard> createCardsByDimension(CardDimension dimension, int count) {
        List<ArtifactCard> cards = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ArtifactCard card = switch (dimension) {
                case SPIRAL -> createRandomMovementCard();
                case STAR -> createRandomEnergyCard();
                case STRIPE -> {
                    if (random.nextBoolean()) {
                        yield createRandomRiftCard();
                    } else {
                        yield createRandomDuplicateCard();
                    }
                }
                case BLACK_HOLE -> EnergyCard.createBasicEnergy(); // Fallback
            };
            cards.add(card);
        }

        return cards;
    }

    public static void printAllAvailableCards() {
        System.out.println("=== ALL AVAILABLE CARD TYPES ===");

        System.out.println("\nSTAR Dimension (Energy):");
        System.out.println("  - Basic Energy (1 energy current era)");
        System.out.println("  - Energy Boost (3 energy current era)");
        System.out.println("  - Adjacent Energy (1 energy to adjacent eras)");
        System.out.println("  - Energy Siphon (steal 2 energy from Dr. Foo)");

        System.out.println("\nSTRIPE Dimension (Rift & Duplicate):");
        System.out.println("  - Basic Repair (remove 1 rift current era)");
        System.out.println("  - Quantum Eraser (remove 2 rifts current era)");
        System.out.println("  - Adjacent Repair (remove 1 rift from adjacent eras)");
        System.out.println("  - Dr. Foo Disruptor (remove 2 rifts from Dr. Foo's era)");
        System.out.println("  - Push Duplicate (push duplicate to adjacent era)");
        System.out.println("  - Pull Duplicate (pull duplicate from adjacent era)");
        System.out.println("  - Destroy Duplicate (destroy duplicate on current era)");

        System.out.println("\nSPIRAL Dimension (Movement):");
        System.out.println("  - Time Walk (move to adjacent era)");
        System.out.println("  - Quantum Leap (move 2 eras)");
        System.out.println("  - Energetic Step (move + add 1 energy)");

        System.out.println("\nTotal: 14 different card types");
    }

    public static List<ArtifactCard> createTestDeck() {
        return getAllCardTypes();
    }
}