package game.model;

/**
 * ProductionItem.java
 *
 * PURPOSE:
 * Interface for items that can be produced by cities. Uses the Strategy pattern
 * to allow different types of production (units, buildings, etc.) with
 * different behaviors when completed.
 *
 * DESIGN PRINCIPLES:
 * - Strategy Pattern: Different production types have different completion behavior
 * - Open/Closed Principle: Easy to add new production types without modifying existing code
 * - Single Responsibility: Each implementation handles one type of production
 */
public interface ProductionItem {

    /**
     * Gets the display name of this production item.
     *
     * @return The name to show in the UI.
     */
    String getName();

    /**
     * Gets the production cost required to complete this item.
     *
     * @return The total production points needed.
     */
    int getProductionCost();

    /**
     * Called when this production item is completed.
     * Implementations should handle the specific effects of completion
     * (e.g., spawning a unit, adding a building to the city).
     *
     * @param city The city that completed this production.
     */
    void onComplete(City city);

    /**
     * Gets a description of what this item does when completed.
     *
     * @return A brief description for UI tooltips.
     */
    String getDescription();
}
