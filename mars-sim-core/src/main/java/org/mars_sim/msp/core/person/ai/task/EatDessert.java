/**
 * Mars Simulation Project
 * EatDessert.java
 * @version 3.07 2015-01-10
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PhysicalCondition;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingException;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparedDessert;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * The EatDessert class is a task for eating dessert.
 * The duration of the task is 40 millisols.
 * Note: Eating dessert reduces hunger to 0 and reduce stress.
 */
public class EatDessert 
extends Task 
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/** default logger. */
	private static Logger logger = Logger.getLogger(EatDessert.class.getName());

    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.eatDessert"); //$NON-NLS-1$

    /** Task phases. */
    private static final TaskPhase EATING = new TaskPhase(Msg.getString(
            "Task.phase.eatingDessert")); //$NON-NLS-1$
    
    // Static members
    /** The stress modified per millisol. */
    private static final double STRESS_MODIFIER = -.2D;

    // Data members
    private PreparedDessert dessert;

    // 2014-11-28 Added HUNGER_REDUCTION_PERCENT
    private static final double HUNGER_REDUCTION_PERCENT = 40D;
    
    //  SERVING_FRACTION was used in PreparingDessert.java
	private static final double SERVING_FRACTION = 1D / 6D;
    // see PrepareDessert.java for the number of dessert served per sol
	private static final double NUM_OF_DESSERT_PER_SOL = 4D;
	
