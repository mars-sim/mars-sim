/*
 * Mars Simulation Project
 * ToolToolBar.java
 * @date 2023-04-02
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.mars.sim.tools.Msg;
import org.mars_sim.msp.core.time.MarsTime;
import org.mars_sim.msp.core.time.MarsTimeFormat;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.swing.tool.commander.CommanderWindow;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.MarsCalendarDisplay;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;
import org.mars_sim.msp.ui.swing.utils.SwingHelper;

/**
 * The ToolToolBar class is a UI toolbar for holding tool buttons. There should
 * only be one instance and it is contained in the {@link MainWindow} instance.
 */
public class ToolToolBar extends JToolBar implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final String SAVE = "SAVE";
	private static final String SAVEAS = "SAVEAS";
	private static final String EXIT = "EXIT";
	private static final String MARSCAL = "MARS-CAL";

	private static final DateTimeFormatter DATE_TIME_FORMATTER
//								= DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
								= DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
	
	/** Main window that contains this toolbar. */
	private MainWindow parentMainWindow;

	private MarsCalendarDisplay calendarDisplay; 
	
	private JLabel monthLabel;
	private JLabel weeksolLabel;

	private JLabel earthDate;
	private JLabel missionSol;
	private JLabel marsTime;

	private JPanel calendarPane;

	private MasterClock masterClock;

	/**
	 * Constructs a ToolToolBar object
	 * @param parentMainWindow the main window pane
	 */
	public ToolToolBar(MainWindow parentMainWindow) {

		// Use JToolBar constructor
		super();

		// WebLaf is breaking the default layout, remove this once totally gone.
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		// Initialize data members
		this.parentMainWindow = parentMainWindow;
		masterClock = parentMainWindow.getDesktop().getSimulation().getMasterClock();

		// Set name
		setName(Msg.getString("ToolToolBar.toolbar")); //$NON-NLS-1$
		// Fix tool bar
		setFloatable(false);

		// Prepare tool buttons
		prepareToolButtons();
		setMaximumSize(new Dimension(0, 32));
		incrementClocks(masterClock);
	}

	/** Prepares tool buttons */
	private void prepareToolButtons() {

		addToolButton(SAVE, Msg.getString("mainMenu.save"), "action/save"); //$NON-NLS-1$ //$NON-NLS-2$
		addToolButton(SAVEAS, Msg.getString("mainMenu.saveAs"), "action/saveAs"); //$NON-NLS-
		addToolButton(EXIT, Msg.getString("mainMenu.exit"), "action/exit"); //$NON-NLS-

		addSeparator(new Dimension(20, 20));

		// Add Tools buttons
		addToolButton(NavigatorWindow.NAME, null, NavigatorWindow.ICON); //$NON-NLS-
		addToolButton(SearchWindow.NAME, null, SearchWindow.ICON); //$NON-NLS-1$
		addToolButton(TimeWindow.NAME, null, TimeWindow.ICON); //$NON-NLS-1$
		addToolButton(MonitorWindow.NAME, null, MonitorWindow.ICON); //$NON-NLS-1$
		addToolButton(MissionWindow.NAME, null, MissionWindow.ICON); //$NON-NLS-1$
		addToolButton(SettlementWindow.NAME, null, SettlementWindow.ICON); //$NON-NLS-1$
		addToolButton(ScienceWindow.NAME, null, ScienceWindow.ICON); //$NON-NLS-1$
		addToolButton(ResupplyWindow.NAME, null, ResupplyWindow.ICON); //$NON-NLS-1$
		addToolButton(CommanderWindow.NAME, null, CommanderWindow.ICON); //$NON-NLS-1$

		// Everythong after this is on teh roght hand side
		add(Box.createHorizontalGlue()); 

		earthDate = createTextLabel("Greenwich Mean Time (GMT) for Earth");
		add(earthDate);
		addSeparator();
		missionSol = createTextLabel("Simulation Sol Count");
		add(missionSol);
		addSeparator();
		marsTime = createTextLabel("Universal Mean Time (UMT) for Mars. Format: 'Orbit-Month-Sol:Millisols Weeksol'");
		add(marsTime);
		addSeparator();

		calendarPane = setupCalendarPanel(masterClock.getMarsTime());	
		addToolButton(MARSCAL, "Open the Mars Calendar", "schedule");

		addToolButton(OrbitViewer.NAME, "Open the Orbit Viewer", OrbitViewer.ICON);
		addSeparator(new Dimension(20, 20));

		// Add guide button
		addToolButton(GuideWindow.NAME, "View the Help tool", GuideWindow.ICON); //$NON-NLS-1$
	}

	private void addToolButton(String toolName, String tooltip, String iconKey) {
		JButton toolButton = new JButton(ImageLoader.getIconByName(iconKey));
		toolButton.setActionCommand(toolName);
		toolButton.setMaximumSize(new Dimension(30, 30));
		if (tooltip == null) {
			tooltip = toolName;
		}
		toolButton.setToolTipText(tooltip);
		toolButton.addActionListener(this);
		add(toolButton);
	}

	
	private JLabel createTextLabel(String tooltip) {
		JLabel label = new JLabel();
		Border margin = new EmptyBorder(2,5,2,5);
		label.setBorder(new CompoundBorder(BorderFactory.createLoweredBevelBorder(), margin));
		label.setToolTipText(tooltip);
		return label;
	}

	private JPanel setupCalendarPanel(MarsTime marsClock) {
		JPanel innerPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

		calendarDisplay = new MarsCalendarDisplay(marsClock, parentMainWindow.getDesktop());
		innerPane.add(calendarDisplay);

		final JPanel midPane = new JPanel(new BorderLayout(2, 2));

		midPane.add(innerPane, BorderLayout.CENTER);
		midPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, new Color(210, 105, 30)));

		final JPanel outerPane = new JPanel(new BorderLayout(2, 2));
		outerPane.add(midPane, BorderLayout.CENTER);

		// Create Martian month label
    	String mn = "Month : " + marsClock.getMonthName();
    	monthLabel = new JLabel(mn, SwingConstants.CENTER);
    	
    	// Create Martian Weeksol label
    	String wd = "Weeksol : " + MarsTimeFormat.getSolOfWeekName(marsClock);
    	weeksolLabel = new JLabel(wd, SwingConstants.CENTER);

		JPanel monthPane = new JPanel(new GridLayout(2, 1));
		monthPane.add(weeksolLabel, SwingConstants.CENTER);
		monthPane.add(monthLabel, SwingConstants.CENTER);
		midPane.add(monthPane, BorderLayout.NORTH);
		
    	return outerPane;
	}

	/**
	 * Increments the label of both the earth and mars clocks.
	 */
	public void incrementClocks(MasterClock master) {
		MarsTime marsClock = master.getMarsTime();
		missionSol.setText("Sol : " + marsClock.getMissionSol());

		earthDate.setText(master.getEarthTime().format(DATE_TIME_FORMATTER));
		marsTime.setText(marsClock.getTruncatedDateTimeStamp());
	}

	/** 
	 * ActionListener method overridden.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {

		switch(event.getActionCommand()) {
			case SAVE:
				parentMainWindow.saveSimulation(true);
				break;

			case SAVEAS:
				parentMainWindow.saveSimulation(false);
				break;

			case EXIT:
				parentMainWindow.exitSimulation();
				break;
			
			case MARSCAL:
				MarsTime mc = masterClock.getMarsTime();
				calendarDisplay.update(mc);
		
				String mn = "Month : " + mc.getMonthName();
				monthLabel.setText(mn);
				
				String wd = "Weeksol : " + MarsTimeFormat.getSolOfWeekName(mc);
				weeksolLabel.setText(wd);

				JDialog popOver = SwingHelper.createPoupWindow(calendarPane, -1, -1, -75, 20);
				popOver.setVisible(true);
				break;
			default:
				parentMainWindow.getDesktop().openToolWindow(event.getActionCommand());
				break;
		}
	}
}