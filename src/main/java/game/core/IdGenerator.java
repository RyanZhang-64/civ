package game.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * IdGenerator.java
 *
 * PURPOSE:
 * Service class responsible for generating unique IDs for game entities.
 * Replaces static ID counters in individual classes with a centralized,
 * thread-safe ID generation system.
 *
 * DESIGN PRINCIPLES:
 * - Thread Safety: Uses AtomicInteger for concurrent access
 * - Single Responsibility: Only handles ID generation
 * - Centralized Control: Single point for all ID generation logic
 * - Testability: Allows setting counters for testing scenarios
 */
public class IdGenerator {

    private final AtomicInteger cityCounter;
    private final AtomicInteger unitCounter;
    private final AtomicInteger civilizationCounter;

    /**
     * Constructs a new IdGenerator with default starting values.
     */
    public IdGenerator() {
        this.cityCounter = new AtomicInteger(1);
        this.unitCounter = new AtomicInteger(1);
        this.civilizationCounter = new AtomicInteger(1);
    }

    /**
     * Generates a unique ID for a city.
     *
     * @return A unique city ID
     */
    public int generateCityId() {
        return cityCounter.getAndIncrement();
    }

    /**
     * Generates a unique ID for a unit.
     *
     * @return A unique unit ID
     */
    public int generateUnitId() {
        return unitCounter.getAndIncrement();
    }

    /**
     * Generates a unique ID for a civilization.
     *
     * @return A unique civilization ID
     */
    public int generateCivilizationId() {
        return civilizationCounter.getAndIncrement();
    }

    /**
     * Gets the current city counter value.
     * Useful for save/load scenarios.
     *
     * @return The current city counter value
     */
    public int getCurrentCityCount() {
        return cityCounter.get();
    }

    /**
     * Gets the current unit counter value.
     * Useful for save/load scenarios.
     *
     * @return The current unit counter value
     */
    public int getCurrentUnitCount() {
        return unitCounter.get();
    }

    /**
     * Gets the current civilization counter value.
     * Useful for save/load scenarios.
     *
     * @return The current civilization counter value
     */
    public int getCurrentCivilizationCount() {
        return civilizationCounter.get();
    }

    /**
     * Sets the city counter to a specific value.
     * Used for testing or when loading saved games.
     *
     * @param value The value to set the counter to
     */
    public void setCityCounter(int value) {
        cityCounter.set(value);
    }

    /**
     * Sets the unit counter to a specific value.
     * Used for testing or when loading saved games.
     *
     * @param value The value to set the counter to
     */
    public void setUnitCounter(int value) {
        unitCounter.set(value);
    }

    /**
     * Sets the civilization counter to a specific value.
     * Used for testing or when loading saved games.
     *
     * @param value The value to set the counter to
     */
    public void setCivilizationCounter(int value) {
        civilizationCounter.set(value);
    }

    /**
     * Resets all counters to their initial values.
     * Useful for testing scenarios.
     */
    public void reset() {
        cityCounter.set(1);
        unitCounter.set(1);
        civilizationCounter.set(1);
    }
}
