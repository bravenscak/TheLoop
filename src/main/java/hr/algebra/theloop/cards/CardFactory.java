package hr.algebra.theloop.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardFactory {

    private static final Random random = new Random();

    public static List<ArtifactCard> createRandomStartingDeck() {
        List<ArtifactCard> allStartingCards = new ArrayList<>();

        allStartingCards.add(createRandomEnergyCard());
        allStartingCards.add(createRandomRiftCard());
        allStartingCards.add(createRandomMovementCard());

        for (int i = 0; i < 6; i++) {
            allStartingCards.add(createRandomCard());
        }

        Collections.shuffle(allStartingCards, random);
        return allStartingCards;
    }

    public static List<ArtifactCard> createBasicStartingDeck() {
        List<ArtifactCard> startingCards = new ArrayList<>();

        startingCards.add(EnergyCard.createBasicEnergy());
        startingCards.add(RiftCard.createBasicRepair());
        startingCards.add(MovementCard.createTimeWalk());

        startingCards.add(EnergyCard.createEnergyBoost());
        startingCards.add(RiftCard.createQuantumEraser());
        startingCards.add(MovementCard.createQuantumLeap());
        startingCards.add(EnergyCard.createAdjacentEnergy());
        startingCards.add(RiftCard.createAdjacentRepair());
        startingCards.add(MovementCard.createEnergeticStep());

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
}