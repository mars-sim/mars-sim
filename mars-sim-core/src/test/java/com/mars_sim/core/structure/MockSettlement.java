package com.mars_sim.core.structure;

import java.util.Collections;

import com.mars_sim.core.authority.Authority;
import com.mars_sim.core.map.location.Coordinates;

@SuppressWarnings("serial")
public class MockSettlement extends Settlement {

	/**
	 *
	 */
	public static final String DEFAULT_NAME = "Mock Settlement";
	public static final String SETTLEMENT_TEMPLATE = "Alpha Base";
	public static final Coordinates DEFAULT_COORDINATES = new Coordinates(Math.PI / 2D, 0);
	private Authority owner;

	public MockSettlement()  {
		this(DEFAULT_NAME, false, DEFAULT_COORDINATES, null);
	}

	public MockSettlement(String name, boolean needGoods, Coordinates locn, Authority owner) {
		// Use Settlement constructor.
		super(name, locn);
					
        // Set inventory total mass capacity.
		getEquipmentInventory().setCargoCapacity(Double.MAX_VALUE);

		initialiseEssentials(needGoods, Collections.emptyList());

		this.owner = owner;
	}	

	@Override
	public Authority getReportingAuthority() {
		return owner;
	}

	@Override
	public String getTemplate() {
		return SETTLEMENT_TEMPLATE;
	}
}