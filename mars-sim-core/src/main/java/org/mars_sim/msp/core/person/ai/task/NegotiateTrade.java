/**
 * Mars Simulation Project
 * NegotiateTrade.java
 * @version 3.02 2012-06-03
 * @author Scott Davis
 */

package org.mars_sim.msp.core.person.ai.task;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.NaturalAttributeManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Skill;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.mission.TradeUtil;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.structure.goods.CreditManager;
import org.mars_sim.msp.core.structure.goods.Good;
import org.mars_sim.msp.core.vehicle.Rover;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Task to perform a trade negotiation between the buyer and seller for a Trade mission.
 */
public class NegotiateTrade extends Task implements Serializable {

    private static Logger logger = Logger.getLogger(NegotiateTrade.class.getName());
    
	// Task phase
	private static final String NEGOTIATING = "Negotiating";
	private static final double DURATION = 50D; // The predetermined duration of task in millisols
	private static final double STRESS_MODIFIER = 0D; // The stress modified per millisol.
	
	// Data members.
	private Map<Good, Integer> buyLoad;
	private Settlement sellingSettlement;
	private Settlement buyingSettlement;
	private Rover rover;
	private Map<Good, Integer> soldLoad;
	private Person buyingTrader;
	private Person sellingTrader;
	
	/**
	 * Constructor
	 * @param sellingSettlement the selling settlement.
	 * @param buyingSettlement the buying settlement.
	 * @param rover the rover to transport the goods.
	 * @param soldLoad the goods sold.
	 * @param buyingTrader the buying trader.
	 * @param sellingTrader the selling trader.
	 * @throws Exception if error creating the task.
	 */
	public NegotiateTrade(Settlement sellingSettlement, Settlement buyingSettlement, Rover rover, 
			Map<Good, Integer> soldLoad, Person buyingTrader, Person sellingTrader) {
		
		// Use trade constructor.
		super("Negotiation Trade", buyingTrader, false, false, STRESS_MODIFIER, true, DURATION);
		
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
	
	/**
	 * Performs the negotiating phase of the task.
	 * @param time the amount (ms) of time the person is performing the phase.
	 * @return time remaining after performing the phase.
	 * @throws Exception if error performing the phase.
	 */
	private double negotiatingPhase(double time) {
		
		// Follow selling trader to his/her building if necessary.
		followSeller();
		
		// If duration, complete trade.
		if (getDuration() < (getTimeCompleted() + time)) {
            
		    double soldLoadValue = determineModifiedSoldLoadValue();
		    
		    CreditManager creditManager = Simulation.instance().getCreditManager();
            double credit = creditManager.getCredit(buyingSettlement, sellingSettlement);
		    
		    double maxBuyLoadValue = soldLoadValue - credit;
		    
		    buyLoad = TradeUtil.determineLoad(buyingSettlement, sellingSettlement, rover, maxBuyLoadValue);
            double buyLoadValue = TradeUtil.determineLoadValue(buyLoad, buyingSettlement, true);
            
            credit += soldLoadValue - buyLoadValue;
            creditManager.setCredit(buyingSettlement, sellingSettlement, credit);
            logger.info("Credit at " + buyingSettlement.getName() + " for " + sellingSettlement.getName() + 
                    " is " + credit);
		}
		
		return getTimeCompleted() + time - getDuration();
	}
	
	/**
	 * Has the buying trader follow the selling trader if he/she has moved to a different building.
	 * @throws BuildingException if error following the selling trader.
	 */
	private void followSeller() {
		Building buyerBuilding = BuildingManager.getBuilding(buyingTrader);
		Building sellerBuilding = BuildingManager.getBuilding(sellingTrader);
		if ((buyerBuilding != sellerBuilding) && (sellerBuilding != null)) 
			BuildingManager.addPersonToBuilding(buyingTrader, sellerBuilding);
	}
	
	/**
	 * Determines the modified sold load value based on the traders' abilities.
	 * @return value (value points)
	 * @throws Exception if error determining modified sold load value.
	 */
	private double determineModifiedSoldLoadValue() {
		
		double modifier = 1D;
		
		// Note: buying and selling traders are reversed here since this is regarding the goods
		// that the buyer is selling and the seller is buying.
		NaturalAttributeManager sellerAttributes = buyingTrader.getNaturalAttributeManager();
		NaturalAttributeManager buyerAttributes = sellingTrader.getNaturalAttributeManager();
		
		// Modify by 10% for conversation natural attributes in buyer and seller.
		modifier += sellerAttributes.getAttribute(NaturalAttributeManager.CONVERSATION) / 1000D;
		modifier -= buyerAttributes.getAttribute(NaturalAttributeManager.CONVERSATION) / 1000D;
		
		// Modify by 10% for attractiveness natural attributes in buyer and seller.
		modifier += sellerAttributes.getAttribute(NaturalAttributeManager.ATTRACTIVENESS) / 1000D;
		modifier -= buyerAttributes.getAttribute(NaturalAttributeManager.ATTRACTIVENESS) / 1000D;
		
		// Modify by 10% for each skill level in trading for buyer and seller.
		modifier += buyingTrader.getMind().getSkillManager().getEffectiveSkillLevel(Skill.TRADING) / 10D;
		modifier -= sellingTrader.getMind().getSkillManager().getEffectiveSkillLevel(Skill.TRADING) / 10D;
		
		// Modify by 10% for the relationship between the buyer and seller.
		RelationshipManager relationshipManager = Simulation.instance().getRelationshipManager();
		modifier += relationshipManager.getOpinionOfPerson(buyingTrader, sellingTrader) / 1000D;
		modifier -= relationshipManager.getOpinionOfPerson(sellingTrader, buyingTrader) / 1000D;
		
		// Get sold load value.
		double soldLoadValue = TradeUtil.determineLoadValue(soldLoad, sellingSettlement, true);
		
		return soldLoadValue * modifier;
	}

	/**
	 * Adds experience to the person's skills used in this task.
	 * @param time the amount of time (ms) the person performed this task.
	 */
	protected void addExperience(double time) {
		// Add experience for the buying trader.
		addExperience(time, buyingTrader);
		
		// Add experience for the selling trader.
		addExperience(time, sellingTrader);
	}
	
	/**
	 * Adds experience to the trading skill for a trader involved in the negotiation.
	 * @param time the amount of time (ms) the task is performed.
	 * @param trader the trader to add the experience to.
	 */
	private void addExperience(double time, Person trader) {
		// Add experience to "Trading" skill for the trader.
		// (1 base experience point per 2 millisols of work)
		// Experience points adjusted by person's "Experience Aptitude" attribute.
        double newPoints = time / 2D;
        int experienceAptitude = trader.getNaturalAttributeManager().getAttribute(
        	NaturalAttributeManager.EXPERIENCE_APTITUDE);
        newPoints += newPoints * ((double) experienceAptitude - 50D) / 100D;
		newPoints *= getTeachingExperienceModifier();
		trader.getMind().getSkillManager().addExperience(Skill.TRADING, newPoints);
	}

	/**
	 * Gets a list of the skills associated with this task.
	 * May be empty list if no associated skills.
	 * @return list of skills as strings
	 */
	public List<String> getAssociatedSkills() {
		List<String> skills = new ArrayList<String>(1);
		skills.add(Skill.TRADING);
		return skills;
	}

	/**
	 * Gets the effective skill level a person has at this task.
	 * @return effective skill level
	 */
	public int getEffectiveSkillLevel() {
		SkillManager manager = person.getMind().getSkillManager();
		return manager.getEffectiveSkillLevel(Skill.TRADING);
	}

    /**
     * Performs the method mapped to the task's current phase.
     * @param time the amount of time (millisol) the phase is to be performed.
     * @return the remaining time (millisol) after the phase has been performed.
     * @throws Exception if error in performing phase or if phase cannot be found.
     */
	protected double performMappedPhase(double time) {
    	if (getPhase() == null) throw new IllegalArgumentException("Task phase is null");
    	if (NEGOTIATING.equals(getPhase())) return negotiatingPhase(time);
    	else return time;
	}
	
	/**
	 * Gets the buy load for the trade.
	 * @return buy load or null if not determined yet.
	 */
	public Map<Good, Integer> getBuyLoad() {
		return buyLoad;
	}
	
	@Override
	public void destroy() {
	    super.destroy();
	    
	    if (buyLoad != null) buyLoad.clear();
	    buyLoad = null;
	    sellingSettlement = null;
	    buyingSettlement = null;
	    rover = null;
	    if (soldLoad != null) soldLoad.clear();
	    soldLoad = null;
	    buyingTrader = null;
	    sellingTrader = null;
	}
}