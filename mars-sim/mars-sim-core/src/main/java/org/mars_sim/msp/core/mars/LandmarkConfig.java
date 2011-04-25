/**
 * Mars Simulation Project
 * LandmarkConfig.java
 * @version 3.01 2011-04-21
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import org.jdom.Document;
import org.jdom.Element;
import org.mars_sim.msp.core.Coordinates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides configuration information about landmarks. Uses a DOM document to
 * get the information.
 */
public class LandmarkConfig implements Serializable {

    // Element names
    private static final String LANDMARK = "landmark";
    private static final String NAME = "name";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";

    private Document landmarkDoc;
    private List<Landmark> landmarkList;

    /**
     * Constructor
     * @param landmarkDoc DOM document of landmark configuration.
     */
    public LandmarkConfig(Document landmarkDoc) {
        this.landmarkDoc = landmarkDoc;
    }

    /**
     * Gets a list of landmarks.
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

                // Get latitude.
                String latitude = landmark.getAttributeValue(LATITUDE);

                // Get longitude.
                String longitude = landmark.getAttributeValue(LONGITUDE);

                // Create location coordinate.
                Coordinates location = new Coordinates(latitude, longitude);

                // Create landmark.
                landmarkList.add(new Landmark(name, location));
            }
        }

        return landmarkList;
    }
}