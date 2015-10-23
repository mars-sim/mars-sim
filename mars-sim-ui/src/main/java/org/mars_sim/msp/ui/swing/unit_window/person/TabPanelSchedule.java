/**
 * Mars Simulation Project
 * TabPanelFavorite.java
 * @version 3.08 2015-03-26
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.TaskSchedule;
import org.mars_sim.msp.core.person.TaskSchedule.OneTask;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.ColumnResizer;
import org.mars_sim.msp.ui.swing.tool.MultisortTableHeaderCellRenderer;
import org.mars_sim.msp.ui.swing.tool.TableStyle;
import org.mars_sim.msp.ui.swing.tool.ZebraJTable;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.StormTrackingWindow;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.BalloonToolTip;
import net.java.balloontip.CustomBalloonTip;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.ModernBalloonStyle;
import net.java.balloontip.styles.TexturedBalloonStyle;
import net.java.balloontip.utils.FadingUtils;
import net.java.balloontip.utils.TimingUtils;
import net.java.balloontip.utils.ToolTipUtils;


/**
 * The TabPanelSchedule is a tab panel showing the daily schedule a person.
 */
public class TabPanelSchedule
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	//private int sol;
	private int todayCache;
	private int today;
	private int start;
	private int end; 

	private boolean hideRepeated, hideRepeatedCache, isRealTimeUpdate;

	private Integer selectedSol;
	private Integer todayInteger;

	private String shiftType, shiftCache = null;

	private JTable table ;

	private JCheckBox hideBox;
	private JCheckBox realTimeBox;
	private JTextField shiftTF;
	private JLabel shiftLabel;

	private JComboBoxMW<Object> comboBox;
	private DefaultComboBoxModel<Object> comboBoxModel;
	private ScheduleTableModel scheduleTableModel;

	private Color fillColorCache;
	//private Color transparentFill;
	//private ModernBalloonStyle style;
	
	private List<OneTask> tasks;
	private List<Object> solList;
	private Map <Integer, List<OneTask>> schedules;

	private Person person;
	private Robot robot;
	private TaskSchedule taskSchedule;
	private PlannerWindow plannerWindow;
	private MainDesktopPane desktop;
	private BalloonToolTip balloonToolTip;
	

	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelSchedule(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelSchedule.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelSchedule.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		this.desktop = desktop;
		isRealTimeUpdate = true;
		
		// Prepare combo box
        if (unit instanceof Person) {
         	person = (Person) unit;
         	taskSchedule = person.getTaskSchedule();
        }
        else if (unit instanceof Robot) {
        	robot = (Robot) unit;
        	taskSchedule = robot.getTaskSchedule();
        }

        schedules = taskSchedule.getSchedules();

        balloonToolTip = new BalloonToolTip();
        
		// Create label panel.
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(labelPanel);

		// Prepare label
		JLabel label = new JLabel(Msg.getString("TabPanelSchedule.label"), JLabel.CENTER); //$NON-NLS-1$
		labelPanel.add(label);

		// Prepare info panel.
//		JPanel infoPanel = new JPanel(new GridLayout(1, 3, 40, 0)); //new FlowLayout(FlowLayout.CENTER));
//		infoPanel.setBorder(new MarsPanelBorder());

       	// Create the button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(buttonPane);//, BorderLayout.NORTH);

        if (unit instanceof Person) {
       	
    		shiftType = taskSchedule.getShiftType();   		
    		shiftCache = shiftType;
    		shiftLabel = new JLabel(Msg.getString("TabPanelSchedule.shift.label"), JLabel.CENTER); //$NON-NLS-1$
    
    		balloonToolTip.createBalloonTip(shiftLabel, Msg.getString("TabPanelSchedule.shift.toolTip")); //$NON-NLS-1$
    		buttonPane.add(shiftLabel);
    		
    		fillColorCache = shiftLabel.getBackground();

    		shiftTF = new JTextField(shiftCache);
    		start = taskSchedule.getShiftStart();
    		end = taskSchedule.getShiftEnd();
    		shiftTF.setEditable(false);
    		shiftTF.setColumns(2);

    		balloonToolTip.createBalloonTip(shiftTF, Msg.getString("TabPanelSchedule.shiftTF.toolTip", shiftCache, start, end)); //$NON-NLS-1$
    		shiftTF.setHorizontalAlignment(JTextField.CENTER);
    		buttonPane.add(shiftTF);
    		//buttonPane.add(new JLabel("           "));
/*
    		// Create the future task planner button
    		JButton button = new JButton("Open Planner");
    		button.setToolTipText("Click to Open Personal Planner");
    		button.addActionListener(
    			new ActionListener() {
    				public void actionPerformed(ActionEvent e) {
    					// Open storm tracking window.
    					openPlannerWindow();
    				}
    			});
    		buttonPane.add(button);
*/
        }

		Box box = Box.createHorizontalBox();
		box.setBorder(new MarsPanelBorder());

//		centerContentPanel.add(infoPanel, BorderLayout.NORTH);
		centerContentPanel.add(box, BorderLayout.NORTH);

		// Create hideRepeatedTaskBox.
		hideBox = new JCheckBox(Msg.getString("TabPanelSchedule.checkbox.showRepeatedTask")); //$NON-NLS-1$
		//hideRepeatedTasksCheckBox.setHorizontalTextPosition(SwingConstants.RIGHT);
		hideBox.setFont(new Font("Serif", Font.PLAIN, 12));
		//hideRepeatedTasksCheckBox.setToolTipText(Msg.getString("TabPanelSchedule.tooltip.showRepeatedTask")); //$NON-NLS-1$
		balloonToolTip.createBalloonTip(hideBox, Msg.getString("TabPanelSchedule.tooltip.showRepeatedTask")); //$NON-NLS-1$);
		hideBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (hideBox.isSelected()) {
					hideRepeated = true;
				}
				else {
					hideRepeated = false;
				}

			}
		});
		hideBox.setSelected(hideRepeated);
