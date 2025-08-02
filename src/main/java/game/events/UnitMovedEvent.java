package game.events;

import game.model.Civilization;
import game.model.Unit;

/**
 * Event fired when a unit moves to a new position.
 */
public class UnitMovedEvent implements GameEvent {
    private final Unit unit;
    private final int fromQ, fromR;
    private final int toQ, toR;
    private final long timestamp;
    
    public UnitMovedEvent(Unit unit, int fromQ, int fromR, int toQ, int toR) {
        this.unit = unit;
        this.fromQ = fromQ;
        this.fromR = fromR;
        this.toQ = toQ;
        this.toR = toR;
        this.timestamp = System.currentTimeMillis();
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
