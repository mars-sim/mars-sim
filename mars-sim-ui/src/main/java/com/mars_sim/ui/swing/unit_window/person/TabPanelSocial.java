/**
 * Mars Simulation Project
 * TabPanelSocial.java
 * @date 2023-11-14
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.person.ai.social.Relation.Opinion;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanel;

/**
 * A tab panel displaying a person's social relationships.
 */
@SuppressWarnings("serial")
public class TabPanelSocial
extends TabPanel
implements ListSelectionListener {

	private static final String SOCIAL_ICON = "social";
	
	/** The Person instance. */
	private Person person = null;
	
	private JTable relationshipTable;
	private RelationshipTableModel relationshipTableModel;

	private Collection<Person> knownPeople;
	
	/**
	 * Constructor.
	 * 
	 * @param person the person.
	 * @param desktop the main desktop.
	 */
	public TabPanelSocial(Person person, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(SOCIAL_ICON),
			Msg.getString("TabPanelSocial.title"), //$NON-NLS-1$
			person, desktop
		);
		this.person = person;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Create relationship scroll panel
		JScrollPane relationshipScrollPanel = new JScrollPane();
		content.add(relationshipScrollPanel, BorderLayout.CENTER);

		// Create relationship table model
		relationshipTableModel = new RelationshipTableModel(person);

		// Create relationship table
		relationshipTable = new JTable(relationshipTableModel);
		relationshipTable.setPreferredScrollableViewportSize(new Dimension(400, 100));
		relationshipTable.getColumnModel().getColumn(0).setPreferredWidth(90);
		relationshipTable.getColumnModel().getColumn(1).setPreferredWidth(90);
		relationshipTable.getColumnModel().getColumn(2).setPreferredWidth(30);
		relationshipTable.getColumnModel().getColumn(3).setPreferredWidth(30);
		relationshipTable.getColumnModel().getColumn(4).setPreferredWidth(30);
		relationshipTable.getColumnModel().getColumn(5).setPreferredWidth(50);
		relationshipTable.setRowSelectionAllowed(true);
		relationshipTable.getTableHeader().setToolTipText("Each Ssore (Respect, Care, or Trust) is between 0 and 100");
		
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
		    			if (selectedPerson != null) getDesktop().showDetails(selectedPerson);
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
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		relationshipTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		relationshipTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
		relationshipTable.getColumnModel().getColumn(4).setCellRenderer(renderer);
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		relationshipTable.getColumnModel().getColumn(5).setCellRenderer(renderer);
		
		// Added sorting
		relationshipTable.setAutoCreateRowSorter(true); // in conflict with valueChanged(), throw exception if clicking on a person
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
			return 6;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = Object.class;
			else if (columnIndex == 1) dataType = Object.class;
			else if (columnIndex == 2) dataType = Object.class;
			else if (columnIndex == 3) dataType = Object.class;
			else if (columnIndex == 4) dataType = Object.class;
			else if (columnIndex == 5) dataType = Object.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSocial.column.settlement"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSocial.column.person"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelSocial.column.respect"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelSocial.column.care"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelSocial.column.trust"); //$NON-NLS-1$
			else if (columnIndex == 5) return Msg.getString("TabPanelSocial.column.relationship"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			Person p = (Person)knownPeople.toArray()[row];
			if (column == 0) 
				return p.getAssociatedSettlement();		
			else if (column == 1) 
				return p;
			else if (column == 2) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + (int)Math.round(opinion.respect()) + " ";
			}
			else if (column == 3) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + (int)Math.round(opinion.care()) + " ";
			}
			else if (column == 4) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + (int)Math.round(opinion.trust()) + " ";
			}
			else if (column == 5) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + getRelationshipString(opinion.getAverage());	
			}

			return null;
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
