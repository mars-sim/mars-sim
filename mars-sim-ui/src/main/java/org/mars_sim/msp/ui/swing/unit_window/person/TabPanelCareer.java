/*
 * Mars Simulation Project
 * TabPanelCareer.java
 * @date 2023-04-08
 * @author Manny KUng
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.data.History.HistoryItem;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.util.Job;
import org.mars_sim.msp.core.person.ai.job.util.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.util.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.util.JobHistory;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.person.ai.role.Role;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MarsTimeFormat;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.StyleManager;
import org.mars_sim.msp.ui.swing.tool.StarRater;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.utils.AttributePanel;

/**
 * The TabPanelCareer is a tab panel for viewing a person's career path and job
 * history and current role.
 */
@SuppressWarnings("serial")
public class TabPanelCareer extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelCareer.class.getName());

	private static final String CAREER_ICON = "career";

	private static final int RATING_DAYS = 7;

	/** data cache */
	private int solCache = 1;
	private int solRatingSubmitted = -1;

	/** Is UI constructed. */
	private boolean firstNotification = true;
	private boolean printLog;
	private boolean printLog2;

	private JobType jobCache;
	private RoleType roleCache;
	private String dateTimeRatingSubmitted;

	private JobAssignmentType statusCache = JobAssignmentType.APPROVED;// PENDING;

	private JComboBox<String> jobComboBox;
	private JComboBox<String> roleComboBox;

	private JobHistoryTableModel jobHistoryTableModel;
	private RoleHistoryTableModel roleHistoryTableModel;

	private StarRater starRater;
	private StarRater aveRater;

	/** The Person instance. */
	private Person person;
	private Settlement settlement;

	private MasterClock masterClock;

	private JLabel changeNotice;


	/**
	 * Constructor.
	 *
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelCareer(Person unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,	
			ImageLoader.getIconByName(CAREER_ICON),
			Msg.getString("TabPanelCareer.title"), //$NON-NLS-1$
			desktop
		);

		person = unit;
		masterClock = getSimulation().getMasterClock();

		if (person.getAssociatedSettlement() != null) {
			settlement = person.getAssociatedSettlement();
		}
		else {
			if (person.isBuried()) {
				settlement = person.getBuriedSettlement();
			}
			else
				settlement = person.getLocationTag().findSettlementVicinity();
		}
	}

	@Override
	protected void buildUI(JPanel content) {

		boolean dead = false;
		DeathInfo deathInfo = null;

		// Prepare label panel
		JPanel northPanel = new JPanel(new BorderLayout());
		content.add(northPanel, BorderLayout.NORTH);

		Mind mind = person.getMind();
		dead = person.getPhysicalCondition().isDead();
		deathInfo = person.getPhysicalCondition().getDeathDetails();

		AttributePanel attrPanel = new AttributePanel(4);
		northPanel.add(attrPanel, BorderLayout.NORTH);

		// Prepare job combo box
		jobCache = mind.getJob();
		Vector<String> jobNames = new Vector<>();
		for (Job job : JobUtil.getJobs()) {
			jobNames.add(job.getName(person.getGender()));
		}
		Collections.sort(jobNames);

		// Prepare job combo box
		jobComboBox = new JComboBox<>(jobNames);
		jobComboBox.setSelectedItem(jobCache.getName());
		jobComboBox.addActionListener(this);
		jobComboBox.setToolTipText(Msg.getString("TabPanelCareer.jobType.tooltip"));
		attrPanel.addLabelledItem(Msg.getString("TabPanelCareer.jobType"), jobComboBox);


		// Prepare role selector
		roleCache = person.getRole().getType();
		List<String> roleNames = RoleUtil.getRoleNames(settlement.getNumCitizens());
		roleComboBox = new JComboBox<>(roleNames.toArray(new String[0]));
		roleComboBox.setSelectedItem(roleCache.getName());
		roleComboBox.addActionListener(this);
		roleComboBox.setToolTipText(Msg.getString("TabPanelCareer.roleType.tooltip"));
		attrPanel.addLabelledItem(Msg.getString("TabPanelCareer.roleType"), roleComboBox);

		// Create ratings
		List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();
		aveRater = new StarRater(5, calculateAveRating(list));
		aveRater.setEnabled(false);
		aveRater.setToolTipText(Msg.getString("TabPanelCareer.aveRater.tooltip"));
		attrPanel.addLabelledItem("Overall Performance", aveRater);

		// Create Star rating
		starRater = new StarRater(5, 0, 0);
		starRater.setToolTipText(Msg.getString("TabPanelCareer.starRater.tooltip"));//$NON-NLS-1$
		starRater.addStarListener(new StarRater.StarListener() {
			public void handleSelection(int selection) {
				if (starRater.isEnabled()) {

					MarsTime mTime = masterClock.getMarsTime();
					int sol = mTime.getMissionSol();
					dateTimeRatingSubmitted = mTime.getTruncatedDateTimeStamp();
					printLog = true;
					displayNotice("Job Rating submitted on " + dateTimeRatingSubmitted, false);
					starRater.setRating(selection);

					int size = list.size();
					// check if a new job reassignment has just been submitted
					if (list.get(size - 1).getStatus() == JobAssignmentType.PENDING) {
						list.get(size - 2).setJobRating(selection);
						list.get(size - 2).setSolRatingSubmitted(sol);
					} else {
						list.get(size - 1).setJobRating(selection);
						list.get(size - 1).setSolRatingSubmitted(sol);
					}
					solRatingSubmitted = sol;
					// starRater.setSelection(0);
					starRater.setEnabled(false);// disable();

					aveRater.setRating(calculateAveRating(list));
				}
			}
		});
		attrPanel.addLabelledItem("Your Rating", starRater);

		changeNotice = new JLabel("");
		changeNotice.setHorizontalAlignment(SwingConstants.CENTER);
		northPanel.add(changeNotice, BorderLayout.SOUTH);

		// Check if user submitted a job rating
		checkingJobRating(list);

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
			checkJobReassignment(person, list);

		// Prepare job title panel
		JPanel historyPanel = new JPanel(new BorderLayout(0, 0));
		content.add(historyPanel, BorderLayout.CENTER);

		// Job history
		JPanel jobHistoryPanel = new JPanel(new BorderLayout(0, 0));
		historyPanel.add(jobHistoryPanel, BorderLayout.NORTH);
		JLabel historyLabel = new JLabel(Msg.getString("TabPanelCareer.history"), JLabel.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(historyLabel);
		jobHistoryPanel.add(historyLabel, BorderLayout.NORTH);
		jobHistoryTableModel = new JobHistoryTableModel(person);
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
		JLabel roleLabel = new JLabel("Role History", JLabel.CENTER); //$NON-NLS-1$
		StyleManager.applySubHeading(roleLabel);
		roleHistoryPanel.add(roleLabel, BorderLayout.NORTH);
		roleHistoryTableModel = new RoleHistoryTableModel(person);
		JScrollPane rscrollPanel = new JScrollPane();
		roleHistoryPanel.add(rscrollPanel, BorderLayout.CENTER);
		JTable rtable = new JTable(roleHistoryTableModel);
		rtable.setPreferredScrollableViewportSize(new Dimension(225, 100));
		rscrollPanel.setViewportView(rtable);

		update();
	}

	/*
	 * Checks a job rating is submitted or a job reassignment is submitted and is
	 * still not being reviewed.
	 *
	 * @param list
	 */
	public void checkingJobRating(List<JobAssignment> list) {
		int size = list.size();
		if (solRatingSubmitted == -1) {
			// the TabPanelCareer was closed and retrieve the saved value of
			// solRatingSubmitted from JobAssignment
			if (list.get(size - 1).getStatus() == JobAssignmentType.PENDING) {
				solRatingSubmitted = list.get(size - 2).getSolRatingSubmitted();
			} else {
				solRatingSubmitted = list.get(size - 1).getSolRatingSubmitted();
			}
		}

		if (solRatingSubmitted == -1) {
			// no rating has ever been submitted. Thus he/she has a new job assignment
			starRater.setEnabled(true);
			starRater.setSelection(0);
			displayNotice("", false);
		} else {
			int solElapsed = masterClock.getMarsTime().getMissionSol();

			if (solElapsed > solRatingSubmitted + RATING_DAYS) {
				// if 7 days have passed since the rating submitted, re-enable the star rater
				starRater.setEnabled(true);
				starRater.setSelection(0);
				displayNotice("", false);
				dateTimeRatingSubmitted = null;
				solRatingSubmitted = -1;
				printLog = true;
				String s = "";
				if (printLog2) {
					s = "Job Rating open for review again.";
					logger.info(person, s);
					printLog2 = false;
				}
			} else {
				starRater.setSelection(0);
				starRater.setEnabled(false);
				String s = "";
				if (dateTimeRatingSubmitted != null) {
					s = "Job Rating last submitted on " + dateTimeRatingSubmitted;
					displayNotice(s, false);
					if (printLog) {
						logger.info(person, s);
						printLog = false;
						printLog2 = true;
					}
				}
			}

			if (solCache != solElapsed) {
				dateTimeRatingSubmitted = null;
			}
		}
	}

	private void displayNotice(String s, boolean error) {
		changeNotice.setText(s);
		changeNotice.setForeground((error ? Color.RED : Color.BLUE));
	}
	
	/*
	 * Calculates the cumulative career performance score of a person.
	 */
	public int calculateAveRating(List<JobAssignment> list) {
		double score = 0;
		int size = list.size();
		for (int i = 0; i < size; i++) {
			score += list.get(i).getJobRating();
		}
		score = score / size; // divided by 2 because job rating in JobAssignment is from 0 to 10
		if (score > 5)
			score = 5;
		return (int) score;
	}

	/*
	 * Checks for any role change or reassignment.
	 * Note that change in population affects the list of role types.
	 */
	public void checkRoleChange() {
		List<String> names = RoleUtil.getRoleNames(settlement.getNumCitizens());

        int oldSize = roleComboBox.getModel().getSize();

        if (oldSize != names.size()) {
	        // Remove old data
        	roleComboBox.removeAllItems();

	        // Add new data
        	for (String s: names) {
        		roleComboBox.addItem(s);
        	}
        }

		// Prepares role combo box
		RoleType newRole = person.getRole().getType();

		if (roleCache != newRole) {
			roleCache = newRole;
			roleComboBox.setSelectedItem(roleCache.getName());
			changeNotice.setText("Role just changed to " + newRole);
		}
	}


	/*
	 * Checks for the status of job reassignment.
	 */
	public void checkJobReassignment(Person person, List<JobAssignment> list) {
		int pop = settlement.getNumCitizens();

		int last = list.size() - 1;

		JobAssignmentType status = list.get(last).getStatus();

		if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {

			if (status == JobAssignmentType.PENDING) {
				statusCache = JobAssignmentType.PENDING;
				jobComboBox.setEnabled(false);
				
				String s = "Job Reassignment submitted on " + list.get(last).getTimeSubmitted();
				changeNotice.setText(s);
				
				if (firstNotification) 
					logger.info(person, s);
				
				firstNotification = false;
			}

			// detects a change of status from pending to approved
			else if (statusCache == JobAssignmentType.PENDING) {
				if (status.equals(JobAssignmentType.APPROVED)) {
					statusCache = JobAssignmentType.APPROVED;
					logger.info(person, "Job reassignment reviewed and approved.");
					
					JobType selectedJob = list.get(last).getJobType();

					if (jobCache != selectedJob) {
						jobCache = selectedJob;
						// Note: must update the jobCache prior to calling setSelectedItem
						// or else a new job reassignment will be submitted
						jobComboBox.setSelectedItem(selectedJob.getName());
						changeNotice.setText("Job just changed to " + selectedJob);
					}

					person.getMind().setJobLock(true);

				} else if (status == JobAssignmentType.NOT_APPROVED) {
					statusCache = JobAssignmentType.NOT_APPROVED;
					logger.info(person, "Job reassignment reviewed and NOT approved.");

					JobType selectedJob = list.get(last - 1).getJobType();
	
					if (jobCache != selectedJob) {
						jobCache = selectedJob;
						// Note: must update the jobCache prior to calling setSelectedItem
						// or else a new job reassignment will be submitted
						jobComboBox.setSelectedItem(selectedJob.getName());
						changeNotice.setText("Job just changed to " + selectedJob);
					}
				}

				jobComboBox.setEnabled(true);
				changeNotice.setText("");

				// Note: determine if new rating submission should be allowed immediately at the
				// beginning of a new assignment
				solRatingSubmitted = -1;
				starRater.setSelection(0);
				starRater.setEnabled(true);

				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();

				RoleType newRole = person.getRole().getType();
				if (roleCache != newRole) {
					roleCache = newRole;
					roleComboBox.setSelectedItem(roleCache.getName());
					changeNotice.setText("Role just changed to " + newRole);
				}

			}
			else {
				// do nothing. It's at the start of sim
			}
		}
		else {
			// Update the jobComboBox if pop is less than
			// POPULATION_WITH_COMMANDER
			JobType selectedJob = list.get(last).getJobType();
			
			if (jobCache != selectedJob) {
				jobCache = selectedJob;
				// Note: must update the jobCache prior to calling setSelectedItem
				// or else a new job reassignment will be submitted
				jobComboBox.setSelectedItem(selectedJob.getName());
				changeNotice.setText("Job just changed to " + selectedJob);
			}
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		boolean dead = false;

		dead = person.getPhysicalCondition().isDead();

		// Update job if necessary.
		if (dead) {
			DeathInfo deathInfo = person.getPhysicalCondition().getDeathDetails();

			jobCache = deathInfo.getJob();
			jobComboBox.setEnabled(false);
			roleComboBox.setEnabled(false);
			starRater.setSelection(0);
			starRater.setEnabled(false);

		} else {

			// Check for the role change
			checkRoleChange();

			List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();

			// Added checking if user submitted a job rating
			checkingJobRating(list);

			// Check for the status of Job Reassignment
			checkJobReassignment(person, list);

			// check for the passing of each day
			int solElapsed = masterClock.getMarsTime().getMissionSol();

			// If the rating or job reassignment request is at least one day ago
			if (solCache != solElapsed) {
				solCache = solElapsed;
			} // end of if (solElapsed != solCache)

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
		MainDesktopPane desktop = getDesktop();
		
		if (source == roleComboBox) {
			RoleType selectedRole = RoleType.getType((String) roleComboBox.getSelectedItem());
			int box = -1;

			if (selectedRole != roleCache) {
				if ((selectedRole.isChief() || selectedRole.isCouncil())
						&& (!roleCache.isChief() || !roleCache.isCouncil())) {
					box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(),
							"Are you sure you want to promote the person's role from " 
							 + roleCache + " to " + selectedRole.getName() + " ?");
				}

				else if ((roleCache.isChief() || roleCache.isCouncil())
						&& (!selectedRole.isChief() || !selectedRole.isCouncil())) {
					box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(),
							"Are you sure you want to demote the person's role from "
							 + roleCache + " to "  + selectedRole.getName() + " ?");
				}

				else {
					box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(),
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
					roleComboBox.setSelectedItem(roleCache.getName());
				}
			}
		}

		else if (source == jobComboBox) {

			JobType selectedJob = JobType.getJobTypeByName((String) jobComboBox.getSelectedItem());
//			JobType jobCache = person.getMind().getJob();

			if (selectedJob != jobCache) {
				int box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(),
						"Are you sure you want to change the person's job from "
						 + jobCache + " to " + selectedJob.getName() + " ?");
				
				if (box == JOptionPane.YES_OPTION) {
					considerJobChange(jobCache, selectedJob);
				}
				else {
					jobComboBox.setSelectedItem(jobCache.getName());
				}
			}
		}
	}

	/**
	 * Determines if the job change request should be granted.
	 *
	 * @param jobStrCache
	 * @param selectedJobStr
	 */
	public void considerJobChange(JobType jobCache, JobType selectedJob) {

		// if job is Politician, loads and set to the previous job and quit;
		if (jobCache == JobType.POLITICIAN) {
			jobComboBox.setSelectedItem(jobCache.getName());
			displayNotice("Mayor cannot switch job arbitrarily!", true);
		}

		else if (selectedJob == JobType.POLITICIAN) {
			jobComboBox.setSelectedItem(jobCache.getName());
			displayNotice("Politician job is reserved for Mayor only.", true);
		}

		else if (jobCache != selectedJob) {
			// Use getAssociatedSettlement instead of getSettlement()
			int pop = 0;

			pop = settlement.getNumCitizens();

			// if the population is beyond 4
			if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {
				String s = "Job Reassignment submitted on " + MarsTimeFormat.getTruncatedDateTimeStamp(
											masterClock.getMarsTime());
				displayNotice(s, false);
				logger.info(person, s);
				firstNotification = true;

				JobHistory jh = person.getJobHistory();

				statusCache = JobAssignmentType.PENDING;

				jh.savePendingJob(selectedJob, JobUtil.USER, statusCache, null, true);
				// Set the combobox selection back to its previous job type for the time being
				// until the reassignment is approved
				jobComboBox.setSelectedItem(jobCache.getName());
				// disable the combobox so that user cannot submit job reassignment for a period
				// of time
				jobComboBox.setEnabled(false);
				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();
			}

			else if (pop > 0 && pop <= ChainOfCommand.POPULATION_WITH_COMMANDER) {
				displayNotice("", false);
				jobComboBox.setSelectedItem(selectedJob.getName());
				// pop is small, things need to be flexible. Thus automatic approval
				statusCache = JobAssignmentType.APPROVED;
				person.getMind().reassignJob(selectedJob, true, JobUtil.USER, statusCache,
						JobUtil.USER);

				// System.out.println("Yes they are diff");
				jobCache = selectedJob;

				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();
			}
		}
		else
			jobComboBox.setSelectedItem(jobCache);
	}

	@Override
	public void destroy() {
		super.destroy();
		
		jobComboBox = null;
		roleComboBox = null;
		jobHistoryTableModel = null;
		starRater = null;
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
				case 0 -> Msg.getString("TabPanelCareer.column.time"); //$NON-NLS-1$
				case 1 -> "Role";
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

		private JobHistory jobHistory;
		private List<JobAssignment> jobAssignmentList;
		private int origSize;

		/**
		 * hidden constructor.
		 *
		 * @param unit {@link Unit}
		 */
		JobHistoryTableModel(Person p) {
			jobHistory = p.getJobHistory();
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
				case 0 -> Msg.getString("TabPanelCareer.column.time"); //$NON-NLS-1$
				case 1 -> Msg.getString("TabPanelCareer.column.jobType"); //$NON-NLS-1$
				case 2 -> Msg.getString("TabPanelCareer.column.initiated"); //$NON-NLS-1$
				case 3 -> Msg.getString("TabPanelCareer.column.status"); //$NON-NLS-1$
				case 4 -> Msg.getString("TabPanelCareer.column.authorized"); //$NON-NLS-1$
				default -> null;
			};
		}

		public Object getValueAt(int row, int column) {
			int r = jobAssignmentList.size() - row - 1;
			JobAssignment ja = jobAssignmentList.get(r);
			return switch(column) {
				case 0 -> ja.getTimeSubmitted(); // MarsClock.getDateTimeStamp(ja.getTimeSubmitted());
				case 1 -> ja.getJobType();
				case 2 -> ja.getInitiator();
				case 3 -> ja.getStatus();
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
