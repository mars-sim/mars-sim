/*
 * Mars Simulation Project
 * EquipmentOwner.java
 * @date 2023-05-17
 * @author Manny Kung
 */
package com.mars_sim.core.equipment;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.structure.Settlement;

public interface EquipmentOwner extends ItemHolder, ResourceHolder, Serializable {
	  
	/**
	 * Gets the total mass held in this entity.
	 * 
	 * @return
	 */
	double getStoredMass();

	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 * 
	 * @return collection of containers or empty collection if none.
	 */
	Collection<Container> findContainersOfType(EquipmentType type);
	
	/**
	 * Gets the equipment set.
	 * 
	 * @return
	 */
	Set<Equipment> getEquipmentSet();
	
	/**
	 * Gets the container set.
	 * 
	 * @return
	 */
	Set<Equipment> getContainerSet();
	
	/**
	 * Gets the EVA suit set.
	 * 
	 * @return
	 */
	Set<Equipment> getSuitSet();

	/**
	 * Gets the recorder set.
	 * 
	 * @return
	 */
	Set<Equipment> getRecorderSet();
	
	/**
	 * Does this inventory possess an equipment of this type ?
	 * 
	 * @param typeID
	 * @return
	 */
	boolean containsEquipment(EquipmentType type);
	
	/**
	 * Adds an equipment to this inventory.
	 * 
	 * @param equipment
	 * @return true if this inventory can carry it
	 */
	boolean addEquipment(Equipment equipment);
	
	/**
	 * Removes an equipment.
	 * 
	 * @param equipment
	 */
	boolean removeEquipment(Equipment equipment);
	
	/**
	 * Stores the item resource.
	 * 
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	int storeItemResource(int resource, int quantity);
	
	/**
	 * Retrieves the item resource.
	 * 
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	int retrieveItemResource(int resource, int quantity);
	
	/**
	 * Gets the item resource stored.
	 * 
	 * @param resource
	 * @return quantity
	 */
	int getItemResourceStored(int resource);
	
	/**
	 * Gets all stored item resources.
	 * 
	 * @return all stored item resources.
	 */
	Set<Integer> getItemResourceIDs();
	
	/**
	 * Finds the number of empty containers of a class that are contained in storage and have
	 * an empty inventory.
	 * 
	 * @param containerClass  the unit class.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew);
	
	/**
	 * Finds the number of containers of a particular type.
	 * 
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	int findNumContainersOfType(EquipmentType containerType);
	
	/**
	 * Finds a container in storage.
	 * 
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource 
	 * @return instance of container or null if none.
	 */
	Container findContainer(EquipmentType containerType, boolean empty, int resource);
	
	/**
	 * Obtains the remaining general storage space.
	 * 
	 * @return quantity
	 */
	double getRemainingCargoCapacity();
	
	/**
	 * Does it have this item resource ?
	 * 
	 * @param resource
	 * @return
	 */
	boolean hasItemResource(int resource);
	
	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	boolean hasAmountResourceRemainingCapacity(int resource);

	/**
	 * Gets the equipment owner from the source object.
	 * 
	 * @param source This could come from many source type classes
	 * @return EquipmentOwner or null if not found.
	 */
	static EquipmentOwner getAttached(Object source) {
		return switch (source) {
			case EquipmentOwner eo -> eo;
			case Worker w -> w.getEquipmentInventory();
			case Settlement s -> s.getEquipmentInventory();
			default -> null;
		};
	}

}
