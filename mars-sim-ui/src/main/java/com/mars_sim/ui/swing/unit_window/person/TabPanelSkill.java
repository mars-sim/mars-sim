/*
 * Mars Simulation Project
 * TabPanelSkill.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.mars_sim.core.person.ai.Skill;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NumberCellRenderer;
import com.mars_sim.ui.swing.entitywindow.EntityTableTabPanel;

/**
 * The SkillTabPanel is a tab panel for the skills of a person or robot.
 */
@SuppressWarnings("serial")
public class TabPanelSkill extends EntityTableTabPanel<Worker> {

	private static final String SKILL_ICON = "skill"; //$NON-NLS-1$
	
	private SkillTableModel skillTableModel;

	/**
	 * Constructor 1.
	 * 
	 * @param worker  the worker for this panel.
	 * @param context the UI context.
	 */
	public TabPanelSkill(Worker worker, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSkill.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(SKILL_ICON),
			null,
			worker, context
		);

		// Create skill table model
		skillTableModel = new SkillTableModel(worker);
	}

	/**
	 * Create teh table model to show the skills of the worker.
	 */
	@Override
	protected TableModel createModel() {
		return skillTableModel;
	}

	/**
	 * Customise the column details.
	 */
	@Override
	protected void setColumnDetails(TableColumnModel columnModel) {
		columnModel.getColumn(0).setPreferredWidth(110);
		columnModel.getColumn(1).setPreferredWidth(50);
		columnModel.getColumn(2).setPreferredWidth(60);
		columnModel.getColumn(3).setPreferredWidth(100);

		columnModel.getColumn(1).setCellRenderer(new NumberCellRenderer(0));

		var r = new NumberCellRenderer(2);
		columnModel.getColumn(2).setCellRenderer(r);
		columnModel.getColumn(3).setCellRenderer(r);
	}

	/**
	 * Updates the info on this panel. This is a full refresh.
	 */
	@Override
	public void refreshUI() {		
		skillTableModel.update();
	}

	/**
	 * Internal class used as model for the skill table.
	 */
	private static class SkillTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private SkillManager skillManager;
		private List<Skill> skills;

		private SkillTableModel(Worker unit) {
	        skillManager = unit.getSkillManager();
			skills = skillManager.getSkills();
		}

		@Override
		public int getRowCount() {
			return skills.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> String.class;
				case 1 -> Integer.class;
				case 2, 3 -> Double.class;
				default -> Object.class;
			};
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch (columnIndex) {
				case 0 -> Msg.getString("TabPanelSkill.column.skill");
				case 1 -> Msg.getString("TabPanelSkill.column.level");
				case 2 -> Msg.getString("TabPanelSkill.column.exp");
				case 3 -> Msg.getString("TabPanelSkill.column.time");
				default -> null;
			};
		}

		@Override
		public Object getValueAt(int row, int column) {
			Skill s = skills.get(row);
			return switch (column) {
				case 0 -> s.getType().getName();
				case 1 -> s.getLevel();
				case 2 -> s.getNeededExp();
				case 3 -> s.getTime()/1_000.0;
				default -> null;
			};
		}

		public void update() {
			List<Skill> newSkills = skillManager.getSkills();
			if(!newSkills.equals(skills)) {
				skills = newSkills;
				fireTableDataChanged();
			}
		}
	}
}
