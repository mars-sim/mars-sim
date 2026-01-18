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

import com.mars_sim.core.Entity;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.Relation.Opinion;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.EntityModel;

/**
 * A tab panel displaying a person's social relationships.
 */
@SuppressWarnings("serial")
class TabPanelSocial extends EntityTableTabPanel<Person>
			implements ListSelectionListener {

	private static final String SOCIAL_ICON = "social";
	
	private RelationshipTableModel relationshipTableModel;

	
	/**
	 * Constructor.
	 * 
	 * @param person the person.
	 * @param context the overall UI context.
	 */
	public TabPanelSocial(Person person, UIContext context) {
		// Use the TabPanel constructor
		super(
			null,
			ImageLoader.getIconByName(SOCIAL_ICON),
			Msg.getString("TabPanelSocial.title"), //$NON-NLS-1$
			person, context
		);
	}
	
	@Override
	protected TableModel createModel() {
		// Create relationship table model
		relationshipTableModel = new RelationshipTableModel(getEntity());
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
	public void refreshUI() {
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
	private static class RelationshipTableModel extends AbstractTableModel
			implements EntityModel {

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

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch (columnIndex) {
				case 0, 1, 5 -> String.class;
				case 2, 3, 4 -> Integer.class;
				default -> Object.class;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("settlement.singular");
				case 1 -> Msg.getString("entity.name");
				case 2 -> Msg.getString("TabPanelSocial.column.respect");
				case 3 -> Msg.getString("TabPanelSocial.column.care");
				case 4 -> Msg.getString("TabPanelSocial.column.trust");
				case 5 -> Msg.getString("TabPanelSocial.column.relationship");
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			Person p = knownPeople.get(row);
			Opinion opinion = person.getRelation().getOpinion(p);

			return switch (column) {
				case 0 -> p.getAssociatedSettlement().getName();
				case 1 -> p.getName();
				case 2 -> (int)Math.round(opinion.d0());
				case 3 -> (int)Math.round(opinion.d1());
				case 4 -> (int)Math.round(opinion.d2());
				case 5 -> getRelationshipString(opinion.getAverage());
				default -> null;
			};
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

		private static String getRelationshipString(double opinion) {
			return Conversion.capitalize(RelationshipUtil.describeRelationship(opinion));
		}

		@Override
		public Entity getAssociatedEntity(int row) {
			return knownPeople.get(row);
		}
	}
}
