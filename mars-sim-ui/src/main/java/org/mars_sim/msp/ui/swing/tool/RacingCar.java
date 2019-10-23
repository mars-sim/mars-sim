package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

// https://codereview.stackexchange.com/questions/136692/swing-keybinding-to-control-an-animated-car
public class RacingCar extends JFrame {
	
	public RacingCar() {
	    add(new CarPanel());
	}
	
	public static void main(String[] args) {
	    JFrame frame = new RacingCar();
	    frame.setSize(400, 300);
	    frame.setLocationRelativeTo(null);
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setVisible(true);
	}
	
	class CarPanel extends JPanel {
	    private int baseX = 0;
	    private int baseY = 0;
	    private boolean reverse = false;
	    private int delay = 10;     // Initial delay
	    private Timer timer = new Timer(delay, new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            //Change the base alignment depending the direction of car
	
	            if (!reverse) {
	                baseX++;
	            }
	            else {
	                baseX--;
	            }
	            repaint();
	        }
	    });
	
	
	    public CarPanel() {
	        //Add keybinds for UP and DOWN keys as well as the 'P' key to pause/resume
	        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "speedUp");
	        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "slowDown");
	        getInputMap().put(KeyStroke.getKeyStroke("P"), "pause");
	        getInputMap().put(KeyStroke.getKeyStroke("released P"), "resume");
	
	
	        //Define actions for each key
	        getActionMap().put("speedUp", new AbstractAction() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                if (delay > 0) {
	                    delay -= 1;
	                    timer.setDelay(delay);
	                }
	            }
	        });
	        getActionMap().put("slowDown", new AbstractAction() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                delay += 1;
	                timer.setDelay(delay);
	            }
	        });
	        getActionMap().put("pause", new AbstractAction() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                timer.stop();
	            }
	        });
	        getActionMap().put("resume", new AbstractAction() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                timer.start();
	            }
	        });
	
	        timer.start();
	
	    }
	
	    //Method for painting the car at the given base
	    private void drawCar(Graphics g) {
	        //Check if car is at an end
	        if (!reverse && baseX == getWidth() - 50)
	            reverse = true;
	        else if (baseX == 0)
	            reverse = false;
	
	        //Draw top of car
	        g.setColor(Color.BLUE);
	        g.fillPolygon(new int[]{baseX + 10, baseX + 20, baseX + 30, baseX + 40},
	          new int[]{baseY - 20, baseY - 30, baseY - 30, baseY - 20}, 4);
	
	        //Draw body of car
	        g.setColor(Color.CYAN);
	        g.fillRect(baseX, baseY - 20, 50, 10);
	
	        //Draw wheels
	        g.setColor(Color.BLACK);
	        g.fillOval(baseX + 10, baseY - 10, 10, 10);
	        g.fillOval(baseX + 30, baseY - 10, 10, 10);
	    }
	
	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	
	        //Initialize baseY coordinates
	        if (baseY == 0)
	            baseY = getHeight();
	
	        drawCar(g);
	    }
	}
}