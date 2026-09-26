/*
 * Mars Simulation Project
 * EquipmentInventory.java
 * @date 2025-07-15
 * @author Barry Evans
 */

package com.mars_sim.core.equipment;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.Unit;
import com.mars_sim.core.data.UnitSet;
import com.mars_sim.core.resource.AmountResource;
import com.mars_sim.core.resource.ResourceUtil;

/**
 * This class represents an Inventory that can hold equipment as well as resources. It provides
 * basic capacity management.
 */
public class EquipmentInventory extends MicroInventory
		implements EquipmentOwner, BinHolder{

	private static final long serialVersionUID = 1L;

	// May reuse: private static final SimLogger logger = SimLogger.getLogger(EquipmentInventory.class.getName())

	/** Locally held data recorder set. */
	private Set<Equipment> recorderSet;
	
	/** Locally held EVA suit set. */
	private Set<Equipment> suitSet;
	
	/** Locally held container set. */
	private Set<Equipment> containerSet;
	
	/** Locally held amount resource bin set. */
	private Set<AmountResourceBin> amountResourceBinSet;

	/**
	 * Construct an EquipmentInventory without any stock storage for Amount Resources.
	 * 
	 * @param owner The owner of this inventory; used for firing events.
	 * @param cargoCapacity The cargo capacity of this inventory.
	 */
	public EquipmentInventory(Unit owner, double cargoCapacity) {
		this(owner, cargoCapacity, 0D);
	}

	/**
	 * Construct a fully initialized EquipmentInventory.
	 * 
	 * @param owner The owner of this inventory; used for firing events.
	 * @param cargoCapacity The cargo capacity of this inventory.
	 * @param amountStockCapacity The stock capacity (overload) for specific amount resources.
	 */
	public EquipmentInventory(Unit owner, double cargoCapacity, double amountStockCapacity) {

		super(owner, cargoCapacity, amountStockCapacity);
	
		// Create equipment set
		recorderSet = new UnitSet<>();
		suitSet = new UnitSet<>();
		containerSet = new UnitSet<>();
				
		// Create the amount resource bin set
		amountResourceBinSet = new HashSet<>();
		
		var baskets = BinFactory.findBinMap(owner, amountResourceBinSet, BinType.BASKET);
		var crates = BinFactory.findBinMap(owner, amountResourceBinSet, BinType.CRATE);
		
		amountResourceBinSet.add(baskets);
		amountResourceBinSet.add(crates);
	}

	/**
	 * Gets the locally held amount resource bin set.
	 * 
	 * @return
	 */
	public Set<AmountResourceBin> getAmountResourceBinSet() {
		return Collections.unmodifiableSet(amountResourceBinSet);
	}
	
	/**
	 * Gets the total mass (stored resource and base equipment) of this inventory.
	 * 
	 * @return
	 */
	@Override
	public double getStoredMass() {
		double result = 0;
		for (Equipment e: suitSet) {
			result += e.getMass();
		}
		for (Equipment e: containerSet) {
			result += e.getMass();
		}
		for (Equipment e: recorderSet) {
			result += e.getMass();
		}
		
		return result + super.getStoredMass();
	}

	/**
	 * Gets the modified mass for a container. Useful when accounting for pushing a wheelbarrow, 
	 * instead of carrying a wheelbarrow.
	 * 
	 * @param type  the equipment type of interest
	 * @param percent the percent of mass of the equipment type to be treated as carrying mass
	 * @return
	 */
	public double getModifiedMass(EquipmentType type, double percent) {
		double result = 0;
		for (Equipment e: containerSet) {
			if (type == EquipmentType.WHEELBARROW) {
				result += e.getMass() * percent / 100;
			}
			else
				result += e.getMass();
		}
		return result +  super.getStoredMass();
	}
	
	/**
	 * Gets the equipment set.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		return Stream.of(containerSet, suitSet, recorderSet)
			    .flatMap(Set::stream)
			    .collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * Gets the container set.
	 *
	 * @return
	 */
	@Override
	public Set<Equipment> getContainerSet() {
		return Collections.unmodifiableSet(containerSet);
	}
	
	/**
	 * Gets the EVA suit set.
	 * 
	 * @return
	 */
	@Override
	public Set<Equipment> getSuitSet() {
		return Collections.unmodifiableSet(suitSet);
	}
	
	/**
	 * Gets the recorder set.
	 * 
	 * @return
	 */
	@Override
	public Set<Equipment> getRecorderSet() {
		return Collections.unmodifiableSet(recorderSet);
	}

	/**
	 * Does this unit possess an equipment of this equipment type ?
	 *
	 * @param typeID
	 * @return
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		if (type == EquipmentType.EVA_SUIT && suitSet.isEmpty())
			return false;
		else if (type == EquipmentType.DATA_RECORDER && recorderSet.isEmpty())
			return false;
		
		return containerSet.stream().anyMatch(e -> e.getEquipmentType() == type);
	}

	/**
	 * Adds an equipment to this unit.
	 *
	 * @param equipment
	 * @return true if this unit can carry it
	 */
	@Override
	public boolean addEquipment(Equipment equipment) {
		getOwner().fireUnitUpdate(EntityEventType.INVENTORY_STORING_UNIT_EVENT, equipment);
		
		if (equipment.getEquipmentType() == EquipmentType.EVA_SUIT) {
			return suitSet.add(equipment);
		}
		else if (equipment.getEquipmentType() == EquipmentType.DATA_RECORDER) {
			return recorderSet.add(equipment);
		}

		return containerSet.add(equipment); 
	}

	/**
	 * Removes an equipment.
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		getOwner().fireUnitUpdate(EntityEventType.INVENTORY_RETRIEVING_UNIT_EVENT, equipment);
		
		if (equipment.getEquipmentType() == EquipmentType.EVA_SUIT) {
			return suitSet.remove(equipment);
		}
		else if (equipment.getEquipmentType() == EquipmentType.DATA_RECORDER) {
			return recorderSet.remove(equipment);
		}
		
		return containerSet.remove(equipment);
	}


	@Override
	public boolean addBin(Bin bin) {
		var	binMap = BinFactory.findBinMap(getOwner(), getAmountResourceBinSet(), bin.getBinType());
		
		binMap.addBin(bin);
		
		return true;
	}

	/**
	 * Stores the resource.
	 *
	 * @param resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		// Note: this method is different from
		// Equipment's storeAmountResource
		if (!isResourceSupported(resource)) {
			// Since cargoCapacity is changing dynamically,
			// does it mean one must constantly update the capacity of this amount resource ?
			setResourceCapacityMap(Map.of(resource, getCargoCapacity()), false);
		}
		return super.storeAmountResource(resource, quantity);
	}

	/**
	 * Obtains the remaining cargo/general/shared capacity.
	 *
	 * @return remaining capacity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return getCargoCapacity() - getStoredMass();
	}
	
	/**
	 * Finds the number of empty containers of a particular equipment type.
	 * 
	 * Note: NOT for EVA suits.
	 * 
	 * @param containerType the equipment type.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		
		return (int) Collections.synchronizedSet(containerSet)
					.stream()
					.filter(e -> e.isEmpty(brandNew) 
						&& (e.getEquipmentType() == containerType))
					.count();
		
		
		// Note: the line .count() below trigger CME
//		return (int) containerSet.stream()
//					.filter(e -> e.isEmpty(brandNew) && (e.getEquipmentType() == containerType))
//					.count();
	}
	
	
	/**
	 * Finds the number of containers of a particular type.
	 *
	 * Note: will not count EVA suits.
	 * 
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return (int) containerSet.stream().filter(e -> e.getEquipmentType() == containerType).count();
	}
	
	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findContainersOfType(EquipmentType type) {
		Collection<Container> result = new HashSet<>();
		for (Equipment e : containerSet) {
			if (type == e.getEquipmentType()) {
				result.add((Container)e);
			}
		}
		return result;
	}
	
	/**
	 * Finds a container in storage.
	 *
	 * Note: will not count EVA suits.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	@Override
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		for (Equipment e : containerSet) {
			if (e.getEquipmentType() == containerType) {
				 Container c = (Container)e;
				// Check it matches the resource spec
				int containerResource = c.getResource();
				if (resource == -1 || containerResource == resource || containerResource == -1) {
					if (!empty || (c.getStoredMass() == 0D)) {
						return c;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds a container in storage.
	 *
	 * @param containerType
	 * @param personId
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	public Container findOwnedContainer(EquipmentType containerType, int personId, int resource) {
		for (Equipment e : containerSet) {
			if (e.getEquipmentType() == containerType) {
				 Container c = (Container)e;
				// Check it matches the resource spec
				int containerResource = c.getResource();
				if (resource == -1 || containerResource == resource || containerResource == -1) {
					if (e.getRegisteredOwnerID() == personId) {
						return c;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds a data recorder with a person's id.
	 * If not found, get an available recorder.
	 *
	 * @param personId
	 * @param retrieving
	 * @return
	 */
	public DataRecorder retrieveOwnedDataRecorder(int personId, boolean retrieving) {
		for (Equipment e : recorderSet) {
			DataRecorder dr = (DataRecorder)e;
			if (dr.checkRegisteredOwnerID(personId)) {
				if (retrieving) {
					recorderSet.remove(dr);
				}
				return dr;
			}
		}
		return findDataRecorder(retrieving);
	}
	
	/**
	 * Finds a data recorder.
	 *
	 * @param retrieving
	 * @return
	 */
	public DataRecorder findDataRecorder(boolean retrieving) {
		if (recorderSet.isEmpty()) {
			return null;
		}
	
		// Select the data recorder that has the least # of datasets
		Optional<Equipment> smallestDataset = recorderSet.stream()
				.min(Comparator.comparingInt(r -> ((DataRecorder)r).getDataset().size()));
		
		DataRecorder dr = (DataRecorder)smallestDataset.get();
		
		if (dr != null && retrieving) {
			recorderSet.remove(dr);
		}
	
		return dr;
	}

	
	
	/**
	 * Finds the number of data recorder .
	 *
	 * Note: will not count EVA suits.
	 * 
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	public int findNumDataRecorder() {
		return recorderSet.size();
	}
	
	/**
	 * Finds the number of empty bins of a particular bin type.
	 * 
	 * @param containerType the equipment type.
	 */
	@Override
	public int findNumBinsOfType(BinType binType) {
		for (AmountResourceBin arb : amountResourceBinSet) {
			if (arb.getBinType() == binType) {
				return arb.getBinMap().size();
			}
		}
		return 0;
	}
	
	/**
	 * Finds all of the bins of a particular type.
	 *
	 * @return collection of bins or empty collection if none.
	 */
	@Override
	public Collection<Bin> findBinsOfType(BinType binType){
		Collection<Bin> result = new HashSet<>();
		for (AmountResourceBin arb : amountResourceBinSet) {
			if (arb.getBinType() == binType) {
				result = arb.getBinMap().values();
			}
		}

		return result;
	}

	/**
	 * Gets a set of IDs of the specific amount resources being stored, 
	 * including those in containers and EVA suit set.
	 * Ignore any IDs that are zero amount.
	 * 
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceStoredIDs() {
		Set<Integer> set = new HashSet<>(super.getAllAmountResourceStoredIDs());
		for (Equipment e: containerSet) {
			if (e instanceof ResourceHolder rh) {
				set.addAll(rh.getSpecificResourceStoredIDs());
			}
		}
		for (Equipment e: suitSet) {
			if (e instanceof ResourceHolder rh) {
				set.addAll(rh.getSpecificResourceStoredIDs());
			}
		}
		for (Equipment e: recorderSet) {
			if (e instanceof ResourceHolder rh) {
				set.addAll(rh.getSpecificResourceStoredIDs());
			}
		}
		
		return set;
	}

	/**
	 * Is this unit empty ?
	 *
	 * @return true if this unit doesn't carry any resources or equipment
	 */
	@Override
	public boolean isEmpty() {
		if (!containerSet.isEmpty())
			return false;
		if (!suitSet.isEmpty())
			return false;
		return super.isEmpty();
	}
	
	/**
	 * Does it have this item resource ?
	 *
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasItemResource(int resource) {
		return getItemResourceIDs().contains(resource);
	}
	
	/**
	 * Checks if it has the container type.
	 * 
	 * @param type
	 * @return
	 */
	public boolean haveContainerType(BinType type) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public double getAmountResourceStored(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					return c.getBinMap().get(id).getAmount();
				}
			}
		}

		return -1;
	}

	@Override
	public double storeAmountResource(BinType type, int id, int resource, double quantity) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					double capacity = c.getCapacity();
					if (existingAmount + quantity > capacity) {
						mc.setAmount(capacity);
						return capacity - existingAmount - quantity;
					}
					else {
						mc.setAmount(existingAmount + quantity);
						return 0;
					}
				}
			}
		}
		
		return -1;
	}

	@Override
	public double retrieveAmountResource(BinType type, int id, int resource, double quantity) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					if (quantity > existingAmount) {
						mc.setAmount(0);
						return quantity - existingAmount;
					}
					else {
						mc.setAmount(existingAmount - quantity);
						return 0;
					}
				}
			}
		}
		
		return -1;
	}

	@Override
	public double getAmountResourceCapacity(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					return c.getCapacity();
				}
			}
		}

		return 0;
	}

	@Override
	public double getAmountResourceRemainingCapacity(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					double capacity = c.getCapacity();
					if (existingAmount <= capacity) {
						return capacity - existingAmount;
					}
					else {
						return 0;
					}
				}
			}
		}
		
		return -1;
	}

	@Override
	public boolean hasAmountResourceRemainingCapacity(BinType type, int id, int resource) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type) {
				AmountResource ar = ResourceUtil.findAmountResource(resource);
				if (ar != null && c.getBinMap().containsKey(id)) {
					Bin mc = c.getBinMap().get(id);
					double existingAmount = mc.getAmount();
					double capacity = c.getCapacity();
					if (existingAmount <= capacity) {
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	public double getCargoCapacity(BinType type, int id) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type && c.getBinMap().containsKey(id)) {
				return c.getCapacity();
			}	
		}
		
		return 0;
	}

	@Override
	public int getAmountResource(BinType type, int id) {
		for (AmountResourceBin c: amountResourceBinSet) {
			if (c.getBinType() == type && c.getBinMap().containsKey(id)) {
				return c.getBinMap().get(id).getAmountResource().getID();
			}
		}
		
		return 0;
	}

	public void destroy() {
		containerSet.clear();
		containerSet = null;
		suitSet.clear();
		suitSet = null;
		recorderSet.clear();
		recorderSet = null;
	}
}