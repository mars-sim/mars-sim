/*
 * Mars Simulation Project
 * TabPanelSkill.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.person.ai.task.util.Worker;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The SkillTabPanel is a tab panel for the skills of a person or robot.
 */
@SuppressWarnings("serial")
public class TabPanelSkill
extends TabPanel {

	private static final String SKILL_ICON = Msg.getString("icon.running"); //$NON-NLS-1$
	
	private JTable skillTable ;
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
			ImageLoader.getNewIcon(SKILL_ICON),
			Msg.getString("TabPanelSkill.title"), //$NON-NLS-1$
			unit, desktop
		);

		// Create skill table model
		skillTableModel = new SkillTableModel((Worker)unit);
	}

	@Override
	protected void buildUI(JPanel content) {

		// Create skill scroll panel
		JScrollPane skillScrollPanel = new JScrollPane();
		content.add(skillScrollPanel);

		// Create skill table
		skillTable = new ZebraJTable(skillTableModel);
		skillTable.setPreferredScrollableViewportSize(new Dimension(250, 100));
		skillTable.getColumnModel().getColumn(0).setPreferredWidth(110);
		skillTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		skillTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		skillTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		skillTable.setRowSelectionAllowed(true);
		skillTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		skillTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		skillTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		skillTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
		skillTable.getColumnModel().getColumn(3).setCellRenderer(renderer);
		
		skillScrollPanel.setViewportView(skillTable);

		// Added sorting
		skillTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(skillTable);
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {		
		if (skillTable != null) {
			TableStyle.setTableStyle(skillTable);
			skillTableModel.update();
		}
	}

	/**
	 * Internal class used as model for the skill table.
	 */
	private static class SkillTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private SkillManager skillManager;
		private Map<String, Integer> levels;
		private Map<String, Integer> times;
		private Map<String, Integer> exps;
		private List<String> skillNames;

		private SkillTableModel(Worker unit) {

	        skillManager = unit.getSkillManager();

			levels = skillManager.getSkillLevelMap();
			exps = skillManager.getSkillDeltaExpMap();
			times = skillManager.getSkillTimeMap();
			skillNames = skillManager.getKeyStrings();
		}

		public int getRowCount() {
			return skillNames.size();
		}

		public int getColumnCount() {
			return 4;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Integer.class;
			else if (columnIndex == 2) dataType = Integer.class;
			else if (columnIndex == 3) dataType = Double.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSkill.column.skill"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSkill.column.level"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelSkill.column.exp"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelSkill.column.time"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return skillNames.get(row);
			else if (column == 1) return levels.get(skillNames.get(row));
			else if (column == 2) return exps.get(skillNames.get(row));
			// Convert the labor time from the unit of millisol to sol
			else if (column == 3) return Math.round(10.0 * times.get(skillNames.get(row)))/1_000.0;
			else return null;
		}

		public void update() {
			SkillType[] keys = skillManager.getKeys();
			List<String> newSkillNames = new ArrayList<>();
			Map<String, Integer> newSkills = new HashMap<>();
			Map<String, Integer> newExps = new HashMap<>();
			Map<String, Integer> newTimes = new HashMap<>();
			for (SkillType skill : keys) {
				int level = skillManager.getSkillLevel(skill);
				int exp = skillManager.getSkillDeltaExp(skill);
				int time = skillManager.getSkillTime(skill);
				newExps.put(skill.getName(), exp);
				newSkillNames.add(skill.getName());
				newSkills.put(skill.getName(), level);
				newTimes.put(skill.getName(), time);
			}

//			if (!levels.equals(newSkills)) {
				skillNames = newSkillNames;
				levels = newSkills;
				exps = newExps;
				times = newTimes;
				fireTableDataChanged();
//			}
		}
	}
}
