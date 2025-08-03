package game.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import game.events.CityFoundedEvent;
import game.events.TurnChangedEvent;
import game.events.UnitMovedEvent;
import game.model.City;
import game.model.Civilization;
import game.model.Hex;
import game.model.Unit;
import processing.core.PVector;

/**
 * GameObjectPoolManager.java
 *
 * PURPOSE:
 * Manages object pools for frequently allocated game objects to reduce
 * garbage collection pressure and improve performance during gameplay.
 *
 * DESIGN PRINCIPLES:
 * - Minimal Overhead: Simple pooling with basic allocation tracking
 * - High-Impact Focus: Pools only the most frequently allocated objects
 * - Performance First: Optimized for game development workflow
 */
public class GameObjectPoolManager {
    
    // Event pools
    private final Queue<TurnChangedEvent> turnEventPool = new ArrayDeque<>();
    private final Queue<UnitMovedEvent> unitMovedPool = new ArrayDeque<>();
    private final Queue<CityFoundedEvent> cityFoundedPool = new ArrayDeque<>();
    
    // Coordinate calculation pools
    private final Queue<PVector> vectorPool = new ArrayDeque<>();
    private final Queue<List<Hex>> hexListPool = new ArrayDeque<>();
    private final Queue<Set<Hex>> hexSetPool = new ArrayDeque<>();
    private final Queue<Map<Hex, Integer>> hexMapPool = new ArrayDeque<>();
    
    // Pool configuration
    private static final int EVENT_POOL_SIZE = 10;
    private static final int VECTOR_POOL_SIZE = 50;
    private static final int COLLECTION_POOL_SIZE = 20;
    
    // Simple allocation tracking
    private int totalAllocations = 0;
    private int poolHits = 0;
    
    /**
     * Constructs the pool manager and pre-allocates objects.
     */
    public GameObjectPoolManager() {
        preAllocateObjects();
    }
    
    /**
     * Pre-allocates objects to avoid allocation during gameplay.
     */
    private void preAllocateObjects() {
        // Pre-allocate events
        for (int i = 0; i < EVENT_POOL_SIZE; i++) {
            turnEventPool.offer(new TurnChangedEvent());
            unitMovedPool.offer(new UnitMovedEvent());
            cityFoundedPool.offer(new CityFoundedEvent());
        }
        
        // Pre-allocate vectors
        for (int i = 0; i < VECTOR_POOL_SIZE; i++) {
            vectorPool.offer(new PVector());
        }
        
        // Pre-allocate collections
        for (int i = 0; i < COLLECTION_POOL_SIZE; i++) {
            hexListPool.offer(new ArrayList<>());
            hexSetPool.offer(new HashSet<>());
            hexMapPool.offer(new HashMap<>());
        }
    }
    
    // ========== Event Pooling ==========
    
    /**
     * Gets a pooled turn changed event.
     */
    public TurnChangedEvent getTurnEvent(Civilization previousCiv, Civilization newCiv, int turnNumber) {
        TurnChangedEvent event = turnEventPool.poll();
        if (event != null) {
            poolHits++;
            event.initialize(previousCiv, newCiv, turnNumber);
        } else {
            event = new TurnChangedEvent(previousCiv, newCiv, turnNumber);
        }
        totalAllocations++;
        return event;
    }
    
    /**
     * Returns a turn event to the pool.
     */
    public void returnTurnEvent(TurnChangedEvent event) {
        if (turnEventPool.size() < EVENT_POOL_SIZE) {
            event.reset();
            turnEventPool.offer(event);
        }
    }
    
    /**
     * Gets a pooled unit moved event.
     */
    public UnitMovedEvent getUnitMovedEvent(Unit unit, int fromQ, int fromR, int toQ, int toR) {
        UnitMovedEvent event = unitMovedPool.poll();
        if (event != null) {
            poolHits++;
            event.initialize(unit, fromQ, fromR, toQ, toR);
        } else {
            event = new UnitMovedEvent(unit, fromQ, fromR, toQ, toR);
        }
        totalAllocations++;
        return event;
    }
    
