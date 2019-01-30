/**
 * Mars Simulation Project
 * QuotationPopup.java
 * @version 3.1.0 2017-09-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.quotation;


import java.util.Map;
import java.util.Random;

import org.mars_sim.javafx.MainScene;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.quotation.Quotation;
import org.mars_sim.msp.core.quotation.QuotationConfig;

import javafx.stage.Stage;

//import javafx.animation.KeyFrame;
//import javafx.animation.Timeline;
import javafx.geometry.Pos;
//import javafx.scene.image.Image;
//import javafx.util.Duration;


/** The Quotation class creates a quotation in proper format for use by MainScene
 */
@SuppressWarnings("restriction")
public class QuotationPopup {

	//private static final int SIZE_ICON = 32;
	//private static final int BASE_HEIGHT = 70;
	//private static final int HEIGHT_PER_LINE = 20;
	//private static final int WIDTH = 510;
	//private static final int CHARS_PER_LINE = 50;
	private static final int POPUP_IN_MILLISECONDS = 20_000;

	// Data members
	private int new_width = 515;

	private Map<Integer, Quotation> quotations;

	private MainScene mainScene;
    private QNotification.Notifier notifier = QNotification.Notifier.INSTANCE;
	private Object[] quoteArray;
	private Quotation q;


    /** Constructs a quotation object for creating a quote popup
     */
    @SuppressWarnings("restriction")
	public QuotationPopup(MainScene mainScene) {
    	this.mainScene = mainScene;
    	QuotationConfig quotationConfig = SimulationConfig.instance().getQuotationConfiguration();
    	quotations = quotationConfig.getQuotations();
    	quoteArray = quotations.values().toArray();
    	//Duration d = new Duration(POPUP_IN_MILLISECONDS);
        //notifier.setPopupLifetime(d);
    }

    @SuppressWarnings("restriction")
	public void popAQuote(Stage stage) {
    	//mainScene.openWindow();
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
    	String str = "\"" + q.getText() + "\"";
		//str = "\"" + wrap(str, CHARS_PER_LINE-1) + "\"";
/*
		int strSize = str.length();
		//int numLines = (int)Math.ceil((double)strSize/CHARS_PER_LINE);
		int nameSize = name.length() + 3;
		//int remaining = CHARS_PER_LINE * numLines - strSize;
		int index = str.lastIndexOf(System.lineSeparator());//"\n");
		String s = str.substring(index+1, strSize);
		int lastLineLength = s.length();
		//int remaining = CHARS_PER_LINE - lastLineLength;
		int numWhiteSpace = 0;
		int new_height = 0;
		//new_width = (int)(CHARS_PER_LINE * 10);// + SIZE_ICON;
		//new_width = 515;

		if (strSize < CHARS_PER_LINE) {
			// case 1: the quote is a short one-liner, type the author name on the second line.
			numWhiteSpace = (int)(strSize - nameSize);

			//new_width = (int)(strSize * 10);// + SIZE_ICON + 17;

			new_height = BASE_HEIGHT + HEIGHT_PER_LINE * 2;

			str += System.lineSeparator();//"\n";

			//System.out.println("Case 1 : quote can fit one line ");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);

		}
		else if (remaining > nameSize) {
			// case 2: the author name can be fit to the last line with the quote.
			numWhiteSpace = (int)(remaining - nameSize);

			//new_width = (int)((numWhiteSpace + nameSize) * 7.818);
			//WIDTH = 40 + new_width;

			new_height = BASE_HEIGHT + HEIGHT_PER_LINE * numLines;

			//System.out.println("Case 2 : last line can fit author's name");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
		}
		else {
			// case 3: author name must be on its own line
			numWhiteSpace = (int)(CHARS_PER_LINE - nameSize - 3);

			//new_width = (int)((numWhiteSpace + nameSize) * 7.818);
			//WIDTH = 40 + new_width;

			new_height = BASE_HEIGHT + HEIGHT_PER_LINE * (numLines + 1);

			str += System.lineSeparator();//"\n";
			//System.out.println("Case 3 : last line cannot fit author's name");
			//System.out.println("# of whitespaces inserted b4 author's name : " + numWhiteSpace);
		}
*/

		//System.out.println("# of remaining whitespaces in the last line: " + remaining);
		//System.out.println("# of chars in the quote : " + strSize);
		//System.out.println("# of lines : " + numLines);
		//System.out.println("CHARS_PER_LINE : " + CHARS_PER_LINE);
		//System.out.println("height in px : " + height);
		//System.out.println("WIDTH in px : " + WIDTH);

		StringBuffer nameLine = new StringBuffer (System.lineSeparator());
		//for (int i = 0; i < 3; i++)
		nameLine.append("\t\t");
		nameLine.append("- ").append(name);
		str += nameLine;

        //notifier.setPopupLifetime(new Duration(POPUP_IN_MILLISECONDS));
        QNotification.Notifier.setNotificationOwner(stage);
        QNotification.Notifier.setPane(mainScene.getAnchorPane());
        notifier.setPopupLocation(stage, Pos.TOP_RIGHT);
        //QNotification.Notifier.setHeight(new_height);
        //QNotification.Notifier.setWidth(new_width);
		//Notification n0 = new NotificationFX("QUOTATION", quoteString, QUOTE_ICON)
		notifier.notify("QUOTATION", str, QNotification.QUOTE_ICON); //INFO_ICON);

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


    public int getWidth() {
    	return new_width;
    }

	public void destroy() {

	    quotations.clear();
	    quotations = null;
	    notifier = null;
	    quoteArray = null;
	    q = null;
	}

}