/*
 * Mars Simulation Project
 * MineralMapLayer.java
 * @date 2024-08-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.environment.MineralMap;
import com.mars_sim.core.map.location.Coordinates;

/**
 * A map layer showing mineral concentrations.
 */
public class MineralMapLayer implements MapLayer {

	// Domain members
	private boolean updateLayer;

	private int numMineralsCache;

	private double rhoCache;
	
	private static final int MAP_BOX_HEIGHT = MapDisplay.MAP_BOX_HEIGHT;
	private static final int MAP_BOX_WIDTH = MapDisplay.MAP_BOX_WIDTH;
	
	private String mapTypeCache;
	
	private int[] mineralArrayCache;

	private Component displayComponent;
	
	private BufferedImage mineralImage;

	private Coordinates mapCenterCache;
	
	private MineralMap mineralMap;
	
	private SortedMap<String, Color> mineralColorMap;

	private Set<String> mineralsDisplaySet = new HashSet<>();
	
	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public MineralMapLayer(Component displayComponent) {
		mineralMap = Simulation.instance().getSurfaceFeatures().getMineralMap();
		this.displayComponent = displayComponent;
	
		mineralColorMap = getMineralColors();
	}
	
	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g2d       graphics context of the map display.
	 */
	@Override
	public List<MapHotspot> displayLayer(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d) {
		
		if (mineralsDisplaySet.isEmpty()) {
			return Collections.emptyList();
		}

		boolean isChanging = ((MapPanel)displayComponent).isChanging();
		
		double rho = baseMap.getRho();
		
		if (isChanging) {
			return Collections.emptyList();
		}
		
		String mapType = baseMap.getMapMetaData().getId();		
		int numMinerals = mineralsDisplaySet.size();
		if (mapCenterCache == null || !mapCenter.equals(mapCenterCache) || !mapType.equals(mapTypeCache) 
				|| updateLayer || rhoCache != rho || numMineralsCache != numMinerals) {
				
			mapTypeCache = mapType;
			updateLayer = false;
			
			int[] newMineralArray = new int[MAP_BOX_WIDTH * MAP_BOX_HEIGHT];

			double mag = baseMap.getScale();
	
			boolean hasMinerals = false;
			
			for (int y = 0; y < MAP_BOX_HEIGHT; y = y + 2) {
				for (int x = 0; x < MAP_BOX_WIDTH; x = x + 2) {
			
					int index = x + y * MAP_BOX_WIDTH;
					
					var point = baseMap.getMapBoxPoint(index);
					if (point == null)
						continue;

					Map<String, Integer> mineralConcentrations = 
							mineralMap.getSomeMineralConcentrations(
										mineralsDisplaySet, 
										point.phi(), 
										point.theta(),
										mag);
									
					if (mineralConcentrations != null && !mineralConcentrations.isEmpty()) {
		
						computeColorMineralArray(mineralConcentrations, newMineralArray, x, y);
						
						hasMinerals = true;
					}
				}
			}
			
			mapCenterCache = mapCenter;
			
			if (hasMinerals)
				mineralArrayCache = newMineralArray;
			
			numMineralsCache = numMinerals;

			rhoCache = rho;
			
			// Create a new buffered image to draw the map on.
			mineralImage = new BufferedImage(MapDisplay.MAP_BOX_WIDTH, MapDisplay.MAP_BOX_HEIGHT, 
	 				BufferedImage.TYPE_INT_ARGB);
	 		
	 		// Create new map image.
			mineralImage.setRGB(0, 0, MapDisplay.MAP_BOX_WIDTH, MapDisplay.MAP_BOX_HEIGHT, newMineralArray, 0, MapDisplay.MAP_BOX_WIDTH);

		}
		
		// Draw the mineral concentration image
		g2d.drawImage(mineralImage, 0, 0, displayComponent);
			
		return Collections.emptyList();
	}

