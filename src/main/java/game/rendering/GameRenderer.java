package game.rendering;

import java.util.List;
import java.util.Set;

import game.Camera;
import game.GameConfig;
import game.core.UnitManager;
import game.core.VisibilityManager;
import game.model.Hex;
import game.model.HexGrid;
import game.model.Unit;
import game.model.VisibilityState;
import processing.core.PApplet;
import processing.core.PVector;

public class GameRenderer {
    private final PApplet p;
    private final Camera camera;
    private final HexGrid hexGrid;
    private final UnitManager unitManager;
    private final VisibilityManager visibilityManager;
    // The HexBoundaryCalculator is no longer needed for this simpler rendering.
    // We will keep it for the movement boundary, however.
    private final HexBoundaryCalculator hexBoundaryCalculator;

    public GameRenderer(PApplet p, Camera cam, HexGrid grid, UnitManager um, VisibilityManager vm, HexBoundaryCalculator hbc) {
        this.p = p;
        this.camera = cam;
        this.hexGrid = grid;
        this.unitManager = um;
        this.visibilityManager = vm;
        this.hexBoundaryCalculator = hbc;
    }

    /**
     * The new, simplified render loop.
     */
    public void render() {
        p.background(GameConfig.FOG_OF_WAR_COLOR); // Color for the undiscovered void.

        camera.beginTransform();

        // The order is important: Grid -> Overlays -> Units
        drawHexGrid();
        drawMovementRange();
        drawMovementBoundary();
        drawUnits();
        drawSelectionIndicator();

        camera.endTransform();
    }

    /**
     * Simplified method to draw the hex grid tile by tile, respecting visibility.
     */
    private void drawHexGrid() {
        for (Hex hex : hexGrid.getAllHexes()) {
            VisibilityState visibility = visibilityManager.getVisibilityState(hex);

            // If the hex is completely undiscovered, we simply don't draw it.
            if (visibility == VisibilityState.UNDISCOVERED) {
                continue;
            }

            p.stroke(0);
            p.strokeWeight(1.0f / camera.getZoom());

            // Set the fill color based on visibility
            if (visibility == VisibilityState.CURRENTLY_VISIBLE) {
                p.fill(hex.biome.biomeColor);
            } else { // DISCOVERED
                p.fill(getDimmedColor(hex.biome.biomeColor));
            }

            // Draw the hex shape
            drawHexShape(hex);
        }
    }

    private void drawMovementRange() {
        if (unitManager.getSelectedUnit() == null) return;
        p.noStroke();
        p.fill(GameConfig.MOVEMENT_RANGE_FILL_COLOR);
        for (Hex hex : unitManager.getReachableHexes().keySet()) {
            drawHexShape(hex);
        }
    }

    private void drawMovementBoundary() {
        if (unitManager.getSelectedUnit() == null) return;
        Set<Hex> reachable = unitManager.getReachableHexes().keySet();
        List<List<PVector>> boundaries = hexBoundaryCalculator.calculateBoundaries(reachable, hexGrid);
        p.stroke(GameConfig.MOVEMENT_BOUNDARY_COLOR);
        p.strokeWeight(2.5f / camera.getZoom());
        p.noFill();
        for (List<PVector> boundary : boundaries) {
            p.beginShape();
            for (PVector v : boundary) {
                p.vertex(v.x, v.y);
            }
            p.endShape();
        }
    }

    private void drawUnits() {
        for (Unit unit : unitManager.getUnits()) {
            Hex hex = hexGrid.getHexAt(unit.q, unit.r);
            if (hex != null && visibilityManager.getVisibilityState(hex) == VisibilityState.CURRENTLY_VISIBLE) {
                float[] pixel = hexToPixel(unit.q, unit.r);
                p.fill(unit.type.unitColor);
                p.stroke(0);
                p.strokeWeight(1.0f / camera.getZoom());
                p.textAlign(PApplet.CENTER, PApplet.CENTER);
                p.textSize(16.0f);
                p.text(unit.type.symbol, pixel[0], pixel[1]);
            }
        }
    }

    private void drawSelectionIndicator() {
        Unit selectedUnit = unitManager.getSelectedUnit();
        if (selectedUnit != null) {
            p.noStroke();
            p.fill(GameConfig.SELECTION_INDICATOR_COLOR);
            float[] pixel = hexToPixel(selectedUnit.q, selectedUnit.r);
            p.circle(pixel[0], pixel[1], GameConfig.HEX_RADIUS * 1.5f);
        }
    }

    private void drawHexShape(Hex hex) {
        float[] center = hexToPixel(hex.q, hex.r);
        p.beginShape();
        for (int i = 0; i < 6; i++) {
            float angle = PApplet.PI / 3 * i + PApplet.PI / 2;
            float x = center[0] + GameConfig.HEX_RADIUS * p.cos(angle);
            float y = center[1] + GameConfig.HEX_RADIUS * p.sin(angle);
            p.vertex(x, y);
        }
        p.endShape(PApplet.CLOSE);
    }

    private float[] hexToPixel(int q, int r) {
        float x = GameConfig.HEX_RADIUS * (PApplet.sqrt(3) * q + PApplet.sqrt(3) / 2.0f * r);
        float y = GameConfig.HEX_RADIUS * (3.0f / 2.0f * r);
        return new float[]{x, y};
    }

    private int getDimmedColor(int originalColor) {
        float r = p.red(originalColor) * GameConfig.DISCOVERED_BRIGHTNESS_MULTIPLIER;
        float g = p.green(originalColor) * GameConfig.DISCOVERED_BRIGHTNESS_MULTIPLIER;
        float b = p.blue(originalColor) * GameConfig.DISCOVERED_BRIGHTNESS_MULTIPLIER;
        return p.color(r, g, b);
    }
}
