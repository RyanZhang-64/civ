package game.events;

import game.model.Civilization;
import game.model.Unit;

/**
 * Event fired when a new unit is created.
 */
public class UnitCreatedEvent implements GameEvent {
    private final Unit unit;
    private final long timestamp;
    
    public UnitCreatedEvent(Unit unit) {
        this.unit = unit;
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
        return "UnitCreated";
    }
    
    public Unit getUnit() { return unit; }
}
