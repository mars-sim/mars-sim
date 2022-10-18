/*
 * Mars Simulation Project
 * TabPanelCareer.java
 * @date 2021-12-05
 * @author Manny KUng
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.logging.SimLogger;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.util.Job;
import org.mars_sim.msp.core.person.ai.job.util.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.util.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.util.JobHistory;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.person.ai.job.util.JobUtil;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MarsClockFormat;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.StarRater;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

/**
 * The TabPanelCareer is a tab panel for viewing a person's career path and job
 * history.
 */
@SuppressWarnings("serial")
public class TabPanelCareer extends TabPanel implements ActionListener {

	/** default logger. */
	private static SimLogger logger = SimLogger.getLogger(TabPanelCareer.class.getName());

	private static final String CAREER_ICON = Msg.getString("icon.career"); //$NON-NLS-1$
	
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

	private JTable table;

	private WebLabel jobLabel;
	private WebLabel roleLabel;
	private WebLabel jobChangeLabel;
	private WebLabel roleChangeLabel;
	private WebLabel ratingLabel;

	private WebComboBox jobComboBox;
	private WebComboBox roleComboBox;

	private JobHistoryTableModel jobHistoryTableModel;

	private StarRater starRater;
	private StarRater aveRater;

	/** The Person instance. */
	private Person person;
	private Settlement settlement;

	private final Font courierNew12 = new Font("Courier New", Font.ITALIC, 12);

	private static MarsClock marsClock;

