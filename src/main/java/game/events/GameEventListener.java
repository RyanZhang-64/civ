package game.events;

/**
 * Interface for objects that want to listen to game events.
 * Implements the Observer pattern for event handling.
 */
public interface GameEventListener {
    /**
     * Called when a subscribed event occurs.
     * @param event The event that occurred
     */
    void onEvent(GameEvent event);
    
    /**
     * Gets the name of this listener for debugging purposes.
     * @return A descriptive name for this listener
     */
    default String getListenerName() {
        return this.getClass().getSimpleName();
    }
}
