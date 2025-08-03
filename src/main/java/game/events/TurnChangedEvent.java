package game.events;

import game.model.Civilization;

/**
 * Event fired when the active civilization changes (turn ends).
 * Supports object pooling for better performance.
 */
public class TurnChangedEvent implements GameEvent {
    private Civilization previousCiv;
    private Civilization newCiv;
    private int turnNumber;
    private long timestamp;
    
    /**
     * Default constructor for pooling.
     */
    public TurnChangedEvent() {
        // Empty constructor for pooling
    }
    
    /**
     * Constructor for direct creation.
     */
    public TurnChangedEvent(Civilization previousCiv, Civilization newCiv, int turnNumber) {
        initialize(previousCiv, newCiv, turnNumber);
    }
    
    /**
     * Initializes the event with new data (for pooling).
     */
    public void initialize(Civilization previousCiv, Civilization newCiv, int turnNumber) {
        this.previousCiv = previousCiv;
        this.newCiv = newCiv;
        this.turnNumber = turnNumber;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Resets the event for return to pool.
     */
    public void reset() {
        this.previousCiv = null;
        this.newCiv = null;
        this.turnNumber = 0;
        this.timestamp = 0;
    }
    
    @Override
    public Civilization getCivilization() {
        return newCiv;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "TurnChanged";
    }
    
    // Getters
    public Civilization getPreviousCivilization() { return previousCiv; }
    public Civilization getNewCivilization() { return newCiv; }
    public int getTurnNumber() { return turnNumber; }
}
