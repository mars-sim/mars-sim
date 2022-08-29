/**
 * Mars Simulation Project
 * MissionStatus.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.person.ai.mission;

import org.mars_sim.msp.core.Msg;
import java.io.Serializable;

public class MissionStatus implements Serializable {

	private String name;

	public MissionStatus(String key) {
		this.name = Msg.getString(key);
	}

	MissionStatus(String key, String argument) {
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
