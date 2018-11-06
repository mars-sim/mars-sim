/**
 * Mars Simulation Project
 * Part.java
 * @version 3.1.0 2017-09-05
 * @author Scott Davis
 */
package org.mars_sim.msp.core.resource;

import java.io.Serializable;
import java.util.*;

import org.mars_sim.msp.core.Msg;
//import org.mars_sim.msp.core.structure.building.Building;

/**
 * The Part class represents a type of unit resource that is used for
 * maintenance and repairs.
 */
public class Part extends ItemResource {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Domain members
	private List<MaintenanceEntity> maintenanceEntities;

	/**
	 * Constructor.
	 * 
	 * @param name        the name of the part.
	 * @param id          the id# of the part
	 * @param description {@link String}
	 * @param mass        the mass of the part (kg)
	 * @param the         sol when this part is put to use
	 */
	public Part(String name, int id, String description, double mass, int solsUsed) {
		// Use ItemResource constructor.
		super(name, id, description, mass, solsUsed);

		maintenanceEntities = new ArrayList<MaintenanceEntity>();
	}

	/**
	 * Adds a maintenance entity for the part.
	 * 
	 * @param name        the name of the entity.
	 * @param probability the probability of the part being needed for maintenance.
	 * @param maxNumber   the maximum number of parts needed for maintenance.
	 */
	void addMaintenanceEntity(String name, int probability, int maxNumber) {
		maintenanceEntities.add(new MaintenanceEntity(name, probability, maxNumber));
	}

	/**
	 * Checks if the part has a maintenance entity of a given name.
	 * 
	 * @param entityName the name of the entity.
	 * @return true if part has the maintenance entity.
	 */
	public boolean hasMaintenanceEntity(String entityName) {
		if (entityName == null) {
			throw new IllegalArgumentException(Msg.getString("Part.error.nameIsNull")); //$NON-NLS-1$
		}
		boolean result = false;
		Iterator<MaintenanceEntity> i = maintenanceEntities.iterator();
		while (i.hasNext()) {
			if (i.next().name.equalsIgnoreCase(entityName)) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Gets the percentage probability of a part being needed by an maintenance
	 * entity.
	 * 
	 * @param entityName the name of the entity.
	 * @return percentage probability (0 - 100)
	 */
	public int getMaintenanceProbability(String entityName) {
		if (entityName == null) {
			throw new IllegalArgumentException(Msg.getString("Part.error.nameIsNull")); //$NON-NLS-1$
		}
		int result = 0;
		Iterator<MaintenanceEntity> i = maintenanceEntities.iterator();
		while (i.hasNext()) {
			MaintenanceEntity entity = i.next();
			if (entity.name.equalsIgnoreCase(entityName)) {
				result = entity.probability;
			}
		}
		return result;
	}

	/**
	 * Gets the maximum number of this part needed by a maintenance entity.
	 * 
	 * @param entityName the name of the entity.
	 * @return maximum number of parts.
	 */
	public int getMaintenanceMaximumNumber(String entityName) {
		if (entityName == null) {
			throw new IllegalArgumentException(Msg.getString("Part.error.nameIsNull")); //$NON-NLS-1$
		}
		int result = 0;
		Iterator<MaintenanceEntity> i = maintenanceEntities.iterator();
		while (i.hasNext()) {
			MaintenanceEntity entity = i.next();
			if (entity.name.equalsIgnoreCase(entityName)) {
				result = entity.maxNumber;
			}
		}
		return result;
	}

	/**
	 * Gets a set of all parts.
	 * 
	 * @return set of parts.
	 */
	public static Set<Part> getParts() {
		return ItemResourceUtil.getItemResources();
	}

	/**
	 * Gets a set of all parts.
	 * 
	 * @return set of parts.
	 */
	public static Set<Integer> getItemIDs() {
		return ItemResourceUtil.getItemIDs();
//    			
//        Set<Integer> result = new HashSet<>();
//
//        Iterator<Integer> i = ItemResourceUtil.getItemIDs().iterator();
//        while (i.hasNext()) {
//        	Integer resource = i.next();
////            if (resource instanceof Part) {
//                result.add(resource);
////            }
//        }
//        
///*
//        Iterator<Part> i = ItemResource.getItemResources().iterator();
//        while (i.hasNext()) {
//            Part p = i.next();
//            result.add(p);
//        }
//*/        
//        return result;
	}

	public final List<MaintenanceEntity> getMaintenanceEntities() {
		return this.maintenanceEntities;
	}

	/**
	 * A private inner class for holding maintenance entity information.
	 */
	public static class MaintenanceEntity implements Serializable {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Domain members
		private String name;
		private int probability;
		private int maxNumber;

		/**
		 * Constructor
		 * 
		 * @param name        name of the entity.
		 * @param probability the probability of this part being needed for maintenance.
		 * @param maxNumber   the maximum number of this part needed for maintenance.
		 */
		private MaintenanceEntity(String name, int probability, int maxNumber) {
			if (name == null) {
				throw new IllegalArgumentException(Msg.getString("Part.error.nameIsNull")); //$NON-NLS-1$
			}
			this.name = name;
			this.probability = probability;
			this.maxNumber = maxNumber;
		}

		public int getProbability() {
			return probability;
		}

		public int getMaxNumber() {
			return maxNumber;
		}

		public String getName() {
			return name;
		}
	}
}
