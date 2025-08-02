package game.events;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central event manager that handles event subscription, publishing, and processing.
 * Implements the Observer pattern for decoupled event handling.
 */
public class GameEventManager {
    
    // Thread-safe list for listeners to handle concurrent modifications
    private final Map<Class<? extends GameEvent>, List<GameEventListener>> listeners = new HashMap<>();
    private final Queue<GameEvent> eventQueue = new LinkedList<>();
    private boolean processingEvents = false;
    
    /**
     * Subscribe a listener to receive events of a specific type.
     * @param eventType The class of events to listen for
     * @param listener The listener to notify when events occur
     */
    public void subscribe(Class<? extends GameEvent> eventType, GameEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        System.out.println("Subscribed " + listener.getListenerName() + " to " + eventType.getSimpleName());
    }
    
    /**
     * Unsubscribe a listener from a specific event type.
     * @param eventType The class of events to stop listening for
     * @param listener The listener to remove
     */
    public void unsubscribe(Class<? extends GameEvent> eventType, GameEventListener listener) {
        List<GameEventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
            System.out.println("Unsubscribed " + listener.getListenerName() + " from " + eventType.getSimpleName());
        }
    }
    
    /**
     * Publish an event to be processed. Events are queued and processed later to avoid
     * recursive event publishing during event processing.
     * @param event The event to publish
     */
    public void publish(GameEvent event) {
        eventQueue.offer(event);
        System.out.println("Published event: " + event.getEventType() + " for " + event.getCivilization().getName());
        
        // Process events immediately if we're not already processing
        if (!processingEvents) {
            processEvents();
        }
    }
    
    /**
     * Processes all queued events, notifying appropriate listeners.
     * This prevents recursive event publishing issues.
     */
    public void processEvents() {
        if (processingEvents) return; // Prevent recursive processing
        
        processingEvents = true;
        try {
            while (!eventQueue.isEmpty()) {
                GameEvent event = eventQueue.poll();
                notifyListeners(event);
            }
        } finally {
            processingEvents = false;
        }
    }
    
    /**
     * Notifies all listeners subscribed to the event's type.
     */
    private void notifyListeners(GameEvent event) {
        List<GameEventListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (GameEventListener listener : eventListeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    System.err.println("Error in event listener " + listener.getListenerName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Gets the number of queued events.
     * @return The number of events waiting to be processed
     */
    public int getQueuedEventCount() {
        return eventQueue.size();
    }
    
    /**
     * Gets the total number of listeners across all event types.
     * @return The total listener count
     */
    public int getTotalListenerCount() {
        return listeners.values().stream().mapToInt(List::size).sum();
    }
}
