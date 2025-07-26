package game.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import game.model.Biome;
import game.model.City;
import game.model.Civilization;
import game.model.Hex;
import game.model.HexGrid;
import game.model.Unit;
import game.model.UnitType;

/**
 * CityManager.java
 *
 * PURPOSE:
 * Manages all city-related logic including city founding, turn processing,
 * and city queries. Acts as the central controller for city operations.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Manages city lifecycle and operations
 * - Manager Pattern: Handles complex city logic separate from data classes
 * - Integration: Works with existing civilization and unit systems
 */
public class CityManager {

    private final List<City> allCities;
    private final HexGrid hexGrid;
    private final CivilizationManager civilizationManager;

    /**
     * Constructs a new CityManager.
     *
     * @param hexGrid The hex grid for the game world.
     * @param civilizationManager The civilization manager.
     */
    public CityManager(HexGrid hexGrid, CivilizationManager civilizationManager) {
        this.allCities = new ArrayList<>();
        this.hexGrid = hexGrid;
        this.civilizationManager = civilizationManager;
    }

    /**
     * Founds a new city using a settler unit.
     * The settler is consumed in the process and foreign units are displaced.
     *
     * @param settler The settler unit to found the city with.
     * @param cityName The name for the new city.
     * @return The newly founded city, or null if founding failed.
     */
    public City foundCity(Unit settler, String cityName) {
        if (settler.type != UnitType.SETTLER) {
            return null; // Only settlers can found cities
        }

        // Check if there's already a city at this location
        if (getCityAt(settler.q, settler.r) != null) {
            return null; // Cannot found city where one already exists
        }

        // Create the new city
        City newCity = new City(cityName, settler.owner, settler.q, settler.r);
        
        // Displace foreign units from the new city's territory
        displaceUnitsFromCityTerritory(newCity);
        
        // Add city to the global list and to the civilization
        allCities.add(newCity);
        settler.owner.addCity(newCity);
        
        // Remove the settler unit (it's consumed)
        settler.owner.removeUnit(settler);
        
        return newCity;
    }

    /**
     * Processes a turn for all cities belonging to a civilization.
     * This includes production advancement and population growth.
     *
     * @param civilization The civilization whose cities to process.
     */
    public void processCityTurns(Civilization civilization) {
        for (City city : civilization.getCities()) {
            processCityTurn(city);
        }
    }

    /**
     * Processes a single city's turn.
     *
     * @param city The city to process.
     */
    private void processCityTurn(City city) {
        // Simple production: cities generate 2 production per turn
        int productionPerTurn = 2;
        city.advanceProduction(productionPerTurn);
        
        // Simple food: cities generate 2 food per turn
        int foodPerTurn = 2;
        city.addFood(foodPerTurn);
    }

    /**
     * Gets the city at the specified hex coordinates.
     *
     * @param q The q-coordinate.
     * @param r The r-coordinate.
     * @return The city at that location, or null if none exists.
     */
    public City getCityAt(int q, int r) {
        for (City city : allCities) {
            if (city.getQ() == q && city.getR() == r) {
                return city;
            }
        }
        return null;
    }

    /**
     * Gets all cities in the game.
     *
     * @return A copy of the cities list.
     */
    public List<City> getAllCities() {
        return new ArrayList<>(allCities);
    }

    /**
     * Gets all cities belonging to a specific civilization.
     *
     * @param civilization The civilization.
     * @return A list of cities owned by that civilization.
     */
    public List<City> getCitiesForCivilization(Civilization civilization) {
        List<City> cities = new ArrayList<>();
        for (City city : allCities) {
            if (city.getOwner().equals(civilization)) {
                cities.add(city);
            }
        }
        return cities;
    }

    /**
     * Checks if a hex location is suitable for founding a city.
     *
     * @param q The q-coordinate.
     * @param r The r-coordinate.
     * @return true if a city can be founded there, false otherwise.
     */
    public boolean canFoundCityAt(int q, int r) {
        // Check if there's already a city at this location
        if (getCityAt(q, r) != null) {
            return false;
        }

        // Check if the hex exists
        Hex hex = hexGrid.getHexAt(q, r);
        if (hex == null) {
            return false;
        }

        // For now, allow cities on any valid hex
        // Future: could add restrictions based on terrain type
        return true;
    }

    /**
     * Gets the total number of cities in the game.
     *
     * @return The city count.
     */
    public int getCityCount() {
        return allCities.size();
    }

    // =========================================================================
    // Unit Displacement System
    // =========================================================================

