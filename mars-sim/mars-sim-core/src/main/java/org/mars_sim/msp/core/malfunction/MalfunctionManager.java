/**
 * Mars Simulation Project
 * MalfunctionManager.java
 * @version 2.84 2008-05-12
 * @author Scott Davis
 */

package org.mars_sim.msp.core.malfunction;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import org.mars_sim.msp.core.*;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.person.*;
import org.mars_sim.msp.core.person.medical.*;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.Part;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The MalfunctionManager class manages the current malfunctions in a unit.
 */
public class MalfunctionManager implements Serializable {
    
    private static String CLASS_NAME = "org.mars_sim.msp.simulation.malfunction.MalfunctionManager";
	
    private static Logger logger = Logger.getLogger(CLASS_NAME);

  // Unit update events.
   public static final String MALFUNCTION_EVENT = "malfunction";
	
	// The default time (millisols) to perform maintenance work.
    private static double DEFAULT_MAINTENANCE_WORK_TIME = 1000D;
    
    // Initial estimate for malfunctions per orbit for an entity.
    private static double ESTIMATED_MALFUNCTIONS_PER_ORBIT = 10D;
    
    // Initial estimate for maintenances per orbit for an entity.
    private static double ESTIMATED_MAINTENANCES_PER_ORBIT = 10D;
    
    // Data members
    private Malfunctionable entity;          // The owning entity.
    private double timeSinceLastMaintenance; // Time passing (in millisols) since
                                             // last maintenance on entity.
    private double effectiveTimeSinceLastMaintenance; // Time (millisols) that entity has been 
                                                      // actively used since last maintenance.
    private double maintenanceWorkTime;      // The required work time for maintenance on entity.
    private double maintenanceTimeCompleted; // The completed
    private Collection<String> scope;        // The scope strings of the unit.
    private Collection<Malfunction> malfunctions; // The current malfunctions in the unit.
    private Map<Part, Integer> partsNeededForMaintenance; // The parts currently needed to maintain this entity.
    private int numberMalfunctions;          // The number of malfunctions the entity has had so far.
    private int numberMaintenances;          // The number of times the entity has been maintained so far.
    
    // Life support modifiers.
    private double oxygenFlowModifier = 100D;
    private double waterFlowModifier = 100D;
    private double airPressureModifier = 100D;
    private double temperatureModifier = 100D;

    /**
     * Constructor
     * @param entity the malfunctionable entity.
     */
    public MalfunctionManager(Malfunctionable entity) {

        // Initialize data members
        this.entity = entity;
        timeSinceLastMaintenance = 0D;
        effectiveTimeSinceLastMaintenance = 0D;
        scope = new ArrayList<String>();
        malfunctions = new ArrayList<Malfunction>();
        maintenanceWorkTime = DEFAULT_MAINTENANCE_WORK_TIME;
    }

    /**
     * Add a unit scope string to the manager.
     * @param scopeString a unit scope string
     */
    public void addScopeString(String scopeString) {
        if ((scopeString != null) && !scope.contains(scopeString))
        scope.add(scopeString);
        
        // Update maintenance parts.
        determineNewMaintenanceParts();
    }

    /**
     * Checks if entity has a malfunction.
     * @return true if malfunction
     */
    public boolean hasMalfunction() {
        return (malfunctions.size() > 0);
    }

    /**
     * Checks if the entity has a given malfunction.
     * @return true if entity has malfunction
     */
    public boolean hasMalfunction(Malfunction malfunction) {
        return malfunctions.contains(malfunction);
    }

