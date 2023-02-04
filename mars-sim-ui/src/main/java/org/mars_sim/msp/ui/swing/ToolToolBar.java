/*
 * Mars Simulation Project
 * ToolToolBar.java
 * @date 2022-07-19
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
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

	public static final String WIKI_URL = Msg.getString("ToolToolBar.calendar.url"); //$NON-NLS-1$
	public static final String WIKI_TEXT = Msg.getString("ToolToolBar.calendar.title"); //$NON-NLS-1$

	private static final String SAVE = "SAVE";
	private static final String SAVEAS = "SAVEAS";
	private static final String EXIT = "EXIT";
	private static final String STARMAP = "STARMAP";
	private static final String MARSCAL = "MARS-CAL";

	/** Main window that contains this toolbar. */
	private MainWindow parentMainWindow;

	private MarsCalendarDisplay calendarDisplay; 
	
	private JLabel monthLabel;

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
		masterClock = parentMainWindow.getSimulation().getMasterClock();

		// Set name
		setName(Msg.getString("ToolToolBar.toolbar")); //$NON-NLS-1$
		// Fix tool bar
		setFloatable(false);

		// Prepare tool buttons
		prepareToolButtons();
		setMaximumSize(new Dimension(0, 32));
		incrementClocks(masterClock, true);
	}

	/** Prepares tool buttons */
	private void prepareToolButtons() {

		addToolButton(SAVE, Msg.getString("mainMenu.save"), "icon.save"); //$NON-NLS-1$ //$NON-NLS-2$
		addToolButton(SAVEAS, Msg.getString("mainMenu.saveAs"), "icon.saveAs"); //$NON-NLS-
		addToolButton(EXIT, Msg.getString("mainMenu.exit"), "icon.exit"); //$NON-NLS-

		addSeparator(new Dimension(20, 20));

		// Add Tools buttons
		addToolButton(NavigatorWindow.NAME, null, "icon.mars"); //$NON-NLS-
		addToolButton(SearchWindow.NAME, null, "icon.find"); //$NON-NLS-1$
		addToolButton(TimeWindow.NAME, null, "icon.time"); //$NON-NLS-1$
		addToolButton(MonitorWindow.TITLE, null, "icon.monitor"); //$NON-NLS-1$
		addToolButton(MissionWindow.NAME, null, "icon.mission"); //$NON-NLS-1$
		addToolButton(SettlementWindow.NAME, null, "icon.map"); //$NON-NLS-1$
		addToolButton(ScienceWindow.NAME, null, "icon.science"); //$NON-NLS-1$
		addToolButton(ResupplyWindow.NAME, null, "icon.resupply"); //$NON-NLS-1$
		addToolButton(CommanderWindow.NAME, null, "icon.dashboard"); //$NON-NLS-1$

		// Everythong after this is on teh roght hand side
		add(Box.createHorizontalGlue()); 

		earthDate = createTextLabel("Greenwich Mean Time (GMT) for Earth");
		add(earthDate);
		addSeparator();
		missionSol = createTextLabel("Simulation Sol Count");
		add(missionSol);
		addSeparator();
		marsTime = createTextLabel("Universal Mean Time (UMT) for Mars. Format: 'Orbit-Month-Sol:Millisols Weekday'");
		add(marsTime);
		addSeparator();

		calendarPane = setupCalendarPanel(masterClock.getMarsClock());	
		addToolButton(MARSCAL, "Open the Mars Calendar", "icon.schedule");

		addToolButton(STARMAP, "Open the Orbit Viewer", "icon.telescope");
		addSeparator(new Dimension(20, 20));

		// Add guide button
		addToolButton(GuideWindow.NAME, "View the Help tool", "img.guide"); //$NON-NLS-1$
	}

	private void addToolButton(String toolName, String tooltip, String iconKey) {
		JButton toolButton = new JButton(ImageLoader.getIcon(Msg.getString(iconKey)));
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

	private JPanel setupCalendarPanel(MarsClock marsClock) {
		JPanel innerPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

		calendarDisplay = new MarsCalendarDisplay(marsClock, parentMainWindow.getDesktop());
		innerPane.add(calendarDisplay);

		final JPanel midPane = new JPanel(new BorderLayout(0, 0));

		midPane.add(innerPane, BorderLayout.CENTER);
		midPane.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.ORANGE, new Color(210,105,30)));

		final JPanel outerPane = new JPanel(new BorderLayout(10, 10));
		outerPane.add(midPane, BorderLayout.CENTER);

		// Create martian month label
    	String mn = "Month of " + marsClock.getMonthName();
    	monthLabel = new JLabel(mn, SwingConstants.CENTER);
		JPanel monthPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		monthPane.add(monthLabel);
		midPane.add(monthPane, BorderLayout.NORTH);

		JButton link = new JButton(WIKI_TEXT);
		link.setAlignmentX(.5f);
		link.setToolTipText("Open the Timekeeping wiki in mars-sim GitHub site");
		link.addActionListener(e -> SwingHelper.openBrowser(WIKI_URL));

		JPanel linkPane = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
		linkPane.add(link);
		outerPane.add(linkPane, BorderLayout.SOUTH);

    	JLabel headerLabel = new JLabel("Mars Calendar", SwingConstants.CENTER);
    	outerPane.add(headerLabel, BorderLayout.NORTH);
    	
    	return outerPane;
	}

	/**
	 * Increment the label of both the earth and mars clocks
	 */
	public void incrementClocks(MasterClock master, boolean newSol) {
		MarsClock marsClock = master.getMarsClock();
		if (newSol) {
			missionSol.setText("Sol : " + marsClock.getMissionSol());
		}

		EarthClock earthClock = master.getEarthClock();
		earthDate.setText(master.getEarthClock().getCurrentDateTimeString(earthClock));
		marsTime.setText(marsClock.getDisplayTruncatedTimeStamp());
	}

	/** 
	 * ActionListener method overridden 
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
			
			case STARMAP:
				parentMainWindow.openOrbitViewer();
				break;

			case MARSCAL:
				MarsClock mc = masterClock.getMarsClock();
				calendarDisplay.update(mc);
		
				String mn = "Month of " + mc.getMonthName();
				monthLabel.setText(mn);

				JDialog popOver = SwingHelper.createPoupWindow(calendarPane, -1, -1, -110, 10);
				popOver.setVisible(true);
				break;
			default:
				parentMainWindow.getDesktop().openToolWindow(event.getActionCommand());
				break;
		}
	}
}