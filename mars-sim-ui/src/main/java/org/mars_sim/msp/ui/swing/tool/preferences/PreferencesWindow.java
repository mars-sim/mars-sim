/**
 * Mars Simulation Project
 * PreferencesWindow.java
 * @version 3.07 2014-11-11
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.preferences;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.JSliderMW;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;

/**
 * The PreferencesWindow is a tool window that allows the user to adjust general
 * aspects of the simulation and interface.
 */
public class PreferencesWindow
extends ToolWindow
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Tool name. */
	public static final String NAME = Msg.getString("PreferencesWindow.title"); //$NON-NLS-1$

	// Data members
	private AudioPlayer soundPlayer;
	private JSliderMW volumeSlider;

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

		// Create audio pane.
		JPanel audioPane = new JPanel(new BorderLayout());
		audioPane.setBorder(new MarsPanelBorder());
		mainPane.add(audioPane, BorderLayout.NORTH);

		// Create volume label.
		JLabel volumeLabel = new JLabel(Msg.getString("PreferencesWindow.volume"), JLabel.CENTER); //$NON-NLS-1$
		audioPane.add(volumeLabel, BorderLayout.NORTH);

		// Create volume slider.
		float volume = soundPlayer.getVolume();
		int intVolume = Math.round(volume * 10F);
		volumeSlider = new JSliderMW(JSlider.HORIZONTAL, 0, 10, intVolume);
		volumeSlider.setMajorTickSpacing(1);
		volumeSlider.setPaintTicks(true);
		volumeSlider.setToolTipText(Msg.getString("PreferencesWindow.tooltip.volume")); //$NON-NLS-1$
		volumeSlider.setEnabled(!soundPlayer.isMute());
		volumeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				float newVolume = (float) volumeSlider.getValue() / 10F;
				soundPlayer.setVolume(newVolume);
			}
		});
		audioPane.add(volumeSlider, BorderLayout.SOUTH);

		// Create UI panel.
		JPanel uiPane = new JPanel(new BorderLayout());
		uiPane.setBorder(new MarsPanelBorder());
		mainPane.add(uiPane, BorderLayout.SOUTH);

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

	@Override
	public void actionPerformed(ActionEvent e) {
volumeSlider.setEnabled(!soundPlayer.isMute());
		}
	}
