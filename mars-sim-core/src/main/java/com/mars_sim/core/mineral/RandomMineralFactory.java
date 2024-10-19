/*
 * Mars Simulation Project
 * RandomMineralFactory.java
 * @date 2024-10-19
 * @author Barry Evans
 */
package com.mars_sim.core.mineral;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import com.mars_sim.core.SimulationConfig;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.map.location.Direction;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.tool.RandomUtil;

/**
 * This is a factory class creates a randomly generated mineral map of Mars.
 */
public final class RandomMineralFactory {

	private static final SimLogger logger = SimLogger.getLogger(RandomMineralFactory.class.getName());
	
	private static final String TOPO_MAP_FOLDER = "/topography/";	 
	
	private static final int TOPO_W = 300;
	private static final int TOPO_H = 150;

	private static final int REGION_FACTOR = 1500;
	private static final int NON_REGION_FACTOR = 50;

	// Topographical Region Strings
	private static final String CRATER_IMG = Msg.getString("RandomMineralMap.image.crater"); //$NON-NLS-1$
	private static final String VOLCANIC_IMG = Msg.getString("RandomMineralMap.image.volcanic"); //$NON-NLS-1$
	private static final String SEDIMENTARY_IMG = Msg.getString("RandomMineralMap.image.sedimentary"); //$NON-NLS-1$

	private static final String CRATER_REGION = "crater";
	private static final String VOLCANIC_REGION = "volcanic";
	private static final String SEDIMENTARY_REGION = "sedimentary";
	
	/**
	 * This class finds a set of potential Coordinates that have the correct geology to
	 * support the present of certain Mineral Types. This is based on the locale of the mineral
	 */
	private static class LocationSelector implements Function<MineralType,List<Coordinates>> {
		
		private Set<Coordinates> craterRegionSet;
		private Set<Coordinates> volcanicRegionSet;
		private Set<Coordinates> sedimentaryRegionSet;

		LocationSelector() {
			// Load topographical regions.
			craterRegionSet = getTopoRegionSet(CRATER_IMG, TOPO_W, TOPO_H);
			volcanicRegionSet = getTopoRegionSet(VOLCANIC_IMG, TOPO_W, TOPO_H);
			sedimentaryRegionSet = getTopoRegionSet(SEDIMENTARY_IMG, TOPO_W, TOPO_H);
		}

		@Override
		public List<Coordinates> apply(MineralType t) {
			// Create super set of topographical regions.
			List<Coordinates> potentialLocns = new ArrayList<>();

			// Each mineral has unique abundance in each of the 3 regions
			for(String locale : t.getLocales()) {
				var newRegion = switch(locale) {
					case CRATER_REGION -> craterRegionSet;
					case VOLCANIC_REGION -> volcanicRegionSet;
					case SEDIMENTARY_REGION -> sedimentaryRegionSet;
					default -> null;
				};
				if (newRegion != null) {
					potentialLocns.addAll(newRegion);
				}
			}

			return potentialLocns;
		}

	}

	private RandomMineralFactory() {
		// Static helper
	}
	
	/**
	 * Create a mineral map based on random assignments. The logic places minerals according to their
	 * preferred geological traits. The traits are identified using a set of topological maps.
	 */
	public static MineralMap createRandomMap() {
	
		var newMap = new MineralMap();

		// Add random mineral to the map using potential Location based on the type of mineral
		addRandomMinerals(newMap, 0, 100, new LocationSelector());

		return newMap;
	}

	/**
	 * Add mineral to a mineralmap randomly over a number of iteratons building up the final
	 * result in layers. A function provides potential locations that can support the mineral
	 * of the required type;
	 * 
	 * @param newMap Map to hold new monerals
	 * @param lowerBaseCon Lower bound of the base concentration
	 * @param highBaseConc Upper bound
	 * @param locator Return potential suitable locations for a certian mineraltype
	 */
	private static void addRandomMinerals(MineralMap newMap, int lowerBaseCon, int highBaseConc,
						Function<MineralType,List<Coordinates>> locator) {

		// Should be passed in as argument
		var mineralMapConfig = SimulationConfig.instance().getMineralMapConfiguration();
		
		List<MineralType> minerals = new ArrayList<>(mineralMapConfig.getMineralTypes());
		
		Collections.shuffle(minerals);
		for(var mineralType : minerals) {		
			// Get potential locations for this mineral
			var potentialLocns = locator.apply(mineralType);

			// Have one region array for each of 10 types of minerals 
			// regionArray between 850 and 3420 for each mineral
			int baseConc = RandomUtil.getRandomInt(lowerBaseCon, highBaseConc);
			
			// Determine individual mineral iteration.
			int numIteration = calculateIteration(mineralType, potentialLocns.size());
					
			// Get the new remainingConc
			double remainingConc = 1.0 * baseConc * numIteration;
		
			for (int x = numIteration; x > 0; x--) {
				// Chose a random location
				var c = RandomUtil.getRandomElement(potentialLocns);
				var conc = 0;
				if (x > 0)
					conc = (int) Math.round(RandomUtil.getRandomDouble(baseConc *.75, baseConc));
				else
					conc = (int) Math.round(remainingConc);
	
				// Determine individual mineral concentrations.
				remainingConc -= createMinerals(newMap, c, conc, mineralType);
								
				if (remainingConc <= 0.0) 
					break;
			}		
		} // end of iterating MineralType
	}

