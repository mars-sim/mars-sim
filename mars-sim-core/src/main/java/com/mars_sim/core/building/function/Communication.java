/*
 * Mars Simulation Project
 * Communication.java
 * @date 2025-08-06
 * @author Scott Davis
 */
package com.mars_sim.core.building.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.FunctionSpec;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.structure.Settlement;

/**
 * The Communication class is a building function for communication.
 */
public class Communication extends Function {
	
    /** default serial id. */
    private static final long serialVersionUID = 1L;
  
	/** default logger. */
	private static final SimLogger logger = SimLogger.getLogger(Communication.class.getName());
	
	public class Band implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		private int bandwidth;
		private String name;
		
		public Band(String name, int bandwidth) {
			this.name = name;
			this.bandwidth = bandwidth;
		}
	}
	
	public class Channel implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private ChannelType channelType;
		
		private List<Band> bands = new ArrayList<>();
		
		public Channel(ChannelType channelType) {
			this.channelType = channelType;
			
			if (channelType == ChannelType.A) {
				int size = 4;
				for (int i=1; i<=size; i++) {
					Band b = new Band(i + "", 25);
					bands.add(b);
				}
			}
			else if (channelType == ChannelType.B) {
				int size = 8;
				for (int i=1; i<=size; i++) {
					Band b = new Band((i + 4) + "", 2 * 25);
					bands.add(b);
				}
			}
			else if (channelType == ChannelType.C) {
				int size = 16;
				for (int i=1; i<=size; i++) {
					Band b = new Band((i + 12) + "", 4 * 25);
					bands.add(b);
				}
			}
			else if (channelType == ChannelType.D) {
				int size = 32;
				for (int i=1; i<=size; i++) {
					Band b = new Band((i + 28) + "", 8 * 25);
					bands.add(b);
				}
			}
			else if (channelType == ChannelType.E) {
				int size = 64;
				for (int i=1; i<=size; i++) {
					Band b = new Band((i + 60) + "", 16 * 25);
					bands.add(b);
				}
			}
		}
		
		public int getNumBand() {
			return bands.size();
		}
	}

	/**
	 * Available communication modes.
	 */
	public enum CommMode {
		PRIORITY 	("priority"),
		EMERGENCY 	("Emergency"), 
		IMMEDIATE	("Immediate"),		
		ROUTINE		("Routine");
		
		private String name;

		private CommMode(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
    
	public enum ChannelType {
		A 	("A"), 
		B	("B"),		
		C	("C"),
		D	("D"),
		E	("E");
	
		private String name;

		private ChannelType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
	}
		
	private int techLevel;
	/** The current overall efficiency of power usage. */
	private double powerEfficiency;
	/** The power load in kW for each running CU [in kW/CU]. */
	private double powerDemand;
	/** The power load in kW needed for cooling each running CU [in kW/CU]. */
	private double coolingDemand;
	
	private List<Channel> availableChannels = new ArrayList<>();
	
	static {
		
	}
	
    /**
     * Constructor.
     * 
     * @param building the building this function is for.
     */
    public Communication(Building building, FunctionSpec spec) {
        // Use Function constructor.
        super(FunctionType.COMMUNICATION, spec, building);
        
        techLevel = spec.getTechLevel();
        
        if (techLevel >= 1) {
        	Channel a = new Channel(ChannelType.A);
        	availableChannels.add(a);
        }
        if (techLevel >= 2) {
        	Channel b = new Channel(ChannelType.B);
        	availableChannels.add(b);
        }
        if (techLevel >= 3) {
        	Channel c = new Channel(ChannelType.C);
        	availableChannels.add(c);
        }
        if (techLevel >= 4) {
        	Channel d = new Channel(ChannelType.D);
        	availableChannels.add(d);
        }
        if (techLevel >= 5) {
        	Channel e = new Channel(ChannelType.E);
        	availableChannels.add(e);
        }
        
        int total = 0;
        for (Channel c: availableChannels) {
        	total += c.getNumBand();
        }
        
        logger.config(getBuilding(), "comm channels: " + availableChannels.size()
        		+ "  comm bands: " + total);
    }

    /**
     * Gets the value of the function for a named building.
     * @param buildingName the building name.
     * @param newBuilding true if adding a new building.
     * @param settlement the settlement.
     * @return value (VP) of building function.
     * @throws Exception if error getting function value.
     */
    public static double getFunctionValue(String buildingName, boolean newBuilding,
            Settlement settlement) {

        // Settlements need one communication building.
        // Note: Might want to update this when we do more with simulating communication.
        double demand = 1D;

        // Supply based on wear condition of buildings.
        double supply = 0D;
        Iterator<Building> i = settlement.getBuildingManager().getBuildingSet(FunctionType.COMMUNICATION).iterator();
        while (i.hasNext()) {
            supply += (i.next().getMalfunctionManager().getWearCondition() / 100D) * .75D + .25D;
        }

        if (!newBuilding) {
            supply -= 1D;
            if (supply < 0D) supply = 0D;
        }

        return demand / (supply + 1D);
    }

    @Override
    public double getMaintenanceTime() {
        return 10;
    }
    
	
	@Override
	public void destroy() {
		availableChannels.clear();
		availableChannels = null;
		super.destroy();
	}

}

