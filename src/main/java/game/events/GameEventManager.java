package game.events;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GameEventManager.java
 *
 * PURPOSE:
 * Central event manager that handles subscription, publishing, and processing
 * of game events. Implements the Observer pattern for loose coupling between
 * game components.
 *
 * DESIGN PRINCIPLES:
 * - Observer Pattern: Manages observers (listeners) and notifies them of events
 * - Single Responsibility: Only handles event routing and processing
 * - Thread Safety: Uses concurrent collections for thread safety
 * - Performance: Efficient event routing using type-based lookup
 */
public class GameEventManager {

    private final Map<Class<? extends GameEvent>, List<GameEventListener>> listeners;
    private final Queue<GameEvent> eventQueue;
    private boolean processingEvents;

    /**
     * Constructs a new GameEventManager.
     */
    public GameEventManager() {
        this.listeners = new ConcurrentHashMap<>();
        this.eventQueue = new LinkedList<>();
        this.processingEvents = false;
    }

    /**
     * Subscribes a listener to a specific event type.
     *
     * @param eventType The class of events to listen for
     * @param listener The listener to subscribe
     */
    public void subscribe(Class<? extends GameEvent> eventType, GameEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Unsubscribes a listener from a specific event type.
     *
     * @param eventType The class of events to stop listening for
     * @param listener The listener to unsubscribe
     */
    public void unsubscribe(Class<? extends GameEvent> eventType, GameEventListener listener) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                listeners.remove(eventType);
            }
        }
    }

    /**
     * Publishes an event immediately to all subscribed listeners.
     * Events are processed synchronously in the order they are published.
     *
     * @param event The event to publish
     */
    public void publish(GameEvent event) {
        if (event == null) return;

        List<GameEventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            // Create a copy to avoid concurrent modification issues
            List<GameEventListener> listenersCopy = new ArrayList<>(eventListeners);
            for (GameEventListener listener : listenersCopy) {
                try {
                    if (listener.canHandle(event.getClass())) {
                        listener.onEvent(event);
                    }
                } catch (Exception e) {
                    // Log error but continue processing other listeners
                    System.err.println("Error processing event " + event.getEventType() + 
                                     " in listener " + listener.getClass().getSimpleName() + 
                                     ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Queues an event for later processing.
     * Useful when you want to batch process events or delay processing.
     *
     * @param event The event to queue
     */
    public void queueEvent(GameEvent event) {
        if (event != null) {
            eventQueue.offer(event);
        }
    }

    /**
     * Processes all queued events.
     * Events are processed in the order they were queued.
     */
    public void processQueuedEvents() {
        if (processingEvents) {
            return; // Prevent recursive processing
        }

        processingEvents = true;
        try {
            while (!eventQueue.isEmpty()) {
                GameEvent event = eventQueue.poll();
                if (event != null) {
                    publish(event);
                }
            }
        } finally {
            processingEvents = false;
        }
    }

    /**
     * Gets the number of listeners for a specific event type.
     *
     * @param eventType The event type to check
     * @return The number of listeners subscribed to this event type
     */
    public int getListenerCount(Class<? extends GameEvent> eventType) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }

    /**
     * Gets the total number of queued events waiting to be processed.
     *
     * @return The number of queued events
     */
    public int getQueuedEventCount() {
        return eventQueue.size();
    }

    /**
     * Clears all queued events without processing them.
     */
    public void clearEventQueue() {
        eventQueue.clear();
    }

    /**
     * Removes all listeners from all event types.
     */
    public void clearAllListeners() {
        listeners.clear();
    }
}