    /**
     * Displaces all foreign units from the new city's territory.
     *
     * @param newCity The newly founded city.
     */
    private void displaceUnitsFromCityTerritory(City newCity) {
        // Define city territory (1-hex radius)
        Set<Hex> cityTerritory = getCityTerritory(newCity);
        
        // Find foreign units in territory
        List<Unit> foreignUnits = findForeignUnitsInTerritory(cityTerritory, newCity.getOwner());
        
        // Displace each foreign unit
        for (Unit foreignUnit : foreignUnits) {
            displaceUnit(foreignUnit, cityTerritory);
        }
    }

    /**
     * Gets the territory controlled by a city (1-hex radius).
     *
     * @param city The city.
     * @return Set of hexes in the city's territory.
     */
    private Set<Hex> getCityTerritory(City city) {
        Set<Hex> territory = new HashSet<>();
        Hex cityHex = hexGrid.getHexAt(city.getQ(), city.getR());
        
        if (cityHex != null) {
            // Add city hex
            territory.add(cityHex);
            
            // Add all neighbors (1-hex radius)
            territory.addAll(hexGrid.getNeighbors(cityHex));
        }
        
        return territory;
    }

    /**
     * Finds all foreign units within the specified territory.
     *
     * @param territory The territory to check.
     * @param foundingCiv The civilization founding the city.
     * @return List of foreign units in the territory.
     */
    private List<Unit> findForeignUnitsInTerritory(Set<Hex> territory, Civilization foundingCiv) {
        List<Unit> foreignUnits = new ArrayList<>();
        
        // Check all civilizations except the founding one
        for (Civilization civ : civilizationManager.getAllCivilizations()) {
            if (civ.equals(foundingCiv)) continue;
            
            for (Unit unit : civ.getUnits()) {
                Hex unitHex = hexGrid.getHexAt(unit.q, unit.r);
                if (territory.contains(unitHex)) {
                    foreignUnits.add(unit);
                }
            }
        }
        
        return foreignUnits;
    }

    /**
     * Displaces a unit to the nearest valid hex outside the forbidden territory.
     *
     * @param unit The unit to displace.
     * @param forbiddenTerritory The territory the unit cannot remain in.
     */
    private void displaceUnit(Unit unit, Set<Hex> forbiddenTerritory) {
        Hex currentHex = hexGrid.getHexAt(unit.q, unit.r);
        Hex targetHex = findNearestValidHex(currentHex, forbiddenTerritory);
        
        if (targetHex != null) {
            unit.setPosition(targetHex.q, targetHex.r);
            // Update visibility for the unit's civilization
            unit.owner.getVisibilityManager().updateGlobalVisibility(unit.owner.getUnits());
        }
    }

    /**
     * Finds the nearest valid hex for unit displacement using breadth-first search.
     *
     * @param startHex The starting position.
     * @param forbiddenTerritory Territory the unit cannot be placed in.
     * @return The nearest valid hex, or null if none found.
     */
    private Hex findNearestValidHex(Hex startHex, Set<Hex> forbiddenTerritory) {
        Queue<Hex> queue = new LinkedList<>();
        Set<Hex> visited = new HashSet<>();
        
        // Start BFS from the unit's current position
        queue.offer(startHex);
        visited.add(startHex);
        
        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            
            // Check if this hex is valid (not in forbidden territory)
            if (!forbiddenTerritory.contains(current) && isValidDisplacementHex(current)) {
                return current;
            }
            
            // Add neighbors to search
            for (Hex neighbor : hexGrid.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }
        
        return null; // No valid hex found (shouldn't happen in normal gameplay)
    }

    /**
     * Checks if a hex is valid for unit displacement.
     *
     * @param hex The hex to check.
     * @return true if the hex is valid for displacement.
     */
    private boolean isValidDisplacementHex(Hex hex) {
        // Check basic validity
        if (hex == null) return false;
        
        // Check if hex is passable (not deep ocean/peaks for most units)
        if (hex.biome == Biome.DEEP_OCEAN || hex.biome == Biome.PEAKS) {
            return false;
        }
        
        // Check if hex is already occupied by another unit
        if (isHexOccupiedByUnit(hex)) {
            return false;
        }
        
        // Check if hex contains a city
        if (getCityAt(hex.q, hex.r) != null) {
            return false;
        }
        
        return true;
    }

    /**
     * Checks if a hex is occupied by any unit.
     *
     * @param hex The hex to check.
     * @return true if the hex is occupied by a unit.
     */
    private boolean isHexOccupiedByUnit(Hex hex) {
        for (Civilization civ : civilizationManager.getAllCivilizations()) {
            for (Unit unit : civ.getUnits()) {
                if (unit.q == hex.q && unit.r == hex.r) {
                    return true;
                }
            }
        }
        return false;
    }
}
