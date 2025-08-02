package game.rendering;

import processing.core.PApplet;
import game.model.City;
import game.model.ProductionItem;

/**
 * CitySummaryPanel.java
 *
 * PURPOSE:
 * Displays a compact city information panel in the bottom-left corner.
 * Shows current production progress and provides button to open full production menu.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Only handles city summary display and button interaction
 * - Event Delegation: Reports button clicks to parent for state management
 * - Responsive Layout: Adapts content based on available city information
 * - Consistent Styling: Matches overall game UI aesthetic
 */
public class CitySummaryPanel {

    private final PApplet p;
    private City currentCity;
    
    // Layout constants
    private static final float PANEL_WIDTH = 300;
    private static final float PANEL_HEIGHT = 120;
    private static final float MARGIN = 20;
    private static final float BUTTON_HEIGHT = 30;
    private static final float BUTTON_MARGIN = 10;
    
    // Calculated positions
    private final float panelX;
    private final float panelY;
    private final float buttonX;
    private final float buttonY;
    private final float buttonWidth;

    /**
     * Constructs a new CitySummaryPanel.
     *
     * @param p The Processing applet for rendering
     */
    public CitySummaryPanel(PApplet p) {
        this.p = p;
        this.currentCity = null;
        
        // Position in bottom-left corner
        this.panelX = MARGIN;
        this.panelY = p.height - PANEL_HEIGHT - MARGIN;
        
        // Button positioning within panel
        this.buttonX = panelX + BUTTON_MARGIN;
        this.buttonY = panelY + PANEL_HEIGHT - BUTTON_HEIGHT - BUTTON_MARGIN;
        this.buttonWidth = PANEL_WIDTH - 2 * BUTTON_MARGIN;
    }

    /**
     * Sets the city to display information for.
     *
     * @param city The city to display
     */
    public void setCity(City city) {
        this.currentCity = city;
    }

    /**
     * Gets the currently displayed city.
     *
     * @return The current city, or null if none set
     */
    public City getCurrentCity() {
        return currentCity;
    }

    /**
     * Renders the city summary panel.
     */
    public void render() {
        if (currentCity == null) {
            return;
        }

        // Draw panel background
        p.fill(240, 240, 240, 230); // Semi-transparent light gray
        p.stroke(100);
        p.strokeWeight(2);
        p.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, 8);
        
        // Draw content
        renderCityInfo();
        renderProductionInfo();
        renderChooseProductionButton();
    }

    /**
     * Renders the city name and basic information.
     */
    private void renderCityInfo() {
        p.fill(0);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(16);
        p.text("City: " + currentCity.getName(), panelX + 10, panelY + 10);
    }

    /**
     * Renders current production information.
     */
    private void renderProductionInfo() {
        float currentY = panelY + 35;
        
        ProductionItem currentProduction = currentCity.getCurrentProduction();
        if (currentProduction != null) {
            // Production name and progress
            int currentProgress = currentCity.getProductionProgress();
            int totalCost = currentProduction.getProductionCost();
            
            p.fill(0);
            p.textSize(12);
            p.text("Currently: " + currentProduction.getName(), panelX + 10, currentY);
            currentY += 16;
            
            // Progress display
            String progressText = "Progress: " + currentProgress + "/" + totalCost;
            p.text(progressText, panelX + 10, currentY);
            
            // Turns remaining
            int productionPerTurn = currentCity.getPopulation();
            if (productionPerTurn > 0) {
                int turnsRemaining = (int) Math.ceil((double)(totalCost - currentProgress) / productionPerTurn);
                p.textAlign(PApplet.RIGHT, PApplet.TOP);
                p.text("Turns: " + turnsRemaining, panelX + PANEL_WIDTH - 10, currentY);
            }
        } else {
            p.fill(100);
            p.textSize(12);
            p.text("No production selected", panelX + 10, currentY);
        }
    }

    /**
     * Renders the "Choose Production" button.
     */
    private void renderChooseProductionButton() {
        // Button background
        p.fill(70, 130, 180); // Steel blue
        p.stroke(50, 100, 150);
        p.strokeWeight(1);
        p.rect(buttonX, buttonY, buttonWidth, BUTTON_HEIGHT, 5);
        
        // Button text
        p.fill(255);
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.textSize(14);
        p.text("Choose Production", buttonX + buttonWidth / 2, buttonY + BUTTON_HEIGHT / 2);
    }

    /**
     * Checks if the "Choose Production" button was clicked.
     *
     * @param mouseX The x-coordinate of the mouse click
     * @param mouseY The y-coordinate of the mouse click
     * @return true if the button was clicked, false otherwise
     */
    public boolean isChooseProductionButtonClicked(float mouseX, float mouseY) {
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth &&
               mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;
    }

    /**
     * Checks if the mouse click is within the panel area.
     *
     * @param mouseX The x-coordinate of the mouse click
     * @param mouseY The y-coordinate of the mouse click
     * @return true if click is within panel bounds, false otherwise
     */
    public boolean isWithinPanel(float mouseX, float mouseY) {
        return mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH &&
               mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT;
    }

    /**
     * Gets the panel bounds for external positioning calculations.
     *
     * @return Array containing [x, y, width, height]
     */
    public float[] getPanelBounds() {
        return new float[]{panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT};
    }
}