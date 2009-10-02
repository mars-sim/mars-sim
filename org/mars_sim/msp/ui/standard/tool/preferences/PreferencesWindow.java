/**
 * Mars Simulation Project
 * PreferencesWindow.java
 * @version 2.87 2009-10-01
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.preferences;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MainWindow;
import org.mars_sim.msp.ui.standard.sound.AudioPlayer;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;
import org.mars_sim.msp.ui.standard.UIConfig;
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
	private JSlider volumeSlider;
	private JCheckBox uiCheck;

	/**
	 * Constructor
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
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, intVolume);
        volumeSlider.setMajorTickSpacing(1);
        volumeSlider.setPaintTicks(true);
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


        // Create UI checkbox.
	boolean nativeLookAndFeel = UIConfig.INSTANCE.useNativeLookAndFeel();
	if (UIConfig.INSTANCE.useUIDefault()) nativeLookAndFeel = false;
        uiCheck = new JCheckBox("Native Look & Feel", nativeLookAndFeel);
	final MainWindow theMainwindow = desktop.getMainWindow();
        uiCheck.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
       			theMainwindow.setLookAndFeel(uiCheck.isSelected());
        		}
        });
        uiPane.add(uiCheck);

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