/**
 * Mars Simulation Project
 * MineralMap.java
 * @version 3.1.0 2017-10-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Direction;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.mars.MineralMapConfig.MineralType;
import org.mars_sim.msp.core.tool.RandomUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A randomly generated mineral map of Mars.
 */
public class RandomMineralMap implements Serializable, MineralMap {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static String CLASS_NAME = "org.mars_sim.msp.simulation.mars.RandomMineralMap";
	private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Topographical Region Strings
	private static final String CRATER_REGION = "crater";
	private static final String VOLCANIC_REGION = "volcanic";
	private static final String SEDIMENTARY_REGION = "sedimentary";

	// Frequency Strings
	private static final String COMMON_FREQUENCY = "common";
	private static final String UNCOMMON_FREQUENCY = "uncommon";
	private static final String RARE_FREQUENCY = "rare";
	private static final String VERY_RARE_FREQUENCY = "very rare";

	// List of all mineral concentrations.
	private List<MineralConcentration> mineralConcentrations;

	/**
	 * Constructor
	 */
	RandomMineralMap() {
		mineralConcentrations = new ArrayList<MineralConcentration>(2000);

		// Determine mineral concentrations.
		determineMineralConcentrations();
	}

	/**
	 * Determine all mineral concentrations.
	 */
	private void determineMineralConcentrations() {
		// Load topographical regions.
		Set<Coordinates> craterRegionSet = getTopoRegionSet("TopographyCrater.png");
		Set<Coordinates> volcanicRegionSet = getTopoRegionSet("TopographyVolcanic.png");
		Set<Coordinates> sedimentaryRegionSet = getTopoRegionSet("TopographySedimentary.png");

		MineralMapConfig config = SimulationConfig.instance().getMineralMapConfiguration();
		try {
			Iterator<MineralType> i = config.getMineralTypes().iterator();
			while (i.hasNext()) {
				MineralType mineralType = i.next();

				// Create super set of topographical regions.
				Set<Coordinates> regionSet = new HashSet<Coordinates>(4000);
				Iterator<String> j = mineralType.locales.iterator();
				while (j.hasNext()) {
					String locale = j.next().trim();
					if (CRATER_REGION.equalsIgnoreCase(locale))
						regionSet.addAll(craterRegionSet);
					else if (VOLCANIC_REGION.equalsIgnoreCase(locale))
						regionSet.addAll(volcanicRegionSet);
					else if (SEDIMENTARY_REGION.equalsIgnoreCase(locale))
						regionSet.addAll(sedimentaryRegionSet);
				}
				Coordinates[] regionArray = regionSet.toArray(new Coordinates[regionSet.size()]);

				if (regionArray.length > 0) {
					// Determine individual mineral concentrations.
					int concentrationNumber = Math
							.round((float) regionArray.length / 10F / getFrequencyModifier(mineralType.frequency));
					for (int x = 0; x < concentrationNumber; x++) {
						int regionLocationIndex = RandomUtil.getRandomInt(regionArray.length - 1);
						Coordinates regionLocation = regionArray[regionLocationIndex];
						Direction direction = new Direction(RandomUtil.getRandomDouble(Math.PI * 2D));
						double pixelRadius = (Mars.MARS_CIRCUMFERENCE / 300D) / 2D;
						double distance = RandomUtil.getRandomDouble(pixelRadius);
						Coordinates location = regionLocation.getNewLocation(direction, distance);
						double concentration = RandomUtil.getRandomDouble(100D);
						mineralConcentrations.add(new MineralConcentration(location, concentration, mineralType.name));
					}
				} else {
					// If no locales, randomly distribute mineral on surface.
					int concentrationNumber = Math.round(100F / getFrequencyModifier(mineralType.frequency));
					for (int x = 0; x < concentrationNumber; x++) {
						double phi = Coordinates.getRandomLatitude();
						double theta = Coordinates.getRandomLongitude();
						Coordinates location = new Coordinates(phi, theta);
						double concentration = RandomUtil.getRandomDouble(100D);
						mineralConcentrations.add(new MineralConcentration(location, concentration, mineralType.name));
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error creating random mineral map.", e);
			e.printStackTrace();
		}
	}

	/**
	 * Gets the dividend due to frequency of mineral type.
	 * 
	 * @param frequency the frequency ("common", "uncommon", "rare" or "very rare").
	 * @return frequency modifier.
	 */
	private float getFrequencyModifier(String frequency) {
		float result = 1F;
		if (COMMON_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 1F;
		else if (UNCOMMON_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 5F;
		else if (RARE_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 10F;
		else if (VERY_RARE_FREQUENCY.equalsIgnoreCase(frequency.trim()))
			result = 15F;
		return result;
	}

	/**
	 * Gets a set of location coordinates representing a topographical region.
	 * 
	 * @param imageMapName the topographical region map image.
	 * @return set of location coordinates.
	 */
	private Set<Coordinates> getTopoRegionSet(String imageMapName) {
		Set<Coordinates> result = new HashSet<Coordinates>(3000);
		/* [landrus, 26.11.09]: don't use the system classloader in a webstart env. */
		URL imageMapURL = getClass().getResource("/images/" + imageMapName);
		ImageIcon mapIcon = new ImageIcon(imageMapURL);
		Image mapImage = mapIcon.getImage();

		int[] mapPixels = new int[300 * 150];
		PixelGrabber topoGrabber = new PixelGrabber(mapImage, 0, 0, 300, 150, mapPixels, 0, 300);
		try {
			topoGrabber.grabPixels();
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE, "grabber error" + e);
		}
		if ((topoGrabber.status() & ImageObserver.ABORT) != 0)
			logger.info("grabber error");

		for (int x = 0; x < 150; x++) {
			for (int y = 0; y < 300; y++) {
				int pixel = mapPixels[(x * 300) + y];
				Color color = new Color(pixel);
				if (Color.white.equals(color)) {
					double pixel_offset = (Math.PI / 150D) / 2D;
					double phi = (((double) x / 150D) * Math.PI) + pixel_offset;
					double theta = (((double) y / 150D) * Math.PI) + Math.PI + pixel_offset;
					if (theta > (2D * Math.PI))
						theta -= (2D * Math.PI);
					result.add(new Coordinates(phi, theta));
				}
			}
		}

		return result;
	}

	/**
	 * Gets all of the mineral concentrations at a given location.
	 * 
	 * @param location the coordinate location.
	 * @return map of mineral types and percentage concentration (0 to 100.0)
	 */
	public Map<String, Double> getAllMineralConcentrations(Coordinates location) {
		Map<String, Double> result = Collections.emptyMap();
		boolean emptyMap = true;

		Iterator<MineralConcentration> i = mineralConcentrations.iterator();
		while (i.hasNext()) {
			MineralConcentration mineralConcentration = i.next();
			double effect = getMineralConcentrationEffect(mineralConcentration, location);
			if (effect > 0D) {
				if (emptyMap) {
					result = new HashMap<String, Double>();
					emptyMap = false;
				}
				double totalConcentration = 0D;
				if (result.containsKey(mineralConcentration.mineralType))
					totalConcentration = result.get(mineralConcentration.mineralType);
				totalConcentration += effect;
				if (totalConcentration > 100D)
					totalConcentration = 100D;
				result.put(mineralConcentration.mineralType, totalConcentration);
			}
		}
		return result;
	}

	/**
	 * Gets the mineral concentration at a given location.
	 * 
	 * @param mineralType the mineral type (see MineralMap.java)
	 * @param location    the coordinate location.
	 * @return percentage concentration (0 to 100.0)
	 */
	public double getMineralConcentration(String mineralType, Coordinates location) {
		double result = 0D;

		Iterator<MineralConcentration> i = mineralConcentrations.iterator();
		while (i.hasNext()) {
			MineralConcentration mineralConcentration = i.next();
			if (mineralConcentration.getMineralType().equalsIgnoreCase(mineralType)) {
				result += getMineralConcentrationEffect(mineralConcentration, location);
				if (result > 100D)
					result = 100D;
			}
		}

		return result;
	}

	/**
	 * Gets the effect of a given mineral concentration on a location.
	 * 
	 * @param concentration the mineral concentration.
	 * @param location      the location to affect.
	 * @return concentration effect (0% - 100%).
	 */
	private double getMineralConcentrationEffect(MineralConcentration concentration, Coordinates location) {
		double result = 0D;

		double concentrationPhi = concentration.getLocation().getPhi();
		double concentrationTheta = concentration.getLocation().getTheta();
		double phiDiff = Math.abs(location.getPhi() - concentrationPhi);
		double thetaDiff = Math.abs(location.getTheta() - concentrationTheta);
		double diffLimit = .04D;
		if ((concentrationPhi < Math.PI / 7D) || concentrationPhi > Math.PI - (Math.PI / 7D))
			diffLimit += Math.abs(Math.cos(concentrationPhi));
		if ((phiDiff < diffLimit) && (thetaDiff < diffLimit)) {
			double distance = location.getDistance(concentration.getLocation());
			double concentrationRange = concentration.getConcentration();
			if (distance < concentrationRange)
				result = (1D - (distance / concentrationRange)) * concentration.getConcentration();
		}

		return result;
	}

	/**
	 * Gets an array of all mineral type names.
	 * 
	 * @return array of name strings.
	 */
	public String[] getMineralTypeNames() {
		String[] result = new String[0];
		MineralMapConfig config = SimulationConfig.instance().getMineralMapConfiguration();
		try {
			List<MineralType> mineralTypes = config.getMineralTypes();
			result = new String[mineralTypes.size()];
			for (int x = 0; x < mineralTypes.size(); x++)
				result[x] = mineralTypes.get(x).name;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error getting mineral types.", e);
		}

		return result;
	}

	/**
	 * Finds a random location with mineral concentrations from a starting location
	 * and within a distance range.
	 * 
	 * @param startingLocation the starting location.
	 * @param range            the distance range (km).
	 * @return location with one or more mineral concentrations or null if none
	 *         found.
	 */
	public Coordinates findRandomMineralLocation(Coordinates startingLocation, double range) {
		Coordinates result = null;

		List<MineralConcentration> locales = new ArrayList<MineralConcentration>(0);

		Iterator<MineralConcentration> i = mineralConcentrations.iterator();
		while (i.hasNext()) {
			MineralConcentration mineralConc = i.next();
			double distance = startingLocation.getDistance(mineralConc.getLocation());
			if (range > (distance - mineralConc.getConcentration())) {
				locales.add(mineralConc);
			}
		}

		if (locales.size() > 0) {
			int index = RandomUtil.getRandomInt(locales.size() - 1);
			MineralConcentration concentration = locales.get(index);
			double distance = startingLocation.getDistance(concentration.getLocation());
			if (range < distance) {
				Direction direction = startingLocation.getDirectionToPoint(concentration.getLocation());
				result = startingLocation.getNewLocation(direction, range);
			} else
				result = new Coordinates(concentration.getLocation());
		}

		return result;
	}

	@Override
	public void destroy() {
		mineralConcentrations.clear();
		mineralConcentrations = null;
	}

	/**
	 * Internal class representing a mineral concentration.
	 */
	private static class MineralConcentration implements Serializable {
		private Coordinates location;
		private double concentration;
		private String mineralType;

		private MineralConcentration(Coordinates location, double concentration, String mineralType) {
			this.location = location;
			this.concentration = concentration;
			this.mineralType = mineralType;
		}

		private Coordinates getLocation() {
			return location;
		}

		private double getConcentration() {
			return concentration;
		}

		private String getMineralType() {
			return mineralType;
		}
	}
}