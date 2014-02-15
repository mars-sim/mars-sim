package org.mars_sim.msp.ui.ogl.sandbox;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.mars_sim.msp.ui.ogl.sandbox.scene.GLDisplay;

public class Input
extends KeyAdapter
implements MouseListener,MouseMotionListener,MouseWheelListener {

	private Display display;
	private boolean mouseCapture = false;
	private int mouseX = 0;
	private int mouseY = 0;

    public Input(Display display, GLDisplay glDisplay) {
        this.display = display;
    }

    public void keyPressed(KeyEvent e) {
        processKeyEvent(e, true);
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            default: processKeyEvent(e, false); break;
        }
    }

    private void processKeyEvent(KeyEvent e, boolean pressed) {
        switch (e.getKeyCode()) {
	        case KeyEvent.VK_PAGE_UP:	// go through to next case
	        case KeyEvent.VK_PLUS:		display.zoomIn(pressed); break;
            case KeyEvent.VK_PAGE_DOWN: // go through to next case
            case KeyEvent.VK_MINUS:		display.zoomOut(pressed); break;
            case KeyEvent.VK_UP:       	display.increaseXspeed(pressed); break;
            case KeyEvent.VK_DOWN:    	display.decreaseXspeed(pressed); break;
            case KeyEvent.VK_RIGHT:    	display.increaseYspeed(pressed); break;
            case KeyEvent.VK_LEFT:     	display.decreaseYspeed(pressed); break;
            case KeyEvent.VK_W:       	display.fore(pressed); break;
            case KeyEvent.VK_S:       	display.back(pressed); break;
            case KeyEvent.VK_A:       	display.left(pressed); break;
            case KeyEvent.VK_D:        	display.right(pressed); break;
            case KeyEvent.VK_Q:        	display.down(pressed); break;
            case KeyEvent.VK_E:        	display.up(pressed); break;
        }
    }
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    	this.mouseCapture = true;
    	this.mouseX = e.getX();
    	this.mouseY = e.getY();
    }

    public void mouseReleased(MouseEvent e) {
    	this.mouseCapture = false;
    }

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	public void mouseMoved(MouseEvent e) {
		if (this.mouseCapture) {
	    	int newX = e.getX();
	    	int newY = e.getY();
	    	double[] rota = this.display.getScene().getRotation();
	    	rota[0] -= (this.mouseY - newY) / 4.0f;
	    	rota[2] -= (this.mouseX - newX) / 4.0f;
	    	this.display.getScene().setRotation(rota);
	    	this.mouseX = newX;
	    	this.mouseY = newY;
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		int x = e.getWheelRotation();
		if (x > 0) {
			this.display.zoomIn(true);
		} else {
			this.display.zoomOut(true);
		}
	}
}
