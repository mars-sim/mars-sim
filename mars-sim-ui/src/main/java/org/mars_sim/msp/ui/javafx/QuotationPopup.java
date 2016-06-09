/**
 * Mars Simulation Project
 * QuotationPopup.java
 * @version 3.08 2016-06-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Random;

import eu.hansolo.enzo.notification.Notification;
import eu.hansolo.enzo.notification.Notification.Notifier;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.quotation.Quotation;
import org.mars_sim.msp.core.quotation.QuotationConfig;
import org.mars_sim.msp.core.structure.building.function.CropConfig;

import javafx.stage.Stage;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;


/** The Quotation class creates a quotation in proper format for use by MainScene 
 */
public class QuotationPopup implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	
	private Map<Integer, Quotation> quotations;
	
    private Notification.Notifier notifier;

	private Timeline notify_timeline;

	
    /** Constructs a quotation object for creating a quote popup
     */
    public QuotationPopup() {     	
    	QuotationConfig quotationConfig = SimulationConfig.instance().getQuotationConfiguration();
    	quotations = quotationConfig.getQuotations();
    }

    public void popAQuote(Stage stage) {

    	Random rand = new Random();
    	Object[] values = quotations.values().toArray();
    	int num = rand.nextInt(values.length);
    	Quotation q = (Quotation)values[num];
    	
    	String name = q.getName();
    	String str = q.getText();
    	
    	int maxPerLine = 55; 

		str = "\"" + wrap(str, maxPerLine) + "\"";	
    	
		int strSize = str.length();	
		int numLines = (int)Math.ceil((double)strSize/maxPerLine);	
		int WIDTH = 365;
	
		int nameSize = name.length() + 3;
		
		int remaining = maxPerLine * numLines - strSize;
		int numWhiteSpace = 0;
		int height = 0;
		
		if (strSize < maxPerLine) {
			numWhiteSpace = (int)((maxPerLine - nameSize)*1.33-2);
			height = (numLines + 4) * 25;
			str += "\n";
			//System.out.println("Case 1 : quote can fit one line ");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
			
		}
		else if (remaining > nameSize + 2) {
			numWhiteSpace = (int)((remaining - nameSize)*1.33-2);	
			
			if (numLines <= 3)
				height = (numLines + 2) * 25;
			else
				height = (numLines + 1) * 25;
			
			//System.out.println("Case 2 : last line can fit author's name");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
		}	
		else {
			numWhiteSpace = (int)((maxPerLine - nameSize)*1.33-2);
			height = (numLines + 2) * 25;
			str += "\n";
			//System.out.println("Case 3 : last line cannot fit author's name");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
		}
		
		
		//System.out.println("# of remaining whitespace: " + remaining);
		//System.out.println("# of chars in the quote : " + strSize);
		//System.out.println("# of lines : " + numLines);
		//System.out.println("max char per line : " + maxPerLine);
		//System.out.println("height in px : " + height);
		
		StringBuffer nameLine = new StringBuffer ("");
		for (int i = 0; i < numWhiteSpace; i++)
			nameLine.append(" ");

		nameLine.append("- ").append(name);	
					
		str += nameLine;
		
		//System.out.println(str);	
		
        notifier = Notification.Notifier.INSTANCE;
        
		notifier.setHeight(height);
        notifier.setWidth(WIDTH);
        
        stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
        notifier.setNotificationOwner(stage);
        
        Duration duration = new Duration(20000);
        
        notifier.setPopupLifetime(duration); 
		//Notification n0 = new NotificationFX("QUOTATION", quoteString, QUOTE_ICON);//QUOTE_ICON);	
		notifier.notify("QUOTATION", str, Notification.INFO_ICON);// QUOTE_ICON);//n0);
				
		notify_timeline = new Timeline(new KeyFrame(Duration.millis(21000), ae -> stopNotification()));
		notify_timeline.setCycleCount(1);//javafx.animation.Animation.INDEFINITE);
		notify_timeline.play();
		
    }
    
    /*
     * Wraps the line properly by considering the word and adding a newline character to the end of each line 
     */
    public static String wrap(String in,int len) {
    	in = in.trim();
    	
    	if (in.length() < len) 
    		return in;
    	
    	if (in.substring(0, len).contains("\n"))
    		return in.substring(0, in.indexOf("\n")).trim() + "\n\n" + wrap(in.substring(in.indexOf("\n") + 1), len);
    	
    	int place = Math.max(Math.max(in.lastIndexOf(" ",len),in.lastIndexOf("\t",len)),in.lastIndexOf("-",len));
    	
    	return in.substring(0,place).trim()+"\n"+wrap(in.substring(place),len);
    }
    
	public void stopNotification() {
		notify_timeline.stop();
		notifier.stop();
	}
	
}