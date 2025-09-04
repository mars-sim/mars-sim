/*
 * Mars Simulation Project
 * ResearchProject.java
 * @date 2023-12-13
 * @author Manny Kung
 */
package com.mars_sim.core.moon.project;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Level;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.moon.Colonist;
import com.mars_sim.core.science.ScienceType;

public class ResearchProject extends LunarProject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final SimLogger logger = SimLogger.getLogger(ResearchProject.class.getName());

	/** Weight applied to the project lead in activeness averaging. */
	private static final double LEAD_WEIGHT = 2.0;

	/**
	 * Constructor.
	 *
	 * @param lead    the project lead (must not be null)
	 * @param name    the project name (must not be null)
	 * @param science the science type (must not be null)
	 */
	ResearchProject(Colonist lead, String name, ScienceType science) {
		super(
			Objects.requireNonNull(lead, "lead"),
			Objects.requireNonNull(name, "name"),
			Objects.requireNonNull(science, "science")
		);
	}

	public void addResearchValue(double value) {
		addValue(value);
	}

	public double getResearchValue() {
		return getValue();
	}

	/**
	 * Computes the weighted average research activeness for this project.
	 * <p>
	 * The lead counts twice (see {@link #LEAD_WEIGHT}); non-researcher
	 * participants (if any) are ignored rather than causing a class cast.
	 * If no valid contributors are present, returns {@code 0.0}.
	 *
	 * @return weighted average activeness in the range determined by contributor values
	 */
	public double getAverageResearchActiveness() {
		double sum = 0.0;
		double denom = 0.0;

		// Lead (weighted)
		if (getLead() instanceof ColonyResearcher) {
			ColonyResearcher lead = (ColonyResearcher) getLead();
			sum += lead.getActiveness() * LEAD_WEIGHT;
			denom += LEAD_WEIGHT;
		} else if (logger.isLoggable(Level.WARNING)) {
			logger.warning("Lead is not a ColonyResearcher; average excludes lead.");
		}

		// Participants (defensively handle any non-researchers)
		for (Object p : getParticipants()) {
			if (p instanceof ColonyResearcher) {
				ColonyResearcher cr = (ColonyResearcher) p;
				sum += cr.getActiveness();
				denom += 1.0;
			} else if (logger.isLoggable(Level.FINE)) {
				logger.fine("Ignoring non-researcher participant " + p);
			}
		}

		return (denom == 0.0) ? 0.0 : (sum / denom);
	}
}
