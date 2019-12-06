/**
 * Mars Simulation Project
 * TabPanelCareer.java
 * @version 3.1.0 2017-10-18
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
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.person.ai.job.JobUtil;
import org.mars_sim.msp.core.person.ai.job.Politician;
import org.mars_sim.msp.core.person.ai.role.RoleType;
import org.mars_sim.msp.core.person.ai.role.RoleUtil;
import org.mars_sim.msp.core.person.health.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.structure.ChainOfCommand;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
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

	private static Logger logger = Logger.getLogger(TabPanelCareer.class.getName());

	private static final int RATING_DAYS = 7;
	private static final String POLITICIAN = Politician.class.getSimpleName();

	/** data cache */
	private int solCache = 1;
	private int solRatingSubmitted = -1;

	/** Is UI constructed. */
	private boolean uiDone = false;
	private boolean firstNotification = true;
	private boolean printLog;
	private boolean printLog2;

	private String jobCache = "";
	private String roleCache;
	private String dateTimeRatingSubmitted;
	
	private JobAssignmentType statusCache = JobAssignmentType.APPROVED;// PENDING;

	private JTable table;

	private WebLabel jobLabel;
	private WebLabel roleLabel;
	private WebLabel jobChangeLabel;
	private WebLabel roleChangeLabel;
	private WebLabel ratingLabel;
	
//	private WebTextField roleTF;

	private WebComboBox jobComboBox;
	private JComboBoxMW<Object> roleComboBox;
	
	private JobHistoryTableModel jobHistoryTableModel;

	private StarRater starRater;
	private StarRater aveRater;
	
	/** The Person instance. */
	private Person person = null;
	/** The Robot instance. */
	private Robot robot = null;
	
	private Settlement settlement;
	
	private static MarsClock marsClock;

	/**
	 * Constructor.
	 * 
	 * @param unit    {@link Unit} the unit to display.
	 * @param desktop {@link MainDesktopPane} the main desktop.
	 */
	public TabPanelCareer(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelCareer.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelCareer.tooltip"), //$NON-NLS-1$
				unit, desktop);

		if (marsClock == null)
			marsClock	= Simulation.instance().getMasterClock().getMarsClock();
		
		if (unit instanceof Person) {
			person = (Person) unit;
			if (person.getAssociatedSettlement() != null) {
				settlement = person.getAssociatedSettlement();
			} 
			else if (person.isInVehicle()) {
				Vehicle vehicle = (Vehicle) person.getContainerUnit();
				settlement = vehicle.getSettlement();
			}
			if (settlement == null) {
				settlement = person.getBuriedSettlement();
			}
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			if (robot.getAssociatedSettlement() != null) {
				settlement = robot.getAssociatedSettlement();
			}
		}
	}

	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		
		Mind mind = null;
		BotMind botMind = null;
		boolean dead = false;
		DeathInfo deathInfo = null;

		if (unit instanceof Person) {
			person = (Person) unit;
			mind = person.getMind();
			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();
		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getSystemCondition().isInoperable();
			// deathInfo = robot.getSystemCondition().getDeathDetails();
		}

		// Prepare label panel
		WebPanel labelPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare title label
		WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelCareer.title"), WebLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(titleLabel);

		if (unit instanceof Person) {
			person = (Person) unit;

			solCache = person.getJobHistory().getSolCache();

			WebPanel firstPanel = new WebPanel(new BorderLayout());// GridLayout(2, 1, 5, 0));
			// firstPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
//			firstPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(firstPanel, BorderLayout.NORTH);

			// Prepare job panel
			WebPanel topSpringPanel = new WebPanel(new SpringLayout());// GridLayout(2, 2, 0, 0));

			firstPanel.add(topSpringPanel, BorderLayout.NORTH);

			// Prepare job label
			jobLabel = new WebLabel(Msg.getString("TabPanelCareer.jobType"), WebLabel.RIGHT); //$NON-NLS-1$
			topSpringPanel.add(jobLabel);
			TooltipManager.setTooltip(jobLabel, Msg.getString("TabPanelCareer.jobType.tooltip"), TooltipWay.down);

			// Prepare job combo box
			jobCache = mind.getJob().getName(person.getGender());
			List<String> jobNames = new ArrayList<String>();
			for (Job job : JobUtil.getJobs()) {
				jobNames.add(job.getName(person.getGender()));
			}
			Collections.sort(jobNames);

			jobComboBox = new WebComboBox(jobNames.toArray());
//			jobComboBox.setWidePopup(true);
			jobComboBox.setSelectedItem(jobCache);
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
			
			// Prepare role combo box
			roleCache = person.getRole().getType().toString();
			List<String> roleNames = getRoleNames();

			roleComboBox = new JComboBoxMW<>();
			
			Iterator<String> k = roleNames.iterator();
			while (k.hasNext()) roleComboBox.addItem(k.next());
			
			roleComboBox.setSelectedItem(roleCache);
			roleComboBox.addActionListener(this);
			
			// Prepare role panel
			WebPanel rolePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT)); 
			rolePanel.add(roleComboBox);
			topSpringPanel.add(rolePanel);

			TooltipManager.setTooltip(roleComboBox, Msg.getString("TabPanelCareer.roleComboBox.tooltip"),
					TooltipWay.down);
			
