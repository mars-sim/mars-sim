/*
 * Mars Simulation Project
 * RobotGood.java
 * @date 2025-07-23
 * @author Barry Evans
 */
package com.mars_sim.core.goods;

import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.resource.ItemResourceUtil;
import com.mars_sim.core.robot.Robot;
import com.mars_sim.core.robot.RobotType;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.tool.MathUtils;

/**
 * This class represents how a Robot can be traded.
 */
class RobotGood extends Good {
	
	private static final long serialVersionUID = 1L;
	
    private static final double INITIAL_ROBOT_DEMAND = 2;
    private static final double INITIAL_ROBOT_SUPPLY = 2;

    private static final int BASE_DEMAND = 3;
	private static final int ROBOT_COST_MOD = 200;
	
	private static final double ROBOT_FLATTENING_FACTOR = 1.1;
	
	/** The fixed flatten demand for this resource. */
	private double flattenDemand;
	/** The projected demand of each refresh cycle. */
	private double projectedDemand;
	/** The trade demand for this resource of each refresh cycle. */
	private double tradeDemand;
	/** The repair demand for this resource of each refresh cycle. */
	private double repairDemand;
	
    private RobotType robotType;

    public RobotGood(RobotType type) {
        super(type.getName(), RobotType.getResourceID(type));
        this.robotType = type;
        
        // Calculate fixed values
     	flattenDemand = ROBOT_FLATTENING_FACTOR;
    }
	
    /**
     * Gets the flattened demand.
     * 
     * @return
     */
	@Override
    public double getFlattenDemand() {
    	return flattenDemand;
    }
    
    /**
     * Gets the projected demand of this resource.
     * 
     * @return
     */
	@Override
    public double getProjectedDemand() {
    	return projectedDemand;
    }
	
    /**
     * Gets the trade demand of this resource.
     * 
     * @return
     */
	@Override
    public double getTradeDemand() {
    	return tradeDemand;
    }
	
    /**
     * Gets the repair demand of this resource.
     * 
     * @return
     */
	@Override
    public double getRepairDemand() {
    	return repairDemand;
    }
	
    @Override
    public GoodCategory getCategory() {
        return GoodCategory.ROBOT;
    }

    @Override
    public double getMassPerItem() {
    	for (Robot r: unitManager.getRobots()) {
    		if (robotType == r.getRobotType()) {
    			return r.getBaseMass();
    		}
    	}
    	
        return Robot.EMPTY_MASS;
    }

    @Override
    public GoodType getGoodType() {
        switch(robotType) {
            case CHEFBOT: return GoodType.CHEFBOT;
            case CONSTRUCTIONBOT: return GoodType.CONSTRUCTIONBOT;
            case DELIVERYBOT: return GoodType.DELIVERYBOT;
            case GARDENBOT: return GoodType.GARDENBOT;
            case MAKERBOT: return GoodType.MAKERBOT;
            case MEDICBOT: return GoodType.MEDICBOT;
            case REPAIRBOT: return GoodType.REPAIRBOT;
        }

        throw new IllegalStateException("Cannot mapt robot type " + robotType + " to GoodType");
    }

    @Override
    protected double computeCostModifier() {
        return ROBOT_COST_MOD;
    }

    @Override
    public double getNumberForSettlement(Settlement settlement) {
		// Get number of robots.
		return (int) settlement.getAllAssociatedRobots().stream()
                        .filter( r -> r.getRobotType() == robotType)
                        .count();	
    }

    @Override
    double calculatePrice(Settlement settlement, double value) {
//        double mass = getMassPerItem();
//        double quantity = settlement.getInitialNumOfRobots() ;
        double supply = settlement.getGoodsManager().getSupplyScore(getID());
        double factor = 2 / (2 + supply);
        // Need to increase the value for robots
        double price = getCostOutput() * (1 + factor * Math.log(Math.sqrt(value) + 1));  
        setPrice(price);
	    return price;
    }

    @Override
    double getDefaultDemandValue() {
        return INITIAL_ROBOT_DEMAND;
    }

    @Override
    double getDefaultSupplyValue() {
        return INITIAL_ROBOT_SUPPLY;
    }

