package game.rendering;

import java.util.List;

import processing.core.PApplet;
import game.model.City;
import game.model.ProductionCategory;
import game.model.ProductionMenuItem;
import game.model.ProductionRegistry;

/**
 * ProductionSidePanel.java
 *
 * PURPOSE:
 * Displays the full production menu on the left side with collapsible categories.
 * Stage 2 version: Unified menu structure with expandable/collapsible sections.
 *
 * DESIGN PRINCIPLES:
 * - Unified Interface: Single scrollable list with integrated categories
 * - Simple State Management: Boolean array for expansion states
 * - Left-Side Scrollbar: As specified in design requirements
 * - Performance Conscious: Only renders visible content
 */
public class ProductionSidePanel {

    private final PApplet p;
    private final ProductionRegistry registry;
    private City currentCity;
    private ProductionMenuItem selectedItem;
    private float scrollOffset;
    
    // Category expansion state (Units, Buildings, Wonders)
    private final boolean[] categoryExpanded = {true, false, false};
    
    // Layout constants
    private static final float PANEL_WIDTH = 350;
    private static final float SCROLLBAR_WIDTH = 12;
    private static final float MARGIN = 10;
    private static final float CATEGORY_HEIGHT = 30;
    private static final float ITEM_HEIGHT = 40;
    private static final float ITEM_INDENT = 20;
    private static final float SCROLL_SPEED = 20;
    
    // Calculated positions
    private final float panelX;
    private final float panelY;
    private final float panelHeight;
    private final float contentX;
    private final float contentWidth;
    private final float scrollbarX;

    /**
     * Constructs a new ProductionSidePanel.
     *
     * @param p The Processing applet for rendering
     */
    public ProductionSidePanel(PApplet p) {
        this.p = p;
        this.registry = ProductionRegistry.getInstance();
        this.currentCity = null;
        this.selectedItem = null;
        this.scrollOffset = 0;
        
        // Position on left side with left scrollbar
        this.panelX = 0;
        this.panelY = 0;
        this.panelHeight = p.height;
        this.scrollbarX = panelX + MARGIN;
        this.contentX = scrollbarX + SCROLLBAR_WIDTH + MARGIN;
        this.contentWidth = PANEL_WIDTH - SCROLLBAR_WIDTH - 3 * MARGIN;
    }

    /**
     * Sets the city context for production options.
     *
     * @param city The city to show production options for
     */
    public void setCity(City city) {
        this.currentCity = city;
        this.selectedItem = null;
        this.scrollOffset = 0;
    }

    /**
     * Gets the currently selected production item.
     *
     * @return The selected item, or null if none selected
     */
    public ProductionMenuItem getSelectedItem() {
        return selectedItem;
    }

    /**
     * Renders the production side panel with collapsible categories.
     */
    public void render() {
        if (currentCity == null) {
            return;
        }

        // Draw panel background
        p.fill(250, 250, 250);
        p.stroke(150);
        p.strokeWeight(2);
        p.rect(panelX, panelY, PANEL_WIDTH, panelHeight);
        
        // Draw title
        renderTitle();
        
        // Draw unified category/item list
        renderUnifiedList();
        
        // Draw left-side scrollbar if needed
        if (getTotalContentHeight() > getAvailableContentHeight()) {
            renderLeftScrollbar();
        }
    }

    /**
     * Renders the panel title.
     */
    private void renderTitle() {
        p.fill(0);
        p.textAlign(PApplet.CENTER, PApplet.TOP);
        p.textSize(16);
        p.text("Production Menu", panelX + PANEL_WIDTH / 2, panelY + 10);
        p.text(currentCity.getName(), panelX + PANEL_WIDTH / 2, panelY + 30);
    }

