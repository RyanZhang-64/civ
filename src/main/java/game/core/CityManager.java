package game.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import game.events.CityFoundedEvent;
import game.model.City;
import game.model.Civilization;
import game.model.Hex;
import game.model.Unit;
import game.model.UnitType;
import game.model.Biome;
import game.model.HexGrid;

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
    private GameObjectPoolManager pools;

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
        
        // Initialize city with starting territory (7 tiles: city center + 6 neighbors)
        Set<Hex> initialTerritory = getInitialCityTerritory(newCity);
        newCity.setOwnedTiles(initialTerritory);
        newCity.setWorkableTiles(new HashSet<>(initialTerritory));
        
        // Initialize border growth system
        initializeCityBorderGrowth(newCity);
        
        // Automatically assign the best tiles to the new city
        assignBestTilesToCity(newCity);
        
        // Displace foreign units from the new city's territory
        displaceUnitsFromCityTerritory(newCity);
        
        // Add city to the global list and to the civilization
        allCities.add(newCity);
        settler.owner.addCity(newCity);
        
        // Fire city founded event using pooled object if available
        if (pools != null) {
            CityFoundedEvent event = pools.getCityFoundedEvent(newCity);
            // Event would be processed by GameEventManager here
            pools.returnCityFoundedEvent(event);
        }
        
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
        // Use tile-based yields for production and food
        int productionPerTurn = city.calculateProductionPerTurn();
        city.advanceProduction(productionPerTurn);
        
        int foodPerTurn = city.calculateFoodPerTurn();
        city.addFood(foodPerTurn);
        
        // Process culture accumulation and border expansion
        int culturePerTurn = city.getCulture();
        city.addCulture(culturePerTurn);
        
        // Check if city needs ring recalculation after border expansion
        if (city.needsRingRecalculation()) {
            recalculateExpansionRings(city);
            city.clearRingRecalculationFlag();
        }
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
     * Gets the territory controlled by a city.
     * Uses the city's actual owned tiles for dynamic border growth.
     *
     * @param city The city to get territory for.
     * @return Set of hexes in the city's territory.
     */
    private Set<Hex> getCityTerritory(City city) {
        // Return the city's actual owned tiles instead of fixed 1-hex radius
        return city.getOwnedTiles();
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

    /**
     * Automatically assigns the best tiles to a newly founded city.
     * Citizens are assigned to tiles with the highest combined yields.
     *
     * @param city The city to assign tiles to.
     */
    private void assignBestTilesToCity(City city) {
        // Get all workable tiles in city territory
        Set<Hex> workableTiles = city.getWorkableTiles();
        
        // Sort tiles by total yield (food + production)
        List<Hex> sortedTiles = workableTiles.stream()
            .filter(hex -> hex.biome.isWorkable())
            .sorted((hex1, hex2) -> {
                int yield1 = hex1.biome.getTotalYield();
                int yield2 = hex2.biome.getTotalYield();
                return Integer.compare(yield2, yield1); // Descending order
            })
            .collect(java.util.stream.Collectors.toList());
        
        // Assign the best tiles up to the city's population
        int citizensToAssign = Math.min(city.getPopulation(), sortedTiles.size());
        for (int i = 0; i < citizensToAssign; i++) {
            city.assignCitizenToTile(sortedTiles.get(i));
        }
    }

    /**
     * Calculates the hexagonal distance between two hex tiles.
     * Uses the standard hex distance formula: (|q1-q2| + |q1+r1-q2-r2| + |r1-r2|) / 2
     *
     * @param hex1 First hex tile
     * @param hex2 Second hex tile  
     * @return The distance between the hexes
     */
    private int hexDistance(Hex hex1, Hex hex2) {
        return (Math.abs(hex1.q - hex2.q) + 
                Math.abs(hex1.q + hex1.r - hex2.q - hex2.r) + 
                Math.abs(hex1.r - hex2.r)) / 2;
    }

    /**
     * Calculates unowned tiles organized by distance rings from city center.
     * Each ring contains only unowned, expandable hexes at exactly that distance.
     * This creates proper concentric hexagonal rings for expansion.
     * Note: Cities start with distance 0 (center) + distance 1 (neighbors) for free,
     * so expansion begins at distance 2.
     *
     * @param city The city to calculate rings for
     * @return List of sets, where each set contains unowned tiles at that distance
     */
    public List<Set<Hex>> calculateUnownedTilesPerRing(City city) {
        List<Set<Hex>> unownedRings = new ArrayList<>();
        Hex cityCenter = hexGrid.getHexAt(city.getQ(), city.getR());
        
        if (cityCenter == null) {
            System.out.println("DEBUG: Cannot find city center hex for " + city.getName());
            return unownedRings;
        }
        
        Set<Hex> ownedTiles = city.getOwnedTiles();
        System.out.println("DEBUG: City " + city.getName() + " calculating rings. Owned tiles: " + ownedTiles.size());
        
        // Check each distance ring starting from 2 (cities get 0+1 for free)
        for (int distance = 2; distance <= 6; distance++) {
            Set<Hex> ringTiles = new HashSet<>();
            
            // Search in a square around the city center
            int searchRadius = distance + 1;
            int candidatesChecked = 0;
            int validCandidates = 0;
            int alreadyOwned = 0;
            int ownedByOtherCity = 0;
            int invalidTerrain = 0;
            
            for (int q = cityCenter.q - searchRadius; q <= cityCenter.q + searchRadius; q++) {
                for (int r = cityCenter.r - searchRadius; r <= cityCenter.r + searchRadius; r++) {
                    Hex candidate = hexGrid.getHexAt(q, r);
                    candidatesChecked++;
                    
                    if (candidate != null && hexDistance(cityCenter, candidate) == distance) {
                        validCandidates++;
                        
                        // Check why each tile is rejected
                        if (ownedTiles.contains(candidate)) {
                            alreadyOwned++;
                            System.out.println("DEBUG: Tile (" + candidate.q + "," + candidate.r + 
                                             ") already owned by " + city.getName());
                        } else if (candidate.biome == Biome.PEAKS || candidate.biome == Biome.DEEP_OCEAN) {
                            invalidTerrain++;
                            System.out.println("DEBUG: Tile (" + candidate.q + "," + candidate.r + 
                                             ") has invalid terrain: " + candidate.biome);
                        } else if (isTileOwnedByAnyCity(candidate)) {
                            ownedByOtherCity++;
                            // isTileOwnedByAnyCity already prints debug info
                        } else {
                            ringTiles.add(candidate);
                            System.out.println("DEBUG: Tile (" + candidate.q + "," + candidate.r + 
                                             ") added to ring " + (distance-1) + " for " + city.getName());
                        }
                    }
                }
            }
            
            System.out.println("DEBUG: City " + city.getName() + " ring " + (distance-1) + 
                              " (distance " + distance + ") - checked " + candidatesChecked + " candidates, " + 
                              validCandidates + " at correct distance, " + 
                              alreadyOwned + " already owned, " +
                              ownedByOtherCity + " owned by other city, " +
                              invalidTerrain + " invalid terrain, " +
                              ringTiles.size() + " available for expansion");
            
            if (ringTiles.isEmpty()) {
                System.out.println("DEBUG: City " + city.getName() + " no more expandable tiles at distance " + distance);
                break; // No more expandable tiles at this distance
            }
            
            unownedRings.add(ringTiles);
        }
        
        System.out.println("DEBUG: City " + city.getName() + " total rings calculated: " + unownedRings.size());
        return unownedRings;
    }

    /**
     * Checks if a tile is already owned by any city.
     *
     * @param hex The hex to check
     * @return true if the tile is owned by a city
     */
    private boolean isTileOwnedByAnyCity(Hex hex) {
        for (City city : allCities) {
            if (city.getOwnedTiles().contains(hex)) {
                System.out.println("DEBUG: Tile (" + hex.q + "," + hex.r + ") already owned by " + city.getName());
                return true;
            }
        }
        return false;
    }

    /**
     * Initializes the border growth system for a newly founded city.
     *
     * @param city The city to initialize
     */
    public void initializeCityBorderGrowth(City city) {
        // Calculate unowned tiles per ring
        List<Set<Hex>> unownedRings = calculateUnownedTilesPerRing(city);
        
        // Calculate expansion rings for reference (keep existing structure)
        List<List<Hex>> rings = new ArrayList<>();
        for (Set<Hex> ringSet : unownedRings) {
            rings.add(new ArrayList<>(ringSet));
        }
        
        // Pass the data to the city
        city.setExpansionRings(rings);
        city.setUnownedTilesPerRing(unownedRings);
    }

    /**
     * Gets the initial 7-tile territory for a newly founded city.
     *
     * @param city The newly founded city
     * @return Set of hexes forming the initial territory
     */
    private Set<Hex> getInitialCityTerritory(City city) {
        Set<Hex> territory = new HashSet<>();
        
        // Add the city center
        Hex cityHex = hexGrid.getHexAt(city.getQ(), city.getR());
        if (cityHex != null) {
            territory.add(cityHex);
            
            // Add the 6 neighboring tiles
            territory.addAll(hexGrid.getNeighbors(cityHex));
        }
        
        return territory;
    }

    /**
     * Recalculates expansion rings for a city after border expansion.
     * This should be called whenever a city's territory changes.
     *
     * @param city The city to recalculate rings for
     */
    public void recalculateExpansionRings(City city) {
        // Calculate new unowned tiles per ring based on current owned tiles
        List<Set<Hex>> newUnownedRings = calculateUnownedTilesPerRing(city);
        
        // Calculate expansion rings for reference (keep existing structure)
        List<List<Hex>> newRings = new ArrayList<>();
        for (Set<Hex> ringSet : newUnownedRings) {
            newRings.add(new ArrayList<>(ringSet));
        }
        
        // Update the city's expansion data
        city.setExpansionRings(newRings);
        city.setUnownedTilesPerRing(newUnownedRings);
    }
    
    /**
     * Sets the pool manager for optimized object allocation.
     */
    public void setPools(GameObjectPoolManager pools) {
        this.pools = pools;
    }
}