//		infoPanel.add(hideRepeatedTasksCheckBox);
		box.add(hideBox);
		box.add(Box.createHorizontalGlue());

    	today = taskSchedule.getSolCache();
    	todayInteger = (Integer) today ;
    	solList = new CopyOnWriteArrayList<Object>();

		int size = schedules.size();
		for (int i = 0 ; i < size + 1; i++ )
			// size + 1 is needed to add today into solList
			solList.add(i + 1);

    	// Create comboBoxModel
    	Collections.sort(solList, Collections.reverseOrder());
    	comboBoxModel = new DefaultComboBoxModel<Object>();
    	// Using internal iterator in lambda expression
		solList.forEach(s -> comboBoxModel.addElement(s));

		// Create comboBox
		comboBox = new JComboBoxMW<Object>(comboBoxModel);
		comboBox.setSelectedItem(todayInteger);
		//comboBox.setOpaque(false);
		comboBox.setRenderer(new PromptComboBoxRenderer());
		//comboBox.setRenderer(new PromptComboBoxRenderer(" List of Schedules "));
		//comboBox.setBackground(new Color(0,0,0,128));
		//comboBox.setBackground(new Color(255,229,204));
		//comboBox.setForeground(Color.orange);
		comboBox.setMaximumRowCount(7);
		//comboBox.setBorder(null);

		JPanel solPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		solPanel.add(comboBox);

