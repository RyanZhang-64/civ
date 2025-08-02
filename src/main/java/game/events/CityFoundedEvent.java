package game.events;

import game.model.City;
import game.model.Civilization;

/**
 * Event fired when a new city is founded.
 */
public class CityFoundedEvent implements GameEvent {
    private final City city;
    private final long timestamp;
    
    public CityFoundedEvent(City city) {
        this.city = city;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public Civilization getCivilization() {
        return city.getOwner();
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "CityFounded";
    }
    
    public City getCity() { return city; }
}
