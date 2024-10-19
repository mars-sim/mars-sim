/*
 * Mars Simulation Project
 * MineralMapLayer.java
 * @date 2024-08-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.mineral.MineralConcentration;
import com.mars_sim.core.mineral.MineralMap;
import com.mars_sim.core.mineral.MineralType;

/**
 * A map layer showing mineral concentrations.
 */
public class MineralMapLayer extends SurfaceFeatureLayer<MineralConcentration> {

	/**
	 * Create a tooltip for a mineral concentation showing the details
	 */
	private static class MineralHotspot extends MapHotspot {

		private MineralConcentration conc;

		protected MineralHotspot(IntPoint center, int radius, MineralConcentration conc) {
			super(center, radius);
			this.conc = conc;
		}

		@Override
		public String getTooltipText() {
			var body = conc.getConcentrations().entrySet().stream()
					.map(e -> e.getKey() + " : " + e.getValue() + "%")
					.collect(Collectors.joining("<br>>"));

			return "<html>" + body + "</html>";
		}
	}

	private double rhoCache;
	
	private static final int MAP_BOX_HEIGHT = MapDisplay.MAP_BOX_HEIGHT;
	private static final int MAP_BOX_WIDTH = MapDisplay.MAP_BOX_WIDTH;

	private static final int CIRCLE_RADIUS = 4;
	private static final int CIRCLE_DIAMETER = (2 * CIRCLE_RADIUS);
	
	private String mapTypeCache;
	
	private MapPanel displayComponent;
	
	private BufferedImage mineralImage;

	private Coordinates mapCenterCache;
	
	private MineralMap mineralMap;
	
	private Map<String, Color> mineralColorMap;

	private Set<String> mineralsDisplaySet = new HashSet<>();

	private int numMineralsCache;

	private int[] mineralArrayCache;


	
	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public MineralMapLayer(MapPanel displayComponent) {
		super("Mineral");
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
	public List<MapHotspot> displayLayerOld(Coordinates mapCenter, MapDisplay baseMap, Graphics2D g2d) {
		
		if (mineralsDisplaySet.isEmpty()) {
			return Collections.emptyList();
		}

		boolean isChanging = displayComponent.isChanging();
		
		double rho = baseMap.getRho();
		
		if (isChanging) {
			return Collections.emptyList();
		}
		
		String mapType = baseMap.getMapMetaData().getId();		
		int numMinerals = mineralsDisplaySet.size();
		if (mapCenterCache == null || !mapCenter.equals(mapCenterCache) || !mapType.equals(mapTypeCache) 
				|| rhoCache != rho || numMineralsCache != numMinerals) {
				
			mapTypeCache = mapType;
			
			int[] newMineralArray = new int[MAP_BOX_WIDTH * MAP_BOX_HEIGHT];

			double mag = baseMap.getScale();
	
			boolean hasMinerals = false;
			
			for (int y = 0; y < MAP_BOX_HEIGHT; y = y + 2) {
				for (int x = 0; x < MAP_BOX_WIDTH; x = x + 2) {
			
					int index = x + y * MAP_BOX_WIDTH;
					
					var point = baseMap.getMapBoxPoint(index);
					if (point == null)
						continue;
					// New approach
					// var mineralConcentrations = 
					// 		mineralMap.getRadiusConcentration(
					// 					mineralsDisplaySet, 
					// 					point,
					// 					0D).getConcentrations();

					// Old Approach
					var mineralConcentrations = 	
										mineralMap.getSomeMineralConcentrations(
											mineralsDisplaySet, 
											point,
											mag);	

					if (!mineralConcentrations.isEmpty()) {
						newMineralArray[index] = concentrationToColour(mineralConcentrations);
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
	 * Convert a mixture of mineral to a combined single color
	 * @param mineralConcentrations
	 * @return
	 */
	private int concentrationToColour(Map<String, Integer> mineralConcentrations) {
		int colorRGB = 0;

		for(var entry : mineralConcentrations.entrySet()) {
			String mineralType = entry.getKey();
			
			if (mineralsDisplaySet.contains(mineralType)) {
				Color baseColor = mineralColorMap.get(mineralType);
				double concentration = entry.getValue();
				int concentrationInt = (int) (255 * (concentration / 100D));
				int concentrationColor = (concentrationInt << 24) | (baseColor.getRGB() & 0x00FFFFFF);
				colorRGB = colorRGB | concentrationColor;
			}
		}
		return colorRGB;
	}

	/**
	 * Computes color for the concentration array.
	 * 
	 * @param mineralConcentrations
	 * @param newMineralArray
	 * @param x
	 * @param y
	 */
	private void computeColorMineralArray(Map<String, Integer> mineralConcentrations, int[] newMineralArray, int x, int y) {
		for(var entry : mineralConcentrations.entrySet()) {
			String mineralType = entry.getKey();
			
			if (isMineralDisplayed(mineralType)) {
				double concentration = entry.getValue();
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
	public Map<String, Color> getMineralColors() {
		
		if (mineralColorMap == null) {
			mineralColorMap = mineralMap.getTypes().stream()
				.collect(Collectors.toMap(MineralType::getName,
								m -> Color.decode(m.getColour())));
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
		if (displayed) {
			mineralsDisplaySet.add(mineralType);	
		}
		else {
			mineralsDisplaySet.remove(mineralType);
		}
	}

	/**
	 * Get the mineral conctrations within the map viewpoint. This ues the MineralMap to
	 * locate the concentratinos but also applies a filter on minteral type displayed
	 * 
	 * @param center The center of the map viewpoint
	 * @param arcAngle Arc of the viewpoint
	 * @return List of visible concentrations
	 */
	@Override
	protected List<MineralConcentration> getFeatures(Coordinates center, double arcAngle) {
		return mineralMap.getConcentrations(center, arcAngle, mineralsDisplaySet);
	}

	/**
	 * Render a Mineral concentration as a symbol on the map and a specific point.
	 * 
	 * @param f Concentration to render
	 * @param location Location on the map display
	 * @param g Graphics to use for drawing
	 * @param isColourful Is the underlying map colourful
	 * 
	 * @return A hotspot
	 */
	@Override
	protected MapHotspot displayFeature(MineralConcentration f, IntPoint location, Graphics2D g,
										boolean isColourful) {
		var colour = concentrationToColour(f.getConcentrations());

		g.setColor(new Color(colour));

		int locX = location.getiX() - CIRCLE_RADIUS;
		int locY = location.getiY() - CIRCLE_RADIUS;

		// Draw a circle at the location.
		g.fillRect(locX, locY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);

		return new MineralHotspot(location, CIRCLE_DIAMETER, f);
	}
}
