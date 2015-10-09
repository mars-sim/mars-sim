package org.mars_sim.msp;

import java.awt.Color;

import mikera.gui.Frames;
import mikera.gui.JConsole;
import mikera.image.Colours;
import mikera.util.Rand;

public class ConsoleApp {

    public static void main(String[] args) {	
    	JConsole jc=new JConsole(100,40);
    	jc.setCursorVisible(true);
    	jc.setCursorBlink(true);
    	jc.write("Hello World\n");
    	jc.write("Hello World\n",Color.BLACK,Color.MAGENTA);
    	jc.write("Hello World\n",Color.GREEN,Color.BLACK);

    	System.out.println("Normal output");
    	jc.captureStdOut();
    	System.out.println("Captured output");
    	
    	
    	// brown box
    	jc.fillArea(' ', Color.WHITE, new Color(100,70,30), 20, 20, 3, 3);
    	
    	jc.setCursorPos(0, 0);

    	Frames.display(jc,"JConsole test application");
    	
    	int SECS=3;
    	long start=System.currentTimeMillis();
    	int iterations=0;
    	
    	while (start>(System.currentTimeMillis()-1000*SECS)) {
    		for (int y=10; y<20; y++) {
    			for (int x=10; x<80; x++) {
    				jc.fillArea((char)Rand.r(256), 
    						Colours.getColor(Rand.nextInt()), 
    						Colours.getColor(Rand.nextInt()), 
    						x, y, 1, 1);
    			}
    		}
    		jc.repaint();
    		iterations++;
    	}
    	
    	jc.setCursorPos(0, 6);
    	System.out.println("FPS="+iterations/SECS);
    }

}
