package org.mars.sim.console.chat.service;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.simcommand.TopLevel;

/**
 * A stateless command that exit and leaves the corrent Conversation
 */
public class PasswordCommand extends ChatCommand {

	public static final ChatCommand PASSWORD = new PasswordCommand();

	public PasswordCommand() {
		super(TopLevel.SIMULATION_GROUP, "pa", "password", "Change the password of current user");
		
		setInteractive(true);
	}

	@Override
	public boolean execute(Conversation context, String input) {
		String toExit = context.getInput("Change the password (Y/N)?");
        SSHConversation sshConv = (SSHConversation) context;
        if ("Y".equalsIgnoreCase(toExit)) {
        	String oldPassword = sshConv.getInput("Enter existing password > ");
        	String newPassword1 = sshConv.getInput("Enter new password > ");
        	String newPassword2 = sshConv.getInput("Repeat new password > ");

        	
        	Credentials cred = sshConv.getService().getCredentials();
        	if (!cred.authenticate(sshConv.getUsername(), oldPassword)) {
                context.println("Existing password is wrong");
        	}
        	else if (!newPassword1.equals(newPassword2)){
                context.println("Passwords don't match");
        	}
        	else if (!cred.setPassword(sshConv.getUsername(), newPassword1)) {
                context.println("Problem saving new password");
        	}
        	else {
                context.println("Password changed");
        	}
        }
        else {
        	context.println("OK, exit skipped");
        }
        return true;

	}

}
