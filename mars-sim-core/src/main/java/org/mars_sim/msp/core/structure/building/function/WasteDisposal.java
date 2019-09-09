/**
 * Mars Simulation Project
 * WasteDisposal.java
 * @version 3.1.0 2017-09-07
 * @author Manny Kung
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;

/**
 * The WasteDisposal class is a building function for waste disposal.
 */
public class WasteDisposal extends Function implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final FunctionType FUNCTION = FunctionType.WASTE_DISPOSAL;

	private int techLevel;
	private int peopleCapacity;
	private List<ScienceType> wasteSpecialties;
	private int peopleNum;

	/**
	 * Constructor.
	 * 
	 * @param building the building this function is for.
	 */
	public WasteDisposal(Building building) {
		// Use Function constructor
		super(FUNCTION, building);

		techLevel = buildingConfig.getWasteDisposalTechLevel(building.getBuildingType());
		peopleCapacity = buildingConfig.getWasteDisposalCapacity(building.getBuildingType());
		wasteSpecialties = buildingConfig.getWasteSpecialties(building.getBuildingType());

		// Load activity spots
		loadActivitySpots(buildingConfig.getWasteDisposalActivitySpots(building.getBuildingType()));
	}

	/**
	 * Gets the value of the function for a named building.
	 * 
	 * @param buildingName the building name.
	 * @param newBuilding  true if adding a new building.
	 * @param settlement   the settlement.
	 * @return value (VP) of building function.
	 */
	public static double getFunctionValue(String buildingName, boolean newBuilding, Settlement settlement) {

		double result = 0D;

		List<ScienceType> specialties = buildingConfig.getWasteSpecialties(buildingName);

		for (ScienceType specialty : specialties) {
			double wasteDisposalDemand = 0D;
			Iterator<Person> j = settlement.getAllAssociatedPeople().iterator();
			while (j.hasNext())
				wasteDisposalDemand += j.next().getSkillManager().getSkillLevel(specialty.getSkill());

			double wasteDisposalSupply = 0D;
			boolean removedBuilding = false;
			Iterator<Building> k = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
			while (k.hasNext()) {
				Building building = k.next();
				if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
					removedBuilding = true;
				} else {
					WasteDisposal wasteDisposalFunction = building.getWaste();
					int techLevel = wasteDisposalFunction.techLevel;
					int labSize = wasteDisposalFunction.peopleCapacity;
					double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
					for (int x = 0; x < wasteDisposalFunction.getTechSpecialties().length; x++) {
						ScienceType wasteDisposalSpecialty = wasteDisposalFunction.getTechSpecialties()[x];
						if (specialty.equals(wasteDisposalSpecialty)) {
							wasteDisposalSupply += techLevel * labSize * wearModifier;
						}
					}
				}
			}

			double existingWasteDisposalValue = wasteDisposalDemand / (wasteDisposalSupply + 1D);

			int techLevel = buildingConfig.getWasteDisposalTechLevel(buildingName);
			int labSize = buildingConfig.getWasteDisposalCapacity(buildingName);
			double buildingWasteDisposalSupply = techLevel * labSize;

			result += buildingWasteDisposalSupply * existingWasteDisposalValue;
		}

		return result;
	}

	/**
	 * Gets the waste disposal tech level of this building.
	 * 
	 * @return tech level
	 */
	public int getTechnologyLevel() {
		return techLevel;
	}

	/**
	 * Gets the number of people who can use the facility at once.
	 * 
	 * @return capacity
	 */
	public int getFacilitySize() {
		return peopleCapacity;
	}

	/**
	 * Gets an array of the building's waste disposal tech specialties.
	 * 
	 * @return array of specialties.
	 */
	public ScienceType[] getTechSpecialties() {
		return wasteSpecialties.toArray(new ScienceType[] {});
	}

	/**
	 * Checks to see if the laboratory has a given tech specialty.
	 * 
	 * @return true if lab has tech specialty
	 */
	public boolean hasSpecialty(ScienceType specialty) {
		return wasteSpecialties.contains(specialty);
	}

	/**
	 * Gets the number of people currently working on waste disposal.
	 * 
	 * @return number of people
	 */
	public int getWasteDisposalerNum() {
		return peopleNum;
	}

	/**
	 * Adds a people to the laboratory.
	 * 
	 * @throws Exception if person cannot be added.
	 */
	public void addWasteDisposaler() {
		peopleNum++;
		if (peopleNum > peopleCapacity) {
			peopleNum = peopleCapacity;
			throw new IllegalStateException("Lab already full of people.");
		}
	}

	/**
	 * Removes a people from the laboratory.
	 * 
	 * @throws Exception if person cannot be removed.
	 */
	public void removePeople() {
		peopleNum--;
		if (peopleNum < 0) {
			peopleNum = 0;
			throw new IllegalStateException("Lab is already empty of people.");
		}
	}

	/**
	 * Time passing for the building.
	 * 
	 * @param time amount of time passing (in millisols)
	 * @throws BuildingException if error occurs.
	 */
	public void timePassing(double time) {
		// Run each waste process.
		// TODO: what to do if grey water storage is full ?	
	}

	/**
	 * Gets the amount of power required when function is at full power.
	 * 
	 * @return power (kW)
	 */
	public double getFullPowerRequired() {
		return 0D;
	}

	/**
	 * Gets the amount of power required when function is at power down level.
	 * 
	 * @return power (kW)
	 */
	public double getPoweredDownPowerRequired() {
		return 0D;
	}

	@Override
	public double getMaintenanceTime() {

		double result = 0D;

		// Add maintenance for tech level.
		result += techLevel * 10D;

		// Add maintenance for people capacity.
		result += peopleCapacity * 10D;

		return result;
	}

	@Override
	public void destroy() {
		super.destroy();
		wasteSpecialties.clear();
		wasteSpecialties = null;
	}

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}

}