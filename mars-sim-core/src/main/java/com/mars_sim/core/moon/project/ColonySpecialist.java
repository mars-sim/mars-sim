/*
 * Mars Simulation Project
 * Specialist.java
 * @date 2023-12-15
 * @author Manny Kung
 */

package com.mars_sim.core.moon.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.moon.Colony;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.Temporal;
import com.mars_sim.tools.util.RandomUtil;

public class ColonySpecialist extends Colonist implements Serializable, Temporal {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	// may add back private static final SimLogger logger = SimLogger.getLogger(ColonistEngineer.class.getName())
	
	private int numDevelopment = 0;
	
	private double activeness = 10;
	
	protected ScienceType scienceType;
	
	private Colony colony;
	
	/** A set of projects this specialist engages in. */
	private Set<EngineeringProject> projects = new HashSet<>();
	
	public ColonySpecialist(String name, Colony colony) {
		super(name, colony);
		this.colony = colony;
		
		scienceType = ScienceType.getRandomEngineeringSubject();
	}
    
	/**
	 * Creates an engineering project.
	 */
	public void createProject() {
		numDevelopment++;
		EngineeringProject proj = new EngineeringProject(this, scienceType.getName() + numDevelopment, scienceType);
		colony.addEngineeringProject(proj);
		projects.add(proj);
	}
	
	/**
	 * Joins an engineering project.
	 */
	public void joinProject() {
		EngineeringProject proj = colony.getOneEngineeringProject(this);
		if (proj != null && proj.canAddParticipants()) {
			numDevelopment++;
			proj.addParticipant(this);
			projects.add(proj);
		}
	}
	
	public double calculatMotivation(ClockPulse pulse) {
		int num = projects.size();

		int numEngineers = colony.getPopulation().getNumEngineers();
		
		int numEngineeringProjects = colony.getNumDevelopmentProjects();
		
		double aveProjPerEngineer = 1.0 * numEngineeringProjects / (.5 + numEngineers);
		
		return RandomUtil.getRandomDouble(pulse.getElapsed() / (1 + num));

	}
	
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		int num = projects.size();

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
					
			for (EngineeringProject p: projects) {
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
}
