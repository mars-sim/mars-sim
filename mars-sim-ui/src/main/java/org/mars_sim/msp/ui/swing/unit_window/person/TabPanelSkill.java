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

import javax.swing.JTable;
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
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * The SkillTabPanel is a tab panel for the skills of a person.
 */
public class TabPanelSkill
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Person instance. */
	private Person person = null;
	/** The Robot instance. */
	private Robot robot = null;
	
	private JTable skillTable ;
	private SkillTableModel skillTableModel;

	
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

		this.person = person;
		
		// Create skill table model
		skillTableModel = new SkillTableModel(person);
	}
	
	public boolean isUIDone() {
		return uiDone;
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

		this.robot = robot;
		
		// Create skill table model
		skillTableModel = new SkillTableModel(robot);

	}
	
	public void initializeUI() {
		uiDone = true;

		// Create skill table model
        if (unit instanceof Person) {
    		skillTableModel = new SkillTableModel(person);
        }
        else if (unit instanceof Robot) {
    		skillTableModel = new SkillTableModel(robot);
        }

		// Create skill label panel.
		WebPanel skillLabelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(skillLabelPanel);

		// Create skill label
		WebLabel skillLabel = new WebLabel(Msg.getString("TabPanelSkill.label"), WebLabel.CENTER); //$NON-NLS-1$
		skillLabel.setFont(new Font("Serif", Font.BOLD, 16));
		skillLabelPanel.add(skillLabel);

		// Create skill scroll panel
		WebScrollPane skillScrollPanel = new WebScrollPane();
//		skillScrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(skillScrollPanel);

		// Create skill table
		skillTable = new ZebraJTable(skillTableModel);
		skillTable.setPreferredScrollableViewportSize(new Dimension(250, 100));
		skillTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		skillTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		skillTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		skillTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		skillTable.setRowSelectionAllowed(true);
		skillTable.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		skillTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
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
		if (!uiDone)
			initializeUI();
		
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

		private SkillTableModel(Unit unit) {
			Person person = null;
	        Robot robot = null;

	        if (unit instanceof Person) {
	         	person = (Person) unit;
	         	skillManager = person.getSkillManager();
	        }
	        else if (unit instanceof Robot) {
	        	robot = (Robot) unit;
	        	skillManager = robot.getSkillManager();
	        }

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
			List<String> newSkillNames = new ArrayList<String>();
			Map<String, Integer> newSkills = new HashMap<String, Integer>();
			Map<String, Integer> newExps = new HashMap<String, Integer>();
			Map<String, Integer> newTimes = new HashMap<String, Integer>();
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