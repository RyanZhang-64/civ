package game.model;

/**
 * ProductionMenuItem.java
 *
 * PURPOSE:
 * Extends ProductionItem with additional properties needed for menu display.
 * Provides categorization, prerequisites, and visual information for the UI.
 *
 * DESIGN PRINCIPLES:
 * - Interface Segregation: Separates menu concerns from core production logic
 * - Single Responsibility: Focuses on menu-specific data and behavior
 * - Composition over Inheritance: Can be implemented alongside ProductionItem
 */
public interface ProductionMenuItem extends ProductionItem {

    /**
     * Gets the category this item belongs to for menu organization.
     *
     * @return The production category
     */
    ProductionCategory getCategory();

    /**
     * Checks if this item can currently be produced by the given city.
     * Considers prerequisites, resources, technology, etc.
     *
     * @param city The city attempting to produce this item
     * @return true if the item can be produced, false otherwise
     */
    boolean canProduce(City city);

    /**
     * Gets the reason why this item cannot be produced (if applicable).
     * Used for tooltip explanations when canProduce() returns false.
     *
     * @param city The city attempting to produce this item
     * @return Explanation text, or null if the item can be produced
     */
    String getUnavailableReason(City city);

    /**
     * Gets the estimated number of turns to complete this item.
     * Based on the city's current production output.
     *
     * @param city The city that would produce this item
     * @return Number of turns needed, or -1 if cannot be produced
     */
    int getTurnsToComplete(City city);

    /**
     * Gets additional tooltip information about this item.
     * Can include effects, bonuses, or other relevant details.
     *
     * @return Extended description for detailed tooltips
     */
    String getTooltipText();
}