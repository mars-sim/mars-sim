/**
 * Mars Simulation Project
 * PreparingDessert.java
 * @version 3.07 2015-01-29
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
import org.mars_sim.msp.core.RandomUtil;
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
    public static final double PREPARE_DESSERT_WORK_REQUIRED = 2D;
    
    // 2015-01-12 Dynamically adjusted the rate of generating desserts
    //public double dessertsReplenishmentRate;
    public static double UP = 0.01;
    public static double DOWN = 0.005;
    //  SERVING_FRACTION also used in GoodsManager
    public static final int NUM_OF_DESSERT_PER_SOL = 4;
    
    public static final double DESSERT_SERVING_FRACTION = 1D / 4D;
    
    // amount of water in kg per dessert during preparation and clean-up
    public static final double WATER_USAGE_PER_DESSERT = 1.0;
    
    private List<PreparedDessert> servingsOfDessertList;
    
    private boolean makeNoMoreDessert = false;
    
	private int dessertCounterPerSol = 0;
	private int solCache = 1;
	private int numServingsCache = 0;
    private int cookCapacity;
	private double preparingWorkTime; // used in numerous places
    @SuppressWarnings("unused")
	private int NumOfServingsCache; // used in timePassing
    private double massPerServing;
    private String producerName;
    
    private Building building;
    private Settlement settlement;
    private Inventory inv ;
    // 2015-01-03 Added availableDesserts
    private String [] availableDesserts = 
    	{ 	"Soymilk",
			"Sugarcane Juice",
			"Strawberry",
			"Granola Bar",
			"Blueberry Muffin", 
			"Cranberry Juice"  };
    
    /**
     * Constructor.
     * @param building the building this function is for.
     * @throws BuildingException if error in constructing function.
     */
    public PreparingDessert(Building building) {
        // Use Function constructor.
        super(FUNCTION, building);
        this.building = building; 
        
        // 2014-12-30 Changed inv to include the whole settlement
        //inv = getBuilding().getInventory();
        inv = getBuilding().getBuildingManager().getSettlement().getInventory();
        
        settlement = getBuilding().getBuildingManager().getSettlement();
        
        //dessertsReplenishmentRate = settlement.getDessertsReplenishmentRate();
        
        PersonConfig personConfig = SimulationConfig.instance().getPersonConfiguration();
        massPerServing = personConfig.getFoodConsumptionRate() * DESSERT_SERVING_FRACTION / NUM_OF_DESSERT_PER_SOL;    
        //System.out.println("massPerServing is " +massPerServing);
        
        preparingWorkTime = 0D;
        servingsOfDessertList = new ArrayList<PreparedDessert>();

        BuildingConfig buildingConfig = SimulationConfig.instance().getBuildingConfiguration();

        this.cookCapacity = buildingConfig.getCookCapacity(building.getBuildingType());

        // Load activity spots
        loadActivitySpots(buildingConfig.getCookingActivitySpots(building.getBuildingType()));
    
    }

    // 2015-01-12 Added setChef()
    public void setChef(String name) {
    	this.producerName = name;
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
        return (getAvailableServingsDesserts() > 0);
    }

    /**
     * Gets the number of cups of fresh dessert in this facility.
     * @return number of servingsOfDessertList
     */
    public int getAvailableServingsDesserts() {
        return servingsOfDessertList.size();
    }

    /**
     * Gets the amount of dessert in the whole settlement.
     * @return dessertAvailable
     */
    // 2014-12-30 Changed name to checkAmountAV() and added a param
    public double checkAmountAV(String name) {
	    AmountResource dessertAR = AmountResource.findAmountResource(name);  
		double dessertAvailable = inv.getAmountResourceStored(dessertAR, false);
    	// 2015-01-09 Added addDemandTotalRequest()
    	// inv.addDemandTotalRequest(dessertAR);   	
    		//System.out.println("Checking " 
    		//		+ dessertAvailable + " kg " 
    		//		+ name + " at " 
    	    //    	+ getBuilding().getNickName() + " in "
    	    //    	+ settlement.getName());
		dessertAvailable = Math.round(dessertAvailable * 10000.0) / 10000.0;
		return dessertAvailable;
	}
    
    /**
     * Gets a dessert from this facility.
     * @return PreparedDessert
     */
    public PreparedDessert eatADessert() {
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
         }
        return bestDessert;
    }
    
    /**
     * Remove dessert from its AmountResource container
     * @return none
     
    // 2014-12-30 Updated removeDessertFromAmountResource() with a param
    public void removeDessertFromAmountResource(String name) {
        AmountResource dessertAR = AmountResource.findAmountResource(name);  
        double mass = getDryMass(name);
        if (inv.getAmountResourceStored(dessertAR, false) < mass )
        	System.out.println("Error retrieving " + name + " at " 
        	+ getBuilding().getNickName() + " in " + settlement.getName());       			
        
        // 2014-11-29 TODO: need to prevent IllegalStateException
  	    inv.retrieveAmountResource(dessertAR, mass);
  	    
  		// 2015-01-09 addDemandRealUsage()
  	    // inv.addDemandTotalRequest(dessertAR);
  	   	inv.addAmountDemand(dessertAR, mass);
    }
    */
    
    /**
     * Gets the quantity of one serving of dessert
     * @return quantity
     */
    //public double getMassPerServing() {
    //    return massPerServing;
    //}
    
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

 	public int getPopulation() {
        return getBuilding().getBuildingManager().getSettlement().getCurrentPopulationNum();
 	}
 	
    /**
     * Cleanup kitchen after eating.
     */
    public void cleanup() {
    	preparingWorkTime = 0D;
        makeNoMoreDessert = false;
    }
    
 	
    // 2015-01-04 Added getMakeNoMoreDessert()
 	public boolean getMakeNoMoreDessert() {
 		return makeNoMoreDessert;
 	}
 	
 	
 	// 2015-01-10 getAListOfDesserts()
 	public List<String> getAListOfDesserts() {

    	List<String> dessertList = new ArrayList<String>();
	
    	// Put together a list of available dessert 
        for(String n : availableDesserts) {
        	if (checkAmountAV(n) > getDryMass(n)) {
        		dessertList.add(n);
            	//logger.info("adding " + n + " into the dessertList at " 
                //    	+ getBuilding().getNickName() + " in " + settlement.getName());
        		// 2015-01-09 Added addDemandTotalRequest()
        	}
        }
		return dessertList;
 	    
 	}
 
	// 2015-01-10 getADessert()
 	public String getADessert(List<String> dessertList) {
    	String selectedDessert = "None";
    	
    	if ( dessertList.size() > 0 ) {
	
			int upperbound = dessertList.size();
	    	int lowerbound = 1;
	    	
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
    	}
		return selectedDessert;
 	}
    
    /**
     * Adds work to this facility. 
     * The amount of work is dependent upon the person's skill.
     * @param workTime work time (millisols)
      */
    public void addWork(double workTime) {
    	preparingWorkTime += workTime; 
        //logger.info("addWork() : preparingWorkTime is " + preparingWorkTime );
        //logger.info("addWork() : workTime is " + workTime);
    	
    	//if (!makeNoMoreDessert && 
    	if (preparingWorkTime >= PREPARE_DESSERT_WORK_REQUIRED) {
	    	// TODO: check if this is proportional to the population
	        double population = building.getBuildingManager().getSettlement().getCurrentPopulationNum();
	        // max allowable # of dessert servings per sol
	        double maxServings = population * settlement.getDessertsReplenishmentRate() / 10.0;
	        
	        int numServings = getAvailableServingsDesserts();
        	//System.out.println("addWork() maxServings : " + maxServings);
        	//System.out.println("addWork() numServings: " + numServings);
	        //if ( numServingsCache != numServings )numServingsCache = numServings;

	        if (maxServings < numServings )
	        	makeNoMoreDessert = true;
        
	        else  {	  // if (numServings >= maxServings ) 	
	        	List<String> dessertList = getAListOfDesserts();
	        	String selectedDessert = getADessert(dessertList);
	        	makeADessert(selectedDessert);
			    makeNoMoreDessert = true; // This cause less dessert made
	        }
    	} // end of if (preparingWorkTime >= PREPARE_DESSERT_WORK_REQUIRED)
    } // end of void addWork()
   
    
    /**
     * Gets the dry mass of a dessert 
     */
    // 2015-01-12 Added getDryMass()
    public double getDryMass(String selectedDessert) {
    	double result = 0;
    	/* availableDesserts = 
        	{ 	"Soymilk",
    			"Sugarcane Juice",
    			"Strawberry",
    			"Granola Bar",
    			"Blueberry Muffin", 
    			"Cranberry Juice"  };
    	*/
    	
    	// TODO: get the real world figure on each serving
    	double [] dryMass = 
        	{ 	0.05,
    			0.01,
    			0.03,
    			0.08,
    			0.07, 
    			0.01 };
    	
    	for(int i = 0; i <availableDesserts.length; i++) {
        	if ( availableDesserts[i].equals(selectedDessert) ) {
        		result = dryMass[i];
        	}
        }
    	return result;
    }
    
    public void makeADessert(String selectedDessert) {
    	// Take out one serving of the selected dessert from the fridge 
		//removeDessertFromAmountResource(selectedDessert);
        double dryMass = getDryMass(selectedDessert);
		retrieveAnResource(dryMass, selectedDessert);
		
        MarsClock time = (MarsClock) Simulation.instance().getMasterClock().getMarsClock().clone();
        // TODO: quality also dependent upon the hygiene of a person
        int dessertQuality = getBestDessertSkill();

        // Create a serving of dessert and add it into the list
	    servingsOfDessertList.add(new PreparedDessert(selectedDessert, dessertQuality, dryMass, time, producerName, this));
	    
	    useWater();
	    
	    dessertCounterPerSol++;
	    //logger.info("addWork() : new dessert just added : " + selectedDessert);
	    //System.out.println("# of available desserts : " + getServingsDesserts());    
    	//System.out.println("desserts made today : " + dessertCounterPerSol);    
	    
        // Arbitrarily choice to reset workTime to zero for making the next serving      	
	    // preparingWorkTime = 0; 
	    preparingWorkTime -= PREPARE_DESSERT_WORK_REQUIRED;	
    }
    
    // 2015-01-28 Added useWater()
    public void useWater() {
    	//TODO: need to move the hardcoded amount to a xml file	    
	    retrieveAnResource(WATER_USAGE_PER_DESSERT, org.mars_sim.msp.core.LifeSupport.WATER);
		double wasteWaterAmount = WATER_USAGE_PER_DESSERT * .95;		
		storeAnResource(wasteWaterAmount, "grey water");  
    }
    
    
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
    	  int newNumOfServings = getAvailableServingsDesserts();
    	  double rate = settlement.getDessertsReplenishmentRate();
          //if ( NumOfServingsCache != newNumOfServings) logger.info("Has " + newNumOfServings +  " Fresh Dessert" );
    		
          // Toss away expired servingsOfDessertList
          Iterator<PreparedDessert> i = servingsOfDessertList.iterator();
          	while (i.hasNext()) {
	  
	            PreparedDessert dessert = i.next();
	            MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();
	            
	            if (MarsClock.getTimeDiff(dessert.getExpirationTime(), currentTime) < 0D) {	            	   	      
	            	try {
		            	i.remove();
		            	// 2015-02-27 The probability that the expired dessert is of no good and must be discarded is also dependent upon the food quality
		            	double quality = dessert.getQuality()/2D + 1D;
	 	      			double num = RandomUtil.getRandomDouble(8*quality);
	 	      			if (num < 1) {
	 	      				storeAnResource(getDryMass(dessert.getName()), "Food Waste");
	 	      				logger.info(getDryMass(dessert.getName()) + " kg " 
			                		+ dessert.getName()
			                		+ " expired, turned bad and discarded at " + getBuilding().getNickName() 
			                		+ " in " + settlement.getName()
			                		);
	 	      			}
	 	      			else  {
			            	refrigerateFood(dessert);	 	      			
			            	logger.info("Dessert Expired. Refrigerate " 
			            			+ getDryMass(dessert.getName()) + " kg " 
			                		+ dessert.getName()
			                		+  " at " + getBuilding().getNickName() 
			                		+ " in " + settlement.getName()
			                		);
			            	if(logger.isLoggable(Level.FINEST)) {
			                     logger.finest("The dessert has lost its freshness at " + 
			                     getBuilding().getBuildingManager().getSettlement().getName());
			                }
	 	      			}
		 	    		// 2015-01-12 For each dessert that needs to be refrigerated
		  	    		// Adjust the rate to go down 
			  	    	if (rate > 0 ) 
			  	    		rate -= DOWN; 
			          	settlement.setDessertsReplenishmentRate(rate);  
	            	} catch (Exception e) {}
	            } //end of if (MarsClock.getTimeDiff(
	        } // end of  while (i.hasNext())
	      //NumOfServingsCache = newNumOfServings;
    	} // end of if ( hasAServing )
 
    	checkEndOfDay();
    }

	  
	// 2015-02-06 Added storeAnResource()
	public void storeAnResource(double amount, String name) {
	
		try {
			AmountResource ar = AmountResource.findAmountResource(name);      
			double remainingCapacity = inv.getAmountResourceRemainingCapacity(ar, false, false);
			
			if (remainingCapacity < amount) {
			    // if the remaining capacity is smaller than the harvested amount, set remaining capacity to full
				amount = remainingCapacity;
			    //logger.info("addHarvest() : storage is full!");
			}
			inv.storeAmountResource(ar, amount, true);
			inv.addAmountSupplyAmount(ar, amount);
			
		} catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
		}
	}
	
	 
    /**
     * Retrieves the resource
     * @param name
     * @parama requestedAmount
     */
    //2015-02-07 Added retrieveAnResource()
    public void retrieveAnResource(double requestedAmount, String name) {
    	try {
	    	AmountResource nameAR = AmountResource.findAmountResource(name);  	
	        double remainingCapacity = inv.getAmountResourceStored(nameAR, false);
	    	inv.addAmountDemandTotalRequest(nameAR);  
	        if (remainingCapacity < requestedAmount) {
	     		requestedAmount = remainingCapacity;
	    		logger.warning("Just used up all " + name);
	        }
	    	else if (remainingCapacity == 0) {
	    		logger.warning("no more " + name + " in " + settlement.getName());
	    	}
	    	else {
	    		inv.retrieveAmountResource(nameAR, requestedAmount);
	    		inv.addAmountDemand(nameAR, requestedAmount);
	    	}
	    }  catch (Exception e) {
    		logger.log(Level.SEVERE,e.getMessage());
	    }
    }
    
    // 2015-01-12 Added checkEndOfDay()
  	public synchronized void checkEndOfDay() {
  			
		MarsClock currentTime = Simulation.instance().getMasterClock().getMarsClock();      
		// Added 2015-01-04 : Sanity check for the passing of each day
		int newSol = currentTime.getSolOfMonth();
	    int newMonth = currentTime.getMonth();
	    double rate = settlement.getDessertsReplenishmentRate();
		if ( newSol != solCache) {	
			solCache = newSol;
	        // reset back to zero at the beginning of a new day.
	    	dessertCounterPerSol = 0;
			// 2015-01-12 Adjust this rate to go up automatically by default
	    	rate += UP;
	      	settlement.setDessertsReplenishmentRate(rate);
	    	/* 
	    	// TODO: do we need to clear all desserts by the end of the day?
	    	// It is causing the system to hang
			// logger.info("Month " + newMonth + " Sol " + newSol + " : " 
			//		+ dessertCounterPerSol + " desserts made yesterday in " 
		    //    	+ building.getNickName() + " at " + settlement.getName()); 
	
			Iterator<PreparedDessert> i = servingsOfDessertList.iterator();           
		      	while (i.hasNext()) {
		      		try {
			            PreparedDessert dessert = i.next();
			            i.remove();
			            refrigerateFood(dessert);
			            logger.info("End of Day. Refrigerate " + getDryMass(dessert.getName()) 
			            		+ " kg " + dessert.getName() 
			            		+ " in " + building.getNickName() 
			            		+ " at " + settlement.getName()); 
			    		// 2015-01-12 For each dessert that needs to be refrigerated
			    		// Adjust the rate to go down 
			  	    	if (rate > 0 ) 
			  	    		rate -= DOWN; 
		      		} catch (Exception e) {}
		      	}
		      	*/
		}
  	}
    
	// 2015-01-12  Added refrigerateFood()
	public void refrigerateFood(PreparedDessert dessert) {	
		try {
			String dessertName = dessert.getName();
			AmountResource dessertAR = AmountResource.findAmountResource(dessertName);            	
			double capacity = inv.getAmountResourceRemainingCapacity(dessertAR, true, false);            	        	
		    double massPerServing = getDryMass(dessertName)  ;
			
			if (massPerServing > capacity) 
				massPerServing = capacity;
			
			massPerServing = Math.round( massPerServing * 100000.0) / 100000.0;
			// 2015-01-03 Put back to storage or freezer if not eaten
		    inv.storeAmountResource(dessertAR, massPerServing , false);
			// 2015-01-15 Add addSupplyAmount()
            inv.addAmountSupplyAmount(dessertAR, massPerServing);
		} catch (Exception e) {}
	}
    
    
    public int getTotalServingsOfDessertsToday() {
        return dessertCounterPerSol;
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
    	// 2015-01-09 Added addDemandTotalRequest()
    	//inv.addDemandTotalRequest(ar);
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

        building = null;
        inv = null;
        settlement = null;
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