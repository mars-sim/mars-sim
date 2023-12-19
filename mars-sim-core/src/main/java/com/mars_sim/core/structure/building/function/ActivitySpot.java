/*
 * Mars Simulation Project
 * ActivitySpot.java
 * @date 2023-11-21
 * @author Manny Kung
 */

package com.mars_sim.core.structure.building.function;

import java.io.Serializable;

import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.building.Building;
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
		private Building owner;

		private AllocatedSpot(Building owner, ActivitySpot spot) {
			this.spot = spot;
			this.owner = owner;
		}

		/**
		 * Leave a previously allocated activity spot.
		 * 
		 * @param w Worker releasing
		 * @param release Release the permanent reservation
		 */
		public void leave(Worker w, boolean release) {
			if (spot.leave(w, release)) {
				spot = null;
			}
		}

		/**
		 * Spot allocated to a Worker.
		 */
		public ActivitySpot getAllocated() {
			return spot;
		}

		/**
		 * Get the owner of this allocation.
		 * @return
		 */
		public Building getOwner() {
			return owner;
		}

		/**
		 * Get a description of the spot
		 */
        public String getSpotDescription() {
            return spot.getName() + " @ " + owner.getName();
        }
		

		@Override
		public int hashCode() {
			return owner.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AllocatedSpot other = (AllocatedSpot) obj;
			if (spot == null) {
				if (other.spot != null)
					return false;
			} else if (!spot.equals(other.spot))
				return false;
			if (owner == null) {
				if (other.owner != null)
					return false;
			} else if (!owner.equals(other.owner))
				return false;
			return true;
		}
	}

	private static final int EMPTY_ID = -1;

	private int id;
	private String name;
	private LocalPosition pos;

	private boolean permanent;
	
	ActivitySpot(String name, LocalPosition pos) {
		this.name = name;
		this.pos = pos;
		this.id = EMPTY_ID;
	}
	
	/**
	 * A Worker claims this spot. The claim is ignored if the spot if not available.
	 * Can only be claimed by a Function and not directly.
	 * Returns a callback instance to allow the spot to be released in the future.

	 * @param w Worker claiming
	 * @param permanent Permanent reservation
	 * @param allocator Building that is doing the allocation.
	 * @return Allocation reference or null if it is already allocated
	 */
	AllocatedSpot claim(Worker w, boolean permanent, Building allocator) {
		if (id == EMPTY_ID) {
			id = w.getIdentifier();
			this.permanent = permanent;
			return new AllocatedSpot(allocator, this);
		}
		return null;
	}

	/**
	 * Leaves this spot for the given worker. Release is ignored if the worker
	 * does not own the spot.
	 * 
	 * @param w Worker leaving the spot
	 * @param release Release any permanent reservation.
	 * @return Allocation releases
	 * 
	 */
	private boolean leave(Worker w, boolean release) {
		// Only leave it if still allocated to the worker
		if (id == w.getIdentifier() && (release || !permanent)) {
			id = EMPTY_ID;
			return true;
		}
		return false;
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

	/**
	 * Gets the name of the spot.
	 * @return Name of the spot
	 */
	public String getName() {
		return name;
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
