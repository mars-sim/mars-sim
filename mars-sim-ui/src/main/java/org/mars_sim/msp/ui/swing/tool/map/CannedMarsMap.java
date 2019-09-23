/**
 * Mars Simulation Project
 * CannedMarsMap.java
 * @version 3.1.0 2017-10-05
 * @author Greg Whelan
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Image;
import java.awt.MediaTracker;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.mars_sim.mapdata.MapData;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;

/**
 * The CannedMarsMap class reads in data from files in the map_data jar file in
 * order to generate a map image.
 */
public abstract class CannedMarsMap implements Map {

	/** default logger. */
	private static Logger logger = Logger.getLogger(CannedMarsMap.class.getName());

	// Data members
	private boolean mapImageDone = false;
	
	private MapData mapData;
	private JComponent displayArea = null;
	private Coordinates currentCenter = null;
	private Image mapImage = null;


	/**
	 * Constructor.
	 * 
	 * @param displayArea the component display area.
	 * @param mapData     the map data.
	 */
	public CannedMarsMap(JComponent displayArea, MapData mapData) {
		this.mapData = mapData;
		this.displayArea = displayArea;
	}

	/**
	 * Creates a map image for a given center location.
	 * 
	 * @param center the center location of the map display.
	 * @return the map image.
	 */
	private Image createMapImage(Coordinates center) {
		return mapData.getMapImage(center.getPhi(), center.getTheta());
	}

	/**
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter the new center location
	 */
	public void drawMap(Coordinates newCenter) {
		if ((newCenter != null) && (!newCenter.equals(currentCenter))) {
			mapImage = createMapImage(newCenter);

			MediaTracker mt = new MediaTracker(displayArea);
			mt.addImage(mapImage, 0);
			try {
				mt.waitForID(0);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, Msg.getString("CannedMarsMap.log.mediaTrackerInterrupted") + e); //$NON-NLS-1$
			}
			mapImageDone = true;
			currentCenter = new Coordinates(newCenter);
		}
	}

	/**
	 * Checks if a requested map is complete.
	 * 
	 * @return true if requested map is complete
	 */
	@Override
	public boolean isImageDone() {
		return mapImageDone;
	}

	/**
	 * Gets the constructed map image.
	 * 
	 * @return constructed map image
	 */
	@Override
	public Image getMapImage() {
		return mapImage;
	}
}