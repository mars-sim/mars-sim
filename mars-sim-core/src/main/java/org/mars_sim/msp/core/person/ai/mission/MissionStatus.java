/*
 * Mars Simulation Project
 * MissionStatus.java
 * @date 2023-07-02
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.goods.GoodsUtil;

import java.io.Serializable;

public class MissionStatus implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Factory helper method to create a status based on a resource.
	 */
	public static MissionStatus createResourceStatus(int missingResourceId) {
		String resourceName = GoodsUtil.getGood(missingResourceId).getName();
		return new MissionStatus("Mission.status.noResources", resourceName);
	}

	private String name;

	public MissionStatus(String key) {
		this.name = Msg.getString(key);
	}

	public MissionStatus(String key, String argument) {
		this.name  = Msg.getString(key, argument);
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MissionStatus other = (MissionStatus) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
