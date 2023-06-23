/*
 * Mars Simulation Project
 * LandmarkConfig.java
 * @date 2023-06-22
 * @author Scott Davis
 */
package org.mars_sim.msp.core.environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;

/**
 * Provides configuration information about landmarks. Uses a DOM document to
 * get the information.
 */
public class LandmarkConfig implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Element names
	private static final String LANDMARK = "landmark";
	private static final String NAME = "name";
	private static final String LOCATION = "location";
	private static final String DESCRIPTION = "description";	
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String DIAMETER = "diameter";
	private static final String ORIGIN = "origin";
	private static final String TYPE = "type";

	private transient List<Landmark> landmarkList;

	/**
	 * Constructor.
	 * 
	 * @param landmarkDoc DOM document of landmark configuration.
	 */
	public LandmarkConfig(Document landmarkDoc) {
		buildLandmarkList(landmarkDoc);
	}

	/**
	 * Gets a list of landmarks.
	 * 
	 * @return list of landmarks
	 * @throws Exception when landmarks can not be parsed.
	 */
	public List<Landmark> getLandmarkList() {
		return landmarkList;
	}
	
	/**
	 * Builds the landmark list.
	 * 
	 * @param landmarkDoc
	 */
	private synchronized void buildLandmarkList(Document landmarkDoc) {
		if (landmarkList != null) {
			// just in case if another thread is being created
			return;
		}
			
		// Build the global list in a temp to avoid access before it is built
		List<Landmark> newList = new ArrayList<>();
		
		Element root = landmarkDoc.getRootElement();
		List<Element> landmarks = root.getChildren(LANDMARK);

		for (Element landmark : landmarks) {
			
			String name = "";

			// Get landmark name.
			name = landmark.getAttributeValue(NAME);

			// Get location.
			String locationString = landmark.getAttributeValue(LOCATION);
			
			// Get description.
			String description = landmark.getAttributeValue(DESCRIPTION);
			
			// Get diameter.
			int diameter = (int) Float.parseFloat(landmark.getAttributeValue(DIAMETER));

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

			// Get origin (we don't want this to be uppercase).
			String origin = landmark.getAttributeValue(ORIGIN);

			// Get type.
			String type = landmark.getAttributeValue(TYPE).toUpperCase();

			// Create landmark.
			newList.add(new Landmark(name, description, locationString, location, diameter, origin, type));
		}
		
		// Assign the newList now built
		landmarkList = Collections.unmodifiableList(newList);
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		if (landmarkList != null) {
			landmarkList = null;
		}
	}
}
