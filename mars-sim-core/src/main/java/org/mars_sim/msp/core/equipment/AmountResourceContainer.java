/*
 * Mars Simulation Project
 * AmountResourceContainer.java
 * @date 2023-07-12
 * @author Manny Kung
 */

package org.mars_sim.msp.core.equipment;

import java.util.HashMap;
import java.util.Map;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.resource.AmountResource;


public class AmountResourceContainer extends BaseContainer {
	
	private Map<Integer, MicroContainer> containerMap = new HashMap<>();
	
	private int lastID = 0;
	
	public AmountResourceContainer(Unit unit, double cap) {
		super(unit, cap);
	}
	
	public Map<Integer, MicroContainer> getContainerMap() {
		return containerMap;
	}
	
	public MicroContainer getMicroContainer(int id) {
		if (containerMap.containsKey(id)) {
			return containerMap.get(id);
		}
		
		return null;
	}
	
	public void addContainer(ContainerType type, AmountResource ar, double amount) {
		lastID++;
		containerMap.put(lastID, new MicroContainer(type, lastID, ar, amount));
	}

	class MicroContainer {
			
		private int id;
		
		private double amount;

		private ContainerType type;
		
		private AmountResource ar;

		MicroContainer(ContainerType type, int id, AmountResource ar, double amount) {
			this.type = type;
			this.id = id;
			this.ar = ar;
			this.amount = amount;
		}
		
		public int getID() {
			return id;
		}
		
		public ContainerType getContainerType() {
			return type;
		}
		
		public AmountResource getAmountResource() {
			return ar;
		}
		
		public void setAmountResource(AmountResource ar) {
			this.ar = ar;
		}
		
		public double getAmount() {
			return amount;
		}
		
		public void setAmount(double value) {
			amount = value;
		}
	}

}

