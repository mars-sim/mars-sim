/**
 * Mars Simulation Project
 * LandmarkConfig.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String DIAMETER = "diameter";
	private static final String APPROVALDATE = "approvaldate";
	private static final String ORIGIN = "origin";
	private static final String TYPE = "type";

	private Document landmarkDoc;
	private List<Landmark> landmarkList;

	/**
	 * Constructor
	 * 
	 * @param landmarkDoc DOM document of landmark configuration.
	 */
	public LandmarkConfig(Document landmarkDoc) {
		this.landmarkDoc = landmarkDoc;
	}

	/**
	 * Gets a list of landmarks.
	 * 
	 * @return list of landmarks
	 * @throws Exception when landmarks can not be parsed.
	 */
	@SuppressWarnings("unchecked")
	public List<Landmark> getLandmarkList() {

		if (landmarkList == null) {
			landmarkList = new ArrayList<Landmark>();

			Element root = landmarkDoc.getRootElement();
			List<Element> landmarks = root.getChildren(LANDMARK);

			for (Element landmark : landmarks) {
				String name = "";

				// Get landmark name.
				name = landmark.getAttributeValue(NAME);

				// Get diameter.
				int diameter = (int) Float.parseFloat(landmark.getAttributeValue(DIAMETER));

				// Get latitude.
				String latitude = landmark.getAttributeValue(LATITUDE).toUpperCase();

				// Get longitude.
				String longitude = landmark.getAttributeValue(LONGITUDE).toUpperCase();

				// take care to internationalize the coordinates
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
				landmarkList.add(new Landmark(name, location, diameter, origin, type));
			}
		}

		return landmarkList;
	}

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		landmarkDoc = null;
		if (landmarkList != null) {

			landmarkList.clear();
			landmarkList = null;
		}
	}
}