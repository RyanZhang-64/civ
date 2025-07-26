package game.rendering;

import java.util.ArrayList;
import java.util.List;

import game.GameConfig;
import game.core.UnitManager;
import processing.core.PApplet;

/**
 * UIManager.java
 *
 * PURPOSE:
 * Manages the creation, rendering, and interaction of all UI elements,
 * such as buttons.
 */
public class UIManager {

    private final PApplet p;
    private final UnitManager unitManager;
    private final List<UIButton> buttons;

    public UIManager(PApplet p, UnitManager um) {
        this.p = p;
        this.unitManager = um;
        this.buttons = new ArrayList<>();
        createButtons();
    }

    /**
     * Creates all the UI buttons and adds them to the manager.
     */
    private void createButtons() {
        float x = p.width - GameConfig.BUTTON_WIDTH - GameConfig.BUTTON_MARGIN;
        float y_refresh = p.height - GameConfig.BUTTON_HEIGHT - GameConfig.BUTTON_MARGIN;
        float y_next = p.height - (2 * GameConfig.BUTTON_HEIGHT) - (2 * GameConfig.BUTTON_MARGIN);

        buttons.add(new NextUnitButton(x, y_next, GameConfig.BUTTON_WIDTH, GameConfig.BUTTON_HEIGHT));
        buttons.add(new RefreshUnitsButton(x, y_refresh, GameConfig.BUTTON_WIDTH, GameConfig.BUTTON_HEIGHT));
    }

    /**
     * Renders all visible UI elements.
     */
    public void render() {
        for (UIButton button : buttons) {
            button.display();
        }
    }

    /**
     * Handles a mouse press event, checking if any button was clicked.
     * @param mouseX The x-coordinate of the mouse.
     * @param mouseY The y-coordinate of the mouse.
     * @return true if a UI element handled the click, false otherwise.
     */
    public boolean handleMousePress(float mouseX, float mouseY) {
        for (UIButton button : buttons) {
            if (button.isClicked(mouseX, mouseY)) {
                button.onClick();
                return true;
            }
        }
        return false;
    }

    // =========================================================================
    // Inner Classes for UI Buttons
    // =========================================================================

    private abstract class UIButton {
        float x, y, width, height;
        String label;
        int backgroundColor, textColor;

        UIButton(float x, float y, float w, float h, String label, int bgColor, int txtColor) {
            this.x = x; this.y = y; this.width = w; this.height = h;
            this.label = label; this.backgroundColor = bgColor; this.textColor = txtColor;
        }

        boolean isClicked(float mouseX, float mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        void display() {
            p.fill(backgroundColor);
            p.stroke(0);
            p.strokeWeight(2);
            p.rect(x, y, width, height, 5);
            p.fill(textColor);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.textSize(14);
            p.text(label, x + width / 2, y + height / 2);
        }

        abstract void onClick();
    }

    private class RefreshUnitsButton extends UIButton {
        RefreshUnitsButton(float x, float y, float w, float h) {
            super(x, y, w, h, "Refresh Units", p.color(100, 150, 255), p.color(255));
        }

        @Override
        void onClick() {
            unitManager.refreshAllUnits();
        }
    }

    private class NextUnitButton extends UIButton {
        NextUnitButton(float x, float y, float w, float h) {
            super(x, y, w, h, "Next Unit", p.color(50, 200, 100), p.color(255));
        }

        @Override
        void onClick() {
            unitManager.selectNextUnitWithMovement();
        }
    }
}
