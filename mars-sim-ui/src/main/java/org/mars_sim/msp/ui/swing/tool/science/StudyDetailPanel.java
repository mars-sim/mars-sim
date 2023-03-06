/**
 * Mars Simulation Project
 * StudyDetailPanel.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.ui.swing.NumberCellRenderer;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;
import org.mars_sim.msp.ui.swing.utils.UnitModel;
import org.mars_sim.msp.ui.swing.utils.UnitTableLauncher;

/**
 * A panel showing details of a selected scientific study.
 */
@SuppressWarnings("serial")
public class StudyDetailPanel
extends JPanel {

	// Data members
	private JLabel scienceFieldLabel;
	private JLabel levelLabel;
	private JLabel phaseLabel;
	private JLabel nameLabel;
	private JLabel leadResearcher;

	private ScientificStudy study;
	private ResearchTableModel researcherModel;
	private JLabel topics;
	private JProgressBar progress;
	
	/**
	 * Constructor
	 */
	StudyDetailPanel(ScienceWindow scienceWindow) {
		// Use JPanel constructor.
		super(new BorderLayout());

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(425, -1));

		JLabel titleLabel = new JLabel(Msg.getString("StudyDetailPanel.details"), JLabel.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(titleLabel);
		add(titleLabel, BorderLayout.NORTH);

		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(BorderFactory.createEtchedBorder());
		add(mainPane, BorderLayout.CENTER);

		AttributePanel infoPane = new AttributePanel(7);
		infoPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPane.add(infoPane, BorderLayout.NORTH);

		nameLabel = infoPane.addTextField("Name", "N/A", null);
		scienceFieldLabel = infoPane.addTextField(Msg.getString("StudyDetailPanel.science"), "N/A", null);
		levelLabel = infoPane.addTextField(Msg.getString("StudyDetailPanel.level"), "N/A", null);
		phaseLabel = infoPane.addTextField(Msg.getString("StudyDetailPanel.phase"), "N/A", null);
		leadResearcher = infoPane.addTextField(Msg.getString("StudyDetailPanel.lead"), "N/A", null);
		topics = infoPane.addTextField(Msg.getString("StudyDetailPanel.topics"), "N/A", null);

		progress = new JProgressBar(0, 100);
		progress.setStringPainted(true);
		infoPane.addLabelledItem(Msg.getString("StudyDetailPanel.completed"), progress);

		// Create table of researcher
		JScrollPane scrollPanel = new JScrollPane();
		mainPane.add(scrollPanel, BorderLayout.CENTER);
		
		// Create schedule table
		researcherModel = new ResearchTableModel();
		JTable table = new JTable(researcherModel);
		table.addMouseListener(new UnitTableLauncher(scienceWindow.getDesktop()));
		table.setAutoCreateRowSorter(true);
		scrollPanel.setViewportView(table);

		TableColumnModel tc = table.getColumnModel();
		tc.getColumn(0).setPreferredWidth(80);
		tc.getColumn(1).setPreferredWidth(60);
		tc.getColumn(2).setPreferredWidth(20);
		tc.getColumn(2).setCellRenderer(new NumberCellRenderer(0));
	}

	/**
	 * Updates the panel.
	 */
	void update() {
		if (study != null) {
			// Update the status label.
			phaseLabel.setText(getPhaseString(study));

			researcherModel.update();	
			updateProgressBar();		
		}
	}

	/**
	 * Display information about a scientific study.
	 * @param study the scientific study.
	 */
	boolean displayScientificStudy(ScientificStudy study) {
		boolean newSelection = false;
		if ((this.study == null) || !this.study.equals(study)) {
			this.study = study;
			newSelection = true;

			nameLabel.setText(study.getName());
			scienceFieldLabel.setText(study.getScience().getName());
			levelLabel.setText(Integer.toString(study.getDifficultyLevel()));
			phaseLabel.setText(getPhaseString(study));
			leadResearcher.setText(study.getPrimaryResearcher().getName());

			String topicsText = study.getTopic().stream().collect(Collectors.joining(","));
			topics.setText(topicsText);
			topics.setToolTipText(topicsText);

			researcherModel.reset(study);		
			
			updateProgressBar();
		}

		
		return newSelection;
	}

	private void updateProgressBar() {
		double value = study.getPhaseProgress();
		progress.setValue((int)(value * 100));
	}


	/**
	 * Get the phase string for a scientific study.
	 * @param study the scientific study.
	 * @return the phase string.
	 */
	private static String getPhaseString(ScientificStudy study) {
		String result = ""; //$NON-NLS-1$

		if (study != null) {
			if (!study.isCompleted()) result = study.getPhase();
			else result = study.getCompletionState();
		}

		return result;
	}

	private static class ResearchTableModel extends AbstractTableModel implements UnitModel {

		public final static int NAME = 0;
		public final static int CONTRIBUTION = 1;
		public final static int WORK = 2;

		private List<Person> researchers = new ArrayList<>();
		private ScientificStudy study;

		public void reset(ScientificStudy study) {
			this.study = study;

			researchers.clear();
			researchers.add(study.getPrimaryResearcher());
			researchers.addAll(study.getCollaborativeResearchers());

			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return researchers.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
				case NAME: return String.class;
				case CONTRIBUTION: return String.class;
				case WORK: return Double.class;
				default: return null;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
				case NAME: return "Name";
				case CONTRIBUTION: return "Contribution";
				case WORK: return "Work";
				default: return "";
			}
		}

		public Object getValueAt(int row, int column) {
			Person p = researchers.get(row);
			boolean isPrimary = (p.equals(study.getPrimaryResearcher()));

			// Safetly check
			if (!isPrimary && !study.getCollaborativeResearchers().contains(p)) {
				return null;
			}

			switch(column) {
				case NAME: return p.getName();
				case CONTRIBUTION: return (isPrimary ? "" : study.getContribution(p).getName());
				case WORK: {
					if (study.getPhase().equals(ScientificStudy.PAPER_PHASE)) {
						return (isPrimary ? study.getPrimaryPaperWorkTimeCompleted() 
											: study.getCollaborativePaperWorkTimeCompleted(p));
					}
					else if (study.getPhase().equals(ScientificStudy.RESEARCH_PHASE)) {
						return (isPrimary ? study.getPrimaryResearchWorkTimeCompleted() 
											: study.getCollaborativeResearchWorkTimeCompleted(p));
					}
					else {
						return 0D;
					}
				}
				default: return "";
			}

		}
		public void update() {
			// Check no one has joined
			Set<Person> newResearchers = study.getCollaborativeResearchers();
			boolean newRows = false;

			// Primary is already in the list
			for(Person r : newResearchers) {
				if (!researchers.contains(r)) {
					researchers.add(r);
					newRows = true;
				}
			}

			// Remove any colloborators no longer taking part
			Person lead = study.getPrimaryResearcher();
			List<Person> oldResearchers = new ArrayList<>();
			for(Person r : researchers) {
				if (!lead.equals(r) && !newResearchers.contains(r)) {
					oldResearchers.add(r);
					newRows = true;
				}
			}
			researchers.removeAll(oldResearchers);

			// Update table
			if (newRows) {
				fireTableDataChanged();
			}
			else {
				for(int r = 0; r < researchers.size(); r++) {
					fireTableCellUpdated(r, WORK);
				}
			}
		}

		@Override
		public Unit getAssociatedUnit(int row) {
			return researchers.get(row);
		}
	}
}