//			roleTF = new WebTextField(roleCache);
//			roleTF.setEditable(false);
//			// roleTF.setBounds(0, 0, 0, 0);
//			roleTF.setColumns(20);

			jobChangeLabel = new WebLabel();
			// jobChangeLabel.setSize(300, 30);
			jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jobChangeLabel.setFont(new Font("Courier New", Font.ITALIC, 10));
			jobChangeLabel.setForeground(Color.blue);
			firstPanel.add(jobChangeLabel, BorderLayout.CENTER);
			TooltipManager.setTooltip(jobChangeLabel, Msg.getString("TabPanelCareer.roleType.tooltip"), TooltipWay.down);//$NON-NLS-1$

			roleChangeLabel = new WebLabel();
			// roleChangeLabel.setSize(300, 30);
			roleChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
			roleChangeLabel.setFont(new Font("Courier New", Font.ITALIC, 10));
			roleChangeLabel.setForeground(Color.blue);
			firstPanel.add(roleChangeLabel, BorderLayout.SOUTH);
			TooltipManager.setTooltip(roleChangeLabel, Msg.getString("TabPanelCareer.roleType.tooltip"), TooltipWay.down);//$NON-NLS-1$
			
			// Prepare SpringLayout
			SpringUtilities.makeCompactGrid(topSpringPanel, 2, 2, // rows, cols
					80, 5, // initX, initY
					10, 1); // xPad, yPad

			WebPanel ratingPanel = new WebPanel(new BorderLayout());
//			ratingPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(ratingPanel, BorderLayout.CENTER);

			List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();
			// int size = list.size();

			WebPanel springPanel = new WebPanel(new SpringLayout());// GridLayout(2,1,5,5));// GridLayout(1, 2, 0, 0));
			ratingPanel.add(springPanel, BorderLayout.CENTER);
			// raterPanel.setAlignmentY(TOP_ALIGNMENT);

			WebLabel aveRatingLabel = new WebLabel("Overall Performance : ", WebLabel.RIGHT);
			springPanel.add(aveRatingLabel);

			aveRater = new StarRater(5, calculateAveRating(list));
			// aveRater.setHorizontalAlignment(SwingConstants.LEFT);
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
						dateTimeRatingSubmitted = MarsClock.getTruncatedDateTimeStamp(marsClock);
						printLog = true;
						ratingLabel.setText("Job rating submitted on " + dateTimeRatingSubmitted);
						logger.info(person + "'s job rating was submitted on " + dateTimeRatingSubmitted);
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
					100, 10, // initX, initY
					20, 10); // xPad, yPad

			ratingLabel = new WebLabel("Job Rating");
			// ratingLabel.setSize(300, 30);
			ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			ratingLabel.setFont(new Font("Courier New", Font.ITALIC, 12));
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
//				roleTF.setText("N/A");
				starRater.setSelection(0);
				starRater.setEnabled(false);

			} else
				// Added checking for the status of Job Reassignment
				checkJobReassignment(person, list);
		}

		// Prepare job title panel
		WebPanel jobHistoryPanel = new WebPanel(new GridLayout(2, 1, 1, 1));
		centerContentPanel.add(jobHistoryPanel, BorderLayout.NORTH);

		// Prepare job title label
		WebLabel historyLabel = new WebLabel(Msg.getString("TabPanelCareer.history"), WebLabel.CENTER); //$NON-NLS-1$
		// historyLabel.setBounds(0, 0, width, height);
		historyLabel.setPadding(7, 0, 3, 0);
		jobHistoryPanel.add(historyLabel, BorderLayout.NORTH);

		// Create schedule table model
		if (unit instanceof Person)
			jobHistoryTableModel = new JobHistoryTableModel((Person) unit);
		else if (unit instanceof Robot)
			jobHistoryTableModel = new JobHistoryTableModel((Robot) unit);

		// Create attribute scroll panel
		WebScrollPane scrollPanel = new WebScrollPane();
