/*
 * Mars Simulation Project
 * FieldDataSet.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityIdentifier;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.EntityListenerManager;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

public abstract class FieldDataSet implements SurfacePOI, MonitorableEntity {
	
	private static final long serialVersionUID = 1L;

	private static int currentIdentifier;

	// Unique identifier
	private int identifier;
	/** What is the starting quality of the data at the time of collection. from 0 to 100. */
	private final int initialQuality;
	/** What is the quality of the data. from 0 to 100. */
	private int quality;
	/** How difficult is it in processing the data. from 0 to 100. */
	private int difficultyLevel;
	/** researchTime in millisols. */
	private double researchTime; 
	/** workTime in millisols. */
	private double workTime;
	
	private DataType dataType;
	
	private Map<ScienceType, Integer> scienceTypes;
	
	private Worker owner;
	
	private Settlement owningSettlement;
	
	private Coordinates location;
	
	private MarsTime startTimpstamp;
	
	/** A set of listeners for this scientific study. */
	private transient EntityListenerManager listeners;
	
	
	public FieldDataSet(Settlement settlement, Worker owner, DataType dataType, MarsTime timeCollected, int initialQuality) {
		this.owningSettlement = settlement;
		this.owner = owner;
		this.dataType = dataType;
		this.startTimpstamp = timeCollected;
		this.initialQuality = initialQuality;
		
		identifier = currentIdentifier;
		currentIdentifier++;
	}
	
	public DataType getDataType() {
		return dataType;
	}
	
	/**
	 * Gets the identifier.
	 * 
	 * @return
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	public int getQuality() {
		return quality;
	}
	
	public void addQuality(int q) {
		quality += q;
		if (quality > 100)
			quality = 100;
	}
	
	public double getResearchTime() {
		return researchTime;
	}
	
	public void addResearchTime(double time) {
		researchTime += time;
	}

	public void addWorkTime(double time) {
		workTime += time;
	}
	
	public double getWorkTime() {
		return workTime;
	}
	
	public Worker getOwner() {
		return owner;
	}
	
	public Settlement getOwningSettlement() {
		return owningSettlement;
	}
	
	/**
	 * Gets the location coordinates.
	 *
	 * @return coordinates.
	 */
	@Override
	public Coordinates getCoordinates() {
		return location;
	}

	@Override
	public String getName() {
		// To be overridden by subclass.
		return "FieldData" + getIdentifier();
	}
	
	public String getContext() {
		return "FieldDataSet";
	}
	

	/**
	 * Adds a listener.
	 * 
	 * @param newListener the listener to add.
	 */
	@Override
	public void addEntityListener(EntityListener newListener) {
		if (newListener == null)
			throw new IllegalArgumentException();
		if (listeners == null)
			listeners = new EntityListenerManager();

		listeners.addEntityListener(newListener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param oldListener the listener to remove.
	 */
	@Override
	public void removeEntityListener(EntityListener oldListener) {
		if (listeners != null)
			listeners.removeEntityListener(oldListener);
	}

	/**
	 * Gets an unmodifiable set of the active listeners on this entity.
	 * 
	 * @return unmodifiable set of entity listeners.
	 */
	@Override
	public Set<EntityListener> getEntityListeners() {
		if (listeners == null) {
			return Collections.emptySet();
		}
		return listeners.getEntityListeners();
	}

	@Override
	public EntityIdentifier getEntityIdentifier() {
		return new EntityIdentifier("DATA", String.valueOf(identifier));
	}
	
	/**
	 * Fires an update event.
	 * 
	 * @param updateType the update type.
	 */
	public void fireUpdate(String updateType) {
		if (listeners == null) {
			return;
		}
		final EntityEvent studyEvent = new EntityEvent(this, updateType, owner);
		listeners.fireEvent(studyEvent);
	}
}


