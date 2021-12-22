/*
 * Mars Simulation Project
 * Person.java
 * @date 2021-10-21
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.LocalPosition;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.environment.MarsSurface;
import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.equipment.EVASuit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.equipment.EquipmentInventory;
import org.mars_sim.msp.core.equipment.EquipmentOwner;
import org.mars_sim.msp.core.equipment.EquipmentType;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.PersonAttributeManager;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionMember;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.utils.TaskManager;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The Person class represents a person on Mars. It keeps track of everything
 * related to that person and provides information about him/her.
 */
public class Person extends Unit implements MissionMember, Serializable, Temporal, EquipmentOwner {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Person.class.getName());

	public static final int MAX_NUM_SOLS = 3;
	/** A small amount. */
	private static final double SMALL_AMOUNT = 0.00001;

	private final static String EARTH_BIRTHPLACE = "Earth";
	private final static String MARS_BIRTHPLACE = "Mars";
	private final static String HEIGHT_GENE = "Height";
	private final static String WEIGHT_GENE = "Weight";

	private final static String EARTHLING = "Earthling";

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
	/** The quarters that the person belongs. */
	private int quartersInt = -1;
	/** The current building location of the person. */
	private int currentBuildingInt;
	/** The carrying capacity of the person. */
	private int carryingCapacity;

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
	/** Settlement position (meters) from settlement center. */
	private LocalPosition position;
	/** Settlement Z location (meters) from settlement center. */
	private double zLoc;

	/** The birthplace of the person. */
	private String birthplace;
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
	/** Person's ReportingAuthority instance. */
	private ReportingAuthority ra;
	/** The bed location of the person */
	private LocalPosition bed;
	/** The EVA suit that the person has donned on. */
	private EVASuit suit;
	/** The person's current scientific study. */
	private ScientificStudy study;
	/** The person's EquipmentInventory instance. */
	private EquipmentInventory eqmInventory;

	/** The person's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement = new ConcurrentHashMap<ScienceType, Double>();
	/** The person's paternal chromosome. */
	private Map<Integer, Gene> paternal_chromosome;
	/** The person's maternal chromosome. */
	private Map<Integer, Gene> maternal_chromosome;
	/** The person's mission experiences. */
	private Map<MissionType, Integer> missionExperiences;
	/** The person's list of prior trainings */
	private List<TrainingType> trainings;
	/** The person's list of collaborative scientific studies. */
	private Set<ScientificStudy> collabStudies;

	/** The person's EVA times. */
	private SolMetricDataLogger<String> eVATaskTime;
	/** The person's water/oxygen consumption. */
	private SolMetricDataLogger<Integer> consumption;

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
		this.position = LocalPosition.DEFAULT_POSITION;
		this.associatedSettlementID = settlement.getIdentifier();
		super.setDescription(EARTHLING);

		// Add this person as a citizen
		settlement.addACitizen(this);
		// Set the container unit
		setContainerUnit(settlement);

		// Add to a random building
		BuildingManager.addPersonToRandomBuilding(this, associatedSettlementID);
		// Create PersonAttributeManager instance
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

		// Initialize data members
		this.position = LocalPosition.DEFAULT_POSITION;
		this.associatedSettlementID = settlement.getIdentifier();

		// create a prior training profile
		generatePriorTraining();
		// Construct the PersonAttributeManager instance
		attributes = new PersonAttributeManager();
		// Construct the SkillManager instance
		skillManager = new SkillManager(this);
		// Construct the Mind instance
		mind = new Mind(this);

		// Set the person's status of death
		isBuried = false;

		// Add this person as a citizen
		settlement.addACitizen(this);
		// Set the container unit
		setContainerUnit(settlement);
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
		// WARNING: setAssociatedSettlement(settlement) may cause suffocation 
		// Reloading from a saved sim
		
		// Add to a random building
		BuildingManager.addPersonToRandomBuilding(this, associatedSettlementID);
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
		missionExperiences = new EnumMap<>(MissionType.class);
		// Create the EVA hours map
		eVATaskTime = new SolMetricDataLogger<String>(MAX_NUM_SOLS);
		// Create the consumption map
		consumption = new SolMetricDataLogger<Integer>(MAX_NUM_SOLS);
		// Create a set of collaborative studies
		collabStudies = new HashSet<>();
		// Construct the EquipmentInventory instance.
		eqmInventory = new EquipmentInventory(this, carryingCapacity);
	}

	/**
	 * Compute a person's chromosome map
	 */
	private void setupChromosomeMap() {
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
	private void setupAttributeTrait() {
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
			load = (int)(baseCap/2.0 + 35 - age / 7.5);
		else if (age > 25 && age <= 35)
			load = (int)(baseCap + 30 - age / 12.5);
		else if (age > 35 && age <= 45)
			load = (int)(baseCap + 25 - age / 10);
		else if (age > 45 && age <= 55)
			load = (int)(baseCap + 20 - age / 7.5);
		else if (age > 55 && age <= 65)
			load = (int)(baseCap/1.25 + 15 - age / 6.0);
		else if (age > 65 && age <= 70)
			load = (int)(baseCap/1.5 + 10 - age / 5.0);
		else if (age > 70 && age <= 75)
			load = (int)(baseCap/1.75 - age / 4.0);
		else if (age > 75 && age <= 80)
			load = (int)(baseCap/2.0 - age / 4.0);
		else
			load = (int)(baseCap/2.5 - age / 4.0);

		// Set inventory total mass capacity based on the person's weight and strength.
		carryingCapacity = Math.max(2, (int)(gym + load + Math.max(20, weight/6.0) + (strength - 50)/1.5 + (endurance - 50)/2.0
				+ RandomUtil.getRandomRegressionInteger(10)));

//		logger.info(name + " (" + weight + " kg) with strength " + strength
//				+ " & endurance " + endurance
//				+ " can carry " + carryCap + " kg");
//		System.out.println(getName() + " age: " + age);
//		System.out.println(getName() + " load: " + load + " kg.");
//		System.out.println(getName() + " can carry " + carryCap + " kg.");

		int score = mind.getMBTI().getIntrovertExtrovertScore();

		Gene trait1_G = new Gene(this, ID, "Trait 1", true, dominant, "Introvert", score);
		paternal_chromosome.put(ID, trait1_G);

	}

	/**
	 * Compute a person's blood type and its chromosome
	 */
	private void setupBloodType() {
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
	 * Gets the blood type
	 *
	 * @return
	 */
	public String getBloodType() {
		return bloodType;
	}

	/**
	 * Compute a person's height and its chromosome
	 */
	private void setupHeight() {
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
	private void setupWeight() {
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
	 * Gets the person's position at a settlement.
	 *
	 * @return distance (meters) from the settlement's center.
	 */
	@Override
	public LocalPosition getPosition() {
		return position;
	}

	/**
	 * Sets the person's position at a settlement.
	 *
	 * @param position
	 */
	@Override
	public void setPosition(LocalPosition position) {
		this.position = position;
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
	@Override
	public Settlement getSettlement() {

		if (getContainerID() == Unit.MARS_SURFACE_UNIT_ID)
			return null;

		Unit c = getContainerUnit();

		if (c.getUnitType() == UnitType.SETTLEMENT) {
			return (Settlement) c;
		}

		if (isInVehicleInGarage()) {
			return ((Vehicle)c).getSettlement();
		}

		if (c.getUnitType() == UnitType.PERSON || c.getUnitType() == UnitType.ROBOT) {
			return c.getSettlement();
		}

		return null;
	}

	/**
	 * Bury the Person at the current location. This happens only if the person can
	 * be retrieved from any containing Settlements or Vehicles found. The body is
	 * fixed at the last location of the containing unit.
	 */
	public void buryBody() {
		// Bury the body
		isBuried = true;
		// Back up the last container unit
		condition.getDeathDetails().backupContainerID(getContainerID());
		// set container unit to null if not done so
		setContainerUnit(null);
		// Set his/her currentStateType
		currentStateType = LocationStateType.WITHIN_SETTLEMENT_VICINITY;
		// Set his/her buried settlement
		buriedSettlement = associatedSettlementID;
		// Remove the person from the settlement's registry
		setAssociatedSettlement(-1);
		// Throw unit event.
		fireUnitUpdate(UnitEventType.BURIAL_EVENT);
	}

	/**
	 * Declares the person dead
	 */
	void setDeclaredDead() {
		declaredDead = true;
		setDescription("Dead");
	}

	/**
	 * Deregisters the person's quarters
	 */
	void deregisterBed() {
		// Set quarters to null
		if (quartersInt != -1) {
			unitManager.getBuildingByID(quartersInt).getLivingAccommodations().releaseBed(this);
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

		// Primary researcher; my responsibility to update Study
		if (study != null) {
			study.timePassing(pulse);
		}

		// If I have a suit then record the use
		if (suit != null) {
			suit.timePassing(pulse);
		}

		if (!condition.isDead()) {
			// Mental changes with time passing.
			mind.timePassing(pulse);
		}

		// If Person is dead, then skip
		if (!condition.isDead() && getLifeSupportType() != null) {

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

						double fatigue = condition.getFatigue();
						if (getShiftType() == ShiftType.B) {
							condition.setFatigue(fatigue + RandomUtil.getRandomInt(500));
						}
						else if (getShiftType() == ShiftType.Y) {
							condition.setFatigue(fatigue + RandomUtil.getRandomInt(333));
						}
						else if (getShiftType() == ShiftType.Z) {
							condition.setFatigue(fatigue + RandomUtil.getRandomInt(667));
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
		else
			checkDecease();

		return true;
	}

	/**
	 *
	 * @return
	 */
	public boolean checkDecease() {
		if (!isBuried && condition.getDeathDetails() != null
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

		Settlement settlement = getSettlement();
		if (settlement != null) {
			// if the person is inside
			return settlement;
		}

		Vehicle vehicle = getVehicle();
		if ((vehicle != null) && (vehicle instanceof LifeSupportInterface)) {

			if (vehicle.isInVehicleInGarage()) {
				// Note: if the vehicle is inside a garage
				// continue to use settlement's life support
				return vehicle.getSettlement();
			}

			else {
				return (LifeSupportInterface) vehicle;
			}
		}

		EVASuit suit = getSuit();
		if (suit != null) {
			return suit;
		}

		// Note: in future a person may rely on a portable gas mask
		// for breathing

		return null;
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
	 * @return collection of people in person's location. The collectino incldues the Person
	 */
	public Collection<Person> getLocalGroup() {
		Collection<Person> localGroup = null;

		if (isInSettlement()) {
			Building building = BuildingManager.getBuilding(this);
			if (building != null) {
				if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
					localGroup = building.getLifeSupport().getOccupants();
				}
			}
		} else if (isInVehicle()) {
			localGroup = ((Crewable) getVehicle()).getCrew();
		}

		if (localGroup == null) {
			localGroup = Collections.emptyList();
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
	 * Sets the person's name
	 *
	 * @param newName the new name
	 */
	public void setName(String newName) {
		if (!getName().equals(newName)) {
			logger.config(this, "The Mission Control renamed to '" + newName + "'.");
			super.setName(newName);
			super.setDescription(EARTHLING);
		}
	}

	/**
	 * Gets the settlement the person is currently associated with.
	 *
	 * @return associated settlement or null if none.
	 */
	@Override
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

	/**
	 * Checks if the adjacent building is the type of interest
	 *
	 * @param type
	 * @return
	 */
	public boolean isAdjacentBuildingType(String type) {
		if (getSettlement() != null) {
			Building b = getBuildingLocation();

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
	@Override
	public Building getBuildingLocation() {
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

	public Settlement findSettlementVicinity() {
		return getLocationTag().findSettlementVicinity();
	}

	@Override
	public String getTaskDescription() {
		return getMind().getTaskManager().getTaskDescription(false);
	}

	public boolean isRestingTask() {
		String des = getTaskDescription().toLowerCase();
		// Check if a person is performing low aerobic tasks
    	return (des.contains("eat")
            || des.contains("drink")
            || des.contains("meet")
            || des.contains("relax")
            || des.contains("rest")
            || des.contains("sleep")
            || des.contains("study")
            || des.contains("compil")
            || des.contains("lab")
            || des.contains("connect")
            || des.contains("convers")
            || des.contains("dream")
            || des.contains("listen")
            || des.contains("invit")
            || des.contains("teach"));
    	
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

	public LocalPosition getBed() {
		return bed;
	}

	public void setBed(LocalPosition bed) {
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

	public boolean isBuried() {
		return isBuried;
	}

	/**
	 * Get vehicle person is in, null if person is not in vehicle
	 *
	 * @return the person's vehicle
	 */
	@Override
	public Vehicle getVehicle() {
		if (getLocationStateType() == LocationStateType.INSIDE_VEHICLE) {
			return (Vehicle) getContainerUnit();
		}

		return null;
	}

	public CircadianClock getCircadianClock() {
		return circadian;
	}

	/**
	 * Adds the mission experience score
	 *
	 * @param missionType
	 * @param score
	 */
	public void addMissionExperience(MissionType missionType, int score) {
		Integer total = missionExperiences.getOrDefault(missionType, 0);
		total += score;
		missionExperiences.put(missionType, total);
	}

	/**
	 * Gets the mission experiences map
	 *
	 * @return a map of mission experiences
	 */
	public int getMissionExperience(MissionType missionType) {
		Integer previous = missionExperiences.get(missionType);
		if (previous != null) {
			return previous;
		}
		return 0;
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
	 * Returns a reference to the Person's skill manager
	 *
	 * @return the person's skill manager
	 */
	@Override
	public SkillManager getSkillManager() {
		return skillManager;
	}

	/**
	 * Randomly generate a list of training the person may have attended
	 */
	private void generatePriorTraining() {
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
	 *
	 * @return
	 */
	public EVASuit getSuit() {
		return suit;
	}

	/**
	 * Gets the EVA suit the person has in inventory
	 *
	 * @return
	 */
	public EVASuit getInventorySuit() {
		for (Equipment e: getEquipmentSet()) {
			if (e.getEquipmentType() == EquipmentType.EVA_SUIT)
				return (EVASuit)e;
		}
		return null;
	}

	public int getExtrovertmodifier() {
		if (extrovertScore == -1) {
			int score = mind.getTraitManager().getIntrovertExtrovertScore();
			extrovertScore = score;

			// if introvert, score  0 to  50 --> -2 to 0
			// if extrovert, score 50 to 100 -->  0 to 2
		}

		return (int)((extrovertScore - 50) / 25D);
	}

	/**
	 * Calculate the modifier for walking speed based on how much this unit is carrying
	 */
	public double calculateWalkSpeed() {
		double mass = getMass();
		// At full capacity, may still move at 10%.
		// Make sure is doesn't go -ve and there is always some movement
		return 1.1 - Math.min(mass/Math.max(carryingCapacity, SMALL_AMOUNT), 1D);
	}

	/**
	 * Generate a unique name for a person based on a country
	 *
	 * @param country
	 * @param gender
	 * @return the unique name
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

	/**
	 * Gets the remaining carrying capacity available.
	 *
	 * @return capacity (kg).
	 */
	public double getRemainingCarryingCapacity() {
		double result = carryingCapacity - eqmInventory.getStoredMass();
		if (result < 0)
			return 0;
		return result;
	}

	/**
	 * Get the capacity a person can carry
	 *
	 * @return capacity (kg)
	 */
	public double getCarryingCapacity() {
		return carryingCapacity;
	}

	/**
	 * Mass of Equipment is the base mass plus what every it is storing
	 */
	@Override
	public double getMass() {
		// TODO because the Person is not fully initialised in the constructor this
		// can be null. The initialise method is the culprit.
		return (eqmInventory != null ? eqmInventory.getStoredMass() : 0) + getBaseMass();

	}

	/**
	 * Get the equipment list
	 *
	 * @return the equipment list
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		if (eqmInventory == null)
			return new HashSet<>();
		return eqmInventory.getEquipmentSet();
	}

	/**
	 * Finds all of the containers (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findAllContainers() {
		return eqmInventory.findAllContainers();
	}

	/**
	 * Finds all of the containers of a particular type (excluding EVA suit).
	 *
	 * @return collection of containers or empty collection if none.
	 */
	@Override
	public Collection<Container> findContainersOfType(EquipmentType type){
		return eqmInventory.findContainersOfType(type);
	}

	/**
	 * Does this person possess an equipment of this equipment type
	 *
	 * @param typeID
	 * @return true if this person possess this equipment type
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		return eqmInventory.containsEquipment(type);
	}

	/**
	 * Adds an equipment to this person
	 *
	 * @param equipment
	 * @return true if this person can carry it
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			e.setCoordinates(getCoordinates());
			e.setContainerUnit(this);
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Remove an equipment
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return eqmInventory.removeEquipment(equipment);
	}

	/**
	 * Stores the item resource
	 *
	 * @param resource the item resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public int storeItemResource(int resource, int quantity) {
		return eqmInventory.storeItemResource(resource, quantity);
	}

	/**
	 * Retrieves the item resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public int retrieveItemResource(int resource, int quantity) {
		return eqmInventory.retrieveItemResource(resource, quantity);
	}

	/**
	 * Gets the item resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return eqmInventory.getItemResourceStored(resource);
	}

	/**
	 * Stores the amount resource
	 *
	 * @param resource the amount resource
	 * @param quantity
	 * @return excess quantity that cannot be stored
	 */
	@Override
	public double storeAmountResource(int resource, double quantity) {
		return eqmInventory.storeAmountResource(resource, quantity);
	}

	/**
	 * Retrieves the resource
	 *
	 * @param resource
	 * @param quantity
	 * @return quantity that cannot be retrieved
	 */
	@Override
	public double retrieveAmountResource(int resource, double quantity) {
		return eqmInventory.retrieveAmountResource(resource, quantity);
	}

	/**
	 * Gets the capacity of a particular amount resource
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
     * Gets the total capacity that this person can hold.
     *
     * @return total capacity (kg).
     */
	@Override
	public double getCargoCapacity() {
		return eqmInventory.getCargoCapacity();
	}

	/**
	 * Gets the amount resource stored
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return eqmInventory.getAmountResourceStored(resource);
	}

	/**
	 * Gets all stored amount resources
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return eqmInventory.getAmountResourceIDs();
	}

	/**
	 * Gets all stored item resources
	 *
	 * @return all stored item resources.
	 */
	@Override
	public Set<Integer> getItemResourceIDs() {
		return eqmInventory.getItemResourceIDs();
	}


	/**
	 * Finds the number of empty containers of a class that are contained in storage and have
	 * an empty inventory.
	 *
	 * @param containerClass  the unit class.
	 * @param brandNew  does it include brand new bag only
	 * @return number of empty containers.
	 */
	@Override
	public int findNumEmptyContainersOfType(EquipmentType containerType, boolean brandNew) {
		return eqmInventory.findNumEmptyContainersOfType(containerType, brandNew);
	}

	/**
	 * Finds the number of containers of a particular type
	 *
	 * @param containerType the equipment type.
	 * @return number of empty containers.
	 */
	@Override
	public int findNumContainersOfType(EquipmentType containerType) {
		return eqmInventory.findNumContainersOfType(containerType);
	}

	/**
	 * Finds a container in storage.
	 *
	 * @param containerType
	 * @param empty does it need to be empty ?
	 * @param resource If -1 then resource doesn't matter
	 * @return instance of container or null if none.
	 */
	@Override
	public Container findContainer(EquipmentType containerType, boolean empty, int resource) {
		return eqmInventory.findContainer(containerType, empty, resource);
	}

	/**
	 * Is this unit empty ?
	 *
	 * @return true if this unit doesn't carry any resources or equipment
	 */
	public boolean isEmpty() {
		return (eqmInventory.getStoredMass() == 0D);
	}


	/**
	 * Gets the stored mass
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Obtains the remaining general storage space
	 *
	 * @return quantity
	 */
	@Override
	public double getRemainingCargoCapacity() {
		return eqmInventory.getRemainingCargoCapacity();
	}

	/**
	 * Does it have this item resource ?
	 *
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasItemResource(int resource) {
		return eqmInventory.hasItemResource(resource);
	}

	/**
	 * Sets the unit's container unit.
	 *
	 * @param newContainer the unit to contain this unit.
	 */
	@Override
	public void setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return;
			}
			// 1. Set Coordinates
			setCoordinates(newContainer.getCoordinates());
			// 2. Set LocationStateType
			updatePersonState(newContainer);
			// 3. Set containerID
			// Q: what to set for a deceased person ?
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}
	}

	/**
	 * Updates the location state type of a person
	 *
	 * @param newContainer
	 */
	public void updatePersonState(Unit newContainer) {
		if (newContainer == null) {
			currentStateType = LocationStateType.UNKNOWN;
			return;
		}

		currentStateType = getNewLocationState(newContainer);
	}

	/**
	 * Gets the location state type based on the type of the new container unit
	 *
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	@Override
	public LocationStateType getNewLocationState(Unit newContainer) {

		if (newContainer.getUnitType() == UnitType.SETTLEMENT)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.BUILDING)
			return LocationStateType.INSIDE_SETTLEMENT;

		if (newContainer.getUnitType() == UnitType.VEHICLE)
			return LocationStateType.INSIDE_VEHICLE;

		if (newContainer.getUnitType() == UnitType.CONSTRUCTION)
			return LocationStateType.MARS_SURFACE;

		if (newContainer.getUnitType() == UnitType.PERSON)
			return LocationStateType.ON_PERSON_OR_ROBOT;

		if (newContainer.getUnitType() == UnitType.PLANET)
			return LocationStateType.MARS_SURFACE;

		return null;
	}

	/**
	 * Is this unit inside a settlement
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {

		if (containerID == MARS_SURFACE_UNIT_ID)
			return false;

		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;

		return false;
	}

	/**
	 * Transfer the unit from one owner to another owner
	 *
	 * @param origin {@link Unit} the original container unit
	 * @param destination {@link Unit} the destination container unit
	 */
	public boolean transfer(Unit destination) {
		boolean transferred = false;
		Unit cu = getContainerUnit();
		UnitType ut = cu.getUnitType();

		if (destination.equals(cu)) {
			return true;
		}

		// Check if the origin is a vehicle
		if (ut == UnitType.VEHICLE) {
			if (((Vehicle)cu).getVehicleType() != VehicleType.DELIVERY_DRONE) {
				transferred = ((Crewable)cu).removePerson(this);
			}
			else {
				logger.warning(this + "Not possible to be retrieved from " + cu + ".");
			}
		}
		else if (ut == UnitType.PLANET) {
			transferred = ((MarsSurface)cu).removePerson(this);
		}
		else if (ut == UnitType.BUILDING) {
//			BuildingManager.removePersonFromBuilding(this, (Building)cu);
			transferred = true;
		}
		else if (ut == UnitType.SETTLEMENT) {
			// Q1: should one remove this person from settlement's peopleWithin list,
			//     especially if he is still inside the garage of a settlement ?
			// Q2: should it be the vehicle's responsibility to remove the person from the settlement
			//     as the vehicle leaves the garage ?
			transferred = ((Settlement)cu).removePeopleWithin(this);
			BuildingManager.removePersonFromBuilding(this, getBuildingLocation());
		}

		if (transferred) {
			// Check if the destination is a vehicle
			if (destination.getUnitType() == UnitType.VEHICLE) {
				if (((Vehicle)destination).getVehicleType() != VehicleType.DELIVERY_DRONE) {
					transferred = ((Crewable)destination).addPerson(this);
				}
				else {
					logger.warning(this + "Not possible to be stored into " + cu + ".");
				}
			}
			else if (destination.getUnitType() == UnitType.PLANET) {
				transferred = ((MarsSurface)destination).addPerson(this);
			}
			else if (destination.getUnitType() == UnitType.SETTLEMENT) {
				transferred = ((Settlement)destination).addPeopleWithin(this);
			}
			else if (destination.getUnitType() == UnitType.BUILDING) {
				BuildingManager.addPersonOrRobotToBuilding(this, (Building)destination);
				transferred = ((Building)destination).getSettlement().addPeopleWithin(this);
			}

			if (!transferred) {
				logger.warning(this + " cannot be stored into " + destination + ".");
				// NOTE: need to revert back the storage action
			}
			else {
				// Set the new container unit (which will internally set the container unit id)
				setContainerUnit(destination);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, this);
				// Fire the unit event type
				getContainerUnit().fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, this);
			}
		}
		else {
			logger.warning(this + " cannot be retrieved from " + cu + ".");
			// NOTE: need to revert back the retrieval action
		}

		return transferred;
	}

	@Override
	public UnitType getUnitType() {
		return UnitType.PERSON;
	}

	/**
	 * Gets the holder's unit instance
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	/**
	 * Sets unit's location coordinates
	 *
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		super.setCoordinates(newLocation);

		if (getEquipmentSet() != null && !getEquipmentSet().isEmpty()) {
			for (Equipment e: getEquipmentSet()) {
				e.setCoordinates(newLocation);
			}
		}
	}

	/**
	 * Does this person have a set of clothing
	 */
	public boolean hasGarment() {
		return getItemResourceStored(ItemResourceUtil.garmentID) > 0;
	}

	/**
	 * Does this person have a pressure suit
	 */
	public boolean hasPressureSuit() {
		return getItemResourceStored(ItemResourceUtil.pressureSuitID) > 0;
	}

	/**
	 * Puts on a garment
	 *
	 * @param holder the previous holder of the clothing
	 */
	public void wearGarment(EquipmentOwner holder) {
		if (!hasGarment() && holder.retrieveItemResource(ItemResourceUtil.garmentID, 1) == 0) {
			storeItemResource(ItemResourceUtil.garmentID, 1);
		}
	}

	/**
	 * Puts on a pressure suit set
	 *
	 * @param suit
	 */
	public void wearPressureSuit(EquipmentOwner holder) {
		if (!hasPressureSuit() && holder.retrieveItemResource(ItemResourceUtil.pressureSuitID, 1) == 0) {
			storeItemResource(ItemResourceUtil.pressureSuitID, 1);
		}
	}


	/**
	 * Puts off the garment
	 *
	 * @param holder the new holder of the clothing
	 * @return true if successful
	 */
	public boolean unwearGarment(EquipmentOwner holder) {
		if (hasGarment() && retrieveItemResource(ItemResourceUtil.garmentID, 1) == 0) {
			if (holder.storeItemResource(ItemResourceUtil.garmentID, 1) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Puts off the pressure suit set
	 *
	 * @param suit
	 * @return true if successful
	 */
	public boolean unwearPressureSuit(EquipmentOwner holder) {
		if (hasPressureSuit() && retrieveItemResource(ItemResourceUtil.pressureSuitID, 1) == 0) {
			if (holder.storeItemResource(ItemResourceUtil.pressureSuitID, 1) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reinitialize references after loading from a saved sim
	 */
	public void reinit() {
		mind.reinit();
		condition.reinit();
	}

	/**
	 * Compares if an object is the same as this equipment
	 *
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		Person p = (Person) obj;
		return this.getIdentifier() == p.getIdentifier();
	}

	/**
	 * Gets the hash code for this object.
	 *
	 * @return hash code.
	 */
	@Override
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

}