	/**
	 * Generates mineral concentrations.
	 * 
	 * @param map Mineral map holding new minerals
	 * @param baseLocn Base of the ne wmineral concentration
	 * @param concentration Concentration of new mineral
	 * @param mineral Typeof mineral to add
	 */
	private static int createMinerals(MineralMap map, Coordinates baseLocn, int concentration,
								MineralType mineral) {
		Direction direction = new Direction(RandomUtil.getRandomDouble(Math.PI * 2D));
		// Spread it over a 10 km radius
		double distance = RandomUtil.getRandomDouble(1, 20);
		Coordinates newLocation = baseLocn.getNewLocation(direction, distance);

		if (concentration > 100)
			concentration = 100;
		if (concentration < 0) {
			concentration = 0;
		}
		
		map.addMineral(newLocation, mineral, concentration);
		return concentration;
	}

	/**
	 * Creates concentration at a specified location.
	 * 
	 * @param location
	 */
	public static void createLocalConcentration(MineralMap targetMap, Coordinates location) {
		// Potential locnations is just a single one
		List<Coordinates> locns = new ArrayList<>();
		locns.add(location);

		// Local minerals have alower concentraton and only a single Location
		addRandomMinerals(targetMap, 5, 25, m -> locns);
	}
	
	/**
	 * Calculate the number of interaction in determining the mineral concentration.
	 * 
	 * @param mineralType
	 * @param length
	 * @return
	 */
	private static int calculateIteration(MineralType mineralType, int length) {
		int num = 0;
		if (length > 1) {
			num = (int)Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* REGION_FACTOR / 1800 * length
					/ mineralType.getFrequency());
		}
		else {
			num = (int)Math.round(RandomUtil.getRandomDouble(.75, 1.25) 
					* NON_REGION_FACTOR * 2
					/ mineralType.getFrequency());
		}
		return num;
	}
	
	/**
	 * Gets a set of location coordinates representing a topographical region.
	 * The logic identifies "white" pixels as the topographical hotspots
	 * 
	 * @param imageMapName the topographical region map image.
	 * @return set of location coordinates.
	 */
	public static Set<Coordinates> getTopoRegionSet(String imageMapName, int w, int h) {
		Set<Coordinates> result = new HashSet<>();
		URL imageMapURL = RandomMineralFactory.class.getResource(TOPO_MAP_FOLDER + imageMapName);
		ImageIcon mapIcon = new ImageIcon(imageMapURL);
		Image mapImage = mapIcon.getImage();

		int[] mapPixels = new int[w * h];
		PixelGrabber topoGrabber = new PixelGrabber(mapImage, 0, 0, w, h, mapPixels, 0, w);
		try {
			topoGrabber.grabPixels();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "grabber error" + e);
			// Restore interrupted state
		    Thread.currentThread().interrupt();
		}
		if ((topoGrabber.status() & ImageObserver.ABORT) != 0)
			logger.info("grabber error");

		for (int x = 0; x < h; x++) {
			for (int y = 0; y < w; y++) {
				int pixel = mapPixels[(x * w) + y];
				Color color = new Color(pixel);
				if (Color.white.equals(color)) {
					double pixelOffset = (Math.PI / 150D) / 2D;
					double phi = ((x / 150D) * Math.PI) + pixelOffset;
					double theta = ((y / 150D) * Math.PI) + Math.PI + pixelOffset;
					if (theta > (2D * Math.PI))
						theta -= (2D * Math.PI);
					result.add(new Coordinates(phi, theta));
				}
			}
		}

		return result;
	}
}
