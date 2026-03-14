/*
 * Mars Simulation Project
 * AudioControl.java
 * @date 2026-03-13
 */
package com.mars_sim.ui.swing.sound;

import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;


import com.mars_sim.ui.swing.ImageLoader;
import com.mars_sim.ui.swing.StyleManager;

/**
 * Swing panel controlling audio mute and volume for music and sound effects.
 */
@SuppressWarnings("serial")
public class AudioControl extends JPanel {

	private static final int MIN_LEVEL = 1;
	private static final int MAX_LEVEL = 10;
	
    public static final String TITLE = "Audio Control";
    public static final String ICON = "sound";

	private final AudioPlayer audioPlayer;

	/**
	 * Creates a control panel bound to an audio player.
	 *
	 * @param audioPlayer the target audio player.
	 */
	public AudioControl(AudioPlayer audioPlayer) {
		super();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.audioPlayer = audioPlayer;

		add(createMusicControls());
		add(createSoundEffectControls());
	}

	private JPanel createMusicControls() {
		var icon = ImageLoader.getIconByName("music");
		JToggleButton muteToggle = new JToggleButton(icon, audioPlayer.isMusicMute());
		muteToggle.setToolTipText("Mute music");

		JSlider volumeSlider = createVolumeSlider(audioPlayer.getMusicVolume());
		if (audioPlayer.isMusicMute()) {
			volumeSlider.setEnabled(false);
		}
		volumeSlider.addChangeListener(e -> audioPlayer.setMusicVolume(toVolume(volumeSlider.getValue())));
		muteToggle.addActionListener(e -> musicMute(muteToggle.isSelected(), volumeSlider));

		return createControlRow("Music", muteToggle, volumeSlider);
	}

	private JPanel createSoundEffectControls() {
		var icon = ImageLoader.getIconByName("sound");
		JToggleButton muteToggle = new JToggleButton(icon, audioPlayer.isSoundEffectMute());
		muteToggle.setToolTipText("Mute sound effects");

		JSlider volumeSlider = createVolumeSlider(audioPlayer.getSoundEffectVolume());
		if (audioPlayer.isSoundEffectMute()) {
			volumeSlider.setEnabled(false);
		}
		volumeSlider.addChangeListener(e -> audioPlayer.setSoundEffectVolume(toVolume(volumeSlider.getValue())));
		muteToggle.addActionListener(e -> soundEffectMute(muteToggle.isSelected(), volumeSlider));

		return createControlRow("Sound Effect", muteToggle, volumeSlider);
	}

	private void soundEffectMute(boolean isMuted, JSlider slider) {
		audioPlayer.setSoundEffectMute(isMuted);
		slider.setEnabled(!isMuted);
	}

	private void musicMute(boolean isMuted, JSlider slider) {
		audioPlayer.setMusicMute(isMuted);
		slider.setEnabled(!isMuted);
	}

	private JPanel createControlRow(String title, JToggleButton muteToggle, JSlider volumeSlider) {

		JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

		var label = new JLabel(title);
		label.setFont(StyleManager.getLabelFont());
		top.add(label);
		top.add(muteToggle);
		top.add(volumeSlider);
		
		return top;
	}

	private static JSlider createVolumeSlider(double volume) {
		int value = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, (int) Math.round(volume * MAX_LEVEL)));

		JSlider slider = new JSlider(MIN_LEVEL, MAX_LEVEL, value);
		slider.setMajorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		return slider;
	}

	private static double toVolume(int sliderValue) {
		return sliderValue / 10D;
	}

	private static JDialog openDialog = null;

	/**
	 * Shows this control panel in a standalone popup frame.
	 *
	 * @param audioPlayer the audio player to control.
	 * @return the created frame.
	 */
	public static JDialog showPopup(AudioPlayer audioPlayer) {
		if (openDialog != null) {
			openDialog.toFront();
		}
		else {
			var frame = new JDialog();
			frame.setTitle("Audio Control");
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setContentPane(new AudioControl(audioPlayer));
			frame.pack();
			frame.setLocationByPlatform(true);
			frame.setResizable(false);
			frame.setVisible(true);
			openDialog = frame;
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					openDialog = null;
				}
			});

		}
		return openDialog;
	}
}