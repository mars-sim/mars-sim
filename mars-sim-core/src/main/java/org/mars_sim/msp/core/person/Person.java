/*
 * Mars Simulation Project
 * Person.java
 * @date 2023-06-25
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import java.util.stream.Collectors;

import org.mars.sim.mapdata.location.Coordinates;
import org.mars.sim.mapdata.location.LocalPosition;
import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.CollectionUtils;
import org.mars_sim.msp.core.LifeSupportInterface;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.data.SolMetricDataLogger;
import org.mars_sim.msp.core.data.UnitSet;
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
import org.mars_sim.msp.core.person.ai.fav.Favorite;
import org.mars_sim.msp.core.person.ai.fav.FavoriteType;
import org.mars_sim.msp.core.person.ai.fav.Preference;
import org.mars_sim.msp.core.person.ai.job.util.AssignmentHistory;
import org.mars_sim.msp.core.person.ai.job.util.AssignmentType;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionType;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.social.Relation;
import org.mars_sim.msp.core.person.ai.task.EVAOperation;
import org.mars_sim.msp.core.person.ai.task.meta.WorkoutMeta;
import org.mars_sim.msp.core.person.ai.task.util.ScheduleManager;
import org.mars_sim.msp.core.person.ai.task.util.TaskManager;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.core.person.ai.training.TrainingType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.science.ResearcherInterface;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.ShiftSlot;
import org.mars_sim.msp.core.structure.ShiftSlot.WorkStatus;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.Temporal;
import org.mars_sim.msp.core.vehicle.Crewable;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.core.vehicle.VehicleType;

/**
 * The Person class represents a person on Mars. It keeps track of everything
 * related to that person and provides information about him/her.
 */
