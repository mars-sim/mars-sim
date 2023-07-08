/*
 * Mars Simulation Project
 * CannedMarsMap.java
 * @date 2023-06-22
 * @author Greg Whelan
 */
package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Image;

import javax.swing.JComponent;

import org.mars.sim.mapdata.MapData;
import org.mars.sim.mapdata.MapMetaData;
import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.map.Map;

/**
 * The CannedMarsMap class reads in data from files in the map_data jar file in
 * order to generate a map image.
 */
@SuppressWarnings("serial")
public class CannedMarsMap extends JComponent implements Map {

	// Data members
	private boolean mapImageDone = false;
	
	private transient Image mapImage = null;
	
	private transient MapData mapData;

	/**
	 * Constructor.
	 * 
	 * @param displayArea the component display area.
	 * @param type The type name
	 * @param mapData     the map data.
	 */
	protected CannedMarsMap(JComponent displayArea, MapData mapData) {
		this.mapData = mapData;
	}

	/** 
	 * Gets meta details of the underlying map.
	 */
	@Override
	public MapMetaData getType() {
		return mapData.getMetaData();
	}

	/**
	 * Creates a map image for a given center location.
	 * 
	 * @param center 	The center location of the map display.
	 * @param scale 	The map scale
	 * @return the map image.
	 */
	private Image createMapImage(Coordinates center, double scale) {
		return mapData.getMapImage(center.getPhi(), center.getTheta(), MapPanel.MAP_BOX_WIDTH, MapPanel.MAP_BOX_HEIGHT, scale);
	}
	
	/**
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter the new center location
	 */
	public void drawMap(Coordinates newCenter, double scale) {	
		mapImage = createMapImage(newCenter, scale);
		mapImageDone = true;
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

	/**
     * Gets the magnification of the Mars surface map.
     * 
     * @return
     */
    public double getMagnification() {
		return mapData.getMagnification();
	}
	   
	/**
	 * Gets the rho of the Mars surface map.
	 * 
	 * @return
	 */
	@Override
	public double getRho() {
		return mapData.getRho();
	}
	
	/**
	 * Sets the map rho.
	 *
	 * @param rho
	 */
	public void setRho(double rho) {
		mapData.setRho(rho);
	}
	
	
	/**
	 * Gets the height of this map in pixels.
	 * 
	 * @return
	 */
	@Override
    public int getPixelHeight() {
		return mapData.getHeight();
	}

	/**
	 * Gets the width of this map in pixels.
	 * 
	 * @return
	 */
	@Override
    public int getPixelWidth() {
		return mapData.getWidth();
	}
	
	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
		mapImage = null;
		mapData = null;
	}
}
