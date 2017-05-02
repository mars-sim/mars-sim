/**
 * Mars Simulation Project
 * BuildingTemplate.java
 * @version 3.08 2015-12-16
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mars_sim.msp.core.Msg;

/**
 * A building template information.
 */
// 2014-12-23 Added Comparable<BuildingTemplate>
public class BuildingTemplate
implements Serializable , Comparable<BuildingTemplate>{

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	//private static Logger logger = Logger.getLogger(BuildingTemplate.class.getName());

	// Data members

	/** an unique template id for this particular settlement template */
	private int id;
	/**
	 * an id for this building in a particular building type. e.g. ERV Base 1, ERV Base 2.
	 */
	//private int typeID;

	private double width;
	private double length;
	private double xLoc;
	private double yLoc;
	private double facing;

	private String buildingType;
    // 2014-10-28 Added nickName
	private String nickName;
	private String scenario;

	private String missionName;

	private List<BuildingConnectionTemplate> connectionList;


	/*
	 * * BuildingTemplate Constructor.
	 */
	// May also be called by ResupplyConfig.java when buildings arrived
	// 2014-10-27 Added nickName
	public BuildingTemplate(String missionName, int id, String scenario, String buildingType, String nickName,
		double width, double length,
		double xLoc, double yLoc,
		double facing) {

		this.missionName = missionName;
		this.id = id;
		this.scenario = scenario;
		this.buildingType = buildingType;
		this.nickName = nickName;
		this.width = width;
		this.length = length;
		this.xLoc = xLoc;
		this.yLoc = yLoc;
		this.facing = facing;
		connectionList = new ArrayList<BuildingConnectionTemplate>(0);
	}

	// 2015-12-16 Added missionName
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
	 * Gets the building template's unique ID.
	 * @return id.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Gets the building type.
	 * @return building type.
	 */
	// 2014-10-27 Switched from "name" to "buildingType"
	// 2014-12-26 Changed method name from getType() to getBuildingType() for consistency
	public String getBuildingType() {
		return buildingType;
	}

	/**
	 * Gets the building nickname.
	 * @return building nickname.
	 */
	//2014-10-27 Added getNickName()
	public String getNickName() {
		return nickName;
	}

	/**
	 * Gets the width of the building.
	 * Note: value is -1 if not set in template.
	 * @return width (meters) of building or -1 if not set.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Gets the length of the building.
	 * Note: value is -1 if not set in template.
	 * @return length (meters) of building or -1 if not set.
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Gets the x location of the building in the settlement.
	 * @return x location (meters from settlement center - West: positive, East: negative).
	 */
	public double getXLoc() {
		return xLoc;
	}

	/**
	 * Gets the y location of the building in the settlement.
	 * @return y location (meters from settlement center - North: positive, South: negative).
	 */
	public double getYLoc() {
		return yLoc;
	}

	/**
	 * Gets the facing of the building.
	 * @return facing (degrees from North clockwise).
	 */
	public double getFacing() {
		return facing;
	}

	/**
	 * Add a new building connection.
	 * @param id the unique id of the building being connected to.
	 * @param xLocation the x axis location (local to the building) (meters).
	 * @param yLocation the y axis location (local to the building) (meters).
	 */
	public void addBuildingConnection(int id, double xLocation, double yLocation) {
		BuildingConnectionTemplate template = new BuildingConnectionTemplate(id, xLocation, yLocation);
		if (!connectionList.contains(template)) {
			connectionList.add(template);
		}
		else {
			throw new IllegalArgumentException(Msg.getString("BuildingTemplate.error.connectionAlreadyExists")); //$NON-NLS-1$
		}
	}

	/**
	 * Get a list of all building connection templates.
	 * @return list of all building connection templates.
	 */
	public List<BuildingConnectionTemplate> getBuildingConnectionTemplates() {
		return new ArrayList<BuildingConnectionTemplate>(connectionList);
	}

	/**
	 * Inner class to represent a building connection template.
	 */
	public class BuildingConnectionTemplate
	implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members
		private int id;
		private double xLocation;
		private double yLocation;

		/**
		 * Constructor.
		 * @param id the unique id of the building being connected to.
		 * @param xLocation the x axis location (local to the building) (meters).
		 * @param yLocation the y axis location (local to the building) (meters).
		 */
		private BuildingConnectionTemplate(int id, double xLocation, double yLocation) {
			this.id = id;
			this.xLocation = xLocation;
			this.yLocation = yLocation;
		}

		public int getID() {
			return id;
		}

		public double getXLocation() {
			return xLocation;
		}

		public double getYLocation() {
			return yLocation;
		}

		@Override
		public boolean equals(Object otherObject) {
			boolean result = false;

			if (otherObject instanceof BuildingConnectionTemplate) {
				BuildingConnectionTemplate template = (BuildingConnectionTemplate) otherObject;
				if ((id == template.id) && (xLocation == template.xLocation) &&
						(yLocation == template.yLocation)) {
					result = true;
				}
			}
			return result;
		}
	}

    // 2014-12-23 Added compareTo()
	public int compareTo(BuildingTemplate o) {
		int compareId = ((BuildingTemplate) o).id;
		return this.id - compareId ;
	}

}