package game.events;

import game.model.Civilization;

/**
 * Base interface for all game events.
 * Events represent significant state changes that other systems need to react to.
 */
public interface GameEvent {
    /**
     * Gets the civilization associated with this event.
     * @return The civilization that triggered or is affected by this event
     */
    Civilization getCivilization();
    
    /**
     * Gets the timestamp when this event occurred.
     * @return The timestamp in milliseconds since epoch
     */
    long getTimestamp();
    
    /**
     * Gets the type of event for logging and debugging.
     * @return A string describing the event type
     */
    String getEventType();
}
