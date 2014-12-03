/**
 * Mars Simulation Project
 * PreparingDessert.java
 * @version 3.07 2014-11-28
 * @author Manny Kung				
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.CookMeal;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingConfig;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Function;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * The PreparingDessert class is a building function for making dessert.
 */
//2014-11-28 Changed Class name from MakingSoy to PreparingDessert
public class PreparingDessert
extends Function
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    /** default logger. */
    private static Logger logger = Logger.getLogger(PreparingDessert.class.getName());

    private static final BuildingFunction FUNCTION = BuildingFunction.PREPARING_DESSERT;

    /** The base amount of work time in milliSols (for cooking skill 0) 
     * to prepare fresh dessert . */
    public static final double WORK_REQUIRED = 5D;
   
    // The number of sols the dessert can be preserved
    //public static final double SHELF_LIFE = .4D;
       
    // the chef will make up to # of serving of soymilk per person in a settlement
    // It's an arbitrary (decimal) number, preventing the chef from making too many servings of soymilk
    public static final double MAX_NUM_SERVING_PER_PERSON = 1.5;

    // Data members
    private int cookCapacity;
    private List<PreparedDessert> servingsOfDessertList;
    private double workTime;

    private int NumOfServingsCache;
    private Building building;
    
    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public PreparingDessert(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);
        this.building = building; 
        
        workTime = 0D;
        servingsOfDessertList = new ArrayList<PreparedDessert>();

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();

        this.cookCapacity = config.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(config.getCookingActivitySpots(building.getBuildingType()));
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    //TODO: make the demand for dessert user-selectable
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // TODO: calibrate this demand
    	// Demand is 1 for every 5 inhabitants.
        double demand = settlement.getAllAssociatedPeople().size() / 5D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getName().equalsIgnoreCase(buildingName) && !removedBuilding) {
                removedBuilding = true;
            }
            else {
                PreparingDessert preparingDessertFunction = (PreparingDessert) building.getFunction(FUNCTION);
                double wearModifier = (building.getMalfunctionManager().getWearCondition() / 100D) * .25D + .25D;
                supply += preparingDessertFunction.cookCapacity * wearModifier;
            }
        }

        double preparingDessertCapacityValue = demand / (supply + 1D);

        BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
        double preparingDessertCapacity = config.getCookCapacity(buildingName);

        return preparingDessertCapacity * preparingDessertCapacityValue;
    }

    /**
     * Get the maximum number of cooks supported by this facility.
     * @return max number of cooks
     */
    public int getCookCapacity() {
        return cookCapacity;
    }

    /**
     * Get the current number of cooks using this facility.
     * @return number of cooks
     */
    public int getNumCooks() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Task task = i.next().getMind().getTaskManager().getTask();
                    if (task instanceof CookMeal) result++;
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Gets the skill level of the best cook using this facility.
     * @return skill level.
     */
    public int getBestDessertSkill() {
        int result = 0;

        if (getBuilding().hasFunction(BuildingFunction.LIFE_SUPPORT)) {
            try {
                LifeSupport lifeSupport = (LifeSupport) getBuilding().getFunction(BuildingFunction.LIFE_SUPPORT);
                Iterator<Person> i = lifeSupport.getOccupants().iterator();
                while (i.hasNext()) {
                    Person person = i.next();
                    Task task = person.getMind().getTaskManager().getTask();
                    if (task instanceof CookMeal) {
                        int preparingDessertSkill = person.getMind().getSkillManager().getEffectiveSkillLevel(SkillType.COOKING);
                        if (preparingDessertSkill > result) result = preparingDessertSkill;
                    }
                }
            }
            catch (Exception e) {}
        }

        return result;
    }

    /**
     * Checks if there are any FreshDessertList in this facility.
     * @return true if yes
     */
    public boolean hasFreshDessert() {
        return (servingsOfDessertList.size() > 0);
    }

    /**
     * Gets the number of cups of fresh dessert in this facility.
     * @return number of servingsOfDessertList
     */
    public int getNumServingsFreshDessert() {
        return servingsOfDessertList.size();
    }

    // 2014-11-28 this method checkAmountOfDessert() was called by PrepareDessert.java
	public double checkAmountOfDessert() {
	    AmountResource soymilkAR = AmountResource.findAmountResource("soymilk");  
	    //double soymilkAvailable = getBuilding().getInventory().getAmountResourceStored(soymilkAR, false);
	    
	    double soymilkAvailable = getBuilding().getBuildingManager().getSettlement().getInventory().getAmountResourceStored(soymilkAR, false);
	    soymilkAvailable = Math.round(soymilkAvailable * 1000.0) / 1000.0;
		return soymilkAvailable;
	}
    
    /**
     * Gets freshDessert from this facility.
     * @return freshDessert
     */
    public PreparedDessert getFreshDessert() {
        PreparedDessert bestDessert = null;
        int bestQuality = -1;
        Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
        while (i.hasNext()) {
            PreparedDessert freshDessert = i.next();
            if (freshDessert.getQuality() > bestQuality) {
                bestQuality = freshDessert.getQuality();
                bestDessert = freshDessert;
            }
        }

        if (bestDessert != null) {
        	servingsOfDessertList.remove(bestDessert);
        	// remove dessert from amount resource 
        	// TODO: why calling this method ? plus it's causing IllegalStateException 
        	//removeDessertFromAmountResource();
         }
        return bestDessert;
    }
    
    /**
     * Remove dessert from its AmountResource container
     * @return none
     */
    // 2014-11-06 Added removeDessertFromAmountResource()
    public void removeDessertFromAmountResource() {
        AmountResource soymilkAR = AmountResource.findAmountResource("soymilk");  
        double soymilkPerServing = getMassPerServing(); 
        // 2014-11-29 TODO: need to prevent IllegalStateException
	    getBuilding().getInventory().retrieveAmountResource(soymilkAR, soymilkPerServing);
    }
    
    public double getMassPerServing() {
        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        return config.getFoodConsumptionRate() * (1D / 3D);    
    }
    /**
     * Gets the quality of the best quality fresh Dessert at the facility.
     * @return quality
     */
    public int getBestDessertQuality() {
        int bestQuality = 0;
        Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
        while (i.hasNext()) {
            PreparedDessert freshDessert = i.next();
            if (freshDessert.getQuality() > bestQuality) bestQuality = freshDessert.getQuality();
        }

        return bestQuality;
    }

    /**
     * Cleanup kitchen after eating.
     */
    public void cleanup() {
        workTime = 0D;
    }

    /**
     * Adds work to this facility. 
     * The amount of work is dependent upon the person's skill.
     * @param workTime work time (millisols)
      */
    public void addWork(double workTime) {
    	workTime += workTime; 
        //logger.info("addWork() : workTime is " + workTime);
        //logger.info("addWork() : workTime is " + workTime);
    	boolean enoughTime = false;
    	if (workTime >= WORK_REQUIRED) {
	    	enoughTime = true;
	    	// TODO: check if this is proportional to the population
	        int size = building.getBuildingManager().getSettlement().getAllAssociatedPeople().size();
	        double maxServings = size * MAX_NUM_SERVING_PER_PERSON;
	
	 	    double soymilkAvailable = checkAmountOfDessert();
	    	// Existing # of servings of dessert already available
	    	double numServings = servingsOfDessertList.size();
	    		//logger.info("addWork() : " + numServings + " servings of fresh dessert available");    	
	    		//logger.info("addWork() : " + servingsOfDessertList.size() + " reshDessertList.size()");    	

	     	if ( enoughTime &&
	     	   ( soymilkAvailable > getMassPerServing() + 0.1) &&
	     	   ( numServings < maxServings + 0.1) ) {
	          
	     		// take out one serving of soymilk from the fridge 
	     		removeDessertFromAmountResource();
	     		
	            int dessertQuality = getBestDessertSkill();
	            // TODO: how to use time ?
	            MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
	  	        // Create a serving of dessert and add it into the list
		        servingsOfDessertList.add(new PreparedDessert("Soymilk", dessertQuality, time));
		        //logger.info(" 1 serving of fresh dessert have just been prepared");
	     		
		        if (logger.isLoggable(Level.FINEST)) {
		        	logger.finest(getBuilding().getBuildingManager().getSettlement().getName() + 
		        			" has prepared " + servingsOfDessertList.size() + " "
		        					+ "servings of tasty dessert (quality is " + dessertQuality + ")");
	            workTime = 0; // Reset workTime to zero for making the next serving
		        
		        }	     	
	     	} // end of if (enoughTime && (dessertAvailable> 0.5)) 
	     	else 
	     		enoughTime = false;    	
    		//logger.info("end of addWork(). soy product not done yet. not enough workTime : " + workTime);
    	} // end of if (workTime >= WORK_REQUIRED) 
       	
	     	//logger.info("addWork() : workTime is " + workTime);
	     	
     } // end of void addWork()
   
 
    /**
     * Time passing for the building.
     * 
     * @param time amount of time passing (in millisols)
     * @throws BuildingException if error occurs.
     */
  	// 2014-11-06 dessert cannot be preserved after a certain number of sols  
    public void timePassing(double time) {
      boolean hasAServing = hasFreshDessert(); 
         //logger.info("");
      if ( hasAServing ) {
           int newNumOfServings = servingsOfDessertList.size();
           //if ( NumOfServingsCache != newNumOfServings)
           //	logger.info("Has " + newNumOfServings +  " Fresh Dessert" );
        // Toss away expired servingsOfDessertList
        Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
        while (i.hasNext()) {
            PreparedDessert aServingOfDessert = i.next();
            //logger.info("Desert : " + aServingOfDessert.getName());
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            if (MarsClock.getTimeDiff(aServingOfDessert.getExpirationTime(), currentTime) < 0D) {
            	
            	i.remove();
               	
                if(logger.isLoggable(Level.FINEST)) {
                     logger.finest("The dessert has lost its freshness at " + 
                     getBuilding().getBuildingManager().getSettlement().getName());

                }         
            }
        }
        NumOfServingsCache = newNumOfServings;
      }
    }

    /**
     * Gets the amount of power required when function is at full power.
     * @return power (kW)
     */
    public double getFullPowerRequired() {
        return getNumCooks() * 10D;
    }

    /**
     * Gets the amount of power required when function is at power down level.
     * @return power (kW)
     */
    public double getPoweredDownPowerRequired() {
        return 0;
    }

    @Override
    public double getMaintenanceTime() {
        return cookCapacity * 10D;
    }

    @Override
    public void destroy() {
        super.destroy();

        servingsOfDessertList.clear();
        servingsOfDessertList = null;
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