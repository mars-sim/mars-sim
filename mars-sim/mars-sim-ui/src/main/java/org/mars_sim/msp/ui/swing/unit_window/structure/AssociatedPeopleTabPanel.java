/**
 * Mars Simulation Project
 * AssociatedPeopleTabPanel.java
 * @version 3.00 2010-08-10
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.monitor.PersonTableModel;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The AssociatedPeopleTabPanel is a tab panel for information on all people 
 * associated with a settlement.
 */
public class AssociatedPeopleTabPanel extends TabPanel implements MouseListener, ActionListener {

	// Data members
	private DefaultListModel populationListModel;
	private JList populationList;
	private Collection<Person> populationCache;

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
		populationCache = settlement.getAllAssociatedPeople();
		Iterator<Person> i = populationCache.iterator();
		while (i.hasNext()) populationListModel.addElement(i.next());
        
		// Create population list
		populationList = new JList(populationListModel);
		populationList.addMouseListener(this);
		populationScrollPanel.setViewportView(populationList);
        
		// Create population monitor button
		JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
		monitorButton.setMargin(new Insets(1, 1, 1, 1));
		monitorButton.addActionListener(this);
		monitorButton.setToolTipText("Open tab in monitor tool");
		populationDisplayPanel.add(monitorButton);		
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		Settlement settlement = (Settlement) unit;

		// Update population list
		if (!Arrays.equals(populationCache.toArray(), settlement.getAllAssociatedPeople().toArray())) {
			populationCache = settlement.getAllAssociatedPeople();
			populationListModel.clear();
			Iterator<Person> i = populationCache.iterator();
			while (i.hasNext()) populationListModel.addElement(i.next());
		}
	}

	/** 
	 * Mouse clicked event occurs.
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open person window.
		if (event.getClickCount() >= 2) desktop.openUnitWindow((Person) populationList.getSelectedValue(), false);
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