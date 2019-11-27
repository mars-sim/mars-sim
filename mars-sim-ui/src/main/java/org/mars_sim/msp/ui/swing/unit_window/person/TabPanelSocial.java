/**
 * Mars Simulation Project
 * TabPanelSocial.java
 * @version 3.1.0 2017-10-18
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

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.social.RelationshipManager;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;

/**
 * A tab panel displaying a person's social relationships.
 */
@SuppressWarnings("serial")
public class TabPanelSocial
extends TabPanel
implements ListSelectionListener {

	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
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
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		// Create relationship label panel.
		WebPanel relationshipLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(relationshipLabelPanel);

		// Create relationship label
		WebLabel relationshipLabel = new WebLabel(Msg.getString("TabPanelSocial.label"), WebLabel.CENTER); //$NON-NLS-1$
		relationshipLabel.setFont(new Font("Serif", Font.BOLD, 16));
		relationshipLabelPanel.add(relationshipLabel);

		// Create relationship scroll panel
		WebScrollPane relationshipScrollPanel = new WebScrollPane();
//		relationshipScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(relationshipScrollPanel);

		// Create relationship table model
		relationshipTableModel = new RelationshipTableModel(person);

		// Create relationship table
		relationshipTable = new ZebraJTable(relationshipTableModel);
		relationshipTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		relationshipTable.getColumnModel().getColumn(0).setPreferredWidth(80);
		relationshipTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		relationshipTable.getColumnModel().getColumn(2).setPreferredWidth(40);
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
		    			if (selectedPerson != null) desktop.openUnitWindow(selectedPerson, false);
		    	    }
		        }
		    }
		});
		
		relationshipScrollPanel.setViewportView(relationshipTable);

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		relationshipTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		relationshipTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		relationshipTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		
		// Added sorting
		relationshipTable.setAutoCreateRowSorter(true); // in conflict with valueChanged(), throw exception if clicking on a person

		TableStyle.setTableStyle(relationshipTable);
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
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
	private class RelationshipTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private RelationshipManager manager;
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
			return 3;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = Object.class;
			else if (columnIndex == 1) dataType = Object.class;
			else if (columnIndex == 2) dataType = Object.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSocial.column.settlement"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSocial.column.person"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelSocial.column.relationship"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) 
				return ((Person)knownPeople.toArray()[row]).getAssociatedSettlement();		
			else if (column == 1) 
				return knownPeople.toArray()[row];
			else if (column == 2) {
				double opinion = manager.getOpinionOfPerson(person, (Person) knownPeople.toArray()[row]);
				return getRelationshipString(opinion);
			}
			else return null;
		}

		public void update() {
			Collection<Person> newKnownPeople = manager.getAllKnownPeople(person);
			if (!knownPeople.equals(newKnownPeople)) {
				knownPeople = newKnownPeople;
				//fireTableDataChanged();
			}
			//else fireTableDataChanged();
			
			fireTableDataChanged();
		}

		private String getRelationshipString(double opinion) {
			return Conversion.capitalize(RelationshipManager.describeRelationship(opinion));
		}
	}
}