/*
 * Mars Simulation Project
 * Person.java
 * @date 2021-09-20
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.PersonAttributeManager;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.core.person.health.MedicalAid;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Medical;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleOperator;

/**
 * The Person class represents a person on Mars. It keeps track of everything
 * related to that person and provides information about him/her.
 */
public class Person extends Unit implements VehicleOperator, MissionMember, Serializable, Temporal {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/* default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Person.class.getName());

	public static final int MAX_NUM_SOLS = 3;
	
	private static final double SMALL_AMOUNT = 0.00001;
	
	private final static String EARTH_BIRTHPLACE = "Earth";
	private final static String MARS_BIRTHPLACE = "Mars";
	private final static String HEIGHT_GENE = "Height";
	private final static String WEIGHT_GENE = "Weight";
	
	private final static String EARTHLING = "Earthling";
	private final static String ONE_SPACE = " ";
	
//	private final static String MARTIAN = "Martian";
	
	/** The average height of a person. */
	private static final double averageHeight;
	/** The average weight of a person. */
	private static final double averageWeight;
	/** The average upper height of a person. */
	private static final double tall;
	/** The average low height of a person. */
	private static final double shortH;
	/** The average high weight of a person. */
	private static final double highW;
	/** The average low weight of a person. */
	private static final double lowW;
	
	// Transient data members
	/** The extrovert score of a person. */
	private transient int extrovertScore = -1;
	
	// Data members
	/** True if the person is a preconfigured crew member. */
	private boolean preConfigured;
	/** True if the person is born on Mars. */
	private boolean bornOnMars;
	/** True if the person is buried. */
	private boolean isBuried;
	/** True if the person is declared dead. */
	private boolean declaredDead;

	/** The year of birth of a person */
	private int year;
	/** The month of birth of a person */
	private int month;
	/** The day of birth of a person */
	private int day;
	/** The age of a person */
	private int age = -1;

	/** The settlement the person is currently associated with. */
	private Integer associatedSettlementID = Integer.valueOf(-1);
	/** The buried settlement if the person has been deceased. */
	private Integer buriedSettlement = Integer.valueOf(-1);

	/** The eating speed of the person [kg/millisol]. */
	private double eatingSpeed = 0.5 + .1 * RandomUtil.getRandomDouble(1) - .1 * RandomUtil.getRandomDouble(1);
	/** The height of the person (in cm). */
	private double height;
	/** The height of the person (in kg). */
	private double weight;
	/** Settlement X location (meters) from settlement center. */
	private double xLoc;
	/** Settlement Y location (meters) from settlement center. */
	private double yLoc;
	/** Settlement Z location (meters) from settlement center. */
	private double zLoc;
	/** The walking speed modifier. */
	private double walkSpeedMod = 1.1;
	
	/** The birth timestamp of the person. */
//	private String birthTimeStamp;
	/** The birthplace of the person. */
	private String birthplace;
	/** The person's name. */
//	private String name;
	/** The person's first name. */
	private String firstName;
	/** The person's last name. */
	private String lastName;
//	/** The person's sponsor. */
//	private String sponsor;
	/** The person's country of origin. */
	private String country;
	/** The person's blood type. */
	private String bloodType;

	/** The gender of the person (male or female). */
	private GenderType gender = GenderType.MALE;
	
	/** The person's skill manager. */
	private SkillManager skillManager;
	/** Manager for Person's natural attributes. */
	private NaturalAttributeManager attributes;
	/** Person's mind. */
	private Mind mind;
	/** Person's physical condition. */
	private PhysicalCondition condition;
	/** Person's circadian clock. */
	private CircadianClock circadian;
	/** Person's Favorite instance. */
	private Favorite favorite;
	/** Person's TaskSchedule instance. */
	private TaskSchedule taskSchedule;
	/** Person's JobHistory instance. */
	private JobHistory jobHistory;
	/** Person's Role instance. */
	private Role role;
	/** Person's Preference instance. */
	private Preference preference;
	/** Person's LifeSupportInterface instance. */
	private LifeSupportInterface support;
	/** Person's Cooking instance. */
	private Cooking kitchenWithMeal;
	/** Person's PreparingDessert instance. */
	private PreparingDessert kitchenWithDessert;
	/** Person's ReportingAuthority instance. */
	private ReportingAuthority ra;
	/** The bed location of the person */
	private Point2D bed;
	/** The quarters that the person belongs. */
	private int quartersInt = -1;
	/** The current building location of the person. */
	private int currentBuildingInt;
	/** The EVA suit that the person has donned on. */
	private EVASuit suit;
	/** The person's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement = new ConcurrentHashMap<ScienceType, Double>();
	/** The person's paternal chromosome. */
	private Map<Integer, Gene> paternal_chromosome;
	/** The person's maternal chromosome. */
	private Map<Integer, Gene> maternal_chromosome;
	/** The person's mission experiences */
	private Map<Integer, List<Double>> missionExperiences;
	/** The person's EVA times */
	private SolMetricDataLogger<String> eVATaskTime;
	/** The person's water/oxygen consumption */
	private SolMetricDataLogger<Integer> consumption;
	/** The person's prior training */
	private List<TrainingType> trainings;
	private ScientificStudy study;
	private Set<ScientificStudy> collabStudies;
	
	static {
		// personConfig is needed by maven unit test
		PersonConfig personConfig = simulationConfig.getPersonConfig();
		
		// Compute the average height for all
		tall = personConfig.getTallAverageHeight();
		shortH = personConfig.getShortAverageHeight();
		averageHeight = (tall + shortH) / 2D;
		// Compute the average weight for all
		highW = personConfig.getHighAverageWeight();
		lowW = personConfig.getLowAverageWeight();
		averageWeight = (highW + lowW) / 2D;
	}

