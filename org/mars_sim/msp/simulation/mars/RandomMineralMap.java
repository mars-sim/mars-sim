/**
 * Mars Simulation Project
 * MineralMap.java
 * @version 2.84 2008-03-15
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.mars;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Direction;
import org.mars_sim.msp.simulation.RandomUtil;

/**
 * A randomly generated mineral map of Mars.
 */
public class RandomMineralMap implements Serializable, MineralMap {

	private static String CLASS_NAME = 
		"org.mars_sim.msp.simulation.mars.RandomMineralMap";
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	private List<MineralConcentration> mineralConcentrations;
	
	/**
	 * Constructor
	 */
	RandomMineralMap() {
		mineralConcentrations = new ArrayList<MineralConcentration>(1000);
		
		// Determine mineral concentrations.
		determineMineralConcentrations();
	}
	
	/**
	 * Determine all mineral concentrations.
	 */
	private void determineMineralConcentrations() {
		// Load topographical regions.
		Set<Coordinates> craterRegionSet = getTopoRegionSet("TopographyCrater.gif");
		Set<Coordinates> volcanicRegionSet = getTopoRegionSet("TopographyVolcanic.gif");
		Set<Coordinates> sedimentaryRegionSet = getTopoRegionSet("TopographySedimentary.gif");
		
		// Create super set of topographical regions.
		Set<Coordinates> regionSet = new HashSet<Coordinates>(3000);
		// regionSet.addAll(craterRegionSet);
		regionSet.addAll(volcanicRegionSet);
		// regionSet.addAll(sedimentaryRegionSet);
		Coordinates[] regionArray = regionSet.toArray(new Coordinates[regionSet.size()]);
		
		// Determine hematite concentrations.
		int concentrationNumber = Math.round((float) regionArray.length / 10F);
		for (int x = 0; x < concentrationNumber; x++) {
			int regionLocationIndex = RandomUtil.getRandomInt(regionArray.length - 1);
			Coordinates regionLocation = regionArray[regionLocationIndex];
			Direction direction = new Direction(RandomUtil.getRandomDouble(Math.PI * 2D));
			double pixelRadius = (Mars.MARS_CIRCUMFERENCE / 300D) / 2D;
			double distance = RandomUtil.getRandomDouble(pixelRadius);
			Coordinates location = regionLocation.getNewLocation(direction, distance);
			double concentration = RandomUtil.getRandomDouble(100D);
			mineralConcentrations.add(new MineralConcentration(
					location, concentration, MineralMap.HEMATITE));
			
			/*
			double phi = Coordinates.getRandomLatitude();
			double theta = Coordinates.getRandomLongitude();
			Coordinates location = new Coordinates(phi, theta);
			double concentration = RandomUtil.getRandomDouble(100D);
			mineralConcentrations.add(new MineralConcentration(
					location, concentration, MineralMap.HEMATITE));
			*/
		}
	}
	
	private Set<Coordinates> getTopoRegionSet(String imageMapName) {
		Set<Coordinates> result = new HashSet<Coordinates>(3000);
		
		URL imageMapURL = ClassLoader.getSystemResource("images/" + imageMapName);
		ImageIcon mapIcon = new ImageIcon(imageMapURL);
		Image mapImage = mapIcon.getImage();
		int[] mapPixels = new int[300 * 150];
		PixelGrabber topoGrabber = new PixelGrabber(mapImage, 0, 0, 300, 150, mapPixels, 0, 300);
		try {
			topoGrabber.grabPixels();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE,"grabber error" + e);
        }
        if ((topoGrabber.status() & ImageObserver.ABORT) != 0)
            logger.info("grabber error");
        
        for (int x = 0; x < 150; x++) {
        	for (int y = 0; y < 300; y++) {
        		int pixel = mapPixels[(x * 300) + y];
        		int alpha = (pixel >> 24) & 0xFF;
        		if (alpha == 255) {
        			double pixel_offset = (Math.PI / 150D) / 2D;
        			double phi = (((double) x / 150D) * Math.PI) + pixel_offset;
        			double theta = (((double) y / 150D) * Math.PI) + Math.PI + pixel_offset;
        			if (theta > (2D * Math.PI)) theta -= (2D * Math.PI);
        			result.add(new Coordinates(phi, theta));
        		}
        	}
        }
		
		return result;
	}
	
    /**
     * Gets the mineral concentration at a given location.
     * @param mineralType the mineral type (see MineralMap.java)
     * @param location the coordinate location.
     * @return percentage concentration (0 to 100.0)
     */
	public Map<String, Double> getAllMineralConcentration(Coordinates location) {
		Map<String, Double> result = new HashMap<String, Double>();
		double hematiteConcentration = getMineralConcentration(MineralMap.HEMATITE, location);
		result.put(MineralMap.HEMATITE, hematiteConcentration);
		return result;
	}

	/**
     * Gets all of the mineral concentrations at a given location.
     * @param location the coordinate location.
     * @return map of mineral types and percentage concentration (0 to 100.0)
     */
	public double getMineralConcentration(String mineralType,
			Coordinates location) {
		
		double result = 0D;
		
		Iterator<MineralConcentration> i = mineralConcentrations.iterator();
		while (i.hasNext()) {
			MineralConcentration mineralConcentration = i.next();
			if (mineralConcentration.getMineralType().equals(mineralType)) {
				double concentrationPhi = mineralConcentration.getLocation().getPhi();
				double concentrationTheta = mineralConcentration.getLocation().getTheta();
				double phiDiff = Math.abs(location.getPhi() - concentrationPhi);
				double thetaDiff = Math.abs(location.getTheta() - concentrationTheta);
				double diffLimit = .04D;
				if ((concentrationPhi < Math.PI / 7D) || concentrationPhi > Math.PI - (Math.PI / 7D))
					diffLimit+= Math.abs(Math.cos(concentrationPhi));
				if ((phiDiff < diffLimit) && (thetaDiff < diffLimit)) {
					double distance = location.getDistance(mineralConcentration.getLocation());
					double concentrationRange = mineralConcentration.getConcentration();
					if (distance < concentrationRange) {
						result += (1D - (distance / concentrationRange)) * mineralConcentration.getConcentration();
						if (result > 100D) result = 100D;
					}
				}
			}
		}
		
		return result;
	}
	
	private class MineralConcentration implements Serializable {
		private Coordinates location;
		private double concentration;
		private String mineralType;
		
		private MineralConcentration(Coordinates location, double concentration, 
				String mineralType) {
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