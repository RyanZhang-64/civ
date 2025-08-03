package game.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * City.java
 *
 * PURPOSE:
 * A pure data class representing a city on the game map. Cities are permanent
 * settlements that belong to civilizations and can produce units and buildings.
 *
 * DESIGN PRINCIPLES:
 * - Single Responsibility: Stores city state only
 * - Data Class: Complex logic handled by CityManager
 * - Immutable Position: Cities cannot move once founded
 */
public class City {

    private static int nextId = 1;

    // Identity & Ownership
    private final int id;
    private final String name;
    private final Civilization owner;
    private final int q, r; // Hex coordinates (final - cities don't move)
    
    // Core City Stats
    private int population;
    private int foodStorage;
    private int productionProgress;
    
    // Production System
    private ProductionItem currentProduction;
    private final Queue<ProductionItem> productionQueue;
    
    // Tile Working System
    private final Set<Hex> workableTiles;
    private final Set<Hex> workedTiles;
    
    // Border Growth System
    private int cultureAccumulated = 0;        // Total culture accumulated
    private int cultureForNextBorder = 10;     // Culture needed for next expansion  
    private int expansionCount = 0;            // Number of times city has expanded
    private final Set<Hex> ownedTiles;         // Tiles actually owned by this city
    private List<List<Hex>> expansionRings;   // Pre-calculated rings for reference
    private List<Set<Hex>> unownedTilesPerRing; // Unowned tiles per ring for expansion
    private int currentRing = 1;               // Currently expanding ring (0 = initial 7 tiles)
    private boolean needsRingRecalculation = false; // Flag for CityManager to recalculate rings
    private static final int MAX_EXPANSION_RINGS = 5; // Maximum rings to pre-calculate

    /**
     * Constructs a new City.
     *
     * @param name The name of the city.
     * @param owner The civilization that owns this city.
     * @param q The q-coordinate of the city.
     * @param r The r-coordinate of the city.
     */
    public City(String name, Civilization owner, int q, int r) {
        this.id = nextId++;
        this.name = name;
        this.owner = owner;
        this.q = q;
        this.r = r;
        
        // Initialize with starting values
        this.population = 1;
        this.foodStorage = 0;
        this.productionProgress = 0;
        this.currentProduction = null;
        this.productionQueue = new LinkedList<>();
        
        // Initialize tile working system
        this.workableTiles = new HashSet<>();
        this.workedTiles = new HashSet<>();
        
        // Initialize border growth system
        this.ownedTiles = new HashSet<>();
        this.expansionRings = new ArrayList<>();
        this.unownedTilesPerRing = new ArrayList<>();
    }

    /**
     * Adds a production item to the city's production queue.
     *
     * @param item The item to produce.
     */
    public void addToProductionQueue(ProductionItem item) {
        if (currentProduction == null) {
            currentProduction = item;
        } else {
            productionQueue.offer(item);
        }
    }

    /**
     * Advances production by the given amount and handles completion.
     *
     * @param productionPoints The production points to add.
     * @return true if an item was completed, false otherwise.
     */
    public boolean advanceProduction(int productionPoints) {
        if (currentProduction == null) return false;
        
        productionProgress += productionPoints;
        
        if (productionProgress >= currentProduction.getProductionCost()) {
            // Complete the current production
            currentProduction.onComplete(this);
            productionProgress = 0;
            
            // Move to next item in queue
            currentProduction = productionQueue.poll();
            return true;
        }
        return false;
    }

