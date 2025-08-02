package game.rendering;

import game.Camera;
import game.GameConfig;
import game.core.CivilizationManager;
import game.core.CityManager;
import game.core.UnitManager;
import game.model.City;
import game.model.Unit;
import game.model.UnitType;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;

/**
 * UIManager.java
 *
 * PURPOSE:
 * Manages the creation, rendering, and interaction of all UI elements,
 * such as buttons.
 */
public class UIManager {

    private final PApplet p;
    private final UnitManager unitManager;
    private final CivilizationManager civilizationManager;
    private final CityManager cityManager;
    private final Camera camera;
    private final List<UIButton> buttons;
    private final ProductionMenuUI productionMenu;

    public UIManager(PApplet p, UnitManager um, CivilizationManager cm, CityManager cityManager, Camera camera) {
        this.p = p;
        this.unitManager = um;
        this.civilizationManager = cm;
        this.cityManager = cityManager;
        this.camera = camera;
        this.buttons = new ArrayList<>();
        this.productionMenu = new ProductionMenuUI(p);
        createButtons();
    }

    /**
     * Creates all the UI buttons and adds them to the manager.
     */
    private void createButtons() {
        float x = p.width - GameConfig.BUTTON_WIDTH - GameConfig.BUTTON_MARGIN;
        float y_nextTurn = p.height - GameConfig.BUTTON_HEIGHT - GameConfig.BUTTON_MARGIN;
        float y_next = p.height - (2 * GameConfig.BUTTON_HEIGHT) - (2 * GameConfig.BUTTON_MARGIN);

        buttons.add(new NextUnitButton(x, y_next, GameConfig.BUTTON_WIDTH, GameConfig.BUTTON_HEIGHT));
        buttons.add(new NextTurnButton(x, y_nextTurn, GameConfig.BUTTON_WIDTH, GameConfig.BUTTON_HEIGHT));
    }

    /**
     * Renders all visible UI elements.
     */
    public void render() {
        for (UIButton button : buttons) {
            button.display();
        }
        
        // Show Found City button if a settler is selected
        Unit selectedUnit = unitManager.getSelectedUnit();
        if (selectedUnit != null && selectedUnit.type == UnitType.SETTLER) {
            renderFoundCityButton();
        }
        
        // Render production menu if visible
        productionMenu.render();
    }
    
    /**
     * Renders the Found City button when a settler is selected.
     */
    private void renderFoundCityButton() {
        float x = p.width - GameConfig.BUTTON_WIDTH - GameConfig.BUTTON_MARGIN;
        float y = p.height - (3 * GameConfig.BUTTON_HEIGHT) - (3 * GameConfig.BUTTON_MARGIN);
        
        // Create temporary button for rendering
        FoundCityButton foundCityButton = new FoundCityButton(x, y, GameConfig.BUTTON_WIDTH, GameConfig.BUTTON_HEIGHT);
        foundCityButton.display();
    }

    /**
     * Handles a mouse press event, checking if any button was clicked.
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @return true if a UI element handled the click, false otherwise.
     */
    public boolean handleMousePress(float mouseX, float mouseY) {
        // Check production menu first (highest priority)
        if (productionMenu.handleMouseClick(mouseX, mouseY)) {
            return true;
        }
        
        // Check regular buttons
        for (UIButton button : buttons) {
            if (button.isClicked(mouseX, mouseY)) {
                button.onClick();
                return true;
            }
        }
        
        // Check Found City button if a settler is selected
        Unit selectedUnit = unitManager.getSelectedUnit();
        if (selectedUnit != null && selectedUnit.type == UnitType.SETTLER) {
            float x = p.width - GameConfig.BUTTON_WIDTH - GameConfig.BUTTON_MARGIN;
            float y = p.height - (3 * GameConfig.BUTTON_HEIGHT) - (3 * GameConfig.BUTTON_MARGIN);
            
            FoundCityButton foundCityButton = new FoundCityButton(x, y, GameConfig.BUTTON_WIDTH, GameConfig.BUTTON_HEIGHT);
            if (foundCityButton.isClicked(mouseX, mouseY)) {
                foundCityButton.onClick();
                return true;
            }
        }
        
        return false;
    }

    // =========================================================================
    // Inner Classes for UI Buttons
    // =========================================================================

    private abstract class UIButton {
        float x, y, width, height;
        String label;
        int backgroundColor, textColor;

        UIButton(float x, float y, float w, float h, String label, int bgColor, int txtColor) {
            this.x = x; this.y = y; this.width = w; this.height = h;
            this.label = label; this.backgroundColor = bgColor; this.textColor = txtColor;
        }

        boolean isClicked(float mouseX, float mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        void display() {
            p.fill(backgroundColor);
            p.stroke(0);
            p.strokeWeight(2);
            p.rect(x, y, width, height, 5);
            p.fill(textColor);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.textSize(14);
            p.text(label, x + width / 2, y + height / 2);
        }

        abstract void onClick();
    }

    private class NextTurnButton extends UIButton {
        NextTurnButton(float x, float y, float w, float h) {
            super(x, y, w, h, "Next Turn", p.color(255, 100, 50), p.color(255));
        }

        @Override
        void onClick() {
            civilizationManager.nextTurn();
            // Center camera on the new civilization's first unit
            Unit firstUnit = civilizationManager.getFirstUnitOfCurrentCivilization();
            if (firstUnit != null) {
                float x = GameConfig.HEX_RADIUS * (PApplet.sqrt(3) * firstUnit.q + PApplet.sqrt(3) / 2.0f * firstUnit.r);
                float y = GameConfig.HEX_RADIUS * (3.0f / 2.0f * firstUnit.r);
                camera.centerOn(x, y);
            }
        }
    }

    private class NextUnitButton extends UIButton {
        NextUnitButton(float x, float y, float w, float h) {
            super(x, y, w, h, "Next Unit", p.color(50, 200, 100), p.color(255));
        }

        @Override
        void onClick() {
            unitManager.selectNextUnitWithMovement();
        }
    }

    private class FoundCityButton extends UIButton {
        FoundCityButton(float x, float y, float w, float h) {
            super(x, y, w, h, "Found City", p.color(200, 150, 50), p.color(255));
        }

        @Override
        void onClick() {
            Unit selectedUnit = unitManager.getSelectedUnit();
            if (selectedUnit != null && selectedUnit.type == UnitType.SETTLER) {
                // Generate a simple city name
                String cityName = "City " + (cityManager.getCityCount() + 1);
                cityManager.foundCity(selectedUnit, cityName);
                // Deselect the unit since it's been consumed
                unitManager.deselectUnit();
            }
        }
    }

    // Add methods for showing the production menu
    public void showProductionMenuForCity(City city) {
        productionMenu.showCitySummary(city); // Changed from showForCity
    }

    public boolean isProductionMenuVisible() {
        return productionMenu.isVisible();
    }

    // Add scroll handling method
    public void handleScroll(float scrollAmount) {
        if (productionMenu.isVisible()) {
            productionMenu.handleScroll(scrollAmount);
        }
    }

    // Add this new method to UIManager
    public void handleKeyPressed(int keyCode) {
        if (productionMenu.isVisible()) {
            productionMenu.handleKeyPressed(keyCode);
        }
    }
}
