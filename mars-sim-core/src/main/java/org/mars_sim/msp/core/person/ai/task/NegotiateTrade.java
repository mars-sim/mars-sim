/**
 * Mars Simulation Project
 * NegotiateTrade.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.TradeUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.vehicle.Rover;

/**
 * Task to perform a trade negotiation between the buyer and seller for a Trade
 * mission.
 */
public class NegotiateTrade extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(NegotiateDelivery.class.getName());

	/** Task name */
	private static final String NAME = Msg.getString("Task.description.negotiateTrade"); //$NON-NLS-1$

	/** Task phases. */
	private static final TaskPhase NEGOTIATING = new TaskPhase(Msg.getString("Task.phase.negotiating")); //$NON-NLS-1$

	/** The predetermined duration of task in millisols. */
	private static final double DURATION = 50D;
	/** The stress modified per millisol. */
	private static final double STRESS_MODIFIER = 0D;

	// Data members.
	private Map<Good, Integer> buyLoad;
	private Settlement sellingSettlement;
	private Settlement buyingSettlement;
	private Rover rover;
	private Map<Good, Integer> soldLoad;
	private Worker buyingTrader;
	private Worker sellingTrader;

	/**
	 * Constructor.
	 * 
	 * @param sellingSettlement the selling settlement.
	 * @param buyingSettlement  the buying settlement.
	 * @param rover             the rover to transport the goods.
	 * @param soldLoad          the goods sold.
	 * @param buyingTrader      the buying trader.
	 * @param sellingTrader     the selling trader.
	 */
	public NegotiateTrade(Settlement sellingSettlement, Settlement buyingSettlement, Rover rover,
			Map<Good, Integer> soldLoad, Worker buyingTrader, Worker sellingTrader) {

		// Use trade constructor.
		super(NAME, buyingTrader, false, false, STRESS_MODIFIER, SkillType.TRADING, 100D, DURATION);

		// Initialize data members.
		this.sellingSettlement = sellingSettlement;
		this.buyingSettlement = buyingSettlement;
		this.rover = rover;
		this.soldLoad = soldLoad;
		this.buyingTrader = buyingTrader;
		this.sellingTrader = sellingTrader;

		// Initialize task phase.
		addPhase(NEGOTIATING);
		setPhase(NEGOTIATING);
	}