	/**
	 * Constructor 0 : used by LoadVehicleTest and other maven test suites
	 * 
	 * @param settlement
	 */
	public Person(Settlement settlement) {
		super("Mock Person", settlement.getCoordinates());
		this.xLoc = 0D;
		this.yLoc = 0D;
		this.associatedSettlementID = settlement.getIdentifier();
		super.setDescription(EARTHLING);
		
		// Put person in settlement
		settlement.getInventory().storeUnit(this);
		// Add this person as a citizen
		settlement.addACitizen(this);

		// reloading from a saved sim
		BuildingManager.addToRandomBuilding(this, associatedSettlementID);
		attributes = new PersonAttributeManager();
	}
	
	/**
	 * Constructor 1 : used by PersonBuilderImpl Creates a Person object at a given
	 * settlement.
	 *
	 * @param name       the person's name
	 * @param settlement {@link Settlement} the settlement the person is at
	 * @throws Exception if no inhabitable building available at settlement.
	 */
	public Person(String name, Settlement settlement) {
		super(name, settlement.getCoordinates());
		super.setDescription(EARTHLING);


		// Store this person in the settlement
		settlement.getInventory().storeUnit(this);
		// Add this person as a citizen
		settlement.addACitizen(this);
		
		// Initialize data members
		super.setName(name);
		firstName = name.substring(0, name.indexOf(ONE_SPACE));
		lastName = name.substring(name.indexOf(ONE_SPACE) + 1, name.length());
		this.xLoc = 0D;
		this.yLoc = 0D;
		this.associatedSettlementID = settlement.getIdentifier();
		
		// create a prior training profile
		generatePriorTraining();
		attributes = new PersonAttributeManager();
		// Construct the SkillManager instance
		skillManager = new SkillManager(this);
		// Construct the Mind instance
		mind = new Mind(this);
		// Set the person's status of death
		isBuried = false;
	}

	/*
	 * Uses static factory method to create an instance of PersonBuilder
	 *
	 * @param name
	 * @param settlement
	 * @return
	 */
	public static PersonBuilder<?> create(String name, Settlement settlement) {
		return new PersonBuilderImpl(name, settlement);
	}

	/**
	 * Initialize field data and class 
	 */
	public void initialize() {
		// WARNING: setAssociatedSettlement(settlement) will cause suffocation when
		// reloading from a saved sim
		BuildingManager.addToRandomBuilding(this, associatedSettlementID);	
		// Set up the time stamp for the person
		calculateBirthDate(earthClock);
		// Create favorites
		favorite = new Favorite(this);
		// Create preferences
		preference = new Preference(this);
		// Set up genetic make-up. Notes it requires attributes.
		setupChromosomeMap();
		// Create ciracdian clock
		circadian = new CircadianClock(this);
		// Create physical condition
		condition = new PhysicalCondition(this);
		// Create job history 		
		jobHistory = new JobHistory();
		// Create the role
		role = new Role(this);
		// Create task schedule
		taskSchedule = new TaskSchedule(this);
		// Set up life support type
		support = getLifeSupportType();
		// Create the mission experiences map
		missionExperiences = new ConcurrentHashMap<>();
		// Create the EVA hours map
		eVATaskTime = new SolMetricDataLogger<String>(MAX_NUM_SOLS);
		// Create the consumption map
		consumption = new SolMetricDataLogger<Integer>(MAX_NUM_SOLS);
		// Asssume the person is not a preconfigured crew member
		preConfigured = false;
		
		collabStudies = new HashSet<>();
	}

	/**
	 * Compute a person's chromosome map
	 */
	public void setupChromosomeMap() {
		paternal_chromosome = new ConcurrentHashMap<>();
		maternal_chromosome = new ConcurrentHashMap<>();

		if (bornOnMars) {
			// Note: figure out how to account for growing characteristics 
			// such as height and weight
			// and define various traits get passed on from parents
		} else {
			// Biochemistry: id 0 - 19
			setupBloodType();
			// Physical Characteristics: id 20 - 39
			// Set up Height
			setupHeight();
			// Set up Weight
			setupWeight();
			// Set up personality traits: id 40 - 59
			setupAttributeTrait();
		}
	}

	/**
	 * Compute a person's attributes and its chromosome
	 */
	public void setupAttributeTrait() {
		// Note: set up a set of genes that was passed onto this person 
		// from two hypothetical parents
		int ID = 40;
		boolean dominant = false;

		int strength = attributes.getAttribute(NaturalAttributeType.STRENGTH);
		int endurance = attributes.getAttribute(NaturalAttributeType.ENDURANCE);
		double gym = 2D * getPreference().getPreferenceScore(new WorkoutMeta());
		if (getFavorite().getFavoriteActivity() == FavoriteType.FIELD_WORK)
			gym += RandomUtil.getRandomRegressionInteger(20);
		else if (getFavorite().getFavoriteActivity() == FavoriteType.SPORT)
			gym += RandomUtil.getRandomRegressionInteger(10);

		PersonConfig personConfig = SimulationConfig.instance().getPersonConfig();
		int baseCap = (int)personConfig.getBaseCapacity();
		int load = 0;
		if (age > 4 && age < 8)
			load = age;
		else if (age > 7 && age <= 12)
			load = age * 2;
		else if (age > 11 && age <= 14)
			load = (int)(baseCap/3 + age * 2);
		else if (age > 14 && age <= 18)
			load = (int)(baseCap/2.5 + age * 1.5);
		else if (age > 18 && age <= 25)
			load = (int)(baseCap/2 + 25 - age / 7.5);
		else if (age > 25 && age <= 35)
			load = (int)(baseCap + 30 - age / 12.5);
		else if (age > 35 && age <= 45)
			load = (int)(baseCap + 25 - age / 10);
		else if (age > 45 && age <= 55)
			load = (int)(baseCap/1.25 + 20 - age / 7.5);
		else if (age > 55 && age <= 65)
			load = (int)(baseCap/1.5 + 15 - age / 6);
		else if (age > 65 && age <= 70)
			load = (int)(baseCap/1.75 + 10 - age / 5);
		else if (age > 70 && age <= 75)
			load = (int)(baseCap/2 - age / 4);
		else if (age > 75 && age <= 80)
			load = (int)(baseCap/3 - age / 4);
		else 
			load = (int)(baseCap/4 - age / 4);
		
		int carryCap = Math.max(2, (int)(gym + load + weight/6.0 + (strength - 50)/1.5 + (endurance - 50)/2.0 
				+ RandomUtil.getRandomRegressionInteger(10)));
		
		// Set inventory total mass capacity based on the person's weight and strength.
		getInventory().addGeneralCapacity(carryCap); 
		
//		logger.info(name + " (" + weight + " kg) with strength " + strength 
//				+ " & endurance " + endurance  
//				+ " can carry " + carryCap + " kg");
//		System.out.println(getName() + " age: " + age);
//		System.out.println(getName() + " load: " + load + " kg.");
//		System.out.println(getName() + " can carry " + carryCap + " kg.");
				
		// Calculate the walking speed modifier
		caculateWalkSpeedMod();

		int score = mind.getMBTI().getIntrovertExtrovertScore();

		Gene trait1_G = new Gene(this, ID, "Trait 1", true, dominant, "Introvert", score);
		paternal_chromosome.put(ID, trait1_G);

	}

