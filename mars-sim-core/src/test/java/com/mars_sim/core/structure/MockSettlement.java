package com.mars_sim.core.structure;

import java.util.Collections;

import com.mars_sim.core.map.location.Coordinates;

@SuppressWarnings("serial")
public class MockSettlement extends Settlement {

	/**
	 *
	 */
	public static final String DEFAULT_NAME = "Mock Settlement";
	public static final String SETTLEMENT_TEMPLATE = "Alpha Base";
	public static final Coordinates DEFAULT_COORDINATES = new Coordinates(Math.PI / 2D, 0);

	public MockSettlement()  {
		this(DEFAULT_NAME, false, DEFAULT_COORDINATES);
	}

	public MockSettlement(String name, boolean needGoods, Coordinates locn) {
		// Use Settlement constructor.
		super(name, locn);
					
        // Set inventory total mass capacity.
		getEquipmentInventory().addCargoCapacity(Double.MAX_VALUE);

		initialiseEssentials(needGoods, Collections.emptyList());
	}	


	@Override
	public String getTemplate() {
		return SETTLEMENT_TEMPLATE;
	}
}