/**
 * Mars Simulation Project
 * BeeHive.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.core.structure.building.function.farming;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.Function;

public class BeeHive
extends Function
implements Serializable {
	
	/** default serial id. */
    private static final long serialVersionUID = 1L;
    /** default logger. */
	private static Logger logger = Logger.getLogger(BeeHive.class.getName());

    private static final FunctionType FUNCTION = FunctionType.FARMING;

    public static final int QUEEN = 0;
    public static final int WORKER = 1;
    public static final int MALE = 2;
    public static final int EGG = 3;
    public static final int LARVAE = 4;
    public static final int PUPA = 5;
    
    public static final int male = 0;
    public static final int female = 1;
    
    
    private Inventory inv;
    private Settlement settlement;
    private Building building;
    private Farming farm;
    private BeeGrowing beeGrowing;
    
    private double propolisAmount;
    private double beeswaxAmount;
    private double honeycombCells;
    private double honeyAmount;
    
    private String beeSpecies;

    private Bee queen;        
    private Bee workerBee;
    private Bee maleBee;       
    private Bee egg; 
    private Bee larvae;
    private Bee pupa;
    

    public BeeHive(BeeGrowing beeGrowing, String beeSpecies) {
        // Use Function constructor.
        super(FUNCTION, beeGrowing.getFarming().getBuilding());
		
    	this.beeGrowing = beeGrowing;
    	this.beeSpecies = beeSpecies;
        this.farm = beeGrowing.getFarming();      
        this.building = farm.getBuilding();		
        this.inv = building.getSettlementInventory();
        this.settlement = building.getSettlement();
        		
        queen = new Bee(this, QUEEN, beeSpecies);        
        //workerBee = new Bee(this, WORKER);
        //maleBee = new Bee(this, MALE);       
        //egg = new Bee(this, EGG); 
        larvae = new Bee(this, LARVAE, beeSpecies);
        //pupa = new Bee(this, PUPA);
        
	}


    public BeeGrowing getBeeGrowing() {
    	return beeGrowing;
    }
    
	@Override
	public double getMaintenanceTime() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void timePassing(double time) {		
        if (queen !=null) queen.timePassing(time);        
        if (workerBee !=null) workerBee.timePassing(time);
        if (maleBee !=null) maleBee.timePassing(time);      
        //if (egg !=null) egg.timePassing(time);
        if (larvae !=null) larvae.timePassing(time);
        if (pupa !=null) pupa.timePassing(time);
	}

	public Bee getQueen() {
		return queen;
	}

	public Bee getWorkerBee() {
		return workerBee;
	}

	public Bee getMaleBee() {
		return maleBee;
	}

	public Bee getLarvae() {
		return larvae;
	}

	public Bee getPupa() {
		return pupa;
	}	

	@Override
	public double getFullHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getPoweredDownHeatRequired() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getFullPowerRequired() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double getPoweredDownPowerRequired() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
