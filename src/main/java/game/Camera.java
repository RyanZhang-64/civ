package game;

import processing.core.PApplet;
import processing.core.PVector;

public class Camera {

    private final PApplet p;
    private float panX;
    private float panY;
    private float zoom;

    public Camera(PApplet p) {
        this.p = p;
        this.panX = 0;
        this.panY = 0;
        this.zoom = 1.0f;
    }

    /**
     * Applies the camera's transformations. The new logic correctly centers the
     * view without conflicting with coordinate conversion.
     */
    public void beginTransform() {
        p.pushMatrix();
        // Step 1: Move the origin to the center of the screen.
        p.translate(p.width / 2f, p.height / 2f);
        // Step 2: Apply zoom and pan relative to this new center.
        p.scale(zoom);
        p.translate(panX, panY);
    }

    public void endTransform() {
        p.popMatrix();
    }

    public void panBy(float dx, float dy) {
        this.panX += dx / zoom;
        this.panY += dy / zoom;
    }

    public void zoom(float amount, float mouseX, float mouseY) {
        // Convert mouse position to world coordinates relative to the current view
        PVector mouseWorldBefore = screenToWorld(new PVector(mouseX, mouseY));
        
        float newZoom = zoom * amount;
        this.zoom = PApplet.constrain(newZoom, GameConfig.MIN_ZOOM, GameConfig.MAX_ZOOM);

        // Find where the same world point is now on the screen
        PVector mouseScreenAfter = worldToScreen(mouseWorldBefore);

        // Adjust pan to move that point back under the cursor
        panX += (mouseX - mouseScreenAfter.x) / zoom;
        panY += (mouseY - mouseScreenAfter.y) / zoom;
    }

    /**
     * Converts screen coordinates to world coordinates.
     * This is the inverse of the transformation in beginTransform.
     */
    public PVector screenToWorld(PVector screenCoords) {
        float worldX = (screenCoords.x - p.width / 2f) / zoom - panX;
        float worldY = (screenCoords.y - p.height / 2f) / zoom - panY;
        return new PVector(worldX, worldY);
    }

    /**
     * Converts world coordinates to screen coordinates.
     */
    public PVector worldToScreen(PVector worldCoords) {
        float screenX = (worldCoords.x + panX) * zoom + p.width / 2f;
        float screenY = (worldCoords.y + panY) * zoom + p.height / 2f;
        return new PVector(screenX, screenY);
    }

    public float getZoom() {
        return zoom;
    }

    /**
     * Centers the camera on a specific world coordinate.
     * @param worldX The x-coordinate in world space to center on
     * @param worldY The y-coordinate in world space to center on
     */
    public void centerOn(float worldX, float worldY) {
        // Since panX and panY are used in the opposite direction of world coordinates
        // (they move the world under the camera), we negate the values
        this.panX = -worldX;
        this.panY = -worldY;
    }
}
