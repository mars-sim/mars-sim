/*
 * Mars Simulation Project
 * ActivitySpot.java
 * @date 2023-11-21
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function;

import java.io.Serializable;

import com.mars_sim.mapdata.location.LocalPosition;

public final class ActivitySpot implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	// May add back private static SimLogger logger = SimLogger.getLogger(ActivitySpot.class.getName())

	private int id;

	private LocalPosition pos;
	
	public ActivitySpot(LocalPosition pos, int id) {
		this.pos = pos;
		this.id = id;
	}
	
	public int getID() {
		return id;
	}

	public LocalPosition getPos() {
		return pos;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		LocalPosition p = ((ActivitySpot) obj).getPos();
		int d = ((ActivitySpot)obj).getID();
		return p.equals(pos) && id == d;
	}
}
