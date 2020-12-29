package org.mars.sim.console.chat;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.mars.sim.console.chat.simcommand.TopLevel;

public class ConsoleApp {
	
	public static void main(String... args) {
    
		UserChannel channel = null;
		
		if ((args.length > 1) && args[1].equals("--swing")) {
			TextIO term =  TextIoFactory.getTextIO();
			channel = new TextIOChannel(term);
		}
		else {
			channel = new StreamChannel(System.in, System.out);
		}
		
        Conversation conversation = new Conversation(channel, new TopLevel(), null);
        conversation.interact();
        
    }

}
