/*
 * Mars Simulation Project
 * MineralMapLayer.java
 * @date 2023-06-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.map.Map;
import org.mars.sim.mapdata.map.MapLayer;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.environment.MineralMap;
import org.mars_sim.msp.core.tool.SimulationConstants;

/**
 * A map layer showing mineral concentrations.
 */
public class MineralMapLayer implements MapLayer, SimulationConstants {

 	private static final Logger logger = Logger.getLogger(MineralMapLayer.class.getName());

	// Domain members
	private boolean updateLayer;

	private int numMineralsCache;

	private double rhoCache;

	private String mapTypeCache;
	
	private int[] mineralConcentrationArray;

	private Component displayComponent;
	private Image mineralConcentrationMap;

	private Coordinates mapCenterCache;
	
	private MineralMap mineralMap;
	
	private java.util.Map<String, Color> mineralColorMap;

	private java.util.Set<String> mineralsDisplaySet = new HashSet<>();
	
	private java.util.Map<String, Color> mineralColors;
	
	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public MineralMapLayer(Component displayComponent) {
		mineralMap = Simulation.instance().getSurfaceFeatures().getMineralMap();
		this.displayComponent = displayComponent;
		mineralConcentrationArray = new int[Map.MAP_BOX_WIDTH * Map.MAP_BOX_HEIGHT];
		
		mineralColors = getMineralColors();
	}
	
	/**
	 * Displays the layer on the map image.
	 * 
	 * @param mapCenter the location of the center of the map.
	 * @param baseMap   the type of map.
	 * @param g         graphics context of the map display.
	 */
	public void displayLayer(Coordinates mapCenter, Map baseMap, Graphics g) {
		
		if (mineralsDisplaySet.isEmpty()) {
			return;
		}
		
		Graphics2D g2d = (Graphics2D) g;
		
		String mapType = baseMap.getType().getId();

		double rho = baseMap.getRho();
		
		int numMinerals = mineralsDisplaySet.size();
		
		if (!mapCenter.equals(mapCenterCache) || !mapType.equals(mapTypeCache) 
				|| updateLayer || rho != rhoCache || numMineralsCache != numMinerals) {
			
			mapCenterCache = mapCenter;
			rhoCache = rho;
			numMineralsCache = numMinerals;

			mapTypeCache = mapType;
			updateLayer = false;

			if (mineralColors == null)
				mineralColors = getMineralColors();
			
			// Clear map concentration array.
			Arrays.fill(mineralConcentrationArray, 0);

			double centerX = Map.HALF_MAP_BOX;
			double centerY = Map.HALF_MAP_BOX;
	
			double mag = baseMap.getMagnification();
				
			for (int x = 0; x < Map.MAP_BOX_WIDTH; x = x + 2) {
				
				for (int y = 0; y < Map.MAP_BOX_HEIGHT; y = y + 2) {

					// param (x - centerX) varies as x goes from 0 to MAP_VIS_WIDTH
					// param (y - centerY) varies as y goes from 0 to MAP_VIS_HEIGHT
					
					java.util.Map<String, Double> mineralConcentrations = mineralMap
									.getSomeMineralConcentrations(mineralsDisplaySet, mapCenter.convertRectToSpherical(x - centerX, y - centerY, rho), mag);
								
					if (mineralConcentrations.isEmpty()) {
						continue;
					}
					
					Iterator<String> i = mineralConcentrations.keySet().iterator();
					while (i.hasNext()) {
						String mineralType = i.next();
						if (isMineralDisplayed(mineralType)) {
//							logger.info(mineralType + " is being drawn.");
							double concentration = mineralConcentrations.get(mineralType);
							if (concentration <= 0) {
								continue;
							}
							Color baseColor = mineralColors.get(mineralType);
							int index = x + (y * Map.MAP_BOX_WIDTH);
							addColorToMineralConcentrationArray(index, baseColor, concentration);
							addColorToMineralConcentrationArray((index + 1), baseColor, concentration);
							if (y < Map.MAP_BOX_HEIGHT - 1) {
								int indexNextLine = x + ((y + 1) * Map.MAP_BOX_WIDTH);
								addColorToMineralConcentrationArray(indexNextLine, baseColor, concentration);
								addColorToMineralConcentrationArray((indexNextLine + 1), baseColor,
										concentration);
							}
						}
					}	
				}
			}

			// Create mineral concentration image for map
			mineralConcentrationMap = displayComponent.createImage(new MemoryImageSource(Map.MAP_BOX_WIDTH,
					Map.MAP_BOX_HEIGHT, mineralConcentrationArray, 0, Map.MAP_BOX_WIDTH));

			MediaTracker mt = new MediaTracker(displayComponent);
			mt.addImage(mineralConcentrationMap, 0);
			try {
				mt.waitForID(0);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "MineralMapLayer interrupted: " + e);
				// Restore interrupted state
			    Thread.currentThread().interrupt();
			}
		}

		// Draw the mineral concentration image
		g2d.drawImage(mineralConcentrationMap, 0, 0, displayComponent);
	}

	/**
	 * Adds a color to the mineral concentration array.
	 * 
	 * @param index         the index of the pixel in the array.
	 * @param color         the mineral color.
	 * @param concentration the amount of concentration (0% - 100.0%).
	 */
	private void addColorToMineralConcentrationArray(int index, Color color, double concentration) {
		int concentrationInt = (int) (255 * (concentration / 100D));
		int concentrationColor = (concentrationInt << 24) | (color.getRGB() & 0x00FFFFFF);
		int currentColor = mineralConcentrationArray[index];
		mineralConcentrationArray[index] = currentColor | concentrationColor;
	}

	/**
	 * Gets a map of all mineral type names and their display colors.
	 * 
	 * @return map of names and colors.
	 */
	public java.util.Map<String, Color> getMineralColors() {
		
		if (mineralColorMap == null || mineralColorMap.isEmpty()) {
			String[] mineralNames = mineralMap.getMineralTypeNames();
			java.util.Map<String, Color> map = new HashMap<>(mineralNames.length);
			for (int x = 0; x < mineralNames.length; x++) {
				String mineralTypeName = mineralMap.getMineralTypeNames()[x];
				// Determine color of a mineral
				int mineralColor = Color.HSBtoRGB(((float) x / (float) mineralNames.length), .9F, .9F);
				map.put(mineralTypeName, new Color(mineralColor));
			}
			
			mineralColorMap = map;
			return map;
		}
		return mineralColorMap;
	}

	
	/**
	 * Updates the set of minerals to be display.
	 * 
	 * @param availableMinerals
	 * @return
	 */
	private Set<String> updateMineralDisplay(Set<String> availableMinerals) {

		Set<String> intersection = new HashSet<>();
		availableMinerals.forEach((i) -> {
			if (mineralsDisplaySet.contains(i))
				intersection.add(i);
		});
		
		return intersection;
	}


	/**
	 * Checks if a mineral type is displayed on the map.
	 * 
	 * @param mineralType the mineral type to display.
	 * @return true if displayed.
	 */
	public boolean isMineralDisplayed(String mineralType) {
		if (mineralsDisplaySet.contains(mineralType))
			return true;
		else
			return false;
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
		mineralConcentrationMap = null;
		mapCenterCache = null;
		mineralMap = null;
		mineralsDisplaySet.clear();
		mineralsDisplaySet = null;
	}
}
