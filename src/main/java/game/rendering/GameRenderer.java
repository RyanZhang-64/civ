package game.rendering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import game.Camera;
import game.GameConfig;
import game.core.CivilizationManager;
import game.core.CityManager;
import game.core.UnitManager;
import game.core.VisibilityManager;
import game.model.City;
import game.model.Civilization;
import game.model.Hex;
import game.model.HexGrid;
import game.model.Unit;
import game.model.VisibilityState;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Enum for city visibility states from the perspective of the current civilization.
 */
enum CityVisibility {
    OWN_CITY,           // Player owns this city - fully visible
    DISCOVERED_FOREIGN, // Foreign city that has been discovered - partially visible
    UNDISCOVERED        // Foreign city never seen - not visible
}

public class GameRenderer {
    private final PApplet p;
    private final Camera camera;
    private final HexGrid hexGrid;
    private final UnitManager unitManager;
    private final CivilizationManager civilizationManager;
    private final CityManager cityManager;
    // The HexBoundaryCalculator is no longer needed for this simpler rendering.
    // We will keep it for the movement boundary, however.
    private final HexBoundaryCalculator hexBoundaryCalculator;

    public GameRenderer(PApplet p, Camera cam, HexGrid grid, UnitManager um, CivilizationManager civManager, CityManager cityManager, HexBoundaryCalculator hbc) {
        this.p = p;
        this.camera = cam;
        this.hexGrid = grid;
        this.unitManager = um;
        this.civilizationManager = civManager;
        this.cityManager = cityManager;
        this.hexBoundaryCalculator = hbc;
    }

    /**
     * The new, simplified render loop.
     */
    public void render() {
        p.background(GameConfig.FOG_OF_WAR_COLOR); // Color for the undiscovered void.

        camera.beginTransform();

        // The order is important: Grid -> City Territories -> Overlays -> Cities -> City Boundaries -> Units
        drawHexGrid();
        drawCityTerritories();
        drawMovementRange();
        drawMovementBoundary();
        drawCityBoundaries();
        drawCities();
        drawUnits();
        drawSelectionIndicator();

        camera.endTransform();
    }

    /**
     * Simplified method to draw the hex grid tile by tile, respecting visibility.
     */
    private void drawHexGrid() {
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) return;
        
        VisibilityManager currentVisibility = currentCiv.getVisibilityManager();
        
        for (Hex hex : hexGrid.getAllHexes()) {
            VisibilityState visibility = currentVisibility.getVisibilityState(hex);

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
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) return;
        
        VisibilityManager currentVisibility = currentCiv.getVisibilityManager();
        