	/**
	 * Constructor.
	 *
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelCareer(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			null,	
			ImageLoader.getNewIcon(CAREER_ICON),
			Msg.getString("TabPanelCareer.title"), //$NON-NLS-1$
			unit, desktop
		);

		person = (Person) unit;

		if (marsClock == null)
			marsClock	= Simulation.instance().getMasterClock().getMarsClock();

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

		WebPanel firstPanel = new WebPanel(new BorderLayout());
		northPanel.add(firstPanel, BorderLayout.NORTH);

		// Prepare job spring panel
		WebPanel topSpringPanel = new WebPanel(new SpringLayout());
		firstPanel.add(topSpringPanel, BorderLayout.NORTH);

		// Prepare job label
		jobLabel = new WebLabel(Msg.getString("TabPanelCareer.jobType"), WebLabel.RIGHT); //$NON-NLS-1$
		topSpringPanel.add(jobLabel);
		TooltipManager.setTooltip(jobLabel, Msg.getString("TabPanelCareer.jobType.tooltip"), TooltipWay.down);

		// Prepare job combo box
		jobCache = mind.getJob();
		List<String> jobNames = new ArrayList<String>();
		for (Job job : JobUtil.getJobs()) {
			jobNames.add(job.getName(person.getGender()));
		}
		Collections.sort(jobNames);

		// Prepare job combo box
		jobComboBox = new WebComboBox(jobNames.toArray());
		jobComboBox.setSelectedItem(jobCache.getName());
		jobComboBox.addActionListener(this);

		// Prepare job panel
		WebPanel jobPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT)); // new GridLayout(3, 1, 0, 0)); //
		jobPanel.add(jobComboBox);
		topSpringPanel.add(jobPanel);

		TooltipManager.setTooltip(jobComboBox, Msg.getString("TabPanelCareer.jobComboBox.tooltip"),
				TooltipWay.down);

		// check if a job reassignment is still pending for review
		// if true, disable the combobox

		// Prepare role label
		roleLabel = new WebLabel(Msg.getString("TabPanelCareer.roleType"), WebLabel.RIGHT); //$NON-NLS-1$
		roleLabel.setSize(10, 2);
		topSpringPanel.add(roleLabel);// , JLabel.BOTTOM);

		roleCache = person.getRole().getType();
		List<String> roleNames = RoleUtil.getRoleNames(settlement.getNumCitizens());

		// Prepare role combo box
		roleComboBox = new WebComboBox(roleNames.toArray());
		roleComboBox.setSelectedItem(roleCache.getName());
		roleComboBox.addActionListener(this);

		// Prepare role panel
		WebPanel rolePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		rolePanel.add(roleComboBox);
		topSpringPanel.add(rolePanel);

		TooltipManager.setTooltip(roleComboBox, Msg.getString("TabPanelCareer.roleComboBox.tooltip"),
				TooltipWay.down);

		jobChangeLabel = new WebLabel("");
		jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jobChangeLabel.setFont(courierNew12);
		jobChangeLabel.setForeground(Color.blue);
		firstPanel.add(jobChangeLabel, BorderLayout.CENTER);
		TooltipManager.setTooltip(jobChangeLabel, Msg.getString("TabPanelCareer.roleType.tooltip"), TooltipWay.down);//$NON-NLS-1$

		roleChangeLabel = new WebLabel("");
		roleChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		roleChangeLabel.setFont(courierNew12);
		roleChangeLabel.setForeground(Color.blue);
		firstPanel.add(roleChangeLabel, BorderLayout.SOUTH);
		TooltipManager.setTooltip(roleChangeLabel, Msg.getString("TabPanelCareer.roleType.tooltip"), TooltipWay.down);//$NON-NLS-1$

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(topSpringPanel, 2, 2, // rows, cols
				150, 3, // initX, initY
				3, 1); // xPad, yPad

		WebPanel ratingPanel = new WebPanel(new BorderLayout());
		northPanel.add(ratingPanel, BorderLayout.CENTER);

		List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();

		WebPanel springPanel = new WebPanel(new SpringLayout());
		ratingPanel.add(springPanel, BorderLayout.CENTER);

		WebLabel aveRatingLabel = new WebLabel("Overall Performance : ", WebLabel.RIGHT);
		springPanel.add(aveRatingLabel);

		aveRater = new StarRater(5, calculateAveRating(list));
		aveRater.setEnabled(false);
		springPanel.add(aveRater);

		TooltipManager.setTooltip(aveRatingLabel, Msg.getString("TabPanelCareer.aveRater.tooltip"), //$NON-NLS-1$
				TooltipWay.down);

		WebLabel raterLabel = new WebLabel("Your Rating : ", WebLabel.RIGHT);
		springPanel.add(raterLabel);
		starRater = new StarRater(5, 0, 0);

		TooltipManager.setTooltip(raterLabel, Msg.getString("TabPanelCareer.raterLabel.tooltip"), TooltipWay.down);//$NON-NLS-1$
		TooltipManager.setTooltip(starRater, Msg.getString("TabPanelCareer.starRater.tooltip"), TooltipWay.down);//$NON-NLS-1$

		starRater.addStarListener(new StarRater.StarListener() {
			public void handleSelection(int selection) {
				if (starRater.isEnabled()) {

					int sol = marsClock.getMissionSol();
					dateTimeRatingSubmitted = MarsClockFormat.getTruncatedDateTimeStamp(marsClock);
					printLog = true;
					ratingLabel.setText("Job Rating submitted on " + dateTimeRatingSubmitted);
					ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
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

		springPanel.add(starRater);

		// Prepare SpringLayout
		SpringUtilities.makeCompactGrid(springPanel, 2, 2, // rows, cols
				80, 10, // initX, initY
				5, 10); // xPad, yPad

		ratingLabel = new WebLabel("");
		// ratingLabel.setSize(300, 30);
		ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ratingLabel.setFont(courierNew12);
		ratingLabel.setForeground(Color.blue);
		ratingPanel.add(ratingLabel, BorderLayout.SOUTH);

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
			// Added checking for the status of Job Reassignment
			checkJobReassignment(person, list);

		// Prepare job title panel
		WebPanel jobHistoryPanel = new WebPanel(new BorderLayout(0, 0));
		content.add(jobHistoryPanel, BorderLayout.CENTER);

		// Prepare job title label
		WebLabel historyLabel = new WebLabel(Msg.getString("TabPanelCareer.history"), WebLabel.CENTER); //$NON-NLS-1$
		// historyLabel.setBounds(0, 0, width, height);
		historyLabel.setFont(SUBTITLE_FONT);
		historyLabel.setPadding(7, 0, 1, 0);
		jobHistoryPanel.add(historyLabel, BorderLayout.NORTH);

		// Create schedule table model
		jobHistoryTableModel = new JobHistoryTableModel();

		// Create attribute scroll panel
		WebScrollPane scrollPanel = new WebScrollPane();
//		scrollPanel.setBorder(new MarsPanelBorder());
		jobHistoryPanel.add(scrollPanel, BorderLayout.CENTER);

		// Create schedule table
		table = new ZebraJTable(jobHistoryTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(25);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.getColumnModel().getColumn(4).setPreferredWidth(50);
		table.setRowSelectionAllowed(true);
//		table.setCellSelectionEnabled(false);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		scrollPanel.setViewportView(table);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);
		table.getColumnModel().getColumn(2).setCellRenderer(renderer);
		table.getColumnModel().getColumn(3).setCellRenderer(renderer);
		table.getColumnModel().getColumn(4).setCellRenderer(renderer);

		// Added sorting
		table.setAutoCreateRowSorter(true);
		
		TableStyle.setTableStyle(table);
		update();
		jobHistoryTableModel.update();
	}

	/*
	 * Checks a job rating is submitted or a job reassignment is submitted and is
	 * still not being reviewed
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
			ratingLabel.setText("");
		} else {
			int solElapsed = marsClock.getMissionSol();

			if (solElapsed > solRatingSubmitted + RATING_DAYS) {
				// if 7 days have passed since the rating submitted, re-enable the star rater
				starRater.setEnabled(true);
				starRater.setSelection(0);
				ratingLabel.setText("");
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
					ratingLabel.setText(s);
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

	/*
	 * Calculate the cumulative career performance score of a person
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
	 * Note that change in population affects the list of role types
	 */
	@SuppressWarnings("unchecked")
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

