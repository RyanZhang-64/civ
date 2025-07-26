package game.events;

import game.model.Civilization;
import game.model.Unit;

/**
 * UnitCreatedEvent.java
 *
 * PURPOSE:
 * Event fired when a new unit is created (spawned) in the game.
 * Used to trigger visibility updates, UI notifications, and other systems.
 *
 * DESIGN PRINCIPLES:
 * - Immutable Event: All event data is final and cannot be changed
 * - Event Pattern: Implements GameEvent interface
 * - Single Responsibility: Only carries unit creation information
 */
public class UnitCreatedEvent implements GameEvent {

    private final Unit unit;
    private final long timestamp;

    /**
     * Constructs a new UnitCreatedEvent.
     *
     * @param unit The unit that was created
     */
    public UnitCreatedEvent(Unit unit) {
        this.unit = unit;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the unit that was created.
     *
     * @return The newly created unit
     */
    public Unit getUnit() {
        return unit;
    }

    @Override
    public Civilization getCivilization() {
        return unit != null ? unit.owner : null;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("UnitCreatedEvent[unit=%s, position=(%d,%d), owner=%s]",
                unit.type,
                unit.q,
                unit.r,
                unit.owner != null ? unit.owner.getName() : "null");
    }
}
