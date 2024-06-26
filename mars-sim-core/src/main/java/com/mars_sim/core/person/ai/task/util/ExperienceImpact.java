/*
 * Mars Simulation Project
 * ExperienceImpact.java
 * @date 2024-04-07
 * @author Barry Evans
 */
package com.mars_sim.core.person.ai.task.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.time.MarsTime;

/**
 * This represents impact applied to a Worker for doing an activity. It cover improvements to skills
 * as well as physical impact on the worker, e.g. stress for a Person.
 */
public class ExperienceImpact implements Serializable {
    
    private static final long serialVersionUID = 1L;
	// Taken from BotTaskManager; should be driven by RobotType
    private static final double ROBOT_WORK_POWER = 0.2D;

    /**
     * Represents a weight of the importance of a skill.
     */
    public record SkillWeight(SkillType skill, int weight) implements Serializable {}

    /**
     * Range of physical effort
     */
    public enum PhysicalEffort {NONE, LOW, HIGH}

    private double skillRatio;
    private Set<SkillWeight> skillWeights;
    private NaturalAttributeType experienceInfluence;
    private double stressModifier;
    private double totalSkillWeight;
    private Set<SkillType> skills;
    private PhysicalEffort effortDriven;

    /**
     * Cuts down constructor using a varargs of skills.
     * 
     * @param skillRatio Ratio to use when updating skills
     * @param effortDriven This activity requires effort
     * @param stressModifier Modifier value for how stressful the activity is
     * @param experienceAttribute Natural attribute used to assess how much the worker can learn
     * @param skills Skill required/improved for this activity; evenly weighted
     */
    public ExperienceImpact(double skillRatio, NaturalAttributeType experienceAttribute,
                            PhysicalEffort effortDriven, double stressModifier, SkillType... skills) {
        this(skillRatio, experienceAttribute, effortDriven, stressModifier, toSkillWeights(skills));
    }

    /**
     * Cuts down constructor using a varargs of skills.
     * 
     * @param skillRatio Ratio to use when updating skills
     * @param effortDriven This activity requires effort
     * @param stressModifier Modifier value for how stressful the activity is
     * @param experienceAttribute Natural attribute used to assess how much the worker can learn
     * @param skills Skill required/improved for this activity; evenly weighted
     */
    public ExperienceImpact(double skillRatio, NaturalAttributeType experienceAttribute,
                            boolean effortDriven, double stressModifier, SkillType... skills) {
        this(skillRatio, experienceAttribute, (effortDriven ? PhysicalEffort.LOW : PhysicalEffort.NONE),
                        stressModifier, toSkillWeights(skills));
    }

    /**
     * Converts a collection of SkillType to an evenly balanced SkillWeights.
     * 
     * @param skills
     * @return
     */
    public static Set<SkillWeight> toSkillWeights(SkillType[] skills) {
        Set<SkillWeight> sw = new HashSet<>();
        for(var s : skills) {
            if (s != null) {
                sw.add(new SkillWeight(s, 1));
            }
        }
        return sw;
    }

    /**
     * Fully qualified constructor allowing full definition of the impact.
     * 
     * @param skillRatio Ratio to use when updating skills
     * @param stressModifier Used to changed stress coupled with the effectiveness of Worker
     * @param effortDriven This activity requires effort
     * @param experienceAttribute Natural attribute used to assess how much the worker can learn
     * @param skillWeights The Skills affected by this experience with associated weights
     */
    public ExperienceImpact(double skillRatio, NaturalAttributeType experienceAttribute,
                            boolean effortDriven, double stressModifier,
                            Set<SkillWeight> skillWeights) {
        this(skillRatio, experienceAttribute, (effortDriven ? PhysicalEffort.LOW : PhysicalEffort.NONE),
                stressModifier, skillWeights);
    }

    /**
     * Fully qualified constructor allowing full definition of the impact.
     * 
     * @param skillRatio Ratio to use when updating skills
     * @param stressModifier Used to changed stress coupled with the effectiveness of Worker
     * @param effortDriven This activity requires effort
     * @param experienceAttribute Natural attribute used to assess how much the worker can learn
     * @param skillWeights The Skills affected by this experience with assoicated weights
     */
    public ExperienceImpact(double skillRatio, NaturalAttributeType experienceAttribute,
                            PhysicalEffort effortDriven, double stressModifier,
                            Set<SkillWeight> skillWeights) {
        this.skillRatio = skillRatio;
        this.skillWeights = skillWeights;
        this.experienceInfluence = experienceAttribute;
        this.effortDriven = effortDriven;
        this.stressModifier = stressModifier;
        this.totalSkillWeight = skillWeights.stream().mapToInt(SkillWeight::weight).sum();
        this.skills = skillWeights.stream().map(SkillWeight::skill).collect(Collectors.toSet());
    }

