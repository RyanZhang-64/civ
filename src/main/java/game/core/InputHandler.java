package game.core;

import game.Camera;
import game.model.City;
import game.GameConfig;
import game.model.Hex;
import game.model.HexGrid;
import game.rendering.UIManager;
import processing.core.PApplet;
import processing.core.PVector;
import processing.event.MouseEvent;

/**
 * InputHandler.java
 *
 * PURPOSE:
 * Processes all raw user input and translates it into game commands. It acts
 * as the controller for player interaction, communicating with other managers.
 */
public class InputHandler {
    private final PApplet p;
    private final Camera camera;
    private final HexGrid hexGrid;
    private final UnitManager unitManager;
    private final UIManager uiManager;
    private final CityManager cityManager;

    private boolean isDragging = false;
    private float initialMouseX;
    private float initialMouseY;
    private boolean hasMovedSinceClick = false;
    private Hex clickedHex = null;

    public InputHandler(PApplet p, Camera cam, HexGrid grid, UnitManager um, UIManager uim, CityManager cm) {
        this.p = p;
        this.camera = cam;
        this.hexGrid = grid;
        this.unitManager = um;
        this.uiManager = uim;
        this.cityManager = cm;
    }

    public void handleMousePressed() {
        // Store initial click position
        initialMouseX = p.mouseX;
        initialMouseY = p.mouseY;
        hasMovedSinceClick = false;
        isDragging = true;

        // First, check if a UI element was clicked.
        if (uiManager.handleMousePress(p.mouseX, p.mouseY)) {
            isDragging = false;
            return; // UI handled the click, so we do nothing else.
        }

        // Convert screen coordinates to world coordinates.
        PVector worldCoords = camera.screenToWorld(new PVector(p.mouseX, p.mouseY));
        clickedHex = pixelToHex(worldCoords);

        // Don't take any action yet - wait to see if this is a click or a drag
        if (clickedHex == null) {
            unitManager.deselectUnit();
        }
    }

    public void handleMouseReleased() {
        if (isDragging && !hasMovedSinceClick && clickedHex != null) {
            // Check for city click first
            City cityAtHex = cityManager.getCityAt(clickedHex.q, clickedHex.r);
            if (cityAtHex != null) {
                uiManager.showProductionMenuForCity(cityAtHex);
            } else if (unitManager.getSelectedUnit() != null && unitManager.getReachableHexes().containsKey(clickedHex)) {
                unitManager.moveSelectedUnit(clickedHex);
            } else {
                unitManager.selectUnitAt(clickedHex);
            }
        }
        isDragging = false;
        hasMovedSinceClick = false;
        clickedHex = null;
    }

    public void handleMouseDragged() {
        if (!isDragging) return;

        float dx = p.mouseX - p.pmouseX;
        float dy = p.mouseY - p.pmouseY;

        // Check if we've moved enough to consider this a drag
        if (!hasMovedSinceClick && 
            (Math.abs(p.mouseX - initialMouseX) > 2 || 
             Math.abs(p.mouseY - initialMouseY) > 2)) {
            hasMovedSinceClick = true;
        }

        // If we're dragging, always pan
        if (hasMovedSinceClick) {
            camera.panBy(dx, dy);
        }
    }

    public void handleMouseWheel(MouseEvent event) {
        // Check if UI should handle the scroll first
        if (uiManager.isProductionMenuVisible()) {
            uiManager.handleScroll(event.getCount());
            return;
        }
        
        // Otherwise handle as camera zoom
        float zoomFactor = (event.getCount() < 0) ? GameConfig.ZOOM_SENSITIVITY : 1.0f / GameConfig.ZOOM_SENSITIVITY;
        camera.zoom(zoomFactor, p.mouseX, p.mouseY);
    }

    public void handleKeyPressed() {
        // Placeholder for keyboard shortcuts
    }

    /**
     * Converts pixel coordinates (in world space) to the corresponding hex.
     * @param worldCoords The world coordinates.
     * @return The Hex at that position, or null.
     */
    private Hex pixelToHex(PVector worldCoords) {
        // This is the corrected formula for converting world-space pixels to fractional axial coordinates.
        // It no longer incorrectly subtracts the screen center.
        float q_frac = (PApplet.sqrt(3) / 3.0f * worldCoords.x - 1.0f / 3.0f * worldCoords.y) / GameConfig.HEX_RADIUS;
        float r_frac = (2.0f / 3.0f * worldCoords.y) / GameConfig.HEX_RADIUS;
        
        // The rounding function correctly turns the fractional coordinates into the nearest integer hex.
        return hexRound(q_frac, r_frac);
    }

    /**
     * Rounds fractional axial coordinates to the nearest integer hex coordinates.
     * @param q_frac The fractional q-coordinate.
     * @param r_frac The fractional r-coordinate.
     * @return The Hex object at the rounded coordinates.
     */
    private Hex hexRound(float q_frac, float r_frac) {
        float s_frac = -q_frac - r_frac;

        int q = Math.round(q_frac);
        int r = Math.round(r_frac);
        int s = Math.round(s_frac);

        float q_diff = Math.abs(q - q_frac);
        float r_diff = Math.abs(r - r_frac);
        float s_diff = Math.abs(s - s_frac);

        if (q_diff > r_diff && q_diff > s_diff) {
            q = -r - s;
        } else if (r_diff > s_diff) {
            r = -q - s;
        }

        return hexGrid.getHexAt(q, r);
    }
}
