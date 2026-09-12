/*
 * Mars Simulation Project
 * PersonMapLayer.java
 * @date 2025-08-27
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.settlement;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.swing.JMenuItem;

import com.mars_sim.core.CollectionUtils;
import com.mars_sim.core.Entity;
import com.mars_sim.core.person.GenderType;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.ui.swing.UIConfig;

/**
 * A settlement map layer for displaying people.
 */
public class PersonMapLayer extends WorkerMapLayer<Person> {
	private static final String PERSON_LABELS_PROP = "PERSON_LABELS";
	
	private static final ColorChoice MALE_UNSELECTED = new ColorChoice(new Color(0, 97, 198), Color.white); // lighter : 52, 152, 255
	private static final ColorChoice FEMALE_UNSELECTED = new ColorChoice(new Color(165, 0, 49), Color.white); // lighter : 236, 0, 70
	private static final ColorChoice MALE_SELECTED = new ColorChoice(new Color(0, 76, 118), Color.white);
	private static final ColorChoice FEMALE_SELECTED = new ColorChoice(new Color(120, 0, 56), Color.white);

	// Data members
	private SettlementMapPanel mapPanel;
	private boolean showLabels;

	/**
	 * Constructor.
	 * 
	 * @param mapPanel the settlement map panel.
	 */
	public PersonMapLayer(SettlementMapPanel mapPanel, Properties userSettings) {
		// Initialize data members.
		this.mapPanel = mapPanel;
		this.showLabels = UIConfig.extractBoolean(userSettings, PERSON_LABELS_PROP, false);
	}
	
	@Override
	public Collection<? extends MapHotspot<?>> displayLayer(Settlement settlement, MapViewPoint viewpoint,
			Entity selectedEntity) {
		Collection<Person> people = CollectionUtils.getPeopleInSettlementVicinity(settlement, false);		
		Person selectedPerson = (selectedEntity instanceof Person p) ? p : null;

		return drawWorkers(people, selectedPerson, showLabels, viewpoint);
	}

	@Override
	public List<JMenuItem> getFilterControls() {
		return List.of(createDisplayToggle("person_labels", showLabels,
				selected -> {
					showLabels = selected;
					mapPanel.repaint();
					return null;
				}));
	}

	@Override
	public void saveUIProperties(Properties props) {
		props.setProperty(PERSON_LABELS_PROP, Boolean.toString(showLabels));
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
}