        // Draw all units from all civilizations, but only if they're visible to current player
        for (Civilization civ : civilizationManager.getAllCivilizations()) {
            for (Unit unit : civ.getUnits()) {
                Hex hex = hexGrid.getHexAt(unit.q, unit.r);
                if (hex != null && currentVisibility.getVisibilityState(hex) == VisibilityState.CURRENTLY_VISIBLE) {
                    float[] pixel = hexToPixel(unit.q, unit.r);
                    // Use the unit's civilization color, not the unit type color
                    p.fill(unit.owner.getPrimaryColor());
                    p.stroke(0);
                    p.strokeWeight(1.0f / camera.getZoom());
                    p.textAlign(PApplet.CENTER, PApplet.CENTER);
                    p.textSize(16.0f);
                    p.text(unit.type.symbol, pixel[0], pixel[1]);
                }
            }
        }
    }

    private void drawCities() {
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) return;
        
        // Draw all cities based on their visibility state
        for (City city : cityManager.getAllCities()) {
            CityVisibility visibility = getCityVisibility(city, currentCiv);
            
            if (visibility != CityVisibility.UNDISCOVERED) {
                drawCityMarker(city, visibility);
            }
        }
    }

    /**
     * Draws a city marker with appropriate detail level based on visibility.
     */
    private void drawCityMarker(City city, CityVisibility visibility) {
        float[] pixel = hexToPixel(city.getQ(), city.getR());
        
        // Draw city background circle
        p.fill(city.getOwner().getPrimaryColor());
        p.stroke(0);
        p.strokeWeight(2.0f / camera.getZoom());
        p.circle(pixel[0], pixel[1], GameConfig.HEX_RADIUS * 1.2f);
        
        // Draw city name
        p.fill(255); // White text
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.textSize(12.0f);
        p.text(city.getName(), pixel[0], pixel[1] - GameConfig.HEX_RADIUS * 0.8f);
        
        // Draw population (show for both own and discovered foreign cities)
        p.textSize(10.0f);
        if (visibility == CityVisibility.OWN_CITY) {
            // Own city - show full information
            p.text("Pop: " + city.getPopulation(), pixel[0], pixel[1] + GameConfig.HEX_RADIUS * 0.8f);
        } else {
            // Foreign discovered city - show basic information
            p.text("Pop: " + city.getPopulation(), pixel[0], pixel[1] + GameConfig.HEX_RADIUS * 0.8f);
        }
    }

    private void drawCityTerritories() {
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) return;
        
        // Draw territory tiles for all discovered cities
        for (City city : cityManager.getAllCities()) {
            CityVisibility visibility = getCityVisibility(city, currentCiv);
            
            if (visibility != CityVisibility.UNDISCOVERED) {
                drawCityTerritoryTiles(city, visibility);
            }
        }
    }

    private void drawCityBoundaries() {
        Civilization currentCiv = civilizationManager.getCurrentCivilization();
        if (currentCiv == null) return;
        
        // Draw boundaries for all discovered cities
        for (City city : cityManager.getAllCities()) {
            CityVisibility visibility = getCityVisibility(city, currentCiv);
            
            if (visibility != CityVisibility.UNDISCOVERED) {
                drawCityBoundary(city);
            }
        }
    }
    
    private void drawCityTerritory(City city) {
        // Draw a simple 1-hex radius territory around the city
        List<Hex> territoryHexes = new ArrayList<>();
        
        // Add the city hex itself
        Hex cityHex = hexGrid.getHexAt(city.getQ(), city.getR());
        if (cityHex != null) {
            territoryHexes.add(cityHex);
        }
        
        // Add all neighboring hexes (1-hex radius)
        for (Hex neighbor : hexGrid.getNeighbors(cityHex)) {
            territoryHexes.add(neighbor);
        }
        
        // Draw territory boundary using the existing boundary calculator
        Set<Hex> territorySet = new HashSet<>(territoryHexes);
        List<List<PVector>> boundaries = hexBoundaryCalculator.calculateBoundaries(territorySet, hexGrid);
        
        // Draw the boundary lines
        p.stroke(city.getOwner().getPrimaryColor());
        p.strokeWeight(3.0f / camera.getZoom());
        p.noFill();
        
        for (List<PVector> boundary : boundaries) {
            p.beginShape();
            for (PVector v : boundary) {
                p.vertex(v.x, v.y);
            }
            p.endShape();
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

    // =========================================================================
    // City Visibility System
    // =========================================================================

    /**
     * Determines the visibility state of a city from the perspective of the observer civilization.
     */
    private CityVisibility getCityVisibility(City city, Civilization observer) {
        // Own city - always fully visible
        if (city.getOwner().equals(observer)) {
            return CityVisibility.OWN_CITY;
        }
        
        // Foreign city - check if discovered
        Hex cityHex = hexGrid.getHexAt(city.getQ(), city.getR());
        VisibilityState hexVisibility = observer.getVisibilityManager().getVisibilityState(cityHex);
        
        if (hexVisibility != VisibilityState.UNDISCOVERED) {
            return CityVisibility.DISCOVERED_FOREIGN;
        }
        
        return CityVisibility.UNDISCOVERED;
    }

    /**
     * Draws territory tiles for a city based on its visibility state.
     */
    private void drawCityTerritoryTiles(City city, CityVisibility visibility) {
        List<Hex> territoryHexes = getCityTerritoryHexes(city);
        
        for (Hex hex : territoryHexes) {
            p.stroke(0);
            p.strokeWeight(1.0f / camera.getZoom());
            
            if (visibility == CityVisibility.OWN_CITY) {
                // Own city territory - fully visible with bright colors
                p.fill(hex.biome.biomeColor);
            } else {
                // Foreign discovered city - dimmed colors
                p.fill(getDimmedColor(hex.biome.biomeColor));
            }
            
            drawHexShape(hex);
        }
    }

    /**
     * Draws the boundary line around a city's territory.
     */
    private void drawCityBoundary(City city) {
        List<Hex> territoryHexes = getCityTerritoryHexes(city);
        Set<Hex> territorySet = new HashSet<>(territoryHexes);
        List<List<PVector>> boundaries = hexBoundaryCalculator.calculateBoundaries(territorySet, hexGrid);
        
        // Draw the boundary lines
        p.stroke(city.getOwner().getPrimaryColor());
        p.strokeWeight(3.0f / camera.getZoom());
        p.noFill();
        
        for (List<PVector> boundary : boundaries) {
            p.beginShape();
            for (PVector v : boundary) {
                p.vertex(v.x, v.y);
            }
            p.endShape();
        }
    }

    /**
     * Gets the list of hexes that make up a city's territory.
     * Uses the city's actual owned tiles for dynamic border expansion.
     */
    private List<Hex> getCityTerritoryHexes(City city) {
        // Use the city's actual owned tiles instead of hardcoded 1-hex radius
        return new ArrayList<>(city.getOwnedTiles());
    }
}
