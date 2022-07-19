/**
 * Mars Simulation Project
 * BuildingTemplate.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mars_sim.msp.core.BoundedObject;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.Msg;

/**
 * A building template information.
 */
public class BuildingTemplate implements Serializable, Comparable<BuildingTemplate> {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private int bid;
	private int eVAAttachedBuildingID;
	private BoundedObject bounds;

	private String buildingType;
	private String nickName;
	private String scenario;

	private String missionName;

	private List<BuildingConnectionTemplate> connectionList;

	/*
	 * * BuildingTemplate Constructor.
	 */
	public BuildingTemplate(String missionName, int bid, String scenario, String buildingType, String nickName,
			BoundedObject bounds) {

		this.missionName = missionName;
		this.bid = bid;
		this.scenario = scenario;
		this.buildingType = buildingType;
		this.nickName = nickName;
		this.bounds = bounds;
		connectionList = new ArrayList<BuildingConnectionTemplate>(0);
	}

	public String getMissionName() {
		return missionName;
	}

	public void setMissionName(String name) {
		this.missionName = name;
	}

	public String getScenario() {
		return scenario;
	}

	/**
	 * Gets the building ID.
	 * 
	 * @return id.
	 */
	public int getID() {
		return bid;
	}

	/**
	 * Gets the building type.
	 * 
	 * @return building type.
	 */
	public String getBuildingType() {
		return buildingType;
	}

	/**
	 * Gets the building nickname.
	 * 
	 * @return building nickname.
	 */
	public String getNickName() {
		return nickName;
	}


	/**
	 * Gets the bounds of the building. Note: value in the individual attributes is -1 if not set in template.
	 * 
	 * @return Physical bounds of the building
	 */
	public BoundedObject getBounds() {
		return bounds;
	}

	/**
	 * Add a new building connection.
	 * 
	 * @param id        the unique id of the building being connected to.
	 * @param location the location (local to the building) (meters).
	 */
	public void addBuildingConnection(int id, LocalPosition location) {
		BuildingConnectionTemplate template = new BuildingConnectionTemplate(id, location);
		if (!connectionList.contains(template)) {
			connectionList.add(template);
		} else {
			throw new IllegalArgumentException(Msg.getString("BuildingTemplate.error.connectionAlreadyExists")); //$NON-NLS-1$
		}
	}

	public void addEVAAttachedBuildingID(int id) {
		eVAAttachedBuildingID = id;
	}
	
	public int getEVAAttachedBuildingID() {
		return eVAAttachedBuildingID;
	}
	
	/**
	 * Get a list of all building connection templates.
	 * 
	 * @return list of all building connection templates.
	 */
	public List<BuildingConnectionTemplate> getBuildingConnectionTemplates() {
		return Collections.unmodifiableList(connectionList);
	}

	/**
	 * Inner class to represent a building connection template.
	 */
	public class BuildingConnectionTemplate implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private int id;
		private LocalPosition location;

		/**
		 * Constructor.
		 * 
		 * @param id        the unique id of the building being connected to.
		 * @param xLocation the x axis location (local to the building) (meters).
		 * @param yLocation the y axis location (local to the building) (meters).
		 */
		private BuildingConnectionTemplate(int id, LocalPosition loc) {
			this.id = id;
			this.location = loc;
		}


		public int getID() {
			return id;
		}

		public LocalPosition getPosition() {
			return location;
		}

		@Override
		public boolean equals(Object otherObject) {
			boolean result = false;

			if (otherObject instanceof BuildingConnectionTemplate) {
				BuildingConnectionTemplate template = (BuildingConnectionTemplate) otherObject;
				if ((id == template.id) && location.equals(template.location)) {
					result = true;
				}
			}
			return result;
		}
		
		/**
		 * Gets the hash code for this object.
		 *
		 * @return hash code.
		 */
		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public int compareTo(BuildingTemplate o) {
		int compareId = ((BuildingTemplate) o).bid;
		return this.bid - compareId;
	}
}
