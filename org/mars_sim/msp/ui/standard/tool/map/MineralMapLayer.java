/**
 * Mars Simulation Project
 * MineralMapLayer.java
 * @version 2.84 2008-03-31
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.MineralMap;

/**
 * A map layer showing mineral concentrations.
 */
public class MineralMapLayer implements MapLayer {

    private static String CLASS_NAME = "org.mars_sim.msp.ui.standard.tool.map.MineralMapLayer";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Domain members
	private int[] mineralConcentrationArray;
    private Component displayComponent;
    private Image mineralConcentrationMap;
    private Coordinates mapCenterCache;
    private String mapTypeCache;
    private boolean updateLayer;
    private java.util.Map<String, Boolean> mineralsDisplayedMap;
	
    /**
     * Constructor
     * @param displayComponent the display component.
     */
    public MineralMapLayer(Component displayComponent) {
    	this.displayComponent = displayComponent;
    	mineralConcentrationArray = new int[Map.DISPLAY_WIDTH * Map.DISPLAY_HEIGHT];
    	updateMineralsDisplayed();
    }
    
	/**
     * Displays the layer on the map image.
     * @param mapCenter the location of the center of the map.
     * @param mapType the type of map.
     * @param g graphics context of the map display.
     */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		
		if (!mapCenter.equals(mapCenterCache) || !mapType.equals(mapTypeCache) || updateLayer) {
			mapCenterCache = new Coordinates(mapCenter);
			mapTypeCache = mapType;
			updateLayer = false;
			
			// Clear map concentration array.
			Arrays.fill(mineralConcentrationArray, 0);
		
			int centerX = 150;
			int centerY = 150;
        
			double rho = 0D;
			if (USGSMarsMap.TYPE.equals(mapType)) rho = USGSMarsMap.PIXEL_RHO;
			else rho = CannedMarsMap.PIXEL_RHO;
        
			MineralMap mineralMap = Simulation.instance().getMars().getSurfaceFeatures().getMineralMap();
			java.util.Map<String, Color> mineralColors = getMineralColors();
			updateMineralsDisplayed();
			
			Coordinates location = new Coordinates(0D, 0D);
			for (int x = 0; x < Map.DISPLAY_WIDTH; x+=2) {
				for (int y = 0; y < Map.DISPLAY_HEIGHT; y+=2) {
					mapCenter.convertRectToSpherical(x - centerX, y - centerY, rho, location);
					java.util.Map<String, Double> mineralConcentrations = 
						mineralMap.getAllMineralConcentrations(location);
					if (mineralConcentrations.size() > 0) {
						Iterator<String> i = mineralConcentrations.keySet().iterator();
						while (i.hasNext()) {
							String mineralType = i.next();
							if (isMineralDisplayed(mineralType)) {
								double concentration = mineralConcentrations.get(mineralType);
								if (concentration > 0D) {
									Color baseColor = mineralColors.get(mineralType);
									int index = x + (y * Map.DISPLAY_WIDTH);
									addColorToMineralConcentrationArray(index, baseColor, concentration);
									addColorToMineralConcentrationArray((index + 1), baseColor, concentration);
									if (y < Map.DISPLAY_HEIGHT -1) {
										int indexNextLine = x + ((y + 1) * Map.DISPLAY_WIDTH);
										addColorToMineralConcentrationArray(indexNextLine, baseColor, concentration);
										addColorToMineralConcentrationArray((indexNextLine + 1), baseColor, concentration);
									}
								}
							}
						}
					}
				}
			}
        
			// Create mineral concentration image for map
			mineralConcentrationMap = displayComponent.createImage(
					new MemoryImageSource(Map.DISPLAY_WIDTH, Map.DISPLAY_HEIGHT, 
							mineralConcentrationArray, 0, Map.DISPLAY_WIDTH));

			MediaTracker mt = new MediaTracker(displayComponent);
			mt.addImage(mineralConcentrationMap, 0);
			try {
				mt.waitForID(0);
			}
			catch (InterruptedException e) {
				logger.log(Level.SEVERE,"MineralMapLayer interrupted: " + e);
			}
		}

        // Draw the mineral concentration image
        g.drawImage(mineralConcentrationMap, 0, 0, displayComponent);
	}

	/**
	 * Adds a color to the mineral concentration array.
	 * @param index the index of the pixel in the array.
	 * @param color the mineral color.
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
	 * @return map of names and colors.
	 */
	public java.util.Map<String, Color> getMineralColors() {
		MineralMap mineralMap = Simulation.instance().getMars().getSurfaceFeatures().getMineralMap();
		String[] mineralNames = mineralMap.getMineralTypeNames();
		java.util.Map<String, Color> result = new HashMap<String, Color>(mineralNames.length);
		for (int x = 0; x < mineralNames.length; x++) {
			String mineralTypeName = mineralMap.getMineralTypeNames()[x];
			int mineralColor = Color.HSBtoRGB(((float) x / (float) mineralNames.length), 1F, 1F);
			result.put(mineralTypeName, new Color(mineralColor));
		}
		return result;
	}
	
	/**
	 * Update which minerals to display on the map if they've changed.
	 */
	private void updateMineralsDisplayed() {
		MineralMap mineralMap = Simulation.instance().getMars().getSurfaceFeatures().getMineralMap();
		String[] mineralNames = mineralMap.getMineralTypeNames();
		Arrays.sort(mineralNames);
		if (mineralsDisplayedMap == null) mineralsDisplayedMap = new HashMap<String, Boolean>(mineralNames.length);
		String[] currentMineralNames = mineralsDisplayedMap.keySet().toArray(new String[mineralsDisplayedMap.size()]);
		Arrays.sort(currentMineralNames);
		if (!Arrays.equals(mineralNames, currentMineralNames)) {
			mineralsDisplayedMap.clear();
			for (int x = 0; x < mineralNames.length; x++)
				mineralsDisplayedMap.put(mineralNames[x], true);
		}
	}
	
	/**
	 * Checks if a mineral type is displayed on the map.
	 * @param mineralType the mineral type to display.
	 * @return true if displayed.
	 */
	public boolean isMineralDisplayed(String mineralType) {
		if ((mineralsDisplayedMap != null) && 
				mineralsDisplayedMap.containsKey(mineralType)) 
			return mineralsDisplayedMap.get(mineralType);
		else return false;
	}
	
	/**
	 * Sets a mineral type to be displayed on the map or not.
	 * @param mineralType the mineral type to display.
	 * @param displayed true if displayed, false if not.
	 */
	public void setMineralDisplayed(String mineralType, boolean displayed) {
		if ((mineralsDisplayedMap != null) && (isMineralDisplayed(mineralType) != displayed)) {
			mineralsDisplayedMap.put(mineralType, displayed);
			updateLayer = true;
		}
	}
}