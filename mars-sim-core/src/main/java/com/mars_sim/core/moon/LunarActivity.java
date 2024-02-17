/*
 * Mars Simulation Project
 * LunarActivity.java
 * @date 2024-02-17
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.project.ColonyResearcher;
import com.mars_sim.core.moon.project.ColonySpecialist;
import com.mars_sim.core.moon.project.DevelopmentProject;
import com.mars_sim.core.moon.project.ResearchProject;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.tools.util.RandomUtil;

public class LunarActivity implements Temporal, Serializable {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(LunarActivity.class.getName());
	/** The demand factor for this activity (between -1 and 1). */
	private double demand = RandomUtil.getRandomDouble(0, .25);
	
	private LunarActivityType type;
	
	private Colony colony;
	
	/** A set of research projects this colony's researchers engage in. */
	private Set<ResearchProject> researchProjects = new HashSet<>();
	/** A set of engineering projects this colony's engineers engage in. */
	private Set<DevelopmentProject> engineeringProjects = new HashSet<>();
	
	LunarActivity(LunarActivityType type, Colony colony) {
		this.type = type;
		this.colony = colony;
	}

	/**
	 * Returns the colony.
	 * 
	 * @return
	 */
	public Colony getColony() {
		return colony;
	}
	
	/**
	 * Returns the LunarActivityType.
	 * 
	 * @return
	 */
	public LunarActivityType getLunarActivityType() {
		return type;
	}
	
	/**
	 * Returns the demand.
	 * 
	 * @return
	 */
	public double getDemand() {
		return demand;
	}
	
	/**
	 * Gets one researcher project that this researcher may join in.
	 * 
	 * @param researcher
	 * @return
	 */
	public ResearchProject getOneResearchProject(ColonyResearcher researcher) {
		for (ResearchProject p: researchProjects) {
			if (!p.getLead().equals(researcher)) {
				Set<Colonist> participants = p.getParticipants();
				for (Colonist c: participants) {
					if (!c.equals(researcher)) {
						return p;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets one engineering project that this engineer may join in.
	 * 
	 * @param Engineer
	 * @return
	 */
	public DevelopmentProject getOneEngineeringProject(ColonySpecialist engineer) {
		for (DevelopmentProject p: engineeringProjects) {
			if (!p.getLead().equals(engineer)) {
				Set<Colonist> participants = p.getParticipants();
				for (Colonist c: participants) {
					if (!c.equals(engineer)) {
						return p;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds a research project.
	 * 
	 * @param rp
	 */
	public void addResearchProject(ResearchProject rp) {
		researchProjects.add(rp);
	}
 	
	/**
	 * Adds an engineering project.
	 * 
	 * @param ep
	 */
	public void addEngineeringProject(DevelopmentProject ep) {
		engineeringProjects.add(ep);
	}
	
	public double getTotalDevelopmentValue() {
		double sum = 0;
		for (DevelopmentProject rp: engineeringProjects) {
			sum += rp.getDevelopmentValue();
		}
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
		double num = 0;
		double sum = 0;
		for (DevelopmentProject rp: engineeringProjects) {
			num++;
			sum += rp.getAverageDevelopmentActiveness();
		}
		
		if (num == 0)
			return 0;
		
		return sum / num;
	}
	
	public int getNumResearchProjects() {
		return researchProjects.size();
	}
	
	public int getNumDevelopmentProjects() {
		return engineeringProjects.size();
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		
		demand = demand + RandomUtil.getRandomDouble(-.002, .002);
		
		if (demand > 1)
			demand = 1;
		else if (demand < -1)
			demand = -1;
		
		return true;
	}
	
	
	/**
	 * Prepares for deletion.
	 */
	public void destroy() {
		type = null;
		colony = null;
		researchProjects.clear();
		researchProjects = null;
		engineeringProjects.clear();
		engineeringProjects = null;
	}
}
