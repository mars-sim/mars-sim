/**
 * Mars Simulation Project
 * PreparingDessert.java
 * @version 3.07 2014-12-30
 * @author Manny Kung				
 */
package org.mars_sim.msp.core.structure.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
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
    private double workTime; // used in numerous places

    private int NumOfServingsCache; // used in timePassing
    private Building building;
    
    private Inventory inv ;
    
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
    
        // 2014-12-30 Changed inv to include the whole settlement
        //inv = getBuilding().getInventory();
        inv = getBuilding().getBuildingManager().getSettlement().getInventory();
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingType the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    //TODO: make the demand for dessert user-selectable
    public static double getFunctionValue(String buildingType, boolean newBuilding,
            Settlement settlement) {

        // TODO: calibrate this demand
    	// Demand is 1 for every 5 inhabitants.
        double demand = settlement.getAllAssociatedPeople().size() / 5D;

        double supply = 0D;
        boolean removedBuilding = false;
        Iterator<Building> i = settlement.getBuildingManager().getBuildings(FUNCTION).iterator();
        while (i.hasNext()) {
            Building building = i.next();
            if (!newBuilding && building.getBuildingType().equalsIgnoreCase(buildingType) && !removedBuilding) {
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
        double preparingDessertCapacity = config.getCookCapacity(buildingType);

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

    /**
     * Gets the amount of dessert in the whole settlement.
     * @return dessertAvailable
     */
    // 2014-11-28 this method checkAmountOfDessert() was called by PrepareDessert.java
    // 2014-12-30 Changed name to checkAmountAV() and added a param
    public double checkAmountAV(String name) {
	    AmountResource dessertAR = AmountResource.findAmountResource(name);  
		double dessertAvailable = inv.getAmountResourceStored(dessertAR, false);
	    dessertAvailable = Math.round(dessertAvailable * 1000.0) / 1000.0;
		return dessertAvailable;
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
    // 2014-12-30 Updated removeDessertFromAmountResource() with a param
    public void removeDessertFromAmountResource(String name) {
        AmountResource dessertAR = AmountResource.findAmountResource(name);  
        double dessertPerServing = getMassPerServing();
        // 2014-11-29 TODO: need to prevent IllegalStateException
  	    inv.retrieveAmountResource(dessertAR, dessertPerServing);
  	    
  	    if (name.equals("Soymilk")) {
	  	    // 2014-12-29 Added sugar dependency
	        String sugar = "Sugar";
	        double sugarAmount = 0.01;
	        AmountResource sugarAR = getFreshFoodAR(sugar);
	        double sugarAvailable = getFreshFood(sugarAR);
	        if (sugarAvailable > 0.01) 
	        	inv.retrieveAmountResource(sugarAR, sugarAmount);
  	    }
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

    	if (workTime >= WORK_REQUIRED) {
	    	// TODO: check if this is proportional to the population
	        int size = building.getBuildingManager().getSettlement().getAllAssociatedPeople().size();
	        double maxServings = size * MAX_NUM_SERVING_PER_PERSON;
	
	        // 2015-01-02 Added random selection of a dessert item 
	        // 2015-01-02 Added Strawberry, Granola Bar
	 	    double soymilkAvailable = checkAmountAV("Soymilk");
	 	    double sugarcaneJuiceAvailable = checkAmountAV("Sugarcane Juice");
	 	    double strawberryAvailable = checkAmountAV("Strawberry");
	 	    double granolaBarAvailable = checkAmountAV("Granola Bar");
	 	   
	 	    boolean hasSoymilk = false;
	 	    boolean hasSugarcaneJuice = false;
	 	    boolean hasStrawberry = false;
	 	    boolean hasGranolaBar = false;
	 	    
	    	// Existing # of servings of dessert already been made
	    	double numServings = servingsOfDessertList.size();	
	    	List<String> dessertList = new ArrayList<String>();
	    	
	    	if (soymilkAvailable > getMassPerServing()) {
	    		hasSoymilk = true;
	    		dessertList.add("Soymilk");
	    	}
	    	if (sugarcaneJuiceAvailable > getMassPerServing()) {
	    		hasSugarcaneJuice = true;
	    		dessertList.add("Sugarcane Juice");
	    	}
	    	if (strawberryAvailable > getMassPerServing()) {
	    		hasStrawberry = true;
	    		dessertList.add("Strawberry");
	    	}
	    	if (granolaBarAvailable > getMassPerServing()) {
	    		hasGranolaBar = true;
	    		dessertList.add("Granola Bar");
	    	}
	    	
	    	if (  dessertList.size() > 0 
	    			&& numServings < maxServings ) {
    	
				int upperbound = dessertList.size();
		    	int lowerbound = 1;
		    	String selectedDessert = "None";
		    	
		    	if (upperbound > 1) {
		    		int index = ThreadLocalRandom.current().nextInt(lowerbound, upperbound);
		    		//int number = (int)(Math.random() * ((upperbound - lowerbound) + 1) + lowerbound);
		    		selectedDessert = dessertList.get(index);
		    	}
		    	else if (upperbound == 1) {
		    		selectedDessert = dessertList.get(0);
		    	}
		    	else if (upperbound == 0)
		    		selectedDessert = "None";
		    	
				//System.out.println("upperbound is "+ upperbound);
		    	//System.out.println("index is "+ index);
		    	//System.out.println("selectedDessert is "+selectedDessert);	
		    	
	     		// Take out one serving of the selected dessert from the fridge 
	    		removeDessertFromAmountResource(selectedDessert);
		        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
		        int dessertQuality = getBestDessertSkill();
		        
		        // Create a serving of dessert and add it into the list
			    servingsOfDessertList.add(new PreparedDessert(selectedDessert, dessertQuality, time));

		        // Reset workTime to zero for making the next serving      	
	            workTime = 0; 
	            
	     	} // end of if ( soymilkAvailable > getMassPerServing() 
	    } // end of if (workTime >= WORK_REQUIRED) 
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
      if ( hasAServing ) {
           //int newNumOfServings = servingsOfDessertList.size();
           //if ( NumOfServingsCache != newNumOfServings)
           	//logger.info("Has " + newNumOfServings +  " Fresh Dessert" );
        // Toss away expired servingsOfDessertList
        Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
        while (i.hasNext()) {
            PreparedDessert aServingOfDessert = i.next();
            //logger.info("Dessert : " + aServingOfDessert.getName());
            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
            if (MarsClock.getTimeDiff(aServingOfDessert.getExpirationTime(), currentTime) < 0D) {
            	
            	i.remove();
               	
                if(logger.isLoggable(Level.FINEST)) {
                     logger.finest("The dessert has lost its freshness at " + 
                     getBuilding().getBuildingManager().getSettlement().getName());

                }         
            }
        }
        //NumOfServingsCache = newNumOfServings;
      }
    }

    

    /**
     * Gets the amount resource of the fresh food from a specified food group. 
     * 
     * @param String food group
     * @return AmountResource of the specified fresh food 
     */
     //2014-12-29 Added getFreshFoodAR() 
    public AmountResource getFreshFoodAR(String foodGroup) {
        AmountResource freshFoodAR = AmountResource.findAmountResource(foodGroup);
        return freshFoodAR;
    }
    
    /**
     * Computes amount of fresh food from a particular fresh food amount resource. 
     * 
     * @param AmountResource of a particular fresh food
     * @return Amount of a particular fresh food in kg, rounded to the 4th decimal places
     */
     //2014-12-29 Added getFreshFood() 
    public double getFreshFood(AmountResource ar) {
        double freshFoodAvailable = inv.getAmountResourceStored(ar, false);
        return freshFoodAvailable;
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