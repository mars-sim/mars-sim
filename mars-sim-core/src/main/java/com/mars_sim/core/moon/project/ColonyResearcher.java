/*
 * Mars Simulation Project
 * ColonyResearcher.java
 * @date 2022-10-05
 * @author Manny Kung
 */

package com.mars_sim.core.moon.project;

import java.util.HashSet;
import java.util.Set;

import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.moon.Colony;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.science.ResearchStudy;
import com.mars_sim.core.science.ScienceType;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.RandomUtil;

public class ColonyResearcher extends Colonist {
	
	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	// May add back private static final SimLogger logger = SimLogger.getLogger(ColonistResearcher.class.getName())
	
	private int numResearch = 0;

	private double activeness = 10;
		
	protected ScienceType mainScienceType;
	
	/** The researcher's skill manager. */
	private SkillManager skillManager;
	/** The person's research instance. */
	private ResearchStudy research;
	
	/** A set of research projects this researcher engage in. */
	private Set<ResearchProject> projects = new HashSet<>();
	
	public ColonyResearcher(String name, Colony colony) {
		super(name, colony);

		// Construct the ResearchStudy instance
		research = new ResearchStudy();
		
		// Determine the main science type
		mainScienceType = ScienceType.getRandomScienceType();	
	}
    
	/**
	 * Creates a research project.
	 */
	public void createProject() {
		numResearch++;
		ResearchProject proj = new ResearchProject(this, mainScienceType.getName() + numResearch, mainScienceType);
		getColony().addResearchProject(proj);
		projects.add(proj);
	}
	
	/**
	 * Joins a research project.
	 */
	public void joinProject() {
		ResearchProject proj = getColony().getOneResearchProject(this);
		if (proj != null && proj.canAddParticipants()) {
			numResearch++;
			proj.addParticipant(this);
			projects.add(proj);
		}
	}
	
	@Override
	public boolean timePassing(ClockPulse pulse) {
		int num = projects.size();
		
		double time = pulse.getElapsed();
		
		var colony = getColony();
		
		int numResearchers = colony.getPopulation().getNumResearchers();
		
		int numResearchProjects = colony.getNumResearchProjects();
		
		double aveProjPerResearcher = 1.0 * numResearchProjects / (.5 + numResearchers);
		
		double motivation = RandomUtil.getRandomDouble(time / (1 + num));
		
		addActiveness(motivation);
		
		if (RandomUtil.getRandomDouble(100) <= activeness) {

			int rand = RandomUtil.getRandomInt((int)(5 + aveProjPerResearcher));
		
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

			double timeValue = time / 100;
			double expertiseValue = Math.log10(1 + experience) * activeness / (1 + num);
			double resourceValue = getResearchArea() / numResearchProjects;
			double compositeValue = timeValue * expertiseValue * resourceValue; 
					
			for (ResearchProject p: projects) {
				if (p.getLead().equals(this)) {
					double value = RandomUtil.getRandomDouble(compositeValue);
					p.addResearchValue(value);
				}
				else {
					int numParticipants = p.getNumParticipants();
					double value = RandomUtil.getRandomDouble(compositeValue / numParticipants);
					p.addResearchValue(value);
				}
			}
		}
	
		// Primary researcher; my responsibility to update Study
		research.timePassing(pulse);
		
		return true;
	}

	private double getResearchArea() {
		return getColony().getResearchArea();
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
	
	protected ScienceType getMainScienceType() {
		return mainScienceType;
	}

	/**
	 * Gets the research study instance.
	 * 
	 * @return
	 */
	public ResearchStudy getResearchStudy() {
		return research;
	}
	
	/**
	 * Gets the skill manager instance.
	 * 
	 * @return
	 */
	@Override
	public SkillManager getSkillManager() {
		return skillManager;
	}
}
