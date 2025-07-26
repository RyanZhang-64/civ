package game.model;

import java.util.LinkedList;
import java.util.Queue;

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

    // --- Setters (for manager classes) ---
    public void setPopulation(int population) { this.population = population; }
    public void setFoodStorage(int foodStorage) { this.foodStorage = foodStorage; }
    public void setProductionProgress(int progress) { this.productionProgress = progress; }
    public void setCurrentProduction(ProductionItem item) { this.currentProduction = item; }

    @Override
    public String toString() {
        return "City[" + name + " at " + q + "," + r + " pop:" + population + "]";
    }
}
