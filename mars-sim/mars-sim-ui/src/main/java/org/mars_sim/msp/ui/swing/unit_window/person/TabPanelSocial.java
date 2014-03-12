/**
 * Mars Simulation Project
 * SocialTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * A tab panel displaying a person's social relationships.
 */
public class TabPanelSocial
extends TabPanel
implements ListSelectionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	private JTable relationshipTable;
	private RelationshipTableModel relationshipTableModel;

	/**
	 * Constructor.
	 * @param person the person.
	 * @param desktop the main desktop.
	 */
	public TabPanelSocial(Person person, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			"Social",
			null,
			"Social",
			person, desktop
		);

		// Create relationship label panel.
		JPanel relationshipLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(relationshipLabelPanel);

		// Create relationship label
		JLabel relationshipLabel = new JLabel("Relationships", JLabel.CENTER);
		relationshipLabelPanel.add(relationshipLabel);

		// Create relationship scroll panel
		JScrollPane relationshipScrollPanel = new JScrollPane();
		relationshipScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(relationshipScrollPanel);

		// Create relationship table model
		relationshipTableModel = new RelationshipTableModel(person);

		// Create relationship table
		relationshipTable = new JTable(relationshipTableModel);
		relationshipTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		relationshipTable.getColumnModel().getColumn(0).setPreferredWidth(80);
		relationshipTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		relationshipTable.setCellSelectionEnabled(true);
		relationshipTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		relationshipTable.getSelectionModel().addListSelectionListener(this);
		relationshipScrollPanel.setViewportView(relationshipTable);		
	}

	/**
	 * Updates this panel.
	 */
	public void update() {
		relationshipTableModel.update();
	}

	/**
	 * Called whenever the value of the selection changes.
	 * @param e the event that characterizes the change.
	 */
	public void valueChanged(ListSelectionEvent e) {
		int index = relationshipTable.getSelectedRow();
		Person selectedPerson = (Person) relationshipTable.getValueAt(index, 0);
		if (selectedPerson != null) desktop.openUnitWindow(selectedPerson, false);
	}

	/** 
	 * Internal class used as model for the relationship table.
	 */
	private static class RelationshipTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private RelationshipManager manager;
		private Collection<?> knownPeople;
		private Person person;

		private RelationshipTableModel(Person person) {
			this.person = person;
			manager = Simulation.instance().getRelationshipManager();
			knownPeople = manager.getAllKnownPeople(person);
		}

		public int getRowCount() {
			return knownPeople.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = String.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Person";
			else if (columnIndex == 1) return "Relationship";
			else return "unknown";
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return knownPeople.toArray()[row];
			else if (column == 1) {
				double opinion = manager.getOpinionOfPerson(person, (Person) knownPeople.toArray()[row]);
				return getRelationshipString(opinion);
			} 
			else return "unknown";
		}

		public void update() {
			Collection<?> newKnownPeople = manager.getAllKnownPeople(person);
			if (!knownPeople.equals(newKnownPeople)) {
				knownPeople = newKnownPeople;
				fireTableDataChanged();
			}
			else fireTableDataChanged();
		}

		private String getRelationshipString(double opinion) {
			String result = "";

			if (opinion < 5) result = "Hatred";
			else if (opinion < 20) result = "Antagonism";
			else if (opinion < 35) result = "Unfriendly";
			else if (opinion < 45) result = "Bothersome";
			else if (opinion < 55) result = "Indifference";
			else if (opinion < 65) result = "Cordial";
			else if (opinion < 80) result = "Amicable";
			else if (opinion < 95) result = "Friendly";
			else result = "Devoted";

			return result;
		}
	}
}