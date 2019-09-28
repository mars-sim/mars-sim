/**
 * Mars Simulation Project
 * TabPanelScience.java
 * @version 3.1.0 2017-10-18
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.button.WebButton;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;

/**
 * A tab panel displaying a settlement's scientific studies and achievements.
 */
@SuppressWarnings("serial")
public class TabPanelScience
extends TabPanel {

	// Data members
	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Settlement instance. */
	private Settlement settlement;
	
	private WebButton scienceToolButton;
	private WebLabel totalAchievementLabel;

	private JTable achievementTable;
	private JTable studyTable;

	private StudyTableModel studyTableModel;
	private AchievementTableModel achievementTableModel;


	/**
	 * Constructor.
	 * @param settlement the settlement.
	 * @param desktop the main desktop.
	 */
	public TabPanelScience(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelScience.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelScience.tooltip"), //$NON-NLS-1$
			settlement, desktop
		);

		this.settlement = settlement;

	}
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		// Create the title panel.
		WebPanel titlePane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		// Create the title label.
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelScience.label"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(titleLabel);

		// Create the main panel.
		WebPanel mainPane = new WebPanel(new GridLayout(2, 1, 0, 0));
		centerContentPanel.add(mainPane);

		// Create the studies panel.
		WebPanel studiesPane = new WebPanel(new BorderLayout());
//		studiesPane.setBorder(new MarsPanelBorder());
		mainPane.add(studiesPane);

		// Create the studies label.
		WebLabel studiesLabel = new WebLabel(Msg.getString("TabPanelScience.scientificStudies"), WebLabel.CENTER); //$NON-NLS-1$
		studiesPane.add(studiesLabel, BorderLayout.NORTH);

		// Create the study scroll panel.
		WebScrollPane studyScrollPane = new WebScrollPane();
//		studyScrollPane.setBorder(new MarsPanelBorder());
		studyScrollPane.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		studiesPane.add(studyScrollPane, BorderLayout.CENTER);

		// Create the study table.
		studyTableModel = new StudyTableModel(settlement);
		studyTable = new ZebraJTable(studyTableModel);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		studyTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
		studyTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
		
		studyTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
//		studyTable.setCellSelectionEnabled(false);
		studyTable.setRowSelectionAllowed(true);
		studyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		studyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting()) {
					if (studyTable.getSelectedRow() >= 0) setEnabledScienceToolButton(true);
				}
			}
		});
		studyScrollPane.setViewportView(studyTable);

		// Added sorting
		studyTable.setAutoCreateRowSorter(true);

		TableStyle.setTableStyle(studyTable);

		// Create the button panel.
		WebPanel buttonPane = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		studiesPane.add(buttonPane, BorderLayout.SOUTH);

		// Create the science tool button.
		scienceToolButton = new WebButton(ImageLoader.getIcon(Msg.getString("img.science"))); //$NON-NLS-1$
		scienceToolButton.setEnabled(false);
		scienceToolButton.setMargin(new Insets(1, 1, 1, 1));
		scienceToolButton.setToolTipText(Msg.getString("TabPanelScience.tooltip.science")); //$NON-NLS-1$
		scienceToolButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				displayStudyInScienceTool();
			}
		});
		buttonPane.add(scienceToolButton);

		// Create the achievement panel.
		WebPanel achievementPane = new WebPanel(new BorderLayout());
//		achievementPane.setBorder(new MarsPanelBorder());
		mainPane.add(achievementPane);

		// Create achievement label panel.
		WebPanel achievementLabelPane = new WebPanel(new GridLayout(2, 1, 0, 0));
		achievementPane.add(achievementLabelPane, BorderLayout.NORTH);

		// Create the achievement label.
		WebLabel achievementLabel = new WebLabel(Msg.getString("TabPanelScience.scientificAchievement"), WebLabel.CENTER); //$NON-NLS-1$
		achievementLabelPane.add(achievementLabel);

		DecimalFormat formatter = new DecimalFormat(Msg.getString("TabPanelScience.decimalFormat")); //$NON-NLS-1$
		String totalAchievementString = formatter.format(settlement.getTotalScientificAchievement());
		totalAchievementLabel = new WebLabel(
			Msg.getString(
				"TabPanelScience.totalAchievementCredit", //$NON-NLS-1$
				totalAchievementString
			),WebLabel.CENTER
		);
		achievementLabelPane.add(totalAchievementLabel);

		// Create the achievement scroll panel.
		WebScrollPane achievementScrollPane = new WebScrollPane();