	/**
	 * Compute a person's blood type and its chromosome
	 */
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

	public String getBloodType() {
		return bloodType;
	}
	
	/**
	 * Compute a person's height and its chromosome
	 */
	public void setupHeight() {
		int ID = 20;
		boolean dominant = false;

		// For a 20-year-old in the US:
		// male : height : 176.5 weight : 68.5
		// female : height : 162.6 weight : 57.2

		// Note: factor in country of origin.
		// Note: look for a gender-correlated curve

		// Note: p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Attempt to compute height with gaussian curve

		double dad_height = tall + RandomUtil.getGaussianDouble() * tall / 7D;// RandomUtil.getRandomInt(22);
		double mom_height = shortH + RandomUtil.getGaussianDouble() * shortH / 10D;// RandomUtil.getRandomInt(15);

		Gene dad_height_G = new Gene(this, ID, HEIGHT_GENE, true, dominant, null, dad_height);
		paternal_chromosome.put(ID, dad_height_G);

		Gene mom_height_G = new Gene(this, ID, HEIGHT_GENE, false, dominant, null, mom_height);
		maternal_chromosome.put(ID, mom_height_G);

		double genetic_factor = .65;
		double sex_factor = (tall - averageHeight) / averageHeight;
		// Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			height = Math.round(
					(genetic_factor * dad_height + (1 - genetic_factor) * mom_height * (1 + sex_factor)) * 100D) / 100D;
		else
			height = Math.round(
					((1 - genetic_factor) * dad_height + genetic_factor * mom_height * (1 - sex_factor)) * 100D) / 100D;

	}

	/**
	 * Compute a person's weight and its chromosome
	 */
	public void setupWeight() {
		int ID = 21;
		boolean dominant = false;

		// For a 20-year-old in the US:
		// male : height : 176.5 weight : 68.5
		// female : height : 162.6 weight : 57.2

		// Note: factor in country of origin.
		// Note: look for a gender-correlated curve

		// Note: p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Attempt to compute height with gaussian curve
		double dad_weight = highW + RandomUtil.getGaussianDouble() * highW / 13.5;// RandomUtil.getRandomInt(10);
		double mom_weight = lowW + RandomUtil.getGaussianDouble() * lowW / 10.5;// RandomUtil.getRandomInt(15);

		Gene dad_weight_G = new Gene(this, ID, WEIGHT_GENE, true, dominant, null, dad_weight);
		paternal_chromosome.put(ID, dad_weight_G);

		Gene mom_weight_G = new Gene(this, ID, WEIGHT_GENE, false, dominant, null, mom_weight);
		maternal_chromosome.put(ID, mom_weight_G);

		double genetic_factor = .65;
		double sex_factor = (highW - averageWeight) / averageWeight; // for male
		double height_factor = height / averageHeight;

		// Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			weight = Math.round(height_factor
					* (genetic_factor * dad_weight + (1 - genetic_factor) * mom_weight * (1 + sex_factor)) * 100D)
					/ 100D;
		else
			weight = Math.round(height_factor
					* ((1 - genetic_factor) * dad_weight + genetic_factor * mom_weight * (1 - sex_factor)) * 100D)
					/ 100D;

		setBaseMass(weight);

	}

	/*
	 * Sets sponsoring agency for the person
	 */
	public void setSponsor(ReportingAuthority sponsor) {
		ra = sponsor;
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
	 * 
	 * @param type {@link RoleType}
	 */
	public void setRole(RoleType type) {
		getRole().changeRoleType(type);

		// In case of the role of the Mayor, his job must be set to Politician instead.
		if (type == RoleType.MAYOR) {
			// Set the job as Politician
			mind.assignJob(JobType.POLITICIAN, true, JobUtil.SETTLEMENT, JobAssignmentType.APPROVED, JobUtil.SETTLEMENT);
		}
	}

	/**
	 * Sets the job of a person
	 * 
	 * @param job JobType
	 * @param authority
	 */
	public void setJob(JobType job, String authority) {
		mind.assignJob(job, true, JobUtil.SETTLEMENT, JobAssignmentType.APPROVED, authority);
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
	 * @param clock 
	 *
	 */
	private void calculateBirthDate(EarthClock clock) {
		// Set a birth time for the person
		if (age != -1) {
			year = EarthClock.getCurrentYear(earthClock) - age - 1;
		}
		else {
			year = EarthClock.getCurrentYear(earthClock) - RandomUtil.getRandomInt(21, 65);
		}

		month = RandomUtil.getRandomInt(11) + 1;

		if (month == 2) {
			if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
				day = RandomUtil.getRandomInt(28) + 1;
			} else {
				day = RandomUtil.getRandomInt(27) + 1;
			}
		}

		else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			day = RandomUtil.getRandomInt(30) + 1;
		} else {
			day = RandomUtil.getRandomInt(29) + 1;
		}

		// Note: find out why sometimes day = 0 as seen on
		if (day == 0) {
			logger.warning(this, "Date of birth is on the day 0th. Incrementing to the 1st.");
			day = 1;
		}

		// Calculate the year
		// Set the age
		age = updateAge(clock);
	}

	/**
	 * Is the person outside of a settlement but within its vicinity
	 * 
	 * @return true if the person is just right outside of a settlement
	 */
	public boolean isRightOutsideSettlement() {
		return LocationStateType.WITHIN_SETTLEMENT_VICINITY == currentStateType || isBuried;
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
	 * @param xLocation the X distance (meters) from the settlement's center.
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
	 * Gets the person's Z location at a settlement.
	 *
	 * @return Z distance (meters) from the settlement's center.
	 */
	public double getZLocation() {
		return zLoc;
	}

	/**
	 * Sets the person's Z location at a settlement.
	 *
	 * @param zLocation the Z distance (meters) from the settlement's center.
	 */
	public void setZLocation(double zLocation) {
		this.zLoc = zLocation;
	}
	
	/**
	 * Get the settlement in vicinity. This is used assume the person is not at a settlement
	 *
	 * @return the person's settlement
	 */
	public Settlement getNearbySettlement() {	
		return CollectionUtils.findSettlement(getCoordinates());
	}
	
	/**
	 * Get the settlement the person is at.
	 * Returns null if person is not at a settlement.
	 *
	 * @return the person's settlement
	 */
	public Settlement getSettlement() {
//		System.out.println("Person: getContainerID() is " + getContainerID());
		
		if (getContainerID() == Unit.MARS_SURFACE_UNIT_ID)
			return null;
//		
//		else if (vehicle == 0)
//			return null;
//
//		else
//			return unitManager.getSettlementByID(getContainerID());

		Unit c = getContainerUnit();

		if (c instanceof Settlement) {
			return (Settlement) c;
		}

		else if (c instanceof EVASuit || c instanceof Person || c instanceof Robot) {
			return c.getSettlement();
		}
		
		else if (c instanceof Vehicle) {
			Building b = BuildingManager.getBuilding((Vehicle) c);
			if (b != null)
				// still inside the garage
				return b.getSettlement();
		}
		return null;
	}

	/**
	 * Bury the Person at the current location. This happens only if the person can
	 * be retrieved from any containing Settlements or Vehicles found. The body is
	 * fixed at the last location of the containing unit.
	 */
	public void buryBody() {
		// Remove the person from the settlement
//		getContainerUnit().getInventory().retrieveUnit(this);
		// Bury the body
		isBuried = true;
		// Back up the last container unit
//		condition.getDeathDetails().backupContainerUnit(containerUnit);
		condition.getDeathDetails().backupContainerID(getContainerID());
		// set container unit to null if not done so
		setContainerUnit(null);
		// Set his/her currentStateType
		currentStateType = LocationStateType.WITHIN_SETTLEMENT_VICINITY;  
		// Set his/her buried settlement
		setBuriedSettlement(associatedSettlementID);
		// Remove the person from being a member of the associated settlement
		setAssociatedSettlement(-1);
		// Throw unit event.
		fireUnitUpdate(UnitEventType.BURIAL_EVENT);
	}

	protected void setDescription(String s) {
		super.setDescription(s);
	}

	/**
	 * Declares the person dead 
	 */
	void setDeclaredDead() {
		declaredDead = true;
	}
	
	/**
	 * Deregisters the person's quarters
	 */
	void deregisterBed() {
		// Set quarters to null
		if (quartersInt != -1) {
			Map<Integer, Point2D>  map = unitManager.getBuildingByID(quartersInt).getLivingAccommodations().getAssignedBeds();
			if (map.containsKey(getIdentifier()))
				map.remove(getIdentifier());
			quartersInt = -1;
		}
		// Empty the bed
		if (bed != null)
			bed = null;
	}

	/**
	 * Person can take action with time passing
	 *
	 * @param pulse amount of time passing (in millisols).
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!isValid(pulse)) {
			return false;
		}
		
		// Primary researcher; my responsiblity to update Study
		if (study != null) {
			study.timePassing(pulse);
		}
		
		if (!condition.isDead()) {
			// Mental changes with time passing.
			mind.timePassing(pulse);
		}
			
		// If Person is dead, then skip
		if (!condition.isDead() && getLifeSupportType() != null) {// health.getDeathDetails() == null) {

			support = getLifeSupportType();

			circadian.timePassing(pulse, support);
			// Pass the time in the physical condition first as this may result in death.
			condition.timePassing(pulse, support);

			if (!condition.isDead()) {

				if (pulse.isNewSol()) {
					// Update the solCache
					int currentSol = pulse.getMarsTime().getMissionSol();
					
					if (currentSol == 1) {
						// On the first mission sol,
						// adjust the sleep habit according to the current work shift
						for (int i=0; i< 15; i++) {
							int shiftEnd = getTaskSchedule().getShiftEnd();
							int m = shiftEnd - 20 * (i+1);
							if (m < 0)
								m = m + 1000;
							// suppress sleep during the work shift
							circadian.updateSleepCycle(m, false);
							
							m = shiftEnd + 10 * (i+1);
							if (m > 1000)
								m = m - 1000;
							// encourage sleep after the work shift
							circadian.updateSleepCycle(m, true);
						}
						
						if (getShiftType() == ShiftType.B) {
							condition.setFatigue(getFatigue() + RandomUtil.getRandomInt(500));
						}
						else if (getShiftType() == ShiftType.Y) {
							condition.setFatigue(getFatigue() + RandomUtil.getRandomInt(333));
						}
						else if (getShiftType() == ShiftType.Z) {
							condition.setFatigue(getFatigue() + RandomUtil.getRandomInt(667));
						}
							
					}
					else {
						// Adjust the sleep habit according to the current work shift
						for (int i=0; i< 5; i++) {
							int m = getTaskSchedule().getShiftEnd() + 10 * (i+1);
							if (m > 1000)
								m = m - 1000;
							circadian.updateSleepCycle(m, true);
						}
						
						// Check if a person's age should be updated
						age = updateAge(pulse.getEarthTime());					
						
						// Checks if a person has a role
						if (role.getType() == null)
							role.obtainNewRole();

						if (currentSol % 3 == 0) {
							// Adjust the shiftChoice once every 3 sols based on sleep hour
							int bestSleepTime[] = getPreferredSleepHours();
							taskSchedule.adjustShiftChoice(bestSleepTime);
						}

						if (currentSol % 4 == 0) {
							// Increment the shiftChoice once every 4 sols
							taskSchedule.incrementShiftChoice();
						}

						if (currentSol % 7 == 0) {
							// Normalize the shiftChoice once every week
							taskSchedule.normalizeShiftChoice();
						}
					}
				}
			}
		}

		else if (!isBuried && condition.getDeathDetails() != null
				&& condition.getDeathDetails().getBodyRetrieved()) {

			if (!declaredDead) {
				// Declares the person dead
				setDeclaredDead();
				// Deregisters the person's quarters
				deregisterBed();
				// Deactivates the person's mind 
				mind.setInactive();
			}
		}
		return true;
	}

	/**
	 * Returns a reference to the Person's natural attribute manager
	 *
	 * @return the person's natural attribute manager
	 */
	@Override
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
				found = (MedicalAid) foundBuilding.getMedical();
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

	@Override
	public TaskManager getTaskManager() {
		return mind.getTaskManager();
	}
	

	/**
	 * Updates and returns the person's age
	 *
	 * @return the person's age
	 */
	private int updateAge(EarthClock clock) {
		int newage = clock.getYear() - year - 1;
		if (clock.getMonth() >= month)
			if (clock.getDayOfMonth() >= day)
				newage++;
		age = newage;
		return age;
	}

	/**
	 * Set a person's age and update one's year of birth
	 *
	 * @param newAge
	 */
	public void changeAge(int newAge) {
		// Back calculate a person's year
		int y = earthClock.getYear() - newAge - 1;
		// Set year to newYear
		year = y;
		age = newAge;
	}
	
	/**
	 * Returns the person's birth date in the format of "2055-05-06"
	 *
	 * @return the person's birth date
	 */
	public String getBirthDate() {
		StringBuilder s = new StringBuilder();
		s.append(year).append("-");
		if (month < 10)
			s.append("0").append(month).append("-");
		else
			s.append(month).append("-");
		if (day < 10)
			s.append("0").append(day);
		else
			s.append(day);

		return s.toString();
	}

	/**
	 * Get the LifeSupport system supporting this Person. This may be from the
	 * Settlement, Vehicle or Equipment.
	 *
	 * @return Life support system.
	 */
	private LifeSupportInterface getLifeSupportType() {

		LifeSupportInterface result = null;
		List<LifeSupportInterface> lifeSupportUnits = new CopyOnWriteArrayList<LifeSupportInterface>();

		Settlement settlement = getSettlement();
		if (settlement != null) {
			lifeSupportUnits.add(settlement);
		}

		else {
			
			Vehicle vehicle = getVehicle();
			if ((vehicle != null) && (vehicle instanceof LifeSupportInterface)) {

				if (vehicle.isInVehicleInGarage()) { //BuildingManager.getBuilding(vehicle) != null) {
					// if the vehicle is inside a garage
					lifeSupportUnits.add(vehicle.getSettlement());
				}

				else {
					lifeSupportUnits.add((LifeSupportInterface) vehicle);
				}
			}
		}

		// Get all contained units.
		Collection<Integer> IDs = getInventory().getContainedUnitIDs();
		for (Integer id : IDs) {
			Unit u = unitManager.getUnitByID(id);
			if (u instanceof LifeSupportInterface)
				lifeSupportUnits.add((LifeSupportInterface) u);
		}

		// If more than one find the best
		if (lifeSupportUnits.size() > 1) {
			for (LifeSupportInterface goodUnit : lifeSupportUnits) {
				if (result == null && goodUnit.lifeSupportCheck()) {
					result = goodUnit;
				}
			}
		}

		// If no good units, just get first life support unit.
		if ((result == null) && (lifeSupportUnits.size() > 0)) {
			result = lifeSupportUnits.get(0);
		}

//		System.out.println(name + " in " + getLocationTag().getImmediateLocation() + " is on " + result.toString() + " life support.");
		return result;
	}

	/**
	 * Gets the gender of the person.
	 *
	 * @return the gender
	 */
	public GenderType getGender() {
		return gender;
	}

	/**
	 * Sets the gender of the person.
	 *
	 * @param gender the GenderType
	 */
	public void setGender(GenderType gender) {
		this.gender = gender;
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
	 * Checks if the person is physically fit.
	 *
	 * @return true if the person is fit.
	 */
	public boolean isFit() {
		return condition.isFit();
	}

	/**
	 * Checks if the person is sick.
	 *
	 * @return true if the person is sick.
	 */
	public boolean isSick() {
		return condition.hasSeriousMedicalProblems();
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
	 * Sets the person's name
	 * 
	 * @param newName the new name
	 */
	public void setName(String newName) {
		if (!getName().equals(newName)) {
			logger.config(this, "The Mission Control renamed to '" + newName + "'.");
			firstName = newName.substring(0, newName.indexOf(" "));
			lastName = newName.substring(newName.indexOf(" ") + 1, newName.length());	
			super.setName(newName);
			super.setDescription(EARTHLING);
		}
	}

	/**
	 * Gets the settlement the person is currently associated with.
	 *
	 * @return associated settlement or null if none.
	 */
	public Settlement getAssociatedSettlement() {
		return unitManager.getSettlementByID(associatedSettlementID);
	}

	/**
	 * Sets the associated settlement for a person.
	 *
	 * @param newSettlement the new associated settlement or null if none.
	 */
	public void setAssociatedSettlement(int newSettlement) {

		if (associatedSettlementID != newSettlement) {

			int oldSettlement = associatedSettlementID;
			associatedSettlementID = newSettlement;

			if (oldSettlement != -1) {
				unitManager.getSettlementByID(oldSettlement).removeACitizen(this);
			}

			if (newSettlement != -1) {
				unitManager.getSettlementByID(newSettlement).addACitizen(this);
			}
		}
	}

	/**
	 * Sets the associated settlement for a person.
	 *
	 * @param settlement
	 */
	public void setBuriedSettlement(int settlement) {
		buriedSettlement = settlement;
	}

	public Settlement getBuriedSettlement() {
		return unitManager.getSettlementByID(buriedSettlement);
	}


	/**
	 * Set the study that this Person is the lead on.
	 * @param scientificStudy
	 */
	public void setStudy(ScientificStudy scientificStudy) {
		this.study = scientificStudy;
	}
	
	public ScientificStudy getStudy() {
		return study;
	}
	
	public Set<ScientificStudy> getCollabStudies() {
		return collabStudies;
	}
	
	public void addCollabStudy(ScientificStudy study) {
		this.collabStudies.add(study);
	}

	public void removeCollabStudy(ScientificStudy study) {
		this.collabStudies.remove(study);
	}
	
	/**
	 * Gets the person's achievement credit for a given scientific field.
	 *
	 * @param science the scientific field.
	 * @return achievement credit.
	 */
	public double getScientificAchievement(ScienceType science) {
		double result = 0D;
		if (science == null)
			return result;
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
	 * @param achievementCredit the achievement credit.
	 * @param science           the scientific field.
	 */
	public void addScientificAchievement(double achievementCredit, ScienceType science) {
		if (scientificAchievement.containsKey(science)) {
			achievementCredit += scientificAchievement.get(science);
		}
		scientificAchievement.put(science, achievementCredit);
//		System.out.println(" Person : " + this + " " + science + " " + achievementCredit);
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
	@Override
	public Building getBuildingLocation() {
		return computeCurrentBuilding();
	}

	
	/**
	 * Is this person at this building type ?
	 * 
	 * @param type
	 * @return
	 */
	public boolean isPersonAtBuilding(String type) {
    	Building b = getBuildingLocation();
    	
    	if (b != null) {
            return b.getBuildingType().equalsIgnoreCase(type);
    	}
    	
    	return false;
    }
	
	/**
	 * Checks if the adjacent building is the type of interest
	 * 
	 * @param type
	 * @return
	 */
	public boolean isAdjacentBuildingType(String type) {	
		if (getSettlement() != null) {
			Building b = computeCurrentBuilding();
			
			List<Building> list = getSettlement().createAdjacentBuildings(b);
			for (Building bb : list) {
				if (bb.getBuildingType().equals(type))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Computes the building the person is currently located at Returns null if
	 * outside of a settlement
	 *
	 * @return building
	 */
	public Building computeCurrentBuilding() {
//		if (currentBuilding != null) {
//			return currentBuilding;
//		} else if (getLocationStateType() == LocationStateType.INSIDE_SETTLEMENT) {//isInSettlement()) {
//			currentBuilding = getSettlement().getBuildingManager().getBuildingAtPosition(getXLocation(),
//					getYLocation());
//		}
		if (currentBuildingInt == -1)
			return null;
		return unitManager.getBuildingByID(currentBuildingInt);
	}

	/**
	 * Computes the building the person is currently located at Returns null if
	 * outside of a settlement
	 *
	 * @return building
	 */
	public void setCurrentBuilding(Building building) {
		if (building == null) {
			currentBuildingInt = -1;
		}
		
		else {
			currentBuildingInt = building.getIdentifier();
		}		
	}

	/**
	 * Computes the building the person is currently located at Returns null if
	 * outside of a settlement
	 *
	 * @param building
	 */
	public void setCurrentMockBuilding(Building building) {
		if (building == null) {
			currentBuildingInt = -1;
		}
		else {
			currentBuildingInt = building.getIdentifier();
		}
	}
	
	/**
	 * Obtains the immediate location (either building, vehicle, a settlement's
	 * vicinity or outside on Mars)
	 * 
	 * @return the name string of the location the unit is at
	 */
	public String getImmediateLocation() {
		return getLocationTag().getImmediateLocation();
	}

	/**
	 * Obtains the modified immediate location 
	 * 
	 * @return the name string of the location the unit is at
	 */
	public String getModifiedLoc() {
		return getLocationTag().getModifiedLoc();
	}
	
	public String getLocale() {
		return getLocationTag().getLocale();
	}
	
	public String getExtendedLocations() {
		return getLocationTag().getExtendedLocation();
	}
	
	public Settlement findSettlementVicinity() {
		return getLocationTag().findSettlementVicinity();
	}
	
	@Override
	public String getTaskDescription() {
		return getMind().getTaskManager().getTaskDescription(false);
	}

	public String getTaskPhase() {
		if (getMind().getTaskManager().getPhase() != null)
		return getMind().getTaskManager().getPhase().getName();
		
		return "";
	}
	
	@Override
	public Mission getMission() {
		return getMind().getMission();
	}
	
	
	@Override
	public void setMission(Mission newMission) {
		getMind().setMission(newMission);
	}

	//@Override
	public void setShiftType(ShiftType shiftType) {
		taskSchedule.setShiftType(shiftType);
	}

	public ShiftType getShiftType() {
		return taskSchedule.getShiftType();
	}

	public double getFatigue() {
		return condition.getFatigue();
	}

	public double getEnergy() {
		return condition.getEnergy();
	}
	
	public double getHunger() {
		return condition.getHunger();
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

	public Building getQuarters() {
		return unitManager.getBuildingByID(quartersInt);
	}

	public void setQuarters(Building b) {
		this.quartersInt = b.getIdentifier();
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
			birthplace = EARTH_BIRTHPLACE;
		else
			birthplace = MARS_BIRTHPLACE;
	}

	public boolean isDeclaredDead() {
		return declaredDead;
	}

//	public String getPlaceOfDeath() {
//		if (condition.isDead() && condition.getDeathDetails() != null) {
//				return condition.getDeathDetails().getPlaceOfDeath();
//		}
//		return "Unknown";
//	}
	
	public boolean isBuried() {
		return isBuried;
	}

	//@Override
	public void setVehicle(Vehicle vehicle) {
//		this.vehicleInt = vehicle.getIdentifier();
	}
	
	/**
	 * Get vehicle person is in, null if person is not in vehicle
	 * 
	 * @return the person's vehicle
	 */
	public Vehicle getVehicle() {
		if (getLocationStateType() == LocationStateType.INSIDE_VEHICLE) {
			Vehicle v = (Vehicle) getContainerUnit();
//			setVehicle(v);
			return v;
		}

		return null;
	}

	public CircadianClock getCircadianClock() {
		return circadian;
	}
	
	/**
	 * Gets the first name of the person
	 * 
	 * @return the first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Gets the last name of the person
	 * 
	 * @return the last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Return the mission description if a person is on a mission
	 * 
	 * @return description
	 */
	public String getMissionDescription() {
		Mission m = null;
		if (mind.getMission() != null) {
			m = mind.getMission();
			return m.getDescription();
		} else {
			return "None";
		}
	}

	/**
	 * Adds the mission experience score
	 * 
	 * @param id
	 * @param score
	 */
	public void addMissionExperience(int id, double score) {
		if (missionExperiences.containsKey(id)) {
			List<Double> scores = missionExperiences.get(id);
			scores.add(score);
//			missionExperiences.get(id).add(score);
//			// Limit the size of each list to 20
//			if (scores.size() > 20)
//				scores.remove(0);
		} else {
			List<Double> scores = new CopyOnWriteArrayList<>();
			scores.add(score);
			missionExperiences.put(id, scores);
		}
	}


	/**
	 * Gets the mission experiences map
	 * 
	 * @return a map of mission experiences
	 */
	public Map<Integer, List<Double>> getMissionExperiences() {
		return missionExperiences;
	}

	/**
	 * Adds the EVA time
	 * 
	 * @param taskName
	 * @param time
	 */
	public void addEVATime(String taskName, double time) {
		eVATaskTime.increaseDataPoint(taskName, time);
	}

	/**
	 * Gets the map of EVA task time.
	 * 
	 * @return a map of EVA time by sol
	 */
	public Map<Integer, Double> getTotalEVATaskTimeBySol() {
		Map<Integer, Double> map = new ConcurrentHashMap<>();
		Map<Integer, Map<String, Double>> history = eVATaskTime.getHistory();
		for (Entry<Integer, Map<String, Double>> day : history.entrySet()) {
			double sum = 0;
			int sol = day.getKey();
			for (Double t : day.getValue().values()) {
				sum += t;
			}

			map.put(sol, sum);
		}

		return map;
	}

	/**
	 * Adds the amount consumed.
	 * 
	 * @param waterID
	 * @param amount
	 */
	public void addConsumptionTime(int waterID, double amount) {
		consumption.increaseDataPoint(waterID, amount);
	}

	/**
	 * Gets the daily average water usage of the last x sols Not: most weight on
	 * yesterday's usage. Least weight on usage from x sols ago
	 *
	 * @param type the id of the resource
	 * @return the amount of resource consumed in a day
	 */
	public double getDailyUsage(Integer type) {
		return consumption.getDailyAverage(type);
	}

	public double getEatingSpeed() {
		return eatingSpeed;
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
	 * Gets the average height of a person.
	 */
	public static double getAverageHeight() {
		return averageHeight;
	}

	/**
	 * Gets the average weight of a person.
	 */
	public static double getAverageWeight() {
		return averageWeight;
	}

	/**
	 * Gets the age of this person.
	 * 
	 * @return
	 */
	public int getAge() {
		return age;
	}

	/**
	 * Sets the age of this person.
	 * 
	 * @param value
	 */
	public void setAge(int value) {
		age = value;
	}

	
	/**
	 * Checks if the person is a preconfigured crew member.
	 */
	public boolean isPreConfigured() {
		return preConfigured;
	}

	/**
	 * Set the person as a preconfigured crew member.
	 */
	public void setPreConfigured(boolean value) {
		preConfigured = value;
	}

	/**
	 * Returns a reference to the Person's skill manager
	 * 
	 * @return the person's skill manager
	 */
	@Override
	public SkillManager getSkillManager() {
		return skillManager;
	}

	/**
	 * Returns the effective integer skill level from a named skill based on
	 * additional modifiers such as fatigue.
	 * 
	 * @param skillType the skill's type
	 * @return the skill's effective level
	 */
	public int getEffectiveSkillLevel(SkillType skillType) {
		// Modify for fatigue, minus 1 skill level for every 1000 points of fatigue.
		return (int) Math.round(getPerformanceRating() * skillManager.getSkillLevel(skillType));
	}


	/**
	 * Randomly generate a list of training the person may have attended
	 */
	public void generatePriorTraining() {
		if (trainings == null) {
			trainings = new CopyOnWriteArrayList<>();
			List<TrainingType> lists = new CopyOnWriteArrayList<>(Arrays.asList(TrainingType.values()));
			int size = lists.size();
			int num = RandomUtil.getRandomRegressionInteger(4);
			// Guarantee at least one training
			if (num == 0) num = 1;
			for (int i= 0 ; i < num; i++) {
				size = lists.size();
				int rand = RandomUtil.getRandomInt(size-1);
				TrainingType t = lists.get(rand);
				trainings.add(t);
				lists.remove(t);
			}
		}
	}
	
	/**
	 * Gets a list of prior training
	 * 
	 * @return {@link List<TrainingType>}
	 */
	public List<TrainingType> getTrainings() {
		return trainings;
	}
	
	public double getPilotingMod() {
		double mod = 0;
		if (trainings.contains(TrainingType.AVIATION_CERTIFICATION))
			mod += .2;
		if (trainings.contains(TrainingType.FLIGHT_SAFETY))
			mod += .25;
		if (trainings.contains(TrainingType.NASA_DESERT_RATS))
			mod += .15;
		
		return mod;
	}
	
	/**
	 * Registers a particular EVA suit to the person
	 * 
	 * @param suit the EVA suit
	 */
	public void registerSuit(EVASuit suit) {
		this.suit = suit;
	}

	/**
	 * Gets the EVA suit the person has donned on
	 * @return
	 */
	public EVASuit getSuit() {
		return suit;
	}
	
	public int getExtrovertScore() {
		if (extrovertScore == -1) {
			int score = mind.getTraitManager().getIntrovertExtrovertScore();
			extrovertScore = score;
			
			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
			return score;
		}
		
		return extrovertScore;
	}
	
	public int getExtrovertmodifier() {
		return (int)((getExtrovertScore() - 50) / 25D);
	}
	
	/**
	 * Calculate the modifier for walking speed based on how much this unit is carrying
	 */
	public void caculateWalkSpeedMod() {
		double mass = getInventory().getTotalInventoryMass(false);
		double cap = getInventory().getGeneralCapacity();
		// At full capacity, may still move at 10%.
		// Make sure is doesn't go -ve and there is always some movement
		walkSpeedMod = 1.1 - Math.min(mass/Math.max(cap, SMALL_AMOUNT), 1D);
	}
	
	public double getWalkSpeedMod() {
		return walkSpeedMod;
	}
	
	
	@Override
	protected UnitType getUnitType() {
		return UnitType.PERSON;
	}

	/**
	 * Reinitialize references after loading from a saved sim
	 */
	public void reinit() {
		mind.reinit();
		condition.reinit();
	}

	// Look to refactor and use the base UNit equals & hashCode
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Person p = (Person) obj;
		return this.getIdentifier() == p.getIdentifier();
//				&& this.firstName.equals(p.getFirstName())
//				&& this.lastName.equals(p.getLastName())
//				&& this.height == p.getHeight()
//				&& this.gender.equals(p.getGender())
//				&& this.age == p.getAge()
//				&& this.getBirthDate() == p.getBirthDate();
	}

	/**
	 * Gets the hash code for this object.
	 * 
	 * @return hash code.
	 */
	public int hashCode() {
		// Hash must be constant and not depend upon changing attributes
		return getIdentifier() % 64;
	}
	
	
	@Override
	public void destroy() {
		super.destroy();
		circadian = null;
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
		// condition.destroy();
		condition = null;
		gender = null;

		skillManager.destroy();
		skillManager = null;

		scientificAchievement.clear();
		scientificAchievement = null;
	}

	/**
	 * Generate a unique name for a person based on a country
	 * @param country
	 * @param gender
	 * @return
	 */
	public static String generateName(String country, GenderType gender) {
		boolean isUniqueName = false;
		PersonConfig personConfig = simulationConfig.getPersonConfig();
		
		// Check for any duplicate full Name
		Collection<Person> people = unitManager.getPeople();
		List<String> existingfullnames = people.stream()
				.map(Person::getName).collect(Collectors.toList());

		// Prevent mars-sim from using the user defined commander's name  
		String userName = personConfig.getCommander().getFullName();
		if (userName != null)
			existingfullnames.add(userName);

		// Setup name ranges
		PersonNameSpec nameSpec = personConfig.getNamesByCountry(country);
		List<String> last_list = nameSpec.getLastNames();
		List<String> first_list = null;
		if (gender == GenderType.MALE) {
			first_list = nameSpec.getMaleNames();
		} else {
			first_list = nameSpec.getFemaleNames();
		}
		
		// Attempt to find a unique combination
		while (!isUniqueName) {
			int rand0 = RandomUtil.getRandomInt(last_list.size() - 1);
			int rand1 = RandomUtil.getRandomInt(first_list.size() - 1);

			String fullname = first_list.get(rand1) + " " + last_list.get(rand0);

			// double checking if this name has already been in use
			if (existingfullnames.contains(fullname)) {
				isUniqueName = false;
				logger.config(fullname + " is a duplicate name. Choose another one.");
			}
			else {
				return fullname;
			}
		}
		
		// Should never get here
		return "Person #" + people.size();
	}
}
