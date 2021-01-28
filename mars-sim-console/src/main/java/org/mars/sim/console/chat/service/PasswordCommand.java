package org.mars.sim.console.chat.service;

import org.mars.sim.console.chat.ChatCommand;
import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.ConversationRole;
import org.mars.sim.console.chat.simcommand.TopLevel;

/**
 * A stateless command to change a remote user's password.
 * If the conversation has ADMIN right any user can be changed.
 */
public class PasswordCommand extends ChatCommand {

	public static final ChatCommand PASSWORD = new PasswordCommand();

	public PasswordCommand() {
		super(TopLevel.SIMULATION_GROUP, "pa", "password", "Change the password of user");
		
		setInteractive(true);
	}

	@Override
	public boolean execute(Conversation context, String input) {
        SSHConversation sshConv = (SSHConversation) context;
		String targetUserName = sshConv.getUsername();
		if (context.getRoles().contains(ConversationRole.ADMIN) && !input.isBlank()) {
			targetUserName = input;
		}
		
		String toExit = context.getInput("Change the password for " + targetUserName + " (Y/N)?");
        if ("Y".equalsIgnoreCase(toExit)) {
        	Credentials cred = sshConv.getService().getCredentials();
        	
        	// Changing current user then have to confirm current password
        	if (targetUserName.equals(sshConv.getUsername())) {
        		String oldPassword = sshConv.getInput("Enter existing password > ");
            	if (!cred.authenticate(targetUserName, oldPassword)) {
                    context.println("Existing password is wrong");
                    return false;
            	}
        	}
        	
        	String newPassword1 = sshConv.getInput("Enter new password > ");
        	String newPassword2 = sshConv.getInput("Repeat new password > ");
        	if (newPassword1.isBlank()) {
                context.println("Passwords cannot be blank");
        	}
        	else if (!newPassword1.equals(newPassword2)){
                context.println("Passwords don't match");
        	}
        	else if (!cred.setPassword(targetUserName, newPassword1)) {
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
