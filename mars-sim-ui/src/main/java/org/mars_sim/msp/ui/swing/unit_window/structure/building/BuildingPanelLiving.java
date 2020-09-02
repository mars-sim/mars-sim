/**
 * Mars Simulation Project
 * BuildingPanelLiving.java
 * @version 3.1.2 2020-09-02
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.building.function.LivingAccommodations;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
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
		super(living.getBuilding(), desktop);

		// Initialize data members
		this.living = living;

		// Set panel layout
		setLayout(new BorderLayout());

		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(5, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create medical care label
		WebLabel titleLabel = new WebLabel(Msg.getString("BuildingPanelLiving.title"), WebLabel.CENTER);
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//medicalCareLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(titleLabel);

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
				living.getNumOccupiedSpots()), WebLabel.CENTER);
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
		if (bedOccupiedCache != living.getNumOccupiedSpots()) {
			bedOccupiedCache = living.getNumOccupiedSpots();
			bedOccupiedLabel.setText(Msg.getString("BuildingPanelLiving.beds.occupied", bedOccupiedCache));
		}	
		
		// Update bedEmptyLabel
		if (bedEmptyCache != living.getNumEmptyActivitySpots()) {
			bedEmptyCache = living.getNumEmptyActivitySpots();
			bedEmptyLabel.setText(Msg.getString("BuildingPanelLiving.beds.empty", bedEmptyCache));
		}	
	}
}
	
