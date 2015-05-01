/**
 * Mars Simulation Project
 * TabPanelCareer.java
 * @version 3.08 2015-03-31
 * @author Manny KUng
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.medical.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelCareer is a tab panel for viewing a person's career path and job history.
 */
public class TabPanelCareer
extends TabPanel
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** data cache */
	private String jobCache = ""; //$NON-NLS-1$

	private JLabel jobLabel, roleLabel, errorLabel;

	private JComboBoxMW<?> jobComboBox;

	private JobHistoryTableModel jobHistoryTableModel;

	/**
	 * Constructor.
	 * @param unit {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelCareer(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCareer.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelCareer.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

	    Person person = null;
	    Robot robot = null;
		Mind mind = null;
		BotMind botMind = null;
		boolean dead = false;
		DeathInfo deathInfo = null;

	    if (unit instanceof Person) {
	    	person = (Person) unit;
			mind = person.getMind();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		}
		else if (unit instanceof Robot) {
	        robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getPhysicalCondition().isDead();
			deathInfo = robot.getPhysicalCondition().getDeathDetails();
		}

		// Prepare label panel
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare job type label
		JLabel label = new JLabel(Msg.getString("TabPanelCareer.title"), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(label);

		if (unit instanceof Person) {
	    	person = (Person) unit;

			// Prepare job panel
			JPanel topPanel = new JPanel(new GridLayout(3, 1, 0, 0));
			topPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(topPanel);

			JPanel jobPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); //new GridLayout(3, 1, 0, 0)); //
			topPanel.add(jobPanel);

			// Prepare job label
			jobLabel = new JLabel(Msg.getString("TabPanelCareer.jobType"), JLabel.CENTER); //$NON-NLS-1$
			jobPanel.add(jobLabel);

			// Prepare job combo box
			jobCache = mind.getJob().getName(person.getGender());
			List<String> jobNames = new ArrayList<String>();
			for (Job job : JobManager.getJobs()) {
				jobNames.add(job.getName(person.getGender()));
			}

			Collections.sort(jobNames);
			jobComboBox = new JComboBoxMW<Object>(jobNames.toArray());
			jobComboBox.setSelectedItem(jobCache);
			jobComboBox.addActionListener(this);
			jobPanel.add(jobComboBox);

			// Prepare role panel
			JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			rolePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			topPanel.setBorder(new MarsPanelBorder());
			topPanel.add(rolePanel);

			// Prepare role label
			roleLabel = new JLabel(Msg.getString("TabPanelCareer.roleType"), JLabel.CENTER); //$NON-NLS-1$
			//roleLabel.setBorder(new MarsPanelBorder());
			//roleLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			rolePanel.add(roleLabel);

			errorLabel = new JLabel();
			errorLabel.setFont(new Font("Courier New", Font.ITALIC, 12));
			errorLabel.setForeground(Color.red);
			topPanel.add(errorLabel);

		}

		else if (unit instanceof Robot) {
			/*
	        robot = (Robot) unit;
			botMind = robot.getBotMind();
			// Prepare job combo box
			jobCache = botMind.getRobotJob().getName(robot.getRobotType());
			List<String> jobNames = new ArrayList<String>();
			for (RobotJob robotJob : JobManager.getRobotJobs()) {
				jobNames.add(robotJob.getName(robot.getRobotType()));
			}
			Collections.sort(jobNames);
			jobComboBox = new JComboBoxMW<Object>(jobNames.toArray());
			jobComboBox.setSelectedItem(jobCache);
			jobComboBox.addActionListener(this);
			jobPanel.add(jobComboBox);
			*/
		}

		// Prepare job title panel
		JPanel jobHistoryPanel = new JPanel(new GridLayout(2, 1, 0, 0));
		centerContentPanel.add(jobHistoryPanel, BorderLayout.NORTH);

		// Prepare job title label
		JLabel historyLabel = new JLabel(Msg.getString("TabPanelCareer.history"), JLabel.CENTER); //$NON-NLS-1$
		jobHistoryPanel.add(new JLabel());
		jobHistoryPanel.add(historyLabel, BorderLayout.NORTH);

		// Create schedule table model
		if (unit instanceof Person)
			jobHistoryTableModel = new JobHistoryTableModel((Person) unit);
		else if (unit instanceof Robot)
			jobHistoryTableModel = new JobHistoryTableModel((Robot) unit);

		// Create attribute scroll panel
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(scrollPanel, BorderLayout.CENTER);

		// Create schedule table
		JTable table = new JTable(jobHistoryTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.setCellSelectionEnabled(false);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		scrollPanel.setViewportView(table);

		update();

		jobHistoryTableModel.update();

	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {

	    Person person = null;
	    Robot robot = null;
		Mind mind = null;
		BotMind botMind = null;
		boolean dead = false;
		DeathInfo deathInfo = null;

		String currentJob = null;

	    if (unit instanceof Person) {
	    	person = (Person) unit;
			mind = person.getMind();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();

			String role = person.getRole().toString();
			roleLabel.setText(Msg.getString("TabPanelCareer.roleType") + " : " + role);

			// Update job if necessary.
			if (dead) {
				jobCache = deathInfo.getJob();
				jobComboBox.setEnabled(false);
			}
/*
			else {
				//jobCache = mind.getJob().getName(person.getGender());
				//currentJob = mind.getJob().getName(person.getGender());

				String selectedJobStr = (String) jobComboBox.getSelectedItem();

				if (!jobCache.equals(selectedJobStr)) {
				    jobComboBox.setSelectedItem(selectedJobStr);
				    // TODO: should we inform jobHistoryTableModel to update a person's job to selectedJob
				    // as soon as the combobox selection is changed or wait for checking of "approval" ?
					jobHistoryTableModel.update();
					jobCache = selectedJobStr;
				}
			}
*/
		}
		else if (unit instanceof Robot) {
	        robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getPhysicalCondition().isDead();
			deathInfo = robot.getPhysicalCondition().getDeathDetails();


		}

	}

	/**
	 * Action event occurs.
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source == jobComboBox) {
			Person person = null;
			Robot robot = null;

			if (unit instanceof Person) {
				person = (Person) unit;

				String selectedJobStr = (String) jobComboBox.getSelectedItem();
				String jobStrCache = person.getMind().getJob().getName(person.getGender());

				// 2015-04-30 if job is Manager, loads and set to the previous job and quit;
				if (jobStrCache.equals("Manager")) {
					jobComboBox.setSelectedItem(jobStrCache);
					errorLabel.setText("Mayor cannot switch job arbitrary!");
					errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
				}

				else if (selectedJobStr.equals("Manager")) {
					jobComboBox.setSelectedItem(jobStrCache);
					errorLabel.setText("Manager job is available for Mayor only!");
					errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
				}

				else if (!jobCache.equals(selectedJobStr)) {
					errorLabel.setText("");
				    jobComboBox.setSelectedItem(selectedJobStr);
				    // TODO: should we inform jobHistoryTableModel to update a person's job to selectedJob
				    // as soon as the combobox selection is changed or wait for checking of "approval" ?
				    // update to the new selected job
				    Job selectedJob = null;
					Iterator<Job> i = JobManager.getJobs().iterator();
					while (i.hasNext()) {
					    Job job = i.next();
					    String n = job.getName(person.getGender());
						if (selectedJobStr.equals(n))
							// gets selectedJob by running through iterator to match it
					        selectedJob = job;
					}

					person.getMind().setJob(selectedJob, true, JobManager.USER);
					// updates the jobHistoryList in jobHistoryTableModel
					jobHistoryTableModel.update();
					//System.out.println("Yes they are diff");
					jobCache = selectedJobStr;
				}
/*
				person = (Person) unit;
				String jobStrCache = person.getMind().getJob().getName(person.getGender());
				if (!selectedJobStr.equals(jobStrCache)) {
					Job selectedJob = null;
					Iterator<Job> i = JobManager.getJobs().iterator();
					while (i.hasNext()) {
					    Job job = i.next();
					    String n = job.getName(person.getGender());
						if (selectedJobStr.equals(n))
							// gets selectedJob by running through iterator to match it
					        selectedJob = job;
					}
					// update to the new selected job
					person.getMind().setJob(selectedJob, true, JobManager.USER);
					// updates the jobHistoryList in jobHistoryTableModel
					jobHistoryTableModel.update();
					System.out.println("Yes they are diff");
				}
*/
			}


			else if (unit instanceof Robot) {
				/*
				robot = (Robot) unit;

				RobotJob selectedJob = null;
				Iterator<RobotJob> i = JobManager.getRobotJobs().iterator();
				while (i.hasNext() && (selectedJob == null)) {
					RobotJob robotJob = i.next();
					//System.out.println("job : " + job.);
					if (jobName.equals(robotJob.getName(robot.getRobotType()))) {
				        selectedJob = robotJob;
				    }
				}

				robot.getBotMind().setRobotJob(selectedJob, true);
				*/
			}
		}
	}


	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class JobHistoryTableModel
	extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private JobHistory jobHistory;

		private List<JobAssignment> jobAssignmentList;

		/**
		 * hidden constructor.
		 * @param unit {@link Unit}
		 */
		private JobHistoryTableModel(Unit unit) {
	        Person person = null;
	        Robot robot = null;
	        if (unit instanceof Person) {
	         	person = (Person) unit;
	         	jobHistory = person.getJobHistory();
	        }
	        else if (unit instanceof Robot) {
	        	//robot = (Robot) unit;
	        	//jobHistory = robot.getJobHistory();
	        }

	        jobAssignmentList = jobHistory.getJobAssignmentList();

		}

		@Override
		public int getRowCount() {
			if (jobAssignmentList != null)
				return jobAssignmentList.size();
			else
				return 0;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = String.class;
			if (columnIndex == 2) dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelCareer.column.time"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelCareer.column.jobType"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelCareer.column.by"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			int r = jobAssignmentList.size() - row - 1;
			//System.out.println(" r is " + r);
			if (column == 0) return MarsClock.getDateTimeStamp(jobAssignmentList.get(r).getTime());
			else if (column == 1) return jobAssignmentList.get(r).getJobType();
			else if (column == 2) return jobAssignmentList.get(r).getInitiator();
			else return null;
		}

		/**
		 * Prepares the job history of the person
		 * @param
		 * @param
		 */
		private void update() {

			jobAssignmentList = jobHistory.getJobAssignmentList();

        	fireTableDataChanged();

		}

	}

}