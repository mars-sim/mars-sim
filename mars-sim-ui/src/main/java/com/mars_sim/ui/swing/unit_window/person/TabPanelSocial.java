/**
 * Mars Simulation Project
 * TabPanelSocial.java
 * @date 2023-11-14
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.social.Relation.Opinion;
import com.mars_sim.core.person.ai.social.RelationshipUtil;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.ColumnSpec;
import com.mars_sim.ui.swing.components.TableModelUpdater;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;
import com.mars_sim.ui.swing.utils.SwingHelper;
import com.mars_sim.ui.swing.utils.model.BasePersonModel;
import com.mars_sim.ui.swing.utils.model.BaseWorkerModel;

/**
 * A tab panel displaying a person's social relationships.
 */
@SuppressWarnings("serial")
class TabPanelSocial extends EntityTableTabPanel<Person> {

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
	}

	/**
	 * Updates this panel.
	 */
	@Override
	public void refreshUI() {
		relationshipTableModel.update();
	}

	/**
	 * Internal class used as model for the relationship table.
	 */
	private static class RelationshipTableModel extends BasePersonModel {
		private static final int RESPECT_VAL = 200;
		private static final int CARE_VAL = 201;
		private static final int TRUST_VAL = 202;
		private static final int RELATIONSHIP_VAL = 203;

		private static final EntityColumnSpec RESPECT = new EntityColumnSpec(new ColumnSpec(RESPECT_VAL, Msg.getString("TabPanelSocial.column.respect"), Integer.class),
                                                            null);
		private static final EntityColumnSpec CARE = new EntityColumnSpec(new ColumnSpec(CARE_VAL, Msg.getString("TabPanelSocial.column.care"), Integer.class),
															null);
		private static final EntityColumnSpec TRUST = new EntityColumnSpec(new ColumnSpec(TRUST_VAL, Msg.getString("TabPanelSocial.column.trust"), Integer.class),
															null);
		private static final EntityColumnSpec RELATIONSHIP = new EntityColumnSpec(new ColumnSpec(RELATIONSHIP_VAL, Msg.getString("TabPanelSocial.column.relationship"), String.class),
															null);

		private Person person;	

		private RelationshipTableModel(Person person) {
			super(NAME, SETTLEMENT, RESPECT, CARE, TRUST, RELATIONSHIP);
			this.person = person;
			setEntities(RelationshipUtil.getAllKnownPeople(person));
		}

		@Override
		protected Object getEntityValue(Person entity, int valueIndex) {
			Opinion opinion = person.getRelation().getOpinion(entity);
			return switch (valueIndex) {
				case RESPECT_VAL -> (int)Math.round(opinion.d0());
				case CARE_VAL -> (int)Math.round(opinion.d1());
				case TRUST_VAL -> (int)Math.round(opinion.d2());
				case RELATIONSHIP_VAL -> RelationshipUtil.describeRelationship(opinion.getAverage());
				default -> BaseWorkerModel.getWorkerValue(entity, valueIndex);
			};
		}

		public void update() {
			if (!setEntities(RelationshipUtil.getAllKnownPeople(person))) {
				// No row changed so update all rows to update values
				SwingHelper.runInEDT(new TableModelUpdater(this));
			}
		}
	}
}
