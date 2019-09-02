package org.mars.sim.console;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.beryx.textio.TextTerminal;
import org.beryx.textio.jline.JLineTextTerminal;
import org.beryx.textio.swing.SwingTextTerminal;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MasterClock;

public class MarsTerminal extends SwingTextTerminal {
    private static Logger logger = Logger.getLogger(MarsTerminal.class.getName());

	/** Icon image filename for frame */
	public static final String ICON_IMAGE = "/icons/landerhab16.png";
	
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


	static Image iconToImage(Icon icon) {
	   if (icon instanceof ImageIcon) {
	      return ((ImageIcon)icon).getImage();
	   } 
	   else {
	      int w = icon.getIconWidth();
	      int h = icon.getIconHeight();
	      GraphicsEnvironment ge = 
	        GraphicsEnvironment.getLocalGraphicsEnvironment();
	      GraphicsDevice gd = ge.getDefaultScreenDevice();
	      GraphicsConfiguration gc = gd.getDefaultConfiguration();
	      BufferedImage image = gc.createCompatibleImage(w, h);
	      Graphics2D g = image.createGraphics();
	      icon.paintIcon(null, g, 0, 0);
	      g.dispose();
	      return image;
	   }
	 }
    
    private void configureMainMenu() {
        JFrame frame = getFrame();
        frame.setTitle("Mars Simulation Project");
        
		ImageIcon icon = new ImageIcon(MarsTerminal.class.getResource(ICON_IMAGE));
		frame.setIconImage(iconToImage(icon));
        		
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);

        JMenuItem pauseItem = new JMenuItem("Pause/Unpause", KeyEvent.VK_P);
        pauseItem.addActionListener(e -> {

        	MasterClock masterClock = Simulation.instance().getMasterClock();
        	
        	if (masterClock != null) {
        		
				if (masterClock.isPaused()) {
					masterClock.setPaused(false, false);
					printf(System.lineSeparator() + System.lineSeparator());
					printf("                      [ Simulation Unpaused ]");
					printf(System.lineSeparator() + System.lineSeparator());
				}
				else {
					masterClock.setPaused(true, false);
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