//		scrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(scrollPanel, BorderLayout.CENTER);

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
//			if (solCache != solElapsed) {
//				printLog2 = true; 
//			} 
			if (solElapsed > solRatingSubmitted + RATING_DAYS) {
				// if 7 days have passed since the rating submitted, re-enable the star rater
				starRater.setEnabled(true);
				starRater.setSelection(0);
				ratingLabel.setText("");
				dateTimeRatingSubmitted = null;
				solRatingSubmitted = -1;
				printLog = true;
				if (printLog2) {
					logger.info(person + "'s job rating is open for review again.");
					printLog2 = false;
				}
			} else {
				starRater.setSelection(0);
				starRater.setEnabled(false);
				String s = "";
				if (dateTimeRatingSubmitted != null) {
					s = person + "'s job rating last submitted on " + dateTimeRatingSubmitted;
					ratingLabel.setText(s);
					if (printLog) {
						logger.info(s);
						printLog = false;
						printLog2 = true;
					}
				}
				else {
					s = person + "'s job rating last submitted on sol " + solRatingSubmitted;
					ratingLabel.setText(s);
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
	 * Checks for any role change or reassignment
	 */
	public void checkRoleChange() {

		List<String> names = getRoleNames();
		
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) roleComboBox.getModel();
        
        int oldSize = model.getSize();
        
        if (oldSize != names.size()) {
	        // removing old data
	        model.removeAllElements();
	
	        for (String item : names) {
	            model.addElement(item);
	        }
	        // setting model with new data
	        roleComboBox.setModel(model);
        }

		// Prepare role combo box
		String newRole = person.getRole().getType().toString();

		if (!roleCache.equalsIgnoreCase(newRole)) {
			roleCache = newRole;
			roleComboBox.setSelectedItem(roleCache);
			
			String s = person + "'s role has just been changed to " + newRole;
			roleChangeLabel.setText(s);
		}
	}
	
	/**
	 * Gets a list of role type name strings
	 * 
	 * @return
	 */
	public List<String> getRoleNames() {

		int pop = settlement.getNumCitizens();
		List<String> roleNames = new ArrayList<String>();
		
		if (pop > ChainOfCommand.POPULATION_WITH_MAYOR) {
			for (RoleType r : RoleType.values()) {
				if (r != RoleType.PRESIDENT)
					roleNames.add(r.getName());
			}
		}
		
		else if (pop > ChainOfCommand.POPULATION_WITH_CHIEFS) {
			for (RoleType r : RoleType.values()) {
				if (r != RoleType.MAYOR || r != RoleType.PRESIDENT)
					roleNames.add(r.getName());
			}
		}
		
		else if (pop > ChainOfCommand.POPULATION_WITH_SUB_COMMANDER) {
			roleNames.add(RoleType.COMMANDER.getName());
			roleNames.add(RoleType.SUB_COMMANDER.getName());
			for (RoleType r : RoleType.getSpecialistRoles()) {
				roleNames.add(r.getName());
			}
		}
		
		else if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {
			roleNames.add(RoleType.COMMANDER.getName());
			for (RoleType r : RoleType.getSpecialistRoles()) {
				roleNames.add(r.getName());
			}
		}
		
		Collections.sort(roleNames);
		return roleNames;
	}
	
	
	/*
	 * Checks for the status of Job Reassignment
	 */
	public void checkJobReassignment(Person person, List<JobAssignment> list) {

		int pop = 0;

//		else if (person.isInVehicle()) {
//			Vehicle vehicle = (Vehicle) person.getContainerUnit();
//			settlement = vehicle.getSettlement();
//		}

		// List<JobAssignment> jobAssignmentList =
		// person.getJobHistory().getJobAssignmentList();
		int last = list.size() - 1;

		JobAssignmentType status = list.get(last).getStatus();

		pop = settlement.getNumCitizens();

		if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {

			if (status == JobAssignmentType.PENDING) {
				statusCache = JobAssignmentType.PENDING;
				// System.out.println("\n< " + person.getName() + " > ");
				// System.out.println("status still pending");
				jobComboBox.setEnabled(false);
				// jobComboBox.setSelectedItem(jobCache);
				jobChangeLabel.setForeground(Color.blue);
				String s = person + "'s job reassignment was submitted on " + list.get(last).getTimeSubmitted();
				jobChangeLabel.setText(s);
				if (firstNotification) logger.info(s);
				firstNotification = false;
			}

			// detects a change of status from pending to approved
			else if (statusCache == JobAssignmentType.PENDING) {
				if (status.equals(JobAssignmentType.APPROVED)) {
					statusCache = JobAssignmentType.APPROVED;
					logger.info(person.getName() + "'s job reassignment had been reviewed and approved.");
					String selectedJobStr = list.get(last).getJobType();
					jobCache = selectedJobStr; // must update the jobCache prior to setSelectedItem or else a new job
												// reassignment will be submitted in
					jobComboBox.setSelectedItem(selectedJobStr);

					person.getMind().setJobLock(true);

				} else if (status == JobAssignmentType.NOT_APPROVED) {
					statusCache = JobAssignmentType.NOT_APPROVED;
					logger.info(person.getName() + "'s job reassignment had been reviewed and was NOT approved.");

					String selectedJobStr = list.get(last - 1).getJobType();
					jobCache = selectedJobStr; // must update the jobCache prior to setSelectedItem or else a new job
												// reassignment will be submitted in
					jobComboBox.setSelectedItem(selectedJobStr);

				}

				jobComboBox.setEnabled(true);
				jobChangeLabel.setText("");

				// TODO: determine if new rating submission should be allowed immediately at the
				// beginning of a new assignment
				solRatingSubmitted = -1;
				starRater.setSelection(0);
				starRater.setEnabled(true);
				ratingLabel.setText("");

				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();

				String roleNew = person.getRole().toString();
				if (!roleCache.equals(roleNew)) {

					roleCache = roleNew;
//					roleTF.setText(roleCache);
					// System.out.println("TabPanelCareer : just set New Role in TextField");
				}

			} // if (statusCache.equals(JobAssignmentType.PENDING))
			else {
				; // do nothing. at the start of sim
			} // if (statusCache.equals(JobAssignmentType.PENDING))

		} else {
			// update the jobComboBox if pop is less than
			// POPULATION_WITH_COMMANDER)
			String selectedJobStr = list.get(last).getJobType();
			jobCache = selectedJobStr; // must update the jobCache prior to setSelectedItem or else a new job
										// reassignment will be submitted in
			jobComboBox.setSelectedItem(selectedJobStr);
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		if (!uiDone)
			initializeUI();
		
		TableStyle.setTableStyle(table);

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

			// Update job if necessary.
			if (dead) {
				jobCache = deathInfo.getJob();
				jobComboBox.setEnabled(false);
				roleComboBox.setEnabled(false);
//				roleTF.setText("N/A");
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
					person.getJobHistory().setSolCache(solCache);
				} // end of if (solElapsed != solCache)
			} // end of else if not dead)

		} else if (unit instanceof Robot) {
			robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getSystemCondition().isInoperable();
			// deathInfo = robot.getSystemCondition().getDeathDetails();
		}
	}

	/**
	 * Action event occurs.
	 * 
	 * @param event {@link ActionEvent} the action event
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		
		if (source == roleComboBox) {
			Person person = (Person) unit;
			String selected = (String) roleComboBox.getSelectedItem();
			String roleStr = person.getRole().getType().getName();
			
			if (!selected.equalsIgnoreCase(roleStr)) {
				if (RoleUtil.isLeadershipRole(RoleType.getType(selected))) {
					int box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(), 
							"Are you sure you want to change the role to " + selected + " ?");  
					if (box == JOptionPane.YES_OPTION) { 
	//					roleComboBox.setSelectedItem(selected);
						roleCache = selected;
						person.getRole().setNewRoleType(RoleType.getType(selected));
	//					desktop.getMainWindow().getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
					}
					else {
						roleComboBox.setSelectedItem(roleStr);
					}
				}
				
				else {
					int box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(), 
							"Are you sure you want to change the role to " + selected + " ?");  
					if (box == JOptionPane.YES_OPTION) { 
	//					roleComboBox.setSelectedItem(selected);
						roleCache = selected;
						person.getRole().setNewRoleType(RoleType.getType(selected));
	//					desktop.getMainWindow().getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
					}
					else {
						roleComboBox.setSelectedItem(roleStr);
					}
				}
			}
		}
		
		else if (source == jobComboBox) {
			Person person = null;
			Robot robot = null;

			if (unit instanceof Person) {
				person = (Person) unit;

				String selectedJobStr = (String) jobComboBox.getSelectedItem();
				String jobStrCache = person.getMind().getJob().getName(person.getGender());

				if (!selectedJobStr.equalsIgnoreCase(jobStrCache)) {
					int box = JOptionPane.showConfirmDialog(desktop.getMainWindow().getFrame(), 
							"Are you sure you want to change the job to " + selectedJobStr + " ?");  
					if (box == JOptionPane.YES_OPTION) { 
	//					
						considerJobChange(jobStrCache, selectedJobStr);
						
//						desktop.getMainWindow().getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
					}
					else {
						jobComboBox.setSelectedItem(jobStrCache);
					}		
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
	public void considerJobChange(String jobStrCache, String selectedJobStr) {
		
		// if job is Politician, loads and set to the previous job and quit;
		if (JobType.getJobType(jobStrCache) == JobType.getJobType(POLITICIAN)) {
			jobComboBox.setSelectedItem(jobStrCache);
			jobChangeLabel.setForeground(Color.red);
			jobChangeLabel.setText("Mayor cannot switch job arbitrarily!");
			jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		else if (JobType.getJobType(selectedJobStr) == JobType.getJobType(POLITICIAN)) {
			jobComboBox.setSelectedItem(jobStrCache);
			jobChangeLabel.setForeground(Color.red);
			jobChangeLabel.setText("The Politician job is currently reserved for Mayor only.");
			jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}

		else if (JobType.getJobType(jobStrCache) != JobType.getJobType(selectedJobStr)) {
			// Use getAssociatedSettlement instead of getSettlement()
			int pop = 0;

			pop = settlement.getNumCitizens();

			// if the population is beyond 4
			if (pop > ChainOfCommand.POPULATION_WITH_COMMANDER) {

				jobChangeLabel.setForeground(Color.BLUE);
				
				String s = person + "'s job reassignment submitted on " + MarsClock.getTruncatedDateTimeStamp(marsClock);
				jobChangeLabel.setText(s);
				logger.info(s);
				firstNotification = true;

				JobHistory jh = person.getJobHistory();

				statusCache = JobAssignmentType.PENDING;

				jh.savePendingJob(selectedJobStr, JobUtil.USER, statusCache, null, true);
				// Set the combobox selection back to its previous job type for the time being
				// until the reassignment is approved
				jobComboBox.setSelectedItem(jobStrCache);
				// disable the combobox so that user cannot submit job reassignment for a period
				// of time
				jobComboBox.setEnabled(false);
				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();
			}

			else if (pop > 0 && pop <= ChainOfCommand.POPULATION_WITH_COMMANDER) {
				jobChangeLabel.setForeground(Color.RED);
				jobChangeLabel.setText("");
				jobComboBox.setSelectedItem(selectedJobStr);
				// pop is small, things need to be flexible. Thus automatic approval
				statusCache = JobAssignmentType.APPROVED;
				person.getMind().reassignJob(selectedJobStr, true, JobUtil.USER, statusCache,
						JobUtil.USER);

				// System.out.println("Yes they are diff");
				jobCache = selectedJobStr;

				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();

			}
		}
		else
			jobComboBox.setSelectedItem(jobStrCache);
	}
	
	public void destroy() {
		table = null;
		jobLabel = null;
//		roleTF = null;
		desktop = null;
		jobChangeLabel = null;
		ratingLabel = null;
		jobComboBox = null;
		jobHistoryTableModel = null;
		starRater = null;
		marsClock = null;
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
		JobHistoryTableModel(Unit unit) {
			Person person = null;
//			Robot robot = null;
			if (unit instanceof Person) {
				person = (Person) unit;
				jobHistory = person.getJobHistory();
			} 
//				else if (unit instanceof Robot) {
//				// robot = (Robot) unit;
//				// jobHistory = robot.getJobHistory();
//			}

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