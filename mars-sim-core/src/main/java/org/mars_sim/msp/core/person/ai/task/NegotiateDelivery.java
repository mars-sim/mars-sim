/*
 * Mars Simulation Project
 * NegotiateDelivery.java
 * @date 2022-06-11
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitType;
import org.mars_sim.msp.core.goods.CreditManager;
import org.mars_sim.msp.core.goods.Good;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.NaturalAttributeManager;
import org.mars_sim.msp.core.person.ai.NaturalAttributeType;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.mission.DeliveryUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.person.ai.task.utils.Task;
import org.mars_sim.msp.core.person.ai.task.utils.TaskPhase;
import org.mars_sim.msp.core.person.ai.task.utils.Worker;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Drone;

/**
 * Task to perform a delivery negotiation between the buyer and seller for a delivery
 * mission.
 */
public class NegotiateDelivery extends Task implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(NegotiateDelivery.class.getName());
	
	/** Task name */
	private static final String NAME = Msg.getString("Task.description.negotiateDelivery"); //$NON-NLS-1$

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
	private Drone drone;
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
	public NegotiateDelivery(Settlement sellingSettlement, Settlement buyingSettlement, Drone drone,
			Map<Good, Integer> soldLoad, Worker buyingTrader, Worker sellingTrader) {

		// Use delivery constructor.
		super(NAME, buyingTrader, false, false, STRESS_MODIFIER, SkillType.TRADING, 100D, DURATION);

		// Initialize data members.
		this.sellingSettlement = sellingSettlement;
		this.buyingSettlement = buyingSettlement;
		this.drone = drone;
		this.soldLoad = soldLoad;
		this.buyingTrader = buyingTrader;
		this.sellingTrader = sellingTrader;

		// Initialize task phase.
		addPhase(NEGOTIATING);
		setPhase(NEGOTIATING);
	}


	/**
	 * Performs the negotiating phase of the task.
	 * 
	 * @param time the amount (ms) of time the person is performing the phase.
	 * @return time remaining after performing the phase.
	 */
	private double negotiatingPhase(double time) {

		// Follow selling trader to his/her building if necessary.
		followSeller();

		// If duration, complete delivery.
		if (getDuration() <= (getTimeCompleted() + time)) {

			double tradeModifier = determineTradeModifier();

			// Get the credit of the load that is being sold to the destination settlement.
			double baseSoldCredit = DeliveryUtil.determineLoadCredit(soldLoad, sellingSettlement, true);
			double soldCredit = baseSoldCredit * tradeModifier;

			// Get the credit that the starting settlement has with the destination
			// settlement.
			double credit = CreditManager.getCredit(buyingSettlement, sellingSettlement);
			credit += soldCredit;
			CreditManager.setCredit(buyingSettlement, sellingSettlement, credit);
			
			logger.log(person, Level.INFO, 0, 
					"Completed a delivery negotiation as follows: "
					+ "  Buyer: " + buyingSettlement.getName() 
					+ "  Seller: " + sellingSettlement.getName()
					+ "  Credit: " + Math.round(credit* 10.0)/10.0 
					+ "  Mod: " + Math.round(tradeModifier * 10.0)/10.0
					);

			// Check if buying settlement owes the selling settlement too much for them to
			// sell.
			if (credit > (-1D * DeliveryUtil.SELL_CREDIT_LIMIT)) {

				// Determine the initial buy load based on goods that are profitable for the
				// destination settlement to sell.
				buyLoad = DeliveryUtil.determineLoad(buyingSettlement, sellingSettlement, drone, Double.POSITIVE_INFINITY);
				double baseBuyLoadValue = DeliveryUtil.determineLoadCredit(buyLoad, buyingSettlement, true);
				double buyLoadValue = baseBuyLoadValue / tradeModifier;

				// Update the credit value between the starting and destination settlements.
				credit -= buyLoadValue;
				CreditManager.setCredit(buyingSettlement, sellingSettlement, credit);
				
				logger.log(person, Level.INFO, 0,
						"Updated the account ledger as follows: "
						+ "  Credit: " + Math.round(credit * 10.0)/10.0
						+ "  Mod: " + Math.round(tradeModifier * 10.0)/10.0
						);
			} else {
				buyLoad = new HashMap<>();
			}
		}

		// Will use all the time
		return 0;
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
	 * Determines the delivery modifier based on the traders' abilities.
	 * 
	 * @return delivery modifier.
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
		if (buyingTrader.getUnitType() == UnitType.PERSON && sellingTrader.getUnitType() == UnitType.PERSON) {
			Person person1 = (Person) buyingTrader;
			Person person2 = (Person) sellingTrader;
			modifier += RelationshipUtil.getOpinionOfPerson(person1, person2) / 1000D;
			modifier += RelationshipUtil.getOpinionOfPerson(person2, person1) / 1000D;
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
		newPoints += newPoints * (experienceAptitude - 50D) / 100D;
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
	 * Gets the buy load for the delivery.
	 * 
	 * @return buy load or null if not determined yet.
	 */
	public Map<Good, Integer> getBuyLoad() {
		return buyLoad;
	}
}
