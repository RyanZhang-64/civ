package game.rendering;

import processing.core.PApplet;
import game.model.City;
import game.model.ProductionMenuItem;

/**
 * ProductionMenuUI.java
 *
 * PURPOSE:
 * Main orchestrator for the two-panel city production menu system.
 * Manages state transitions between hidden, summary, and full menu states.
 *
 * DESIGN PRINCIPLES:
 * - State Machine: Clear state management with defined transitions
 * - Component Coordination: Orchestrates child components without tight coupling
 * - Event Delegation: Proper separation between input handling and state management
 * - Progressive Disclosure: Information revealed in logical stages
 */
public class ProductionMenuUI {

    private final PApplet p;
    
    // Child components
    private final CitySummaryPanel summaryPanel;
    private final ProductionSidePanel sidePanel;
    private final CityProductivityPanel productivityPanel;
    
    // State management
    private MenuState currentState;
    private City currentCity;

    /**
     * Constructs a new ProductionMenuUI.
     *
     * @param p The Processing applet for rendering
     */
    public ProductionMenuUI(PApplet p) {
        this.p = p;
        this.currentState = MenuState.HIDDEN;
        this.currentCity = null;
        
        // Initialize child components
        this.summaryPanel = new CitySummaryPanel(p);
        this.sidePanel = new ProductionSidePanel(p);
        this.productivityPanel = new CityProductivityPanel(p);
    }

    /**
     * Shows the city summary panel for the specified city.
     * This is the first stage of the two-stage interaction.
     *
     * @param city The city to show summary for
     */
    public void showCitySummary(City city) {
        this.currentCity = city;
        this.currentState = MenuState.SUMMARY_VISIBLE;
        
        // Update child components
        summaryPanel.setCity(city);
        sidePanel.setCity(city);
        productivityPanel.setCity(city);
    }

    /**
     * Shows the full production menu.
     * This is the second stage triggered by the "Choose Production" button.
     */
    public void showFullMenu() {
        if (currentState == MenuState.SUMMARY_VISIBLE && currentCity != null) {
            currentState = MenuState.FULL_MENU_VISIBLE;
        }
    }

    /**
     * Hides the entire menu system.
     */
    public void hide() {
        currentState = MenuState.HIDDEN;
        currentCity = null;
    }

    /**
     * Checks if any part of the menu system is visible.
     *
     * @return true if menu is visible in any state, false if hidden
     */
    public boolean isVisible() {
        return currentState != MenuState.HIDDEN;
    }

    /**
     * Gets the current menu state.
     *
     * @return The current MenuState
     */
    public MenuState getCurrentState() {
        return currentState;
    }

    /**
     * Gets the current city being displayed.
     *
     * @return The current city, or null if menu is hidden
     */
    public City getCurrentCity() {
        return currentCity;
    }

    /**
     * Renders the appropriate UI components based on current state.
     */
    public void render() {
        switch (currentState) {
            case HIDDEN:
                // Nothing to render
                break;
                
            case SUMMARY_VISIBLE:
                renderSummaryState();
                break;
                
            case FULL_MENU_VISIBLE:
                renderFullMenuState();
                break;
        }
    }

    /**
     * Renders the summary state: darkened screen + summary panel + productivity panel.
     */
    private void renderSummaryState() {
        // Draw darkening overlay
        p.fill(0, 0, 0, 150); // Semi-transparent black
        p.noStroke();
        p.rect(0, 0, p.width, p.height);
        
        // Render both panels
        summaryPanel.render();        // Bottom-left
        productivityPanel.render();   // Top-left
    }

    /**
     * Renders the full menu state: side panel only (no overlay).
     */
    private void renderFullMenuState() {
        // No overlay - allow game to remain visible
        sidePanel.render();
    }

    /**
     * Handles mouse click events and manages state transitions.
     *
     * @param mouseX The x-coordinate of the click
     * @param mouseY The y-coordinate of the click
     * @return true if the event was handled, false otherwise
     */
    public boolean handleMouseClick(float mouseX, float mouseY) {
        switch (currentState) {
            case HIDDEN:
                return false; // No handling when hidden
                
            case SUMMARY_VISIBLE:
                return handleSummaryClick(mouseX, mouseY);
                
            case FULL_MENU_VISIBLE:
                return handleFullMenuClick(mouseX, mouseY);
                
            default:
                return false;
        }
    }

    /**
     * Handles clicks when in summary state.
     */
    private boolean handleSummaryClick(float mouseX, float mouseY) {
        // Check if "Choose Production" button was clicked
        if (summaryPanel.isChooseProductionButtonClicked(mouseX, mouseY)) {
            showFullMenu();
            return true;
        }
        
        // Check if click is within summary panel (consume event)
        if (summaryPanel.isWithinPanel(mouseX, mouseY)) {
            return true;
        }
        
        // Check if click is within productivity panel (consume event)
        if (productivityPanel.isWithinPanel(mouseX, mouseY)) {
            return true;
        }
        
        // Click outside both panels - hide menu
        hide();
        return true;
    }

    /**
     * Handles clicks when in full menu state.
     */
    private boolean handleFullMenuClick(float mouseX, float mouseY) {
        // Forward to side panel first
        if (sidePanel.handleMouseClick(mouseX, mouseY)) {
            // Check if an item was selected
            ProductionMenuItem selectedItem = sidePanel.getSelectedItem();
            if (selectedItem != null && currentCity != null) {
                // Apply selection and close menu
                currentCity.setCurrentProduction(selectedItem);
                hide();
            }
            return true;
        }
        
        // Click outside panel - hide menu
        if (!sidePanel.isWithinPanel(mouseX, mouseY)) {
            hide();
            return true;
        }
        
        return false;
    }

    /**
     * Handles mouse scroll events.
     *
     * @param scrollAmount The scroll amount (positive = down, negative = up)
     */
    public void handleScroll(float scrollAmount) {
        if (currentState == MenuState.FULL_MENU_VISIBLE) {
            sidePanel.handleScroll(scrollAmount);
        }
    }

    /**
     * Handles keyboard input for menu navigation.
     *
     * @param keyCode The key that was pressed
     */
    public void handleKeyPressed(int keyCode) {
        // ESC key handling
        if (keyCode == 27) { // ESC key
            switch (currentState) {
                case FULL_MENU_VISIBLE:
                    currentState = MenuState.SUMMARY_VISIBLE;
                    break;
                case SUMMARY_VISIBLE:
                    hide();
                    break;
                default:
                    // No action for HIDDEN state
                    break;
            }
        }
    }

    /**
     * Gets debug information about the current state.
     *
     * @return String containing current state information
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder();
        info.append("MenuState: ").append(currentState.getDescription());
        if (currentCity != null) {
            info.append(", City: ").append(currentCity.getName());
            info.append(", Pop: ").append(currentCity.getPopulation());
            info.append(", Gold: ").append(currentCity.getGold());
            info.append(", Culture: ").append(currentCity.getCulture());
        }
        return info.toString();
    }
}