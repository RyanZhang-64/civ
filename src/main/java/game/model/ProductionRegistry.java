package game.model;

import java.util.*;

/**
 * ProductionRegistry.java
 *
 * PURPOSE:
 * Central registry for all available production items organized by category.
 * Provides easy access to production options for the UI and game logic.
 *
 * DESIGN PRINCIPLES:
 * - Centralized Configuration: Single source of truth for all production items
 * - Category Organization: Items grouped by type for easy menu display
 * - Lazy Initialization: Items created on first access for performance
 * - Immutable Collections: Prevents external modification of registered items
 */
public class ProductionRegistry {

    private static ProductionRegistry instance;
    private final Map<ProductionCategory, List<ProductionMenuItem>> productionItems;

    /**
     * Private constructor for singleton pattern.
     */
    private ProductionRegistry() {
        this.productionItems = new EnumMap<>(ProductionCategory.class);
        initializeProductionItems();
    }

    /**
     * Gets the singleton instance of the production registry.
     *
     * @return The global production registry
     */
    public static ProductionRegistry getInstance() {
        if (instance == null) {
            instance = new ProductionRegistry();
        }
        return instance;
    }

    /**
     * Initializes all available production items.
     * This is where new items should be registered.
     */
    private void initializeProductionItems() {
        // Initialize category lists
        for (ProductionCategory category : ProductionCategory.values()) {
            productionItems.put(category, new ArrayList<>());
        }

        // Register Units
        registerItem(new UnitProductionMenuItem(UnitType.SCOUT));
        registerItem(new UnitProductionMenuItem(UnitType.SETTLER));

        // Register Buildings
        registerItem(new BuildingProductionMenuItem(
            "Monument", 
            40, 
            "Increases culture and city border growth",
            "Culture: +2\nBorders expand faster\nHelps establish territorial claims"
        ));

        // Register Wonders
        registerItem(new WonderProductionMenuItem(
            "Taj Mahal", 
            200, 
            "Magnificent wonder providing happiness and culture",
            "Culture: +6\nHappiness: +4\nGreat Engineer point: +1\nOne of the most beautiful wonders ever built"
        ));
    }

    /**
     * Registers a production item in the appropriate category.
     *
     * @param item The item to register
     */
    private void registerItem(ProductionMenuItem item) {
        productionItems.get(item.getCategory()).add(item);
    }

    /**
     * Gets all production items for a specific category.
     *
     * @param category The category to retrieve
     * @return Immutable list of production items in that category
     */
    public List<ProductionMenuItem> getItemsForCategory(ProductionCategory category) {
        return Collections.unmodifiableList(productionItems.get(category));
    }

    /**
     * Gets all available production items across all categories.
     *
     * @return Immutable list of all production items
     */
    public List<ProductionMenuItem> getAllItems() {
        List<ProductionMenuItem> allItems = new ArrayList<>();
        for (List<ProductionMenuItem> categoryItems : productionItems.values()) {
            allItems.addAll(categoryItems);
        }
        return Collections.unmodifiableList(allItems);
    }

    /**
     * Gets all categories that have registered items.
     *
     * @return Set of categories with available items
     */
    public Set<ProductionCategory> getAvailableCategories() {
        Set<ProductionCategory> availableCategories = EnumSet.noneOf(ProductionCategory.class);
        for (Map.Entry<ProductionCategory, List<ProductionMenuItem>> entry : productionItems.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                availableCategories.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(availableCategories);
    }

    /**
     * Finds a production item by name across all categories.
     *
     * @param name The name to search for
     * @return The matching item, or null if not found
     */
    public ProductionMenuItem findItemByName(String name) {
        for (List<ProductionMenuItem> categoryItems : productionItems.values()) {
            for (ProductionMenuItem item : categoryItems) {
                if (item.getName().equals(name)) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Gets production items available for a specific city.
     * Filters out items that cannot be produced due to prerequisites.
     *
     * @param city The city to check production options for
     * @param category The category to filter by, or null for all categories
     * @return List of producible items for the city
     */
    public List<ProductionMenuItem> getAvailableItemsForCity(City city, ProductionCategory category) {
        List<ProductionMenuItem> availableItems = new ArrayList<>();
        List<ProductionMenuItem> itemsToCheck;

        if (category != null) {
            itemsToCheck = getItemsForCategory(category);
        } else {
            itemsToCheck = getAllItems();
        }

        for (ProductionMenuItem item : itemsToCheck) {
            if (item.canProduce(city)) {
                availableItems.add(item);
            }
        }

        return availableItems;
    }
}