/**
 * Mars Simulation Project
 * TabPanelCareer.java
 * @version 3.08 2015-06-16
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
import javax.swing.JTextField;
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
import org.mars_sim.msp.ui.swing.tool.MultisortTableHeaderCellRenderer;
import org.mars_sim.msp.ui.swing.tool.StarRater;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import net.java.balloontip.BalloonToolTip;

/**
 * The TabPanelCareer is a tab panel for viewing a person's career path and job history.
 */
public class TabPanelCareer
extends TabPanel
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	private static final int RATING_DAYS = 1;

	/** data cache */
	private int solCache = 1;

	private String jobCache = "";
	private String roleCache;
	private JobAssignmentType statusCache = JobAssignmentType.APPROVED;


	private int solRatingSubmitted = 0;

	private JTable table;

	private JLabel jobLabel, roleLabel, errorLabel, ratingLabel;
	private JTextField roleTF;

	private JComboBoxMW<?> jobComboBox;

	private JobHistoryTableModel jobHistoryTableModel;

	private StarRater starRater;
	private MarsClock clock;

	private BalloonToolTip balloonToolTip = new BalloonToolTip();
	
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

		clock = Simulation.instance().getMasterClock().getMarsClock();

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
			JPanel topPanel = new JPanel(new GridLayout(3, 2, 0, 0));
			topPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			topPanel.setBorder(new MarsPanelBorder());
			topContentPanel.add(topPanel, BorderLayout.NORTH);

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
			JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); //GridLayout(1, 2));
			//rolePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			topPanel.add(rolePanel);

			// Prepare role label
			roleLabel = new JLabel(Msg.getString("TabPanelCareer.roleType"));//, JLabel.RIGHT); //$NON-NLS-1$
			roleLabel.setSize(10, 2);
			balloonToolTip.createBalloonTip(roleLabel, "<html>The role in the settlement based <br> on one's skills and talents.</html>"); //$NON-NLS-1$
			rolePanel.add(roleLabel);

			roleCache = person.getRole().toString();
			roleTF = new JTextField(roleCache);
			roleTF.setEditable(false);
			//roleTF.setBounds(0, 0, 0, 0);
			roleTF.setColumns(13);
			rolePanel.add(roleTF);

			JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));// GridLayout(1, 2, 0, 0));
			//JPanel rPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JLabel rLabel = new JLabel("Rating : ");//, JLabel.CENTER);
			ratingPanel.add(rLabel);
			starRater = new StarRater(5, 0, 0);
			//starRater.setToolTipText("Click to submit your rating to supervisor (once every 7 sols)");
			balloonToolTip.createBalloonTip(starRater, "<html>Submit your rating of this person <br> (once every 7 sols only)</html>"); //$NON-NLS-1$

    		List<JobAssignment> list = person.getJobHistory().getJobAssignmentList();

	        starRater.addStarListener(
	            new StarRater.StarListener()   {
	                @SuppressWarnings("deprecation")
					public void handleSelection(int selection) {
	                	if (starRater.isEnabled()) {
		                    //System.out.println(selection);
		            		MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
		                	ratingLabel.setText("Rating submitted on " + MarsClock.getDateTimeStamp(clock));
		                	ratingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			        		starRater.setRating(selection);
			        		list.get(list.size()-1).setJobRating(selection);
			        		starRater.disable();
			        		solRatingSubmitted = MarsClock.getSolOfYear(clock);
	                	}
	                }
	            });

	        ratingPanel.add(starRater);
			topPanel.add(ratingPanel);

			JPanel midPanel = new JPanel(new GridLayout(2, 1, 0, 0));
			topContentPanel.add(midPanel, BorderLayout.CENTER);

			ratingLabel = new JLabel();
			ratingLabel.setFont(new Font("Courier New", Font.ITALIC, 12));
			ratingLabel.setForeground(Color.blue);
			midPanel.add(ratingLabel);

			errorLabel = new JLabel();
			errorLabel.setFont(new Font("Courier New", Font.ITALIC, 12));
			errorLabel.setForeground(Color.red);
			midPanel.add(errorLabel);

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
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.setCellSelectionEnabled(false);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		scrollPanel.setViewportView(table);

		// 2015-06-08 Added sorting
		table.setAutoCreateRowSorter(true);
		table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());

		// 2015-06-08 Added setTableStyle()
		//TableStyle.setTableStyle(table);

		update();

		jobHistoryTableModel.update();

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
				starRater.disable();

			} else {

				if (clock == null)
					clock = Simulation.instance().getMasterClock().getMarsClock();
		        // check for the passing of each day
		        int solElapsed = MarsClock.getSolOfYear(clock);

	        	// If the rating or job reassignment request is at least one day ago
		        if (solElapsed != solCache) {

		        	if (solElapsed > solRatingSubmitted + RATING_DAYS) {
						starRater.setRating(0);
			        	starRater.enable();
			        	ratingLabel.setText("");
		        	}

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

			        if (pop > UnitManager.POPULATION_WITH_COMMANDER) {

			        	List<JobAssignment> jobAssignmentList = person.getJobHistory().getJobAssignmentList();
			        	int last = jobAssignmentList.size()-1;

			        	JobAssignmentType status = jobAssignmentList.get(last).getStatus();

			        	if (status.equals(JobAssignmentType.PENDING)) {
					    	System.out.println("\n< " + person.getName() + " > ");
				        	System.out.println("status still pending");
			        	}

			        	else if (status.equals(JobAssignmentType.APPROVED)) {

			        		if (statusCache.equals(JobAssignmentType.PENDING)) {
						    	System.out.println("\n< " + person.getName() + " > ");

				        		statusCache = JobAssignmentType.APPROVED;
					        	System.out.println("status is now approved");

					        	String selectedJobStr = jobAssignmentList.get(last).getJobType();
							    jobComboBox.setSelectedItem(selectedJobStr);
							    System.out.println("TabPanelCareer : selectedJobStr is " + selectedJobStr);

					        	jobComboBox.setEnabled(true);

								errorLabel.setForeground(Color.red);
					        	errorLabel.setText("");

								// updates the jobHistoryList in jobHistoryTableModel
								jobHistoryTableModel.update();

								String roleNew = person.getRole().toString();
								if (!roleCache.equals(roleNew)) {
									System.out.println("TabPanelCareer : Old Role : " + roleCache + "   New Role : " + roleNew);
									roleCache = roleNew;
									roleTF.setText(roleCache);
									System.out.println("TabPanelCareer : just set New Role in TextField");
								}
			        		}
				        }
			        }
		        	solCache = solElapsed;
			    } // end of if (solElapsed != solCache)
			} // end of else if not dead)

		} else if (unit instanceof Robot) {
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
					errorLabel.setText("Mayor cannot switch job arbitrarily!");
					errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
				}

				else if (selectedJobStr.equals("Manager")) {
					jobComboBox.setSelectedItem(jobStrCache);
					errorLabel.setText("Manager job is available for Mayor only!");
					errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
				}

				else if (!jobCache.equals(selectedJobStr)) {
/*
					int pop = 0;
					// 2015-09-03 Check to see if getSettlement() is null
					if (person.getSettlement() == null) {
						// the person is NOT inside a settlement
						jobComboBox.setSelectedItem(jobStrCache);
						errorLabel.setForeground(Color.RED);
						errorLabel.setText("Must be inside a settlement to file the request.");
						errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
					}
					else
						pop = person.getSettlement().getAllAssociatedPeople().size();
*/
					// 2015-09-03 use getAssociatedSettlement instead of getSettlement()
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

			        	if (clock == null)
			        		clock = Simulation.instance().getMasterClock().getMarsClock();

						errorLabel.setForeground(Color.BLUE);
			        	errorLabel.setText("Job Reassignment submitted on " + MarsClock.getDateTimeStamp(clock));
			        	errorLabel.setHorizontalAlignment(SwingConstants.CENTER);

			        	JobHistory jh = person.getJobHistory();
			        	//System.out.println("TabPanelCareer : actionPerformed() : calling savePendingJob()");
			        	jh.savePendingJob(selectedJobStr, JobManager.USER, JobAssignmentType.PENDING, null, true);

			        	//statusCache = JobAssignmentType.PENDING;

			        	// set the combobox selection back to its previous job type for the time being until the reassignment is approved
			        	jobComboBox.setSelectedItem(jobCache);

			        	// disable the combobox so that user cannot submit job reassignment for a period of time
						jobComboBox.setEnabled(false);

						// updates the jobHistoryList in jobHistoryTableModel
						jobHistoryTableModel.update();
			        }

			        else if (pop > 0 && pop <= UnitManager.POPULATION_WITH_COMMANDER){
			        	System.out.println("TabPanelCareer : actionPerformed() : pop <= 4");
						errorLabel.setForeground(Color.RED);
						errorLabel.setText("");
					    jobComboBox.setSelectedItem(selectedJobStr);

						person.getMind().reassignJob(selectedJobStr, true, JobManager.USER, JobAssignmentType.APPROVED, JobManager.USER);

						//List<JobAssignment> jobAssignmentList = person.getJobHistory().getJobAssignmentList();

			        	//jobAssignmentList.get(jobAssignmentList.size()-1).setAuthorizedBy("Settlement");
			        	//jobAssignmentList.get(jobAssignmentList.size()-1).setStatus(JobAssignmentType.APPROVED);
	                	//if (clock == null)
	                	//	clock = Simulation.instance().getMasterClock().getMarsClock();
			        	//jobAssignmentList.get(jobAssignmentList.size()-1).setTimeAuthorized(clock);

						// updates the jobHistoryList in jobHistoryTableModel
						jobHistoryTableModel.update();
						//System.out.println("Yes they are diff");
						jobCache = selectedJobStr;

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
		 * @param
		 * @param
		 */
		private void update() {
			jobAssignmentList = jobHistory.getJobAssignmentList();
        	fireTableDataChanged();
		}

	}

}