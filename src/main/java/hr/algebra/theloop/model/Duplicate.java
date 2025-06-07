package hr.algebra.theloop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;


@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Duplicate implements Serializable {

    @EqualsAndHashCode.Include
    private String id;

    private Era spawnEra;
    private Era destroyEra;
    private Era currentEra;
    private int turnsActive;

    public Duplicate(Era spawnEra) {
        this.id = "DUP_" + UUID.randomUUID().toString().substring(0, 8);
        this.spawnEra = spawnEra;
        this.currentEra = spawnEra;
        this.destroyEra = calculateDestroyEra(spawnEra);
        this.turnsActive = 0;
    }

    public Duplicate(Duplicate other) {
        this.id = other.id;
        this.spawnEra = other.spawnEra;
        this.destroyEra = other.destroyEra;
        this.currentEra = other.currentEra;
        this.turnsActive = other.turnsActive;
    }

    private Era calculateDestroyEra(Era spawn) {
        return spawn.getOpposite();
    }

    public void moveTo(Era newEra) {
        this.currentEra = newEra;
    }

    public boolean pushTo(Era targetEra) {
        if (currentEra.isAdjacentTo(targetEra)) {
            this.currentEra = targetEra;
            return true;
        }
        return false;
    }

    public boolean canBeDestroyedAt(Era era) {
        return era.equals(destroyEra);
    }

    public boolean isAtDestructionEra() {
        return currentEra.equals(destroyEra);
    }

    public void ageTurn() {
        turnsActive++;
    }

    public int distanceToDestruction() {
        return currentEra.distanceTo(destroyEra);
    }

    public boolean isAdjacentToDestruction() {
        return currentEra.isAdjacentTo(destroyEra);
    }

    public String getDisplayName() {
        return "Dr. Foo #" + id.substring(4);
    }

    public String getIcon() {
        return "👤"; // Generic duplicate icon
    }

    public String getMonocleDisplay() {
        return destroyEra.getIcon() + " " + destroyEra.getDisplayName();
    }

    public String getStatusInfo() {
        return String.format("Duplicate %s at %s (destroy at %s, age: %d)",
                getDisplayName(),
                currentEra.getDisplayName(),
                destroyEra.getDisplayName(),
                turnsActive);
    }

    @Override
    public String toString() {
        return String.format("Duplicate[%s: %s → %s → %s]",
                id, spawnEra.getDisplayName(),
                currentEra.getDisplayName(),
                destroyEra.getDisplayName());
    }
}