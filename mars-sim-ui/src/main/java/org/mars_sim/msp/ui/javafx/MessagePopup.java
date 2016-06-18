/**
 * Mars Simulation Project
 * MessagePopup.java
 * @version 3.08 2016-06-17
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.mars_sim.msp.ui.javafx.PNotification;
import org.mars_sim.msp.ui.javafx.quotation.QNotification;
import org.reactfx.util.FxTimer;
import org.reactfx.util.Timer;
//import org.reactfx.util.Duration;
//import eu.hansolo.enzo.notification.NotifierBuilder;
//import eu.hansolo.enzo.notification.Notification.Notifier;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.quotation.Quotation;
import org.mars_sim.msp.core.quotation.QuotationConfig;
import org.mars_sim.msp.core.structure.building.function.CropConfig;

import javafx.stage.Stage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.util.Duration;


/** The Message class creates a quotation in proper format for use by MainScene 
 */
public class MessagePopup implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 300;
	private static final int CHARS_PER_LINE = 40;
	private static final int POPUP_IN_MILLISECONDS = 20_000;
	
	// Data members		
    private PNotification.Notifier notifier = PNotification.Notifier.INSTANCE;

	
    /** Constructs a quotation object for creating a quote popup
     */
    public MessagePopup() {     
    	//System.out.println("instantiating MessagePopup()");
    	
    }

    public void stop() {
    	notifier.stop();
    }
    
    public void popAMessage(String title, String msg, String name, Stage stage, Pos location, Image IMAGE) {  
    	//System.out.println("calling popAMessage()");
    	
    	//notifier = Notification.Notifier.INSTANCE;
		//msg = "\"" + wrap(msg, CHARS_PER_LINE-1) + "\"";	
		msg = wrap(msg, CHARS_PER_LINE-1);	
		    	
		int strSize = msg.length();	
		int numLines = (int)Math.ceil((double)strSize/CHARS_PER_LINE);	
	
		int nameSize = 0;
			
		if (name != null)
			nameSize = name.length() + 3;
		else
			nameSize = 0;
		
		//int remaining = CHARS_PER_LINE * numLines - strSize;
		int index = msg.lastIndexOf(System.lineSeparator());//"\n");
		String s = msg.substring(index+1, strSize);
		int lastLineLength = s.length();
		int remaining = CHARS_PER_LINE - lastLineLength;
		
		int numWhiteSpace = 0;
		int height = 0;
		int new_width = WIDTH;
		int base_height = 25 * 2 ;
		
		if (strSize < CHARS_PER_LINE) {
			
			numWhiteSpace = (int)(strSize - nameSize);
			
			new_width = (int)(strSize * 7.818);
			new_width = 50 + new_width;
			
			height = base_height + 15;
			
			msg += System.lineSeparator();//"\n";
			
			//System.out.println("Case 1 : quote can fit one line ");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
			
		}
		else if (remaining > nameSize) {
			numWhiteSpace = (int)(remaining - nameSize);
			
			//new_width = (int)((numWhiteSpace + nameSize) * 7.818);
			//WIDTH = 40 + new_width;

			height = base_height + 20 * numLines;

			//System.out.println("Case 2 : last line can fit author's name");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
		}	
		else {
			numWhiteSpace = (int)(CHARS_PER_LINE - nameSize);	
		
			//new_width = (int)((numWhiteSpace + nameSize) * 7.818);
			//WIDTH = 40 + new_width;
			
			height = base_height + 20 * (numLines + 1);
		
			msg += System.lineSeparator();//"\n";
			//System.out.println("Case 3 : last line cannot fit author's name");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
		}
		

		StringBuffer nameLine = new StringBuffer ("");
		for (int i = 0; i < numWhiteSpace; i++)
			nameLine.append(" ");

		if (name != null)
			nameLine.append("- ").append(name);	
					
		msg += nameLine;
		
        //Duration duration = new Duration(POPUP_IN_MILLISECONDS);
		//notifier.setPopupLifetime(duration);
		
        notifier.setPopupLifetime(Duration.ZERO);//INDEFINITE);//duration); 
        PNotification.Notifier.setNotificationOwner(stage);
        notifier.setPopupLocation(stage, location);
        PNotification.Notifier.setHeight(height);
        PNotification.Notifier.setWidth(new_width);
        //System.out.println("wait time : " + notifier.getPopupLifetime().toSeconds() + " secs");
        notifier.notify(title, msg, IMAGE); //INFO_ICON);
	        //notifier.setNotificationOwner(stage);
 
		stage.requestFocus();

    }
    
    /*
     * Wraps the line properly by considering the word and adding a newline character to the end of each line 
     */
    public static String wrap(String in,int len) {
    	in = in.trim();
    	
    	if (in.length() < len) 
    		return in;
    	
    	if (in.substring(0, len).contains(System.lineSeparator()))//"\n"))
    		return in.substring(0, in.indexOf(System.lineSeparator())).trim() + System.lineSeparator() + System.lineSeparator()
    		+ wrap(in.substring(in.indexOf(System.lineSeparator()) + 1), len);
    	
    	int place = Math.max(Math.max(in.lastIndexOf(" ",len),in.lastIndexOf("\t",len)),in.lastIndexOf("-",len));
    	
    	return in.substring(0,place).trim() + System.lineSeparator() + wrap(in.substring(place),len);
    }
    
    
	public void destroy() {

	    notifier = null;
	}


}