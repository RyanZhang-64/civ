package game.model;

/**
 * ProductionCategory.java
 *
 * PURPOSE:
 * Defines the different categories of items that can be produced by cities.
 * Used for organizing the production menu and providing visual separation.
 *
 * DESIGN PRINCIPLES:
 * - Type Safety: Enum prevents invalid category values
 * - Extensibility: Easy to add new categories without breaking existing code
 * - UI Integration: Provides display names and descriptions for menu sections
 */
public enum ProductionCategory {
    UNITS("Units", "Military and civilian units"),
    BUILDINGS("Buildings", "City improvements and infrastructure"),
    WONDERS("Wonders", "Unique world wonders with special effects");

    private final String displayName;
    private final String description;

    /**
     * Constructor for ProductionCategory.
     *
     * @param displayName The name to show in the UI
     * @param description Brief description of this category
     */
    ProductionCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Gets the display name for UI presentation.
     *
     * @return The formatted name to show in menus
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the category description for tooltips.
     *
     * @return Brief description of what this category contains
     */
    public String getDescription() {
        return description;
    }
}