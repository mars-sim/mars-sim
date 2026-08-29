/*
 * Mars Simulation Project
 * FieldDataSet.java
 * @date 2026-08-27
 * @author Manny Kung
 */
package com.mars_sim.core.data.collection;

import java.util.Map;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.SurfacePOI;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.MarsTime;

public abstract class FieldDataSet implements SurfacePOI {
	
	private static int currentIdentifier;
	
	private DataType dataType;
	
	private Map<ScienceType, Integer> scienceTypes;
	
	// Unique identifier
	private int identifier;
	
	/** What is the starting quality of the data at the time of collection. from 0 to 100. */
	private final int initialQuality;
	
	/** What is the quality of the data. from 0 to 100. */
	private int quality;
	
	/** How difficult is it in processing the data. from 0 to 100. */
	private int difficultyLevel;

	/** researchTime in millisols. */
	private int researchTime; 
	
	private Person owner;
	
	private Settlement owningSettlement;
	
	private Coordinates location;
	
	private MarsTime timeCollected;
	
	public FieldDataSet(DataType dataType, MarsTime timeCollected, int initialQuality) {
		this.dataType = dataType;
		this.timeCollected = timeCollected;
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
	
	public int getResearchTime() {
		return researchTime;
	}
	
	public void addResearchTime(int time) {
		researchTime += time;
	}

	
	private Person getOwner() {
		return owner;
	}
	
	private Settlement getOwningSettlement() {
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
}


