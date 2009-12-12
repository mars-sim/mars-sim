/**
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @version 2.84 2008-06-24
 * @author Scott Davis
 */
 
package org.mars_sim.msp.core.mars;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;

/** 
 * SurfaceFeatures represents the surface terrain and landmarks of the virtual Mars. 
 */
public class SurfaceFeatures implements Serializable {
    
    // Data members 
    private transient TerrainElevation surfaceTerrain;
    private List landmarks;
    private MineralMap mineralMap;
    private List<ExploredLocation> exploredLocations;
    
    /** 
     * Constructor 
     * @throws Exception when error in creating surface features.
     */
    public SurfaceFeatures() throws Exception {
        
        surfaceTerrain = new TerrainElevation();
        mineralMap = new RandomMineralMap();
        exploredLocations = new ArrayList<ExploredLocation>();

		try {
			landmarks = SimulationConfig.instance().getLandmarkConfiguration().getLandmarkList();
		}
		catch (Exception e) {
			throw new Exception("Landmarks could not be loaded: " + e.getMessage());
		}
    }
    
	/**
	 * Initialize transient data in the simulation.
	 * @throws Exception if transient data could not be constructed.
	 */
	public void initializeTransientData() throws Exception {
		
		// Initialize surface terrain.
		surfaceTerrain = new TerrainElevation();
	}    
    
    /** Returns the surface terrain
     *  @return surface terrain
     */
    public TerrainElevation getSurfaceTerrain() {
        return surfaceTerrain;
    }

    /** 
     * Returns a float value representing the current sunlight
     * conditions at a particular location.
     *  
     * @return value from 0.0 - 1.0
     * 0.0 represents night time darkness.
     * 1.0 represents daylight. 
     * Values in between 0.0 and 1.0 represent twilight conditions. 
     */
    public double getSurfaceSunlight(Coordinates location) {
        
		Mars mars = Simulation.instance().getMars();
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();
        double angleFromSun = sunDirection.getAngle(location);

        double result = 0;
        double twilightzone = .2D; // Angle width of twilight border (radians)
        if (angleFromSun < (Math.PI / 2D) - (twilightzone / 2D)) {
            result = 1D;
        }
        else if (angleFromSun > (Math.PI / 2D) + (twilightzone / 2D)) {
            result = 0D;
        }
        else {
            double twilightAngle = angleFromSun - ((Math.PI / 2D) - (twilightzone / 2D));
            result = 1D - (twilightAngle / twilightzone);
        }

        return result;
    }    

    /** Returns true if location is in a dark polar region.
     *  A dark polar region is where the sun doesn't rise in the current sol.
     *  @return true if location is in dark polar region
     */
    public boolean inDarkPolarRegion(Coordinates location) {
        
        boolean result = false;

		Mars mars = Simulation.instance().getMars();
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();
        double sunPhi = sunDirection.getPhi();
        double darkPhi = 0D;

        if (sunPhi < (Math.PI / 2D)) {
            darkPhi = Math.PI - ((Math.PI / 2D) - sunPhi);
            if (location.getPhi() >= darkPhi) result = true;
        }
        else {
            darkPhi = sunPhi - (Math.PI / 2D);
            if (location.getPhi() < darkPhi) result = true;
        }

        return result;
    }
    
    /**
     * Checks if location is within a polar region of Mars.
     * @param location the location to check.
     * @return true if in polar region.
     */
    public boolean inPolarRegion(Coordinates location) {
    	double polarPhi = .1D * Math.PI;
    	
    	if ((location.getPhi() < polarPhi) || (location.getPhi() > Math.PI - polarPhi))
    		return true;
    	else return false;
    }
    
    /**
     * Gets a list of landmarks on Mars.
     * @return list of landmarks.
     */
    public List getLandmarks() {
    	return landmarks;
    }
    
    /**
     * Gets the mineral map.
     * @return mineral map.
     */
    public MineralMap getMineralMap() {
    	return mineralMap;
    }
    
    /**
     * Adds an explored location.
     * @param location the location coordinates.
     * @param estimatedMineralConcentrations a map of all mineral types 
     * and their estimated concentrations (0% -100%)
     * @param settlement the settlement the exploring mission is from.
     * @return the explored location
     */
    public ExploredLocation addExploredLocation(Coordinates location, 
    		Map<String, Double> estimatedMineralConcentrations, Settlement settlement) {
    	ExploredLocation result = new ExploredLocation(location, 
    			estimatedMineralConcentrations, settlement);
    	exploredLocations.add(result);
    	return result;
    }
    
    /**
     * Gets a list of all explored locations on Mars.
     * @return list of explored locations.
     */
    public List<ExploredLocation> getExploredLocations() {
    	return exploredLocations;
    }
    
    /**
     * Time passing in the simulation.
     * @param time time in millisols
     * @throws Exception if error during time.
     */
    public void timePassing(double time) throws Exception {
    	// Update any reserved explored locations.
    	Iterator<ExploredLocation> i = exploredLocations.iterator();
    	while (i.hasNext()) {
    		ExploredLocation site = i.next();
    		if (site.isReserved()) {
    			// Check if site is reserved by a current mining mission.
    			// If not, mark as unreserved.
    			boolean goodMission = false;
    			MissionManager missionManager = Simulation.instance().getMissionManager();
    			Iterator<Mission> j = missionManager.getMissions().iterator();
    			while (j.hasNext()) {
    				Mission mission = j.next();
    				if (mission instanceof Mining) {
    					if (site.equals(((Mining) mission).getMiningSite())) goodMission = true;
    				}
    			}
    			if (!goodMission) {
    				site.setReserved(false);
    			}
    		}
    	}
    }
}