//	private static final double AVE_WATER_CONTENT_FRACTION = .2;
	
    private String dessertLocation ;
    private PreparingDessert kitchen;
  
    /** 
     * Constructs a EatMeal object, hence a constructor.
     * @param person the person to perform the task
     */
    public EatDessert(Person person) {
        super(NAME, person, false, false, STRESS_MODIFIER, true, 10D + 
                RandomUtil.getRandomDouble(30D));
        //logger.info("just called EatDessert's constructor");

        boolean walkSite = false;

        LocationSituation location = person.getLocationSituation();
        if (location == LocationSituation.IN_SETTLEMENT) {
        	
        	// 2015-02-17 Called getDiningBuilding()
        	Building diningBuilding = person.getDiningBuilding();
                         
        	if (diningBuilding == null)
        		// If person is in a settlement, try to find a dining area.    
        		diningBuilding = getAvailableDiningBuilding(person);
                    
            else {                 	
                // Walk to dining building.
                walkToActivitySpotInBuilding(diningBuilding, true);
                walkSite = true;
            }
        	
            kitchen = person.getKitchenWithDessert();
            
            if (kitchen == null) {
            	kitchen = getKitchenWithDessert(person);
            }
            
           	else {  // If a fresh dessert in a local kitchen available
    			dessertLocation = kitchen.getBuilding().getNickName();
           		// grab this fresh dessert and tag it for this person
             	dessert = kitchen.eatADessert();
               	if (dessert != null) {
               		//2015-01-12 Added setConsumerName()
               		dessert.setConsumerName(person.getName());
               	}
            }
            
            walkSite = true;
            
        }
        else if (location == LocationSituation.OUTSIDE) {
            endTask();
        }

        if (!walkSite) {
            if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
                // If person is in rover, walk to passenger activity spot.
                if (person.getVehicle() instanceof Rover) {
                    walkToPassengerActivitySpotInRover((Rover) person.getVehicle(), true);
                }
            }
            else {
                // Walk to random location.
                walkToRandomLocation(true);
             	//System.out.println(person.getName() + " is not in a vehicle and is walking to another location in " + person.getContainerUnit());
        		//System.out.println("EatDessert constructor : other circumstances calling walkToRandomLocation()");
            }
        }

        // Initialize task phase.
        addPhase(EATING);
        setPhase(EATING);
    }

    @Override
    protected BuildingFunction getRelatedBuildingFunction() {
        return BuildingFunction.DINING;
    }

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     */
    protected double performMappedPhase(double time) {
        if (getPhase() == null) {
            throw new IllegalArgumentException("The task phase is null");
        }
        else if (EATING.equals(getPhase())) {
            return eatingPhase(time);
        }
        else {
            return time;
        }
    }

    /**
     * Performs the eating phase of the task.
     * @param time the amount of time (millisol) to perform the eating phase.
     * @return the amount of time (millisol) left after performing the eating phase.
     */
	// 2015-01-05 Reworked if-then-else clauses
    private double eatingPhase(double time) {
     	 
    	//String namePerson = person.getName();
    	//System.out.println(namePerson + " is entering the eatingPhase() in EatDessert.java");	
        PhysicalCondition condition = person.getPhysicalCondition();

        // If a person has a serving of dessert, stress is reduced.
        if (dessert != null) {
            double stress = condition.getStress();
            condition.setStress(stress - (STRESS_MODIFIER * (dessert.getQuality() + 1D)));
            
            double fatigue = condition.getFatigue();
            if (fatigue > 100)
            	condition.setFatigue(fatigue - 100);
        }

        if (getDuration() <= (getTimeCompleted() + time)) {
            PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
            try {
      	
            	if (dessert != null) {
            		setDescription(Msg.getString("Task.description.eatDessert.made")); //$NON-NLS-1$
            		Inventory inv = kitchen.getBuilding().getInventory();
                   	//String nameDessert = dessert.getName();
            		//logger.info( namePerson + " has just eaten " + nameDessert + " in " + dessertLocation );
                   	Storage.retrieveAnResource(.0025D, "napkin", inv, true);
                	Storage.storeAnResource(.0025D,"solid waste", inv);
            	}
            	else {
            		// if a person does not get a hold of a serving of dessert in a settlement
            		if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
            			//person.consumeDessert(config.getDessertConsumptionRate() / NUM_OF_DESSERT_PER_SOL , (dessert == null));
                      	setDescription(Msg.getString("Task.description.eatDessert.vehicle")); //$NON-NLS-1$
                        
	                    eatDessert();
	           			//System.out.println( namePerson + " has just eaten a dessert in " + person.getContainerUnit()); //or person.getVehicle().getName()
            		}
            		else 
            		{
            			//System.out.println(namePerson + " not in a vehicle. can't obtain food from container, end the task.");
                        endTask();
            		}
            	}
            	
            	// 2014-11-28 Computed new hunger level
                double hunger = condition.getHunger();
                // 2015-02-01 Added energy level 
                double energy = condition.getEnergy();
                if (hunger < 900 || energy > 1000) { 
                	hunger = hunger * (1 - HUNGER_REDUCTION_PERCENT/100);
                	//energy = energy * (1 + HUNGER_REDUCTION_PERCENT/100);
                }
                else if (hunger > 900 || energy < 1000) {
                	hunger = 900;
                	//energy = 1000;
                }
                //System.out.println("EatDessert : dessert.getDryMass() "+ Math.round(dessert.getDryMass()*10.0)/10.0);
                condition.setHunger(hunger);
                condition.addEnergy(dessert.getDryMass());
                
            }
            catch (Exception e) {
                // If person can't obtain dessert from container, end the task.
                endTask();
            }
        }

        return 0D; 
        
    }
    
    /**
     * Eats a dessert of choice or random selection.
     * @throws Exception if problems finding preserved food to eat.
     */
    // 2015-03-10 Added eatDessert()
    private void eatDessert() throws Exception {
    	
        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        double amount = config.getDessertConsumptionRate() / NUM_OF_DESSERT_PER_SOL;
        Unit containerUnit = person.getContainerUnit();
        
        if (containerUnit != null) {
        	
            Inventory inv = containerUnit.getInventory();
            boolean exit = false;
            String dessertName = null;
            
            while (!exit) {
	        	List<String> dessertList = new ArrayList<String>();
	      	  	// Put together a list of available dessert 
	    		String [] availableDesserts = PreparingDessert.getArrayOfDesserts();
	            for(String n : availableDesserts) {   	
	             	boolean isAvailable = Storage.retrieveAnResource(amount, n, inv, false);
	            	if (isAvailable) dessertList.add(n);   	  	        	
	            }
	            // Pick one of the desserts
	            dessertName = PreparingDessert.getADessert(dessertList);	
	            
			 	// 10% probability that the preserved food is of no good and must be discarded from container unit.
	      		int num = RandomUtil.getRandomInt(9);
	      		if (num == 0) {
	      			//System.out.println("EatMeal. preserved food is bad ");
	      			Storage.retrieveAnResource(amount, dessertName, inv, true);
	      			Storage.storeAnResource(amount, "food waste", inv);
	      			exit = false;
	      		}
	      		else
	      			// exit the while loop
	      			exit = true;
      		
            }
      		
        	//if (person.getLocationSituation() != LocationSituation.IN_VEHICLE) {
        		Storage.retrieveAnResource(.0025D, "napkin", inv, true);
      			Storage.retrieveAnResource(amount, dessertName, inv, true);
        		Storage.storeAnResource(.0025D,"solid waste", inv);
        	//}
        }
        else {
            throw new Exception(person + " does not have a container unit to get preserved food from.");
        }
        
    }

    
    /**
     * Adds experience to the person's skills used in this task.
     * @param time the amount of time (ms) the person performed this task.
     */
    protected void addExperience(double time) {
        // This task adds no experience.
    }

    /**
     * Gets an available dining building that the person can use.
     * Returns null if no dining building is currently available.
     *
     * @param person the person
     * @return available dining building
     * @throws BuildingException if error finding dining building.
     */
    //TODO: For now, get a dessert from refrigerator only from dining building  
    public static Building getAvailableDiningBuilding(Person person) {

        Building result = null;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
            BuildingManager manager = settlement.getBuildingManager();
            List<Building> diningBuildings = manager.getBuildings(BuildingFunction.DINING);
            diningBuildings = BuildingManager.getWalkableBuildings(person, diningBuildings);
            diningBuildings = BuildingManager.getNonMalfunctioningBuildings(diningBuildings);
            diningBuildings = BuildingManager.getLeastCrowdedBuildings(diningBuildings);

            if (diningBuildings.size() > 0) {
                Map<Building, Double> diningBuildingProbs = BuildingManager.getBestRelationshipBuildings(
                        person, diningBuildings);
                result = RandomUtil.getWeightedRandomObject(diningBuildingProbs);
            }
        }

        return result;
    }

    /**
     * Gets a kitchen in the person's settlement that currently has a dessert made available.
     * @param person the person to check for
     * @return the kitchen or null if none.
     */
    public static PreparingDessert getKitchenWithDessert(Person person) {
    	PreparingDessert result = null;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
            Settlement settlement = person.getSettlement();
            BuildingManager manager = settlement.getBuildingManager();
            List<Building> cookingBuildings = manager.getBuildings(BuildingFunction.PREPARING_DESSERT);
            Iterator<Building> i = cookingBuildings.iterator();
            while (i.hasNext()) {
                Building building = i.next();
                PreparingDessert kitchen = (PreparingDessert) building.getFunction(BuildingFunction.PREPARING_DESSERT);
                if (kitchen.hasFreshDessert()) result = kitchen;
            }
        }

        return result;
    }

    /**
     * Gets the amount of dessert in the whole settlement.
     * @return dessertAvailable
     
    public static Boolean checkAmountAV(String name, Inventory inv) {
	    AmountResource dessertAR = AmountResource.findAmountResource(name);  
		double dessertAvailable = inv.getAmountResourceStored(dessertAR, false);
		boolean result;
		if (dessertAvailable > getMassPerServing())
			result = true;
		else
			result = false;
		return result;
    }
    */
    
    /**
     * Gets the quantity of one serving of dessert
     * @return quantity
     */
    public static double getMassPerServing() {
        PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
        return config.getDessertConsumptionRate() / NUM_OF_DESSERT_PER_SOL;    
    }
    
    /**
     * Checks if any dessert ingredient is available.
     * @param person the person to check.
     * @return true if any dessert ingredient is available.
     */
    public static boolean isDessertIngredientAvailable(Person person) {
        boolean isAvailable = false;
        Unit containerUnit = person.getContainerUnit();
        // if a person is inside a settlement or a vehicle
        if (containerUnit != null) {
            try {
                Inventory inv = containerUnit.getInventory();
          		String [] availableDesserts = PreparingDessert.getArrayOfDesserts(); 	        
    	        for(String n : availableDesserts) {
    	        	//double amount = PreparingDessert.getDryMass(n);
    	        	double amount = PreparingDessert.getDessertMassPerServing();
    	        	isAvailable = Storage.retrieveAnResource(amount, n, inv, false);
    	        } 	         	
/*
                boolean d1 = checkAmountAV("Soymilk", inv);
                boolean d2 = checkAmountAV("Sugarcane Juice", inv);
                boolean d3 = checkAmountAV("Strawberry", inv);
                boolean d4 = checkAmountAV("Granola Bar", inv);
                boolean d5 = checkAmountAV("Blueberry Muffin", inv);
                boolean d6 = checkAmountAV("Cranberry Juice", inv);                           
            	result = d1 || d2 || d3 || d4 || d5 || d6;
 */
 
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        return isAvailable;
    }

    @Override
    public int getEffectiveSkillLevel() {
        return 0;
    }

    @Override
    public List<SkillType> getAssociatedSkills() {
        List<SkillType> results = new ArrayList<SkillType>(0);
        return results;
    }

    @Override
    public void destroy() {
        super.destroy();

        dessert = null;
    }
}