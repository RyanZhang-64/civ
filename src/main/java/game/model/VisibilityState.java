package game.model;

/**
 * Defines the three possible visibility states for a hex tile under the fog of war.
 */
public enum VisibilityState {
    /** The tile has never been seen by any unit. It will be completely hidden. */
    UNDISCOVERED,

    /** The tile has been seen before but is not currently in a unit's line of sight. */
    DISCOVERED,

    /** The tile is currently within sight of at least one unit. */
    CURRENTLY_VISIBLE
}
