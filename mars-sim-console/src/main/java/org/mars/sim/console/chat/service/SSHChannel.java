package org.mars.sim.console.chat.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;

import org.mars.sim.console.chat.Conversation;
import org.mars.sim.console.chat.UserChannel;
import org.mars.sim.console.chat.UserOutbound;
import org.mars.sim.console.chat.simcommand.TopLevel;
import org.mars_sim.msp.core.Simulation;

public class SSHChannel implements UserChannel, Command {
	
	/**
	 * Runnable that drives the blockable Conversation
	 */
	private final class ConversationThread implements Runnable {  
		@Override
	    public void run() {
            try {
                conv.interact();
            } catch (Exception e) {
                callback.onExit(-1, e.getMessage());
                return;
            }
            LOGGER.info("Conversation ended" + conv);
	        callback.onExit(0);
	    }
	}
	
	private final static Logger LOGGER = Logger.getLogger(SSHChannel.class.getName());

	private PrintStream out;
	private ChannelSession channel;
	private InputStreamReader in;
	private ExitCallback callback;
	private ExecutorService executor;
	
	private Conversation conv;

	private int userInputIdx;
	private char[] userInput;

	private Map<String, UserOutbound> hotkeys;

	private String onscreenReplacement = null;

	private Simulation sim;

	
	/**
	 * Use the parent executor
	 * @param executor
	 */
	SSHChannel(ExecutorService executor, Simulation sim) {
		this.executor = executor;
		this.sim = sim;
		this.hotkeys = new HashMap<>();
	}

	@Override
	public String getInput(String prompt) {
		print(prompt);
		
		userInput = new char[128];
		userInputIdx = 0;
		boolean inControl = false;
        boolean completed = false;
        char [] buffer = new char[10];
        int bufferIdx = 0;
        while (!completed) {
			try {
		       	int i = in.read();
	
		        if (i < 0) {
		        	LOGGER.warning("Input ended");
		        	completed = true;
		        }
		        else {
					char ch = (char)i;	
					
					// In case we lose out synch
					if (inControl && (bufferIdx == 3)) {
						// GOne too far
						inControl = false;
					}
					buffer[bufferIdx++] = ch;
					
					// Check not a hot key
					UserOutbound listener = null;
					String hotkey = convertToKeyStroke(buffer, bufferIdx);
					if (hotkey != null) {
						// Is there a listener
						listener = hotkeys.get(hotkey);
					}
					if (listener != null) {
						listener.keyStrokeApplied(convertToKeyStroke(buffer, bufferIdx));
						inControl = false;
						bufferIdx = 0;
					}
					else if (!inControl)
					{
						// If there is a onscreen replacement; then accept it as entered input as user has pressed a key
						if (onscreenReplacement != null) {
							onscreenReplacement.getChars(0, onscreenReplacement.length(), userInput, 0);
							userInputIdx = onscreenReplacement.length();
							onscreenReplacement = null;
						}
						// Process user input
						if (ch == 127) {
							if (userInputIdx >= 1) {
								out.append(ch).flush();
								userInputIdx--;
							}
							bufferIdx = 0;
						}
						else if (ch == '\r') {
							out.append('\r').append('\n').flush();
							completed = true;
						}
						else if (Character.isISOControl(ch)) {
							System.out.println("Control character seen");
							inControl = true;
						}
						else {
							out.append(ch).flush();
							userInput[userInputIdx++] = ch;
							bufferIdx = 0;
						}
			        }
				}
			} catch (IOException e) {
				e.printStackTrace();
				completed = true;
			}
        }
		return String.valueOf(userInput, 0, userInputIdx);
	}

	@Override
	public void println(String text) {
		out.println(text);
	}

	@Override
	public void print(String text) {
		out.print(text);
		out.flush();
	}

	@Override
	public void close() {
		channel.close(false);
		channel = null;
	}

	@Override
	public boolean registerHandler(String keyStroke, UserOutbound listener, boolean interuptKey) {

		LOGGER.info("Add listener for hotkey '" + keyStroke + "'");
		hotkeys.put(keyStroke.toLowerCase(), listener);
		return true;
	}

	private static String convertToKeyStroke(char [] ch, int size) {
		String key = null;
		if (size == 1) {
			if (ch[0] == '\t') {
				key = "tab";
			}
//			else if (ch[0] == ) {
//				key = "escape";
//			}
		}
		else if (size == 3) {
			if ((ch[0] == 27) && (ch[1] == 91) && (ch[2] == 65)) {
				key = "up";
			}
			else if ((ch[0] == 27) && (ch[1] == 91) && (ch[2] == 66)) {
				key = "down";
			}
		}
		return key;
	}

	@Override
	public String getPartialInput() {
		return String.valueOf(userInput, 0, userInputIdx);
	}

	@Override
	public void replaceUserInput(String replacement) {
		// got back size of Partial Input
		int backwards = (onscreenReplacement != null ? onscreenReplacement.length() : userInputIdx);
		for(int b = backwards; b > 0; b--) {
			if (b > replacement.length()) {
				out.append("\b \b");
			}
			else {
				out.append('\b');
			}
		}
		
		out.append(replacement).flush();
		onscreenReplacement = replacement;
	}

	@Override
	public void start(ChannelSession channel, Environment env) throws IOException {
		this.channel = channel;
	
		LOGGER.info("Starting conversation ");
		conv = new Conversation(this, new TopLevel(), sim);
		
		// Create a Runnable to do the conversation driving
		ConversationThread ct = new ConversationThread();
		executor.execute(ct);
	}

	@Override
	public void destroy(ChannelSession channel) throws Exception {
		channel = null;
		conv.setCompleted();
		out = null;
		in = null;
	}

	@Override
	public void setInputStream(InputStream in) {
        this.in = new InputStreamReader(in);
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = new PrintStream(out);
		
	}

	@Override
	public void setErrorStream(OutputStream err) {
		// Only using output stream
	}

	@Override
	public void setExitCallback(ExitCallback callback) {
		this.callback = callback;
	}
}