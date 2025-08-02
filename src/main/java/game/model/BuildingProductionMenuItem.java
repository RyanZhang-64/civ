package game.model;

/**
 * BuildingProductionMenuItem.java
 *
 * PURPOSE:
 * Concrete implementation of ProductionMenuItem for building production.
 * Handles city improvements like granaries, libraries, etc.
 *
 * DESIGN PRINCIPLES:
 * - Future-Proof: Designed to work with a building system when implemented
 * - Consistent: Follows same pattern as UnitProductionMenuItem
 * - Extensible: Easy to add new building types and requirements
 */
public class BuildingProductionMenuItem implements ProductionMenuItem {

    private final String name;
    private final int productionCost;
    private final String description;
    private final String tooltipText;

    /**
     * Constructs a new BuildingProductionMenuItem.
     *
     * @param name The building name
     * @param productionCost The cost to build
     * @param description Short description
     * @param tooltipText Detailed tooltip information
     */
    public BuildingProductionMenuItem(String name, int productionCost, String description, String tooltipText) {
        this.name = name;
        this.productionCost = productionCost;
        this.description = description;
        this.tooltipText = tooltipText;
    }

    @Override
    public ProductionCategory getCategory() {
        return ProductionCategory.BUILDINGS;
    }

    @Override
    public boolean canProduce(City city) {
        // Future: Check if city already has this building, meets prerequisites, etc.
        return true;
    }

    @Override
    public String getUnavailableReason(City city) {
        if (canProduce(city)) {
            return null;
        }
        return "Building requirements not met";
    }

    @Override
    public int getTurnsToComplete(City city) {
        if (!canProduce(city)) {
            return -1;
        }
        
        int productionPerTurn = city.getPopulation();
        if (productionPerTurn <= 0) {
            return -1;
        }
        
        return (int) Math.ceil((double) productionCost / productionPerTurn);
    }

    @Override
    public String getTooltipText() {
        return tooltipText;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getProductionCost() {
        return productionCost;
    }

    @Override
    public void onComplete(City city) {
        // Future: Add building to city, apply effects
        System.out.println("Built " + name + " in " + city.getName());
    }

    @Override
    public String getDescription() {
        return description;
    }
}