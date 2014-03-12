/**
 * Mars Simulation Project
 * SkillTabPanel.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/** 
 * The SkillTabPanel is a tab panel for the skills of a person.
 */
public class SkillTabPanel
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private SkillTableModel skillTableModel;

	/**
	 * Constructor.
	 * @param person the person.
	 * @param desktop the main desktop.
	 */
	public SkillTabPanel(Person person, MainDesktopPane desktop) { 
		// Use the TabPanel constructor
		super(
			"Skills",
			null,
			"Skills",
			person, desktop
		);

		// Create skill label panel.
		JPanel skillLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(skillLabelPanel);

		// Create skill label
		JLabel skillLabel = new JLabel("Skills", JLabel.CENTER);
		skillLabelPanel.add(skillLabel);

		// Create skill scroll panel
		JScrollPane skillScrollPanel = new JScrollPane();
		skillScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(skillScrollPanel);

		// Create skill table model
		skillTableModel = new SkillTableModel(person);

		// Create skill table
		JTable skillTable = new JTable(skillTableModel);
		skillTable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		skillTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		skillTable.getColumnModel().getColumn(1).setPreferredWidth(120);
		skillTable.setCellSelectionEnabled(false);
		skillTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		skillScrollPanel.setViewportView(skillTable);
	}

	/**
	 * Updates the info on this panel.
	 */
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

		SkillManager manager;
		Map<String, Integer> skills;
		List<String> skillNames;

		private SkillTableModel(Person person) {
			manager = person.getMind().getSkillManager();

			SkillType[] keys = manager.getKeys();
			skills = new HashMap<String, Integer>();
			skillNames = new ArrayList<String>();
			for (SkillType skill : keys) {
				int level = manager.getSkillLevel(skill);
				if (level > 0) {
					skillNames.add(skill.getName());
					skills.put(skill.getName(), level);
				}
			}
		}

		public int getRowCount() {
			return skillNames.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 1) dataType = String.class;
			if (columnIndex == 0) dataType = Integer.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 1) return "Skill";
			else if (columnIndex == 0) return "Level";
			else return "unknown";
		}

		public Object getValueAt(int row, int column) {
			if (column == 1) return skillNames.get(row);
			else if (column == 0) return skills.get(skillNames.get(row));
			else return "unknown";
		}

		public void update() {
			SkillType[] keys = manager.getKeys();
			List<String> newSkillNames = new ArrayList<String>();
			Map<String, Integer> newSkills = new HashMap<String, Integer>();
			for (SkillType skill : keys) {
				int level = manager.getSkillLevel(skill);
				if (level > 0) {
					newSkillNames.add(skill.getName());
					newSkills.put(skill.getName(), level);
				}
			}

			if (!skills.equals(newSkills)) {
				skillNames = newSkillNames;
				skills = newSkills;
				fireTableDataChanged();
			}
		}
	}
}