package hr.algebra.theloop.cards.movement;

import hr.algebra.theloop.cards.ArtifactCard;
import hr.algebra.theloop.cards.CardDimension;
import hr.algebra.theloop.model.Era;
import hr.algebra.theloop.model.GameState;
import hr.algebra.theloop.model.Player;

public class MovementCard extends ArtifactCard {
    private final int moveDistance;

    public MovementCard(String name, Era originalEra, int moveDistance) {
        super(name, "Move " + moveDistance + " era(s)", originalEra, CardDimension.SPIRAL);
        this.moveDistance = moveDistance;
    }

    @Override
    public void execute(GameState gameState, Player player) {
        Era currentEra = player.getCurrentEra();
        Era targetEra = currentEra;

        for (int i = 0; i < moveDistance; i++) {
            targetEra = targetEra.getNext();
        }

        player.moveToEra(targetEra);
        exhaust();
        System.out.println("🚶 " + getName() + ": " + player.getName() + " moved to " + targetEra.getDisplayName());
    }

    @Override
    public boolean canExecute(GameState gameState, Player player) {
        return !isExhausted();
    }
}