    @Override
    void refreshSupplyDemandScore(GoodsManager owner) {
		Settlement settlement = owner.getSettlement();

		double previousDemand = owner.getDemandScore(this);

		double totalDemand = 0;
		
		// Determine projected demand for this cycle
		double newProjDemand = determineRobotDemand(owner, settlement);

		newProjDemand = MathUtils.between(newProjDemand, LOWEST_PROJECTED_VALUE, HIGHEST_PROJECTED_VALUE);
		
		double projected = newProjDemand * flattenDemand;
		
		this.projectedDemand = .1 * projected + .9 * this.projectedDemand;
		
		double totalSupply = getNumberForSettlement(settlement);
				
		owner.setSupplyScore(this, totalSupply);
		
		// This method is not using cache
		tradeDemand = owner.determineTradeDemand(this);
		
		// Gets the repair part demand
		// Note: need to look into parts reliability in MalfunctionManager to derive the repair value 
		repairDemand = (owner.getMaintenanceLevel() + owner.getRepairLevel())/2.0 
				* owner.getDemandScore(this);
	
		
		if (previousDemand == 0) {
			totalDemand = .5 * projected 
						+ .1 * repairDemand
						+ .4 * tradeDemand;
		}
		else {
			// Intentionally lose 2% of its value
			totalDemand = .97 * previousDemand 
						+ .005 * projected
						+ .005 * repairDemand
						+ .005 * tradeDemand;
		}
				
		owner.setDemandScore(this, totalDemand);
    }
    
	/**
	 * Determines the demand for a robot type.
	 *
	 * @param settlement the location of this demand
	 * @return demand
	 */
	private double determineRobotDemand(GoodsManager owner, Settlement settlement) {
		double baseDemand = BASE_DEMAND * getWholeBotDemand(owner)
				 + owner.getBotMod();
				
		if (robotType == RobotType.MAKERBOT) {
			
			int tech = JobUtil.numJobs(JobType.TECHNICIAN, settlement);
			int engineer = JobUtil.numJobs(JobType.ENGINEER, settlement);
			int comp = JobUtil.numJobs(JobType.COMPUTER_SCIENTIST, settlement);
			double makerFactor = .5 * engineer + .25 * tech + .1 * comp;
			
			baseDemand += baseDemand / (.5 + makerFactor);
		}
		
		else if (robotType == RobotType.REPAIRBOT) {
			int tech = JobUtil.numJobs(JobType.TECHNICIAN, settlement);
			int engineer = JobUtil.numJobs(JobType.ENGINEER, settlement);
			double repairFactor = .5 * tech + .25 * engineer;
			
			baseDemand += baseDemand / (.5 + repairFactor);
		}
		
		else if (robotType == RobotType.CONSTRUCTIONBOT) {
			int engineer = JobUtil.numJobs(JobType.ENGINEER, settlement);
			int comp = JobUtil.numJobs(JobType.COMPUTER_SCIENTIST, settlement);
			int architect = JobUtil.numJobs(JobType.ARCHITECT, settlement);
			double constructFactor = .5 * architect + .25 * engineer + .15 * comp;
			
			baseDemand += baseDemand / (.5 + constructFactor);
		}
		
		else if (robotType == RobotType.GARDENBOT) {
			int botanistFactor = JobUtil.numJobs(JobType.BOTANIST, settlement);
			
			baseDemand += baseDemand / (.5 + botanistFactor);
		}
		
		else if (robotType == RobotType.CHEFBOT) {
			int chiefFactor = JobUtil.numJobs(JobType.CHEF, settlement);
			
			baseDemand += baseDemand / (.5 + chiefFactor);
		}
		
		else if (robotType == RobotType.DELIVERYBOT) {
			int trader = JobUtil.numJobs(JobType.TRADER, settlement);
			int pilot = JobUtil.numJobs(JobType.PILOT, settlement);
			double traderFactor = .5 + .75 * trader + .25 * pilot;
			
			baseDemand += baseDemand / (.5 + traderFactor);
		}
		
		else if (robotType == RobotType.MEDICBOT) {
			int doc = JobUtil.numJobs(JobType.DOCTOR, settlement);
			int psy = JobUtil.numJobs(JobType.PSYCHOLOGIST, settlement);
			double medicFactor = .5 + .75 * doc + .25 * psy;
			
			baseDemand += baseDemand / (.5 + medicFactor);
		}
		
		return baseDemand * settlement.getPopulationFactor();
	}

	/**
	 * Gets the bot demand from its part.
	 *
	 * @param owner Owner of Goods
	 * @return demand
	 */
	private static double getWholeBotDemand(GoodsManager owner) {
		double demand = 0;
	
		if (ItemResourceUtil.botPartIDs != null && !ItemResourceUtil.botPartIDs.isEmpty()) {
			for (int id : ItemResourceUtil.botPartIDs) {
				demand += owner.getDemandScoreWithID(id);
			}
		}
		return demand;
	}	
	
}
