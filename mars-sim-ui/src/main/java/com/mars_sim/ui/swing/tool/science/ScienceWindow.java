/**
 * Mars Simulation Project
 * ScienceWindow.java
 * @version 3.2.0 2021-06-20
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.tool.science;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.science.ScientificStudyManager;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.tool_window.ToolWindow;

/**
 * Window for the science tool.
 */
public class ScienceWindow
extends ToolWindow {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = "science";
	public static final String ICON = "science";
    public static final String TITLE = Msg.getString("ScienceWindow.title");

	// Data members
	private AbstractStudyListPanel ongoingStudyListPane;
	private AbstractStudyListPanel finishedStudyListPane;
	private StudyDetailPanel studyDetailPane;
	private ScientificStudy selectedStudy;

	/**
	 * Constructor
	 * @param desktop the main desktop panel.
	 */
	@SuppressWarnings("serial")
	public ScienceWindow(MainDesktopPane desktop) {

		// Use ToolWindow constructor
		super(NAME, TITLE, desktop);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);//.HIDE_ON_CLOSE);

		selectedStudy = null;

		// Create content panel.
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(StyleManager.newEmptyBorder());
		setContentPane(mainPane);

		// Create lists panel.
		JPanel listsPane = new JPanel(new GridLayout(2, 1, 0, 0));
		mainPane.add(listsPane, BorderLayout.WEST);

		ScientificStudyManager mgr = desktop.getSimulation().getScientificStudyManager();
		
		// Create ongoing study list panel.
		ongoingStudyListPane = new AbstractStudyListPanel(this, "OngoingStudyListPanel") {		
			@Override
			protected List<ScientificStudy> getStudies() {
				return mgr.getAllStudies(false);
			}
		};
		listsPane.add(ongoingStudyListPane);

		// Create finished study list panel.
		finishedStudyListPane = new AbstractStudyListPanel(this, "FinishedStudyListPanel") {		
			@Override
			protected List<ScientificStudy> getStudies() {
				return mgr.getAllStudies(true);
			}
		};
		listsPane.add(finishedStudyListPane);

		// Create study detail panel.
		studyDetailPane = new StudyDetailPanel(this);
		mainPane.add(studyDetailPane, BorderLayout.CENTER);

		setMinimumSize(new Dimension(480, 480));
		setMaximizable(true);
		setResizable(false);
		setVisible(true);
		
		// Pack window.
		pack();
	}

	/**
	 * Sets the scientific study to display in the science window.
	 * @param study the scientific study to display.
	 */
	public void setScientificStudy(ScientificStudy study) {
		selectedStudy = study;
		if (studyDetailPane.displayScientificStudy(study)) {
			ongoingStudyListPane.selectScientificStudy(study, true);
			finishedStudyListPane.selectScientificStudy(study, true);
		}
	}

	/**
	 * Gets the displayed scientific study.
	 * @return study or null if none displayed.
	 */
	public ScientificStudy getScientificStudy() {
		return selectedStudy;
	}

	/**
	 * Update the window.
	 * @param pulse Unused clock pulse; window is time independent
	 */
	@Override
	public void update(ClockPulse pulse) {
		// Update all of the panels.
		ongoingStudyListPane.update();
		finishedStudyListPane.update();
		studyDetailPane.update();
	}
}
