package game.events;

import game.model.City;
import game.model.Civilization;

/**
 * CityFoundedEvent.java
 *
 * PURPOSE:
 * Event fired when a new city is founded by a civilization.
 * Used to trigger territory updates, UI notifications, and other systems.
 *
 * DESIGN PRINCIPLES:
 * - Immutable Event: All event data is final and cannot be changed
 * - Event Pattern: Implements GameEvent interface
 * - Rich Context: Provides city information for comprehensive handling
 */
public class CityFoundedEvent implements GameEvent {

    private final City city;
    private final long timestamp;

    /**
     * Constructs a new CityFoundedEvent.
     *
     * @param city The city that was founded
     */
    public CityFoundedEvent(City city) {
        this.city = city;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the city that was founded.
     *
     * @return The newly founded city
     */
    public City getCity() {
        return city;
    }

    @Override
    public Civilization getCivilization() {
        return city != null ? city.getOwner() : null;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("CityFoundedEvent[city=%s, position=(%d,%d), owner=%s]",
                city.getName(),
                city.getQ(),
                city.getR(),
                city.getOwner() != null ? city.getOwner().getName() : "null");
    }
}
