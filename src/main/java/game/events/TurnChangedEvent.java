package game.events;

import game.model.Civilization;

/**
 * Event fired when the active civilization changes (turn ends).
 */
public class TurnChangedEvent implements GameEvent {
    private final Civilization previousCiv;
    private final Civilization newCiv;
    private final int turnNumber;
    private final long timestamp;
    
    public TurnChangedEvent(Civilization previousCiv, Civilization newCiv, int turnNumber) {
        this.previousCiv = previousCiv;
        this.newCiv = newCiv;
        this.turnNumber = turnNumber;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public Civilization getCivilization() {
        return newCiv;
    }
    
    @Override
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventType() {
        return "TurnChanged";
    }
    
    // Getters
    public Civilization getPreviousCivilization() { return previousCiv; }
    public Civilization getNewCivilization() { return newCiv; }
    public int getTurnNumber() { return turnNumber; }
}
