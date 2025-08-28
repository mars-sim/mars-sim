/*
 * Mars Simulation Project
 * PersonMapLayer.java
 * @date 2025-08-27
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.util.Collection;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel.DisplayOption;

/**
 * A settlement map layer for displaying people.
 */
public class PersonMapLayer extends WorkerMapLayer<Person> {
	
	private static final ColorChoice MALE_UNSELECTED = new ColorChoice(new Color(0, 97, 198), Color.white); // lighter : 52, 152, 255
	private static final ColorChoice FEMALE_UNSELECTED = new ColorChoice(new Color(165, 0, 49), Color.white); // lighter : 236, 0, 70
	private static final ColorChoice MALE_SELECTED = new ColorChoice(new Color(0, 76, 118), Color.white);
	private static final ColorChoice FEMALE_SELECTED = new ColorChoice(new Color(120, 0, 56), Color.white);

	// Data members
	private SettlementMapPanel mapPanel;

	/**
	 * Constructor.
	 * 
	 * @param mapPanel the settlement map panel.
	 */
	public PersonMapLayer(SettlementMapPanel mapPanel) {
		// Initialize data members.
		this.mapPanel = mapPanel;
	}
	
	@Override
	public void displayLayer(Settlement settlement, MapViewPoint viewpoint) {
		Collection<Person> people = CollectionUtils.getPeopleInSettlementVicinity(settlement, false);		
		Person selectedPerson = mapPanel.getSelectedPerson();

		drawWorkers(people, selectedPerson, mapPanel.isOptionDisplayed(DisplayOption.PERSON_LABELS), viewpoint);
	}

	/**
	 * Identifies the best colour to render this Person in the Settlement Map.
	 * 
	 * @param p Person
	 * @param selected Are they selected
	 * @return
	 */
	@Override
    protected ColorChoice getColor(Person p, boolean selected) {
		if (selected) {
			return (p.getGender() == GenderType.MALE ? MALE_SELECTED : FEMALE_SELECTED);
		}
		else {
			return (p.getGender() == GenderType.MALE ? MALE_UNSELECTED : FEMALE_UNSELECTED);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		mapPanel = null;
	}
}
