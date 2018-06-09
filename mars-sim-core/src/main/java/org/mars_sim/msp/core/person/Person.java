/**
 * Mars Simulation Project
 * Person.java
 * @version 3.1.0 2017-01-14
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LifeSupportType;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.task.Relax;
import org.mars_sim.msp.core.person.ai.task.Sleep;
import org.mars_sim.msp.core.person.ai.task.Walk;
import org.mars_sim.msp.core.person.medical.MedicalAid;
import org.mars_sim.msp.core.reportingAuthority.CNSAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.CSAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ESAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ISROMissionControl;
import org.mars_sim.msp.core.reportingAuthority.JAXAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.MarsSocietyMissionControl;
import org.mars_sim.msp.core.reportingAuthority.NASAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.RKAMissionControl;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Medical;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The Person class represents a person on Mars. It keeps track of everything
 * related to that person and provides information about him/her.
 */
public class Person extends Unit implements VehicleOperator, MissionMember, Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static transient Logger logger = Logger.getLogger(Person.class.getName());
	/** The base carrying capacity (kg) of a person. */
	private final static double BASE_CAPACITY = 60D;

	private final static String EARTH = "Earth";
	private final static String MARS = "Mars";
	
	private final static String HEIGHT = "Height";
	private final static String WEIGHT = "Weight";
	
	private final static double AVERAGE_TALL_HEIGHT = 176.5;
	private final static double AVERAGE_SHORT_HEIGHT = 162.5;
	final static double AVERAGE_HEIGHT = 169.5;//(AVERAGE_TALL_HEIGHT + AVERAGE_SHORT_HEIGHT)/2D;
	
	private final static double AVERAGE_HIGH_WEIGHT = 68.5;
	private final static double AVERAGE_LOW_WEIGHT = 57.2;
	final static double AVERAGE_WEIGHT = 62.85 ; //(AVERAGE_HIGH_WEIGHT + AVERAGE_LOW_WEIGHT)/2D ;
	
	private static final String POLITICIAN = "Politician";
	
	// static unit identifier
	private static int unitCount = 0;
	
	// Data members
	/** Unique person id. */
	private int pid;
	/** True if a person is rationing water */
	private boolean waterRation;
	/** True if a person is born on Mars. */	
	private boolean bornOnMars;
	/** True if person is buried. */
	private boolean isBuried;
	/** True if person is declared dead. */	
	private boolean declaredDead;	
	/** The age of a person */
	private int age;
	/** The cache for sol. */
	private int solCache = 1;
    /** The cache for msol1 */     
 	private double msolCache = -1D;
	//private int[] emotional_states;
	/** The height of the person (in cm). */
	private double height;
	/** The height of the person (in kg). */
	private double weight;
	/** Settlement X location (meters) from settlement center. */
	private double xLoc;
	/** Settlement Y location (meters) from settlement center. */
	private double yLoc;
	
	/** The birthplace of the person. */
	private String birthplace;
	/** The person's name. */
	private String name;
	/** The person's sponsor. */
	private String sponsor;
	/** The person's country. */	
	private String country;
	/** The person's blood type. */
	private String bloodType;
	/** The person's last word before departing. */
	private String lastWord;
	
	/** The gender of the person (male or female). */
	private GenderType gender;
	/** The birth time of the person. */
	private EarthClock birthTimeStamp;
	/** The settlement the person is currently associated with. */
	private Settlement associatedSettlement;
	/** Manager for Person's natural attributes. */
	private NaturalAttributeManager attributes;
	/** Person's mind. */
	private Mind mind;
	/** Person's physical condition. */
	private PhysicalCondition condition;
	/** Person's circadian clock. */	
	private CircadianClock circadian;
			
	private Favorite favorite;
	
	private TaskSchedule taskSchedule;
	
	private JobHistory jobHistory;
	
	private Role role;
	
	private Preference preference;

	private LifeSupportType support;

	private Cooking kitchenWithMeal;
	
	private PreparingDessert kitchenWithDessert;

	private ReportingAuthority ra;
	
	private Point2D bed;

	private Settlement buriedSettlement;
	
	private Building quarters;
	
	private Building diningBuilding;
	
	private Building currentBuilding;
	
	private Vehicle vehicle;

	private Vehicle associatedVehicle;
	
	private Relax relax;
	
	private Sleep sleep;
	
	private Walk walk;
	
	/** The person's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement;
	/** The person's paternal chromosome. */
	private Map<Integer, Gene> paternal_chromosome;
	/** The person's maternal chromosome. */
	private Map<Integer, Gene> maternal_chromosome;

	//private Simulation sim = Simulation.instance();
	private MarsClock marsClock;
	
	private EarthClock earthClock;
	
	private MasterClock masterClock;

	//private PersonConfig config; 

	/**
	 * Must be synchronised to prevent duplicate ids being assigned via different threads.
	 * @return
	 */
	private static synchronized int getNextCount() {
		return unitCount++;
	}
	
	/**
	 * Constructor 1 : used by PersonBuilderImpl
	 * Creates a Person object at a given settlement.
	 *
	 * @param name
	 *            the person's name
	 * @param settlement
	 *            {@link Settlement} the settlement the person is at
	 * @throws Exception
	 *             if no inhabitable building available at settlement.
	 */
	protected Person(String name, Settlement settlement) {
		super(name, settlement.getCoordinates());
		super.setDescription(settlement.getName());

		this.pid = getNextCount();
		
		// Initialize data members
		this.name = name;
		this.xLoc = 0D;
		this.yLoc = 0D;
		this.associatedSettlement = settlement;
	}

	/**
	 * Constructor 2
	 * Creates a Person object at a given settlement.
	 *
	 * @param name
	 *            the person's name
	 * @param gender
	 *            {@link GenderType} the person's gender
	 * @param country
	 *            the person's country of origin
	 * @param settlement
	 *            {@link Settlement} the settlement the person is at
	 * @param sponsor
	 *            the person's sponsoring agency
	 * @throws Exception
	 *             if no inhabitable building available at settlement.

	Person(String name, PersonGender gender, String country, Settlement settlement, String sponsor) {
		// logger.info("Person's constructor is in " +
		// Thread.currentThread().getName() + " Thread");
		// Use Unit constructor
		super(name, settlement.getCoordinates());
		super.setDescription(settlement.getName());

		// Initialize data members
		this.name = name;
		this.country = country;
		if (country != null)
			birthplace = "Earth";
		else
			birthplace = "Mars";
		this.xLoc = 0D;
		this.yLoc = 0D;
		this.gender = gender;
		this.associatedSettlement = settlement;
		this.sponsor = sponsor;

	}
*/

	/*
	 * Uses static factory method to create an instance of PersonBuilder
	 */
	public static PersonBuilder<?> create(String name, Settlement settlement) {
		return new PersonBuilderImpl(name, settlement);
	}

	/**
	 * Initialize field data, class and maps
	 */
	public void initialize() {

		//sim = Simulation.instance();
		masterClock = Simulation.instance().getMasterClock();
		if (masterClock != null) { // avoid NullPointerException during maven test
			marsClock = masterClock.getMarsClock();
			earthClock = masterClock.getEarthClock();
	        // TODO : avoid declaring a birth clock for each person
	        // Find a way to use existing EarthClock inside MasterClock, plus the difference in date
			birthTimeStamp = new EarthClock(createBirthTimeString());
		}

		isBuried = false;

		//config = SimulationConfig.instance().getPersonConfiguration();

		attributes = new NaturalAttributeManager(this);

		jobHistory = new JobHistory(this);

		mind = new Mind(this);

		setupChromosomeMap();
				
		circadian = new CircadianClock(this);

		condition = new PhysicalCondition(this);

		scientificAchievement = new HashMap<ScienceType, Double>(0);

		favorite = new Favorite(this);

		role = new Role(this);

		taskSchedule = new TaskSchedule(this);

		preference = new Preference(this);

		assignReportingAuthority();

		// Put person in proper building.
		associatedSettlement.getInventory().storeUnit(this);
		// Note: setAssociatedSettlement(settlement)  will cause suffocation when reloading from a saved sim
		BuildingManager.addToRandomBuilding(this, associatedSettlement); // why failed ?
																// testWalkingStepsRoverToExterior(org.mars_sim.msp.core.person.ai.task.WalkingStepsTest)
		support = getLifeSupportType();
	}

	/**
	 * Compute a person's chromosome map
	 */
	// 2016-01-13 Added setupChromosomeMap()
	public void setupChromosomeMap() {
		paternal_chromosome = new HashMap<>();
		maternal_chromosome = new HashMap<>();

		if (bornOnMars) {
			// TODO: figure out how to account for growing characteristics such as height and weight
			// and define various traits get passed on from parents
		} else {
			// Biochemistry: id 0 - 19
			setupBloodType();
			// Physical Characteristics: id 20 - 39
			setupHeight();
			setupWeight();
			// Personality traits: id 40 - 59
			setupAttributeTrait();
		}

	}

	/**
	 * Compute a person's attributes and its chromosome
	 */
	// 2016-01-12 Added setupTrait()
	public void setupAttributeTrait() {
		// TODO: set up a set of genes that was passed onto this person from two
		// hypothetical parents
		int ID = 40;
		boolean dominant = false;

		// Set inventory total mass capacity based on the person's strength.
		int strength = attributes.getAttribute(NaturalAttribute.STRENGTH);
		getInventory().addGeneralCapacity(BASE_CAPACITY + strength);

		int rand = RandomUtil.getRandomInt(100);

		Gene trait1_G = new Gene(this, ID, "Trait 1", true, dominant, "Introvert", rand);
		paternal_chromosome.put(ID, trait1_G);

	}

	/**
	 * Compute a person's blood type and its chromosome
	 */
	// 2016-01-12 Added setupBloodType()
	public void setupBloodType() {
		int ID = 1;
		boolean dominant = false;

		String dad_bloodType = null;
		int rand = RandomUtil.getRandomInt(2);
		if (rand == 0) {
			dad_bloodType = "A";
			dominant = true;
		} else if (rand == 1) {
			dad_bloodType = "B";
			dominant = true;
		} else if (rand == 2) {
			dad_bloodType = "O";
			dominant = false;
		}

		// Biochemistry 0 - 19
		Gene dad_bloodType_G = new Gene(this, ID, "Blood Type", true, dominant, dad_bloodType, 0);
		paternal_chromosome.put(ID, dad_bloodType_G);

		String mom_bloodType = null;
		rand = RandomUtil.getRandomInt(2);
		if (rand == 0) {
			mom_bloodType = "A";
			dominant = true;
		} else if (rand == 1) {
			mom_bloodType = "B";
			dominant = true;
		} else if (rand == 2) {
			mom_bloodType = "O";
			dominant = false;
		}

		Gene mom_bloodType_G = new Gene(this, 0, "Blood Type", false, dominant, mom_bloodType, 0);
		maternal_chromosome.put(0, mom_bloodType_G);

		if (dad_bloodType.equals("A") && mom_bloodType.equals("A"))
			bloodType = "A";
		else if (dad_bloodType.equals("A") && mom_bloodType.equals("B"))
			bloodType = "AB";
		else if (dad_bloodType.equals("A") && mom_bloodType.equals("O"))
			bloodType = "A";
		else if (dad_bloodType.equals("B") && mom_bloodType.equals("A"))
			bloodType = "AB";
		else if (dad_bloodType.equals("B") && mom_bloodType.equals("B"))
			bloodType = "B";
		else if (dad_bloodType.equals("B") && mom_bloodType.equals("O"))
			bloodType = "B";
		else if (dad_bloodType.equals("O") && mom_bloodType.equals("A"))
			bloodType = "A";
		else if (dad_bloodType.equals("O") && mom_bloodType.equals("B"))
			bloodType = "B";
		else if (dad_bloodType.equals("O") && mom_bloodType.equals("O"))
			bloodType = "O";

	}

	/**
	 * Compute a person's height and its chromosome
	 */
	// 2016-01-12 Added setupHeight()
	public void setupHeight() {
		int ID = 20;
		boolean dominant = false;

		// For a 20-year-old in the US:
		// male :   height : 176.5	weight : 68.5
		// female : height : 162.6	weight : 57.2

		// TODO: factor in country of origin.
		// TODO: look for a gender-correlated curve

		// 2017-04-11 Attempt to compute height with gaussian curve
		double dad_height = AVERAGE_TALL_HEIGHT + RandomUtil.getGaussianDouble() * RandomUtil.getRandomInt(22);
		double mom_height = AVERAGE_SHORT_HEIGHT + RandomUtil.getGaussianDouble() * RandomUtil.getRandomInt(15);

		Gene dad_height_G = new Gene(this, ID, HEIGHT, true, dominant, null, dad_height);
		paternal_chromosome.put(ID, dad_height_G);

		Gene mom_height_G = new Gene(this, ID, HEIGHT, false, dominant, null, mom_height);
		maternal_chromosome.put(ID, mom_height_G);

		double genetic_factor = .65;
		double sex_factor = (AVERAGE_TALL_HEIGHT - AVERAGE_HEIGHT)/AVERAGE_HEIGHT; 
		// 2017-04-11 Add Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			height = Math.round((genetic_factor * dad_height + (1-genetic_factor) * mom_height * (1 + sex_factor))*100D)/100D;
		else
			height = Math.round(((1-genetic_factor) * dad_height + genetic_factor * mom_height * (1 - sex_factor))*100D)/100D;


	}


	/**
	 * Compute a person's weight and its chromosome
	 */
	// 2016-01-12 Added setupWeight()
	public void setupWeight() {
		int ID = 21;
		boolean dominant = false;

		// For a 20-year-old in the US:
		// male :   height : 176.5	weight : 68.5
		// female : height : 162.6	weight : 57.2

		// TODO: factor in country of origin.
		// TODO: look for a gender-correlated curve

		// 2017-04-11 Attempt to compute height with gaussian curve
		double dad_weight = AVERAGE_HIGH_WEIGHT + RandomUtil.getGaussianDouble() * RandomUtil.getRandomInt(10);
		double mom_weight = AVERAGE_LOW_WEIGHT + RandomUtil.getGaussianDouble() * RandomUtil.getRandomInt(15) ;

		Gene dad_weight_G = new Gene(this, ID, WEIGHT, true, dominant, null, dad_weight);
		paternal_chromosome.put(ID, dad_weight_G);

		Gene mom_weight_G = new Gene(this, ID, WEIGHT, false, dominant, null, mom_weight);
		maternal_chromosome.put(ID, mom_weight_G);

		double genetic_factor = .65;
		double sex_factor = (AVERAGE_HIGH_WEIGHT - AVERAGE_WEIGHT)/AVERAGE_WEIGHT; // for male
		double height_factor = height/AVERAGE_HEIGHT;
		// 2017-04-11 Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			weight = Math.round(height_factor*(genetic_factor * dad_weight + (1-genetic_factor) * mom_weight * (1 + sex_factor))*100D)/100D;
		else
			weight = Math.round(height_factor*((1-genetic_factor) * dad_weight + genetic_factor * mom_weight * (1 - sex_factor))*100D)/100D;

		setBaseMass(weight);

	}

	/*
	 * Sets sponsoring agency for the person
	 */
	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}

	/*
	 * Assigns sponsoring agency and set up specific mission control for the person
	 */
	public void assignReportingAuthority() {
		if (ra == null) {
			if (sponsor.contains("CNSA")) {
				ra = CNSAMissionControl.createMissionControl(); // ProspectingMineral

			} else if (sponsor.contains("CSA")) {
				ra = CSAMissionControl.createMissionControl(); // AdvancingSpaceKnowledge

			} else if (sponsor.contains("ESA")) {
				ra = ESAMissionControl.createMissionControl(); // DevelopingSpaceActivity;

			} else if (sponsor.contains("ISRO")) {
				ra = ISROMissionControl.createMissionControl(); // DevelopingAdvancedTechnology

			} else if (sponsor.contains("JAXA")) {
				ra = JAXAMissionControl.createMissionControl(); // ResearchingSpaceApplication

			} else if (sponsor.contains("NASA")) {
				ra = NASAMissionControl.createMissionControl(); // FindingLife

			} else if (sponsor.contains("MS")) {
				ra = MarsSocietyMissionControl.createMissionControl(); // SettlingMars

			} else if (sponsor.contains("RKA")) {
				ra = RKAMissionControl.createMissionControl(); // ResearchingHealthHazard

			} else {
				logger.warning(name + " has no reporting authority!");
				//ra = null;
			}
		}
	}

	/*
	 * Gets sponsoring agency for the person
	 */
	public ReportingAuthority getReportingAuthority() {
		return ra;
	}

	/*
	 * Gets task preference for the person
	 */
	public Preference getPreference() {
		return preference;
	}

	/**
	 * Sets the role for a person.
	 */
	public void setRole(RoleType type) {

		if (type == RoleType.MAYOR) {
			getRole().setNewRoleType(type);
			Job job = JobManager.getJob(POLITICIAN);
			if (job != null) {
				mind.setJob(job, true, JobManager.SETTLEMENT, JobAssignmentType.APPROVED, JobManager.SETTLEMENT);
			}
		} else
			getRole().setNewRoleType(type);
	}

	/**
	 * Gets the instance of Role for a person.
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Gets the instance of JobHistory for a person.
	 */
	public JobHistory getJobHistory() {
		return jobHistory;
	}

	/**
	 * Gets the instance of Favorite for a person.
	 */
	public Favorite getFavorite() {
		return favorite;
	}

	/**
	 * Gets the instance of the task schedule for a person.
	 */
	public TaskSchedule getTaskSchedule() {
		return taskSchedule;
	}

	/**
	 * Create a string representing the birth time of the person.
	 *
	 * @return birth time string.
	 */
	// 2017-01-03 Revise createBirthTimeString()
	private String createBirthTimeString() {
		StringBuilder s = new StringBuilder();
		// Set a birth time for the person
		int year = EarthClock.getCurrentYear(earthClock) - RandomUtil.getRandomInt(22, 62);
		// 2003 + RandomUtil.getRandomInt(10) + RandomUtil.getRandomInt(10);
		s.append(year);

		int month = RandomUtil.getRandomInt(11) + 1;
        //String monthString = EarthClock.getMonthForInt(month-1).substring(0, 3);
        s.append("-").append(month).append("-");

		int day;
		if (month == 2) {
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				day = RandomUtil.getRandomInt(28) + 1;
			} else {
				day = RandomUtil.getRandomInt(27) + 1;
			}
		} else {
			if (month % 2 == 1) {
				day = RandomUtil.getRandomInt(30) + 1;
			} else {
				day = RandomUtil.getRandomInt(29) + 1;
			}
		}
		// TODO: find out why sometimes day = 0 as seen on
		if (day == 0) {
			logger.warning(name + "'s date of birth is on the day 0th. Incrementing to the 1st.");
			day = 1;
		}

		if (day < 10)
			s.append(0);
		s.append(day).append(" ");

		int hour = RandomUtil.getRandomInt(23);
		if (hour < 10)
			s.append(0);
		s.append(hour).append(":");

		int minute = RandomUtil.getRandomInt(59);
		if (minute < 10)
			s.append(0);
		s.append(minute).append(":");

		int second = RandomUtil.getRandomInt(59);
		if (second < 10)
			s.append(0);
		s.append(second);

		return s.toString();
	}

	/**
	 * Is the person outside
	 * @return true if the person is outside
	 */
	public boolean isOutside() {
		if (isBuried)
			return true;
		else {
			if (getContainerUnit() == null)
				return true;
			else
				return false;
		}
	}
	
	/**
	 * Is the person inside a vehicle
	 * @return true if the person is inside a vehicle
	 */
	public boolean isInVehicle() {
		if (isBuried)
			return false;
		else {
			if (getContainerUnit() instanceof Vehicle)
				return true;
			else
				return false;
		}
	}
	
	/**
	 * Is the person inside a settlement
	 * @return true if the person is inside a settlement
	 */
	public boolean isInSettlement() {
		if (isBuried)
			return false;
		else {
			if (getContainerUnit() instanceof Settlement)
				return true;
			else
				return false;
		}
	}
	
	/**
	 * Is the person inside a settlement or a vehicle
	 * @return true if the person is inside a settlement or a vehicle
	 */
	public boolean isInside() {
		if (isBuried)
			return false;
		else {
			Unit container = getContainerUnit();
			if (container == null)
				return false;
			else if (container instanceof Settlement)
				return true;
			else if (container instanceof Vehicle)
				return true;
			else
				return false;
		}

	}
	
	/**
	 * @return {@link LocationSituation} the person's location
	 */
	public LocationSituation getLocationSituation() {
		if (isBuried)
			return LocationSituation.BURIED;
		else {
			Unit container = getContainerUnit();
			if (container instanceof Settlement)
				return LocationSituation.IN_SETTLEMENT;
			else if (container instanceof Vehicle)
				return LocationSituation.IN_VEHICLE;
			else if (container == null)
				return LocationSituation.OUTSIDE;
			else
				return LocationSituation.UNKNOWN;
		}

	}

	/**
	 * Gets the person's X location at a settlement.
	 *
	 * @return X distance (meters) from the settlement's center.
	 */
	public double getXLocation() {
		return xLoc;
	}

	/**
	 * Sets the person's X location at a settlement.
	 *
	 * @param xLocation
	 *            the X distance (meters) from the settlement's center.
	 */
	public void setXLocation(double xLocation) {
		this.xLoc = xLocation;
	}

	/**
	 * Gets the person's Y location at a settlement.
	 *
	 * @return Y distance (meters) from the settlement's center.
	 */
	public double getYLocation() {
		return yLoc;
	}

	/**
	 * Sets the person's Y location at a settlement.
	 *
	 * @param yLocation
	 */
	public void setYLocation(double yLocation) {
		this.yLoc = yLocation;
	}

	/**
	 * Get settlement person is at, null if person is not at a settlement
	 *
	 * @return the person's settlement
	 */
	public Settlement getSettlement() {

		Unit container = getContainerUnit();
		
		if (container instanceof Settlement) {
			return (Settlement) container;
		}
		
		else if (container instanceof Vehicle){
			Building b = BuildingManager.getBuilding((Vehicle) getContainerUnit());
			if (b != null)
				// still inside the garage
				return b.getSettlement();
			else 
				// either at the vicinity of a settlement or already outside on a mission
				// TODO: need to differentiate which case in future better granularity 
				return null;
		}
		
		else if (container == null) {
			return null;

		}
		
		logger.warning("Error in determining " + getName() + "'s getSettlement() ");
		return null;
/*		
		if (getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
			return (Settlement) getContainerUnit();
		}

		else if (getLocationSituation() == LocationSituation.OUTSIDE) {
			return null;
		}
		
		else if (getLocationSituation() == LocationSituation.IN_VEHICLE) {
			Building b = BuildingManager.getBuilding((Vehicle) getContainerUnit());
			if (b != null)
				return b.getSettlement();
		}

		else if (getLocationSituation() == LocationSituation.BURIED) {
			return null;
		}

		logger.warning("Error in determining " + getName() + "'s getSettlement() ");
		return null;
*/		
	}


	/**
	 * Sets the unit's container unit. Overridden from Unit class.
	 *
	 * @param containerUnit
	 *            the unit to contain this unit.
	 */
	public void setContainerUnit(Unit containerUnit) {
		if (containerUnit instanceof Vehicle) {
			vehicle = (Vehicle) containerUnit;
		}
		super.setContainerUnit(containerUnit);
	}

	/**
	 * Bury the Person at the current location. This happens only if the person
	 * can be retrieved from any containing Settlements or Vehicles found.
	 * The body is fixed at the last location of the containing unit.
	 */
	public void buryBody() {
		Unit containerUnit = getContainerUnit();
		if (containerUnit != null) {
			// Note: if a person is dead inside a vehicle that's outside on Mars,
			// should NOT be retrieved until the body arrives at a settlement
			containerUnit.getInventory().retrieveUnit(this);
		}
		
		// Bury the body
		isBuried = true;
		
		//setAssociatedSettlement(null);
		
		// Set his/her buried settlement
		setBuriedSettlement(associatedSettlement);
		
		// Set unit description to "Dead"
		super.setDescription("Dead");
		
        // Throw unit event.
        fireUnitUpdate(UnitEventType.BURIAL_EVENT);
	}

	/**
	 * Declares the person dead, removes the designated quarter and work shift
	 */
	void setDeclaredDead() {

		declaredDead = true;

		setShiftType(ShiftType.OFF);

		if (quarters != null) {
			LivingAccommodations accommodations = quarters.getLivingAccommodations();
			accommodations.getBedMap().remove(this);
			quarters = null;
		}

		if (bed != null)
			bed = null;

	}


	/**
	 * Person can take action with time passing
	 *
	 * @param time
	 *            amount of time passing (in millisols).
	 */
	public void timePassing(double time) {
		
    	if (marsClock == null) {
    		masterClock = Simulation.instance().getMasterClock();
    		marsClock = masterClock.getMarsClock();
    	}
    	
	    double msol1 = marsClock.getMsol1();
	    
	    if (msolCache != msol1) {
	    	msolCache = msol1;
	    	    
			// If Person is dead, then skip
			if (!condition.isDead()) {// health.getDeathDetails() == null) {

				support = getLifeSupportType();
				
				circadian.timePassing(time, support);
				// Pass the time in the physical condition first as this may result in death.
				condition.timePassing(time, support);

				if (!condition.isDead()) {

					// 2015-06-29 Added calling preference
					preference.timePassing(time);

					try {
						// Mental changes with time passing.
						mind.timePassing(time);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					// check for the passing of each day
					int solElapsed = marsClock.getMissionSol();

					if (solCache != solElapsed) {
						// Check if a person's age should be updated
						age = updateAge();
						solCache = solElapsed;
						
						if (solElapsed % 3 == 0) {
							// Adjust the shiftChoice once every 3 sols based on sleep hour
							int bestSleepTime[] = getPreferredSleepHours();
							taskSchedule.adjustShiftChoice(bestSleepTime);
						}
						
						
						if (solElapsed % 4 == 0) {
							// Increment the shiftChoice once every 4 sols
							taskSchedule.incrementShiftChoice();
						}

						if (solElapsed % 7 == 0) {
							// Normalize the shiftChoice once every week
							taskSchedule.normalizeShiftChoice();
						}				
					}
				}
			}

			else if (!declaredDead) {
				setDeclaredDead();
				mind.setInactive();
			}
			
			else if (!isBuried 
					&& condition.getDeathDetails() != null 
					&& condition.getDeathDetails().getBodyRetrieved()) {
						buryBody();

			}
	    }


		// final long time1 = System.nanoTime();
		// logger.info((time1-time0)/1.0e3 + " ms to process " + name);
	}

	/**
	 * Returns a reference to the Person's natural attribute manager
	 *
	 * @return the person's natural attribute manager
	 */
	public NaturalAttributeManager getNaturalAttributeManager() {
		return attributes;
	}

	/**
	 * Get the performance factor that effect Person with health complaint.
	 *
	 * @return The value is between 0 -> 1.
	 */
	public double getPerformanceRating() {
		return condition.getPerformanceFactor();
	}

	/**
	 * Returns a reference to the Person's physical condition
	 *
	 * @return the person's physical condition
	 */
	public PhysicalCondition getPhysicalCondition() {
		return condition;
	}

	/**
	 * Find a medical aid according to the current location.
	 *
	 * @return Accessible aid.
	 */
	MedicalAid getAccessibleAid() {
		MedicalAid found = null;

		Settlement settlement = getSettlement();
		if (settlement != null) {
			List<Building> infirmaries = settlement.getBuildingManager().getBuildings(FunctionType.MEDICAL_CARE);
			if (infirmaries.size() > 0) {
				int rand = RandomUtil.getRandomInt(infirmaries.size() - 1);
				Building foundBuilding = infirmaries.get(rand);
				found = (MedicalAid) foundBuilding.getMedical();//.getFunction(FunctionType.MEDICAL_CARE);
			}
		}
		
		Vehicle vehicle = getVehicle();
		if (vehicle != null && vehicle instanceof Medical) {
				found = ((Medical) vehicle).getSickBay();
		}

		return found;
	}

	/**
	 * Returns the person's mind
	 *
	 * @return the person's mind
	 */
	public Mind getMind() {
		return mind;
	}

	/**
	 * Updates and returns the person's age
	 *
	 * @return the person's age
	 */
	public int updateAge() {
		age = earthClock.getYear() - birthTimeStamp.getYear() - 1;
		if (earthClock.getMonth() >= birthTimeStamp.getMonth())
			if (earthClock.getDayOfMonth() >= birthTimeStamp.getDayOfMonth())
				age++;

		return age;
	}

	/**
	 * Returns the person's height in cm
	 *
	 * @return the person's height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Returns the person's birth date
	 *
	 * @return the person's birth date
	 */
	public String getBirthDate() {
		return birthTimeStamp.getDateString();
	}

	/**
	 * Get the LifeSupport system supporting this Person. This may be from the
	 * Settlement, Vehicle or Equipment.
	 *
	 * @return Life support system.
	 */
	private LifeSupportType getLifeSupportType() {

		LifeSupportType result = null;
		List<LifeSupportType> lifeSupportUnits = new ArrayList<LifeSupportType>();

		Settlement settlement = getSettlement();
		if (settlement != null) {
			lifeSupportUnits.add(settlement);
		} else {
			Vehicle vehicle = getVehicle();
			if ((vehicle != null) && (vehicle instanceof LifeSupportType)) {

				if (BuildingManager.getBuilding(vehicle) != null) {
					// if the vehicle is inside a garage
					lifeSupportUnits.add(vehicle.getSettlement());
				} else {
					lifeSupportUnits.add((LifeSupportType) vehicle);
				}
			}
		}
/*

		Iterator<Unit> i = getInventory().getContainedUnits().iterator();
		
		while (i.hasNext()) {
			Unit contained = i.next();
			if (contained instanceof LifeSupportType) {
				lifeSupportUnits.add((LifeSupportType) contained);
			}
		}
*/		
		// Get all contained units.
		Collection<Unit> units = getInventory().getContainedUnits();
		for (Unit u : units) {
			if (u instanceof LifeSupportType)
				lifeSupportUnits.add((LifeSupportType) u);
		}
		
		
/*
		Iterator<LifeSupportType> j = lifeSupportUnits.iterator();
		while (j.hasNext() && (result == null)) {
			LifeSupportType goodUnit = j.next();
			if (goodUnit.lifeSupportCheck()) {
				result = goodUnit;
			}
		}
*/	
		// Get first life support unit that checks out.
		for (LifeSupportType goodUnit : lifeSupportUnits) {
			if (result ==  null && goodUnit.lifeSupportCheck()) {
				result = goodUnit;
			}
		}
		
		// If no good units, just get first life support unit.
		if ((result == null) && (lifeSupportUnits.size() > 0)) {
			result = lifeSupportUnits.get(0);
		}

		return result;
	}

	/**
	 * Person consumes given amount of food.
	 *
	 * @param amount
	 *            the amount of food to consume (in kg)
	 * @param takeFromInv
	 *            is food taken from local inventory?
	 
	public void consumeFood(double amount, boolean takeFromInv) {
		if (takeFromInv) {
			// takeFrom Inv is true if meal == null, meaning that the person is
			// on an excursion
			// System.out.println(this.getName() + " is calling consumeFood() in
			// Person.java");
			health.consumeFood(amount, getContainerUnit());
		} else { // The person is in a settlement, a cookedMeal has been eaten
					// no need to call health.consumeFood()
			// health.consumeFood(amount);
		}
	}
*/
	
	// /**
	// * Person consumes given amount of food.
	// * @param amount the amount of food to consume (in kg)
	// * @param takeFromInv
	// */
	// // 2014-11-28 Added consumeDessert()
	// public void consumeDessert(double amount, boolean takeFromInv) {
	// if (takeFromInv) {
	// // takeFrom Inv is true if meal == null, meaning that the person is on an
	// excursion
	// //System.out.println(this.getName() + " is is calling consumeDessert() in
	// Person.java");
	// health.consumeDessert(amount, getContainerUnit());
	// }
	// }

	/**
	 * Person consumes given amount of water.
	 *
	 * @param amount
	 *            the amount of water to consume (in kg)
	 * @param takeFromInv
	 *            is water taken from local inventory?
	 *
	 *            //2014-11-06 ****NOT USED **** Added consumeWater() public
	 *            void consumeLiquid(double amount, boolean takeFromInv) { if
	 *            (takeFromInv) { health.consumeLiquid(amount,
	 *            getContainerUnit()); } else { health.consumeLiquid(amount); }
	 *            }
	 */
	/**
	 * Gets the gender of the person.
	 *
	 * @return the gender
	 */
	public GenderType getGender() {
		return gender;
	}

	public void setGender(GenderType g) {
		gender = g;
	}

	/**
	 * Gets the birthplace of the person
	 *
	 * @return the birthplace
	 * @deprecated TODO internationalize the place of birth for display in user
	 *             interface.
	 */
	public String getBirthplace() {
		return birthplace;
	}

	/**
	 * Gets the person's local group of people (in building or rover)
	 *
	 * @return collection of people in person's location.
	 */
	public Collection<Person> getLocalGroup() {
		Collection<Person> localGroup = new ConcurrentLinkedQueue<Person>();

		if (isInSettlement()) {
			Building building = BuildingManager.getBuilding(this);
			if (building != null) {
				if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
					LifeSupport lifeSupport = building.getLifeSupport();
					localGroup = new ConcurrentLinkedQueue<Person>(lifeSupport.getOccupants());
				}
			}
		} else if (isInVehicle()) {
			Crewable crewableVehicle = (Crewable) getVehicle();
			localGroup = new ConcurrentLinkedQueue<Person>(crewableVehicle.getCrew());
		}

		if (localGroup.contains(this)) {
			localGroup.remove(this);
		}
		return localGroup;
	}

	/**
	 * Checks if the vehicle operator is fit for operating the vehicle.
	 *
	 * @return true if vehicle operator is fit.
	 */
	public boolean isFitForOperatingVehicle() {
		return !condition.hasSeriousMedicalProblems();
	}

	/**
	 * Gets the name of the vehicle operator
	 *
	 * @return vehicle operator name.
	 */
	public String getOperatorName() {
		return getName();
	}

	/**
	 * Gets the settlement the person is currently associated with.
	 *
	 * @return associated settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return associatedSettlement;
	}

	/**
	 * Sets the associated settlement for a person.
	 *
	 * @param newSettlement
	 *            the new associated settlement or null if none.
	 */
	public void setAssociatedSettlement(Settlement newSettlement) {
		if (associatedSettlement != newSettlement) {
			Settlement oldSettlement = associatedSettlement;
			associatedSettlement = newSettlement;
			fireUnitUpdate(UnitEventType.ASSOCIATED_SETTLEMENT_EVENT, associatedSettlement);
			if (oldSettlement != null) {
				oldSettlement.removePerson(this);
				oldSettlement.fireUnitUpdate(UnitEventType.REMOVE_ASSOCIATED_PERSON_EVENT, this);
			}
			if (newSettlement != null) {
				newSettlement.addPerson(this);
				newSettlement.fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_PERSON_EVENT, this);
			}

			// set description for this person
			//if (associatedSettlement == null) {
			//	setBuriedSettlement(oldSettlement);
			//	super.setDescription("Dead");
			//} else
				super.setDescription(associatedSettlement.getName());
		}
	}

	/**
	 * Sets the associated settlement for a person.
	 *
	 * @param settlement
	 */
	public void setBuriedSettlement(Settlement settlement) {
		buriedSettlement = settlement;
	}

	public Settlement getBuriedSettlement() {
		return buriedSettlement;
	}

	/**
	 * Gets the person's achievement credit for a given scientific field.
	 *
	 * @param science
	 *            the scientific field.
	 * @return achievement credit.
	 */
	public double getScientificAchievement(ScienceType science) {
		double result = 0D;
		if (scientificAchievement.containsKey(science)) {
			result = scientificAchievement.get(science);
		}
		return result;
	}

	/**
	 * Gets the person's total scientific achievement credit.
	 *
	 * @return achievement credit.
	 */
	public double getTotalScientificAchievement() {
		double result = 0d;
		for (double value : scientificAchievement.values()) {
			result += value;
		}
		return result;
	}

	/**
	 * Add achievement credit to the person in a scientific field.
	 *
	 * @param achievementCredit
	 *            the achievement credit.
	 * @param science
	 *            the scientific field.
	 */
	public void addScientificAchievement(double achievementCredit, ScienceType science) {
		if (scientificAchievement.containsKey(science)) {
			achievementCredit += scientificAchievement.get(science);
		}
		scientificAchievement.put(science, achievementCredit);
	}

	public void setDiningBuilding(Building diningBuilding) {
		this.diningBuilding = diningBuilding;
	}

	public Building getDiningBuilding() {
		return diningBuilding;
	}

	public void setKitchenWithMeal(Cooking kitchen) {
		this.kitchenWithMeal = kitchen;
	}

	public Cooking getKitchenWithMeal() {
		return kitchenWithMeal;
	}

	public void setKitchenWithDessert(PreparingDessert kitchen) {
		this.kitchenWithDessert = kitchen;
	}

	public PreparingDessert getKitchenWithDessert() {
		return kitchenWithDessert;
	}

	/**
	 * Gets the building the person is located at Returns null if outside of a
	 * settlement
	 *
	 * @return building
	 */
	// 2015-05-18 Added getBuildingLocation()
	public Building getBuildingLocation() {
		return computeCurrentBuilding();
	}

	/**
	 * Computes the building the person is currently located at Returns null if
	 * outside of a settlement
	 *
	 * @return building
	 */
	// 2017-03-08 Added setCurrentBuilding()
	public Building computeCurrentBuilding() {
		if (currentBuilding != null) {
			return currentBuilding;
		}
		else if (isInSettlement()) {
			currentBuilding = getSettlement().getBuildingManager().getBuildingAtPosition(getXLocation(), getYLocation());
		}

		return currentBuilding;
	}

	/**
	 * Computes the building the person is currently located at Returns null if
	 * outside of a settlement
	 *
	 * @return building
	 */
	// 2017-03-08 Added setCurrentBuilding()
	public void setCurrentBuilding(Building building) {
		currentBuilding = building;
	}

	@Override
	public String getTaskDescription() {
		return getMind().getTaskManager().getTaskDescription(false);
	}

	@Override
	public void setMission(Mission newMission) {
		getMind().setMission(newMission);
	}

	@Override
	public void setShiftType(ShiftType shiftType) {
		taskSchedule.setShiftType(shiftType);
	}

	public double getFatigue() {
		return condition.getFatigue();
	}

	public double getStress() {
		return condition.getStress();
	}

	public int[] getPreferredSleepHours() {
		return circadian.getPreferredSleepHours();
	}

	public void updateSleepCycle(int millisols, boolean updateType) {
		circadian.updateSleepCycle(millisols, updateType);
	}

	// 2015-12-12 Added setEmotionalStates()
	//public void setEmotionalStates(int[] states) {
	//	emotional_states = states;
	//}

	public Building getQuarters() {
		return quarters;
	}

	public void setQuarters(Building quarters) {
		this.quarters = quarters;
	}

	public Point2D getBed() {
		return bed;
	}

	public void setBed(Point2D bed) {
		this.bed = bed;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String c) {
		this.country = c;
		if (c != null)
			birthplace = EARTH;
		else
			birthplace = MARS;
	}

	public boolean isDeclaredDead() {
		return declaredDead;
	}

	public boolean isBuried() {
		return isBuried;
	}

	public void setLastWord(String s) {
		lastWord = s;
	}

	public String getLastWord() {
		return lastWord;
	}

	//@Override
	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	/**
	 * Get vehicle person is in, null if person is not in vehicle
	 * @return the person's vehicle
	 */
	public Vehicle getVehicle() {
		//if (getLocationSituation() == LocationSituation.IN_VEHICLE)
		//	return (Vehicle) getContainerUnit();
		//else
			return vehicle;
	}

	public Vehicle getAssociatedVehicle() {
		return associatedVehicle;
	}

	public void setAssociatedVehicle(Vehicle v) {
		associatedVehicle = v;
	}
	
	public CircadianClock getCircadianClock() {
		return circadian;
	}
	
	public void setWaterRation(boolean value) {
		waterRation = value;
	}

	public boolean getWaterRation() {
		return waterRation;
	}
	
	public Relax getRelax() {
		return relax;
	}
	 
	public void setRelax(Relax relax) {
		this.relax = relax;
	}

	public Sleep getSleep() {
		return sleep;
	}
	 
	public void setSleep(Sleep sleep) {
		this.sleep = sleep;
	}
	
	public Walk getWalk() {
		return walk;
	}
	
	public void setWalk(Walk walk) {
		this.walk = walk;
	}
	
	/**
	 * Get the unique person id for this unit
	 * @return pid
	 */
	public int getPid() {
		return pid;
	}
	
	@Override
	public void destroy() {
		super.destroy();
		relax = null;
		sleep = null;
		walk = null;
		circadian = null;
		vehicle = null;
		associatedVehicle = null;
		associatedSettlement = null;
		buriedSettlement = null;
		quarters = null;	
		diningBuilding = null;	
		currentBuilding = null;
		condition = null;
		favorite = null;
		taskSchedule = null;
		jobHistory = null;
		role = null;
		preference = null;
		support = null;
		kitchenWithMeal = null;	
		kitchenWithDessert = null;
		ra = null;
		bed = null;
		
		attributes.destroy();
		attributes = null;
		mind.destroy();
		mind = null;
		//condition.destroy();
		condition = null;
		gender = null;
		birthTimeStamp = null;

		scientificAchievement.clear();
		scientificAchievement = null;
	}

}