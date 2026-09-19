/*
 * Mars Simulation Project
 * DataCollectionSite.java
 * @date 2026-08-27
 * @author Manny Kung
 */

package com.mars_sim.core.data.collection;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.EntityIdentifier;
import com.mars_sim.core.environment.CollectionSite;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalBoundedObject;
import com.mars_sim.core.map.location.LocalPosition;
import com.mars_sim.core.map.location.SettlementPOI;
import com.mars_sim.core.structure.Settlement;

public class DataCollectionSite extends CollectionSite
	implements LocalBoundedObject, SettlementPOI {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** Static members */
	private static final int WIDTH = 6;
	private static final int LENGTH = 6;
	public static final double HYPOTENUSE = Math.sqrt(WIDTH * WIDTH + LENGTH * LENGTH);
	
	private static final String DCS_ENTITY_TYPE = "DATACOLLECTIONSITE";
	private static final String DATA_COLLECTION_SITE = "Data Collection Site";
	private static final String DATA_SITE = "DCS ";
	/** Static identifier that increment when a new site is created. */
	private static int currentIdentifier;
	/** Unique identifier for each site. */
	private int identifier;
	
	
	/** The quality of being known about this site. */
	private int familiarity = 0;
	/** The list of instruments present on site. */
	private List<Integer> instrumentAvailability = new ArrayList<>();
	/** The local position of this site of a given coordinates. */
	private LocalPosition localPosition;
	private Settlement settlement;
	
	
	/**
	 * Constructor 1.
	 * 
	 * @param location
	 */
	public DataCollectionSite(Coordinates location, Settlement settlement, LocalPosition localPosition) {
		super(location);
				
		identifier = currentIdentifier;
		currentIdentifier++;

		setPosition(settlement, localPosition);
	}

	/**
	 * Constructor 2.
	 * 
	 * @param location
	 */
	public DataCollectionSite(Coordinates location) {
		super(location);
	}
	
	/**
	 * Creates a empty site.
	 * 
	 * @param location
	 * @return
	 */
	public static DataCollectionSite creatEmptySite(Coordinates location) {
		return new DataCollectionSite(location);
	}
	
	/**
	 * Gets the identifier.
	 * 
	 * @return
	 */
	public int getIdentifier() {
		return identifier;
	}
	
	/**
	 * Gets the degree of familiarity.
	 *  
	 * @return
	 */
	public int getFamiliarity() {
		return familiarity;
	}
	
	/**
	 * Sets the degree of familiarity.
	 *  
	 * @return
	 */
	public void setFamiliarity(int value) {
		familiarity = value;
	}
	
	/**
	 * Gets the instrument availability list.
	 * 
	 * @return
	 */
	public List<Integer> getInstrumentAvailability() {
		return instrumentAvailability;
	}
	
	/**
	 * Gets the number of instruments available.
	 * 
	 * @return
	 */
	public int getNumInstrumentAvailable() {
		if (instrumentAvailability.isEmpty())
			return 0;
		else {
			return instrumentAvailability.size();
		}
	}
	
	/**
	 * Adds an instrument.
	 * 
	 * @param id
	 * @return
	 */
	public boolean addInstrument(Integer id) {
		return instrumentAvailability.add(id);
	}

	/**
	 * Removes an instrument.
	 * 
	 * @param id
	 * @return
	 */
	public boolean removeInstrument(Integer id) {
		return instrumentAvailability.remove(id);
	}
	
	/**
	 * Removes an instrument.
	 * 
	 * @param id
	 * @return
	 */
	public boolean hasInstrument(Integer id) {
		return instrumentAvailability.contains(id);
	}
	
	/**
	 * Gets the settlement in which this data collection site is located.
	 * @see com.mars_sim.core.map.location.SettlementPOI#getSettlement()
	 * @return the settlement in which this data collection site is located. Maybe null if on surface.
	 */
	@Override
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Gets the local position.
	 * 
	 * @return
	 */
	@Override
	public LocalPosition getPosition() {
		return localPosition;
	}
	
	/**
	 * Sets the local position within a Settlement,
	 * 
	 * @param settlement the settlement in which the local position is set
	 * @param localPosition the local position to set
	 */
	public void setPosition(Settlement settlement, LocalPosition localPosition) {
		this.settlement = settlement;
		this.localPosition = localPosition;
	}
	
	
	/**
	 * Gets the name of the site.
	 *
	 * @return the name of the site
	 */
	@Override
	public String getName() {
		return DATA_SITE + identifier;
	}

	/**
	 * Gets the type.
	 * 
	 * @return name
	 */
	public String getType() {
		return DATA_COLLECTION_SITE;
	}
	
	/**
	 * Gets the description of this site.
	 * 
	 * @return
	 */
	public String getDescription() {
		return "# Instruments available: " + getNumInstrumentAvailable();
	}
	
	@Override
	public double getWidth() {
		return WIDTH;
	}

	@Override
	public double getLength() {
		return LENGTH;
	}

	@Override
	public double getFacing() {
		return 0;
	}

	@Override
	public String getContext() {
		return getLocation().getFormattedString();
	}

	@Override
	public EntityIdentifier getEntityIdentifier() {
		return new EntityIdentifier(DCS_ENTITY_TYPE, String.valueOf(identifier));
	}
	
	/**
	 * Returns true if the collection site have the same coordinates
	 *
	 * @param o
	 * @return true if matched, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof DataCollectionSite s) {
            return this.location.equals(s.getLocation())
                    && this.localPosition == s.getPosition();
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
		return (localPosition.hashCode() + location.hashCode());
	}
}
