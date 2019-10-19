/**
 * Mars Simulation Project
 * PlannerWindow.java
 * @version 3.1.0 2017-03-19
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule;
import org.mars_sim.msp.core.person.ai.task.utils.TaskSchedule.OneActivity;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.TableStyle;



/**
 * The window for planning future activities.
 */
@SuppressWarnings("serial")
public class PlannerWindow
extends JInternalFrame
implements InternalFrameListener, ActionListener {

	// Data members
	private JTextField orbit, month, sol, millisols;
	private JButton add;
	private JTable table;

	private Unit unit;
	//private JComboBoxMW<Object> comboBox;
	//private DefaultComboBoxModel<Object> comboBoxModel;
	private PlannerTableModel PlannerTableModel;

	//private Person person;
	//private Robot robot;
	private JPanel topPanel, panel;
	private TabPanelSchedule tabPanelSchedule;

	public PlannerWindow(Unit unit, MainDesktopPane desktop, TabPanelSchedule tabPanelSchedule) {
		// Use JInternalFrame constructor
        super("Personal Planner", false, true, false, false);

        this.unit = unit;
        this.tabPanelSchedule = tabPanelSchedule;

		// Create info panel.
		//infoPane = new JPanel(new CardLayout());
		//infoPane.setBorder(new MarsPanelBorder());
		//add(infoPane, BorderLayout.NORTH);

		addInternalFrameListener(this);

        topPanel = new JPanel(new BorderLayout());

        add(topPanel);

		init();

		createTable();

		pack();

		desktop.add(this);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setSize(new Dimension(512, 512));

		Dimension desktopSize = desktop.getParent().getSize();
	    Dimension jInternalFrameSize = this.getSize();
	    int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    setLocation(width, height);

	    setVisible(true);

	}

	public void init() {

		panel = new JPanel(new GridBagLayout());
		//panel.setLayout(new GridBagLayout());

		addItem(panel, new JLabel("Orbit : "), 0, 0, 1, 1, GridBagConstraints.EAST);
		addItem(panel, new JLabel("Month : "), 0, 1, 1, 1, GridBagConstraints.EAST);
		addItem(panel, new JLabel("Sol : "), 0, 2, 1, 1, GridBagConstraints.EAST);
		addItem(panel, new JLabel("Millisols : "), 0, 3, 1, 1, GridBagConstraints.EAST);

		orbit = new JTextField(5);
		month = new JTextField(5);
		sol = new JTextField(5);
		millisols = new JTextField(5);

		addItem(panel, orbit, 1, 0, 1, 1, GridBagConstraints.WEST);
		addItem(panel, month, 1, 1, 1, 1, GridBagConstraints.WEST);
		addItem(panel, sol, 1, 2, 1, 1, GridBagConstraints.WEST);
		addItem(panel, millisols, 1, 3, 1, 1, GridBagConstraints.WEST);

		topPanel.add(panel, BorderLayout.NORTH);

	}


	public void createTable() {

		// Create schedule table model
		if (unit instanceof Person)
			PlannerTableModel = new PlannerTableModel((Person) unit);
		else if (unit instanceof Robot)
			PlannerTableModel = new PlannerTableModel((Robot) unit);

		// Create attribute scroll panel
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setBorder(new MarsPanelBorder());

		// Create schedule table
		table = new JTable(PlannerTableModel);
		table.setPreferredScrollableViewportSize(new Dimension(225, 100));
		table.getColumnModel().getColumn(0).setPreferredWidth(25);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.setCellSelectionEnabled(false);
		// table.setDefaultRenderer(Integer.class, new NumberCellRenderer());
		scrollPanel.setViewportView(table);

		// 2015-06-08 Added sorting
		table.setAutoCreateRowSorter(true);
	    //if (!MainScene.OS.equals("linux")) {
	    //	table.getTableHeader().setDefaultRenderer(new MultisortTableHeaderCellRenderer());
	    //}
		topPanel.add(scrollPanel, BorderLayout.CENTER);

		// 2015-06-08 Added setTableStyle()
		TableStyle.setTableStyle(table);

		update();
	}

	public void addItem(JPanel p, JComponent c, int x, int y, int w, int h, int align) {

		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = w;
		gc.gridheight = h;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		gc.insets = new Insets(2,2,2,2);
		gc.anchor = align;
		gc.fill = GridBagConstraints.NONE;
		p.add(c,gc);

	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		//if (source == prevButton) buttonClickedPrev();
		//else if (source == nextButton) buttonClickedNext();
		//else if (source == finalButton) buttonClickedFinal();
	}


	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub
//		tabPanelSchedule.setViewer(null);
		//System.out.println("internalFrameClosing()");
	}


	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
