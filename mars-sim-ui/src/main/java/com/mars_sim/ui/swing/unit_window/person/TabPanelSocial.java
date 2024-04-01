/**
 * Mars Simulation Project
 * TabPanelSocial.java
 * @date 2023-11-14
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.Unit;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.Relation.Opinion;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;
import com.mars_sim.ui.swing.utils.UnitModel;

/**
 * A tab panel displaying a person's social relationships.
 */
@SuppressWarnings("serial")
public class TabPanelSocial extends TabPanelTable
implements ListSelectionListener {

	private static final String SOCIAL_ICON = "social";
	
	/** The Person instance. */
	private Person person = null;
	
	private RelationshipTableModel relationshipTableModel;

	
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

		setHeaderToolTips(null);
	}
	
	@Override
	protected TableModel createModel() {
		// Create relationship table model
		relationshipTableModel = new RelationshipTableModel(person);
		return relationshipTableModel;
	}

	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(90);
		columnModel.getColumn(1).setPreferredWidth(90);
		columnModel.getColumn(2).setPreferredWidth(30);
		columnModel.getColumn(3).setPreferredWidth(30);
		columnModel.getColumn(4).setPreferredWidth(30);
		columnModel.getColumn(5).setPreferredWidth(50);

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		columnModel.getColumn(0).setCellRenderer(renderer);
		columnModel.getColumn(1).setCellRenderer(renderer);
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		columnModel.getColumn(2).setCellRenderer(renderer);
		columnModel.getColumn(3).setCellRenderer(renderer);
		columnModel.getColumn(4).setCellRenderer(renderer);
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		columnModel.getColumn(5).setCellRenderer(renderer);
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
	private class RelationshipTableModel extends AbstractTableModel
			implements UnitModel {

		private Person person;	
		private List<Person> knownPeople;

		private RelationshipTableModel(Person person) {
			this.person = person;
			knownPeople = new ArrayList<>(RelationshipUtil.getAllKnownPeople(person));
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
			Person p = knownPeople.get(row);
			if (column == 0) 
				return p.getAssociatedSettlement();		
			else if (column == 1) 
				return p;
			else if (column == 2) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + (int)Math.round(opinion.d0()) + " ";
			}
			else if (column == 3) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + (int)Math.round(opinion.d1()) + " ";
			}
			else if (column == 4) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + (int)Math.round(opinion.d2()) + " ";
			}
			else if (column == 5) {
				Opinion opinion = person.getRelation().getOpinion(p);
				return " " + getRelationshipString(opinion.getAverage());	
			}

			return null;
		}

		public void update() {
			List<Person> newKnownPeople = new ArrayList<>(RelationshipUtil.getAllKnownPeople(person));
			if (!knownPeople.equals(newKnownPeople)) {
				knownPeople = newKnownPeople;
				fireTableDataChanged();
			}
			else {
				fireTableRowsUpdated(0, knownPeople.size()-1);
			}
			
		}

		private String getRelationshipString(double opinion) {
			return Conversion.capitalize(RelationshipUtil.describeRelationship(opinion));
		}

		@Override
		public Unit getAssociatedUnit(int row) {
			return knownPeople.get(row);
		}
	}
}
