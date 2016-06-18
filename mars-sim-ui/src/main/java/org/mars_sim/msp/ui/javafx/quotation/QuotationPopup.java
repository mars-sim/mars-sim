/**
 * Mars Simulation Project
 * QuotationPopup.java
 * @version 3.08 2016-06-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.quotation;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.mars_sim.msp.ui.javafx.PNotification;
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


/** The Quotation class creates a quotation in proper format for use by MainScene 
 */
public class QuotationPopup implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 470;
	private static final int CHARS_PER_LINE = 55;
	private static final int POPUP_IN_MILLISECONDS = 20_000;
	
	// Data members	
	private Map<Integer, Quotation> quotations;
	
    private QNotification.Notifier notifier = QNotification.Notifier.INSTANCE;
	//private Timer timer;
	
	private Object[] quoteArray;
	
	private Quotation q;

	
    /** Constructs a quotation object for creating a quote popup
     */
    public QuotationPopup() {     		
    	QuotationConfig quotationConfig = SimulationConfig.instance().getQuotationConfiguration();
    	quotations = quotationConfig.getQuotations();
    	quoteArray = quotations.values().toArray();         
    	//System.out.println("# of quotes : " + quoteArray.length);
    	
    	Duration duration = new Duration(POPUP_IN_MILLISECONDS);       
        notifier.setPopupLifetime(duration); 
        //Notification.Notifier.setPopupLocation(stage, Pos.TOP_RIGHT);
       
    }

    @SuppressWarnings("restriction")
	public void popAQuote(Stage stage) {	
    	//System.out.println("calling popAQuote()");
     	//notifier = Notification.Notifier.INSTANCE;
    
        //Notification.Notifier notifier = NotifierBuilder.create().build();
                //.popupLocation(Pos.TOP_RIGHT)
                //.popupLifeTime(Duration.millis(10000))
                //.styleSheet(getClass().getResource("mynotification.css").toExternalForm())
        //        .build();
        
    	Random rand = new Random();
    	
    	int num = rand.nextInt(quoteArray.length);
    	q = (Quotation)quoteArray[num];
    	
    	String name = q.getName();
    	String str = q.getText();  	 

		str = "\"" + wrap(str, CHARS_PER_LINE-1) + "\"";	
    	
		int strSize = str.length();	
		int numLines = (int)Math.ceil((double)strSize/CHARS_PER_LINE);	
	
		int nameSize = name.length() + 3;
		
		//int remaining = CHARS_PER_LINE * numLines - strSize;
		int index = str.lastIndexOf(System.lineSeparator());//"\n");
		String s = str.substring(index+1, strSize);
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
			
			str += System.lineSeparator();//"\n";
			
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
		
			str += System.lineSeparator();//"\n";
			//System.out.println("Case 3 : last line cannot fit author's name");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
		}
		
		
		//System.out.println("# of remaining whitespaces in the last line: " + remaining);
		//System.out.println("# of chars in the quote : " + strSize);
		//System.out.println("# of lines : " + numLines);
		//System.out.println("CHARS_PER_LINE : " + CHARS_PER_LINE);
		//System.out.println("height in px : " + height);
		//System.out.println("WIDTH in px : " + WIDTH);
		
		StringBuffer nameLine = new StringBuffer ("");
		for (int i = 0; i < numWhiteSpace; i++)
			nameLine.append(" ");

		nameLine.append("- ").append(name);	
					
		str += nameLine;
		
		//System.out.println(str);	
		

        //stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));


        //Duration duration = new Duration(POPUP_IN_MILLISECONDS);       
        //notifier.setPopupLifetime(duration);        
        QNotification.Notifier.setNotificationOwner(stage);
        //notifier.setPopupLocation(stage, Pos.TOP_RIGHT);
        QNotification.Notifier.setHeight(height);
        QNotification.Notifier.setWidth(new_width);
		//Notification n0 = new NotificationFX("QUOTATION", quoteString, QUOTE_ICON)
		notifier.notify(" QUOTATION", str, QNotification.QUOTE_ICON); //INFO_ICON);

		stage.requestFocus();
/*		
 		// The timeer is for removing the icon in the windows taskbar. 
 		// Using it will clamp the next notifier from having lived the full 20 seconds life time
		timer = FxTimer.runLater(
				java.time.Duration.ofMillis(POPUP_IN_MILLISECONDS),
		        () -> {
		        	//stopTimer();
		        	notifier.stop();
		        });
*/
		
		/*
		Timeline notify_timeline = null;
		notify_timeline = new Timeline(new KeyFrame(Duration.millis(21000), ae -> {
			Timeline n = notify_timeline;
			n.stop();
			notifier.stop();	
		}));
		
		notify_timeline.setCycleCount(1);//javafx.animation.Animation.INDEFINITE);
		notify_timeline.play();
		*/
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
    
/*    
	public void stopNotification() {
	//	notify_timeline.stop();
	//	notifier.stop();
	}
	
	public void stopTimer() {//Timer timer) {
		//	notify_timeline.stop();
			timer.stop();
		}
*/		

	public void destroy() {

	    quotations.clear();
	    quotations = null;
	    notifier = null;
	    quoteArray = null;
	    q = null;
	}
    
}