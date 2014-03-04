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

import org.mars_sim.msp.core.Msg;
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
	private JCheckBox checkMute;
	private JCheckBox checkToolToolBar;
	private JCheckBox checkUnitToolBar;

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

		// Create mute checkbox.
		checkMute = new JCheckBox(Msg.getString("PreferencesWindow.mute"), soundPlayer.isMute()); //$NON-NLS-1$
		checkMute.setToolTipText(Msg.getString("PreferencesWindow.tooltip.mute")); //$NON-NLS-1$
		checkMute.addActionListener(this);
		audioPane.add(checkMute);

		// Create UI panel.
		JPanel uiPane = new JPanel(new BorderLayout());
		uiPane.setBorder(new MarsPanelBorder());
		mainPane.add(uiPane, BorderLayout.SOUTH);

		final MainWindow theMainwindow = desktop.getMainWindow();

		// Create Unit Toolbar Visibility checkbox.
		boolean unitToolBarVisible = theMainwindow.getUnitToolBar().isVisible();
		checkUnitToolBar = new JCheckBox(Msg.getString("PreferencesWindow.showUnitToolbar"), //$NON-NLS-1$
				unitToolBarVisible);
		checkUnitToolBar.setToolTipText(Msg.getString("PreferencesWindow.tooltip.showUnitToolbar")); //$NON-NLS-1$
		checkUnitToolBar.addActionListener(this);

		uiPane.add(checkUnitToolBar, BorderLayout.CENTER);

		// Create Toolbar Visibility checkbox.
		boolean toolToolBarVisible = theMainwindow.getToolToolBar().isVisible();
		checkToolToolBar = new JCheckBox(Msg.getString("PreferencesWindow.showToolbar"), toolToolBarVisible); //$NON-NLS-1$
		checkToolToolBar.setToolTipText(Msg.getString("PreferencesWindow.tooltip.showToolbar")); //$NON-NLS-1$
		checkToolToolBar.addActionListener(this);

		uiPane.add(checkToolToolBar, BorderLayout.SOUTH);

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
		Object source = e.getSource();
		if (source == this.checkToolToolBar) {
			desktop.getMainWindow().getToolToolBar().setVisible(checkToolToolBar.isSelected());
		} else if (source == checkUnitToolBar) {
			desktop.getMainWindow().getUnitToolBar().setVisible(checkUnitToolBar.isSelected());
		} else if (source == checkMute) {
			soundPlayer.setMute(checkMute.isSelected());
			volumeSlider.setEnabled(!soundPlayer.isMute());
		}
	}
}