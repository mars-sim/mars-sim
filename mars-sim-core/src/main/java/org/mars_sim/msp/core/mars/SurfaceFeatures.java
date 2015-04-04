/**
 * Mars Simulation Project
 * SurfaceFeatures.java
 * @version 3.08 2014-04-03
 * @author Scott Davis
 */
package org.mars_sim.msp.core.mars;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.ai.mission.Mining;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.structure.Settlement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * SurfaceFeatures represents the surface terrain and landmarks of the virtual Mars.
 */
public class SurfaceFeatures implements Serializable {

	private static final long serialVersionUID = 1L;
	private static double MEAN_SOLAR_IRRADIANCE =  590D; // in W/m2  = 1371 / (1.52*1.52) in contrast with the Earth's solar constant (1350 Wm-2)-- about 44% .
	// Data members
    private List<Landmark> landmarks;
    private MineralMap mineralMap;
    private List<ExploredLocation> exploredLocations;

    private transient TerrainElevation surfaceTerrain;
    private AreothermalMap areothermalMap;
    private transient Mars mars;
    private MissionManager missionManager;
    private Coordinates sunDirection;

    /**
     * Constructor
     * @throws Exception when error in creating surface features.
     */
    public SurfaceFeatures() {

        surfaceTerrain = new TerrainElevation();
        mineralMap = new RandomMineralMap();
        exploredLocations = new ArrayList<ExploredLocation>();
        areothermalMap = new AreothermalMap();

        mars = Simulation.instance().getMars();
        missionManager = Simulation.instance().getMissionManager();

        try {
            landmarks = SimulationConfig.instance().getLandmarkConfiguration().getLandmarkList();
        } catch (Exception e) {
            throw new IllegalStateException("Landmarks could not be loaded: " + e.getMessage(), e);
        }
    }

    /**
     * Initialize transient data in the simulation.
     * @throws Exception if transient data could not be constructed.
     */
    public void initializeTransientData() {

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
        if (mars == null)
        	mars = Simulation.instance().getMars();
        Coordinates sunDirection = mars.getOrbitInfo().getSunDirection();
        double angleFromSun = sunDirection.getAngle(location);

        double result = 0;
        double twilightzone = .2D; // Angle width of twilight border (radians)
        if (angleFromSun < (Math.PI / 2D) - (twilightzone / 2D)) {
            result = 1D;
        } else if (angleFromSun > (Math.PI / 2D) + (twilightzone / 2D)) {
            result = 0D;
        } else {
            double twilightAngle = angleFromSun - ((Math.PI / 2D) - (twilightzone / 2D));
            result = 1D - (twilightAngle / twilightzone);
        }

        return result;
    }

    /**
     * Calculate the solar irradiance at a particular location on Mars
     * @param location
     * @return solar irradiance.
     */
	// 2015-03-17 Added getSolarIrradiance()
    public double getSolarIrradiance(Coordinates location) {
        if (mars == null)
        	mars = Simulation.instance().getMars();
    	// The solar irradiance value below is the value on top of the atmosphere only
    	//double lat = location.getPhi2Lat(location.getPhi());
    	//System.out.println("lat is " + lat);
/*
// Approach 1  (more cumbersome)
		double s1 = 0;
        double L_s = mars.getOrbitInfo().getL_s();
        double e = OrbitInfo.ECCENTRICITY;
    	double z = mars.getOrbitInfo().getSolarZenithAngle(phi);
    	double num =  1 + e * Math.cos( (L_s - 248) /180D * Math.PI);
    	double den = 1 - e * e;
    	s1 = MEAN_SOLAR_IRRADIANCE * Math.cos(z) * num / den * num / den  ;
    	System.out.println("solar irradiance s1 is " + s1);
*/

// Approach 2
    	double s2 = 0;
    	double part2 =  mars.getOrbitInfo().getCosineSolarZenithAngle(location);
    	//System.out.println("part2 is " + part2);
    	if (part2 <= 0) // the sun is set. no need of calculation.
    		s2 = 0;
    	else {
	    	double part1 =  MEAN_SOLAR_IRRADIANCE;
	    	double r =  mars.getOrbitInfo().getRadius();
	    	double part3 =  OrbitInfo.SEMI_MAJOR_AXIS * OrbitInfo.SEMI_MAJOR_AXIS / r / r;
	    	//System.out.println("part3 is " + part3);
	    	s2 = part1 * part2 * part3;

			// Added randomness
			double up = RandomUtil.getRandomDouble(5);
			double down = RandomUtil.getRandomDouble(5);

			s2 +=  up - down;

			if (s2 <= 0)
				s2 = 0;
    	}
    	//System.out.println("solar irradiance s2 is " + s2);

    	// TODO: calculate the solar irradiance components on horizontal surface on Mars :
    	// G_h = G_direct + G_diffuse

    	return s2;
    }

    /** Returns true if location is in a dark polar region.
     *  A dark polar region is where the sun doesn't rise in the current sol.
     *  @return true if location is in dark polar region
     */
    public boolean inDarkPolarRegion(Coordinates location) {

        boolean result = false;

        if (mars == null)
        	mars = Simulation.instance().getMars();
        if (sunDirection == null)
        	sunDirection = mars.getOrbitInfo().getSunDirection();

        double sunPhi = sunDirection.getPhi();
        double darkPhi = 0D;

        if (sunPhi < (Math.PI / 2D)) {
            darkPhi = Math.PI - ((Math.PI / 2D) - sunPhi);
            if (location.getPhi() >= darkPhi) {
                result = true;
            }
        } else {
            darkPhi = sunPhi - (Math.PI / 2D);
            if (location.getPhi() < darkPhi) {
                result = true;
            }
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

        return (location.getPhi() < polarPhi) || (location.getPhi() > Math.PI - polarPhi);
    }

    /**
     * Gets a list of landmarks on Mars.
     * @return list of landmarks.
     */
    public List<Landmark> getLandmarks() {
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
     * Gets the areothermal heat potential for a given location.
     * @param location the coordinate location.
     * @return areothermal heat potential as percentage (0% - low, 100% - high).
     */
    public double getAreothermalPotential(Coordinates location) {
        return areothermalMap.getAreothermalPotential(location);
    }

    /**
     * Time passing in the simulation.
     * @param time time in millisols
     * @throws Exception if error during time.
     */
    public void timePassing(double time) {
        // Update any reserved explored locations.
        Iterator<ExploredLocation> i = exploredLocations.iterator();
        while (i.hasNext()) {
            ExploredLocation site = i.next();
            if (site.isReserved()) {
                // Check if site is reserved by a current mining mission.
                // If not, mark as unreserved.
                boolean goodMission = false;
                if (missionManager == null)
                	missionManager = Simulation.instance().getMissionManager();
                Iterator<Mission> j = missionManager.getMissions().iterator();
                while (j.hasNext()) {
                    Mission mission = j.next();
                    if (mission instanceof Mining) {
                        if (site.equals(((Mining) mission).getMiningSite())) {
                            goodMission = true;
                        }
                    }
                }
                if (!goodMission) {
                    site.setReserved(false);
                }
            }
        }
    }

    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {
        surfaceTerrain = null;
        landmarks.clear();
        landmarks = null;
        mineralMap.destroy();
        mineralMap = null;
        exploredLocations.clear();
        exploredLocations = null;
        areothermalMap.destroy();
        areothermalMap = null;
    }
}