	/**
	 * Computes color for the concentration array.
	 * 
	 * @param mineralConcentrations
	 * @param newMineralArray
	 * @param x
	 * @param y
	 */
	private void computeColorMineralArray(java.util.Map<String, Integer> mineralConcentrations, int[] newMineralArray, int x, int y) {
	
		Iterator<String> i = mineralConcentrations.keySet().iterator();
		while (i.hasNext()) {
			String mineralType = i.next();
			
			if (isMineralDisplayed(mineralType)) {
				double concentration = mineralConcentrations.get(mineralType);
				if (concentration <= 0) {
					continue;
				}
				Color baseColor = mineralColorMap.get(mineralType);
				int index = x + (y * MapDisplay.MAP_BOX_WIDTH);
				addColorToMineralConcentrationArray(index, baseColor, concentration, newMineralArray);
				addColorToMineralConcentrationArray((index + 1), baseColor, concentration, newMineralArray);
				
				if (y < MapDisplay.MAP_BOX_HEIGHT - 1) {
					int indexNextLine = x + ((y + 1) * MapDisplay.MAP_BOX_WIDTH);
					addColorToMineralConcentrationArray(indexNextLine, baseColor, concentration, newMineralArray);
					addColorToMineralConcentrationArray((indexNextLine + 1), baseColor,
							concentration, newMineralArray);
				}
			}
		}
	}
	
	/**
	 * Adds a color to the mineral concentration array.
	 * 
	 * @param index         the index of the pixel in the array.
	 * @param color         the mineral color.
	 * @param concentration the amount of concentration (0% - 100.0%).
	 */
	private void addColorToMineralConcentrationArray(int index, Color color, double concentration, int[] newMineralArray) {
		int concentrationInt = (int) (255 * (concentration / 100D));
		int concentrationColor = (concentrationInt << 24) | (color.getRGB() & 0x00FFFFFF);
		int currentColor = newMineralArray[index];
		newMineralArray[index] = currentColor | concentrationColor;
	}

	/**
	 * Gets a map of all mineral type names and their display colors.
	 * 
	 * @return map of names and colors.
	 */
	public SortedMap<String, Color> getMineralColors() {
		
		if (mineralColorMap == null || mineralColorMap.isEmpty()) {
			String[] mineralNames = mineralMap.getMineralTypeNames();
			int num = mineralNames.length;
			SortedMap<String, Color> map = new TreeMap<>();
			for (int x = 0; x < num ; x++) {
				String mineralTypeName = mineralMap.getMineralTypeNames()[x];
				// Determine color of a mineral
				// See https://stackoverflow.com/questions/44326765/color-mapping-for-specific-range
//				float value = 1f * x / num; //this is your value between 0 and 1
//				float minHue = 255f/255; // 300f corresponds to Magenta // 120f corresponds to Green
//				float maxHue = 0; //corresponds to red
//				float hue = value * maxHue + (1 - value) * minHue; // ((float) x + 1 / (float) mineralNames.length)
//				int mineralColor = Color.HSBtoRGB(hue, .9F, .9F)
				
				String rgbString = mineralMap.getColorString(mineralTypeName);
						
				Color rgbColor = Color.decode(rgbString);
				
				map.put(mineralTypeName, rgbColor);
			}
			
			mineralColorMap = map;
			return map;
		}
		return mineralColorMap;
	}


	/**
	 * Checks if a mineral type is displayed on the map.
	 * 
	 * @param mineralType the mineral type to display.
	 * @return true if displayed.
	 */
	public boolean isMineralDisplayed(String mineralType) {
		return mineralsDisplaySet.contains(mineralType);
	}

	/**
	 * Sets a mineral type to be displayed on the map or not.
	 * 
	 * @param mineralType the mineral type to display.
	 * @param displayed   true if displayed, false if not.
	 */
	public void setMineralDisplayed(String mineralType, boolean displayed) {
		if (mineralsDisplaySet.isEmpty()) {
			if (displayed) {
				mineralsDisplaySet.add(mineralType);	
				updateLayer = true;
			}
		}
		else {
			if (displayed) {
				mineralsDisplaySet.add(mineralType);	
				updateLayer = true;
			}
			else {
				if (mineralsDisplaySet.contains(mineralType)) {
					mineralsDisplaySet.remove(mineralType);
					updateLayer = true;
				}
			}
		}
	}
	
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {

		displayComponent = null;
		mineralImage = null;
		mapCenterCache = null;
		mineralMap = null;
		mineralsDisplaySet.clear();
		mineralsDisplaySet = null;
	}
}
