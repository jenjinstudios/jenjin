package com.jenjinstudios.world;

import com.jenjinstudios.world.math.Dimension2D;
import com.jenjinstudios.world.math.Vector2D;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains all the Zones, Locations and GameObjects.
 * @author Caleb Brinkman
 */
public class World
{
	/** The list of in-world Zones. */
	private final Zone[] zones;
	/** The GameObjects contained in the world. */
	private final WorldObjectMap worldObjects;
	/** The time at which the most recent update completed. */
	private long lastUpdateCompleted;
	/** The start time of the most recent update. */
	private long lastUpdateStarted;

	/** Construct a new World. */
	public World() {
		zones = new Zone[1];
		/* The default size of the world's location grid. */
		int DEFAULT_SIZE = 50;
		zones[0] = new Zone(0, new Dimension2D(DEFAULT_SIZE, DEFAULT_SIZE));
		worldObjects = new WorldObjectMap();
	}

	/**
	 * Construct a new world with the specified Zone array.
	 * @param zones The zones used to create the world.
	 */
	public World(Zone[] zones) {
		this.zones = zones;
		worldObjects = new WorldObjectMap();
	}

	/**
	 * Add an object to the world.
	 * @param object The object to add.
	 */
	public void addObject(WorldObject object) {
		int id = worldObjects.getAvailableId();
		this.addObject(object, id);
	}

	/**
	 * Add an object with the specified ID.
	 * @param object The object to add.
	 * @param id The id.
	 */
	public void addObject(WorldObject object, int id) {
		if (object == null)
			throw new IllegalArgumentException("addObject(WorldObject obj) argument 0 not allowed to be null!");

		if (worldObjects.get(id) != null)
			throw new IllegalArgumentException("addObject(WorldObject obj) not allowed to be an occupied id: "
					+ id + ".  Existing object: " + worldObjects.get(id));

		object.setWorld(this);
		object.setVector2D(object.getVector2D());
		synchronized (worldObjects)
		{
			object.setId(id);
			worldObjects.put(id, object);
		}
	}

	/**
	 * Remove an object from the world.  Specifically, sets the index of the given object in the world's array to null.
	 * @param object The object to remove.
	 */
	public void removeObject(WorldObject object) {
		removeObject(object.getId());
	}

	/**
	 * Remove the object with the specified id.
	 * @param id The id.
	 */
	public void removeObject(int id) {
		synchronized (worldObjects)
		{
			worldObjects.remove(id);
		}
	}

	/**
	 * Get the location from the zone grid that contains the specified vector2D.
	 * @param zoneID The ID of the zone in which to look for the location.
	 * @param vector2D The vector2D.
	 * @return The location that contains the specified vector2D.
	 */
	public Location getLocationForCoordinates(int zoneID, Vector2D vector2D) {
		return zones[zoneID].getLocationForCoordinates(vector2D);
	}

	/** Update all objects in the world. */
	public void update() {
		lastUpdateStarted = System.nanoTime();
		synchronized (worldObjects)
		{
			Collection<WorldObject> values = worldObjects.values();
			setUpObjects(values);
			updateObjects(values);
			resetObjects(values);
		}
		lastUpdateCompleted = System.nanoTime();
	}

	private void resetObjects(Collection<WorldObject> values) {
		for (WorldObject o : values)
			if (o != null)
				o.reset();
	}

	private void updateObjects(Collection<WorldObject> values) {
		for (WorldObject o : values)
			if (o != null)
				o.update();
	}

	private void setUpObjects(Collection<WorldObject> values) {
		for (WorldObject o : values)
			if (o != null)
				o.setUp();
	}

	/**
	 * Get the number of objects currently in the world.
	 * @return The number of objects currently in the world.
	 */
	public int getObjectCount() { return worldObjects.size(); }

	public WorldObject getObject(int id) { return worldObjects.get(id); }

	/**
	 * Get a list of all valid Zone IDs in this world.
	 * @return A List of all IDs which are linked to a zone.
	 */
	public List<Integer> getZoneIDs() {
		LinkedList<Integer> r = new LinkedList<>();
		synchronized (zones)
		{
			for (Zone z : zones)
			{
				r.add(z.id);
			}
		}
		return r;
	}

	/**
	 * Get the zone with the given id.
	 * @param id The id of the zone to retrieve.
	 * @return The zone with the given id.
	 */
	public Zone getZone(int id) {
		Zone r = null;
		synchronized (zones)
		{
			for (Zone z : zones)
			{
				if (z.id == id)
					r = z;
			}
		}
		return r;
	}

	/**
	 * Get the time at which the most recent update completed.
	 * @return The time at which the most recent update completed.
	 */
	public long getLastUpdateCompleted() { return lastUpdateCompleted; }

	/**
	 * Get the time at which the most recent update started.
	 * @return The time at which the most recent update started.
	 */
	public long getLastUpdateStarted() { return lastUpdateStarted; }
}
