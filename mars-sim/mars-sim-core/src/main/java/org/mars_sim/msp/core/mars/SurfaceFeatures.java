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
	public static double MEAN_SOLAR_IRRADIANCE =  586D; // in flux or [W/m2]  = 1371 / (1.52*1.52)
	// This is the so-called "solar constant" of Mars (not really a constant per se), which is the flux of solar radiation at the top of the atmosphere (TOA) at the mean distance a between Mars and the sun.
	// Note: at the top of the Mars atmosphere
	// The solar irradiance at Mars' mean distance from the Sun (1.52 AU) is S0 = 590 Wm-2.
	// This is about 44% of the Earth's solar constant (1350 Wm-2).
	// At perihelion (1.382 AU), the maximum available irradiance is S = 717 Wm-2, while at apohelion (1.666 AU) the maximum is S = 493 Wm-2.
	// see http://ccar.colorado.edu/asen5050/projects/projects_2001/benoit/solar_irradiance_on_mars.htm


	// Data members
	private double opticalDepth;

    private List<Landmark> landmarks;
    private List<ExploredLocation> exploredLocations;

    private transient Mars mars;
    private transient OrbitInfo orbitInfo;
    private transient TerrainElevation surfaceTerrain;
    private MineralMap mineralMap;
    private AreothermalMap areothermalMap;
    private MissionManager missionManager;
    private Coordinates sunDirection;
    private Weather weather;

    /**
     * Constructor
     * @throws Exception when error in creating surface features.
     */
    public SurfaceFeatures() {

        surfaceTerrain = new TerrainElevation();
        mineralMap = new RandomMineralMap();
        exploredLocations = new ArrayList<ExploredLocation>();
        areothermalMap = new AreothermalMap();

        //weather = Simulation.instance().getMars().getWeather();

        mars = Simulation.instance().getMars();
        //orbitInfo = mars.getOrbitInfo();
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
		// Part 1: get cosine solar zenith angle
    	double G_0 = 0;
    	double G_h = 0;
    	double G_bh = 0;
    	double G_dh = 0;
    	//G_0: solar irradiance at the top of the atmosphere
    	//G_h: global irradiance on a horizontal surface
    	//G_b: direct beam irradiance on a horizontal surface
    	//G_d: diffuse irradiance on a horizontal surface

    	if (orbitInfo == null)
    		orbitInfo = mars.getOrbitInfo();
    	double part1 =  orbitInfo.getCosineSolarZenithAngle(location);
    	//System.out.println("part2 is " + part2);
    	if (part1 <= 0) // the sun is set behind the planet Mars, total darkness and  no need of calculation.
    		G_0 = 0;

    	else {

    		// Part 2: get the new average solar irradiance as a result of the changing distance between Mars and Sun  with respect to the value of L_s.
    		//double L_s = orbitInfo.getL_s();

    		// Note a: Because of Mars's orbital eccentricity, L_s advances somewhat unevenly with time, but can be evaluated
    		// as a trigonometric power series for the orbital eccentricity and the orbital mean anomaly measured with respect to the perihelion.
    		// The areocentric longitude at perihelion, L_s = 251.000 + 0.00645 * (year - 2000),
    		// indicates a near alignment of the planet's closest approach to the Sun in its orbit with its winter solstice season,

    		// Note b: In 2043, there is 35% (max is 45.4%) on average more sunlight at perihelion (L_s = 251.2774 deg) than at aphelion (L_s = 71.2774 deg)
    		// Equation: 135% * (.5 * sin (L_s - 251.2774 + 180 - 90) + .5 )
	    	double part2 =  MEAN_SOLAR_IRRADIANCE; // * 0.675 * (1 + Math.sin((L_s - 161.2774)/180D * Math.PI));
	    	//System.out.println("part2 is "+ part2);

	    	// Part 3: get the instantaneous radius and semi major axis
	    	double r =  orbitInfo.getRadius();
	    	double part3 =  OrbitInfo.SEMI_MAJOR_AXIS * OrbitInfo.SEMI_MAJOR_AXIS / r / r;
	    	//System.out.println("part3 is " + part3);

	    	// Part 4 : opacity of the Martian atmosphere due to local dust storm
	    	// The extinction of radiation through the Martian atmosphere is caused mainly by suspended dust particles.
	    	// Although dust particles are effective at scattering direct solar irradiance, a substantial amount of diffuse light is able to penetrate to the surface of the planet.
	    	// The amount of PAR available on the Martian surface can then be calculated to be 42% of the total PAR to reach the surface.

	    	// Based on Viking observation, it's estimated approximately 100 local dust storms (each last a few days) can occur in a given Martian year

	    	G_0 = part1 * part2 * part3;

			if (G_0 <= 0)
				G_0 = 0;

			// Part 4 :  absorption and scattering of solar radiation and solar optical depth

			// Added randomness
			double up = RandomUtil.getRandomDouble(.001);
			double down = RandomUtil.getRandomDouble(.001);

			// limits the value to fluctuate between .32 and .52 (average is 42%)
			if (opticalDepth > 6)
				opticalDepth = 6;
			if (opticalDepth < .18)
				opticalDepth = .18;
			// From Viking data, at no time did the optical depth fall below 0.18,

	    	// Note that it carries the memory of its past value
	    	double tau = opticalDepth +  up - down;
	    	if (weather == null)
	    		weather = Simulation.instance().getMars().getWeather();
	        opticalDepth = 0.2342 + 0.2237 * weather.getDailyVariationAirPressure(location) + RandomUtil.getRandomDouble(.1);
	        // starting value between 0.2 and 0.5
	    	// Note: during relatively periods of clear sky, typical values for optical depth were between 0.2 and 0.5
	    	double cos_z =  orbitInfo.getCosineSolarZenithAngle(location);
	    	G_bh = G_0 * cos_z * Math.exp(-tau/cos_z);

			// TODO: Part 5 : diffuse solar irradiance
	    	G_dh = G_bh/3; // assumming it's good enough

	    	// Final:
	    	G_h = G_bh + G_dh;
    	}

    	//System.out.println("solar irradiance si is " + si);

    	// TODO: calculate the solar irradiance components on horizontal surface on Mars :
    	// G_h = G_direct + G_diffuse

    	return G_h;
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
        orbitInfo = null;
        mars = null;
        missionManager = null;;
        sunDirection = null;
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