public class Person extends Unit implements Worker, Temporal, ResearcherInterface {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Person.class.getName());
	
	/** The maximum number of sols for storing stats. */
	public static final int MAX_NUM_SOLS = 7;
	/** The standard hand carrying capacity for food in a person. */
	public static final int CARRYING_CAPACITY_FOOD = 1;
	
	/** A small amount. */
	private static final double SMALL_AMOUNT = 0.00001D;

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

	/** The birth of a person */
	private LocalDate birthDate;
	/** The age of a person */
	private int age = -1;
	/** The quarters that the person belongs. */
	private int quartersInt = -1;
	/** The current building location of the person. */
	private int currentBuildingInt;
	/** The carrying capacity of the person. */
	private int carryingCapacity;
	/** The id of the person who invite this person for a meeting. */
	private int initiatorId = -1;
	/** The id of the person being invited by this person for a meeting. */
	private int inviteeId = -1;
	
	/** The settlement the person is currently associated with. */
	private Integer associatedSettlementID = Integer.valueOf(-1);
	/** The buried settlement if the person has been deceased. */
	private Integer buriedSettlement = Integer.valueOf(-1);

	/** The eating speed of the person [kg/millisol]. */
	private double eatingSpeed = .5 + RandomUtil.getRandomDouble(-.05, .05);
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
	/** Person's JobHistory instance. */
	private AssignmentHistory jobHistory;
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
	/** The person's current scientific study. */
	private ScientificStudy study;
	/** The person's EquipmentInventory instance. */
	private EquipmentInventory eqmInventory;
	/** The schedule manger that keeps track of scheduled appointments. */
	private ScheduleManager scheduleManager;
	
	/** The person's achievement in scientific fields. */
	private Map<ScienceType, Double> scientificAchievement = new ConcurrentHashMap<>();
	/** The person's paternal chromosome. */
	private Map<Integer, Gene> paternalChromosome;
	/** The person's maternal chromosome. */
	private Map<Integer, Gene> maternalChromosome;
	/** The person's mission experiences. */
	private Map<MissionType, Integer> missionExperiences;
	/** The person's list of prior trainings */
	private List<TrainingType> trainings;
	/** The person's list of collaborative scientific studies. */
	private Set<ScientificStudy> collabStudies;

	/** The person's EVA times. */
	private SolMetricDataLogger<String> eVATaskTime;
	private ShiftSlot shiftSlot;

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
	 * Constructor 0 : used by LoadVehicleTest and other maven test suites.
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
		BuildingManager.addPersonToRandomBuilding(this, settlement);
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

		// Create a prior training profile
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
	 * Uses static factory method to create an instance of PersonBuilder.
	 *
	 * @param name
	 * @param settlement
	 * @return
	 */
	public static PersonBuilder<?> create(String name, Settlement settlement) {
		return new PersonBuilderImpl(name, settlement);
	}

	/**
	 * Initializes field data and class for maven test.
	 */
	public void initializeForMaven() {
		// Construct the EquipmentInventory instance.
		eqmInventory = new EquipmentInventory(this, carryingCapacity);
		// Create favorites
		favorite = new Favorite(this);
		// Create preferences
		preference = new Preference(this);
		
		setupChromosomeMap();

		eqmInventory.addCargoCapacity(carryingCapacity);
		
		eqmInventory.setResourceCapacity(ResourceUtil.foodID, CARRYING_CAPACITY_FOOD);
	}
	
	/**
	 * Initializes field data and class.
	 */
	public void initialize() {
		// WARNING: setAssociatedSettlement(settlement) may cause suffocation 
		// Reloading from a saved sim
		
		// Add to a random building
		BuildingManager.addPersonToRandomBuilding(this, getAssociatedSettlement());
		// Set up the time stamp for the person
		calculateBirthDate(masterClock.getEarthTime());
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
		// Initialize field data in circadian clock
		circadian.initialize();
		// Create job history
		jobHistory = new AssignmentHistory();
		// Create the role
		role = new Role(this);
		// Create shift schedule
		shiftSlot = getAssociatedSettlement().getShiftManager().allocationShift(this);
		
		scheduleManager = new ScheduleManager(this);
		// Set up life support type
		support = getLifeSupportType();
		// Create the mission experiences map
		missionExperiences = new EnumMap<>(MissionType.class);
//		// Create the EVA hours map
		eVATaskTime = new SolMetricDataLogger<>(MAX_NUM_SOLS);
		// Create a set of collaborative studies
		collabStudies = new HashSet<>();
		// Construct the EquipmentInventory instance.
		eqmInventory = new EquipmentInventory(this, carryingCapacity);
		
		eqmInventory.setResourceCapacity(ResourceUtil.foodID, .6);
	}

	/**
	 * Computes a person's chromosome map.
	 */
	private void setupChromosomeMap() {
		paternalChromosome = new ConcurrentHashMap<>();
		maternalChromosome = new ConcurrentHashMap<>();

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
			// Set up carrying capacity and personality traits: id 40 - 59
			setupCarryingCapAttributeTrait();
		}
	}

	/**
	 * Computes a person's carrying capacity and attributes and its chromosome.
	 */
	private void setupCarryingCapAttributeTrait() {
		// Note: set up a set of genes that was passed onto this person
		// from two hypothetical parents
		int id = 40;
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
			load = (baseCap/3 + age * 2);
		else if (age > 14 && age <= 18)
			load = (int)(baseCap/2.5 + age * 1.5);
		else if (age > 18 && age <= 25)
			load = (int)(baseCap/2.0 + 35 - age / 7.5);
		else if (age > 25 && age <= 35)
			load = (int)(baseCap + 30 - age / 12.5);
		else if (age > 35 && age <= 45)
			load = (baseCap + 25 - age / 10);
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
		
		int score = mind.getMBTI().getIntrovertExtrovertScore();

		Gene trait1G = new Gene(this, id, "Trait 1", true, dominant, "Introvert", score);
		
		paternalChromosome.put(id, trait1G);
	}

	/**
	 * Computes a person's blood type and its chromosome.
	 */
	private void setupBloodType() {
		int id = 1;
		boolean dominant = false;

		String dadBloodType = null;
		int rand = RandomUtil.getRandomInt(2);
		if (rand == 0) {
			dadBloodType = "A";
			dominant = true;
		} else if (rand == 1) {
			dadBloodType = "B";
			dominant = true;
		} else if (rand == 2) {
			dadBloodType = "O";
			dominant = false;
		}

		// Biochemistry 0 - 19
		Gene dadBloodTypeG = new Gene(this, id, "Blood Type", true, dominant, dadBloodType, 0);
		paternalChromosome.put(id, dadBloodTypeG);

		String momBloodType = null;
		rand = RandomUtil.getRandomInt(2);
		if (rand == 0) {
			momBloodType = "A";
			dominant = true;
		} else if (rand == 1) {
			momBloodType = "B";
			dominant = true;
		} else if (rand == 2) {
			momBloodType = "O";
			dominant = false;
		}

		Gene momBloodTypeG = new Gene(this, 0, "Blood Type", false, dominant, momBloodType, 0);
		maternalChromosome.put(0, momBloodTypeG);

		if (dadBloodType.equals("A") && momBloodType.equals("A"))
			bloodType = "A";
		else if (dadBloodType.equals("A") && momBloodType.equals("B"))
			bloodType = "AB";
		else if (dadBloodType.equals("A") && momBloodType.equals("O"))
			bloodType = "A";
		else if (dadBloodType.equals("B") && momBloodType.equals("A"))
			bloodType = "AB";
		else if (dadBloodType.equals("B") && momBloodType.equals("B"))
			bloodType = "B";
		else if (dadBloodType.equals("B") && momBloodType.equals("O"))
			bloodType = "B";
		else if (dadBloodType.equals("O") && momBloodType.equals("A"))
			bloodType = "A";
		else if (dadBloodType.equals("O") && momBloodType.equals("B"))
			bloodType = "B";
		else if (dadBloodType.equals("O") && momBloodType.equals("O"))
			bloodType = "O";

	}

	/**
	 * Gets the blood type.
	 *
	 * @return
	 */
	public String getBloodType() {
		return bloodType;
	}

	/**
	 * Computes a person's height and its chromosome.
	 */
	private void setupHeight() {
		int id = 20;
		boolean dominant = false;

		// For a 20-year-old in the US:
		//   male - height: 176.5,  weight: 68.5
		// female - height: 162.6,  weight: 57.2

		// Note: factor in country of origin.
		// Note: look for a gender-correlated curve

		// Note: p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Attempt to compute height with gaussian curve

		double dadHeight = tall + RandomUtil.getGaussianDouble() * tall / 7D;// RandomUtil.getRandomInt(22);
		double momHeight = shortH + RandomUtil.getGaussianDouble() * shortH / 10D;// RandomUtil.getRandomInt(15);

		Gene dadHeightG = new Gene(this, id, HEIGHT_GENE, true, dominant, null, dadHeight);
		paternalChromosome.put(id, dadHeightG);

		Gene momHeightG = new Gene(this, id, HEIGHT_GENE, false, dominant, null, momHeight);
		maternalChromosome.put(id, momHeightG);

		double geneticFactor = .65;
		double sexFactor = (tall - averageHeight) / averageHeight;
		// Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			height = Math.round(
					(geneticFactor * dadHeight + (1 - geneticFactor) * momHeight * (1 + sexFactor)) * 100D) / 100D;
		else
			height = Math.round(
					((1 - geneticFactor) * dadHeight + geneticFactor * momHeight * (1 - sexFactor)) * 100D) / 100D;

	}

	/**
	 * Computes a person's weight and its chromosome.
	 */
	private void setupWeight() {
		int id = 21;
		boolean dominant = false;

		// For a 20-year-old in the US:
		// male : height : 176.5 weight : 68.5
		// female : height : 162.6 weight : 57.2

		// Note: factor in country of origin.
		// Note: look for a gender-correlated curve

		// Note: p = mean + RandomUtil.getGaussianDouble() * standardDeviation
		// Attempt to compute height with gaussian curve
		double dadWeight = highW + RandomUtil.getGaussianDouble() * highW / 13.5;// RandomUtil.getRandomInt(10);
		double momWeight = lowW + RandomUtil.getGaussianDouble() * lowW / 10.5;// RandomUtil.getRandomInt(15);

		Gene dadWeightG = new Gene(this, id, WEIGHT_GENE, true, dominant, null, dadWeight);
		paternalChromosome.put(id, dadWeightG);

		Gene momWeightG = new Gene(this, id, WEIGHT_GENE, false, dominant, null, momWeight);
		maternalChromosome.put(id, momWeightG);

		double geneticFactor = .65;
		double sexFactor = (highW - averageWeight) / averageWeight; // for male
		double heightFactor = height / averageHeight;

		// Add arbitrary (US-based) sex and genetic factor
		if (gender == GenderType.MALE)
			weight = Math.round(heightFactor
					* (geneticFactor * dadWeight + (1 - geneticFactor) * momWeight * (1 + sexFactor)) * 100D)
					/ 100D;
		else
			weight = Math.round(heightFactor
					* ((1 - geneticFactor) * dadWeight + geneticFactor * momWeight * (1 - sexFactor)) * 100D)
					/ 100D;

		setBaseMass(weight);
	}

	/*
	 * Sets sponsoring agency for the person.
	 */
	public void setSponsor(ReportingAuthority sponsor) {
		ra = sponsor;
	}

	/*
	 * Gets sponsoring agency for the person.
	 */
	public ReportingAuthority getReportingAuthority() {
		return ra;
	}

	/*
	 * Gets task preference for the person.
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
			mind.assignJob(JobType.POLITICIAN, true, JobUtil.SETTLEMENT, AssignmentType.APPROVED, JobUtil.SETTLEMENT);
		}
	}

	/**
	 * Sets the job of a person.
	 *
	 * @param job JobType
	 * @param authority
	 */
	public void setJob(JobType job, String authority) {
		mind.assignJob(job, true, JobUtil.SETTLEMENT, AssignmentType.APPROVED, authority);
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
	public AssignmentHistory getJobHistory() {
		return jobHistory;
	}

	/**
	 * Gets the instance of Favorite for a person.
	 */
	public Favorite getFavorite() {
		return favorite;
	}

	/**
	 * Get details of the 
	 */
	public ShiftSlot getShiftSlot() {
		return shiftSlot;
	}

	/**
	 * Is this Person OnDuty. This does not include On Call.
	 */
	public boolean isOnDuty() {
		return shiftSlot.getStatus() == WorkStatus.ON_DUTY;
	}


	/**
	 * Creates a string representing the birth time of the person.
	 * @param clock
	 *
	 */
	private void calculateBirthDate(LocalDateTime earthLocalTime) {
		// Remove a random number of days from the current earth date
		int daysPast = RandomUtil.getRandomInt(21*365, 65*365);
		birthDate = earthLocalTime.minusDays(daysPast).toLocalDate();

		// Calculate the year
		// Set the age
		age = updateAge(earthLocalTime);
	}

	/**
	 * Is the person outside of a settlement but within its vicinity ?
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
	 * Gets the settlement in vicinity. This is used assume the person is not at a settlement.
	 *
	 * @return the person's settlement
	 */
	public Settlement getNearbySettlement() {
		return CollectionUtils.findSettlement(getCoordinates());
	}
	
	/**
	 * Gets the settlement the person is at.
	 * Returns null if person is not at a settlement.
	 *
	 * @return the person's settlement
	 */
	@Override
	public Settlement getSettlement() {

		if (getContainerID() <= Unit.MARS_SURFACE_UNIT_ID)
			return null;

		Unit c = getContainerUnit();

		if (c.getUnitType() == UnitType.SETTLEMENT) {
			return (Settlement) c;
		}

		if (c.getUnitType() == UnitType.VEHICLE) {
			// Will see if vehicle is inside a garage or not
			return ((Vehicle)c).getSettlement();
		}

		if (c.getUnitType() == UnitType.BUILDING || c.getUnitType() == UnitType.PERSON
				|| c.getUnitType() == UnitType.ROBOT) {
			return c.getSettlement();
		}

		return null;
	}

	/**
	 * Buries the Person at the current location. This happens only if the person can
	 * be retrieved from any containing Settlements or Vehicles found. The body is
	 * fixed at the last location of the containing unit.
	 */
	public void buryBody() {
		// Bury the body
		isBuried = true;
		// Back up the last container unit
		condition.getDeathDetails().backupContainerUnit(getContainerUnit());

		// Set his/her currentStateType
		currentStateType = LocationStateType.WITHIN_SETTLEMENT_VICINITY;
		// Set his/her buried settlement
		buriedSettlement = associatedSettlementID;

		// Throw unit event.
		fireUnitUpdate(UnitEventType.BURIAL_EVENT);
	}

	/**
	 * Declares the person dead.
	 */
	void setDeclaredDead() {
		declaredDead = true;
		setDescription("Dead");
	}

	/**
	 * Deregisters the person's quarters.
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
	 * Person can take action with time passing.
	 *
	 * @param pulse amount of time passing (in millisols).
	 */
	@Override
	public boolean timePassing(ClockPulse pulse) {
		if (!isValid(pulse)) {
			return false;
		}

		// Check to see if the person has deceased
		if (condition.getDeathDetails() != null
				&& condition.getDeathDetails().getBodyRetrieved()) {
			setDeceased();
		}
		
		// Check to see if the person is dead
		if (condition.isDead()) {
			return false;
		}

		// Primary researcher; my responsibility to update Study
		if (study != null) {
			study.timePassing(pulse);
		}

		EVASuit suit = getSuit();
		// Record the use of it
		if (suit != null) {
			suit.recordUsageTime(pulse);
		}

		// Mental changes with time passing.
		mind.timePassing(pulse);

		// Check schedule
		scheduleManager.timePassing(pulse);
		
		// If Person is dead, then skip
		if (getLifeSupportType() != null) {
			// Get the life support type
			support = getLifeSupportType();
		}

		circadian.timePassing(pulse, support);
		// Pass the time in the physical condition first as this may result in death.
		condition.timePassing(pulse, support);

		if (pulse.isNewSol()) {
			// Update the solCache
			int currentSol = pulse.getMarsTime().getMissionSol();

			if (currentSol == 1) {
				// On the first mission sol,
				// adjust the sleep habit according to the current work shift
				for (int i=0; i< 15; i++) {
					int shiftEnd = shiftSlot.getShift().getEnd();
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

				condition.increaseFatigue(RandomUtil.getRandomInt(333));
			}
			else {
				// Adjust the sleep habit according to the current work shift
				for (int i=0; i< 5; i++) {
					int m = shiftSlot.getShift().getEnd() + 10 * (i+1);
					if (m > 1000)
						m = m - 1000;
					circadian.updateSleepCycle(m, true);
				}

				// Check if a person's age should be updated
				age = updateAge(pulse.getMasterClock().getEarthTime());

				// Checks if a person has a role
				if (role.getType() == null)
					role.obtainNewRole();
			}
		}
		
		return true;
	}

	/**
	 * Checks if the person has deceased.
	 * 
	 * @return
	 */
	public void setDeceased() {
		if (!isBuried && !declaredDead) {
			// Declares the person dead
			setDeclaredDead();
			// Deregisters the person's quarters
			deregisterBed();
			// Deactivates the person's mind
			mind.setInactive();
		}
	}

	/**
	 * Returns a reference to the Person's natural attribute manager.
	 *
	 * @return the person's natural attribute manager
	 */
	@Override
	public NaturalAttributeManager getNaturalAttributeManager() {
		return attributes;
	}

	/**
	 * Gets the performance factor that effect Person with health complaint.
	 *
	 * @return The value is between 0 -> 1.
	 */
	public double getPerformanceRating() {
		return condition.getPerformanceFactor();
	}

	/**
	 * Returns a reference to the Person's physical condition.
	 *
	 * @return the person's physical condition
	 */
	public PhysicalCondition getPhysicalCondition() {
		return condition;
	}

	/**
	 * Returns the person's mind.
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
	 * Updates and returns the person's age.
	 *
	 * @return the person's age
	 */
	private int updateAge(LocalDateTime localDateTime) {
		age = (int)ChronoUnit.YEARS.between(birthDate, localDateTime);
		return age;
	}

	/**
	 * Sets a person's age and update one's year of birth.
	 *
	 * @param newAge
	 */
	public void changeAge(int newAge) {
		// Back calculate a person's year
		int offset = age - newAge;
		// Set year to newYear
		birthDate = LocalDate.of(birthDate.getYear() + offset, birthDate.getMonth(), birthDate.getDayOfMonth());
		age = newAge;
	}

	/**
	 * Returns the person's birth date in the format of "2055-05-06".
	 *
	 * @return the person's birth date
	 */
	public String getBirthDate() {
		return birthDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
	}

	/**
	 * Gets the LifeSupport system supporting this Person. This may be from the
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
	 * Gets the birthplace of the person.
	 *
	 * @return the birthplace
	 */
	public String getBirthplace() {
		return birthplace;
	}

	/**
	 * Gets the person's local group of people (in building or rover).
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
	 * Checks if a person is super unfit.
	 *
	 * @return true if a person is super fit
	 */
	public boolean isSuperUnFit() {
		return condition.isSuperUnFit();
    }
	
	/**
	 * Checks if a person is unfit.
	 *
	 * @return true if a person is unfit
	 */
	public boolean isUnFit() {
		return condition.isUnFit();
    }

	/**
	 * Checks if a person is nominally fit.
	 *
	 * @return true if a person is nominally fit
	 */
	public boolean isNominallyFit() {
        return condition.isNominallyFit();
    }
	
	/**
	 * Sets the person's name.
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
	 * 
	 * @param scientificStudy
	 */
	@Override
	public void setStudy(ScientificStudy scientificStudy) {
		this.study = scientificStudy;
	}

	
	/**
	 * Gets the scientific study instance.		
	 */
	@Override
	public ScientificStudy getStudy() {
		return study;
	}

	/**
	 * Gets the collaborative study sets.
	 */
	@Override
	public Set<ScientificStudy> getCollabStudies() {
		return collabStudies;
	}
	
	/**
	 * Adds the collaborative study.
	 * 
	 * @param study
	 */
	@Override
	public void addCollabStudy(ScientificStudy study) {
		this.collabStudies.add(study);
	}

	/**
	 * Removes the collaborative study.
	 * 
	 * @param study
	 */
	@Override
	public void removeCollabStudy(ScientificStudy study) {
		this.collabStudies.remove(study);
	}

	/**
	 * Gets the person's achievement credit for a given scientific field.
	 *
	 * @param science the scientific field.
	 * @return achievement credit.
	 */
	@Override
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
	@Override
	public double getTotalScientificAchievement() {
		double result = 0d;
		for (double value : scientificAchievement.values()) {
			result += value;
		}
		return result;
	}

	/**
	 * Adds achievement credit to the person in a scientific field.
	 *
	 * @param achievementCredit the achievement credit.
	 * @param science           the scientific field.
	 */
	@Override
	public void addScientificAchievement(double achievementCredit, ScienceType science) {
		if (scientificAchievement.containsKey(science)) {
			achievementCredit += scientificAchievement.get(science);
		}
		scientificAchievement.put(science, achievementCredit);
	}


	/**
	 * Computes the building the person is currently located at.
	 * Returns null if outside of a settlement.
	 *
	 * @return building
	 */
	@Override
	public Building getBuildingLocation() {
		if (currentBuildingInt == -1)
			return null;
		return unitManager.getBuildingByID(currentBuildingInt);
	}

	/**
	 * Computes the building the person is currently located at.
	 * Returns null if outside of a settlement.
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
		if (isRightOutsideSettlement())
			return getLocationTag().findSettlementVicinity();
		else
			return null;
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

	public int[] getPreferredSleepHours() {
		return circadian.getPreferredSleepHours();
	}

	/**
	 * Returns the weight/desire for sleep at a msol.
	 * 
	 * @param index
	 * @return
	 */
	public int getSleepWeight(int msol) {
		return circadian.getSleepWeight(msol);
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
			birthplace = c + ", " + EARTH_BIRTHPLACE;
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
	 * Randomly generates a list of training the person may have attended.
	 */
	private void generatePriorTraining() {
		if (trainings == null) {
			trainings = new ArrayList<>();
			List<TrainingType> lists = new ArrayList<>(Arrays.asList(TrainingType.values()));
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
	 * Gets a list of prior training.
	 *
	 * @return {@link List<TrainingType>}
	 */
	public List<TrainingType> getTrainings() {
		return trainings;
	}

	/**
	 * Gets the EVA suit the person has donned on.
	 *
	 * @return
	 */
	public EVASuit getSuit() {
		return getInventorySuit();
	}

	/**
	 * Gets the EVA suit instance the person has in inventory.
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
	 * Calculates the modifier for walking speed based on how much this unit is carrying.
	 */
	public double calculateWalkSpeed() {
		double mass = getMass();
		// At full capacity, may still move at 10%.
		// Make sure is doesn't go -ve and there is always some movement
		return 1.1 - Math.min(mass/Math.max(carryingCapacity, SMALL_AMOUNT), 1D);
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
	 * Gets the capacity a person can carry.
	 *
	 * @return capacity (kg)
	 */
	public double getCarryingCapacity() {
		return carryingCapacity;
	}

	/**
	 * Returns the mass as the base mass plus whatever being stored in him.
	 */
	@Override
	public double getMass() {
		// TODO because the Person is not fully initialised in the constructor this
		// can be null. The initialise method is the culprit.
		return (eqmInventory != null ? eqmInventory.getModifiedMass(EquipmentType.WHEELBARROW, 20) : 0) + getBaseMass();
	}
	
	/**
	 * Gets the equipment list.
	 *
	 * @return the equipment list
	 */
	@Override
	public Set<Equipment> getEquipmentSet() {
		if (eqmInventory == null)
			return new UnitSet<>();
		return eqmInventory.getEquipmentSet();
	}

	public EquipmentInventory getEquipmentInventory() {
		return eqmInventory;
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
	 * Does this person possess an equipment of this equipment type ?
	 *
	 * @param typeID
	 * @return true if this person possess this equipment type
	 */
	@Override
	public boolean containsEquipment(EquipmentType type) {
		return eqmInventory.containsEquipment(type);
	}

	/**
	 * Adds an equipment to this person.
	 *
	 * @param equipment
	 * @return true if this person can carry it
	 */
	@Override
	public boolean addEquipment(Equipment e) {
		if (eqmInventory.addEquipment(e)) {
			fireUnitUpdate(UnitEventType.ADD_ASSOCIATED_EQUIPMENT_EVENT, this);
			return true;
		}
		return false;
	}

	/**
	 * Removes an equipment.
	 *
	 * @param equipment
	 */
	@Override
	public boolean removeEquipment(Equipment equipment) {
		return eqmInventory.removeEquipment(equipment);
	}
	
	/**
	 * Stores the item resource.
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
	 * Retrieves the item resource.
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
	 * Gets the item resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public int getItemResourceStored(int resource) {
		return eqmInventory.getItemResourceStored(resource);
	}

	/**
	 * Stores the amount resource.
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
	 * Retrieves the resource.
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
	 * Gets the capacity of a particular amount resource.
	 *
	 * @param resource
	 * @return capacity
	 */
	@Override
	public double getAmountResourceCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Obtains the remaining storage space of a particular amount resource.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.getAmountResourceCapacity(resource);
	}

	/**
	 * Does it have unused space or capacity for a particular resource ?
	 * 
	 * @param resource
	 * @return
	 */
	@Override
	public boolean hasAmountResourceRemainingCapacity(int resource) {
		return eqmInventory.hasAmountResourceRemainingCapacity(resource);
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
	 * Gets the amount resource stored.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAmountResourceStored(int resource) {
		return eqmInventory.getAmountResourceStored(resource);
	}

	/**
	 * Gets all the amount resource resource stored, including inside equipment.
	 *
	 * @param resource
	 * @return quantity
	 */
	@Override
	public double getAllAmountResourceStored(int resource) {
		return eqmInventory.getAllAmountResourceStored(resource);
	}
	
	/**
	 * Gets all stored amount resources in eqmInventory.
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAmountResourceIDs() {
		return eqmInventory.getAmountResourceIDs();
	}
	
	/**
	 * Gets all stored amount resources in eqmInventory, including inside equipment
	 *
	 * @return all stored amount resources.
	 */
	@Override
	public Set<Integer> getAllAmountResourceIDs() {
		return eqmInventory.getAllAmountResourceIDs();
	}

	/**
	 * Gets all stored item resources.
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
	 * Finds the number of containers of a particular type.
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
	 * Gets the stored mass.
	 */
	@Override
	public double getStoredMass() {
		return eqmInventory.getStoredMass();
	}

	/**
	 * Obtains the remaining general storage space.
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
	public boolean setContainerUnit(Unit newContainer) {
		if (newContainer != null) {
			if (newContainer.equals(getContainerUnit())) {
				return false;
			}
			// 1. Set Coordinates
			if (newContainer.getUnitType() == UnitType.MARS) {
				// Since it's on the surface of Mars,
				// First set its initial location to its old parent's location as it's leaving its parent.
				// Later it may move around and updates its coordinates by itself
				setCoordinates(getContainerUnit().getCoordinates());
			}
			else {
				// Null its coordinates since it's now slaved after its parent
				setCoordinates(null);
			}
			// 2. Set new LocationStateType
			updatePersonState(newContainer);
			// 3. Set containerID
			// TODO: what to set for a deceased person ?
			setContainerID(newContainer.getIdentifier());
			// 4. Fire the container unit event
			fireUnitUpdate(UnitEventType.CONTAINER_UNIT_EVENT, newContainer);
		}
		return true;
	}

	/**
	 * Updates the location state type of a person.
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
	 * Gets the location state type based on the type of the new container unit.
	 *
	 * @param newContainer
	 * @return {@link LocationStateType}
	 */
	private LocationStateType getNewLocationState(Unit newContainer) {

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

		if (newContainer.getUnitType() == UnitType.MARS)
			return LocationStateType.MARS_SURFACE;

		return null;
	}

	/**
	 * Is this unit inside a settlement ?
	 *
	 * @return true if the unit is inside a settlement
	 */
	@Override
	public boolean isInSettlement() {

		if (containerID <= MARS_SURFACE_UNIT_ID)
			return false;

		if (LocationStateType.INSIDE_SETTLEMENT == currentStateType)
			return true;

		return false;
	}

	/**
	 * Transfer the unit from one owner to another owner.
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
		else if (ut == UnitType.MARS) {
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
				if (destination instanceof Crewable cr) {
					transferred = cr.addPerson(this);
				}
				else {
					logger.warning(this + "Not possible to be stored into " + cu + ".");
				}
			}
			else if (destination.getUnitType() == UnitType.MARS) {
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
				// Fire the unit event type for old container
				cu.fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, this);
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
	 * Gets the holder's unit instance.
	 *
	 * @return the holder's unit instance
	 */
	@Override
	public Unit getHolder() {
		return this;
	}

	/**
	 * Sets unit's location coordinates.
	 *
	 * @param newLocation the new location of the unit
	 */
	public void setCoordinates(Coordinates newLocation) {
		super.setCoordinates(newLocation);

//		if (getEquipmentSet() != null && !getEquipmentSet().isEmpty()) {
//			for (Equipment e: getEquipmentSet()) {
//				e.setCoordinates(newLocation);
//			}
//		}
	}

	/**
	 * Does this person have a set of clothing ?
	 */
	public boolean hasGarment() {
		return getItemResourceStored(ItemResourceUtil.garmentID) > 0;
	}

	/**
	 * Does this person have a pressure suit ?
	 */
	public boolean hasPressureSuit() {
		return getItemResourceStored(ItemResourceUtil.pressureSuitID) > 0;
	}

	/**
	 * Does this person have a thermal bottle ?
	 * 
	 * @return
	 */
	public boolean hasThermalBottle() {
		return findNumContainersOfType(EquipmentType.THERMAL_BOTTLE) > 0;
	}
	
	/**
	 * Fills up a thermal bottle with water.
	 * 
	 * @param amount
	 */
	public void fillUpThermalBottle(double amount) {
		Container bottle = lookForThermalBottle();
		bottle.storeAmountResource(ResourceUtil.waterID, amount);
	}
	
	/**
	 * Looks for one's thermal bottle.
	 * 
	 * @return
	 */
	public Container lookForThermalBottle() {
		Container c = eqmInventory.findOwnedContainer(EquipmentType.THERMAL_BOTTLE, getIdentifier(), ResourceUtil.waterID);
		if (c == null)
			return findContainer(EquipmentType.THERMAL_BOTTLE, false, ResourceUtil.waterID);
		else
			return c;
	}
	
	/**
	 * Assigns a thermal bottle as a standard living necessity.
	 */
	public void assignThermalBottle() {

		if (!hasThermalBottle() && isInside()) {
			Equipment aBottle = null;
			for(Equipment e : ((EquipmentOwner)getContainerUnit()).getEquipmentSet()) {
				if (e.getEquipmentType() == EquipmentType.THERMAL_BOTTLE) {
					Person originalOwner = e.getRegisteredOwner();
					if (originalOwner != null && originalOwner.equals(this)) {
						// Remove it from the container unit
						e.transfer(this);
						// Register the person as the owner of this bottle
						e.setRegisteredOwner(this);
						
						return;
					}
					
					// Save this bottle first
					if (aBottle == null)
						aBottle = e;
				}
			}
			
			// After done with iterating over all the bottle,
			// if it still can't find a bottle that was last assigned to this person
			// get the first saved one 
			if (aBottle != null) {
				// Remove it from the container unit
				aBottle.transfer(this);
				// Register the person as the owner of this bottle
				aBottle.setRegisteredOwner(this);
			}
		}
	}
	
	/**
	 * Drops off the thermal bottle such as when going out for an EVA.
	 */
	public void dropOffThermalBottle() {

		if (hasThermalBottle() && isInside()) {

			for(Equipment e : getEquipmentSet()) {
				if (e.getEquipmentType() == EquipmentType.THERMAL_BOTTLE) {
					// Transfer to this person's container unit 
					e.transfer(getContainerUnit());
					
					break;
				}
			}
		}
	}
	
	
	
	/**
	 * Puts on a garment.
	 *
	 * @param holder the previous holder of the clothing
	 */
	public void wearGarment(EquipmentOwner holder) {
		if (!hasGarment() && holder.retrieveItemResource(ItemResourceUtil.garmentID, 1) == 0) {
			storeItemResource(ItemResourceUtil.garmentID, 1);
		}
	}

	/**
	 * Puts on a pressure suit set.
	 *
	 * @param suit
	 */
	public void wearPressureSuit(EquipmentOwner holder) {
		if (!hasPressureSuit() && holder.retrieveItemResource(ItemResourceUtil.pressureSuitID, 1) == 0) {
			storeItemResource(ItemResourceUtil.pressureSuitID, 1);
		}
	}


	/**
	 * Puts off the garment.
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
	 * Puts off the pressure suit set.
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
	 * Rescues the person from the rover.
	 * Note: this is more like a hack, rather than a legitimate 
	 * way of transferring a person through the airlock into the settlement.			
	 *
	 * @param r the rover
	 * @param p the person
	 * @param s the settlement
	 */
	public boolean rescueOperation(Rover r, Person p, Settlement s) {
		boolean result = false;
		
		if (p.isDeclaredDead()) {
			result = p.transfer(s);
		}
		else if (r != null || p.isOutside()) {
			result = p.transfer(s);
		}

		EVAOperation.send2Medical(p, s);
		
		return result;
	}
	
	/**
	 * Gets the relation instance.
	 * 
	 * @return
	 */
	public Relation getRelation( ) {
		return mind.getRelation();
	}
	
	public void setMeetingInitiator(int initiatorId) {
		this.initiatorId = initiatorId;
	}
	
	public void setMeetingInvitee(int inviteeId) {
		this.inviteeId = inviteeId;
	}
	
	public int getMeetingInitiator() {
		return initiatorId;
	}
	
	public int getMeetingInvitee() {
		return inviteeId;
	}
	
	public ScheduleManager getScheduleManager() {
		return scheduleManager;
	}
	
	/**
	 * Reinitialize references after loading from a saved sim.
	 */
	public void reinit() {
		mind.reinit();
		condition.reinit();
	}

	/**
	 * Compares if an object is the same as this person.
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
