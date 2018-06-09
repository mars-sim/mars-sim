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
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.Mind;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.job.JobAssignment;
import org.mars_sim.msp.core.person.ai.job.JobAssignmentType;
import org.mars_sim.msp.core.person.ai.job.JobHistory;
import org.mars_sim.msp.core.person.ai.job.JobManager;
import org.mars_sim.msp.core.person.medical.DeathInfo;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.BotMind;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.tool.StarRater;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.managers.tooltip.TooltipWay;
//import com.alee.managers.language.data.TooltipWay;
import com.alee.managers.tooltip.TooltipManager;

/**
 * The TabPanelCareer is a tab panel for viewing a person's career path and job history.
 */
public class TabPanelCareer
extends TabPanel
implements ActionListener {

	private static Logger logger = Logger.getLogger(TabPanelCareer.class.getName());

	private static final int RATING_DAYS = 7;
	private static final String POLITICIAN = "Politician";
	
	/** data cache */
	private int solCache = 1;

	private String jobCache = "";
	private String roleCache;
	private String dateTimeRatingSubmitted;
	private JobAssignmentType statusCache = JobAssignmentType.APPROVED;//PENDING;

	private int solRatingSubmitted = -1;

	private JTable table;

	private JLabel jobLabel, roleLabel, jobChangeLabel, ratingLabel;
	private JTextField roleTF;

	private JComboBoxMW<?> jobComboBox;

	private JobHistoryTableModel jobHistoryTableModel;

	private StarRater starRater, aveRater;
	private MarsClock marsClock;

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

		//MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
		marsClock = Simulation.instance().getMasterClock().getMarsClock();
		//int solElapsed = MarsClock.getSolOfYear(clock);

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
			dead = robot.getSystemCondition().isInoperable();
			//deathInfo = robot.getSystemCondition().getDeathDetails();
		}

		// Prepare label panel
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare title label
		JLabel titleLabel = new JLabel(Msg.getString("TabPanelCareer.title"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		labelPanel.add(titleLabel);

		if (unit instanceof Person) {
	    	person = (Person) unit;

    		solCache = person.getJobHistory().getSolCache();

    		JPanel firstPanel = new JPanel(new BorderLayout());//GridLayout(2, 1, 5, 0));
			//firstPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			firstPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(firstPanel, BorderLayout.NORTH);

			// Prepare job panel
			JPanel topPanel = new JPanel(new SpringLayout());//GridLayout(2, 2, 0, 0));

			firstPanel.add(topPanel, BorderLayout.CENTER);


			// Prepare job label
			jobLabel = new JLabel(Msg.getString("TabPanelCareer.jobType"), JLabel.RIGHT); //$NON-NLS-1$
			topPanel.add(jobLabel);
			//balloonToolTip.createBalloonTip(jobLabel, Msg.getString("TabPanelCareer.jobType.tooltip")); //$NON-NLS-1$
			TooltipManager.setTooltip (jobLabel, Msg.getString("TabPanelCareer.jobType.tooltip"), TooltipWay.down);
			
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

			JPanel jobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //new GridLayout(3, 1, 0, 0)); //
			jobPanel.add(jobComboBox);

			topPanel.add(jobPanel);
			TooltipManager.setTooltip (jobComboBox, Msg.getString("TabPanelCareer.jobComboBox.tooltip"), TooltipWay.down);
			//balloonToolTip.createBalloonTip(jobComboBox, Msg.getString("TabPanelCareer.jobComboBox.tooltip")); //$NON-NLS-1$

			// check if a job reassignment is still pending for review
			// if true, disable the combobox

			// Prepare role label
			roleLabel = new JLabel(Msg.getString("TabPanelCareer.roleType"), JLabel.RIGHT); //$NON-NLS-1$
			roleLabel.setSize(10, 2);
			topPanel.add(roleLabel);//, JLabel.BOTTOM);

			roleCache = person.getRole().toString();
			roleTF = new JTextField(roleCache);
			roleTF.setEditable(false);
			//roleTF.setBounds(0, 0, 0, 0);
			roleTF.setColumns(20);

			// Prepare role panel
			JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); //GridLayout(1, 2));
			//rolePanel.setSize(120, 20);
			//rolePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			rolePanel.add(roleTF);

			topPanel.add(rolePanel);

			jobChangeLabel = new JLabel();
			//jobChangeLabel.setSize(300, 30);
			jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
			jobChangeLabel.setFont(new Font("Courier New", Font.ITALIC, 12));
			jobChangeLabel.setForeground(Color.blue);
			firstPanel.add(jobChangeLabel, BorderLayout.SOUTH);
			//balloonToolTip.createBalloonTip(roleLabel, roleTip);
			//balloonToolTip.createBalloonTip(roleTF, roleTip);
			TooltipManager.setTooltip (roleLabel, Msg.getString("TabPanelCareer.roleType.tooltip"), TooltipWay.down);//$NON-NLS-1$
			
			// 2017-03-28 Prepare SpringLayout
			SpringUtilities.makeCompactGrid(topPanel,
			                                2, 2, //rows, cols
			                                80, 5,        //initX, initY
			                                10, 1);       //xPad, yPad


			JPanel ratingPanel = new JPanel(new BorderLayout());
			ratingPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(ratingPanel, BorderLayout.CENTER);

			List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();
			//int size = list.size();

			JPanel springPanel = new JPanel(new SpringLayout());//GridLayout(2,1,5,5));// GridLayout(1, 2, 0, 0));
			ratingPanel.add(springPanel, BorderLayout.CENTER);
			//raterPanel.setAlignmentY(TOP_ALIGNMENT);

			JLabel aveRatingLabel = new JLabel("Overall Performance : ", JLabel.RIGHT);
			springPanel.add(aveRatingLabel);

			aveRater = new StarRater(5, calculateAveRating(list));
			//aveRater.setHorizontalAlignment(SwingConstants.LEFT);
			aveRater.setEnabled(false);
			springPanel.add(aveRater);

			//String tip = Msg.getString("TabPanelCareer.aveRater.tooltip");
			//balloonToolTip.createBalloonTip(aveRatingLabel, tip); //$NON-NLS-1$
			//balloonToolTip.createBalloonTip(aveRater, tip); //$NON-NLS-1$
			TooltipManager.setTooltip (aveRatingLabel, Msg.getString("TabPanelCareer.aveRater.tooltip"), TooltipWay.down);//$NON-NLS-1$
			
			JLabel raterLabel = new JLabel("Your Rating : ", JLabel.RIGHT);
			springPanel.add(raterLabel);
			starRater = new StarRater(5, 0, 0);
			
			TooltipManager.setTooltip (raterLabel, Msg.getString("TabPanelCareer.raterLabel.tooltip"), TooltipWay.down);//$NON-NLS-1$
			TooltipManager.setTooltip (starRater, Msg.getString("TabPanelCareer.starRater.tooltip"), TooltipWay.down);//$NON-NLS-1$
			//starRater.setToolTipText("Click to submit your rating to supervisor (once every 7 sols)");
			//balloonToolTip.createBalloonTip(raterLabel, Msg.getString("TabPanelCareer.raterLabel.tooltip")); //$NON-NLS-1$
			//balloonToolTip.createBalloonTip(starRater, Msg.getString("TabPanelCareer.starRater.tooltip")); //$NON-NLS-1$

	        starRater.addStarListener(
	            new StarRater.StarListener()   {
					public void handleSelection(int selection) {
	                	if (starRater.isEnabled()) {
		                    //System.out.println(selection);
		            		//MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
		            		int sol = marsClock.getMissionSol();
		            		dateTimeRatingSubmitted =  MarsClock.getDateTimeStamp(marsClock);
		                	ratingLabel.setText("Job rating submitted on " + dateTimeRatingSubmitted);
		                	ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			        		starRater.setRating(selection);

			        		int size = list.size();
			        		// check if a new job reassignment has just been submitted
			        		if (list.get(size-1).getStatus().equals(JobAssignmentType.PENDING)) {
			        			list.get(size-2).setJobRating(selection);
			        			list.get(size-2).setSolRatingSubmitted(sol);
			        		}
			        		else {
			        			list.get(size-1).setJobRating(selection);
			        			list.get(size-1).setSolRatingSubmitted(sol);
			        		}
			        		solRatingSubmitted = sol;
			    			//starRater.setSelection(0);
			        		starRater.setEnabled(false);//disable();

			        		aveRater.setRating(calculateAveRating(list));
	                	}
	                }
	            });

	        springPanel.add(starRater);

			// 2017-03-28 Prepare SpringLayout
			SpringUtilities.makeCompactGrid(springPanel,
			                                2, 2, //rows, cols
			                                80, 10,        //initX, initY
			                                20, 10);       //xPad, yPad

			ratingLabel = new JLabel("Job Rating");
			//ratingLabel.setSize(300, 30);
			ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			ratingLabel.setFont(new Font("Courier New", Font.ITALIC, 12));
			ratingLabel.setForeground(Color.blue);
			ratingPanel.add(ratingLabel, BorderLayout.SOUTH);

			// 2015-10-30 Added checking if user already submitted rating or submitted a job reassignment that's still not being reviewed
			checkingJobRating(list);

			dead = person.getPhysicalCondition().isDead();
			deathInfo = person.getPhysicalCondition().getDeathDetails();

			// 2016-09-21 Checked if the person is dead
			if (dead) {
				jobCache = deathInfo.getJob();
				jobComboBox.setEnabled(false);
				roleTF.setText("N/A");
				starRater.setSelection(0);
				starRater.setEnabled(false);

			}
			else
				// 2015-10-30 Added checking for the status of Job Reassignment
				checkingJobReassignment(person, list);
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
		JPanel jobHistoryPanel = new JPanel(new GridLayout(2, 1, 1, 1));
		centerContentPanel.add(jobHistoryPanel, BorderLayout.NORTH);

		// Prepare job title label
		JLabel historyLabel = new JLabel(Msg.getString("TabPanelCareer.history"), JLabel.CENTER); //$NON-NLS-1$
		//historyLabel.setBounds(0, 0, width, height);
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
		table = new ZebraJTable(jobHistoryTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(25);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.getColumnModel().getColumn(4).setPreferredWidth(50);
		table.setCellSelectionEnabled(false);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		scrollPanel.setViewportView(table);

		// 2015-06-08 Added sorting
		table.setAutoCreateRowSorter(true);
		//if (!MainScene.OS.equals("linux")) {
        //	table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
		//}
		// 2015-06-08 Added setTableStyle()
		TableStyle.setTableStyle(table);
		update();
		jobHistoryTableModel.update();

	}

	/*
	 * Checks a job rating is submitted or a job reassignment is submitted and is still not being reviewed
	 */
	public void checkingJobRating(List<JobAssignment> list) {
		int size = list.size();
    	if (solRatingSubmitted == -1) {
    		// the TabPanelCareer was closed and retrieve the saved value of solRatingSubmitted from JobAssignment
        	if (list.get(size-1).getStatus().equals(JobAssignmentType.PENDING)) {
        		solRatingSubmitted = list.get(size-2).getSolRatingSubmitted();
            	//System.out.println("Yes Pending, solRatingSubmitted is "+ solRatingSubmitted);
        	}
    		else {
    			solRatingSubmitted = list.get(size-1).getSolRatingSubmitted();
    	    	//System.out.println("Nothing Pending. solRatingSubmitted is "+ solRatingSubmitted);
    		}
    	}

    	if (solRatingSubmitted == -1) {
        	// no rating has ever been submitted. Thus he/she has a new job assignment
        	starRater.setEnabled(true);
			starRater.setSelection(0);
        	ratingLabel.setText("");
        }
        else {
    		int solElapsed = marsClock.getMissionSol();
        	if (solElapsed > solRatingSubmitted + RATING_DAYS) {
	        	starRater.setEnabled(true);
				starRater.setSelection(0);
	        	ratingLabel.setText("");
        	}
        	else {
        		starRater.setSelection(0);
            	starRater.setEnabled(false);
            	if (dateTimeRatingSubmitted != null)
                	ratingLabel.setText("Job rating last submitted on " + dateTimeRatingSubmitted);
            	else
            		ratingLabel.setText("Job rating last submitted on Sol " + solRatingSubmitted);
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
		score = score/size; // divided by 2 because job rating in JobAssignment is from 0 to 10
		if (score > 5)
			score = 5;
		return (int)score;
	}

	/*
	 * Checks for the status of Job Reassignment
	 */
	public void checkingJobReassignment(Person person, List<JobAssignment> list) {

        int pop = 0;

        Settlement settlement = null;
        if (person.getAssociatedSettlement() != null)
        	settlement = person.getAssociatedSettlement();
        else if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
        	settlement = (Settlement) person.getTopContainerUnit();
        }
        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
        	Vehicle vehicle = (Vehicle) person.getContainerUnit();
        	settlement = vehicle.getSettlement();
        }

      //List<JobAssignment> jobAssignmentList = person.getJobHistory().getJobAssignmentList();
    	int last = list.size()-1;

    	JobAssignmentType status = list.get(last).getStatus();

        pop = settlement.getAllAssociatedPeople().size();

        if (pop > UnitManager.POPULATION_WITH_COMMANDER) {

        	if (status.equals(JobAssignmentType.PENDING)) {
        		statusCache = JobAssignmentType.PENDING;
		    	//System.out.println("\n< " + person.getName() + " > ");
	        	//System.out.println("status still pending");
	        	jobComboBox.setEnabled(false);
	        	//jobComboBox.setSelectedItem(jobCache);
				jobChangeLabel.setForeground(Color.blue);
	        	jobChangeLabel.setText("Job reassignment submitted on " + list.get(last).getTimeSubmitted());
        	}

        	// detects a change of status from pending to approved
        	else if (statusCache.equals(JobAssignmentType.PENDING)) {
        		if (status.equals(JobAssignmentType.APPROVED)) {
               		statusCache = JobAssignmentType.APPROVED;
		        	logger.info(person.getName() + "'s job reassignment had been reviewed and approved.");

		        	String selectedJobStr = list.get(last).getJobType();
		        	jobCache = selectedJobStr; // must update the jobCache prior to setSelectedItem or else a new job reassignment will be submitted in
				    jobComboBox.setSelectedItem(selectedJobStr);

				    person.getMind().setJobLock(true);

        		}
        		else if (status.equals(JobAssignmentType.NOT_APPROVED))	{
               		statusCache = JobAssignmentType.NOT_APPROVED;
		        	logger.info(person.getName() + "'s job reassignment had been reviewed and was NOT approved.");

		        	String selectedJobStr = list.get(last-1).getJobType();
		        	jobCache = selectedJobStr; // must update the jobCache prior to setSelectedItem or else a new job reassignment will be submitted in
				    jobComboBox.setSelectedItem(selectedJobStr);

        		}

        		//int solElapsed = MarsClock.getSolOfYear(clock);
				//if (solElapsed != solCache) {
	        	//solCache = solElapsed;
	    		//person.getJobHistory().setSolCache(solCache);

	        	jobComboBox.setEnabled(true);
	        	jobChangeLabel.setText("");

	        	// TODO: determine if new rating submission should be allowed immediately at the beginning of a new assignment
	        	solRatingSubmitted = -1;
	        	starRater.setSelection(0);
	        	starRater.setEnabled(true);
	        	ratingLabel.setText("");

				// updates the jobHistoryList in jobHistoryTableModel
				jobHistoryTableModel.update();

				String roleNew = person.getRole().toString();
				if (!roleCache.equals(roleNew)) {
					//System.out.println("TabPanelCareer : Old Role : " + roleCache + "   New Role : " + roleNew);
					roleCache = roleNew;
					roleTF.setText(roleCache);
					//System.out.println("TabPanelCareer : just set New Role in TextField");
				}
			    //} // end of if (solElapsed != solCache)
				    //else {
						//jobChangeLabel.setForeground(Color.blue);
				    	//jobChangeLabel.setText("The new job " + selectedJobStr + " will be effective at the start of next sol");
				    //}
        		} //  if (statusCache.equals(JobAssignmentType.PENDING))
        		else {
    				; // do nothing. at the start of sim
    		    } //if (statusCache.equals(JobAssignmentType.PENDING))

	        } else  {
	        	//2016-04-20 update the jobComboBox if pop is less than POPULATION_WITH_COMMANDER)
	        	String selectedJobStr = list.get(last).getJobType();
	        	jobCache = selectedJobStr; // must update the jobCache prior to setSelectedItem or else a new job reassignment will be submitted in
			    jobComboBox.setSelectedItem(selectedJobStr);
	        }
        }
	/**
	 * Updates the info on this panel.
	 */
	public void update() {

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
				roleTF.setText("N/A");
				starRater.setSelection(0);
				starRater.setEnabled(false);

			} else {

	        	List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();
		        //System.out.println("solCache is " + solCache);
				// 2015-10-30 Added checking if user already submitted rating or submitted a job reassignment that's still not being reviewed
				checkingJobRating(list);

				// 2015-10-30 Added checking for the status of Job Reassignment
				checkingJobReassignment(person, list);

		        // check for the passing of each day
		        int solElapsed = marsClock.getMissionSol();

	        	// If the rating or job reassignment request is at least one day ago
		        if (solElapsed != solCache) {
		        	solCache = solElapsed;
		    		person.getJobHistory().setSolCache(solCache);
		        } // end of if (solElapsed != solCache)
			} // end of else if not dead)

		} else if (unit instanceof Robot) {
	        robot = (Robot) unit;
			botMind = robot.getBotMind();
			dead = robot.getSystemCondition().isInoperable();
			//deathInfo = robot.getSystemCondition().getDeathDetails();
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

				// if job is Politician, loads and set to the previous job and quit;
				if (jobStrCache.equals(POLITICIAN)) {
					jobComboBox.setSelectedItem(jobStrCache);
					jobChangeLabel.setForeground(Color.red);
					jobChangeLabel.setText("Mayor cannot switch job arbitrarily!");
					jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
				}

				else if (selectedJobStr.equals(POLITICIAN)) {
					jobComboBox.setSelectedItem(jobStrCache);
					jobChangeLabel.setForeground(Color.red);
					jobChangeLabel.setText("The Job Politician is currently reserved for Mayor only.");
					jobChangeLabel.setHorizontalAlignment(SwingConstants.CENTER);
				}

				else if (!jobCache.equals(selectedJobStr)) {
				// Use getAssociatedSettlement instead of getSettlement()
					int pop = 0;
			        Settlement settlement = null;
			        if (person.getAssociatedSettlement() != null)
			        	settlement = person.getAssociatedSettlement();
			        else if (person.getLocationSituation() == LocationSituation.OUTSIDE) {
			        	settlement = (Settlement) person.getTopContainerUnit();
			        }
			        else if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {
			        	Vehicle vehicle = (Vehicle) person.getContainerUnit();
			        	settlement = vehicle.getSettlement();
			        }

			        pop = settlement.getAllAssociatedPeople().size();

					// if the population is beyond 4
			        if (pop > UnitManager.POPULATION_WITH_COMMANDER) {
				    	//System.out.println("\n< " + person.getName() + " > ");
			        	//System.out.println("TabPanelCareer : actionPerformed() : pop > 4");

			        	//if (clock == null)
			        	//	clock = Simulation.instance().getMasterClock().getMarsClock();

						jobChangeLabel.setForeground(Color.BLUE);
			        	jobChangeLabel.setText("Job reassignment submitted on " + MarsClock.getDateTimeStamp(marsClock));

			        	JobHistory jh = person.getJobHistory();

			        	statusCache = JobAssignmentType.PENDING;
			        	//System.out.println("TabPanelCareer : actionPerformed() : calling savePendingJob()");
			        	jh.savePendingJob(selectedJobStr, JobManager.USER, statusCache, null, true);
			        	// set the combobox selection back to its previous job type for the time being until the reassignment is approved
			        	jobComboBox.setSelectedItem(jobCache);
			        	// disable the combobox so that user cannot submit job reassignment for a period of time
						jobComboBox.setEnabled(false);
						// updates the jobHistoryList in jobHistoryTableModel
						jobHistoryTableModel.update();
			        }

			        else if (pop > 0 && pop <= UnitManager.POPULATION_WITH_COMMANDER){
			        	//System.out.println("TabPanelCareer : actionPerformed() : pop <= 4");
						jobChangeLabel.setForeground(Color.RED);
						jobChangeLabel.setText("");
					    jobComboBox.setSelectedItem(selectedJobStr);
					    // pop is small, things need to be flexible. Thus automatic approval
					    statusCache = JobAssignmentType.APPROVED;
						person.getMind().reassignJob(selectedJobStr, true, JobManager.USER, statusCache, JobManager.USER);

						//System.out.println("Yes they are diff");
						jobCache = selectedJobStr;

						// updates the jobHistoryList in jobHistoryTableModel
						jobHistoryTableModel.update();


			        }
				}
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
	private class JobHistoryTableModel
	extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private JobHistory jobHistory;
		private JobAssignment ja;

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
			return 5;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			else if (columnIndex == 1) dataType = String.class;
			else if (columnIndex == 2) dataType = String.class;
			else if (columnIndex == 3) dataType = String.class;
			else if (columnIndex == 4) dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelCareer.column.time"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelCareer.column.jobType"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelCareer.column.initiated"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelCareer.column.status"); //$NON-NLS-1$
			else if (columnIndex == 4) return Msg.getString("TabPanelCareer.column.authorized"); //$NON-NLS-1$
			else return null;
		}

		public Object getValueAt(int row, int column) {
			int r = jobAssignmentList.size() - row - 1;
			ja = jobAssignmentList.get(r);
			//System.out.println(" r is " + r);
			if (column == 0) return ja.getTimeSubmitted();   //MarsClock.getDateTimeStamp(ja.getTimeSubmitted());
			else if (column == 1) return ja.getJobType();
			else if (column == 2) return ja.getInitiator();
			else if (column == 3) return ja.getStatus();
			else if (column == 4) return ja.getAuthorizedBy();
			else return null;
		}

		/**
		 * Prepares the job history of the person
		 */
		private void update() {
			jobAssignmentList = jobHistory.getJobAssignmentList();
        	fireTableDataChanged();
		}

	}


	public void destroy() {
		table =  null;
		jobLabel = null;
		roleTF = null;
		desktop = null;
		jobChangeLabel = null;
		ratingLabel = null;
		jobComboBox = null;
		jobHistoryTableModel = null;
		starRater = null;
		marsClock = null;
		//balloonToolTip = null;
	}

}