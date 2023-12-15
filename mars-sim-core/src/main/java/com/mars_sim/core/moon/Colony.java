/*
 * Mars Simulation Project
 * Colony.java
 * @date 2023-09-25
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.authority.Nation;
import com.mars_sim.core.authority.Organization;
import com.mars_sim.core.logging.Loggable;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.project.ResearchProject;
import com.mars_sim.core.moon.project.ColonistResearcher;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.mapdata.location.Coordinates;

public class Colony implements Serializable, Temporal, Loggable, Comparable<Colony> {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(Colony.class.getName());

	private boolean startup = true;
	
	private int id;
	
	private String name;
	
	/** The settlement's ReportingAuthority instance. */
	private Authority sponsor;

	private Coordinates location;
	
	private Population population;
	
	private Simulation sim;
	
	private Nation nation;
	
	private Zone researchZone;
	
	private Zone developmentZone;
	
	private Set<Zone> zones = new HashSet<>();
	/** A set of research projects this colony's researchers engage in. */
	private Set<ResearchProject> researchProjects = new HashSet<>();
	
	public Colony(int id, String name, Authority sponsor, Coordinates location) {
		this.id = id;
		this.name = name;
		this.sponsor = sponsor;
		this.location = location;
		
		population = new Population(this);

		for (ZoneType type: ZoneType.values()) {
			
			Zone zone = new Zone(type, this);
			if (type == ZoneType.RESEARCH) {
				researchZone = zone;
			}
			else if (type == ZoneType.ENGINEERING) {
				developmentZone = zone;
			}
			
			addZone(zone);
		}
	}
	
	public void init() {
		population.init();
	}

	
	/**
	 * Gets one researcher project that this researcher may join in.
	 * 
	 * @param researcher
	 * @return
	 */
	public ResearchProject getOneResearchProject(ColonistResearcher researcher) {
		for (ResearchProject p: researchProjects) {
			if (!p.getLead().equals(researcher)) {
				Set<ColonistResearcher> participants = p.getParticipants();
				for (ColonistResearcher r: participants) {
					if (!r.equals(researcher)) {
						return p;
					}
				}
			}
		}
		return null;
	}
	
	public void addResearchProject(ResearchProject rp) {
		researchProjects.add(rp);
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
		return getResearchZone().getGrowthRate();
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
		return getDevelopmentZone().getGrowthRate();
	}

	/**
	 * Gets the organization.
	 * 
	 * @return
	 */
	public Organization getOrganization() {
		return sponsor.getOrganization();
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		if (startup && pulse.isNewMSol()) {
			startup = false;
			init();
		}
		
		getOrganization().timePassing(pulse);
		
		population.timePassing(pulse);
		
		for (Zone z: zones) {
			z.timePassing(pulse);
		}
		
		return true;
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

	public double getTotalDevelopmentValue() {
		double sum = 0;
//		for (DevelopmentProject rp: developmentProjects) {
//			sum += rp.getDevelopmentValue();
//		}
		return sum;
	}
	
	public double getTotalResearchValue() {
		double sum = 0;
		for (ResearchProject rp: researchProjects) {
			sum += rp.getResearchValue();
		}
		return sum;
	}
	
	public double getAverageResearchActiveness() {
		double num = 0;
		double sum = 0;
		for (ResearchProject rp: researchProjects) {
			num++;
			sum += rp.getAverageResearchActiveness();
		}
		
		if (num == 0)
			return 0;
		
		return sum / num;
	}
	
	public double getAverageDevelopmentActiveness() {
//		double num = 0;
//		double sum = 0;
//		for (DevelopmentProject rp: developmentProjects) {
//			num++;
//			sum += rp.getAverageDevelopmentActiveness();
//		}
//		
//		if (num == 0)
			return 0;
		
//		return sum / num;
	}
	
	public int getNumResearchProjects() {
		return researchProjects.size();
	}
	
	public int getNumDevelopmentProjects() {
		return 0; //developmentProjects.size();
	}
	
	/**
	 * Gets the authority.
	 */
	public Authority getAuthority() {
		return sponsor;
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
	public Coordinates getCoordinates() {
		return location;
	}

	@Override
	public Unit getContainerUnit() {
		if (sim == null) {
			sim = Simulation.instance();
		}
		return sim.getUnitManager().getMoon();
	}
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		sponsor = null;
		location = null;
		population = null;
		sim = null;
		nation = null; 
		zones.clear();
		zones = null;
	}
}



