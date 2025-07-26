package game.events;

import game.model.Civilization;

/**
 * GameEvent.java
 *
 * PURPOSE:
 * Base interface for all game events in the event-driven architecture.
 * Provides common properties that all events should have.
 *
 * DESIGN PRINCIPLES:
 * - Observer Pattern: Base interface for event system
 * - Common Interface: Ensures all events have essential properties
 * - Extensibility: Easy to add new event types by implementing this interface
 */
public interface GameEvent {

    /**
     * Gets the civilization associated with this event.
     * Some events may not be civilization-specific and can return null.
     *
     * @return The civilization involved in this event, or null if not applicable
     */
    Civilization getCivilization();

    /**
     * Gets the timestamp when this event occurred.
     *
     * @return The timestamp in milliseconds since epoch
     */
    long getTimestamp();

    /**
     * Gets the type of this event as a string.
     * Used for logging and debugging purposes.
     *
     * @return A string representation of the event type
     */
    default String getEventType() {
        return this.getClass().getSimpleName();
    }
}