    /**
     * Checks if entity has any emergency malfunctions.
     * @return true if emergency malfunction
     */
    public boolean hasEmergencyMalfunction() {
        boolean result = false;

        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if ((malfunction.getEmergencyWorkTime() -
                    malfunction.getCompletedEmergencyWorkTime()) > 0D) result = true;
            }
        }

        return result;
    }

    /**
     * Checks if entity has any normal malfunctions.
     * @return true if normal malfunction
     */
    public boolean hasNormalMalfunction() {
        boolean result = false;

        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if ((malfunction.getWorkTime() -
                    malfunction.getCompletedWorkTime()) > 0D) result = true;
            }
        }

        return result;
    }

    /**
     * Checks if entity has any EVA malfunctions.
     * @return true if EVA malfunction
     */
    public boolean hasEVAMalfunction() {
        boolean result = false;

        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if ((malfunction.getEVAWorkTime() -
                    malfunction.getCompletedEVAWorkTime()) > 0D) result = true;
            }
        }

        return result;
    }

    /**
     * Gets a list of the unit's current malfunctions.
     * @return malfunction list
     */
    public List<Malfunction> getMalfunctions() {
        return new ArrayList<Malfunction>(malfunctions);
    }

    /**
     * Gets the most serious malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0;

        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if ((malfunction.getSeverity() > highestSeverity) && !malfunction.isFixed()) {
                    highestSeverity = malfunction.getSeverity();
                    result = malfunction;
                }
            }
        }

        return result;
    }

    /**
     * Gets the most serious emergency malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousEmergencyMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0D;

        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if ((malfunction.getEmergencyWorkTime() - malfunction.getCompletedEmergencyWorkTime()) > 0D) {
                    if (malfunction.getSeverity() > highestSeverity) {
                        highestSeverity = malfunction.getSeverity();
                        result = malfunction;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the most serious normal malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousNormalMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0D;

        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if ((malfunction.getWorkTime() - malfunction.getCompletedWorkTime()) > 0D) {
                    if (malfunction.getSeverity() > highestSeverity) {
                        highestSeverity = malfunction.getSeverity();
                        result = malfunction;
                    }
                }
            }
        }

        return result;
    }
    
    /**
     * Gets a list of all normal malfunctions sorted by highest severity first.
     * @return list of malfunctions.
     */
    public List<Malfunction> getNormalMalfunctions() {
    	List<Malfunction> result = new ArrayList<Malfunction>();
    	Iterator<Malfunction> i = malfunctions.iterator();
    	while (i.hasNext()) {
    		Malfunction malfunction = i.next();
    		if ((malfunction.getWorkTime() - malfunction.getCompletedWorkTime()) > 0D) 
    			result.add(malfunction);
    	}
    	Collections.sort(result, new MalfunctionSeverityComparator());
    	return result;
    }

    /**
     * Gets the most serious EVA malfunction the entity has.
     * @return malfunction
     */
    public Malfunction getMostSeriousEVAMalfunction() {

        Malfunction result = null;
        double highestSeverity = 0D;

        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if ((malfunction.getEVAWorkTime() - malfunction.getCompletedEVAWorkTime()) > 0D) {
                    if (malfunction.getSeverity() > highestSeverity) {
                        highestSeverity = malfunction.getSeverity();
                        result = malfunction;
                    }
                }
            }
        }

        return result;
    }
    
    /**
     * Gets a list of all EVA malfunctions sorted by highest severity first.
     * @return list of malfunctions.
     */
    public List<Malfunction> getEVAMalfunctions() {
    	List<Malfunction> result = new ArrayList<Malfunction>();
    	Iterator<Malfunction> i = malfunctions.iterator();
    	while (i.hasNext()) {
    		Malfunction malfunction = i.next();
    		if ((malfunction.getEVAWorkTime() - malfunction.getCompletedEVAWorkTime()) > 0D) 
    			result.add(malfunction);
    	}
    	Collections.sort(result, new MalfunctionSeverityComparator());
    	return result;
    }

    /**
     * Adds a randomly selected malfunction to the unit (if possible).
     */
    private void addMalfunction() {
        MalfunctionFactory factory = Simulation.instance().getMalfunctionFactory();
        Malfunction malfunction = factory.getMalfunction(scope);
        if (malfunction != null) {
        	addMalfunction(malfunction);
        	numberMalfunctions++;
        }
    }
    
    /**
     * Adds a malfunction to the unit.
     * @param malfunction the malfunction to add.
     */
    void addMalfunction(Malfunction malfunction) {
    	if (malfunction == null) throw new IllegalArgumentException("malfunction is null");
    	else {
    		malfunctions.add(malfunction);
            
            try {
            	getUnit().fireUnitUpdate(MALFUNCTION_EVENT, malfunction);
            }
            catch (Exception e) {
            	e.printStackTrace(System.err);
            }
            
            HistoricalEvent newEvent = new MalfunctionEvent(entity, malfunction, false);
			Simulation.instance().getEventManager().registerNewEvent(newEvent);

            issueMedicalComplaints(malfunction);
    	}
    }

    /**
     * Time passing while the unit is being actively used.
     * @param time amount of time passing (in millisols)
     */
    public void activeTimePassing(double time) {

        effectiveTimeSinceLastMaintenance += time;

        // Check for malfunction due to lack of maintenance.
        double chance = time * .0000001D * effectiveTimeSinceLastMaintenance;

        if (RandomUtil.lessThanRandPercent(chance)) {
        	int solsLastMaint =  (int) (effectiveTimeSinceLastMaintenance / 1000D);
            logger.info(entity.getName() + 
            		" has maintenance-triggered malfunction: " + 
            		"time since late maintenance: " + 
            		solsLastMaint + " Sols.");
            addMalfunction();
        }
    }

    /**
     * Time passing for unit.
     * @param time amount of time passing (in millisols)
     */
    public void timePassing(double time) {

        Collection<Malfunction> fixedMalfunctions = new ArrayList<Malfunction>();

        // Check if any malfunctions are fixed.
        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if (malfunction.isFixed()) fixedMalfunctions.add(malfunction);
            }
        }

        if (fixedMalfunctions.size() > 0) {
            Iterator<Malfunction> i = fixedMalfunctions.iterator();
            while (i.hasNext()) {
                Malfunction item = i.next();
                malfunctions.remove(item);
                
                try {
                	getUnit().fireUnitUpdate(MALFUNCTION_EVENT, item);
                }
                catch (Exception e) {
                	e.printStackTrace(System.err);
                }
                
                HistoricalEvent newEvent = new MalfunctionEvent(entity, item, true);
				Simulation.instance().getEventManager().registerNewEvent(newEvent);
            }
        }

        // Determine life support modifiers.
        setLifeSupportModifiers(time);

        // Deplete resources.
        try {
        	depleteResources(time);
        }
        catch(Exception e) {
        	e.printStackTrace(System.err);
        }
        
        // Add time passing.
		timeSinceLastMaintenance += time;
    }

    /**
     * Determine life support modifiers for given time.
     * @param time amount of time passing (in millisols)
     */
    public void setLifeSupportModifiers(double time) {

        double tempOxygenFlowModifier = 0D;
        double tempWaterFlowModifier = 0D;
        double tempAirPressureModifier = 0D;
        double tempTemperatureModifier = 0D;

        // Make any life support modifications.
        if (hasMalfunction()) {
            Iterator<Malfunction> i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = i.next();
                if (malfunction.getEmergencyWorkTime() > malfunction.getCompletedEmergencyWorkTime()) {
                    Map effects = malfunction.getLifeSupportEffects();
                    if (effects.get("Oxygen") != null)
                        tempOxygenFlowModifier += ((Double) effects.get("Oxygen")).doubleValue();
                    if (effects.get("Water") != null)
                        tempWaterFlowModifier += ((Double) effects.get("Water")).doubleValue();
                    if (effects.get("Air Pressure") != null)
                        tempAirPressureModifier += ((Double) effects.get("Air Pressure")).doubleValue();
                    if (effects.get("Temperature") != null)
                        tempTemperatureModifier += ((Double) effects.get("Temperature")).doubleValue();
                }
            }
        }

        if (tempOxygenFlowModifier < 0D) oxygenFlowModifier += tempOxygenFlowModifier * time;
        else oxygenFlowModifier = 100D;

        if (tempWaterFlowModifier < 0D) waterFlowModifier += tempWaterFlowModifier * time;
        else waterFlowModifier = 100D;

        if (tempAirPressureModifier < 0D) airPressureModifier += tempAirPressureModifier * time;
        else airPressureModifier = 100D;

        if (tempTemperatureModifier != 0D) temperatureModifier += tempTemperatureModifier * time;
        else temperatureModifier = 100D;
    }

    /**
     * Depletes resources due to malfunctions.
     * @param time amount of time passing (in millisols)
     * @throws Exception if error depleting resources.
     */
    public void depleteResources(double time) throws Exception {

        if (hasMalfunction()) {
            Iterator i = malfunctions.iterator();
            while (i.hasNext()) {
                Malfunction malfunction = (Malfunction) i.next();
                if (malfunction.getEmergencyWorkTime() > malfunction.getCompletedEmergencyWorkTime()) {
                    Map effects = malfunction.getResourceEffects();
                    Iterator i2 = effects.keySet().iterator();
                    while (i2.hasNext()) {
                        AmountResource resource = (AmountResource) i2.next();
                        double amount = ((Double) effects.get(resource)).doubleValue();
                        double amountDepleted = amount * time;
                        Inventory inv = entity.getInventory();
                        double amountStored = inv.getAmountResourceStored(resource);
                        if (amountStored < amountDepleted) amountDepleted = amountStored;
                        try {
                        	inv.retrieveAmountResource(resource, amountDepleted);
                        }
                        catch (InventoryException e) {}
                    }
                }
            }
        }
    }

    /**
     * Called when the unit has an accident.
     */
    public void accident() {

        logger.info(entity.getName() + " accident()");

        // Multiple malfunctions may have occured.
        // 50% one malfunction, 25% two etc.
        boolean done = false;
        double chance = 100D;
        while (!done) {
            if (RandomUtil.lessThanRandPercent(chance)) {
                addMalfunction();
                chance /= 2D;
            }
            else done = true;
        }
        
		// Add stress to people affected by the accident.
		Collection<Person> people = entity.getAffectedPeople();
		Iterator<Person> i = people.iterator();
		while (i.hasNext()) {
			PhysicalCondition condition = i.next().getPhysicalCondition();
			condition.setStress(condition.getStress() + PhysicalCondition.ACCIDENT_STRESS);
		}
    }

    /**
     * Gets the time since last maintenance on entity.
     * @return time (in millisols)
     */
    public double getTimeSinceLastMaintenance() {
        return timeSinceLastMaintenance;
    }
    
    /**
     * Gets the time the entity has been actively used
     * since its last maintenance.
     * @return time (in millisols)
     */
    public double getEffectiveTimeSinceLastMaintenance() {
    	return effectiveTimeSinceLastMaintenance;
    }

    /**
     * Gets the required work time for maintenance for the entity.
     * @return time (in millisols)
     */
    public double getMaintenanceWorkTime() {
        return maintenanceWorkTime;
    }

    /**
     * Sets the required work time for maintenance for the entity.
     * @param maintenanceWorkTime (in millisols)
     */
    public void setMaintenanceWorkTime(double maintenanceWorkTime) {
        this.maintenanceWorkTime = maintenanceWorkTime;
    }

    /**
     * Gets the work time completed on maintenance.
     * @return time (in millisols)
     */
    public double getMaintenanceWorkTimeCompleted() {
        return maintenanceTimeCompleted;
    }

    /**
     * Add work time to maintenance.
     * @param time (in millisols)
     */
    public void addMaintenanceWorkTime(double time) {
    	maintenanceTimeCompleted += time;
    	if (maintenanceTimeCompleted >= maintenanceWorkTime) {
    		maintenanceTimeCompleted = 0D;
    		timeSinceLastMaintenance = 0D;
    		effectiveTimeSinceLastMaintenance = 0D;
    		determineNewMaintenanceParts();
    		numberMaintenances++;
        }
    }

    /**
     * Issues any necessary medical complaints.
     * @param malfunction the new malfunction
     */
    public void issueMedicalComplaints(Malfunction malfunction) {

        // Determine medical complaints for each malfunction.
        Iterator i1 = malfunction.getMedicalComplaints().keySet().iterator();
        while (i1.hasNext()) {
            String complaintName = (String) i1.next();
            double probability = ((Double) malfunction.getMedicalComplaints().get(complaintName)).doubleValue();
            MedicalManager medic = Simulation.instance().getMedicalManager();
            Complaint complaint = medic.getComplaintByName(complaintName);
            if (complaint != null) {
            	// Get people who can be affected by this malfunction.
                Iterator<Person> i2 = entity.getAffectedPeople().iterator();
                while (i2.hasNext()) {
                    Person person = i2.next();
                    if (RandomUtil.lessThanRandPercent(probability)) {
                    	person.getPhysicalCondition().addMedicalComplaint(complaint);
                    	person.fireUnitUpdate(PhysicalCondition.ILLNESS_EVENT);
                    }
                }
            }
        }
    }

    /**
     * Gets the oxygen flow modifier.
     * @return modifier
     */
    public double getOxygenFlowModifier() {
        return oxygenFlowModifier;
    }

    /**
     * Gets the water flow modifier.
     * @return modifier
     */
    public double getWaterFlowModifier() {
        return waterFlowModifier;
    }

    /**
     * Gets the air flow modifier.
     * @return modifier
     */
    public double getAirPressureModifier() {
        return airPressureModifier;
    }

    /**
     * Gets the temperature modifier.
     * @return modifier
     */
    public double getTemperatureModifier() {
        return temperatureModifier;
    }
    
    /**
     * Gets the unit associated with this malfunctionable.
     * @return associated unit.
     * @throws Exception if error finding associated unit.
     */
    private Unit getUnit() throws Exception {
    	if (entity instanceof Unit) return (Unit) entity;
    	else if (entity instanceof Building) 
    		return ((Building) entity).getBuildingManager().getSettlement();
    	else throw new Exception("Could not find unit associated with malfunctionable.");
    }
    
    /**
     * Determines a new set of required maintenance parts.
     */
    private void determineNewMaintenanceParts() {
    	if (partsNeededForMaintenance == null) partsNeededForMaintenance = new HashMap<Part, Integer>();
    	partsNeededForMaintenance.clear();
    	
    	Iterator<String> i = scope.iterator();
    	while (i.hasNext()) {
    		String entity = i.next();
    		Iterator<Part> j = Part.getParts().iterator();
    		while (j.hasNext()) {
    			Part part = j.next();
    			if (part.hasMaintenanceEntity(entity)) {
    				if (RandomUtil.lessThanRandPercent(part.getMaintenanceProbability(entity))) {
    					int number = RandomUtil.getRandomRegressionInteger(part.getMaintenanceMaximumNumber(entity));
    					if (partsNeededForMaintenance.containsKey(part)) number += partsNeededForMaintenance.get(part);
    					partsNeededForMaintenance.put(part, number);
    				}
    			}
    		}
    	}
    }
    
    /**
     * Gets the parts needed for maintenance on this entity.
     * @return map of parts and their number.
     */
    public Map<Part, Integer> getMaintenanceParts() {
    	if (partsNeededForMaintenance == null) partsNeededForMaintenance = new HashMap<Part, Integer>();
    	return new HashMap<Part, Integer>(partsNeededForMaintenance);
    }
    
    /**
     * Adds a number of a part to the entity for maintenance.
     * @param part the part.
     * @param number the number used.
     */
    public void maintainWithParts(Part part, int number) {
    	if (part == null) throw new IllegalArgumentException("part is null");
    	if (partsNeededForMaintenance.containsKey(part)) {
    		int numberNeeded = partsNeededForMaintenance.get(part);
    		if (number > numberNeeded) throw new IllegalArgumentException("number " + number + 
    				" is greater that number of parts needed: " + numberNeeded);
    		else {
    			numberNeeded -= number;
    			if (numberNeeded > 0) partsNeededForMaintenance.put(part, numberNeeded);
    			else partsNeededForMaintenance.remove(part);
    		}
    	}
    	else throw new IllegalArgumentException("Part " + part + " is not needed for maintenance.");
    }

    /**
     * Gets off of the repair part probabilities for the malfunctionable.
     * @return maps of parts and probable number of parts needed per malfunction.
     * @throws Exception if error finding probabilities.
     */
	public Map<Part, Double> getRepairPartProbabilities() throws Exception {
		MalfunctionFactory factory = Simulation.instance().getMalfunctionFactory();
		return factory.getRepairPartProbabilities(scope);
	}
	
	public Map<Part, Double> getMaintenancePartProbabilities() throws Exception {
		MalfunctionFactory factory = Simulation.instance().getMalfunctionFactory();
		return factory.getMaintenancePartProbabilities(scope);
	}
	
    /**
     * Gets the estimated number of malfunctions this entity will
     * have in one Martian orbit.
     * @return number of malfunctions.
     */
    public double getEstimatedNumberOfMalfunctionsPerOrbit() {
    	
    	MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
    	MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
    	double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
    	double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
    	
    	double avgMalfunctionsPerOrbit = numberMalfunctions / totalTimeOrbits;
    	
    	if (totalTimeOrbits < 1D) {
    		double estimateTime = 1D - totalTimeOrbits;
    		double estimatedMalfunctionsPerOrbit = ESTIMATED_MALFUNCTIONS_PER_ORBIT * estimateTime;
    		avgMalfunctionsPerOrbit += estimatedMalfunctionsPerOrbit;
    	}
    	
    	return avgMalfunctionsPerOrbit;
    }
    
    /**
     * Gets the estimated number of periodic maintenances this entity will 
     * have in one Martian orbit.
     * @return number of maintenances.
     */
    public double getEstimatedNumberOfMaintenancesPerOrbit() {
    	
    	MarsClock startTime = Simulation.instance().getMasterClock().getInitialMarsTime();
    	MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
    	double totalTimeMillisols = MarsClock.getTimeDiff(currentTime, startTime);
    	double totalTimeOrbits = totalTimeMillisols / 1000D / MarsClock.SOLS_IN_ORBIT_NON_LEAPYEAR;
    	
    	double avgMaintenancesPerOrbit = numberMaintenances / totalTimeOrbits;
    	
    	if (totalTimeOrbits < 1D) {
    		double estimateTime = 1D - totalTimeOrbits;
    		double estimatedMaintenancesPerOrbit = ESTIMATED_MAINTENANCES_PER_ORBIT * estimateTime;
    		avgMaintenancesPerOrbit += estimatedMaintenancesPerOrbit;
    	}
    	
    	return avgMaintenancesPerOrbit;
    }
	
	/**
	 * Inner class comparator for sorting malfunctions my highest severity to lowest.
	 */
	private class MalfunctionSeverityComparator implements Comparator<Malfunction>, Serializable {
		public int compare(Malfunction malfunction1, Malfunction malfunction2) {
			int severity1 = malfunction1.getSeverity();
			int severity2 = malfunction2.getSeverity();
			if (severity1 > severity2) return -1;
			else if (severity1 == severity2) return 0;
			else return 1;
		}
	}
}