    /**
     * Adds food to the city's storage and handles population growth.
     *
     * @param food The food points to add.
     * @return true if population grew, false otherwise.
     */
    public boolean addFood(int food) {
        foodStorage += food;
        int foodNeeded = population * 2; // Simple growth formula
        
        if (foodStorage >= foodNeeded) {
            foodStorage -= foodNeeded;
            population++;
            return true;
        }
        return false;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public Civilization getOwner() { return owner; }
    public int getQ() { return q; }
    public int getR() { return r; }
    public int getPopulation() { return population; }
    public int getFoodStorage() { return foodStorage; }
    public int getProductionProgress() { return productionProgress; }
    public ProductionItem getCurrentProduction() { return currentProduction; }
    public Queue<ProductionItem> getProductionQueue() { return productionQueue; }

    /**
     * Gets the city's gold per turn.
     * Placeholder implementation until economic system is added.
     *
     * @return Gold generated per turn
     */
    public int getGold() {
        // Placeholder: Simple formula based on population
        return Math.max(1, population / 2);
    }

    /**
     * Gets the culture production per turn for this city.
     * This represents how much culture the city generates each turn.
     *
     * @return Culture generated per turn
     */
    public int getCulture() {
        // Base culture generation - will be enhanced with buildings/bonuses later
        return 1 + (population / 5); // 1 base + 1 per 5 population
    }

    /**
     * Gets the turns until next border expansion.
     * Calculates based on current culture accumulation and culture per turn.
     *
     * @return Turns until borders expand
     */
    public int getTurnsToNextBorderGrowth() {
        if (!canExpandBorder()) {
            return -1; // Cannot expand
        }
        
        int cultureNeeded = cultureForNextBorder - cultureAccumulated;
        int culturePerTurn = getCulture();
        
        if (culturePerTurn <= 0) {
            return -1; // No culture generation
        }
        
        return (int) Math.ceil((double) cultureNeeded / culturePerTurn);
    }

    // --- Tile Working System ---
    
    /**
     * Gets the set of tiles that can be worked by this city.
     * 
     * @return The workable tiles within this city's radius
     */
    public Set<Hex> getWorkableTiles() {
        return new HashSet<>(workableTiles);
    }
    
    /**
     * Gets the set of tiles currently being worked by this city.
     * 
     * @return The tiles currently being worked
     */
    public Set<Hex> getWorkedTiles() {
        return new HashSet<>(workedTiles);
    }
    
    /**
     * Sets the tiles that can be worked by this city.
     * Called during city initialization to define working radius.
     * 
     * @param tiles The tiles within working radius
     */
    public void setWorkableTiles(Set<Hex> tiles) {
        workableTiles.clear();
        workableTiles.addAll(tiles);
    }
    
    /**
     * Sets which tiles are currently being worked by citizens.
     * 
     * @param tiles The tiles to assign citizens to work
     */
    public void setWorkedTiles(Set<Hex> tiles) {
        workedTiles.clear();
        workedTiles.addAll(tiles);
    }
    
    /**
     * Calculates total food per turn from city center and worked tiles.
     * Replaces the hardcoded food value with tile-based calculation.
     * 
     * @return Total food generated per turn
     */
    public int calculateFoodPerTurn() {
        int totalFood = 2; // City center base yield
        
        for (Hex workedTile : workedTiles) {
            if (workedTile.biome.isWorkable()) {
                totalFood += workedTile.biome.foodYield;
            }
        }
        
        return totalFood;
    }
    
    /**
     * Calculates total production per turn from city center and worked tiles.
     * Replaces the hardcoded production value with tile-based calculation.
     * 
     * @return Total production generated per turn
     */
    public int calculateProductionPerTurn() {
        int totalProduction = 1; // City center base yield
        
        for (Hex workedTile : workedTiles) {
            if (workedTile.biome.isWorkable()) {
                totalProduction += workedTile.biome.productionYield;
            }
        }
        
        return totalProduction;
    }
    
    /**
     * Gets debug information about tile working status.
     * 
     * @return String with tile working information
     */
    public String getTileWorkingDebugInfo() {
        return String.format("Workable: %d, Worked: %d, Food: %d, Production: %d", 
                           workableTiles.size(), 
                           workedTiles.size(),
                           calculateFoodPerTurn(),
                           calculateProductionPerTurn());
    }

    // --- Setters (for manager classes) ---
    public void setPopulation(int population) { this.population = population; }
    public void setFoodStorage(int foodStorage) { this.foodStorage = foodStorage; }
    public void setProductionProgress(int progress) { this.productionProgress = progress; }
    public void setCurrentProduction(ProductionItem item) { this.currentProduction = item; }

    @Override
    public String toString() {
        return "City[" + name + " at " + q + "," + r + " pop:" + population + "]";
    }

    /**
     * Assigns a citizen to work a specific tile.
     * The tile must be within the city's workable range.
     *
     * @param tile The hex tile to assign a citizen to
     */
    public void assignCitizenToTile(Hex tile) {
        if (workableTiles.contains(tile) && !workedTiles.contains(tile)) {
            workedTiles.add(tile);
        }
    }

    /**
     * Removes a citizen from working a specific tile.
     *
     * @param tile The hex tile to remove a citizen from
     */
    public void removeCitizenFromTile(Hex tile) {
        workedTiles.remove(tile);
    }

    // Border Growth System Methods

    /**
     * Adds culture to the city and triggers border expansion if threshold is reached.
     *
     * @param culture The amount of culture to add
     */
    public void addCulture(int culture) {
        cultureAccumulated += culture;
        
        // Check if we can expand (expansion logic will be implemented in Phase 3)
        while (cultureAccumulated >= cultureForNextBorder && canExpandBorder()) {
            expandBorder();
            expansionCount++;
            
            // Increase threshold for next expansion (scaling cost)
            cultureForNextBorder = 10 + (expansionCount * 5); // 10, 15, 20, 25...
        }
    }

    /**
     * Checks if the city can expand its borders.
     *
     * @return true if expansion is possible
     */
    public boolean canExpandBorder() {
        return currentRing <= unownedTilesPerRing.size() && 
               currentRing <= MAX_EXPANSION_RINGS &&
               hasUnownedTilesInCurrentRing();
    }

    /**
     * Checks if the current ring has any unowned tiles.
     *
     * @return true if current ring has unowned tiles
     */
    private boolean hasUnownedTilesInCurrentRing() {
        if (currentRing <= unownedTilesPerRing.size()) {
            Set<Hex> currentRingTiles = unownedTilesPerRing.get(currentRing - 1);
            return currentRingTiles != null && !currentRingTiles.isEmpty();
        }
        return false;
    }

    /**
     * Gets the next tile queued for expansion from the current ring.
     *
     * @return The next hex tile to expand into, or null if none available
     */
    public Hex getNextExpansionTile() {
        if (hasUnownedTilesInCurrentRing()) {
            Set<Hex> currentRingTiles = unownedTilesPerRing.get(currentRing - 1);
            // Return any tile from the current ring (no prioritization needed)
            return currentRingTiles.iterator().next();
        }
        return null;
    }

    /**
     * Executes border expansion by adding the next tile from current ring.
     * Completes entire rings before advancing to the next ring.
     */
    private void expandBorder() {
        // Check if current ring has unowned tiles
        if (!hasUnownedTilesInCurrentRing()) {
            // Current ring is complete, try to advance to next ring
            if (currentRing < unownedTilesPerRing.size()) {
                currentRing++;
                System.out.println("City " + name + " advanced to ring " + currentRing);
            }
            return;
        }
        
        // Get next tile from current ring
        Hex nextTile = getNextExpansionTile();
        if (nextTile == null) {
            return; // No tile available for expansion
        }
        
        // Add the tile to owned territory
        ownedTiles.add(nextTile);
        
        // Add to workable tiles so citizens can work it
        workableTiles.add(nextTile);
        
        // Remove the tile from unowned tiles in current ring
        Set<Hex> currentRingTiles = unownedTilesPerRing.get(currentRing - 1);
        currentRingTiles.remove(nextTile);
        
        System.out.println("City " + name + " expanded into new tile at ring " + currentRing + 
                          " (" + currentRingTiles.size() + " tiles remaining in ring)");
        
        // Check if current ring is now complete
        if (currentRingTiles.isEmpty()) {
            System.out.println("City " + name + " completed ring " + currentRing);
            currentRing++; // Advance to next ring
            
            // Only trigger recalculation when advancing to next ring
            needsRingRecalculation = true;
        }
    }

    /**
     * Initializes the border growth system for a newly founded city.
     * This should be called from CityManager after the city's initial territory is set.
     * The actual ring calculation and queue selection is done by CityManager.
     */
    public void initializeBorderGrowthSystem() {
        // This method is now just a placeholder
        // The actual initialization is handled by CityManager.initializeCityBorderGrowth()
        // which calls setExpansionRings() and setExpansionQueue()
    }

    // Border Growth Getters

    /**
     * Gets the amount of culture accumulated towards next border expansion.
     *
     * @return The accumulated culture
     */
    public int getCultureAccumulated() {
        return cultureAccumulated;
    }

    /**
     * Gets the culture threshold needed for next border expansion.
     *
     * @return The culture threshold
     */
    public int getCultureForNextBorder() {
        return cultureForNextBorder;
    }

    /**
     * Gets the number of times this city has expanded its borders.
     *
     * @return The expansion count
     */
    public int getExpansionCount() {
        return expansionCount;
    }

    /**
     * Gets the tiles actually owned by this city.
     *
     * @return Set of owned hex tiles
     */
    public Set<Hex> getOwnedTiles() {
        return new HashSet<>(ownedTiles);
    }

    /**
     * Sets the tiles owned by this city.
     *
     * @param tiles The set of tiles to be owned by this city
     */
    public void setOwnedTiles(Set<Hex> tiles) {
        ownedTiles.clear();
        ownedTiles.addAll(tiles);
    }

    /**
     * Gets the current expansion ring being worked on.
     *
     * @return The current ring number
     */
    public int getCurrentRing() {
        return currentRing;
    }

    /**
     * Sets the expansion rings for this city.
     *
     * @param rings The list of expansion rings
     */
    public void setExpansionRings(List<List<Hex>> rings) {
        this.expansionRings = new ArrayList<>();
        for (List<Hex> ring : rings) {
            this.expansionRings.add(new ArrayList<>(ring));
        }
    }

    /**
     * Sets the expansion queue for this city.
     *
     * @param unownedRings The list of unowned tiles per ring
     */
    public void setUnownedTilesPerRing(List<Set<Hex>> unownedRings) {
        this.unownedTilesPerRing = new ArrayList<>();
        for (Set<Hex> ring : unownedRings) {
            this.unownedTilesPerRing.add(new HashSet<>(ring));
        }
    }

    /**
     * Gets the expansion rings for this city.
     *
     * @return List of expansion rings
     */
    public List<List<Hex>> getExpansionRings() {
        List<List<Hex>> result = new ArrayList<>();
        for (List<Hex> ring : expansionRings) {
            result.add(new ArrayList<>(ring));
        }
        return result;
    }

    /**
     * Gets the unowned tiles per ring for this city.
     *
     * @return List of unowned tile sets per ring
     */
    public List<Set<Hex>> getUnownedTilesPerRing() {
        List<Set<Hex>> result = new ArrayList<>();
        for (Set<Hex> ring : unownedTilesPerRing) {
            result.add(new HashSet<>(ring));
        }
        return result;
    }

    /**
     * Gets the number of expansion rings calculated for this city.
     *
     * @return Number of expansion rings
     */
    public int getExpansionRingCount() {
        return expansionRings.size();
    }

    /**
     * Checks if this city needs ring recalculation due to border expansion.
     *
     * @return true if rings need recalculation
     */
    public boolean needsRingRecalculation() {
        return needsRingRecalculation;
    }

    /**
     * Resets the ring recalculation flag.
     * This should be called by CityManager after recalculating rings.
     */
    public void clearRingRecalculationFlag() {
        needsRingRecalculation = false;
    }
}