    /**
     * Returns a unit moved event to the pool.
     */
    public void returnUnitMovedEvent(UnitMovedEvent event) {
        if (unitMovedPool.size() < EVENT_POOL_SIZE) {
            event.reset();
            unitMovedPool.offer(event);
        }
    }
    
    /**
     * Gets a pooled city founded event.
     */
    public CityFoundedEvent getCityFoundedEvent(City city) {
        CityFoundedEvent event = cityFoundedPool.poll();
        if (event != null) {
            poolHits++;
            event.initialize(city);
        } else {
            event = new CityFoundedEvent(city);
        }
        totalAllocations++;
        return event;
    }
    
    /**
     * Returns a city founded event to the pool.
     */
    public void returnCityFoundedEvent(CityFoundedEvent event) {
        if (cityFoundedPool.size() < EVENT_POOL_SIZE) {
            event.reset();
            cityFoundedPool.offer(event);
        }
    }
    
    // ========== Coordinate Pooling ==========
    
    /**
     * Gets a pooled vector for coordinate calculations.
     */
    public PVector getVector() {
        PVector vector = vectorPool.poll();
        if (vector != null) {
            poolHits++;
            vector.set(0, 0, 0);
        } else {
            vector = new PVector();
        }
        totalAllocations++;
        return vector;
    }
    
    /**
     * Returns a vector to the pool.
     */
    public void returnVector(PVector vector) {
        if (vectorPool.size() < VECTOR_POOL_SIZE) {
            vector.set(0, 0, 0);
            vectorPool.offer(vector);
        }
    }
    
    /**
     * Gets a pooled hex list.
     */
    public List<Hex> getHexList() {
        List<Hex> list = hexListPool.poll();
        if (list != null) {
            poolHits++;
            list.clear();
        } else {
            list = new ArrayList<>();
        }
        totalAllocations++;
        return list;
    }
    
    /**
     * Returns a hex list to the pool.
     */
    public void returnHexList(List<Hex> list) {
        if (hexListPool.size() < COLLECTION_POOL_SIZE && list.size() < 100) {
            list.clear();
            hexListPool.offer(list);
        }
    }
    
    /**
     * Gets a pooled hex set.
     */
    public Set<Hex> getHexSet() {
        Set<Hex> set = hexSetPool.poll();
        if (set != null) {
            poolHits++;
            set.clear();
        } else {
            set = new HashSet<>();
        }
        totalAllocations++;
        return set;
    }
    
    /**
     * Returns a hex set to the pool.
     */
    public void returnHexSet(Set<Hex> set) {
        if (hexSetPool.size() < COLLECTION_POOL_SIZE && set.size() < 100) {
            set.clear();
            hexSetPool.offer(set);
        }
    }
    
    /**
     * Gets a pooled hex map.
     */
    public Map<Hex, Integer> getHexMap() {
        Map<Hex, Integer> map = hexMapPool.poll();
        if (map != null) {
            poolHits++;
            map.clear();
        } else {
            map = new HashMap<>();
        }
        totalAllocations++;
        return map;
    }
    
    /**
     * Returns a hex map to the pool.
     */
    public void returnHexMap(Map<Hex, Integer> map) {
        if (hexMapPool.size() < COLLECTION_POOL_SIZE && map.size() < 100) {
            map.clear();
            hexMapPool.offer(map);
        }
    }
    
    /**
     * Gets allocation efficiency metrics.
     * @return String with allocation count and pool hit rate.
     */
    public String getAllocationMetrics() {
        if (totalAllocations == 0) {
            return "Allocations: 0, Pool Hit Rate: 0%";
        }
        
        double hitRate = (poolHits * 100.0) / totalAllocations;
        return String.format("Allocations: %d, Pool Hit Rate: %.1f%%", totalAllocations, hitRate);
    }
    
    /**
     * Resets allocation counters.
     */
    public void resetMetrics() {
        totalAllocations = 0;
        poolHits = 0;
    }
}
