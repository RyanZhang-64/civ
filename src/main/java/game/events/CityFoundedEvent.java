package game.events;

import game.model.City;
import game.model.Civilization;

/**
 * Event fired when a new city is founded.
 * Supports object pooling for better performance.
 */
public class CityFoundedEvent implements GameEvent {
    private City city;
    private long timestamp;
    
    /**
     * Default constructor for pooling.
     */
    public CityFoundedEvent() {
        // Empty constructor for pooling
    }
    
    /**
     * Constructor for direct creation.
     */
    public CityFoundedEvent(City city) {
        initialize(city);
    }
    
    /**
     * Initializes the event with new data (for pooling).
     */
    public void initialize(City city) {
        this.city = city;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Resets the event for return to pool.
     */
    public void reset() {
        this.city = null;
        this.timestamp = 0;
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
