/*
 * Mars Simulation Project
 * CannedMarsMap.java
 * @date 2023-06-22
 * @author Greg Whelan
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Image;

import javax.swing.JComponent;

import com.mars_sim.core.data.Range;
import com.mars_sim.core.map.MapData;
import com.mars_sim.core.map.MapMetaData;
import com.mars_sim.core.map.location.Coordinates;

/**
 * The CannedMarsMap class reads in data from files in the map_data jar file in
 * order to generate a map image.
 */
@SuppressWarnings("serial")
public class CannedMarsMap extends JComponent implements MapDisplay {

	// Data members
	private boolean mapImageDone = false;

	private transient Image mapImage = null;
	
	private transient MapData mapData;

	private double rho;

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
	public MapMetaData getMapMetaData() {
		return mapData.getMetaData();
	}

	/**
	 * Creates a 2D map at a given center point.
	 * 
	 * @param newCenter the new center location
	 */
	public void drawMap(Coordinates newCenter, double rho) {	
		mapImage = mapData.createMapImage(newCenter.getPhi(), newCenter.getTheta(),
								MapPanel.MAP_BOX_WIDTH, MapPanel.MAP_BOX_HEIGHT, rho);
		this.rho = rho;
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
     * Gets the scale of the Mars surface map.
     * 
     * @return
     */
    public double getScale() {
		return rho/mapData.getRhoDefault();
	}
	   
	/**
	 * Gets the rho of the Mars surface map.
	 * 
	 * @return
	 */
	@Override
	public double getRho() {
		return rho;
	}
	
	/**
     * Gets the half angle of the Mars surface map.
     * 
     * @return
     */
	@Override
    public double getHalfAngle() {

    	double ha = Math.sqrt(HALF_MAP_ANGLE / getScale() / (0.25 + mapData.getResolution()));
    	return Math.min(Math.PI, ha);
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
	 * Gets the resolution of this map in the map stack
	 * 
	 * @return
	 */
	@Override
    public int getResolution() {
		return mapData.getResolution();
	}

	/**
	 * Prepares map panel for deletion.
	 */
	public void destroy() {
		mapImage = null;
		mapData = null;
	}

	@Override
	public Range getRhoRange() {
		return mapData.getRhoRange();
	}

	@Override
	public double getRhoDefault() {
		return mapData.getRhoDefault();
	}
}
