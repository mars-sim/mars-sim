/*
 * Mars Simulation Project
 * MoonConfig.java
 * @date 2026-05-05
 * @author Manny Kung
 */
package com.mars_sim.core.moon;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.tool.Msg;

/**
 * Provides configuration information about moon colonies. Uses a DOM document to
 * get the information.
 */
public class MoonConfig {

	// Element names
	private static final String BASE = "base";
	private static final String BASE_NAME = "name";
	private static final String AGENCY = "agency";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	
	private List<ColonySpec> colonySpecs;

	/**
	 * Constructor.
	 * 
	 * @param landmarkDoc DOM document of landmark configuration.
	 */
	public MoonConfig(Document doc) {
		buildColonyList(doc);
	}

	/**
	 * Gets a landmarks.
	 * 
	 * @return Manager of landmarks
	 */
	public List<ColonySpec> getColonySpecs() {
		return colonySpecs;
	}
	
	/**
	 * Builds the colony list.
	 * 
	 * @param colonyDoc
	 */
	private synchronized void buildColonyList(Document colonyDoc) {
		if (colonySpecs != null) {
			// just in case if another thread is being created
			return;
		}
			
		// Build the global list in a temp to avoid access before it is built
		List<ColonySpec> colonies = new ArrayList<>();
		
		Element root = colonyDoc.getRootElement();
		List<Element> landmarks = root.getChildren(BASE);

		for (Element landmark : landmarks) {
			
			String name = "";

			// Get base name.
			name = landmark.getAttributeValue(BASE_NAME);

			// Get agency.
			String agency = landmark.getAttributeValue(AGENCY);

			// Get latitude.
			String latitude = landmark.getAttributeValue(LATITUDE).toUpperCase();

			// Get longitude.
			String longitude = landmark.getAttributeValue(LONGITUDE).toUpperCase();

			latitude = latitude.replace("N", Msg.getString("direction.northShort")); //$NON-NLS-1$ //$NON-NLS-2$
			latitude = latitude.replace("S", Msg.getString("direction.southShort")); //$NON-NLS-1$ //$NON-NLS-2$
			longitude = longitude.replace("E", Msg.getString("direction.eastShort")); //$NON-NLS-1$ //$NON-NLS-2$
			longitude = longitude.replace("W", Msg.getString("direction.westShort")); //$NON-NLS-1$ //$NON-NLS-2$

			// Create location coordinate.
			Coordinates location = new Coordinates(latitude, longitude);

			// Create landmark.
			colonies.add(new ColonySpec(name, agency, location));
		}
		
		// Make the new colonySpecs visible
		colonySpecs = colonies;
	}
}
