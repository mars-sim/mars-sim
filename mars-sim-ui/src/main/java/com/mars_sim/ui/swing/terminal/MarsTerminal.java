/*
 * Mars Simulation Project
 * MarsTerminal.java
 * @date 2025-08-07
 * @author Manny Kung
 */
package com.mars_sim.ui.swing.terminal;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.UserChannel;
import com.mars_sim.console.chat.simcommand.TopLevel;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.time.ClockListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.ui.swing.MainWindow;

public class MarsTerminal extends SwingTextTerminal implements ClockListener {
	
    private static final Logger logger = Logger.getLogger(MarsTerminal.class.getName());

	/** Icon image filename for frame */
    public static final String MARS_SIM = "Mars Simulation Project";
    public static final String ABOUT_MSG =
			"     Version " + SimulationRuntime.VERSION.getVersionTag() + "\n"
			+ "     " + SimulationRuntime.VERSION.getBuildString();
		
	private static final int DEFAULT_WIDTH = 1024;
	private static final int DEFAULT_HEIGHT = 600;

	private JFrame frame = getFrame();
	
    private Simulation sim;
    private TextIO textIO;

	/**
	 * The ConsoleTask allows running the beryx console in a thread.
	 */
	private class ConsoleTask implements Runnable {

		@Override
		public void run() {
            UserChannel channel = new TextIOChannel(textIO);
            // Console is always an admin
            Set<ConversationRole> roles = new HashSet<>();
            roles.add(ConversationRole.ADMIN);
            Conversation conversation = new Conversation(channel, new TopLevel(), roles, sim);

            conversation.interact();
            logger.info("Conversation ended.");
		}
	}

    private static class PopupListener extends MouseAdapter {
    	
        private final JPopupMenu popup;

        public PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }
        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public MarsTerminal(Simulation sim) {

        this.sim = sim;
        configureMainMenu();

        var popup = new JPopupMenu();

        JTextPane textPane = getTextPane();
        popup.add(addAction("ctrl C", "Copy", textPane::copy));
        popup.add(addAction("ctrl V", "Paste", textPane::paste));
        
        MouseListener popupListener = new PopupListener(popup);
        textPane.addMouseListener(popupListener);
            
    	frame.toBack();
    	frame.setAlwaysOnTop(false);
        
		init();
		  
		// Prevent allow users from arbitrarily close the terminal by clicking top right
		// close button
		registerUserInterruptHandler(term -> {
		}, false);


		// Add Mars Terminal to the clock listener
		sim.getMasterClock().addClockListener(this, 1000);
		// Update title
		changeTitle(false);

        this.textIO = new TextIO(this);

        // What does this actually do ??
        new SwingHandler(textIO, new GameManager());

        // Start console in a separate thread
		Thread consoleThread = new Thread(new ConsoleTask());
		consoleThread.setName("ConsoleThread");
		consoleThread.start();
	}

	
	public void positionTerminal(Point location, Dimension terminalSize) {
		
		logger.config("Mars Terminal position: " + location + " dimension: " + terminalSize);
		frame.setLocation(location);

		frame.setSize(terminalSize);
		frame.setPreferredSize(terminalSize);
		frame.pack();

	};

    private void configureMainMenu() {

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Exit", JOptionPane.YES_NO_OPTION);
		        if (reply == JOptionPane.YES_OPTION) {
		            printf("Exiting the Simulation..." + System.lineSeparator());
		        	sim.endSimulation();
		    		if (sim.getMasterClock() != null)
		    			sim.getMasterClock().exitProgram();

					frame.setVisible(false);
			    	dispose(null);
					System.exit(0);
		        }
			}
		});

        frame.setResizable(true);
        
		setPaneTitle(SimulationRuntime.SHORT_TITLE);

        positionTerminal(new Point(0, 0), new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

		frame.setIconImage(MainWindow.getIconImage());
        frame.setVisible(true);
    }


    private JMenuItem addAction(String keyStroke, String menuText, Runnable action) {
        JMenuItem menuItem = new JMenuItem(menuText);
        menuItem.addActionListener(e -> action.run());

        JTextPane textPane = getTextPane();
        String actionKey = "MarsTerminal." + keyStroke.replaceAll("\\s", "-");
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        if(ks != null) {
            textPane.getInputMap().put(ks, actionKey);
        }
        textPane.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
        return menuItem;
    }

	public void changeTitle(boolean isPaused) {
		String mode = switch (GameManager.getGameMode()) {
			case COMMAND -> "Command Mode";
			case SANDBOX -> "Sandbox Mode";
			case SPONSOR -> "Sponsor Mode";
			case SOCIETY -> "Society Mode";
		};

		setPaneTitle(SimulationRuntime.SHORT_TITLE + "  -  " + mode + (isPaused ? "  -  [ P A U S E ]" : ""));
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		// not needed
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		changeTitle(isPaused);
	}
}