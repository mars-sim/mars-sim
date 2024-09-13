/*
 * Mars Simulation Project
 * MissionRating.java
 * @date 2024-08-04
 * @author Manny Kung
 */
package com.mars_sim.core.mission.util;

import com.mars_sim.core.data.Rating;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.ai.mission.meta.MetaMission;
import com.mars_sim.core.person.ai.task.util.TaskJob;

/**
 * This represents a mission selection that has a meta mission and a rating score.
 */
public class MissionRating implements Rating {

	private MetaMission meta;
	private RatingScore score;

	public MissionRating(MetaMission meta, RatingScore score) {
		this.meta = meta;
		this.score = score;
	}

	@Override
	public String getName() {
		return meta.getName();
	}

	public MetaMission getMeta() {
		return meta;
	}
	
	@Override
	public RatingScore getScore() {
		return score;
	}

}
