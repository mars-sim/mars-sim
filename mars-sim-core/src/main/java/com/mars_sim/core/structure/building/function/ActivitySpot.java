/*
 * Mars Simulation Project
 * ActivitySpot.java
 * @date 2023-11-21
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function;

import java.io.Serializable;

import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.mapdata.location.LocalPosition;

/**
 * Represents an activity spot that can be claimed by a Worker.
 */
public final class ActivitySpot implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * This provides a binding to an allocated ActivitySpot. Only an Allocated spot
	 * can be released.
	 */
	public static class AllocatedSpot implements Serializable {
		
		/** default serial id. */
		private static final long serialVersionUID = 1L;
		
		private ActivitySpot spot;

		private AllocatedSpot(ActivitySpot spot) {
			this.spot = spot;
		}

		/**
		 * Releases a previously allocated activity spot.
		 * 
		 * @param w Worker releasing
		 */
		public void release(Worker w) {
			spot.release(w);
			spot = null;
		}

		/**
		 * Spot allocated to a Worker.
		 */
		public ActivitySpot getAllocated() {
			return spot;
		}
	}

	private static final int EMPTY_ID = -1;

	private int id;

	private LocalPosition pos;
	
	ActivitySpot(LocalPosition pos) {
		this.pos = pos;
		this.id = EMPTY_ID;
	}
	
	/**
	 * A Worker claims this spot. The claim is ignored if the spot if not available.
	 * Can only be claimed by a Function and not directly.
	 * Returns a callback instance to allow the spot to be released in the future.

	 * @param w Worker claiming
	 * @return Allocation reference or null if it is already allocated
	 */
	AllocatedSpot claim(Worker w) {
		if (id == EMPTY_ID) {
			id = w.getIdentifier();
			return new AllocatedSpot(this);
		}
		return null;
	}

	/**
	 * Releases this spot for the given worker. Release is ignored if the worker
	 * does not own the spot.
	 * 
	 * @param w Worker doing the release.
	 */
	private void release(Worker w) {
		// Only release it if still allocated to the worker
		if (id == w.getIdentifier()) {
			id = EMPTY_ID;
		}
	}

	/**
	 * Is the spot empty and not claimed ?
	 * 
	 * @return is unallocated
	 */
	public boolean isEmpty() {
		return id == EMPTY_ID;
	}

	/**
	 * Gets the identifier of the Worker allocated.
	 */
	public int getID() {
		return id;
	}

	public LocalPosition getPos() {
		return pos;
	}
	
	public boolean hasSpot(int testID) {
		return (testID == id);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		LocalPosition p = ((ActivitySpot) obj).getPos();
		return p.equals(pos);
	}

	@Override
	public int hashCode() {
		return pos.hashCode();
	}
}
