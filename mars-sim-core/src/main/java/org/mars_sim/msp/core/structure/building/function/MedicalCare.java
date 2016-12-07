/**
 * Mars Simulation Project
 * MedicalCare.java
 * @version 3.07 2014-11-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.MedicalAssistance;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.person.medical.HealthProblem;
import org.mars_sim.msp.core.person.medical.MedicalAid;
import org.mars_sim.msp.core.person.medical.MedicalStation;
import org.mars_sim.msp.core.person.medical.Treatment;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;

/**
 * The MedicalCare class represents a building function for providing medical care.
 */
public class MedicalCare
extends Function
implements MedicalAid, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static final BuildingFunction FUNCTION = BuildingFunction.MEDICAL_CARE;

    private MedicalStation medicalStation;

	public static AmountResource toxicWasteAR = AmountResource.findAmountResource("toxic waste");
	
    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if function could not be constructed.
     */
    public MedicalCare(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        int techLevel = config.getMedicalCareTechLevel(building.getBuildingType());
        int beds = config.getMedicalCareBeds(building.getBuildingType());
        medicalStation = new MedicalStation(techLevel, beds);

        // Load activity spots
        loadActivitySpots(config.getMedicalCareActivitySpots(building.getBuildingType()));
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Demand is 5 medical points per inhabitant.
        double demand = settlement.getAllAssociatedPeople().size() * 5D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                MedicalCare medFunction = (MedicalCare) building.getFunction(FUNCTION);
                double tech = medFunction.getTechLevel();
                double beds = medFunction.getSickBedNum();
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += (tech * tech) * beds * wearModifier;
            }
        }

        double medicalPointValue = demand / (supply + 1D) / 10D;

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double tech = config.getMedicalCareTechLevel(buildingName);
        double beds = config.getMedicalCareBeds(buildingName);
        double medicalPoints = (tech * tech) * beds;

        return medicalPoints * medicalPointValue;
    }

    /**
     * Gets the number of sick beds.
     * @return Sick bed count.
     */
    public int getSickBedNum() {
        return medicalStation.getSickBedNum();
    }

    /**
     * Gets the current number of people being treated here.
     * @return Patient count.
     */
    public int getPatientNum() {
        return medicalStation.getPatientNum();
    }

    /**
     * Gets the patients at this medical station.
     * @return Collection of People.
     */
    public Collection<Person> getPatients() {
        return medicalStation.getPatients();
    }

    /**
     * Gets the number of people using this medical aid to treat sick people.
     * @return number of people
     */
    public int getPhysicianNum() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof MedicalAssistance) {
                        MedicalAid aid = ((MedicalAssistance) task).getMedicalAid();
                        if ((aid != null) && (aid == this)) result++;
                    }
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    @Override
    public List<HealthProblem> getProblemsAwaitingTreatment() {
        return medicalStation.getProblemsAwaitingTreatment();
    }

    @Override
    public List<HealthProblem> getProblemsBeingTreated() {
        return medicalStation.getProblemsBeingTreated();
    }

    @Override
    public List<Treatment> getSupportedTreatments() {
        return medicalStation.getSupportedTreatments();
    }

    @Override
    public boolean canTreatProblem(HealthProblem problem) {
        return medicalStation.canTreatProblem(problem);	
    }

    @Override
    public void requestTreatment(HealthProblem problem) {
        medicalStation.requestTreatment(problem);
    }
    
    @Override
    public void cancelRequestTreatment(HealthProblem problem) {
        medicalStation.cancelRequestTreatment(problem);
    }

    @Override
    public void startTreatment(HealthProblem problem, double treatmentDuration) {
        medicalStation.startTreatment(problem, treatmentDuration);
    }

    @Override
    public void stopTreatment(HealthProblem problem) {
        medicalStation.stopTreatment(problem);
    }
    
    @Override
    public List<Person> getRestingRecoveryPeople() {
        return medicalStation.getRestingRecoveryPeople();
    }
    
    @Override
    public void startRestingRecovery(Person person) {
        medicalStation.startRestingRecovery(person);
    }
    
    @Override
    public void stopRestingRecovery(Person person) {
        medicalStation.stopRestingRecovery(person);
    }

    @Override
    public void timePassing(double time) {

        // Do nothing.
    }

    @Override
    public double getFullPowerRequired() {
        return 0D;
    }

    @Override
    public double getPoweredDownPowerRequired() {
        return 0D;
    }

    /**
     * Gets the treatment level.
     * @return treatment level
     */
    public int getTechLevel() {
        return medicalStation.getTreatmentLevel();
    }

    @Override
    public double getMaintenanceTime() {

        double result = 0D;

        // Add maintenance for treatment level.
        result += medicalStation.getTreatmentLevel() * 10D;

        // Add maintenance for number of sick beds.
        result += medicalStation.getSickBedNum() * 10D;

        return result;
    }

    @Override
    public void destroy() {
        super.destroy();

        medicalStation = null;
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