/**
 * Mars Simulation Project
 * AssociatedPeopleTabPanel.java
 * @version 2.77 2004-09-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.ui.standard.*;
import org.mars_sim.msp.ui.standard.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.standard.unit_window.TabPanel;

/** 
 * The AssociatedPeopleTabPanel is a tab panel for information on all people 
 * associated with a settlement.
 */
public class AssociatedPeopleTabPanel extends TabPanel implements MouseListener, ActionListener {

	// Data members
	private DefaultListModel populationListModel;
	private JList populationList;
	private PersonCollection populationCache;

	/**
	 * Constructor
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public AssociatedPeopleTabPanel(Unit unit, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super("Associated", null, "Associated People", unit, desktop);
		
		Settlement settlement = (Settlement) unit;
		
		// Create label
		JPanel associatedLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(associatedLabelPanel);
        
		// Create associated people label
		JLabel associatedLabel = new JLabel("All People Associated with Settlement", JLabel.CENTER);
		associatedLabelPanel.add(associatedLabel);
		
		// Create population display panel
		JPanel populationDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		populationDisplayPanel.setBorder(new MarsPanelBorder());
		topContentPanel.add(populationDisplayPanel);
        
		// Create scroll panel for population list.
		JScrollPane populationScrollPanel = new JScrollPane();
		populationScrollPanel.setPreferredSize(new Dimension(175, 100));
		populationDisplayPanel.add(populationScrollPanel);
        
		// Create population list model
		populationListModel = new DefaultListModel();
		populationCache = new PersonCollection(settlement.getAllAssociatedPeople());
		PersonIterator i = populationCache.iterator();
		while (i.hasNext()) populationListModel.addElement(i.next());
        
		// Create population list
		populationList = new JList(populationListModel);
		populationList.addMouseListener(this);
		populationScrollPanel.setViewportView(populationList);
        
		// Create population monitor button
		JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		populationDisplayPanel.add(monitorButton);		
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		Settlement settlement = (Settlement) unit;

		// Update population list
		if (!populationCache.equals(settlement.getAllAssociatedPeople())) {
			populationCache = new PersonCollection(settlement.getAllAssociatedPeople());
			populationListModel.clear();
			PersonIterator i = populationCache.iterator();
			while (i.hasNext()) populationListModel.addElement(i.next());
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open person window.
		if (event.getClickCount() >= 2) desktop.openUnitWindow((Person) populationList.getSelectedValue());
	}

	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}

	/** 
	 * Action event occurs.
	 * @param event the action event
	 */
	public void actionPerformed(ActionEvent event) {
		// If the population monitor button was pressed, create tab in monitor tool.
		desktop.addModel(new PersonTableModel((Settlement) unit, true));
	}
}