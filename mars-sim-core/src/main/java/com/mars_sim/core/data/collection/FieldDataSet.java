/*
 * Mars Simulation Project
 * FieldDataSet.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import java.util.Collections;
import java.util.Set;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityIdentifier;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.EntityListenerManager;
import com.mars_sim.core.MonitorableEntity;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

public abstract class FieldDataSet implements SurfacePOI, MonitorableEntity {
	
	private static final long serialVersionUID = 1L;

    public static final String UPDATE = "FieldDataSet Update";

	private static int currentIdentifier;

	// Unique identifier
	private int identifier;
	/** What is the starting quality of the data at the time of collection. from 0 to 100. */
	private final double initialQuality;
	/** What is the quality of the data. from 0 to 100. */
	private double quality = -1;
	/** researchTime in millisols. */
	private double researchTime; 
	/** workTime in millisols. */
	private double workTime;
	
	private DataType dataType;
		
	private Worker owner;
	
	private Settlement owningSettlement;
	
	private Coordinates location;
	
	private MarsTime startTimpstamp;
	
	/** A set of listeners for this scientific study. */
	private transient EntityListenerManager listenerManager;
	
	
	public FieldDataSet(Settlement settlement, Worker owner, DataType dataType, MarsTime timeCollected, double initialQuality) {
		this.owningSettlement = settlement;
		this.owner = owner;
		this.dataType = dataType;
		this.startTimpstamp = timeCollected;
		this.initialQuality = initialQuality;
		
		this.location = settlement.getCoordinates();
		
		identifier = currentIdentifier;
		currentIdentifier++;
	}
	
	/**
	 * Gets the data type.
	 * 
	 * @return
	 */
	public DataType getDataType() {
		return dataType;
	}
	
	/**
	 * Gets the start timestamp.
	 * 
	 * @return
	 */
	public MarsTime getTimpstamp() {
		return startTimpstamp;
	}
	
	/**
	 * Gets the identifier.
	 * 
	 * @return
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	public double getInitialQuality() {
		return initialQuality;
	}
	
	public double getQuality() {
		if (quality == -1) {
			quality = initialQuality;
		}
		return quality;
	}
	
	public void addQuality(double q) {
		if (quality == -1) {
			quality = 0;
		}
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
		if (listenerManager == null)
			listenerManager = new EntityListenerManager();

		listenerManager.addEntityListener(newListener);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param oldListener the listener to remove.
	 */
	@Override
	public void removeEntityListener(EntityListener oldListener) {
		if (listenerManager != null)
			listenerManager.removeEntityListener(oldListener);
	}

	/**
	 * Gets an unmodifiable set of the active listeners on this entity.
	 * 
	 * @return unmodifiable set of entity listeners.
	 */
	@Override
	public Set<EntityListener> getEntityListeners() {
		if (listenerManager == null) {
			return Collections.emptySet();
		}
		return listenerManager.getEntityListeners();
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
		if (listenerManager == null) {
			return;
		}
		final EntityEvent studyEvent = new EntityEvent(this, updateType, owner);
		listenerManager.fireEvent(studyEvent);
	}
	
	
	/**
	 * Returns true if the dataset have the same signature.
	 *
	 * @param o
	 * @return true if matched, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof FieldDataSet s) {
            return this.location.equals(s.getLocation())
            		&& this.startTimpstamp == s.getTimpstamp()
                    && this.identifier == s.getIdentifier();
		}

		return false;
	}
	
	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
	public int hashCode() {
		return (startTimpstamp.hashCode() + owner.hashCode() + location.hashCode());
	}
}


