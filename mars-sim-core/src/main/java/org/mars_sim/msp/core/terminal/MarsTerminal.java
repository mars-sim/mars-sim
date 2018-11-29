package org.mars_sim.msp.core.terminal;

import org.beryx.textio.TextTerminal;
import org.beryx.textio.jline.JLineTextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.Simulation;

import javax.swing.*;
import java.awt.event.*;
import java.util.logging.Logger;

public class MarsTerminal extends SwingTextTerminal {
    private static Logger logger = Logger.getLogger(MarsTerminal.class.getName());

    private final JPopupMenu popup = new JPopupMenu();

    private static class PopupListener extends MouseAdapter {
        private final JPopupMenu popup;

        public PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    public MarsTerminal() {	
//    	System.out.println("w: " + getFrame().getWidth()); // w: 656  	
//    	System.out.println("h: " + getFrame().getHeight()); // h: 519    	
    	this.getFrame().setSize(1024, 600);
        configureMainMenu();

        JTextPane textPane = getTextPane();
        addAction("ctrl C", "Copy", () -> textPane.copy());
        addAction("ctrl V", "Paste", () -> textPane.paste());
        MouseListener popupListener = new PopupListener(popup);
        textPane.addMouseListener(popupListener);
    }

    public static void clearScreen(TextTerminal<?> terminal) {
        if (terminal instanceof JLineTextTerminal) {
            terminal.print("\033[H\033[2J");
        } else if (terminal instanceof SwingTextTerminal) {
            ((SwingTextTerminal) terminal).resetToOffset(0);
        }
    }
    
    private void configureMainMenu() {
        JFrame frame = getFrame();
        frame.setTitle("Mars Simulation Project");
        
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);

        JMenuItem pauseItem = new JMenuItem("Pause/Unpause", KeyEvent.VK_P);
        pauseItem.addActionListener(e -> {

        	if (Simulation.instance().getMasterClock() != null) {
				if (Simulation.instance().getMasterClock().isPaused()) {
					Simulation.instance().getMasterClock().setPaused(false, false);
					printf(System.lineSeparator() + System.lineSeparator());
					printf("                      [ Simulation Unpaused ]");
					printf(System.lineSeparator() + System.lineSeparator());
				}
				else {
					Simulation.instance().getMasterClock().setPaused(true, false);
					printf(System.lineSeparator() + System.lineSeparator());
					printf("                       [ Simulation Paused ]");
					printf(System.lineSeparator() + System.lineSeparator());
				}
        	}
        });
        menu.add(pauseItem);
        
        JMenuItem clearItem = new JMenuItem("Clear Screen", KeyEvent.VK_C);
        clearItem.addActionListener(e -> clearScreen(this));
        menu.add(clearItem);
        
        JMenuItem menuItem = new JMenuItem("About", KeyEvent.VK_A);
        menuItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, 
        		  "      Mars Simulation Project\n"
        		+ "                v3.1.0-b2\n"
        		+ "                   2018"));
        menu.add(menuItem);

        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
    }

    private boolean addAction(String keyStroke, String menuText, Runnable action) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyStroke);
        if(ks == null) {
            logger.warning("Invalid keyStroke: " + keyStroke);
            return false;
        }
        JMenuItem menuItem = new JMenuItem(menuText);
        menuItem.addActionListener(e -> action.run());
        popup.add(menuItem);

        JTextPane textPane = getTextPane();
        String actionKey = "MarsTerminal." + keyStroke.replaceAll("\\s", "-");
        textPane.getInputMap().put(ks, actionKey);
        textPane.getActionMap().put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
        return true;
    }
}