/**
 * Mars Simulation Project
 * TabPanelSocial.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipUtil;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.scroll.WebScrollPane;

/**
 * A tab panel displaying a person's social relationships.
 */
@SuppressWarnings("serial")
public class TabPanelSocial
extends TabPanel
implements ListSelectionListener {

	/** The Person instance. */
	private Person person = null;
	
	private JTable relationshipTable;
	private RelationshipTableModel relationshipTableModel;

	private Collection<Person> knownPeople;
	
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
		this.person = person;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Create relationship scroll panel
		WebScrollPane relationshipScrollPanel = new WebScrollPane();
//		relationshipScrollPanel.setBorder(new MarsPanelBorder());
		content.add(relationshipScrollPanel, BorderLayout.CENTER);

		// Create relationship table model
		relationshipTableModel = new RelationshipTableModel(person);

		// Create relationship table
		relationshipTable = new ZebraJTable(relationshipTableModel);
		relationshipTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		relationshipTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		relationshipTable.getColumnModel().getColumn(1).setPreferredWidth(120);
		relationshipTable.getColumnModel().getColumn(2).setPreferredWidth(25);
		relationshipTable.getColumnModel().getColumn(3).setPreferredWidth(70);
		relationshipTable.setRowSelectionAllowed(true);
		
		// For single clicking on a person to pop up his person window.
		//relationshipTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);	
		//relationshipTable.getSelectionModel().addListSelectionListener(this); 

		// Add a mouse listener to hear for double-clicking a person (rather than single click using valueChanged()
		relationshipTable.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	JTable table =(JTable) me.getSource();
		        Point p = me.getPoint();
		        int row = table.rowAtPoint(p);
		        int col = table.columnAtPoint(p);
		        if (me.getClickCount() == 2) {
		            if (row > 0 && col > 0) {
		    			Person selectedPerson = (Person) relationshipTable.getValueAt(row, 1);  			
		    			if (selectedPerson != null) getDesktop().openUnitWindow(selectedPerson, false);
		    	    }
		        }
		    }
		});
		
		relationshipScrollPanel.setViewportView(relationshipTable);

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		relationshipTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		relationshipTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		relationshipTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		relationshipTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
		
		// Added sorting
		relationshipTable.setAutoCreateRowSorter(true); // in conflict with valueChanged(), throw exception if clicking on a person

		TableStyle.setTableStyle(relationshipTable);
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {
		relationshipTableModel.update();
	}

	/**
	 * Called whenever the value of the selection changes.
	 * @param e the event that characterizes the change.
	 */
	public void valueChanged(ListSelectionEvent e) {
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
	private class RelationshipTableModel extends AbstractTableModel {

		private Person person;

		private RelationshipTableModel(Person person) {
			this.person = person;
			knownPeople = RelationshipUtil.getAllKnownPeople(person);
		}

		public int getRowCount() {
			return knownPeople.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = Object.class;
			else if (columnIndex == 1) dataType = Object.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Object.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSocial.column.settlement"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSocial.column.person"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelSocial.column.score"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelSocial.column.relationship"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			Person p = (Person)knownPeople.toArray()[row];
			if (column == 0) 
				return p.getAssociatedSettlement();		
			else if (column == 1) 
				return p;
			else if (column == 2) {
				double opinion = RelationshipUtil.getOpinionOfPerson(person, p);
				return Math.round(opinion*10.0)/10.0;
			}
			else if (column == 3) {
				double opinion = RelationshipUtil.getOpinionOfPerson(person, p);
				return " " + getRelationshipString(opinion);
			}
			else return null;
		}

		public void update() {
			Collection<Person> newKnownPeople = RelationshipUtil.getAllKnownPeople(person);
			if (!knownPeople.equals(newKnownPeople)) {
				knownPeople = newKnownPeople;
				//fireTableDataChanged();
			}
			//else fireTableDataChanged();
			
			fireTableDataChanged();
		}

		private String getRelationshipString(double opinion) {
			return Conversion.capitalize(RelationshipUtil.describeRelationship(opinion));
		}
	}
}
