/*
 * Mars Simulation Project
 * TabPanelCareer.java
 * @date 2023-06-17
 * @author Manny KUng
 */

package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityEventType;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.Unit;
import com.mars_sim.core.data.History.HistoryItem;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.Mind;
import com.mars_sim.core.person.ai.job.util.Assignment;
import com.mars_sim.core.person.ai.job.util.AssignmentHistory;
import com.mars_sim.core.person.ai.job.util.AssignmentType;
import com.mars_sim.core.person.ai.job.util.JobType;
import com.mars_sim.core.person.ai.job.util.JobUtil;
import com.mars_sim.core.person.ai.role.Role;
import com.mars_sim.core.person.ai.role.RoleType;
import com.mars_sim.core.person.health.DeathInfo;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.NamedListCellRenderer;
import com.mars_sim.ui.swing.components.StarRater;
import com.mars_sim.ui.swing.entitywindow.EntityTabPanel;
import com.mars_sim.ui.swing.utils.AttributePanel;

/**
 * The TabPanelCareer is a tab panel for viewing a person's career path and job
 * history and current role.
 */
@SuppressWarnings("serial")
class TabPanelCareer extends EntityTabPanel<Person>
			implements ActionListener, EntityListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelCareer.class.getName());

	private static final String CAREER_ICON = "career";

	private static final int RATING_DAYS = 7;

	/** data cache */
	private int solRatingSubmitted = -1;

	private JobType jobCache;
	private RoleType roleCache;
	private String dateTimeRatingSubmitted;

	private AssignmentType statusCache = AssignmentType.APPROVED;

	private JComboBox<JobType> jobComboBox;
	private JComboBox<RoleType> roleComboBox;

	private JobHistoryTableModel jobHistoryTableModel;
	private RoleHistoryTableModel roleHistoryTableModel;

	private StarRater starRater;
	private JLabel changeNotice;

	/**
	 * Constructor.
	 *
	 * @param unit    {@link Unit} the unit to display.
	 * @param context {@link UIContext} the UI context.
	 */
	public TabPanelCareer(Person unit, UIContext context) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelCareer.title"),	
			ImageLoader.getIconByName(CAREER_ICON),
			null, context, unit
		);
	}

	@Override
	protected void buildUI(JPanel content) {
 		StarRater aveRater;

		boolean dead = false;
		DeathInfo deathInfo = null;

		// Prepare label panel
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);

		var person = getEntity();
		Mind mind = person.getMind();

		AttributePanel attrPanel = new AttributePanel(4);
		northPanel.add(attrPanel, BorderLayout.NORTH);

		// Prepare job combo box
		jobCache = mind.getJobType();
		DefaultComboBoxModel<JobType> jobModel = new DefaultComboBoxModel<>(JobType.values());

		// Prepare job combo box
		jobComboBox = new JComboBox<>(jobModel);
		jobComboBox.setSelectedItem(jobCache);
		jobComboBox.addActionListener(this);
		jobComboBox.setRenderer(new NamedListCellRenderer());
		attrPanel.addLabelledItem(Msg.getString("Person.job"), jobComboBox);

		// Prepare role selector
		roleCache = person.getRole().getType();
		var roleModel = new DefaultComboBoxModel<RoleType>();
		roleModel.addAll(person.getAssociatedSettlement().getChainOfCommand().getGovernance().getAllRoles());
		roleComboBox = new JComboBox<>(roleModel);
		roleComboBox.setSelectedItem(roleCache);
		roleComboBox.addActionListener(this);
		roleComboBox.setRenderer(new NamedListCellRenderer());
		attrPanel.addLabelledItem(Msg.getString("Person.role"), roleComboBox);

		// Create ratings
		AssignmentHistory jobHistory = person.getJobHistory();
		aveRater = new StarRater(5, calculateAveRating(jobHistory));
		aveRater.setEnabled(false);
		attrPanel.addLabelledItem("Overall Performance", aveRater);

		// Create Star rating
		starRater = new StarRater(5, 0, 0);
		starRater.addStarListener(selection -> {
			if (starRater.isEnabled()) {
				var mTime = getNow();
				int sol = mTime.getMissionSol();
				dateTimeRatingSubmitted = mTime.getTruncatedDateTimeStamp();
				displayNotice("Job Rating submitted on " + dateTimeRatingSubmitted, false);
				starRater.setRating(selection);

				Assignment approved = person.getJobHistory().getLastApproved();
				approved.setJobRating(selection, sol);
			
				solRatingSubmitted = sol;
				starRater.setEnabled(false);

				aveRater.setRating(calculateAveRating(person.getJobHistory()));
			}
		});
		attrPanel.addLabelledItem("Your Rating", starRater);

		changeNotice = new JLabel("");
		changeNotice.setHorizontalAlignment(SwingConstants.CENTER);
		northPanel.add(changeNotice, BorderLayout.SOUTH);

		// Check if user submitted a job rating
		checkingJobRating(jobHistory);

		dead = person.getPhysicalCondition().isDead();
		deathInfo = person.getPhysicalCondition().getDeathDetails();

		// Checked if the person is dead
		if (dead) {
			jobCache = deathInfo.getJob();
			jobComboBox.setEnabled(false);
			roleComboBox.setEnabled(false);
			starRater.setSelection(0);
			starRater.setEnabled(false);

		} else
			// Add checking for the status of Job Reassignment
			checkJobReassignment(person, jobHistory);

		// Prepare job title panel
		JPanel historyPanel = new JPanel(new BorderLayout(0, 0));
		content.add(historyPanel, BorderLayout.CENTER);

		// Job history
		JPanel jobHistoryPanel = new JPanel(new BorderLayout(0, 0));
		historyPanel.add(jobHistoryPanel, BorderLayout.NORTH);
		JLabel historyLabel = new JLabel(Msg.getString("TabPanelCareer.history"), SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(historyLabel);
		jobHistoryPanel.add(historyLabel, BorderLayout.NORTH);
		jobHistoryTableModel = new JobHistoryTableModel(jobHistory);
		JScrollPane scrollPanel = new JScrollPane();
		jobHistoryPanel.add(scrollPanel, BorderLayout.CENTER);
		JTable table = new JTable(jobHistoryTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));

		TableColumnModel tc = table.getColumnModel();
		tc.getColumn(0).setPreferredWidth(25);
		tc.getColumn(1).setPreferredWidth(50);
		tc.getColumn(2).setPreferredWidth(50);
		tc.getColumn(3).setPreferredWidth(50);
		tc.getColumn(4).setPreferredWidth(50);

		scrollPanel.setViewportView(table);
		
		// Role history
		JPanel roleHistoryPanel = new JPanel(new BorderLayout(0, 0));
		historyPanel.add(roleHistoryPanel, BorderLayout.SOUTH);
		JLabel roleLabel = new JLabel("Role History", SwingConstants.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(roleLabel);
		roleHistoryPanel.add(roleLabel, BorderLayout.NORTH);
		roleHistoryTableModel = new RoleHistoryTableModel(person);
		JScrollPane rscrollPanel = new JScrollPane();
		roleHistoryPanel.add(rscrollPanel, BorderLayout.CENTER);
		JTable rtable = new JTable(roleHistoryTableModel);
		rtable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		rscrollPanel.setViewportView(rtable);

		updateDetails();
	}

	/**
	 * Checks a job rating is submitted or a job reassignment is submitted and is
	 * still not being reviewed.
	 *
	 * @param list
	 */
	private void checkingJobRating(AssignmentHistory jobHistory) {
		Assignment lastApproved = jobHistory.getLastApproved();
		if (solRatingSubmitted == -1) {
			solRatingSubmitted = lastApproved.getSolRatingSubmitted();
		}

		if (solRatingSubmitted == -1) {
			// no rating has ever been submitted. Thus he/she has a new job assignment
			starRater.setEnabled(true);
			starRater.setSelection(0);
			displayNotice("", false);
		} else {
			int solElapsed = getNow().getMissionSol();

			if (solElapsed > solRatingSubmitted + RATING_DAYS) {
				// if 7 days have passed since the rating submitted, re-enable the star rater
				starRater.setEnabled(true);
				starRater.setSelection(0);
				displayNotice("", false);
				dateTimeRatingSubmitted = null;
				solRatingSubmitted = -1;
			} else {
				starRater.setSelection(0);
				starRater.setEnabled(false);
				String s = "";
				if (dateTimeRatingSubmitted != null) {
					s = "Job Rating last submitted on " + dateTimeRatingSubmitted;
					displayNotice(s, false);
				}
			}
		}
	}

	private MarsTime getNow() {
		MasterClock masterClock = getContext().getSimulation().getMasterClock();
		return masterClock.getMarsTime();
	}

	private void displayNotice(String s, boolean error) {
		changeNotice.setText(s);
		changeNotice.setForeground((error ? Color.RED : Color.BLUE));
	}
	
	/**
	 * Calculates the cumulative career performance score of a person.
	 * 
	 * @param assignmentHistory
	 * @return
	 */
	private static int calculateAveRating(AssignmentHistory assignmentHistory) {
		double score = assignmentHistory.getCummulativeJobRating();
		if (score > 5)
			score = 5;
		return (int) score;
	}


	/**
	 * Checks for the status of job reassignment.
	 * 
	 * @param assignments
	 */
	private void checkJobReassignment(Person person, AssignmentHistory assignments) {
		List<HistoryItem<Assignment>> jobHistory = assignments.getJobAssignmentList();
		int last = jobHistory.size() - 1;

		AssignmentType status = jobHistory.get(last).getWhat().getStatus();

		if (person.getAssociatedSettlement().getChainOfCommand().getGovernance().needJobApproval()) {

			if (status == AssignmentType.PENDING) {
				statusCache = AssignmentType.PENDING;
				jobComboBox.setEnabled(false);
				
				String s = "Job Reassignment submitted on " + jobHistory.get(last).getWhen().getTruncatedDateTimeStamp();
				changeNotice.setText(s);
			}

			// Detect a change of status from pending to approved
			else if (statusCache == AssignmentType.PENDING) {
				if (status.equals(AssignmentType.APPROVED)) {
					statusCache = AssignmentType.APPROVED;
					logger.info(person, "Job reassignment reviewed and approved.");
					
					JobType selectedJob = jobHistory.get(last).getWhat().getType();

					if (jobCache != selectedJob) {
						changeJobCombo(selectedJob);
					}

					person.getMind().setJobLock(true);

				} else if (status == AssignmentType.NOT_APPROVED) {
					statusCache = AssignmentType.NOT_APPROVED;

					JobType selectedJob = jobHistory.get(last - 1).getWhat().getType();
	
					if (jobCache != selectedJob) {
						changeJobCombo(selectedJob);
					}
				}

				jobComboBox.setEnabled(true);
				changeNotice.setText("");

				// Note: determine if new rating submission should be allowed immediately at the
				// beginning of a new assignment
				solRatingSubmitted = -1;
				starRater.setSelection(0);
				starRater.setEnabled(true);

				// ates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();

				RoleType newRole = person.getRole().getType();
				if (roleCache != newRole) {
					roleCache = newRole;
					roleComboBox.setSelectedItem(roleCache);
					changeNotice.setText("Role just changed to " + newRole);
				}

			}
			else {
				// do nothing. It's at the start of sim
			}
		}
		else {
			JobType selectedJob = jobHistory.get(last).getWhat().getType();
			if (jobCache != selectedJob) {
				changeJobCombo(selectedJob);
			}
		}
	}

	private void changeJobCombo(JobType job) {
		jobComboBox.setSelectedItem(job);
		changeNotice.setText("Job just changed to " + job);
		jobCache = job;
 	}
	
	/**
	 * Change of Job or Role triggers a refresh
	 * @param event
	 */
	@Override
	public void entityUpdate(EntityEvent event) {
		if (EntityEventType.JOB_EVENT.equals(event.getType())
			|| Role.ROLE_EVENT.equals(event.getType())) {

			updateDetails();
		}
	}

	/**
	 * Refreshes the UI.
	 */
	@Override
	public void refreshUI() {
		updateDetails();
	}

	/**
	 * Updates the info on this panel.
	 */
	private void updateDetails() {
		var person = getEntity();

		var dead = person.getPhysicalCondition().isDead();

		// Update job if necessary.
		if (dead) {
			DeathInfo deathInfo = person.getPhysicalCondition().getDeathDetails();

			jobCache = deathInfo.getJob();
			jobComboBox.setEnabled(false);
			roleComboBox.setEnabled(false);
			starRater.setSelection(0);
			starRater.setEnabled(false);

		} else {

			AssignmentHistory list = person.getJobHistory();

			// Added checking if user submitted a job rating
			checkingJobRating(list);

			// Check for the status of Job Reassignment
			checkJobReassignment(person, list);

			jobHistoryTableModel.update();
			roleHistoryTableModel.update();
		} // end of else if not dead
	}

	/**
	 * Action event occurs.
	 *
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		var person = getEntity();

		if (source == roleComboBox) {
			RoleType selectedRole = (RoleType) roleComboBox.getSelectedItem();
			int box = -1;

			if (selectedRole != roleCache) {
				if (isTopRole(selectedRole) && !isTopRole(roleCache)) {
					box = JOptionPane.showConfirmDialog(getContext().getTopFrame(),
							"Are you sure you want to promote the person's role from " 
							 + roleCache + " to " + selectedRole.getName() + " ?");
				}

				else if (isTopRole(roleCache) && !isTopRole(selectedRole)) {
					box = JOptionPane.showConfirmDialog(getContext().getTopFrame(),
							"Are you sure you want to demote the person's role from "
							 + roleCache + " to "  + selectedRole.getName() + " ?");
				}

				else {
					box = JOptionPane.showConfirmDialog(getContext().getTopFrame(),
							"Are you sure you want to change the person's role from "
							 + roleCache + " to " + selectedRole.getName() + " ?");
				}

				if (box == JOptionPane.YES_OPTION) {
					person.getRole().changeRoleType(selectedRole);
					
					logger.info(person, "Player just changed his/her role from " 
							+ roleCache.getName() + " to " + selectedRole.getName() + ".");
					
					roleCache = selectedRole;
				}
				else {
					roleComboBox.setSelectedItem(roleCache);
				}
			}
		}

		else if (source == jobComboBox) {

			JobType selectedJob = (JobType)jobComboBox.getSelectedItem();

			if (selectedJob != jobCache) {
				int box = JOptionPane.showConfirmDialog(getContext().getTopFrame(),
						"Are you sure you want to change the person's job from "
						 + jobCache + " to " + selectedJob.getName() + " ?");
				
				if (box == JOptionPane.YES_OPTION) {
					considerJobChange(person, jobCache, selectedJob);
				}
				else {
					jobComboBox.setSelectedItem(jobCache.getName());
				}
			}
		}
	}

	private static boolean isTopRole(RoleType roleType) {
		return roleType.isChief() || roleType.isCouncil();
	}

	/**
	 * Determines if the job change request should be granted.
	 *
	 * @param person Person in question
	 * @param currentJob Current job
	 * @param selectedJob Select new Job
	 */
	private void considerJobChange(Person person, JobType currentJob, JobType selectedJob) {

		// if job is Politician, loads and set to the previous job and quit
		if (currentJob == JobType.POLITICIAN) {
			jobComboBox.setSelectedItem(currentJob);
			displayNotice("Mayor cannot switch job arbitrarily!", true);
		}

		else if (selectedJob == JobType.POLITICIAN) {
			jobComboBox.setSelectedItem(currentJob);
			displayNotice("Politician job is reserved for Mayor only.", true);
		}

		else if (currentJob != selectedJob) {

			// if the population is beyond 4
			if (person.getAssociatedSettlement().getChainOfCommand().getGovernance().needJobApproval()) {
				String s = "Job Reassignment submitted on " + MarsTimeFormat.getTruncatedDateTimeStamp(
											getNow());
				displayNotice(s, false);

				AssignmentHistory jh = person.getJobHistory();

				statusCache = AssignmentType.PENDING;

				jh.saveJob(selectedJob, JobUtil.USER, statusCache, null);
				// Set the combobox selection back to its previous job type for the time being
				// until the reassignment is approved
				jobComboBox.setSelectedItem(currentJob);
				// disable the combobox so that user cannot submit job reassignment for a period
				// of time
				jobComboBox.setEnabled(false);
				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();
			}

			else {
				displayNotice("", false);
				jobComboBox.setSelectedItem(selectedJob);
				// pop is small, things need to be flexible. Thus automatic approval
				statusCache = AssignmentType.APPROVED;
				person.getMind().reassignJob(selectedJob, true, JobUtil.USER, statusCache,
						JobUtil.USER);

				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();
			}
		}
		else
			jobComboBox.setSelectedItem(currentJob);
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class RoleHistoryTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private Role role;
		private int origSize;
		private List<HistoryItem<RoleType>> roleChanges;

		/**
		 * hidden constructor.
		 *
		 * @param unit {@link Unit}
		 */
		RoleHistoryTableModel(Person p) {
			role = p.getRole();
			roleChanges = role.getChanges();
			origSize = roleChanges.size();
		}

		@Override
		public int getRowCount() {
			return roleChanges.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> Msg.getString("Time");
				case 1 -> Msg.getString("Person.role");
				default -> null;
			};
		}

		public Object getValueAt(int row, int column) {
			HistoryItem<RoleType> ja = roleChanges.get(row);
			return switch(column) {
				case 0 -> ja.getWhen().getTruncatedDateTimeStamp(); 
				case 1 -> ja.getWhat().getName();
				default -> null;
			};
		}

		/**
		 * Prepares the job history of the person.
		 */
		void update() {
			if (roleChanges.size() != origSize) {
				origSize = roleChanges.size();
				fireTableDataChanged();
			}
		}
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class JobHistoryTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private List<HistoryItem<Assignment>> jobAssignmentList;
		private int origSize;

		/**
		 * hidden constructor.
		 *
		 * @param unit {@link Unit}
		 */
		JobHistoryTableModel(AssignmentHistory jobHistory) {
			jobAssignmentList = jobHistory.getJobAssignmentList();
			origSize = jobAssignmentList.size();
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
			return 5;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return switch(columnIndex) {
				case 0 -> Msg.getString("Time"); //$NON-NLS-1$
				case 1 -> Msg.getString("Person.job"); //$NON-NLS-1$
				case 2 -> Msg.getString("TabPanelCareer.column.initiated"); //$NON-NLS-1$
				case 3 -> Msg.getString("TabPanelCareer.column.status"); //$NON-NLS-1$
				case 4 -> Msg.getString("TabPanelCareer.column.authorized"); //$NON-NLS-1$
				default -> null;
			};
		}

		public Object getValueAt(int row, int column) {
			int r = jobAssignmentList.size() - row - 1;
			HistoryItem<Assignment> item = jobAssignmentList.get(r);
			Assignment ja = item.getWhat();
			return switch(column) {
				case 0 -> item.getWhen().getTruncatedDateTimeStamp();
				case 1 -> ja.getType().getName();
				case 2 -> ja.getInitiator();
				case 3 -> ja.getStatus().getName();
				case 4 -> ja.getAuthorizedBy();
				default -> null;
			};
		}

		/**
		 * Prepares the job history of the person.
		 */
		void update() {
			if (origSize != jobAssignmentList.size()) {
				origSize = jobAssignmentList.size();
				fireTableDataChanged();
			}
		}
	}
}