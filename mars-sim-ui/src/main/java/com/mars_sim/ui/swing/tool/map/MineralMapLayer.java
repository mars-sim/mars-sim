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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.IntPoint;
import com.mars_sim.core.mineral.MineralDeposit;
import com.mars_sim.core.mineral.MineralMap;
import com.mars_sim.core.mineral.MineralType;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * A map layer showing mineral concentrations.
 */
public class MineralMapLayer extends SurfaceFeatureLayer<MineralDeposit>
	implements FilteredMapLayer {

	/**
	 * Create a tooltip for a mineral concentation showing the details
	 */
	private static class MineralHotspot extends MapHotspot {

		private MineralDeposit conc;

		protected MineralHotspot(IntPoint center, int radius, MineralDeposit conc) {
			super(center, radius);
			this.conc = conc;
		}

		@Override
		public String getTooltipText() {
			var body = conc.getConcentrations().entrySet().stream()
					.map(e -> ResourceUtil.findAmountResourceName(e.getKey()) + " : " + e.getValue() + "%")
					.collect(Collectors.joining("<br>>"));

			return "<html>" + body + "</html>";
		}
	}

	private static final int CIRCLE_RADIUS = 4;
	private static final int CIRCLE_DIAMETER = (2 * CIRCLE_RADIUS);
	private static Map<String, Integer> mineralNames;
	
	private MineralMap mineralMap;
	private Map<Integer, Color> mineralColorMap;
	private Set<Integer> mineralsDisplaySet = new HashSet<>();
	private Component displayComponent;

	
	/**
	 * Constructor
	 * 
	 * @param displayComponent the display component.
	 */
	public MineralMapLayer(MapPanel map) {
		super("Mineral");
		this.displayComponent = map;
		mineralMap = map.getDesktop().getSimulation().getSurfaceFeatures().getMineralMap();
	
		mineralColorMap = getMineralColors();
	}
	
	/**
	 * Convert a mixture of mineral to a combined single color
	 * @param mineralConcentrations
	 * @return
	 */
	private int concentrationToColour(Map<Integer, Integer> mineralConcentrations) {
		int colorRGB = 0;

		for(var entry : mineralConcentrations.entrySet()) {
			int mineralType = entry.getKey();
			
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
	 * Gets a map of all mineral type names and their display colors.
	 * 
	 * @return map of names and colors.
	 */
	public Map<Integer, Color> getMineralColors() {
		
		if (mineralColorMap == null) {
			mineralColorMap = mineralMap.getTypes().stream()
				.collect(Collectors.toMap(MineralType::getResourceId,
								m -> Color.decode(m.getColour())));
		}
		return mineralColorMap;
	}

	/**
	 * Sets a mineral type to be displayed on the map or not.
	 * 
	 * @param mineralType the mineral type to display.
	 * @param displayed   true if displayed, false if not.
	 */
	@Override
	public void displayFilter(String mineralType, boolean displayed) {
		int minId = getMineralId(mineralType);
		if (displayed) {
			mineralsDisplaySet.add(minId);	
		}
		else {
			mineralsDisplaySet.remove(minId);
		}
	}

	/**
	 * Create a quick lookup for mineral name to resource id.
	 * @param mineralType Name of the mineral
	 * @return Resource Id
	 */
	private static int getMineralId(String mineralType) {
		if (mineralNames == null) {
			mineralNames = new HashMap<>();
			
			var types = SimulationConfig.instance().getMineralMapConfiguration().getMineralTypes();
			types.forEach(m -> mineralNames.put(m.getName(), m.getResourceId()));
		}
		return mineralNames.get(mineralType);
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
	protected List<MineralDeposit> getFeatures(Coordinates center, double arcAngle) {
		return mineralMap.getDeposits(center, arcAngle, mineralsDisplaySet);
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
	protected MapHotspot displayFeature(MineralDeposit f, IntPoint location, Graphics2D g,
										boolean isColourful) {
		var colour = concentrationToColour(f.getConcentrations());

		g.setColor(new Color(colour));

		int locX = location.getiX() - CIRCLE_RADIUS;
		int locY = location.getiY() - CIRCLE_RADIUS;

		// Draw a circle at the location.
		g.fillRect(locX, locY, CIRCLE_DIAMETER, CIRCLE_DIAMETER);

		return new MineralHotspot(location, CIRCLE_DIAMETER, f);
	}

	@Override
	public List<MapFilter> getFilterDetails() {
		List<MapFilter> results = new ArrayList<>();
		for(var e : getMineralColors().entrySet()) {
			String name = ResourceUtil.findAmountResourceName(e.getKey());
			results.add(new MapFilter(name, name,
								ColorLegendFactory.getLegend(e.getValue(), displayComponent)));
		}
		return results;
	}

	@Override
	public boolean isFilterActive(String filterName) {
		return mineralsDisplaySet.contains(getMineralId(filterName));
	}
}
