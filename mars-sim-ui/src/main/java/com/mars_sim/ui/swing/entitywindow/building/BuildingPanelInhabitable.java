/*
 * Mars Simulation Project
 * BuildingPanelInhabitable.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitListPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * This class is a building function panel representing 
 * the inhabitants of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelInhabitable extends EntityTabPanel<Building> implements TemporalComponent {

	private static final String PEOPLE_ICON = "people";
	
	/** The inhabitable building. */
	private LifeSupport inhabitable;
	private JLabel numberLabel;
	private UnitListPanel<Person> inhabitantListPanel;

	/**
	 * Constructor.
	 * 
	 * @param inhabitable The inhabitable building this panel is for.
	 * @param context The UI context.
	 */
	public BuildingPanelInhabitable(LifeSupport inhabitable, UIContext context) {

		// Use BuildingFunctionPanel constructor
		super(
			Msg.getString("BuildingPanelInhabitable.title"), 
			ImageLoader.getIconByName(PEOPLE_ICON), null,
			context, inhabitable.getBuilding() 
		);

		// Initialize data members.
		this.inhabitable = inhabitable;
	}
	
	/**
	 * Builds the UI.
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
		inhabitantListPanel = new UnitListPanel<>(getContext(), new Dimension(200, 250)) {
			@Override
			protected Collection<Person> getData() {
				return inhabitable.getOccupants();
			}
		};
		
		inhabitantListPanel.setBorder(SwingHelper.createLabelBorder("Inhabitants"));
		center.add(inhabitantListPanel, BorderLayout.NORTH);
	}


	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Update population list and number label
		if (inhabitantListPanel.update()) {
			numberLabel.setText(Integer.toString(inhabitantListPanel.getUnitCount()));
		}
	}
}
