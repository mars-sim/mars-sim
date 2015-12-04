/**
 * Mars Simulation Project
 * LivingAccommodations.java
 * @version 3.07 2015-01-28
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.time.MarsClock;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LivingAccommodations class is a building function for a living accommodations.
 */
public class LivingAccommodations extends Function implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /* default logger.*/
 	private static Logger logger = Logger.getLogger(LivingAccommodations.class.getName());   

    /** Amount of water in kg used per person per Sol for cleaning, bathing, etc. */
    //public final static double WASH_WATER_USAGE_PERSON_SOL = 26D;
    public final static double TOILET_WASTE_PERSON_SOL = .02D;
    //public final static double GREY_WATER_RATIO = .8;
    //public final static double BLACK_WATER_RATIO = .2;

    private static final BuildingFunction FUNCTION = BuildingFunction.LIVING_ACCOMODATIONS;

    private int beds;
    private int sleepers;
	//private int solCache = 1;
    
    private double wasteWaterUsage; // wasteWaterUsage per person per millisol
    private double greyWaterFraction; // percent portion of grey water generated
    
    private Settlement settlement;
    private Inventory inv;

    
    /**
     * Constructor
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public LivingAccommodations(Building building) {
        // Call Function constructor.
        super(FUNCTION, building);

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();
        beds = buildingConfig.getLivingAccommodationBeds(building.getBuildingType());
        
        PersonConfig personconfig = SimulationConfig.instance().getPersonConfiguration();
        wasteWaterUsage = personconfig.getWaterUsageRate() / 1000D;
        double grey2BlackWaterRatio = personconfig.getGrey2BlackWaterRatio();
        greyWaterFraction = grey2BlackWaterRatio / (grey2BlackWaterRatio + 1);

        
        // Load activity spots
        loadActivitySpots(buildingConfig.getLivingAccommodationsActivitySpots(building.getBuildingType()));
        
        settlement = building.getBuildingManager().getSettlement();
        inv = building.getInventory();

    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName,
            boolean newBuilding, Settlement settlement) {

        // Demand is two beds for every inhabitant (with population expansion in mind).
        double demand = settlement.getAllAssociatedPeople().size() * 2D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding
                    && building.getBuildingType().equalsIgnoreCase(buildingName)
                    && !removedBuilding) {
                removedBuilding = true;
            } else {
                LivingAccommodations livingFunction = (LivingAccommodations) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
                supply += livingFunction.beds * wearModifier;
            }
        }

        double bedCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance()
                .getBuildingConfiguration();
        double bedCapacity = config.getLivingAccommodationBeds(buildingName);

        return bedCapacity * bedCapacityValue;
    }

    /**
     * Gets the number of beds in the living accommodations.
     * @return number of beds.
     */
    public int getBeds() {
        return beds;
    }

    /**
     * Gets the number of people sleeping in the beds.
     * @return number of people
     */
    public int getSleepers() {
        return sleepers;
    }

    /**
     * Adds a sleeper to a bed.
     * @throws BuildingException if beds are already in use.
     */
    public void addSleeper() {
        sleepers++;
        if (sleepers > beds) {
            sleepers = beds;
            throw new IllegalStateException("All beds are full.");
        }
    }

    /**
     * Removes a sleeper from a bed.
     * @throws BuildingException if no sleepers to remove.
     */
    public void removeSleeper() {
        sleepers--;
        if (sleepers < 0) {
            sleepers = 0;
            throw new IllegalStateException("Beds are empty.");
        }
    }


    /**
     * Retrieves an resource
     * @param name
     * @param requestedAmount
     */
    //2015-02-27 Added retrieveAnResource()
    public void retrieveAnResource(String name, double requestedAmount) {
    	try {
	    	AmountResource nameAR = AmountResource.findAmountResource(name);  	
	        double amountStored = inv.getAmountResourceStored(nameAR, false);
	    	inv.addAmountDemandTotalRequest(nameAR);  
	        if (amountStored < requestedAmount) {
	     		requestedAmount = amountStored;
	    		logger.warning("Just used up all " + name);
	        }
	    	else if (amountStored < 0.00001) {
	            Settlement settlement = getBuilding().getBuildingManager()
	                    .getSettlement();
	    		logger.warning("no more " + name + " at " + getBuilding().getNickName() + " in " + settlement.getName());	
	    	}
	    	else {
	    		inv.retrieveAmountResource(nameAR, requestedAmount);
	    		inv.addAmountDemand(nameAR, requestedAmount);
	    	}
	    }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
	    }
    }
    

    /**
     * Stores an resource
     * @param amount
     * @param name
     */
    //2015-02-27 Added storeAnResource()
    public void storeAnResource(double amount, String name) {
    	try {
            AmountResource ar = AmountResource.findAmountResource(name);      
            double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);

            if (remainingCapacity < amount) {
                // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
            	amount = remainingCapacity;
                //logger.info(" storage is full!");
            }	            
            // TODO: consider the case when it is full  	            
            inv.storeAmountResource(ar, amount, true);
            inv.addAmountSupplyAmount(ar, amount);
        }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
        }
    }


    /**
     * Time passing for the building.
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
    public void timePassing(double time) {
        generateWaste(time);
    }

    /**
     * Utilizes water for bathing, washing, etc based on population.
     * @param time amount of time passing (millisols)
     * @throws Exception if error in water usage.
     */
    //2015-12-04 Modified and renamed to generateWaste()
    public void generateWaste(double time) {

/*    	
        double waterUsageSettlement = washWaterUsagePerPersonPerTime  * time * settlement.getCurrentPopulationNum();
        double buildingProportionCap = (double) beds / (double) settlement.getPopulationCapacity();
        double waterUsageBuilding = waterUsageSettlement * buildingProportionCap;
        double waterUsed = waterUsageBuilding;        
*/        
        
    	// Note: the ratio of non-sleepers to sleepers is about 3 to 1 
    	// Thus, 2/3 of population are awake and active while 1/3 are sleeping.
    	// Assume a 5% of chance that this 2/3 population will use rest room at a given instant of time
    	// factor = 2 * .05 * # of sleepers * usage per person per sol /1000 * millisols 
    	double toiletUsagefactor = .1; 
    	
    	// computes water waste -- grey water and black water
    	double waterUsed = toiletUsagefactor * sleepers * wasteWaterUsage  * time;
        retrieveAnResource(org.mars_sim.msp.core.LifeSupportType.WATER, waterUsed);
       
        
        double greyWaterProduced = waterUsed * greyWaterFraction;
        double blackWaterProduced = waterUsed * (1 - greyWaterFraction);             
        //System.out.println("blackWaterProduced "+ blackWaterProduced);      
        storeAnResource(greyWaterProduced, "grey water");
        storeAnResource(blackWaterProduced, "black water");
    	
    	// computes toxic waste        
        double toiletTissueUsed = toiletUsagefactor * (1 - greyWaterFraction) * sleepers * TOILET_WASTE_PERSON_SOL * time;
        retrieveAnResource("toilet tissue", toiletTissueUsed);
        storeAnResource(toiletTissueUsed, "toxic waste");

    }
    
    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        return 0D;
    }

    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {
        return 0D;
    }

    @Override
    public double getMaintenanceTime() {
        return beds * 7D;
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