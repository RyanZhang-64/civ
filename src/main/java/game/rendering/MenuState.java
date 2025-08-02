package game.rendering;

/**
 * MenuState.java
 *
 * PURPOSE:
 * Defines the different states of the city production menu system.
 * Manages the two-stage interaction flow from city summary to full production menu.
 *
 * DESIGN PRINCIPLES:
 * - State Pattern: Clear state definitions prevent invalid transitions
 * - Type Safety: Enum prevents invalid state values
 * - Single Responsibility: Each state has specific UI and interaction behavior
 */
public enum MenuState {
    /**
     * No menu is visible. Normal game interaction.
     */
    HIDDEN("No menu visible"),
    
    /**
     * City summary panel is visible in bottom-left.
     * Shows current production and "Choose Production" button.
     */
    SUMMARY_VISIBLE("City summary panel visible"),
    
    /**
     * Full production menu is visible on left side.
     * Shows all available production options organized by category.
     */
    FULL_MENU_VISIBLE("Full production menu visible");

    private final String description;

    /**
     * Constructor for MenuState.
     *
     * @param description Human-readable description of the state
     */
    MenuState(String description) {
        this.description = description;
    }

    /**
     * Gets the human-readable description of this state.
     *
     * @return The state description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this state allows normal game interaction.
     *
     * @return true if game is interactive, false if UI blocks interaction
     */
    public boolean allowsGameInteraction() {
        return this == HIDDEN || this == FULL_MENU_VISIBLE;
    }

    /**
     * Checks if this state should darken the screen.
     *
     * @return true if screen should be darkened
     */
    public boolean shouldDarkenScreen() {
        return this == SUMMARY_VISIBLE;
    }
}