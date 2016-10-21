/**
 * Mars Simulation Project
 * SocialTabPanel.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.MultisortTableHeaderCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
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
			Msg.getString("TabPanelSocial.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSocial.tooltip"), //$NON-NLS-1$
			person, desktop
		);

		// Create relationship label panel.
		JPanel relationshipLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(relationshipLabelPanel);

		// Create relationship label
		JLabel relationshipLabel = new JLabel(Msg.getString("TabPanelSocial.label"), JLabel.CENTER); //$NON-NLS-1$
		relationshipLabel.setFont(new Font("Serif", Font.BOLD, 16));
		relationshipLabelPanel.add(relationshipLabel);

		// Create relationship scroll panel
		JScrollPane relationshipScrollPanel = new JScrollPane();
		relationshipScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(relationshipScrollPanel);

		// Create relationship table model
		relationshipTableModel = new RelationshipTableModel(person);

		// Create relationship table
		relationshipTable = new ZebraJTable(relationshipTableModel);
		relationshipTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		relationshipTable.getColumnModel().getColumn(0).setPreferredWidth(80);
		relationshipTable.getColumnModel().getColumn(1).setPreferredWidth(70);
		
		relationshipTable.setCellSelectionEnabled(true);
		
		// For single clicking on a person to pop up his person window.
		//relationshipTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);	
		//relationshipTable.getSelectionModel().addListSelectionListener(this); 

		// 2016-10-19 Add a mouse listener to hear for double-clicking a person (rather than single click using valueChanged()
		relationshipTable.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		        JTable table =(JTable) me.getSource();
		        Point p = me.getPoint();
		        int row = table.rowAtPoint(p);
		        int col = table.columnAtPoint(p);
		        if (me.getClickCount() == 2) {
		            if (row > 0 && col == 0) {
		    			Person selectedPerson = (Person) relationshipTable.getValueAt(row, 0);
		    			if (selectedPerson != null) desktop.openUnitWindow(selectedPerson, false);
		    	    }
		        }
		    }
		});
		
		// 2015-09-24 Align the content to the center of the cell
		//DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		//renderer.setHorizontalAlignment(SwingConstants.CENTER);
		//relationshipTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		//relationshipTable.getColumnModel().getColumn(1).setCellRenderer(renderer);

		relationshipScrollPanel.setViewportView(relationshipTable);

		// 2015-06-08 Added sorting
		relationshipTable.setAutoCreateRowSorter(true); // in conflict with valueChanged(), throw exception if clicking on a person
	    //if (!MainScene.OS.equals("linux")) {
	    //	relationshipTable.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
	    //}
		// 2015-06-08 Added setTableStyle()
		TableStyle.setTableStyle(relationshipTable);
	}

	/**
	 * Updates this panel.
	 */
	public void update() {
		TableStyle.setTableStyle(relationshipTable);
		relationshipTableModel.update();
	}

	/**
	 * Called whenever the value of the selection changes.
	 * @param e the event that characterizes the change.
	 */
	public void valueChanged(ListSelectionEvent e) {
		TableStyle.setTableStyle(relationshipTable);
		relationshipTableModel.update();
		//int index = relationshipTable.getSelectedRow();
        //if (index > 0) {
		//	Person selectedPerson = (Person) relationshipTable.getValueAt(index, 0);
		//	if (selectedPerson != null) desktop.openUnitWindow(selectedPerson, false);
	    //}
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
			if (columnIndex == 0) dataType = Object.class;
			if (columnIndex == 1) dataType = Object.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSocial.column.person"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSocial.column.relationship"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return knownPeople.toArray()[row];
			// TODO: why on above line Exception in thread "pool-1924-thread-1" java.lang.ArrayIndexOutOfBoundsException: -1
			else if (column == 1) {
				double opinion = manager.getOpinionOfPerson(person, (Person) knownPeople.toArray()[row]);
				return getRelationshipString(opinion);
			}
			else return null;
		}

		public void update() {
			Collection<?> newKnownPeople = manager.getAllKnownPeople(person);
			if (!knownPeople.equals(newKnownPeople)) {
				knownPeople = newKnownPeople;
				//fireTableDataChanged();
			}
			//else fireTableDataChanged();
			
			fireTableDataChanged();
		}

		private String getRelationshipString(double opinion) {
			String result = null;
			if (opinion < 5) result = Msg.getString("TabPanelSocial.opinion.0"); //$NON-NLS-1$
			else if (opinion < 20) result = Msg.getString("TabPanelSocial.opinion.1"); //$NON-NLS-1$
			else if (opinion < 35) result = Msg.getString("TabPanelSocial.opinion.2"); //$NON-NLS-1$
			else if (opinion < 45) result = Msg.getString("TabPanelSocial.opinion.3"); //$NON-NLS-1$
			else if (opinion < 55) result = Msg.getString("TabPanelSocial.opinion.4"); //$NON-NLS-1$
			else if (opinion < 65) result = Msg.getString("TabPanelSocial.opinion.5"); //$NON-NLS-1$
			else if (opinion < 80) result = Msg.getString("TabPanelSocial.opinion.6"); //$NON-NLS-1$
			else if (opinion < 95) result = Msg.getString("TabPanelSocial.opinion.7"); //$NON-NLS-1$
			else result = Msg.getString("TabPanelSocial.opinion.8"); //$NON-NLS-1$
			return result;
		}
	}
}