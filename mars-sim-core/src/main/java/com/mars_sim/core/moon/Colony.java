/*
 * Mars Simulation Project
 * Colony.java
 * @date 2024-02-17
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.Entity;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.authority.Nation;
import com.mars_sim.core.authority.Organization;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.project.ColonyResearcher;
import com.mars_sim.core.moon.project.ColonySpecialist;
import com.mars_sim.core.moon.project.DevelopmentProject;
import com.mars_sim.core.moon.project.ResearchProject;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.mapdata.location.Coordinates;

public class Colony implements Temporal, Entity, Comparable<Colony> {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Colony.class.getName());

	private boolean startup = true;
	
	private int id;
	
	private String name;
	
	/** The settlement's ReportingAuthority instance. */
	private Authority sponsor;
	
	private Population population;
		
	private Nation nation;

	private Coordinates location;

	private LunarActivity dev;
	private LunarActivity eco;
	private LunarActivity ind;
	private LunarActivity res;
			
	private Zone researchZone;
	private Zone developmentZone;
	
	private Set<LunarActivity> activities = new HashSet<>();
	/** A set of research projects this colony's researchers engage in. */
	private Set<Zone> zones = new HashSet<>();


	/**
	 * Constructor.
	 * 
	 * @param id
	 * @param name
	 * @param sponsor
	 * @param location
	 * @param startup Is it at the startup of the simulation
	 */
	public Colony(int id, String name, Authority sponsor, Coordinates location, boolean startup) {
		this.id = id;
		this.name = name;
		this.sponsor = sponsor;
		this.location = location;
		
		population = new Population(this);

		initActivity();

		initZone(startup);
	}
	
	/**
	 * Initializes population instance.
	 */
	public void initPop() {
		population.init();
	}
	
	/**
	 * Initializes activities.
	 */
	public void initActivity() {
		dev = new LunarActivity(LunarActivityType.DEVELOPMENT, this);
		activities.add(dev);
		eco = new LunarActivity(LunarActivityType.ECONOMIC, this);
		activities.add(eco);
		ind = new LunarActivity(LunarActivityType.INDUSTRIAL, this);
		activities.add(ind);
		res = new LunarActivity(LunarActivityType.RESEARCH, this);
		activities.add(res);
	}
	
	/**
	 * Initializes zones.
	 * 
	 * @param startup Is it at the startup of the simulation
	 */
	public void initZone(boolean startup) {
		for (ZoneType type: ZoneType.values()) {
			
			Zone zone = new Zone(type, this, startup);
			if (type == ZoneType.RESEARCH) {
				researchZone = zone;
			}
			else if (type == ZoneType.ENGINEERING) {
				developmentZone = zone;
			}
			
			addZone(zone);
		}	
	}
	
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		if (startup && pulse.isNewMSol()) {
			startup = false;
			initPop();
		}
		
		getOrganization().timePassing(pulse);
		
		population.timePassing(pulse);
		
		for (LunarActivity a: activities) {
			a.timePassing(pulse);
		}
		
		for (Zone z: zones) {
			z.timePassing(pulse);
		}
		
		return true;
	}
	
	/**
	 * Returns the research demand.
	 * 
	 * @return
	 */
	public double getResearchDemand() {
		return res.getDemand();
	}
	
	/**
	 * Returns the development demand.
	 * 
	 * @return
	 */
	public double getDevelopmentDemand() {
		return dev.getDemand();
	}
	
	/**
	 * Gets one researcher project that this researcher may join in.
	 * 
	 * @param researcher
	 * @return
	 */
	public ResearchProject getOneResearchProject(ColonyResearcher researcher) {
		return res.getOneResearchProject(researcher);
	}
	
	/**
	 * Gets one engineering project that this engineer may join in.
	 * 
	 * @param Engineer
	 * @return
	 */
	public DevelopmentProject getOneEngineeringProject(ColonySpecialist engineer) {
		return dev.getOneEngineeringProject(engineer);
	}
	
	/**
	 * Adds a research project.
	 * 
	 * @param rp
	 */
	public void addResearchProject(ResearchProject rp) {
		res.addResearchProject(rp);
	}
 	
	/**
	 * Adds an engineering project.
	 * 
	 * @param ep
	 */
	public void addEngineeringProject(DevelopmentProject ep) {
		dev.addEngineeringProject(ep);
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the nation.
	 * 
	 * @return
	 */
	public Nation getNation() {
		if (nation == null) {
			nation = sponsor.getOneNation();
		}
		return nation;
	}
	
	/**
	 * Adds a zone.
	 * 
	 * @param zone
	 */
	public void addZone(Zone zone) {
		zones.add(zone);
	}
	
	/**
	 * Gets a set of zones.
	 * 
	 * @return
	 */
	public Set<Zone> getZones() {
		return zones;
	}
	
	/**
	 * Gets the research zone.
	 * 
	 * @return
	 */
	public Zone getResearchZone() {
		return researchZone;
	}
	
	/**
	 * Gets the area of research zone.
	 *  
	 * @return
	 */
	public double getResearchArea() {
		return researchZone.getArea();
	}
	
	/**
	 * Gets the research area growth rate.
	 * 
	 * @return
	 */
	public double getResearchAreaGrowthRate() {
		return researchZone.getGrowthRate();
	}	
	
	/**
	 * Gets the development zone.
	 * 
	 * @return
	 */
	public Zone getDevelopmentZone() {
		return developmentZone;
	}
	
	/**
	 * Gets the area of development zone.
	 *  
	 * @return
	 */
	public double getDevelopmentArea() {
		return developmentZone.getArea();
	}
	
	/**
	 * Gets the development area growth rate.
	 * 
	 * @return
	 */
	public double getDevelopmentAreaGrowthRate() {
		return developmentZone.getGrowthRate();
	}

	/**
	 * Gets the organization.
	 * 
	 * @return
	 */
	public Organization getOrganization() {
		return sponsor.getOrganization();
	}

	/**
	 * Gets the population.
	 * 
	 * @return
	 */
	public Population getPopulation() {
		return population;
	}
	
	/**
	 * Gets the total occupied area.
	 * 
	 * @return
	 */
	public double getTotalArea() {
		double sum = 0;
		for (Zone z: zones) {
			sum += z.getArea();
		}
		return sum;
	}

	public double getTotalResearchValue() {
		return res.getTotalResearchValue();
	}

	public double getTotalDevelopmentValue() {
		return dev.getTotalDevelopmentValue();
	}
	
	public double getAverageResearchActiveness() {
		return res.getAverageResearchActiveness();
	}
	
	public double getAverageDevelopmentActiveness() {
		return dev.getAverageDevelopmentActiveness();
	}
	
	public int getNumResearchProjects() {
		return res.getNumResearchProjects();
	}
	
	public int getNumDevelopmentProjects() {
		return dev.getNumDevelopmentProjects();
	}
	
	/**
	 * Gets the authority.
	 */
	public Authority getAuthority() {
		return sponsor;
	}

	public Coordinates getCoordinates() {
		return location;
	}
	
	/**
	 * Compares this object with the specified object for order.
	 *
	 * @param o the Object to be compared.
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(Colony	o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getContext() {
		return "Colony";
	}

	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		sponsor = null;
		location = null;
		population = null;
		nation = null; 
		zones.clear();
		zones = null;
		dev = null;
		eco = null;
		ind = null;
		res = null;
		researchZone = null;
		developmentZone = null;
		activities.clear();
		activities = null;
	}

}



