/*
 * Mars Simulation Project
 * ColonistEngineer.java
 * @date 2023-12-15
 * @author Manny Kung
 */

package com.mars_sim.core.moon.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.moon.Colony;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.mapdata.location.Coordinates;
import com.mars_sim.tools.util.RandomUtil;

public class ColonistEngineer extends Colonist implements Serializable, Temporal {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	// may add back private static final SimLogger logger = SimLogger.getLogger(ColonistEngineer.class.getName())
	
	private int numDevelopment = 0;
	
	private double activeness = 10;
	
	private String name;
	
	protected ScienceType mainEngineering;
	
	private Colony colony;
	
	/** A set of research projects this researcher engage in. */
	private Set<EngineeringProject> engineeringProjects = new HashSet<>();
	
	public ColonistEngineer(String name, Colony colony) {
		super(name, colony);
		this.name = name;
		this.colony = colony;
		
		mainEngineering = ScienceType.getRandomEngineeringSubject();
	}
    
	/**
	 * Creates an engineering project.
	 */
	public void createProject() {
		numDevelopment++;
		EngineeringProject proj = new EngineeringProject(this, mainEngineering.getName() + numDevelopment, mainEngineering);
		colony.addEngineeringProject(proj);
		engineeringProjects.add(proj);
	}
	
	/**
	 * Joins an engineering project.
	 */
	public void joinProject() {
		EngineeringProject proj = colony.getOneEngineeringProject(this);
		if (proj != null && proj.canAddParticipants()) {
			numDevelopment++;
			proj.addParticipant(this);
			engineeringProjects.add(proj);
		}
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		int num = engineeringProjects.size();

		int numEngineers = colony.getPopulation().getNumEngineers();
		
		int numEngineeringProjects = colony.getNumDevelopmentProjects();
		
		double aveProjPerEngineer = 1.0 * numEngineeringProjects / (.5 + numEngineers);
		
		double motivation = RandomUtil.getRandomDouble(pulse.getElapsed() / (1 + num));
		
		addActiveness(motivation);
		
		if (RandomUtil.getRandomDouble(100) <= activeness) {

			int rand = RandomUtil.getRandomInt((int)(5 + aveProjPerEngineer));
		
			// Limit the number of research projects that can be carried out by each researcher
			if (rand == 0 && num < 4) {
				createProject();
				activeness = 0;
			}
			else {
				joinProject();
				activeness = 0;
			}
		}
		
		if (pulse.isNewHalfSol()) {
			// Update the experience once every half sol
			double experience = getTotalSkillExperience();
//			logger.info(colony.getName() + " - " + name + " exp: " + Math.round(experience * 100.0)/100.0);

			double timeValue = pulse.getElapsed() / 100;
			double expertiseValue = Math.log10(1 + experience) * activeness / (1 + num);
			double resourceValue = getDevelopmentArea() / numEngineeringProjects;
			double compositeValue = timeValue * expertiseValue * resourceValue; 
					
			for (EngineeringProject p: engineeringProjects) {
				if (p.getLead().equals(this)) {
					double value = RandomUtil.getRandomDouble(compositeValue);
					p.addDevelopmentValue(value);
				}
				else {
					int numParticipants = p.getNumParticipants();
					double value = RandomUtil.getRandomDouble(compositeValue / numParticipants);
					p.addDevelopmentValue(value);
				}
			}
		}
	
		return true;
	}
	

	private double getDevelopmentArea() {
		return colony.getDevelopmentArea();
	}
	
	
	public void addActiveness(double value) {
		if (activeness + value > 100) {
			activeness = 100;
		}
		else {
			activeness += value;
		}
	}
	
	public double getActiveness() {
		return activeness;
	}
	
	public void setColony(Colony newColony) {
		colony = newColony;
	}


	public Coordinates getCoordinates() {
		if (colony != null) {
			return colony.getCoordinates();
		}
		return null;
	}
}
