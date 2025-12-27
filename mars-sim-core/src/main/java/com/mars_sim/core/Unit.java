/*
 * Mars Simulation Project
 * Unit.java
 * @date 2023-05-09
 * @author Scott Davis
 */
package com.mars_sim.core;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.environment.Weather;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.ai.mission.MissionManager;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;

/**
 * The Unit class is the abstract parent class to all units in the simulation.
 * Units include people, vehicles and settlements. This class provides data
 * members and methods common to all units.
 */
public abstract class Unit implements MonitorableEntity, UnitIdentifer, Comparable<Unit> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Unit.class.getName());

	// Event type constants for Unit-specific events
	public static final String NOTES_EVENT = "notes";

	public static final int MOON_UNIT_ID = -2;
	public static final int OUTER_SPACE_UNIT_ID = -1;
	public static final int MARS_SURFACE_UNIT_ID = 0;
	public static final Integer UNKNOWN_UNIT_ID = -3;

	// Data members

	// Unique Unit identifier
	private int identifier;
	/** The last pulse applied. */
	private long lastPulse = 0;
	
	private String name;
	private String description = "No Description";
	/** Commander's notes on this unit. */
	private String notes = "";

	/** Entity listeners. */
	private transient Set<EntityListener> listeners;

	protected static MasterClock masterClock;

	protected static UnitManager unitManager;
	protected static MissionManager missionManager;

	protected static Weather weather;

	// File for diagnostics output
	private static PrintWriter diagnosticFile = null;

	/**
	 * Enable the detailed diagnostics
	 *
	 * @throws FileNotFoundException
	 */
	public static void setDiagnostics(boolean diagnostics) throws FileNotFoundException {
		if (diagnostics) {
			if (diagnosticFile == null) {
				String filename = SimulationRuntime.getLogDir() + "/unit-create.txt";
				diagnosticFile = new PrintWriter(filename);
				logger.config("Diagnostics enabled to " + filename);
			}
		} else if (diagnosticFile != null) {
			diagnosticFile.close();
			diagnosticFile = null;
		}
	}

	/**
	 * Log the creation of a new Unit
	 *
	 * @param entry
	 */
	private static void logCreation(Unit entry) {
		StringBuilder output = new StringBuilder();
		output.append(masterClock.getMarsTime().getDateTimeStamp()).append(" Id:").append(entry.getIdentifier())
				.append(" Type:").append(entry.getUnitType()).append(" Name:").append(entry.getName());

		synchronized (diagnosticFile) {
			diagnosticFile.println(output.toString());
			diagnosticFile.flush();
		}
	}

	/**
	 * Gets the identifier of this unit.
	 */
	public final int getIdentifier() {
		return identifier;
	}

	/**
	 * Constructor 1: the name and identifier are defined.
	 *
	 * @param name     {@link String} the name of the unit
	 * @param id Unit identifier
	 */
	protected Unit(String name, int id) {
		// Initialize data members from parameters
		this.name = name;
		this.identifier = id;
	}

	/**
	 * Constructor 2: where the name and location are defined.
	 *
	 * @param name     {@link String} the name of the unit
	 */
	protected Unit(String name) {
		// Initialize data members from parameters
		this.name = name;

		if (masterClock != null) {
			// Needed for maven test
			this.lastPulse = masterClock.getNextPulse() - 1;
	
			// Calculate the new Identifier for this type
			identifier = unitManager.generateNewId(getUnitType());
		}

		if (diagnosticFile != null) {
			logCreation(this);
		}
	}

	/**
	 * What logical UnitType of this object in terms of the management. This is NOT
	 * a direct mapping to the concrete subclass of Unit since some logical
	 * UnitTypes can have multiple implementation, e.g. Equipment.
	 *
	 * @return
	 */
	public abstract UnitType getUnitType();

	/**
	 * Is this time pulse valid for the Unit. Has it been already applied? The logic
	 * on this method can be commented out later on
	 *
	 * @param pulse Pulse to apply
	 * @return Valid to accept
	 */
	protected boolean isValid(ClockPulse pulse) {
		long newPulse = pulse.getId();
		boolean result = (newPulse > lastPulse && pulse.getElapsed() > 0);
		if (result) {
			long expectedPulse = lastPulse + 1;
			if (expectedPulse != newPulse) {
				// Pulse out of sequence. May have missed one.
				// Note: this usually happens when restarting the simulation from pause
				logger.warning(getName() + " expected pulse #" + expectedPulse + " but received #" + newPulse + ".");
			}
			lastPulse = newPulse;
		} else {
			if (newPulse == lastPulse) {
				// This is a newly added unit such as person/vehicle/robot in a resupply transport.
				return true;
			}
			else
				logger.severe(getName() + " rejected pulse #" + newPulse + ". Last pulse was #" + lastPulse+ ".");
		}
		return result;
	}

	/**
	 * Gets the unit's name.
	 *
	 * @return the unit's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the unit's name.
	 *
	 * @param name new name
	 */
	public void setName(String name) {
		this.name = name;
		fireUnitUpdate(EntityEventType.NAME_EVENT, name);
	}

	/**
	 * Gets the unit's description.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the unit's description.
	 *
	 * @param description new description.
	 */
	protected void setDescription(String description) {
		this.description = description;
		fireUnitUpdate(EntityEventType.DESCRIPTION_EVENT, description);
	}

	/**
	 * Gets the commander's notes on this unit.
	 *
	 * @return notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Sets the commander's notes on this unit.
	 *
	 * @param notes.
	 */
	public void setNotes(String notes) {
		this.notes = notes;
		fireUnitUpdate(NOTES_EVENT, notes);
	}

	/**
	 * Checks if it has an entity listener.
	 * 
	 * @param listener
	 * @return
	 */
	public synchronized boolean hasEntityListener(EntityListener listener) {
		if (listeners == null)
			return false;
		return listeners.contains(listener);
	}

	/**
	 * Adds an entity listener.
	 *
	 * @param newListener the listener to add.
	 */
	@Override
	public final synchronized void addEntityListener(EntityListener newListener) {
		if (newListener == null)
			throw new IllegalArgumentException();
		if (listeners == null)
			listeners = new HashSet<>();

		synchronized(listeners) {	
			listeners.add(newListener);
		}
	}

	/**
	 * Removes an entity listener.
	 *
	 * @param oldListener the listener to remove.
	 */
	@Override
	public final synchronized void removeEntityListener(EntityListener oldListener) {
		if (oldListener == null)
			throw new IllegalArgumentException();

		if (listeners != null) {
			synchronized(listeners) {
				listeners.remove(oldListener);
			}
		}
	}

	/**
	 * Gets an unmodifiable set of the active listeners on this entity.
	 * 
	 * @return unmodifiable set of entity listeners.
	 */
	@Override
	public final synchronized Set<EntityListener> getListeners() {
		if (listeners == null) {
			return Set.of();
		}
		return Set.copyOf(listeners);
	}

	/**
	 * Fires an entity update event.
	 *
	 * @param updateType the update type.
	 */
	public final void fireUnitUpdate(String updateType) {
		fireUnitUpdate(updateType, null);
	}

	/**
	 * Fires an entity update event.
	 *
	 * @param updateType the update type.
	 * @param target     the event target object or null if none.
	 */
	public final void fireUnitUpdate(String updateType, Object target) {
		if (listeners == null || listeners.isEmpty()) {
			return;
		}
		final EntityEvent ue = new EntityEvent(this, updateType, target);
		synchronized (listeners) {
			for(EntityListener i : listeners) {
				try {
					// Stop listeners breaking the update thread
					i.entityUpdate(ue);
				}
				catch(RuntimeException rte) {
					logger.severe(this, "Problem executing listener " + i + " for event " + ue, rte);
				}
			}
		}
	}

	/**
	 * Gets the associated settlement this unit is with.
	 *
	 * @return the associated settlement
	 */
	public Settlement getAssociatedSettlement() {
		return null;
	}

	/**
	 * Loads instances.
	 *
	 */
	public static void initializeInstances(MasterClock c0, UnitManager um,
			Weather w, MissionManager mm) {
		masterClock = c0;
		weather = w;
		unitManager = um;
		missionManager = mm;
	}

	/**
	 * Compares this object with the specified object for order.
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Unit o) {
		return name.compareToIgnoreCase(o.name);
	}

	/**
	 * String representation of this Unit.
	 *
	 * @return The units name.
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Compares if an object is the same as this unit
	 *
	 * @param obj
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		return this.getIdentifier() == ((Unit) obj).getIdentifier();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return getIdentifier() % 32;
	}

	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		name = null;
		description = null;
		listeners = null;
	}
}
