/**
 * Mars Simulation Project
 * BuildingPanelLiving.java
 * @version 3.2.0 2021-06-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.border.Border;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;


/**
 * The BuildingPanelLiving class is a building function panel representing
 * the living accommodation info of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelLiving extends BuildingFunctionPanel {

	private int bedCapCache;
	private int bedAssignedCache;
	private int bedOccupiedCache;
	private int bedEmptyCache;

	private WebLabel bedCapLabel;
	private WebLabel bedAssignsLabel;
	private WebLabel bedOccupiedLabel;
	private WebLabel bedEmptyLabel;

	private LivingAccommodations living;

	/**
	 * Constructor.
	 * @param medical the medical care building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelLiving(LivingAccommodations living, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelLiving.title"), living.getBuilding(), desktop);

		// Initialize data members
		this.living = living;
	}
	
	/**
	 * Build the UI elements
	 */
	@Override
	protected void buildUI(JPanel center, JPanel bottom) {

		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(5, 1, 0, 0));
		center.add(labelPanel, BorderLayout.NORTH);

		// Create bed capacity label
		bedCapLabel = new WebLabel(Msg.getString("BuildingPanelLiving.beds.capacity",
				living.getBedCap()), WebLabel.CENTER);
		labelPanel.add(bedCapLabel);

		// Create # of assigned bed label
		bedAssignsLabel = new WebLabel(Msg.getString("BuildingPanelLiving.beds.assigned",
				living.getNumAssignedBeds()), WebLabel.CENTER);
		labelPanel.add(bedAssignsLabel);

		// Create bedOccupiedLabel
		bedOccupiedLabel = new WebLabel(Msg.getString("BuildingPanelLiving.beds.occupied",
				living.getNumOccupiedActivitySpots()), WebLabel.CENTER);
		labelPanel.add(bedOccupiedLabel);

		// Create bedEmptyLabel
		bedEmptyLabel = new WebLabel(Msg.getString("BuildingPanelLiving.beds.empty",
				living.getNumEmptyActivitySpots()), WebLabel.CENTER);
		labelPanel.add(bedEmptyLabel);

	}

	@Override
	public void update() {
		// Update bedCapLabel
		if (bedCapCache != living.getBedCap()) {
			bedCapCache = living.getBedCap();
			bedCapLabel.setText(Msg.getString("BuildingPanelLiving.beds.capacity", bedCapCache));
		}

		// Update bedAssignsLabel
		if (bedAssignedCache != living.getNumAssignedBeds()) {
			bedAssignedCache = living.getNumAssignedBeds();
			bedAssignsLabel.setText(Msg.getString("BuildingPanelLiving.beds.assigned", bedAssignedCache));
		}

		// Update bedOccupiedLabel
		if (bedOccupiedCache != living.getNumOccupiedActivitySpots()) {
			bedOccupiedCache = living.getNumOccupiedActivitySpots();
			bedOccupiedLabel.setText(Msg.getString("BuildingPanelLiving.beds.occupied", bedOccupiedCache));
		}

		// Update bedEmptyLabel
		if (bedEmptyCache != living.getNumEmptyActivitySpots()) {
			bedEmptyCache = living.getNumEmptyActivitySpots();
			bedEmptyLabel.setText(Msg.getString("BuildingPanelLiving.beds.empty", bedEmptyCache));
		}
	}
}

