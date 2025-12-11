package com.example.polystirolstats.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe mapper for world names to world IDs.
 * Maintains a mapping from world name (String) to world ID (Integer).
 */
public class WorldIdMapper {
	private final ConcurrentHashMap<String, Integer> worldNameToId = new ConcurrentHashMap<>();
	private final AtomicInteger nextId = new AtomicInteger(1);
	
	/**
	 * Register a world and assign it an ID if not already registered.
	 * @param worldName The name of the world to register
	 * @return The world ID assigned to this world (existing ID if already registered)
	 */
	public Integer registerWorld(String worldName) {
		if (worldName == null || worldName.isEmpty()) {
			return null;
		}
		
		return worldNameToId.computeIfAbsent(worldName, name -> nextId.getAndIncrement());
	}
	
	/**
	 * Get the world ID for a given world name.
	 * @param worldName The name of the world
	 * @return The world ID if registered, null otherwise
	 */
	public Integer getWorldId(String worldName) {
		if (worldName == null || worldName.isEmpty()) {
			return null;
		}
		
		return worldNameToId.get(worldName);
	}
	
	/**
	 * Check if a world is registered.
	 * @param worldName The name of the world
	 * @return true if the world is registered, false otherwise
	 */
	public boolean isWorldRegistered(String worldName) {
		if (worldName == null || worldName.isEmpty()) {
			return false;
		}
		
		return worldNameToId.containsKey(worldName);
	}
	
	/**
	 * Clear all registered worlds and reset the ID counter.
	 * Useful for testing or when worlds need to be re-registered.
	 */
	public void clear() {
		worldNameToId.clear();
		nextId.set(1);
	}
	
	/**
	 * Get the number of registered worlds.
	 * @return The number of registered worlds
	 */
	public int size() {
		return worldNameToId.size();
	}
}

