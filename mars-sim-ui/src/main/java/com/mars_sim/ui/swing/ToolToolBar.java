/*
 * Mars Simulation Project
 * ToolToolBar.java
 * @date 2025-10-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.time.MarsTimeFormat;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.astroarts.OrbitWindow;
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.resupply.ResupplyWindow;
import com.mars_sim.ui.swing.tool.science.ScienceWindow;
import com.mars_sim.ui.swing.tool.search.SearchWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;
import com.mars_sim.ui.swing.tool.time.MarsCalendarDisplay;
import com.mars_sim.ui.swing.tool.time.TimeWindow;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The ToolToolBar class is a UI toolbar for holding tool buttons. There should
 * only be one instance and it is contained in the {@link MainWindow} instance.
 */
public class ToolToolBar extends JToolBar implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(ToolToolBar.class.getName());
	
	private static final String SAVE = "SAVE";
	private static final String SAVEAS = "SAVEAS";
	private static final String EXIT = "EXIT";
	private static final String MARSCAL = "MARS-CAL";

	private static final DateTimeFormatter SHORT_TIMESTAMP_FORMATTER = 
			DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm");

	private static final String SOL = "Sol:";
	private static final String DISPLAY_HELP = "display-help";
	private static final String MAIN_WIKI = "main-wiki";
	
	private static final String WIKI_URL = Msg.getString("ToolToolBar.wiki.url"); //$NON-NLS-1$
	
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
	 * Constructs a ToolToolBar object.
	 * 
	 * @param parentMainWindow the main window pane
	 */
	public ToolToolBar(MainWindow parentMainWindow) {

		// Use JToolBar constructor
		super();

		// Initialize data members
		this.parentMainWindow = parentMainWindow;
		masterClock = parentMainWindow.getDesktop().getSimulation().getMasterClock();

		// Set name
		setName(Msg.getString("ToolToolBar.toolbar")); //$NON-NLS-1$

		setFloatable(true);

//		setRollover(true);
		
		setAlignmentY(SwingConstants.CENTER);
		
		addSeparator(new Dimension(20, 20));
		
		// Prepare tool buttons
		prepareToolButtons();

		createDatePanel();
	
		addSeparator(new Dimension(20, 20));
		
		calendarPane = setupCalendarPanel(masterClock.getMarsTime());	

		addToolButton(MARSCAL, "Mars Calendar", "schedule");
		
		addSeparator(new Dimension(20, 20));

		addToolButton(SAVE, Msg.getString("mainMenu.save"), "action/save"); //$NON-NLS-1$
		addToolButton(SAVEAS, Msg.getString("mainMenu.saveAs"), "action/saveAs"); //$NON-NLS-1$
		addToolButton(EXIT, Msg.getString("mainMenu.exit"), "action/exit"); //$NON-NLS-1$

		addSeparator(new Dimension(20, 20));

		// Add wiki button
		addToolButton(MAIN_WIKI, "Wiki", GuideWindow.wikiIcon);
		// Add guide button
		addToolButton(DISPLAY_HELP, "Help Tool", GuideWindow.guideIcon);
		
		addSeparator(new Dimension(20, 20));
		
		incrementClocks(masterClock);
	}

	/** 
	 * Prepares tool buttons.
	 */
	private void prepareToolButtons() {

		// Add Tools buttons
		addToolButton(NavigatorWindow.NAME, NavigatorWindow.ICON);
		addToolButton(SearchWindow.NAME, SearchWindow.ICON);
		addToolButton(TimeWindow.NAME, TimeWindow.ICON);
		addToolButton(MonitorWindow.NAME, MonitorWindow.ICON);
		addToolButton(MissionWindow.NAME, MissionWindow.ICON);
		addToolButton(SettlementWindow.NAME, SettlementWindow.ICON);
		addToolButton(ScienceWindow.NAME, ScienceWindow.ICON);
		addToolButton(ResupplyWindow.NAME, ResupplyWindow.ICON);
		addToolButton(CommanderWindow.NAME, CommanderWindow.ICON);

		addToolButton(OrbitWindow.NAME, "Orbit Viewer", OrbitWindow.ICON);
		
		addSeparator(new Dimension(20, 20));
	}
  
	
	/**
	 * Creates the date panel and add it to the tool tool bar.
	 */
	private void createDatePanel() {
		
		JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		marsTime = createTextLabel(12, Font.PLAIN, true, true, "Mars Coordinated Time (MCT) for Mars. Format: 'Orbit-Month-Sol:Millisols Weeksol'");
		timePanel.add(marsTime);		
	
		missionSol = createTextLabel(12, Font.BOLD, false, false, "Simulation Sol Count");
		timePanel.add(missionSol);
		
		earthDate = createTextLabel(12, Font.PLAIN, false, true, "Greenwich Mean Time (GMT) for Earth");
		timePanel.add(earthDate);
		
		add(timePanel);
	}
	
	/**
	 * Adds a tool bar button.
	 * 
	 * @param toolName
	 * @param iconKey
	 */
	private void addToolButton(String toolName, String iconKey) {
		addToolButton(toolName, null, ImageLoader.getIconByName(iconKey));
	}
	
	/**
	 * Adds a tool bar button.
	 * 
	 * @param toolName
	 * @param tooltip
	 * @param iconKey
	 */
	private void addToolButton(String toolName, String tooltip, String iconKey) {
		addToolButton(toolName, tooltip, ImageLoader.getIconByName(iconKey));
	}

	/**
	 * Adds a tool bar button.
	 * 
	 * @param toolName
	 * @param tooltip
	 * @param icon
	 */
	private void addToolButton(String toolName, String tooltip, Icon icon) {
		JButton toolButton = new JButton(icon);
		toolButton.setActionCommand(toolName);
		toolButton.setPreferredSize(new Dimension(25, 25));
		toolButton.setMaximumSize(new Dimension(25, 25));
		if (tooltip == null) {
			tooltip = Conversion.capitalize(toolName.replace("_", " "));
		}
		toolButton.setToolTipText(tooltip);
		toolButton.addActionListener(this);
		add(toolButton);
	}
	
	
	/**
	 * Creates text label.
	 * 
	 * @param size
	 * @param style
	 * @param tooltip
	 * @return
	 */
	private JLabel createTextLabel(int size, int style, boolean isFront, boolean haveBorder, String tooltip) {
		Icon icon = null;
		if (haveBorder) {
			icon = new Icon() {
				@Override public void paintIcon(Component c, Graphics g, int x, int y) {
					Graphics2D g2  = (Graphics2D)g.create();
			        Point2D start  = new Point2D.Float(0f, 0f);
			        Point2D end    = new Point2D.Float(99f, 0f);
			        float[] dist   = {0.0f, 0.5f, 1.0f};
			        Color[] colors = { Color.YELLOW, Color.ORANGE, Color.WHITE};
			        g2.setPaint(new LinearGradientPaint(start, end, dist, colors));
			        g2.fillRect(x, y, 100, 10);
			        g2.dispose();
				}
			      @Override public int getIconWidth()  { return 10; }
			      @Override public int getIconHeight() { return 10; }
			};
		}
		
		JLabel label = new JLabel();
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setAlignmentY(SwingConstants.CENTER);
		label.setFont(new Font(Font.SANS_SERIF, style, size));
		if (haveBorder) {
			if (isFront) {
				label.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 0, icon));
			}
			else {
				label.setBorder(BorderFactory.createMatteBorder(2, 0, 2, 2, icon));
			}
		}
		else {
			label.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		}
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
	 * 
	 * @param master
	 */
	public void incrementClocks(MasterClock master) {
		missionSol.setText(SOL + master.getMarsTime().getMissionSol());
		earthDate.setText(master.getEarthTime().format(SHORT_TIMESTAMP_FORMATTER));
		marsTime.setText(master.getMarsTime().getTruncatedDateTimeStamp());
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

				JDialog popOver = SwingHelper.createPopupWindow(calendarPane, -1, -1, -75, 20);
				popOver.setVisible(true);
				break;
			
			case DISPLAY_HELP:
				parentMainWindow.showHelp(null); // Default help page
				break;
				
			case MAIN_WIKI:
				SwingHelper.openBrowser(WIKI_URL);
				break;
				
			default:
				parentMainWindow.getDesktop().openToolWindow(event.getActionCommand());
				break;
		}
	}
}