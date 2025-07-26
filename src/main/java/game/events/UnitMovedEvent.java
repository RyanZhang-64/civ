package game.events;

import game.model.Civilization;
import game.model.Hex;
import game.model.Unit;

/**
 * UnitMovedEvent.java
 *
 * PURPOSE:
 * Event fired when a unit moves from one hex to another.
 * Used to trigger updates to visibility, UI, and other systems.
 *
 * DESIGN PRINCIPLES:
 * - Immutable Event: All event data is final and cannot be changed
 * - Rich Context: Provides both old and new positions for comprehensive handling
 * - Event Pattern: Implements GameEvent interface
 */
public class UnitMovedEvent implements GameEvent {

    private final Unit unit;
    private final Hex fromHex;
    private final Hex toHex;
    private final long timestamp;

    /**
     * Constructs a new UnitMovedEvent.
     *
     * @param unit The unit that moved
     * @param fromHex The hex the unit moved from
     * @param toHex The hex the unit moved to
     */
    public UnitMovedEvent(Unit unit, Hex fromHex, Hex toHex) {
        this.unit = unit;
        this.fromHex = fromHex;
        this.toHex = toHex;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the unit that moved.
     *
     * @return The unit that moved
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Gets the hex the unit moved from.
     *
     * @return The origin hex
     */
    public Hex getFromHex() {
        return fromHex;
    }

    /**
     * Gets the hex the unit moved to.
     *
     * @return The destination hex
     */
    public Hex getToHex() {
        return toHex;
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
        return String.format("UnitMovedEvent[unit=%s, from=(%d,%d), to=(%d,%d)]",
                unit.type,
                fromHex != null ? fromHex.q : -1,
                fromHex != null ? fromHex.r : -1,
                toHex != null ? toHex.q : -1,
                toHex != null ? toHex.r : -1);
    }
}
