package game.events;

import game.model.Civilization;
import game.model.Unit;

/**
 * Event fired when a unit moves to a new position.
 * Supports object pooling for better performance.
 */
public class UnitMovedEvent implements GameEvent {
    private Unit unit;
    private int fromQ, fromR;
    private int toQ, toR;
    private long timestamp;
    
    /**
     * Default constructor for pooling.
     */
    public UnitMovedEvent() {
        // Empty constructor for pooling
    }
    
    /**
     * Constructor for direct creation.
     */
    public UnitMovedEvent(Unit unit, int fromQ, int fromR, int toQ, int toR) {
        initialize(unit, fromQ, fromR, toQ, toR);
    }
    
    /**
     * Initializes the event with new data (for pooling).
     */
    public void initialize(Unit unit, int fromQ, int fromR, int toQ, int toR) {
        this.unit = unit;
        this.fromQ = fromQ;
        this.fromR = fromR;
        this.toQ = toQ;
        this.toR = toR;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Resets the event for return to pool.
     */
    public void reset() {
        this.unit = null;
        this.fromQ = 0;
        this.fromR = 0;
        this.toQ = 0;
        this.toR = 0;
        this.timestamp = 0;
    }
    
    @Override
    public Civilization getCivilization() {
        return unit.owner;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "UnitMoved";
    }
    
    // Getters
    public Unit getUnit() { return unit; }
    public int getFromQ() { return fromQ; }
    public int getFromR() { return fromR; }
    public int getToQ() { return toQ; }
    public int getToR() { return toR; }
}
