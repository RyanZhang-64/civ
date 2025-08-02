package game.model;

/**
 * WonderProductionMenuItem.java
 *
 * PURPOSE:
 * Concrete implementation of ProductionMenuItem for world wonder production.
 * Handles unique buildings with special effects and global limitations.
 *
 * DESIGN PRINCIPLES:
 * - Uniqueness: Wonders can only be built once per game
 * - High Value: Generally expensive but provide significant benefits
 * - Special Effects: Framework for unique wonder abilities
 */
public class WonderProductionMenuItem implements ProductionMenuItem {

    private final String name;
    private final int productionCost;
    private final String description;
    private final String tooltipText;
    private final boolean isBuilt; // Future: Track global wonder state

    /**
     * Constructs a new WonderProductionMenuItem.
     *
     * @param name The wonder name
     * @param productionCost The cost to build
     * @param description Short description
     * @param tooltipText Detailed tooltip information
     */
    public WonderProductionMenuItem(String name, int productionCost, String description, String tooltipText) {
        this.name = name;
        this.productionCost = productionCost;
        this.description = description;
        this.tooltipText = tooltipText;
        this.isBuilt = false; // Future: Check global game state
    }

    @Override
    public ProductionCategory getCategory() {
        return ProductionCategory.WONDERS;
    }

    @Override
    public boolean canProduce(City city) {
        // Future: Check if wonder already built globally, technology requirements, etc.
        return !isBuilt;
    }

    @Override
    public String getUnavailableReason(City city) {
        if (isBuilt) {
            return "Wonder already built elsewhere";
        }
        if (canProduce(city)) {
            return null;
        }
        return "Wonder requirements not met";
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
        return tooltipText + (isBuilt ? "\n[ALREADY BUILT]" : "");
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
        // Future: Add wonder to city, apply global effects, mark as built
        System.out.println("Built wonder " + name + " in " + city.getName());
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this wonder has been built anywhere in the world.
     *
     * @return true if the wonder exists, false otherwise
     */
    public boolean isBuiltGlobally() {
        return isBuilt;
    }
}