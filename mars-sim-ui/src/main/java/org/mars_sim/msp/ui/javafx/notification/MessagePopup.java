/**
 * Mars Simulation Project
 * MessagePopup.java
 * @version 3.1.0 2017-09-14
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.notification;


import javafx.stage.Stage;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.util.Duration;


/** 
 * The Message class creates a quotation in proper format for use by MainScene
 */
public class MessagePopup  {

	private static final int SIZE_ICON = 64;
	private static final int BASE_HEIGHT = 60;
	private static final int HEIGHT_PER_LINE = 20;
	private static final int WIDTH = 300;
	private static final int CHARS_PER_LINE = 30;
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

    public int numPopups() {
        // Note: (NOT WORKING) popups.size() is always zero no matter what.
    	return notifier.numPopups();
    }

    //public boolean isOn() {
    //	return notifier.getIsOn();
    //}

    public void popAMessage(String title, String str, String name, Stage stage, Pos location, Image IMAGE) {
    	//System.out.println("calling popAMessage()");
/*
    	//notifier = Notification.Notifier.INSTANCE;
		//msg = "\"" + wrap(msg, CHARS_PER_LINE-1) + "\"";
		str = wrap(str, CHARS_PER_LINE-1);

		int strSize = str.length();
		int numLines = (int)Math.ceil((double)strSize/CHARS_PER_LINE);

		int nameSize = 0;

		if (name != null)
			nameSize = name.length() + 3;
		else
			nameSize = 0;

		//int remaining = CHARS_PER_LINE * numLines - strSize;
		int index = str.lastIndexOf(System.lineSeparator());//"\n");
		String s = str.substring(index+1, strSize);
		int lastLineLength = s.length();
		int remaining = CHARS_PER_LINE - lastLineLength;

		int numWhiteSpace = 0;
		int new_height = 0;
		int new_width = WIDTH;

		if (strSize < CHARS_PER_LINE) {
			// case 1: the quote is a short one-liner, type the author name on the second line.
			numWhiteSpace = (int)(strSize - nameSize);

			new_width = (int)(strSize * 8) + SIZE_ICON + 15;

			new_height = BASE_HEIGHT + HEIGHT_PER_LINE;

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


		StringBuffer nameLine = new StringBuffer ("");
		for (int i = 0; i < numWhiteSpace; i++)
			nameLine.append(" ");

		if (name != null)
			nameLine.append("- ").append(name);

		str = System.lineSeparator() + str + nameLine;
*/
        //Duration duration = new Duration(POPUP_IN_MILLISECONDS);
		//notifier.setPopupLifetime(duration);

        notifier.setPopupLifetime(Duration.ZERO);//INDEFINITE);//duration);
        PNotification.Notifier.setNotificationOwner(stage);
        notifier.setPopupLocation(stage, location);
        //PNotification.Notifier.setHeight(new_height);
        PNotification.Notifier.setWidth(100);
        //System.out.println("wait time : " + notifier.getPopupLifetime().toSeconds() + " secs");
        notifier.notify(title, str, IMAGE); //INFO_ICON);
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