    /**
     * Renders the unified list with categories and items.
     */
    private void renderUnifiedList() {
        float startY = panelY + 60;
        float availableHeight = getAvailableContentHeight();
        float currentY = startY - scrollOffset;
        
        // Render each category and its items
        ProductionCategory[] categories = ProductionCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ProductionCategory category = categories[i];
            
            // Render category header
            if (currentY + CATEGORY_HEIGHT >= startY && currentY <= startY + availableHeight) {
                renderCategoryHeader(category, i, currentY);
            }
            currentY += CATEGORY_HEIGHT;
            
            // Render items if category is expanded
            if (categoryExpanded[i]) {
                List<ProductionMenuItem> items = registry.getAvailableItemsForCity(currentCity, category);
                for (ProductionMenuItem item : items) {
                    if (currentY + ITEM_HEIGHT >= startY && currentY <= startY + availableHeight) {
                        renderProductionItem(item, currentY);
                    }
                    currentY += ITEM_HEIGHT;
                }
            }
        }
    }

    /**
     * Renders a category header with expand/collapse indicator.
     */
    private void renderCategoryHeader(ProductionCategory category, int categoryIndex, float y) {
        // Background
        p.fill(200, 200, 200);
        p.stroke(150);
        p.strokeWeight(1);
        p.rect(contentX, y, contentWidth, CATEGORY_HEIGHT, 3);
        
        // Expand/collapse indicator
        String indicator = categoryExpanded[categoryIndex] ? "-" : "+";
        p.fill(0);
        p.textAlign(PApplet.LEFT, PApplet.CENTER);
        p.textSize(14);
        p.text(indicator, contentX + 8, y + CATEGORY_HEIGHT / 2);
        
        // Category name
        p.text(category.getDisplayName(), contentX + 25, y + CATEGORY_HEIGHT / 2);
        
        // Item count
        int itemCount = registry.getAvailableItemsForCity(currentCity, category).size();
        p.textAlign(PApplet.RIGHT, PApplet.CENTER);
        p.textSize(11);
        p.fill(100);
        p.text("(" + itemCount + ")", contentX + contentWidth - 8, y + CATEGORY_HEIGHT / 2);
    }

    /**
     * Renders a production item (indented under its category).
     */
    private void renderProductionItem(ProductionMenuItem item, float y) {
        boolean isSelected = item == selectedItem;
        boolean canProduce = currentCity != null && item.canProduce(currentCity);
        
        // Background
        if (isSelected) {
            p.fill(150, 200, 255); // Selected blue
        } else if (!canProduce) {
            p.fill(230, 230, 230); // Disabled light gray
        } else {
            p.fill(255); // Available white
        }
        
        p.stroke(120);
        p.strokeWeight(1);
        p.rect(contentX + ITEM_INDENT, y, contentWidth - ITEM_INDENT, ITEM_HEIGHT, 3);

        // Item name
        p.fill(canProduce ? 0 : 150);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(12);
        p.text(item.getName(), contentX + ITEM_INDENT + 8, y + 6);

        // Cost and turns on same line
        p.textSize(10);
        String costText = "Cost: " + item.getProductionCost();
        p.text(costText, contentX + ITEM_INDENT + 8, y + 22);

        if (currentCity != null && canProduce) {
            int turns = item.getTurnsToComplete(currentCity);
            String turnsText = turns > 0 ? turns + "t" : "N/A";
            p.textAlign(PApplet.RIGHT, PApplet.TOP);
            p.text(turnsText, contentX + contentWidth - 8, y + 22);
        }
    }

    /**
     * Renders the left-side scrollbar.
     */
    private void renderLeftScrollbar() {
        float scrollbarY = panelY + 60;
        float scrollbarHeight = getAvailableContentHeight();
        
        // Scrollbar track
        p.fill(220);
        p.noStroke();
        p.rect(scrollbarX, scrollbarY, SCROLLBAR_WIDTH, scrollbarHeight, 3);
        
        // Scrollbar thumb
        float contentHeight = getTotalContentHeight();
        float thumbHeight = Math.max(15, scrollbarHeight * scrollbarHeight / contentHeight);
        float thumbY = scrollbarY + (scrollOffset / contentHeight) * scrollbarHeight;
        
        p.fill(120);
        p.rect(scrollbarX, thumbY, SCROLLBAR_WIDTH, thumbHeight, 3);
    }

    /**
     * Handles mouse click events.
     *
     * @param mouseX The x-coordinate of the click
     * @param mouseY The y-coordinate of the click
     * @return true if the event was handled, false otherwise
     */
    public boolean handleMouseClick(float mouseX, float mouseY) {
        if (!isWithinPanel(mouseX, mouseY)) {
            return false;
        }

        // Check if click is in content area
        if (mouseX >= contentX) {
            return handleContentClick(mouseX, mouseY);
        }
        
        return false;
    }

    /**
     * Handles clicks within the content area.
     */
    private boolean handleContentClick(float mouseX, float mouseY) {
        float startY = panelY + 60;
        float relativeY = mouseY - startY + scrollOffset;
        float currentY = 0;
        
        // Check each category and its items
        ProductionCategory[] categories = ProductionCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ProductionCategory category = categories[i];
            
            // Check category header click
            if (relativeY >= currentY && relativeY < currentY + CATEGORY_HEIGHT) {
                categoryExpanded[i] = !categoryExpanded[i];
                return true;
            }
            currentY += CATEGORY_HEIGHT;
            
            // Check item clicks if category is expanded
            if (categoryExpanded[i]) {
                List<ProductionMenuItem> items = registry.getAvailableItemsForCity(currentCity, category);
                for (ProductionMenuItem item : items) {
                    if (relativeY >= currentY && relativeY < currentY + ITEM_HEIGHT) {
                        if (currentCity == null || item.canProduce(currentCity)) {
                            selectedItem = item;
                        }
                        return true;
                    }
                    currentY += ITEM_HEIGHT;
                }
            }
        }
        
        return false;
    }

    /**
     * Handles mouse scroll events.
     *
     * @param scrollAmount The scroll amount (positive = down, negative = up)
     */
    public void handleScroll(float scrollAmount) {
        float maxScroll = Math.max(0, getTotalContentHeight() - getAvailableContentHeight());
        scrollOffset = PApplet.constrain(scrollOffset + scrollAmount * SCROLL_SPEED, 0, maxScroll);
    }

    /**
     * Checks if the mouse is within the panel area.
     */
    public boolean isWithinPanel(float mouseX, float mouseY) {
        return mouseX >= panelX && mouseX <= panelX + PANEL_WIDTH &&
               mouseY >= panelY && mouseY <= panelY + panelHeight;
    }

    /**
     * Calculates the total height of all content based on expansion states.
     */
    private float getTotalContentHeight() {
        float height = 0;
        ProductionCategory[] categories = ProductionCategory.values();
        
        for (int i = 0; i < categories.length; i++) {
            height += CATEGORY_HEIGHT; // Category header always visible
            
            if (categoryExpanded[i]) {
                int itemCount = registry.getAvailableItemsForCity(currentCity, categories[i]).size();
                height += itemCount * ITEM_HEIGHT;
            }
        }
        
        return height;
    }

    /**
     * Gets the available height for content display.
     */
    private float getAvailableContentHeight() {
        return panelHeight - 70; // Account for title area
    }

    /**
     * Gets debug information about expansion states.
     *
     * @return String containing expansion state info
     */
    public String getDebugInfo() {
        StringBuilder info = new StringBuilder("Categories: ");
        ProductionCategory[] categories = ProductionCategory.values();
        for (int i = 0; i < categories.length; i++) {
            info.append(categories[i].name())
                .append(categoryExpanded[i] ? "(expanded)" : "(collapsed)")
                .append(" ");
        }
        return info.toString();
    }
}