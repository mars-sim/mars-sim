/*
 * Mars Simulation Project
 * BuildingPanelInhabitable.java
 * @date 2022-07-10
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.entitywindow.building;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.mars_sim.core.building.Building;
import com.mars_sim.core.building.function.LifeSupport;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.AttributePanel;
import com.mars_sim.ui.swing.components.JIntegerLabel;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.GenericPersonModel;

/**
 * This class is a building function panel representing 
 * the inhabitants of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelInhabitable extends EntityTabPanel<Building> implements TemporalComponent {

	private static final String PEOPLE_ICON = "people";
	
	/** The inhabitable building. */
	private LifeSupport inhabitable;
	private JIntegerLabel numberLabel;
	private InhabitantModel inhabitants;

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
		numberLabel = new JIntegerLabel(inhabitable.getOccupantNumber());
		labelPanel.addLabelledItem(Msg.getString("BuildingPanelInhabitable.number"),
								   numberLabel, null); //$NON-NLS-1$

		// Create capacity label
		labelPanel.addTextField(Msg.getString("BuildingPanelInhabitable.capacity"),
					 Integer.toString(inhabitable.getOccupantCapacity()), null);

		// Create inhabitant list panel
		inhabitants = new InhabitantModel();
		center.add(SwingHelper.createScrolledTable(inhabitants, getContext(), "Inhabitants",
				new Dimension(200, 250)),
					BorderLayout.CENTER);
	}

	@Override
	public void clockUpdate(ClockPulse pulse) {
		// Update population list and number label
		numberLabel.setValue(inhabitants.update());
	}

	/**
	 * Clean up listeners when destroyed.
	 */
	@Override
	public void destroy() {
		if (inhabitants != null) {
			inhabitants.release();
		}
		super.destroy();
	}

	/**
	 * Models the persons in the building.
	 */
	private class InhabitantModel extends GenericPersonModel {

		InhabitantModel	() {
			super(NAME, TASK);
		}

		public int update() {
			var occ = inhabitable.getOccupants();
			setEntities(occ);	
			return occ.size();
		}
	}
}