//    public NegotiateTrade(Settlement sellingSettlement, Settlement buyingSettlement, Rover rover, 
//            Map<Good, Integer> soldLoad, Person buyingTrader, Person sellingTrader) {
//
//        // Use trade constructor.
//        super(NAME, buyingTrader, false, false, STRESS_MODIFIER, true, DURATION);
//
//        // Initialize data members.
//        this.sellingSettlement = sellingSettlement;
//        this.buyingSettlement = buyingSettlement;
//        this.rover = rover;
//        this.soldLoad = soldLoad;
//        this.buyingTrader = buyingTrader;
//        this.sellingTrader = sellingTrader;
//
//        // Initialize task phase.
//        addPhase(NEGOTIATING);
//        setPhase(NEGOTIATING);
//    }
//    public NegotiateTrade(Settlement sellingSettlement, Settlement buyingSettlement, Rover rover, 
//            Map<Good, Integer> soldLoad, Robot buyingRobotTrader, Robot sellingRobotTrader) {
//
//        // Use trade constructor.
//        super(NAME, buyingRobotTrader, false, false, STRESS_MODIFIER, true, DURATION);
//
//        // Initialize data members.
//        this.sellingSettlement = sellingSettlement;
//        this.buyingSettlement = buyingSettlement;
//        this.rover = rover;
//        this.soldLoad = soldLoad;
//        this.buyingRobotTrader = buyingRobotTrader;
//        this.sellingRobotTrader = sellingRobotTrader;
//
//        // Initialize task phase.
//        addPhase(NEGOTIATING);
//        setPhase(NEGOTIATING);
//    }

	/**
	 * Performs the negotiating phase of the task.
	 * 
	 * @param time the amount (ms) of time the person is performing the phase.
	 * @return time remaining after performing the phase.
	 */
	private double negotiatingPhase(double time) {

		// Follow selling trader to his/her building if necessary.
		followSeller();

		// If duration, complete trade.
		if (getDuration() <= (getTimeCompleted() + time)) {

			double tradeModifier = determineTradeModifier();

			// Get the value of the load that is being sold to the destination settlement.
			double baseSoldLoadValue = TradeUtil.determineLoadValue(soldLoad, sellingSettlement, true);
			double soldLoadValue = baseSoldLoadValue * tradeModifier;

			// Get the credit that the starting settlement has with the destination
			// settlement.
			CreditManager creditManager = Simulation.instance().getCreditManager();
			double credit = creditManager.getCredit(buyingSettlement, sellingSettlement);
			credit += soldLoadValue;
			creditManager.setCredit(buyingSettlement, sellingSettlement, credit);

			logger.log(person, Level.INFO, 0, 
					"Completed a trade negotiation as follows: "
					+ "  Buyer: " + buyingSettlement.getName() 
					+ "  Seller: " + sellingSettlement.getName()
					+ "  Credit: " + Math.round(credit* 10.0)/10.0 
					+ "  Mod: " + Math.round(tradeModifier * 10.0)/10.0
					);

			// Check if buying settlement owes the selling settlement too much for them to
			// sell.
			if (credit > (-1D * TradeUtil.SELL_CREDIT_LIMIT)) {

				// Determine the initial buy load based on goods that are profitable for the
				// destination settlement to sell.
				buyLoad = TradeUtil.determineLoad(buyingSettlement, sellingSettlement, rover, Double.POSITIVE_INFINITY);
				double baseBuyLoadValue = TradeUtil.determineLoadValue(buyLoad, buyingSettlement, true);
				double buyLoadValue = baseBuyLoadValue / tradeModifier;

				// Update the credit value between the starting and destination settlements.
				credit -= buyLoadValue;
				creditManager.setCredit(buyingSettlement, sellingSettlement, credit);
				
				logger.log(person, Level.INFO, 0,
						"Updated the account ledger as follows: "
						+ "  Credit: " + Math.round(credit * 10.0)/10.0
						+ "  Mod: " + Math.round(tradeModifier * 10.0)/10.0
						);
			} else {
				buyLoad = new HashMap<Good, Integer>(0);
			}
		}

		return getTimeCompleted() + time - getDuration();
	}

	/**
	 * Has the buying trader follow the selling trader if he/she has moved to a
	 * different building.
	 */
	private void followSeller() {
		Building sellerBuilding = null;
		Building building = null;
		Person person = null;
		Robot robot = null;

		if (sellingTrader instanceof Person) {
			person = (Person) sellingTrader;
			sellerBuilding = BuildingManager.getBuilding(person);
			building = BuildingManager.getBuilding(person);
		} else if (sellingTrader instanceof Robot) {
			robot = (Robot) sellingTrader;
			sellerBuilding = BuildingManager.getBuilding(robot);
			building = BuildingManager.getBuilding(robot);
		}

		if ((sellerBuilding != null) && (!sellerBuilding.equals(building))) {
			// Walk to seller trader's building.
			walkToRandomLocInBuilding(sellerBuilding, false);
		}
	}

	/**
	 * Determines the trade modifier based on the traders' abilities.
	 * 
	 * @return trade modifier.
	 */
	private double determineTradeModifier() {

		double modifier = 1D;
		// Note: buying and selling traders are reversed here since this is regarding
		// the goods
		// that the buyer is selling and the seller is buying.
		NaturalAttributeManager sellerAttributes = sellingTrader.getNaturalAttributeManager();
		// Modify by 10% for conversation natural attributes in buyer and seller.
		modifier += sellerAttributes.getAttribute(NaturalAttributeType.CONVERSATION) / 1000D;
		// Modify by 10% for attractiveness natural attributes in buyer and seller.
		// Robots have zero ATTRACTIVENESS !!!!
		modifier += sellerAttributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS) / 1000D;

		NaturalAttributeManager buyerAttributes = buyingTrader.getNaturalAttributeManager();
		// Modify by 10% for conversation natural attributes in buyer and seller.
		modifier -= buyerAttributes.getAttribute(NaturalAttributeType.CONVERSATION) / 1000D;
		// Modify by 10% for attractiveness natural attributes in buyer and seller.
		modifier -= buyerAttributes.getAttribute(NaturalAttributeType.ATTRACTIVENESS) / 1000D;

		// Modify by 10% for each skill level in trading for buyer and seller.
		modifier += buyingTrader.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING) / 10D;
		modifier += sellingTrader.getSkillManager().getEffectiveSkillLevel(SkillType.TRADING) / 10D;

		// Modify by 10% for the relationship between the buyer and seller.
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		if (buyingTrader instanceof Person && sellingTrader instanceof Person) {
			Person person1 = (Person) buyingTrader;
			Person person2 = (Person) sellingTrader;
			modifier += relationshipManager.getOpinionOfPerson(person1, person2) / 1000D;
		}

		if (buyingTrader instanceof Person && sellingTrader instanceof Person) {
			Person person1 = (Person) sellingTrader;
			Person person2 = (Person) buyingTrader;
			modifier += relationshipManager.getOpinionOfPerson(person1, person2) / 1000D;
		}

		return modifier;
	}

	@Override
	protected void addExperience(double time) {
		// Add experience for the buying trader.
		addExperience(time, buyingTrader);
		// Add experience for the selling trader.
		addExperience(time, sellingTrader);
	}

	/**
	 * Adds experience to the trading skill for a trader involved in the
	 * negotiation.
	 * 
	 * @param time   the amount of time (ms) the task is performed.
	 * @param trader the trader to add the experience to.
	 */
	private void addExperience(double time, Worker trader) {
		// Add experience to "Trading" skill for the trader.
		// (1 base experience point per 2 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
		double newPoints = time / 2D;
		int experienceAptitude = 0;

		experienceAptitude = trader.getNaturalAttributeManager()
				.getAttribute(NaturalAttributeType.EXPERIENCE_APTITUDE);
		newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		trader.getSkillManager().addExperience(SkillType.TRADING, newPoints, time);
	}

	@Override
	protected double performMappedPhase(double time) {
		if (getPhase() == null) {
			throw new IllegalArgumentException("Task phase is null");
		} else if (NEGOTIATING.equals(getPhase())) {
			return negotiatingPhase(time);
		} else {
			return time;
		}
	}

	/**
	 * Gets the buy load for the trade.
	 * 
	 * @return buy load or null if not determined yet.
	 */
	public Map<Good, Integer> getBuyLoad() {
		return buyLoad;
	}
}
