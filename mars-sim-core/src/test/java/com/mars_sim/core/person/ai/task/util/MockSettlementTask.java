package com.mars_sim.core.person.ai.task.util;

import com.mars_sim.core.Entity;
import com.mars_sim.core.data.RatingScore;
import com.mars_sim.core.person.ai.task.util.MetaTask.TaskScope;
import com.mars_sim.core.structure.Settlement;

class MockSettlementTask extends SettlementTask {

	private static final long serialVersionUID = 1L;

	MockSettlementTask(SettlementMetaTask parent, Settlement owner, String name, Entity focus, RatingScore score) {
		super(parent, owner, name, focus, score);
	}

	public MockSettlementTask(SettlementMetaTask parent, Settlement owner, String name, Entity focus,
			RatingScore score, TaskScope scope) {
		super(parent, owner, name, focus, score);

		setScope(scope);
	}

	void updateDemand(int demand) {
		setDemand(demand);
	}
}