package com.mars_sim.core.data.collection;

import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.environment.CollectionSite;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.LocalPosition;

public class DataCollectionSite extends CollectionSite {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The quality of being known about this site. */
	private int familiarity = 0;
	/** The list of instruments present on site. */
	private List<Integer> instrumentAvailability = new ArrayList<>();
	/** The local position of this site of a given coordinates. */
	private LocalPosition localPosition;
	
	/**
	 * Constructor.
	 * 
	 * @param location
	 */
	public DataCollectionSite(Coordinates location, LocalPosition localPosition) {
		super(location);
		
		this.localPosition = localPosition;
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
	 * Gets the local position.
	 * 
	 * @return
	 */
	public LocalPosition getLocalPosition() {
		return localPosition;
	}
	
	/**
	 * Returns true if the collection site have the same coordinates
	 *
	 * @param o
	 * @return true if matched, false otherwise
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if ((o != null) && (o instanceof DataCollectionSite)) {
			DataCollectionSite s = (DataCollectionSite) o;
            return this.location.equals(s.getLocation())
                    && this.localPosition == s.getLocalPosition();
		}

		return false;
	}
	
	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	public int hashCode() {
		return (int)(localPosition.hashCode() + location.hashCode());
	}
}
