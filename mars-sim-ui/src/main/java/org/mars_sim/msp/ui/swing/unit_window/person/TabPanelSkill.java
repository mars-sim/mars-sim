/**
 * Mars Simulation Project
 * TabPanelSkill.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.SkillManager;
import org.mars_sim.msp.core.person.ai.SkillType;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.table.WebTable;

/**
 * The SkillTabPanel is a tab panel for the skills of a person.
 */
public class TabPanelSkill
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private WebTable skillTable ;
	private SkillTableModel skillTableModel;
	//private Person person;
	//private Robot robot;
	/**
	 * Constructor 1.
	 * @param person the person.
	 * @param desktop the main desktop.
	 */
	public TabPanelSkill(Person person, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSkill.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSkill.tooltip"), //$NON-NLS-1$
			person, desktop
		);

		//this.person = person;

		// Create skill table model
		skillTableModel = new SkillTableModel(person);

		init();
	}

	/**
	 * Constructor 2.
	 * @param person the person.
	 * @param desktop the main desktop.
	 */
	public TabPanelSkill(Robot robot, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSkill.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSkill.tooltip"), //$NON-NLS-1$
			robot, desktop
		);

		//this.robot = robot;

		// Create skill table model
		skillTableModel = new SkillTableModel(robot);

		init();
	}

	public void init() {

		// Create skill label panel.
		WebPanel skillLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(skillLabelPanel);

		// Create skill label
		WebLabel skillLabel = new WebLabel(Msg.getString("TabPanelSkill.label"), WebLabel.CENTER); //$NON-NLS-1$
		skillLabel.setFont(new Font("Serif", Font.BOLD, 16));
		skillLabelPanel.add(skillLabel);

		// Create skill scroll panel
		WebScrollPane skillScrollPanel = new WebScrollPane();
		skillScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(skillScrollPanel);

		// Create skill table
		skillTable = new ZebraJTable(skillTableModel);
		skillTable.setPreferredScrollableViewportSize(new Dimension(250, 100));
		skillTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		skillTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		skillTable.setCellSelectionEnabled(false);
		skillTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		skillTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		skillTable.getColumnModel().getColumn(1).setCellRenderer(renderer);

		skillScrollPanel.setViewportView(skillTable);

		// Added sorting
		skillTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(skillTable);
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		TableStyle.setTableStyle(skillTable);
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
		private Map<String, Integer> skills;
		private List<String> skillNames;

		private SkillTableModel(Unit unit) {
			Person person = null;
	        Robot robot = null;

	        if (unit instanceof Person) {
	         	person = (Person) unit;
	         	skillManager = person.getMind().getSkillManager();
	        }
	        else if (unit instanceof Robot) {
	        	robot = (Robot) unit;
	        	skillManager = robot.getBotMind().getSkillManager();
	        }

			skills = skillManager.getSkillsMap();
			skillNames = skillManager.getSkillNames();
			
//			SkillType[] keys = manager.getKeys();
//			skills = new HashMap<String, Integer>();
//			skillNames = new ArrayList<String>();
//			for (SkillType skill : keys) {
//				int level = manager.getSkillLevel(skill);
//				if (level > 0) {
//					skillNames.add(skill.getName());
//					skills.put(skill.getName(), level);
//				}
//			}
		}

		public int getRowCount() {
			return skillNames.size();
		}

		public int getColumnCount() {
			return 2;
		}

		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = Integer.class;
			return dataType;
		}

		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSkill.column.skill"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSkill.column.level"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) return skillNames.get(row);
			else if (column == 1) return skills.get(skillNames.get(row));
			else return null;
		}

		public void update() {
			SkillType[] keys = skillManager.getKeys();
			List<String> newSkillNames = new ArrayList<String>();
			Map<String, Integer> newSkills = new HashMap<String, Integer>();
			for (SkillType skill : keys) {
				int level = skillManager.getSkillLevel(skill);
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