//		infoPanel.add(solPanel);
		box.add(solPanel);
		box.add(Box.createHorizontalGlue());

    	selectedSol = (Integer) comboBox.getSelectedItem();
		if (selectedSol == null)
			comboBox.setSelectedItem(todayInteger);

		comboBox.setSelectedItem((Integer)1);
		comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	selectedSol = (Integer) comboBox.getSelectedItem();
            	if ( selectedSol != null ) // e.g. when first loading up
            		scheduleTableModel.update(hideRepeated, (int) selectedSol);
            	if (selectedSol == todayInteger)
            		// Binds comboBox with realTimeUpdateCheckBox
            		realTimeBox.setSelected(true);
            }
		});

		// Create realTimeUpdateCheckBox.
		realTimeBox = new JCheckBox(Msg.getString("TabPanelSchedule.checkbox.realTimeUpdate")); //$NON-NLS-1$
		realTimeBox.setSelected(true);
		realTimeBox.setHorizontalTextPosition(SwingConstants.RIGHT);
		realTimeBox.setFont(new Font("Serif", Font.PLAIN, 12));
		//realTimeUpdateCheckBox.setToolTipText(Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate")); //$NON-NLS-1$
		balloonToolTip.createBalloonTip(realTimeBox, Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate"));
		realTimeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (realTimeBox.isSelected()){
					isRealTimeUpdate = true;
					scheduleTableModel.update(hideRepeated, today);
					comboBox.setSelectedItem(todayInteger);
				}
				else
					isRealTimeUpdate = false;
			}
		});
		box.add(realTimeBox);

		// Create schedule table model
		if (unit instanceof Person)
			scheduleTableModel = new ScheduleTableModel((Person) unit);
		else if (unit instanceof Robot)
			scheduleTableModel = new ScheduleTableModel((Robot) unit);

		// Create attribute scroll panel
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(scrollPanel);

		// Create schedule table
		table = new ZebraJTable(scheduleTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(25);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.setCellSelectionEnabled(false);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());

		// 2015-09-24 Align the content to the center of the cell
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(renderer);
		
	    SwingUtilities.invokeLater(() -> ColumnResizer.adjustColumnPreferredWidths(table));

	    scrollPanel.setViewportView(table);

		// 2015-06-08 Added sorting
		//table.setAutoCreateRowSorter(true);
		//table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());

		// 2015-06-08 Added setTableStyle()
		//TableStyle.setTableStyle(table);

		update();
	}

	/**
	 * Updates the info on this panel.
	 */
	public void update() {
			
		//TableStyle.setTableStyle(table);

		if (person != null) {
			shiftType = person.getTaskSchedule().getShiftType();

			//if (shiftCache != null)
			if (!shiftCache.equals(shiftType)) {
				shiftCache = shiftType;
				shiftTF.setText(shiftCache);
			}
/*
			//System.out.println("fillColorCache is "+ fillColorCache);
			if (fillColorCache != shiftTF.getBackground()) {
				fillColorCache = shiftTF.getBackground();
			//	System.out.println("Set fillColorCache to "+ fillColorCache);
	    		balloonToolTip.createBalloonTip(shiftLabel, Msg.getString("TabPanelSchedule.shift.toolTip")); //$NON-NLS-1$
	      		balloonToolTip.createBalloonTip(shiftTF, Msg.getString("TabPanelSchedule.shiftTF.toolTip", shiftCache, start, end));  //$NON-NLS-1$    	  
	    		balloonToolTip.createBalloonTip(hideBox, Msg.getString("TabPanelSchedule.tooltip.showRepeatedTask")); //$NON-NLS-1$);
	    		balloonToolTip.createBalloonTip(realTimeBox, Msg.getString("TabPanelSchedule.tooltip.realTimeUpdate")); //$NON-NLS-1$
	    		//SwingUtilities.updateComponentTreeUI(desktop);
				desktop.updateToolWindowLF();
			}
*/			
		}

    	today = taskSchedule.getSolCache();
    	todayInteger = (Integer) today ;
       	selectedSol = (Integer) comboBox.getSelectedItem(); // necessary or else if (isRealTimeUpdate) below will have NullPointerException

       	// Update the sol box at the beginning of a new sol
    	if (today != todayCache) {
    		int size = schedules.size();
			solList.clear();
    		for (int i = 0 ; i < size + 1; i++ ) {
        		// size + 1 is needed for starting on sol 1
    			solList.add(i + 1);
    		}

	    	Collections.sort(solList, Collections.reverseOrder());
	    	DefaultComboBoxModel<Object> newComboBoxModel = new DefaultComboBoxModel<Object>();
			solList.forEach(s -> newComboBoxModel.addElement(s));

			// Update the solList comboBox
			comboBox.setModel(newComboBoxModel);
			comboBox.setRenderer(new PromptComboBoxRenderer());

			// Note: Below is needed or else users will be constantly interrupted
			// as soon as the combobox got updated with the new day's schedule
			// and will be swapped out without warning.
			if (selectedSol != null)
				comboBox.setSelectedItem(selectedSol);
			else {
				comboBox.setSelectedItem(todayInteger);
				selectedSol = null;
			}

			todayCache = today;
    	}

       	// Turn off the Real Time Update if the user is still looking at a previous sol's schedule
       	if (selectedSol != todayInteger) {
       		isRealTimeUpdate = false;
       		realTimeBox.setSelected(false);
       	}

		// Detects if the Hide Repeated box has changed. If yes, call for update
		if (hideRepeatedCache != hideRepeated) {
			hideRepeatedCache = hideRepeated;
			scheduleTableModel.update(hideRepeated, selectedSol);
		}

		if (isRealTimeUpdate)
			scheduleTableModel.update(hideRepeated, todayInteger);
	}

    public void setViewer(PlannerWindow w) {
    	this.plannerWindow = w;
    }

	/**
	 * Opens PlannerWindow
	 */
    // 2015-05-21 Added openPlannerWindow()
	private void openPlannerWindow() {

		MainWindow mw = desktop.getMainWindow();
		if (mw !=null )  {
			// Pause simulation
			//mw.pauseSimulation();
			// Create PlannerWindow
			if (plannerWindow == null)
				plannerWindow = new PlannerWindow(unit, desktop, this);
			// Unpause simulation
			//mw.unpauseSimulation();
		}

		MainScene ms = desktop.getMainScene();
		if (ms !=null )  {
			// Pause simulation
			//ms.pauseSimulation();
			// Create PlannerWindow
			if (plannerWindow == null) {
				plannerWindow = new PlannerWindow(unit, desktop, this);
			}
			// Unpause simulation
			//ms.unpauseSimulation();
		}

	}

	class PromptComboBoxRenderer extends BasicComboBoxRenderer {

		private static final long serialVersionUID = 1L;
		private String prompt;
		//public boolean isOptimizedDrawingEnabled();
		//private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		public PromptComboBoxRenderer(){
			//defaultRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
		    //settlementListBox.setRenderer(defaultRenderer);
		    //setOpaque(false);
		    setHorizontalAlignment(CENTER);
		    setVerticalAlignment(CENTER);
		}

		public PromptComboBoxRenderer(String prompt){
				this.prompt = prompt;
			}

			@Override
		    public Component getListCellRendererComponent(JList list, Object value,
		            int index, boolean isSelected, boolean cellHasFocus) {
		        JComponent result = (JComponent)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				if (value == null) {
					setText( prompt );
					//this.setForeground(Color.orange);
			        //this.setBackground(new Color(184,134,11));
					return this;
				}

				setText(" Sol " + value);

				if (isSelected) {
					result.setForeground(new Color(184,134,11));
			        result.setBackground(Color.orange);

		          // unselected, and not the DnD drop location
		        } else {
		        	  result.setForeground(new Color(184,134,11));
		        	  result.setBackground(new Color(255,229,204)); //pale yellow (255,229,204)
				      //Color(184,134,11)) brown
		        }

		        //result.setOpaque(false);

		        return result;
		    }
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private class ScheduleTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		//private TaskSchedule taskSchedule;
		//private List<OneTask> tasks;

		DecimalFormat fmt = new DecimalFormat("0000");

		/**
		 * hidden constructor.
		 * @param person {@link Person}
		 */
		private ScheduleTableModel(Unit unit) {
/*
	        Person person = null;
	        Robot robot = null;
	        if (unit instanceof Person) {
	         	person = (Person) unit;
	         	taskSchedule = person.getTaskSchedule();
	        }
	        else if (unit instanceof Robot) {
	        	robot = (Robot) unit;
	        	taskSchedule = robot.getTaskSchedule();
	        }

	        tasks = taskSchedule.getTodaySchedule();
*/

			//tasks = new ArrayList<>(taskSchedule.getTodaySchedule());
		}

		@Override
		public int getRowCount() {
			if (tasks != null)
				return tasks.size();
			else
				return 0;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = String.class;
			if (columnIndex == 2) dataType = String.class;
			if (columnIndex == 3) dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSchedule.column.time"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSchedule.column.description"); //$NON-NLS-1$
			else if (columnIndex == 2) return Msg.getString("TabPanelSchedule.column.phase"); //$NON-NLS-1$
			else if (columnIndex == 3) return Msg.getString("TabPanelSchedule.column.taskName"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) return fmt.format(tasks.get(row).getStartTime());
			else if (column == 1) return tasks.get(row).getDescription();
			else if (column == 2) return tasks.get(row).getPhase();
			else if (column == 3) return tasks.get(row).getTaskName();
			else return null;
		}

		/**
		 * Prepares a list of activities done on the selected day
		 * @param hideRepeatedTasks
		 * @param selectedSol
		 */
		public void update(boolean hideRepeatedTasks, int selectedSol) {
	        int todaySol = taskSchedule.getSolCache();

	        // Load previous day's schedule if selected
			if (todaySol == selectedSol) {
				// Load today's schedule
				tasks = new CopyOnWriteArrayList<OneTask>(taskSchedule.getTodaySchedule());
			}
			else {
				tasks = new CopyOnWriteArrayList<OneTask>(schedules.get(selectedSol));
			}

			// check if user selected hide repeated tasks checkbox
			if (tasks != null && hideRepeatedTasks) {
				// show only non-repeating consecutive tasks
				List<OneTask> displaySchedule = new CopyOnWriteArrayList<OneTask>(tasks);
				
				int size = displaySchedule.size();
				
				for (int i = size - 1; i >= 0; i--) {
					OneTask currentTask = displaySchedule.get(i);
					String currentName = currentTask.getTaskName();
					String currentDes = currentTask.getDescription();
					String currentPhase = currentTask.getPhase();					
					OneTask lastTask = null;
		        	String lastName = null;
		        	String lastDes = null;
		        	String lastPhase = null;
		        	
		        	// make sure this is NOT the very first task (i = 0) of the day
		        	if (i != 0) {
		        		lastTask = displaySchedule.get(i - 1);
		        		lastName = lastTask.getTaskName();
		        		lastDes = lastTask.getDescription();
		        		lastPhase = lastTask.getPhase();
		        		
		        		// check if the last task is the same as the current task
			        	if (lastDes.equals(currentDes)) {
			        		//if (lastName.equals(currentName)) {	
				        		//if (lastPhase.equals(currentPhase)) {	
				        			// remove the current task if they are the same
				        			displaySchedule.remove(i);
				        		//}
			        		//}
			        	}
		        	}        		
				}
				
		        tasks = displaySchedule;
			}

        	fireTableDataChanged();

		}

	}

}