//		achievementScrollPane.setBorder(new MarsPanelBorder());
		achievementScrollPane.setHorizontalScrollBarPolicy(WebScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		achievementPane.add(achievementScrollPane, BorderLayout.CENTER);

		// Create the achievement table.
		achievementTableModel = new AchievementTableModel(settlement);
		achievementTable = new ZebraJTable(achievementTableModel);
		achievementTable.setPreferredScrollableViewportSize(new Dimension(225, -1));
		achievementTable.setRowSelectionAllowed(true);
		achievementTable.setDefaultRenderer(Double.class, new NumberCellRenderer(1));
		achievementScrollPane.setViewportView(achievementTable);

		// Added sorting
		achievementTable.setAutoCreateRowSorter(true);

		// Align the preference score to the center of the cell
		DefaultTableCellRenderer renderer1 = new DefaultTableCellRenderer();
		renderer1.setHorizontalAlignment(SwingConstants.CENTER);
		achievementTable.getColumnModel().getColumn(0).setCellRenderer(renderer1);
		achievementTable.getColumnModel().getColumn(1).setCellRenderer(renderer1);

		TableStyle.setTableStyle(achievementTable);
	}

	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		TableStyle.setTableStyle(studyTable);
		TableStyle.setTableStyle(achievementTable);

		// Get selected study in table if any.
		int selectedStudyIndex = studyTable.getSelectedRow();
		ScientificStudy selectedStudy = null;
		if (selectedStudyIndex >= 0) selectedStudy = studyTableModel.getStudy(selectedStudyIndex);

		// Update study table model.
		studyTableModel.update();

		// Reselect study in table.
		if (selectedStudy != null) {
			int newStudyIndex = studyTableModel.getStudyIndex(selectedStudy);
			if (newStudyIndex >= 0)
				studyTable.getSelectionModel().setSelectionInterval(newStudyIndex, newStudyIndex);
		}

		// Update achievement table model.
		achievementTableModel.update();

		// Update total achievement label.
		Settlement settlement = (Settlement) getUnit();
		DecimalFormat formatter = new DecimalFormat(Msg.getString("TabPanelScience.decimalFormat")); //$NON-NLS-1$
		String totalAchievementString = formatter.format(settlement.getTotalScientificAchievement());
		//totalAchievementLabel.setText(Msg.getString("TabPanelScience.totalAchievementCredit") + totalAchievementString); //$NON-NLS-1$
		totalAchievementLabel.setText(Msg.getString("TabPanelScience.totalAchievementCredit", totalAchievementString)); //$NON-NLS-1$
	}

	/**
	 * Sets if the science tool button is enabled or not.
	 * @param enabled true if button enabled.
	 */
	private void setEnabledScienceToolButton(boolean enabled) {
		scienceToolButton.setEnabled(enabled);
	}

	/**
	 * Displays the scientific study selected in the table in the science tool.
	 */
	private void displayStudyInScienceTool() {
		int selectedStudyIndex = studyTable.getSelectedRow();
		if (selectedStudyIndex >= 0) {
			ScientificStudy selectedStudy = studyTableModel.getStudy(selectedStudyIndex);
			((ScienceWindow) desktop.getToolWindow(ScienceWindow.NAME)).setScientificStudy(selectedStudy);
			getDesktop().openToolWindow(ScienceWindow.NAME);
		}
	}

	/**
	 * Inner class for study table model.
	 */
	private class StudyTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private Settlement settlement;
		private List<ScientificStudy> studies;

		/**
		 * Constructor
		 * @param settlement the settlement.
		 */
		private StudyTableModel(Settlement settlement) {
			// Use AbstractTableModel constructor.
			super();

			this.settlement = settlement;

			// Get all studies the settlement is primary for.
			ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
			studies = manager.getAllStudies(settlement);
		}

		/**
		 * Returns the number of columns in the model.
		 * @return the number of columns in the model.
		 */
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelScience.column.study"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelScience.column.phase"); //$NON-NLS-1$
			else return null;
		}

		/**
		 * Returns the number of rows in the model.
		 * @return the number of rows in the model.
		 */
		public int getRowCount() {
			return studies.size();
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param rowIndex the row whose value is to be queried.
		 * @param columnIndex the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			String result = null;
			if ((rowIndex >= 0) && (rowIndex < studies.size())) {
				ScientificStudy study = studies.get(rowIndex);
				// 2014-12-01 Added Conversion.capitalize()
				if (columnIndex == 0) result = Conversion.capitalize(study.toString());
				else if (columnIndex == 1) {
					if (study.isCompleted()) result = study.getCompletionState();
					else result = study.getPhase();
				}
			}
			return result;
		}

		/**
		 * Updates the table model.
		 */
		private void update() {
			ScientificStudyManager manager = Simulation.instance().getScientificStudyManager();
			List<ScientificStudy> newStudies = manager.getAllStudies(settlement);
			if (!newStudies.equals(studies)) studies = newStudies;
			fireTableDataChanged();
		}

		/**
		 * Gets the scientific study in the table at a given row index.
		 * @param rowIndex the row index in the table.
		 * @return scientific study or null if invalid index.
		 */
		private ScientificStudy getStudy(int rowIndex) {
			ScientificStudy result = null;
			if ((rowIndex >= 0) && (rowIndex < studies.size()))
				result = studies.get(rowIndex);
			return result;
		}

		/**
		 * Gets the row index of a given scientific study.
		 * @param study the scientific study.
		 * @return the table row index or -1 if not in table.
		 */
		private int getStudyIndex(ScientificStudy study) {
			int result = -1;
			if ((study != null) && studies.contains(study)) result = studies.indexOf(study);
			return result;
		}
	}

	/**
	 * Inner class for achievement table model.
	 */
	private class AchievementTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		// Data members.
		private Settlement settlement;
		private List<ScienceType> sciences;

		/** hidden constructor. */
		private AchievementTableModel(Settlement settlement) {
			// Use AbstractTableModel constructor.
			super();
			this.settlement = settlement;
			sciences = ScienceType.valuesList();
		}

		/**
		 * Returns the number of columns in the model.
		 * @return the number of columns in the model.
		 */
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelScience.column.science"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelScience.column.achievementCredit"); //$NON-NLS-1$
			else return null;
		}

		/**
		 * Returns the most specific superclass for all the cell values in the column.
		 * @param columnIndex the index of the column.
		 * @return the common ancestor class of the object values in the model.
		 */
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = Double.class;
			return dataType;
		}

		/**
		 * Returns the number of rows in the model.
		 * @return the number of rows in the model.
		 */
		public int getRowCount() {
			return sciences.size();
		}

		/**
		 * Returns the value for the cell at columnIndex and rowIndex.
		 * @param rowIndex the row whose value is to be queried.
		 * @param columnIndex the column whose value is to be queried.
		 * @return the value Object at the specified cell.
		 */
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object result = null;
			if ((rowIndex >= 0) && (rowIndex < sciences.size())) {
				ScienceType science = sciences.get(rowIndex);
				if (columnIndex == 0) result = science.getName();
				else if (columnIndex == 1) {
					result = settlement.getScientificAchievement(science);
				}
			}
			return result;
		}

		/**
		 * Updates the table model.
		 */
		private void update() {
			fireTableDataChanged();
		}
	}
	
	/**
     * Prepare object for garbage collection.
     */
    public void destroy() {
    	scienceToolButton = null;
    	totalAchievementLabel = null;

    	achievementTable = null;
    	studyTable = null;

    	studyTableModel = null;
    	achievementTableModel = null;

    }
}