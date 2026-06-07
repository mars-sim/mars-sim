/*
 * Mars Simulation Project
 * ContentManager.java
 * @date 2026-03-17
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import java.awt.Taskbar;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.util.SystemInfo;
import com.mars_sim.core.Simulation;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.utils.SaveDialog;


/**
 * This represents the content manager for the main window. It provides methods to get the properties of all UI elements.
 * This is used to save the UI configuration.
 */
public abstract class ContentManager {

    private static final String AUDIO_PROPS = "audio";
    private static final String STYLE_PROPS = "style";
    private static final String NOTIFICATION_PROPS = "notification";

    private UIConfig config;
    private AudioPlayer audio = null;
    private JFrame mainFrame;
    private Simulation sim;
    private NotificationManager noticeMgr;

    protected ContentManager(Simulation sim, UIConfig config, boolean useAudio) {
        this.config = config;
        this.sim = sim;

        // Set up the look and feel library to be used
		StyleManager.setUIProps(config.getPropSet(STYLE_PROPS));
		
		// Start audio if enabled
		if (useAudio) {
            Properties props = config.getPropSet(AUDIO_PROPS);
            audio = new AudioPlayer(props);
		}
			
        mainFrame = new JFrame("Mars Simulation Project");
		mainFrame.setResizable(true);
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				SaveDialog.createEndSimulation(sim, ContentManager.this);
			}
		});

        // Setup icons
		if (SystemInfo.isMacOS) {
			Taskbar taskbar = Taskbar.getTaskbar();
			taskbar.setIconImage(StyleManager.getIconImage());
			
			// Move the menu bar out of the main window to the top of the screen
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		}
		else {
			mainFrame.setIconImage(StyleManager.getIconImage());
		}

        // Activate notification manager
        this.noticeMgr = new NotificationManager(sim, mainFrame, config.getPropSet(NOTIFICATION_PROPS));
    }

	/**
	 * Sets up the screen config used from last saved session.
	 */
	protected boolean loadSavedScreen() {
		// Display screen at a certain location
		var location = config.getMainWindowLocation();
		if (location == null) {
			return false;
		}
		mainFrame.setLocation(config.getMainWindowLocation());

		// For the Main Window	
		var selectedSize = config.getMainWindowDimension();
		if (selectedSize != null) {
			// Set frame size
			mainFrame.setSize(selectedSize);
		}

		return true;
	}

    /**
	 * Gets the UI property sets of the application. Each set has a name. 
	 * @return A map of UI property sets.
	 */
	public Map<String, Properties> getUIProps() {
        Map<String, Properties> result = new HashMap<>();

        result.put(STYLE_PROPS, StyleManager.getUIProps());

        if (audio != null) {
            result.put(AUDIO_PROPS, audio.getUIProps());
        }

        if (noticeMgr != null) {
            result.put(NOTIFICATION_PROPS, noticeMgr.getUIProps());
        }
        return result;
    }

    /**
	 * Get the details of all content windows currently open on the desktop. This is used to save the UI configuration.
	 * @return
	 */
	public abstract List<WindowSpec> getContentSpecs();
    
    /**
     * Get the UI configuration for the main window.
     * @return UI configuration.
     */
    public UIConfig getConfig() {
        return config;
    }
    
    /**
     * Get the top-level frame of the main window.
     * @return Top level main frame.
     */
    public JFrame getTopFrame() {
        return mainFrame;
    }

    /**
     * Get the Simulation instance.
     * @return The Simulation instance.
     */
    public Simulation getSimulation() {
        return sim;
    }

    /**
     * Get the notification manager instance.
     */
    public NotificationManager getNotifications() {
        return noticeMgr;   
    }

    /**
     * Shutdown the UI. The subclass should implement this method to perform any necessary cleanup when the application is closed..
     */
    public void shutdown() {
        mainFrame.dispose();
    }

    /**
     * Get the AudioPlayer instance. This could be null if audio is not enabled.
      * @return The AudioPlayer instance, or null if audio is not enabled.
     */
    public AudioPlayer getAudio() {
        return audio;
    }
}