		// Prepare role combo box
		RoleType newRole = person.getRole().getType();

		if (roleCache != newRole) {
			roleCache = newRole;
			roleComboBox.setSelectedItem(roleCache.getName());
			roleChangeLabel.setText("Role just changed to " + newRole);
		}
	}


	/*
	 * Checks for the status of Job Reassignment
	 */
	public void checkJobReassignment(Person person, List<JobAssignment> list) {
		int pop = settlement.getNumCitizens();

		int last = list.size() - 1;

		JobAssignmentType status = list.get(last).getStatus();

		if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {

			if (status == JobAssignmentType.PENDING) {
				statusCache = JobAssignmentType.PENDING;
				jobComboBox.setEnabled(false);
				jobChangeLabel.setForeground(Color.blue);
				String s = "Job Reassignment submitted on " + list.get(last).getTimeSubmitted();
				jobChangeLabel.setText(s);
				if (firstNotification) logger.info(person, s);
				firstNotification = false;
			}

			// detects a change of status from pending to approved
			else if (statusCache == JobAssignmentType.PENDING) {
				if (status.equals(JobAssignmentType.APPROVED)) {
					statusCache = JobAssignmentType.APPROVED;
					logger.info(person, "Job reassignment reviewed and approved.");
					JobType selectedJob = list.get(last).getJobType();
					jobCache = selectedJob;
					// must update the jobCache prior to setSelectedItem
					// or else a new job reassignment will be submitted
					jobComboBox.setSelectedItem(selectedJob.getName());

					person.getMind().setJobLock(true);

				} else if (status == JobAssignmentType.NOT_APPROVED) {
					statusCache = JobAssignmentType.NOT_APPROVED;
					logger.info(person, "Job reassignment reviewed and NOT approved.");

					JobType selectedJob = list.get(last - 1).getJobType();
					jobCache = selectedJob;
					// must update the jobCache prior to setSelectedItem
					// or else a new job reassignment will be submitted
					jobComboBox.setSelectedItem(selectedJob.getName());

				}

				jobComboBox.setEnabled(true);
				jobChangeLabel.setText("");

				// Note: determine if new rating submission should be allowed immediately at the
				// beginning of a new assignment
				solRatingSubmitted = -1;
				starRater.setSelection(0);
				starRater.setEnabled(true);
				ratingLabel.setText("");

				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();

				RoleType roleNew = person.getRole().getType();
				if (roleCache != roleNew) {
					roleCache = roleNew;
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
			jobCache = selectedJob;
			// must update the jobCache prior to setSelectedItem
			// or else a new job reassignment will be submitted
			jobComboBox.setSelectedItem(selectedJob.getName());
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		TableStyle.setTableStyle(table);

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

			checkRoleChange();

			List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();

			// Added checking if user submitted a job rating
			checkingJobRating(list);

			// Check for the status of Job Reassignment
			checkJobReassignment(person, list);

			// check for the passing of each day
			int solElapsed = marsClock.getMissionSol();

			// If the rating or job reassignment request is at least one day ago
			if (solCache != solElapsed) {
				solCache = solElapsed;
			} // end of if (solElapsed != solCache)
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
							"Are you sure you want to promote the role to " + selectedRole.getName() + " ?");
				}

				else if ((roleCache.isChief() || roleCache.isCouncil())
						&& (!selectedRole.isChief() || !selectedRole.isCouncil())) {
					box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(),
							"Are you sure you want to demote the role to " + selectedRole.getName() + " ?");
				}

				else {
					box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(),
							"Are you sure you want to change the role to " + selectedRole.getName() + " ?");
				}

				if (box == JOptionPane.YES_OPTION) {
					roleCache = selectedRole;
					person.getRole().changeRoleType(selectedRole);
				}
				else {
					roleComboBox.setSelectedItem(selectedRole.getName());
				}
			}
		}

		else if (source == jobComboBox) {

			JobType selectedJob = JobType.getJobTypeByName((String) jobComboBox.getSelectedItem());
			JobType jobCache = person.getMind().getJob();

			if (selectedJob != jobCache) {
				int box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(),
						"Are you sure you want to change the job to " + selectedJob.getName() + " ?");
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
	 * Determine if the job change request should be granted
	 *
	 * @param jobStrCache
	 * @param selectedJobStr
	 */
	public void considerJobChange(JobType jobCache, JobType selectedJob) {

		// if job is Politician, loads and set to the previous job and quit;
		if (jobCache == JobType.POLITICIAN) {
			jobComboBox.setSelectedItem(jobCache.getName());
			jobChangeLabel.setForeground(Color.red);
			jobChangeLabel.setText("Mayor cannot switch job arbitrarily!");
			jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		else if (selectedJob == JobType.POLITICIAN) {
			jobComboBox.setSelectedItem(jobCache.getName());
			jobChangeLabel.setForeground(Color.red);
			jobChangeLabel.setText("Politician job is reserved for Mayor only.");
			jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		else if (jobCache != selectedJob) {
			// Use getAssociatedSettlement instead of getSettlement()
			int pop = 0;

			pop = settlement.getNumCitizens();

			// if the population is beyond 4
			if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {

				jobChangeLabel.setForeground(Color.BLUE);

				String s = "Job Reassignment submitted on " + MarsClockFormat.getTruncatedDateTimeStamp(marsClock);
				jobChangeLabel.setText(s);
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
				jobChangeLabel.setForeground(Color.RED);
				jobChangeLabel.setText("");
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
		
		table = null;
		jobLabel = null;
		jobChangeLabel = null;
		ratingLabel = null;
		jobComboBox = null;
		roleComboBox = null;
		jobHistoryTableModel = null;
		starRater = null;
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	class JobHistoryTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private JobHistory jobHistory;
		private JobAssignment ja;

		private List<JobAssignment> jobAssignmentList;

		/**
		 * hidden constructor.
		 *
		 * @param unit {@link Unit}
		 */
		JobHistoryTableModel() {
			jobHistory = person.getJobHistory();
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
			return 5;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0)
				dataType = String.class;
			else if (columnIndex == 1)
				dataType = String.class;
			else if (columnIndex == 2)
				dataType = String.class;
			else if (columnIndex == 3)
				dataType = String.class;
			else if (columnIndex == 4)
				dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0)
				return Msg.getString("TabPanelCareer.column.time"); //$NON-NLS-1$
			else if (columnIndex == 1)
				return Msg.getString("TabPanelCareer.column.jobType"); //$NON-NLS-1$
			else if (columnIndex == 2)
				return Msg.getString("TabPanelCareer.column.initiated"); //$NON-NLS-1$
			else if (columnIndex == 3)
				return Msg.getString("TabPanelCareer.column.status"); //$NON-NLS-1$
			else if (columnIndex == 4)
				return Msg.getString("TabPanelCareer.column.authorized"); //$NON-NLS-1$
			else
				return null;
		}

		public Object getValueAt(int row, int column) {
			int r = jobAssignmentList.size() - row - 1;
			ja = jobAssignmentList.get(r);
			// System.out.println(" r is " + r);
			if (column == 0)
				return ja.getTimeSubmitted(); // MarsClock.getDateTimeStamp(ja.getTimeSubmitted());
			else if (column == 1)
				return ja.getJobType();
			else if (column == 2)
				return ja.getInitiator();
			else if (column == 3)
				return ja.getStatus();
			else if (column == 4)
				return ja.getAuthorizedBy();
			else
				return null;
		}

		/**
		 * Prepares the job history of the person
		 */
		void update() {
			jobAssignmentList = jobHistory.getJobAssignmentList();
			fireTableDataChanged();
		}
	}
}
