/**
 * Mars Simulation Project
 * BuildingPanelInhabitable.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

import com.alee.laf.panel.WebPanel;

/**
 * The InhabitableBuildingPanel class is a building function panel representing 
 * the inhabitants of a settlement building.
 */
@SuppressWarnings("serial")
public class BuildingPanelInhabitable
	extends BuildingFunctionPanel {

	/** The inhabitable building. */
	private LifeSupport inhabitable;
	private JTextField numberLabel;
	private UnitListPanel<Person> inhabitantListPanel;

	/**
	 * Constructor.
	 * @param inhabitable The inhabitable building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelInhabitable(LifeSupport inhabitable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelInhabitable.title"), inhabitable.getBuilding(), desktop);

		// Initialize data members.
		this.inhabitable = inhabitable;
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {
		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(2, 2, 3, 1));
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create number label
		numberLabel = addTextField(labelPanel, Msg.getString("BuildingPanelInhabitable.number"),
								   inhabitable.getOccupantNumber(), null); //$NON-NLS-1$

		// Create capacity label
		addTextField(labelPanel, Msg.getString("BuildingPanelInhabitable.capacity"),
					 inhabitable.getOccupantCapacity(), null);


		// Create inhabitant list panel
		inhabitantListPanel = new UnitListPanel<Person>(desktop, new Dimension(150, 100)) {
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
