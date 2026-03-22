/*
 * Mars Simulation Project
 * SpeedControl.java
 * @date 2026-03-21
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import com.mars_sim.core.time.ClockListener;
import com.mars_sim.core.time.MasterClock;

/**
 * Control panel for adjusting the speed of the simulation and pausing/resuming it.
 */
public class SpeedControl extends JPanel implements ClockListener {

    private JToggleButton playPauseSwitch;
    private MasterClock clock;
	private JLabel speedLabel;
	private JButton increaseSpeed;
	private JButton decreaseSpeed;

    public SpeedControl(MasterClock masterClock) {
        super();
        setLayout(new FlowLayout());

        this.clock = masterClock;
        clock.addClockListener(this);

		setToolTipText("Speed Control");
		
		Dimension d1 = new Dimension(25, 25);
		
		// Add the decrease speed button
		decreaseSpeed = new JButton("-");
		decreaseSpeed.setAlignmentX(SwingConstants.CENTER);
		decreaseSpeed.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
		decreaseSpeed.setPreferredSize(d1);
		decreaseSpeed.setMaximumSize(d1);
		decreaseSpeed.setToolTipText("Decrease the sim speed (aka time ratio)");
		
		decreaseSpeed.addActionListener(e -> {
				masterClock.decreaseSpeed();
		});
		
		// Create pause switch
		createPauseSwitch(d1, masterClock);

		increaseSpeed = new JButton("+");
		increaseSpeed.setAlignmentX(SwingConstants.CENTER);
		increaseSpeed.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
		increaseSpeed.setPreferredSize(d1);
		increaseSpeed.setMaximumSize(d1);
		increaseSpeed.setToolTipText("Increase the sim speed (aka time ratio)");

		increaseSpeed.addActionListener(e -> {
				masterClock.increaseSpeed();
		});
		
		// Add the increase speed button
		add(decreaseSpeed);
		add(playPauseSwitch);
		add(increaseSpeed);

		// Simulate change to corrrect buttons
		pauseChange(masterClock.isPaused());

		speedLabel = new JLabel("x" + masterClock.getDesiredTR());
		add(speedLabel);
	}

	/**
	 * Creates the pause button.
	 */
	private JToggleButton createPauseSwitch(Dimension d1, MasterClock clock) {
		playPauseSwitch = new JToggleButton("\u23E8");
		playPauseSwitch.setAlignmentX(SwingConstants.CENTER);
		playPauseSwitch.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
		playPauseSwitch.setPreferredSize(d1);
		playPauseSwitch.setMaximumSize(d1);
		playPauseSwitch.setToolTipText("Pause or Resume the Simulation");
		playPauseSwitch.addActionListener(e -> {
            boolean isSel = playPauseSwitch.isSelected();
            if (isSel) {
                // To show play symbol
                playPauseSwitch.setText("\u23F5");
            }
            else {
                // To show pause symbol 
                playPauseSwitch.setText("\u23F8");
            }		
            clock.setPaused(isSel);	
        });
		playPauseSwitch.setText("\u23F8");
		
		return playPauseSwitch;
	}

	/**
	 * The speed of the clock has changed, update the label to show the new speed.
	 * @param desiredTR The new desired time ratio.
	 */
    @Override
    public void desiredTimeRatioChange(int desiredTR) {
        speedLabel.setText("x" + desiredTR);
    }

	/**
	 * The pause state of the clock has changed, update the play/pause button and enable/disable speed controls accordingly.
	 * @param isPaused true if the clock is paused, false otherwise.
	 */
    @Override
    public void pauseChange(boolean isPaused) {
        playPauseSwitch.setSelected(isPaused);
		increaseSpeed.setEnabled(!isPaused);
		decreaseSpeed.setEnabled(!isPaused);
    }

	/**
	 * Unregister the listener from the clock to prevent memory leaks when this component is no longer needed.
	 */
    public void unregister() {
		clock.removeClockListener(this);
	}
}
