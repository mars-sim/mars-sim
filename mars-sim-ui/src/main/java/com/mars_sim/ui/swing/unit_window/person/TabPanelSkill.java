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

import com.mars_sim.core.Unit;
import com.mars_sim.core.person.ai.Skill;
import com.mars_sim.core.person.ai.SkillManager;
import com.mars_sim.core.person.ai.task.util.Worker;
import com.mars_sim.tools.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.NumberCellRenderer;
import com.mars_sim.ui.swing.unit_window.TabPanelTable;

/**
 * The SkillTabPanel is a tab panel for the skills of a person or robot.
 */
@SuppressWarnings("serial")
public class TabPanelSkill extends TabPanelTable {

	private static final String SKILL_ICON = "skill"; //$NON-NLS-1$
	
	private SkillTableModel skillTableModel;

	/**
	 * Constructor 1.
	 * 
	 * @param unit the unit.
	 * @param desktop the main desktop.
	 */
	public TabPanelSkill(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSkill.title"), //$NON-NLS-1$
			ImageLoader.getIconByName(SKILL_ICON),
			Msg.getString("TabPanelSkill.title"), //$NON-NLS-1$
			unit, desktop
		);

		// Create skill table model
		skillTableModel = new SkillTableModel((Worker)unit);
	}

	@Override
	protected TableModel createModel() {
		return skillTableModel;
	}

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
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {		
		skillTableModel.update();
	}

	/**
	 * Internal class used as model for the skill table.
	 */
	private static class SkillTableModel
	extends AbstractTableModel {

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
			Class<?> dataType = null;
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Integer.class;
			else if (columnIndex == 2) dataType = Double.class;
			else if (columnIndex == 3) dataType = Double.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSkill.column.skill"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSkill.column.level"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelSkill.column.exp"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelSkill.column.time"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Skill s = skills.get(row);
			if (column == 0) return s.getType().getName();
			else if (column == 1) return s.getLevel();
			else if (column == 2) return s.getNeededExp();
			// Convert the labor time from the unit of millisol to sol
			else if (column == 3) return s.getTime()/1_000.0;
			else return null;
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