//		tabPanelSchedule.setViewer(null);
		//System.out.println("internalFrameClosed()");
	}


	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	/**
	 * Updates the info on this panel.
	 */
	public void update() {
		TableStyle.setTableStyle(table);
		// Update if necessary.
	}

	class PromptComboBoxRenderer extends DefaultListCellRenderer {

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
		    public Component getListCellRendererComponent(JList<?> list, Object value,
		            int index, boolean isSelected, boolean cellHasFocus) {
//		        JComponent result = (JComponent)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        //Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
		        //component.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
				
		        if (value == null) {
					setText( prompt );
					//this.setForeground(Color.orange);
			        //this.setBackground(new Color(184,134,11));
					return this;
				}

				setText(" Sol " + value);

				if (isSelected) {
					c.setForeground(new Color(184,134,11));
			        c.setBackground(Color.orange);

		          // unselected, and not the DnD drop location
		        } else {
		        	  c.setForeground(new Color(184,134,11));
		        	  c.setBackground(new Color(255,229,204)); //pale yellow (255,229,204)
				      //Color(184,134,11)) brown
		        }

		        //result.setOpaque(false);

		        return c;
		    }
	}

	/**
	 * Internal class used as model for the attribute table.
	 */
	private static class PlannerTableModel
	extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;

		private TaskSchedule taskSchedule;
		//private List<OneTask> tasks;
		private List<OneActivity> activities;

		DecimalFormat fmt = new DecimalFormat("0000");

		/**
		 * hidden constructor.
		 * @param person {@link Person}
		 */
		private PlannerTableModel(Unit unit) {
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

	        //tasks = taskSchedule.getTodaySchedule();
	        activities = taskSchedule.getTodayActivities();

		}

		@Override
		public int getRowCount() {
			if (activities != null)
				return activities.size();
			else
				return 0;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			Class<?> dataType = super.getColumnClass(columnIndex);
			if (columnIndex == 0) dataType = String.class;
			if (columnIndex == 1) dataType = String.class;
			//if (columnIndex == 2) dataType = String.class;
			return dataType;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return Msg.getString("TabPanelSchedule.column.time"); //$NON-NLS-1$
			//else if (columnIndex == 1) return Msg.getString("TabPanelSchedule.column.task"); //$NON-NLS-1$
			else if (columnIndex == 1) return Msg.getString("TabPanelSchedule.column.activity"); //$NON-NLS-1$
			else return null;
		}

		@Override
		public Object getValueAt(int row, int column) {
/*
			if (column == 0) return fmt.format(tasks.get(row).getStartTime());
			//else if (column == 1) return tasks.get(row).getTaskName();
			else if (column == 1) return tasks.get(row).getDescription();
			else
*/
			return null;
		}

		/**
		 * Prepares a list of activities done on the selected day
		 * @param hideRepeatedTasks
		 * @param selectedSol
		 */
		public void update(boolean hideRepeatedTasks, int selectedSol) {
	        int sol = taskSchedule.getSolCache();
/*
	        // Load previous day's schedule if selected
			if (sol != selectedSol) {
				//Map <Integer, List<OneTask>> schedules = taskSchedule.getSchedules();
				//tasks = schedules.get(selectedSol);
				activities = taskSchedule.getAllActivities().get(selectedSol);
			}

			else {
				// Load today's schedule
				//tasks = taskSchedule.getTodaySchedule();
				activities = taskSchedule.getTodayActivities();
			}

			// check if user selected hide repeated tasks checkbox
			if (activities != null && hideRepeatedTasks) {
				// show only non-repeating consecutive tasks
				List<OneActivity> thisSchedule = new ArrayList<OneActivity>(activities);
		        int i = thisSchedule.size() - 1;
		        //for (int i = size - 1; i > 0; i--) {
		        while (i > 0 ) {

		        	OneTask currentTask = thisSchedule.get(i);
		        	OneTask lastTask = null;

		        	if ( i - 1 > -1 )
		        		lastTask = thisSchedule.get(i - 1);

		        	String lastActivity = lastTask.getDescription();
		        	String currentActivity = currentTask.getDescription();
		        	// check if the last task is the same as the current task
		        	if (lastActivity.equals(currentActivity)) {
		        		// remove the current task if it's the same as the last task
		        		thisSchedule.remove(i);
		        	}

		        	i--;
		        }

		        tasks = thisSchedule;
			}
*/
        	fireTableDataChanged();

		}

	}

}
