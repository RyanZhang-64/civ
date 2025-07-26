package game.events;

import game.model.Civilization;

/**
 * TurnChangedEvent.java
 *
 * PURPOSE:
 * Event fired when the game turn changes from one civilization to another.
 * Used to trigger turn processing, UI updates, and other turn-based systems.
 *
 * DESIGN PRINCIPLES:
 * - Immutable Event: All event data is final and cannot be changed
 * - Event Pattern: Implements GameEvent interface
 * - Rich Context: Provides both previous and current civilization info
 */
public class TurnChangedEvent implements GameEvent {

    private final Civilization previousCivilization;
    private final Civilization currentCivilization;
    private final int turnNumber;
    private final long timestamp;

    /**
     * Constructs a new TurnChangedEvent.
     *
     * @param previousCivilization The civilization whose turn just ended
     * @param currentCivilization The civilization whose turn is starting
     * @param turnNumber The current turn number
     */
    public TurnChangedEvent(Civilization previousCivilization, Civilization currentCivilization, int turnNumber) {
        this.previousCivilization = previousCivilization;
        this.currentCivilization = currentCivilization;
        this.turnNumber = turnNumber;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the civilization whose turn just ended.
     *
     * @return The previous civilization
     */
    public Civilization getPreviousCivilization() {
        return previousCivilization;
    }

    /**
     * Gets the civilization whose turn is starting.
     *
     * @return The current civilization
     */
    public Civilization getCurrentCivilization() {
        return currentCivilization;
    }

    /**
     * Gets the current turn number.
     *
     * @return The turn number
     */
    public int getTurnNumber() {
        return turnNumber;
    }

    @Override
    public Civilization getCivilization() {
        return currentCivilization;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("TurnChangedEvent[from=%s, to=%s, turn=%d]",
                previousCivilization != null ? previousCivilization.getName() : "null",
                currentCivilization != null ? currentCivilization.getName() : "null",
                turnNumber);
    }
}
