/*
 * Mars Simulation Project
 * NegotiateTrade.java
 * @date 2022-06-11
 * @author Scott Davis
 */
package com.mars_sim.core.mission.task;

import java.util.Map;

import com.mars_sim.core.UnitType;
import com.mars_sim.core.building.BuildingManager;
import com.mars_sim.core.goods.CommerceUtil;
import com.mars_sim.core.goods.Good;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.NaturalAttributeManager;
import com.mars_sim.core.person.ai.NaturalAttributeType;
import com.mars_sim.core.person.ai.SkillType;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.task.util.Task;
import com.mars_sim.core.person.ai.task.util.TaskPhase;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.core.vehicle.Vehicle;

/**
 * Task to perform a trade negotiation between the buyer and seller for a Trade
 * mission.
 */
public class NegotiateTrade extends Task {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(NegotiateTrade.class.getName());

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
	private Vehicle delivery;
	private Map<Good, Integer> soldLoad;
	private Worker buyingTrader;
	private Worker sellingTrader;
	private boolean inPerson;

	/**
	 * Constructor.
	 * 
	 * @param buyingTrader      the buying trader.
	 * @param sellingTrader     the selling trader.
	 * @param inPerson			Do negiotation in person
	 * @param delivery          the vehicle to transport the goods.
	 * @param soldLoad          the goods sold.

	 */
	public NegotiateTrade(Worker buyingTrader, Worker sellingTrader, boolean inPerson, Vehicle delivery,
			Map<Good, Integer> soldLoad) {

		// Use trade constructor.
		super(NAME, buyingTrader, false, false, STRESS_MODIFIER, SkillType.TRADING, 100D, DURATION);

		// Initialize data members.
		this.delivery = delivery;
		this.soldLoad = soldLoad;
		this.buyingTrader = buyingTrader;
		this.sellingTrader = sellingTrader;
		this.inPerson = inPerson;

		// Initialize task phase.
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
		if (inPerson) {
			followSeller();
		}

		// If duration, complete trade.
		if (getDuration() <= (getTimeCompleted() + time)) {
			var sellingSettlement = sellingTrader.getAssociatedSettlement();
			var buyingSettlement = buyingTrader.getAssociatedSettlement();
			double tradeModifier = determineTradeModifier();
			logger.info(person, 
					"Trade negotiation completed. "
					+ "Buyer: " + buyingSettlement.getName() 
					+ ". Seller: " + sellingSettlement.getName()
					+ ". Trade Mod: " + Math.round(tradeModifier * 10.0)/10.0);

			buyLoad = CommerceUtil.negotiateDeal(sellingSettlement, buyingSettlement, delivery, tradeModifier, soldLoad);

		}

		return 0;
	}

	/**
	 * Has the buying trader follow the selling trader if he/she has moved to a
	 * different building.
	 */
	private void followSeller() {
		var sellerBuilding = BuildingManager.getBuilding(sellingTrader);
		var building = BuildingManager.getBuilding(worker);
		
		if ((sellerBuilding != null) && (!sellerBuilding.equals(building))) {
			// Walk to seller trader's building.
			walkToEmptyActivitySpotInBuilding(sellerBuilding, false);
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
	 * Gets the buy load for the trade.
	 * 
	 * @return buy load or null if not determined yet.
	 */
	public Map<Good, Integer> getBuyLoad() {
		return buyLoad;
	}
}
