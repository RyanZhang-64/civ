package game.events;

/**
 * GameEventListener.java
 *
 * PURPOSE:
 * Interface for classes that want to listen to and respond to game events.
 * Part of the Observer pattern implementation for the event system.
 *
 * DESIGN PRINCIPLES:
 * - Observer Pattern: Defines the observer interface
 * - Single Responsibility: Each listener handles specific event types
 * - Loose Coupling: Listeners don't need to know about event publishers
 */
public interface GameEventListener {

    /**
     * Called when a game event occurs that this listener is subscribed to.
     *
     * @param event The event that occurred
     */
    void onEvent(GameEvent event);

    /**
     * Determines if this listener can handle the given event type.
     * Used by the event manager for efficient event routing.
     *
     * @param eventType The class of the event type to check
     * @return true if this listener can handle events of this type
     */
    default boolean canHandle(Class<? extends GameEvent> eventType) {
        return true; // Default implementation accepts all events
    }
}
