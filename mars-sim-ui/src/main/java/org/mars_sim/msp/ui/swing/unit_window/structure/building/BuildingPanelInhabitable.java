/*
 * Mars Simulation Project
 * BuildingPanelInhabitable.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The InhabitableBuildingPanel class is a building function panel representing 
 * the inhabitants of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelInhabitable extends BuildingFunctionPanel {

	private static final String PEOPLE_ICON = "people";

	/** The inhabitable building. */
	private LifeSupport inhabitable;
	private JLabel numberLabel;
	private UnitListPanel<Person> inhabitantListPanel;

	/**
	 * Constructor.
	 * 
	 * @param inhabitable The inhabitable building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelInhabitable(LifeSupport inhabitable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelInhabitable.title"), 
			ImageLoader.getIconByName(PEOPLE_ICON),
			inhabitable.getBuilding(), 
			desktop
		);

		// Initialize data members.
		this.inhabitable = inhabitable;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		// Create label panel
		AttributePanel labelPanel = new AttributePanel(2);
		center.add(labelPanel, BorderLayout.NORTH);

		// Create number label
		numberLabel = labelPanel.addTextField(Msg.getString("BuildingPanelInhabitable.number"),
								   Integer.toString(inhabitable.getOccupantNumber()), null); //$NON-NLS-1$

		// Create capacity label
		labelPanel.addTextField(Msg.getString("BuildingPanelInhabitable.capacity"),
					 Integer.toString(inhabitable.getOccupantCapacity()), null);


		// Create inhabitant list panel
		inhabitantListPanel = new UnitListPanel<>(getDesktop(), new Dimension(200, 250)) {
			@Override
			protected Collection<Person> getData() {
				return inhabitable.getOccupants();
			}
		};
		
		addBorder(inhabitantListPanel, "Inhabitants");
		center.add(inhabitantListPanel, BorderLayout.NORTH);
	}

	/**
	 * Update this panel.
	 */
	@Override
	public void update() {
		// Update population list and number label
		if (inhabitantListPanel.update()) {
			numberLabel.setText(Integer.toString(inhabitantListPanel.getUnitCount()));
		}
	}
}
