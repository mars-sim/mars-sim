/**
 * Mars Simulation Project
 * PreferencesWindow.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.tool.preferences;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * The PreferencesWindow is a tool window that allows the user to adjust general
 * aspects of the simulation and interface.
 */
public class PreferencesWindow extends ToolWindow {

	// Tool name
	public static final String NAME = "Preferences Tool";

	// Data members
	private AudioPlayer soundPlayer;
	private JCheckBox muteCheck;
	private JSliderMW volumeSlider;
	private JCheckBox toolToolBarCheck;
	private JCheckBox unitToolBarCheck;

	/**
	 * Constructor
	 * 
	 * @param desktop
	 */
	public PreferencesWindow(MainDesktopPane desktop) {
		// Use ToolWindow constructor
		super(NAME, desktop);

		// Set window resizable to false.
		setResizable(false);

		// Initialize data members.
		soundPlayer = desktop.getSoundPlayer();

		// Get content pane
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new MarsPanelBorder());
		setContentPane(mainPane);

		// Create audio panel.
		JPanel audioPane = new JPanel(new BorderLayout());
		audioPane.setBorder(new MarsPanelBorder());
		mainPane.add(audioPane, BorderLayout.NORTH);

		// Create audio label.
		JLabel volumeLabel = new JLabel("Volume", JLabel.CENTER);
		audioPane.add(volumeLabel, BorderLayout.NORTH);

		// Create volume slider.
		float volume = soundPlayer.getVolume();
		int intVolume = Math.round(volume * 10F);
		volumeSlider = new JSliderMW(JSlider.HORIZONTAL, 0, 10, intVolume);
		volumeSlider.setMajorTickSpacing(1);
		volumeSlider.setPaintTicks(true);
		volumeSlider.setToolTipText("Adjust the volume of sound output.");
		volumeSlider.setEnabled(!soundPlayer.isMute());
		volumeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float newVolume = (float) volumeSlider.getValue() / 10F;
				soundPlayer.setVolume(newVolume);
			}
		});
		audioPane.add(volumeSlider, BorderLayout.SOUTH);

		// Create mute checkbox.
		muteCheck = new JCheckBox("Mute", soundPlayer.isMute());
		muteCheck.setToolTipText("Mute all sound output.");
		muteCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				soundPlayer.setMute(muteCheck.isSelected());
				volumeSlider.setEnabled(!soundPlayer.isMute());
				;
			}
		});
		audioPane.add(muteCheck);

		// Create UI panel.
		JPanel uiPane = new JPanel(new BorderLayout());
		uiPane.setBorder(new MarsPanelBorder());
		mainPane.add(uiPane, BorderLayout.SOUTH);

		final MainWindow theMainwindow = desktop.getMainWindow();

		// Create Unit Toolbar Visibility checkbox.
		boolean unitToolBarVisible = theMainwindow.getUnitToolBar().isVisible();
		unitToolBarCheck = new JCheckBox("Show Unit Toolbar",
				unitToolBarVisible);
		unitToolBarCheck.setToolTipText("Show the Unit Bar at the bottom.");
		unitToolBarCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				theMainwindow.getUnitToolBar().setVisible(
						unitToolBarCheck.isSelected());
			}
		});

		uiPane.add(unitToolBarCheck, BorderLayout.CENTER);

		// Create Toolbar Visibility checkbox.
		boolean toolToolBarVisible = theMainwindow.getToolToolBar().isVisible();
		toolToolBarCheck = new JCheckBox("Show Toolbar", toolToolBarVisible);
		toolToolBarCheck.setToolTipText("Show the Tool Bar at the top.");
		toolToolBarCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				theMainwindow.getToolToolBar().setVisible(
						toolToolBarCheck.isSelected());
			}
		});

		uiPane.add(toolToolBarCheck, BorderLayout.SOUTH);

		// Pack window
		pack();
	}

	/**
	 * Prepare tool window for deletion.
	 */
	public void destroy() {
		soundPlayer.cleanAudioPlayer();
		soundPlayer = null;
	}
}