    /**
     * Applies impacts to a worker for a given duration.
     * 
     * @param worker Worker in question
     * @param duration Duration they did the activity
     * @param assistance Further percentage modifier to reflect local circumstances of assistance
     * @param effectiveness How effective where they doing the work
     */
    public void apply(Worker worker, double duration, double assistance, double effectiveness) {
        // Update skills
        if (!skillWeights.isEmpty()) {
            // Points calculated
            // 1) By the duration divided by the skill ratio
            // 2) Multiplied by any assitence, e.g. teaching
            double newPoints = (duration / skillRatio) * assistance;

            // 3) Natural ability to handle the experience
            newPoints *= getExperienceModifier(worker);

            // 4) Pro-rate the new points according to the individual skill weights
			SkillManager sm = worker.getSkillManager();
			for (var skillType : skillWeights) {
				sm.addExperience(skillType.skill(), (newPoints * skillType.weight())/totalSkillWeight,
                                    duration);
			}
		}

        // Update natural attributes?

        // Update person
        if (worker instanceof Person p) {
            applyPersonBenfits(p, duration, effectiveness);
        }
        else if (worker instanceof Robot r) {
            applyRobotBenefits(r, duration);
        }
    }

    /**
     * Returns the experience modifier for this Worker to learn from this experience.
     * 
     * @param worker
     * @return Should return a positive value
     */
    protected double getExperienceModifier(Worker worker) {
        // Experience points adjusted by worker natural attribute.
        int experienceAptitude = worker.getNaturalAttributeManager().getAttribute(experienceInfluence);
        return 1D + ((experienceAptitude - 50D) / 100D);
    }

    /**
     * Applies the energy time to a robot.
     * 
     * @param r
     * @param duration
     */
    private void applyRobotBenefits(Robot r, double duration) {
        double energyTime = duration;
		    
        // Double energy expenditure if performing effort-driven task.
        if (effortDriven != PhysicalEffort.NONE) {
            energyTime *= 2D;
        }

        // Checks if the robot is charging
        if (energyTime > 0.01 && !r.getSystemCondition().isCharging()) {
            // Expend energy based on activity.
            r.consumeEnergy(energyTime * MarsTime.HOURS_PER_MILLISOL * ROBOT_WORK_POWER, energyTime);
        }
    }
    
    /**
     * Applies the stress to a person.
     * 
     * @param p
     * @param duration
     * @param effectiveness
     */
    private void applyPersonBenfits(Person p, double duration, double effectiveness) {
        // This is odd but matches the original in Task
		double stressRatio = stressModifier - (stressModifier * effectiveness);

        // Reduce stress modifier for person's skill related to the task.
        if (stressRatio != 0D) {
            double deltaStress = stressRatio * duration;
            p.getPhysicalCondition().addStress(deltaStress);
        }

        // need to modify happiness & other emotions

        // Check effort
        if (effortDriven != PhysicalEffort.NONE) {
            double energyTime = duration * p.getPerformanceRating();
    
            // Double energy expenditure if performing effort-driven task.
            if (energyTime > 0D) {

                if (p.isOutside()) {
                    // Take more energy to be in EVA doing work
                    
                	// Consider skill level 
                	double skill = p.getSkillManager().getEffectiveSkillLevel(SkillType.EVA_OPERATIONS);
            		if (skill == 0)
            			skill = .5;
                	
                	// Consider the strength, resilience, endurance, agility
		  			int strength = p.getNaturalAttributeManager()
							.getAttribute(NaturalAttributeType.STRENGTH);
		  			int resilience = p.getNaturalAttributeManager()
							.getAttribute(NaturalAttributeType.STRESS_RESILIENCE);
		  			int endurance = p.getNaturalAttributeManager()
							.getAttribute(NaturalAttributeType.ENDURANCE);
		  			int agility = p.getNaturalAttributeManager()
							.getAttribute(NaturalAttributeType.AGILITY);
		  			
		  			double modifier = 1 + .25/skill 
		  					 + (400 - 1.5 * strength 
		  							- 1.25 * endurance 
		  							- 0.75 * agility
		  							- 0.5 * resilience
		  							) / 800D; 
		  			
                	energyTime = energyTime * modifier;
                }
                
                p.getPhysicalCondition().reduceEnergy(energyTime);
            }

            if (effortDriven == PhysicalEffort.HIGH) {
                p.getPhysicalCondition().stressMuscle(duration);
            }
         }
    }

	/**
	 * Gets the effective skill level a worker compared to this experience. 
	 * 
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel(Worker w) {
        SkillManager manager = w.getSkillManager();
        double result = skillWeights.stream()
                        .mapToDouble(sw -> sw.weight()
                            * manager.getEffectiveSkillLevel(sw.skill()))
                        .sum();

        // Take the average
        return (int)(result / totalSkillWeight);
	}

    /**
     * What skills are impacted in this experience
     * @return
     */
    public Set<SkillType> getImpactedSkills() {
        return skills;
    }

    /**
     * Does this experience require effort and impact energy/fatigue?
     */
    public PhysicalEffort getEffortRequired() {
        return effortDriven;
    }

    /**
     * Create a new varient of this Experience but with a different 
     * @param newSkillsRatio The new skill ratio
     * @return
     */
    public ExperienceImpact changeSkillsRatio(double newSkillsRatio) {
        return new ExperienceImpact(newSkillsRatio, experienceInfluence, effortDriven, newSkillsRatio, skillWeights);
    }
}
