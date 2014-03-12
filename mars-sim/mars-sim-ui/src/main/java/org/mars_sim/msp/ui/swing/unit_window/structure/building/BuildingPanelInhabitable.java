/**
 * Mars Simulation Project
 * InhabitableBuildingPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import org.apache.commons.collections.CollectionUtils;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * The InhabitableBuildingPanel class is a building function panel representing 
 * the inhabitants of a settlement building.
 */
public class BuildingPanelInhabitable
extends BuildingFunctionPanel
implements MouseListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** The inhabitable building. */
	private LifeSupport inhabitable;
	private DefaultListModel<Person> inhabitantListModel;
	private JList<Person> inhabitantList;
	private Collection<Person> inhabitantCache;
	private JLabel numberLabel;

	/**
	 * Constructor.
	 * @param inhabitable The inhabitable building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelInhabitable(LifeSupport inhabitable, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(inhabitable.getBuilding(), desktop);

		// Initialize data members.
		this.inhabitable = inhabitable;

		// Set panel layout
		setLayout(new BorderLayout());

		// Create label panel
		JPanel labelPanel = new JPanel(new GridLayout(3, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);

		// Create inhabitant label
		JLabel inhabitantLabel = new JLabel(Msg.getString("BuildingPanelInhabitable.occupants"), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(inhabitantLabel);

		// Create number label
		numberLabel = new JLabel(Msg.getString("BuildingPanelInhabitable.number", inhabitable.getOccupantNumber()), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(numberLabel);

		// Create capacity label
		JLabel capacityLabel = new JLabel(
			Msg.getString(
				"BuildingPanelInhabitable.capacity", //$NON-NLS-1$
				inhabitable.getOccupantCapacity()
			),JLabel.CENTER
		);
		labelPanel.add(capacityLabel);

		// Create inhabitant list panel
		JPanel inhabitantListPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(inhabitantListPanel, BorderLayout.CENTER);

		// Create scroll panel for inhabitant list
		JScrollPane inhabitantScrollPanel = new JScrollPane();
		inhabitantScrollPanel.setPreferredSize(new Dimension(160, 60));
		inhabitantListPanel.add(inhabitantScrollPanel);

		// Create inhabitant list model
		inhabitantListModel = new DefaultListModel<Person>();
		inhabitantCache = new ArrayList<Person>(inhabitable.getOccupants());
		Iterator<Person> i = inhabitantCache.iterator();
		while (i.hasNext()) inhabitantListModel.addElement(i.next());

		// Create inhabitant list
		inhabitantList = new JList<Person>(inhabitantListModel);
		inhabitantList.addMouseListener(this);
		inhabitantScrollPanel.setViewportView(inhabitantList);
	}

	/**
	 * Update this panel.
	 */
	public void update() {

		// Update population list and number label
		if (!CollectionUtils.isEqualCollection(inhabitantCache, inhabitable.getOccupants())) {
			inhabitantCache = new ArrayList<Person>(inhabitable.getOccupants());
			inhabitantListModel.clear();
			Iterator<Person> i = inhabitantCache.iterator();
			while (i.hasNext()) inhabitantListModel.addElement(i.next());

			numberLabel.setText(Msg.getString("BuildingPanelInhabitable.number", inhabitantCache.size())); //$NON-NLS-1$
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {

		// If double-click, open person window.
		if (event.getClickCount() >= 2) 
			desktop.openUnitWindow((Person) inhabitantList.getSelectedValue(), false);
	}

	public void mousePressed(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
}
