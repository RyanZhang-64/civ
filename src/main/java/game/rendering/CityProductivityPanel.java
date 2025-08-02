package game.rendering;

import processing.core.PApplet;
import game.model.City;

/**
 * CityProductivityPanel.java
 *
 * PURPOSE:
 * Displays city productivity metrics in the top-left corner of the screen.
 * Shows population, production, gold, culture, and border growth information
 * with clean visual separation between each metric.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Focuses only on displaying productivity metrics
 * - Read-Only Interface: No user interactions, pure information display
 * - Data Binding: Updates automatically when city data changes
 * - Visual Consistency: Matches styling of other city UI panels
 */
public class CityProductivityPanel {

    private final PApplet p;
    private City currentCity;
    
    // Layout constants
    private static final float PANEL_WIDTH = 250;
    private static final float PANEL_HEIGHT = 172;
    private static final float MARGIN = 15;
    private static final float LINE_HEIGHT = 22;
    private static final float SEPARATOR_MARGIN = 8;
    
    // Calculated positions
    private final float panelX;
    private final float panelY;

    /**
     * Constructs a new CityProductivityPanel.
     *
     * @param p The Processing applet for rendering
     */
    public CityProductivityPanel(PApplet p) {
        this.p = p;
        this.currentCity = null;
        
        // Position in top-left corner
        this.panelX = MARGIN;
        this.panelY = MARGIN;
    }

    /**
     * Sets the city to display productivity information for.
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
     * Renders the city productivity panel.
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
        
        // Render productivity metrics
        renderProductivityMetrics();
    }

    /**
     * Renders all productivity metrics with separator lines.
     */
    private void renderProductivityMetrics() {
        float currentY = panelY + MARGIN;
        
        // Population and growth
        renderPopulationInfo(currentY);
        currentY += LINE_HEIGHT;
        
        // Separator line
        renderSeparatorLine(currentY);
        currentY += SEPARATOR_MARGIN;
        
        // Production per turn
        renderProductionInfo(currentY);
        currentY += LINE_HEIGHT;
        
        // Separator line
        renderSeparatorLine(currentY);
        currentY += SEPARATOR_MARGIN;
        
        // Gold per turn
        renderGoldInfo(currentY);
        currentY += LINE_HEIGHT;
        
        // Separator line
        renderSeparatorLine(currentY);
        currentY += SEPARATOR_MARGIN;
        
        // Culture per turn
        renderCultureInfo(currentY);
        currentY += LINE_HEIGHT;
        
        // Separator line
        renderSeparatorLine(currentY);
        currentY += SEPARATOR_MARGIN;
        
        // Border growth
        renderBorderGrowthInfo(currentY);
    }

    /**
     * Renders population and turns to next growth.
     */
    private void renderPopulationInfo(float y) {
        int population = currentCity.getPopulation();
        int turnsToGrowth = calculateTurnsToPopulationGrowth();
        
        p.fill(0);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(12);
        
        String populationText;
        if (turnsToGrowth > 0) {
            populationText = "Population: " + population + " (+1 in " + turnsToGrowth + "t)";
        } else {
            populationText = "Population: " + population + " (growing...)";
        }
        
        p.text(populationText, panelX + MARGIN, y);
    }

    /**
     * Renders production per turn information.
     */
    private void renderProductionInfo(float y) {
        int productionPerTurn = calculateProductionPerTurn();
        
        p.fill(0);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(12);
        p.text("Production: " + productionPerTurn + " per turn", panelX + MARGIN, y);
    }

    /**
     * Renders gold per turn information.
     */
    private void renderGoldInfo(float y) {
        int goldPerTurn = calculateGoldPerTurn();
        
        p.fill(0);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(12);
        p.text("Gold: " + goldPerTurn + " per turn", panelX + MARGIN, y);
    }

    /**
     * Renders culture per turn information.
     */
    private void renderCultureInfo(float y) {
        int culturePerTurn = calculateCulturePerTurn();
        
        p.fill(0);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(12);
        p.text("Culture: " + culturePerTurn + " per turn", panelX + MARGIN, y);
    }

    /**
     * Renders border growth information.
     */
    private void renderBorderGrowthInfo(float y) {
        int turnsToNextBorder = calculateTurnsToNextBorder();
        
        p.fill(0);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(12);
        p.text("Borders: +1 in " + turnsToNextBorder + " turns", panelX + MARGIN, y);
    }

    /**
     * Renders a thin white horizontal separator line.
     */
    private void renderSeparatorLine(float y) {
        p.stroke(255); // White line
        p.strokeWeight(1);
        p.line(panelX + MARGIN, y, panelX + PANEL_WIDTH - MARGIN, y);
    }

    /**
     * Calculates turns until next population growth.
     * Uses existing City food mechanics from CityManager.
     */
    private int calculateTurnsToPopulationGrowth() {
        try {
            int foodPerTurn = 2; // From CityManager.processCityTurn()
            int foodNeeded = currentCity.getPopulation() * 2; // From City.addFood()
            int currentFood = currentCity.getFoodStorage();
            
            if (foodPerTurn <= 0) {
                return -1; // No growth possible
            }
            
            int foodRequired = foodNeeded - currentFood;
            if (foodRequired <= 0) {
                return 1; // Will grow next turn
            }
            
            return (int) Math.ceil((double) foodRequired / foodPerTurn);
            
        } catch (Exception e) {
            // Graceful degradation
            return -1;
        }
    }

    /**
     * Calculates production per turn.
     * Uses the same calculation as CityManager.processCityTurn().
     */
    private int calculateProductionPerTurn() {
        try {
            // From CityManager.processCityTurn() - cities generate 2 production per turn
            return 2;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculates gold per turn.
     * Uses City.getGold() method.
     */
    private int calculateGoldPerTurn() {
        try {
            return currentCity.getGold();
        } catch (Exception e) {
            // Fallback calculation
            return Math.max(1, currentCity.getPopulation() / 2);
        }
    }

    /**
     * Calculates culture per turn.
     * Uses City.getCulture() method.
     */
    private int calculateCulturePerTurn() {
        try {
            return currentCity.getCulture();
        } catch (Exception e) {
            // Fallback calculation
            return 1; // Minimal culture generation
        }
    }

    /**
     * Calculates turns until next border expansion.
     * Uses City.getTurnsToNextBorderGrowth() method.
     */
    private int calculateTurnsToNextBorder() {
        try {
            return currentCity.getTurnsToNextBorderGrowth();
        } catch (Exception e) {
            // Fallback calculation
            return 10; // Default border growth time
        }
    }

    /**
     * Checks if the mouse click is within the panel area.
     * Note: This panel is read-only, but this method is provided for consistency.
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

    /**
     * Gets debug information about the panel state.
     *
     * @return String containing panel state information
     */
    public String getDebugInfo() {
        if (currentCity == null) {
            return "CityProductivityPanel: No city set";
        }
        
        return String.format("CityProductivityPanel: %s (Pop: %d, Food: %d)", 
                           currentCity.getName(), 
                           currentCity.getPopulation(), 
                           currentCity.getFoodStorage());
    }
}
