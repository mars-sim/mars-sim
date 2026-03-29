/*
 * Mars Simulation Project
 * ConsolePanel.java
 * @author Barry Evans
 * @date 2026-03-23
 */
package com.mars_sim.ui.swing.tool.console;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;

import com.mars_sim.console.chat.Conversation;
import com.mars_sim.console.chat.ConversationRole;
import com.mars_sim.console.chat.UserChannel;
import com.mars_sim.console.chat.UserOutbound;
import com.mars_sim.console.chat.simcommand.TopLevel;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.UIContext;

/**
 * A simple terminal-style panel backed by a single {@link JTextArea}.
 * User input is constrained to the active line at the bottom.
 */
@SuppressWarnings("serial")
public class ConsolePanel extends ContentPanel {
    
    public static final String NAME = "console";
    public static final String TITLE = "Console";
    public static final String ICON = "action/console";
    
    private static final char DELETE_CHAR = (char)127;
    private static final char BACKSPACE_CHAR = (char)8;
    private static final char UP_ARROW_CHAR = '\uE000';
    private static final char DOWN_ARROW_CHAR = '\uE001';

    private JTextArea textArea;
    private int inputStart;
    private boolean bypassDocumentInputRestriction;
    private Conversation conversation;
    private Thread consoleThread;
    private TextAreaChannel channel;

    public ConsolePanel(UIContext context, Properties toolProps) {
        super(NAME, TITLE, Placement.BOTTOM);

        buildUI();

        channel = new TextAreaChannel();

        Set<ConversationRole> roles = new HashSet<>();
        roles.add(ConversationRole.ADMIN);
        conversation = new Conversation(channel, new TopLevel(), roles,
                        context.getSimulation());
        
        // Run the conversation in a Virtual thread because it is a blocking operation
        consoleThread = Thread.ofVirtual().name("Console").start(() ->
            conversation.interact()
        );
    }

    private void buildUI() {
        
        textArea = new JTextArea();
        textArea.setEditable(true);
        textArea.setLineWrap(false);
        textArea.setWrapStyleWord(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));

        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new TerminalDocumentFilter());

        // Process all key clicks
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Only accept input if it's after the input start.
                if (e.isConsumed() || textArea.getCaretPosition() < inputStart) {
                    moveCaretToEnd();
                }

                char character = e.getKeyChar();
                if (character == KeyEvent.CHAR_UNDEFINED) {
                    return;
                }

                channel.characterTyped(character);
            }
        });

        var inputMap = textArea.getInputMap();
        var actionMap = textArea.getActionMap();

        // Handle up and down keys for input history navigation
        inputMap.put(KeyStroke.getKeyStroke("UP"), "terminal-up");
        actionMap.put("terminal-up", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                channel.characterTyped(UP_ARROW_CHAR);
            }
        });
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "terminal-down");
        actionMap.put("terminal-down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                channel.characterTyped(DOWN_ARROW_CHAR);
            }
        });

        // Consume left and right keys
        var consumeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Do nothing - just consume the event to prevent caret movement
            }
        };
        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "terminal-left");
        actionMap.put("terminal-left", consumeAction);
        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "terminal-right");
        actionMap.put("terminal-right", consumeAction);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        Dimension dim = new Dimension(700, 300);
        //setMinimumSize(dim);
        setPreferredSize(dim);
    }

    @Override
    public void destroy() {
        if (consoleThread != null && consoleThread.isAlive()) {
            conversation.setCompleted();
            consoleThread.interrupt();
        }

        super.destroy();
    }

    private void appendOutput(String output) {
        if ((output == null) || output.isEmpty()) {
            return;
        }
        
        moveCaretToEnd();
        runBypassingDocumentInputRestriction(() -> textArea.append(output));
    }

    /**
     * Move the caret to the end of the document and scroll to make it visible.
     */
    private void moveCaretToEnd() {
        // Always move to the end.
        int endPosition = textArea.getDocument().getLength();
        textArea.setCaretPosition(endPosition);

        try {
            var caretView = textArea.modelToView2D(endPosition);

            if (caretView != null) {
                textArea.scrollRectToVisible(caretView.getBounds());
            }
        }
        catch (BadLocationException e) {
            // Ignore invalid caret locations
        }
    }

    /**
     * Check if the character is a restricted input character that should not be added to the input buffer, but should still be passed to the channel for hotkey processing.
     * @param character Inbound character
     * @return is restricted.
     */
    private boolean isRestrictedInputCharacter(char character) {
        return (character != '\n' && Character.isISOControl(character))  
                || character == '\t';
    }

    private String removeRestrictedInputCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder filtered = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (!isRestrictedInputCharacter(character)) {
                filtered.append(character);
            }
        }
        return filtered.toString();
    }

    private void runBypassingDocumentInputRestriction(Runnable action) {
        boolean previous = bypassDocumentInputRestriction;
        bypassDocumentInputRestriction = true;
        try {
            action.run();
        }
        finally {
            bypassDocumentInputRestriction = previous;
        }
    }

    /**
     * A UserChannel implementation backed by the console's JTextArea. User input is read character-by-character and buffered
     * until a newline is entered, at which point the full line of input is returned.
     * Restricted characters are not added to the buffer, but are still passed to the channel for hotkey processing.
     * Output is appended to the text area.
     */
    private class TextAreaChannel implements UserChannel {

        private BlockingQueue<Character> charQueue = new LinkedBlockingDeque<>();
        private String buffer;
        private int userEntered = 0;
        private Map<String, UserOutbound> hotkeys = new HashMap<>();

        @Override
        public void close() {
            // No resources to clean up
        }

        private static String convertToKeyStroke(char ch) {
            return switch (ch) {
                case '\t' -> "tab";
                case UP_ARROW_CHAR -> "up";
                case DOWN_ARROW_CHAR -> "down";
                default -> null;
            };
        }

        @Override
        public String getInput(String prompt) {
            appendOutput(prompt);

            // Remember the start of the user input so we can restrict editing
            inputStart = textArea.getCaretPosition();

            userEntered = 0;
            buffer = "";
            try {
                while (true) {
                    Character character = charQueue.take();

                    var keyStroke = convertToKeyStroke(character);
                    if (keyStroke != null) {
                        UserOutbound handler = hotkeys.get(keyStroke);
                        if (handler != null) {
                            handler.keyStrokeApplied(keyStroke);
                            continue;
                        }
                    }

                    // No key stroke so consume
                    if (character == '\n') {
                        break;
                    }
                    else if (character == DELETE_CHAR || character == BACKSPACE_CHAR) {
                        if (buffer.length() > 0) {
                            buffer = buffer.substring(0, buffer.length() - 1);
                        }
                    }
                    else if (!Character.isISOControl(character)) {
                        buffer += character;
                    }
                    userEntered = buffer.length();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }

            return buffer;
        }

        @Override
        public void println(String text) {
            appendOutput(text + "\n");
        }

        @Override
        public void print(String text) {
            appendOutput(text);
        }

        @Override
        public boolean registerHandler(String keyStroke, UserOutbound listener, boolean interuptExecution) {
            hotkeys.put(keyStroke.toLowerCase(), listener);
            return true;
        }

        @Override
        public String getPartialInput() {
            return buffer.substring(0, userEntered);
        }

        @Override
        public void replaceUserInput(String replacement) {
            String nextBuffer = (replacement == null ? "" : replacement);
            int previousBufferLength = (buffer == null ? 0 : buffer.length());

            runBypassingDocumentInputRestriction(() -> {
                int documentLength = textArea.getDocument().getLength();
                int replaceStart = Math.max(inputStart, documentLength - previousBufferLength);

                textArea.replaceRange("", replaceStart, documentLength);
                textArea.insert(nextBuffer, replaceStart);
            });

            buffer = nextBuffer;
        }

        public void characterTyped(Character character) {
            try {
                charQueue.put(character);
            } catch (InterruptedException e) {
                // Restore interrupt status and exit without noisy logging
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * Document filter to restrict user input to the active line and prevent restricted characters from being entered.
     */
    private class TerminalDocumentFilter extends DocumentFilter {

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (offset < inputStart) {
                return;
            }

            if (bypassDocumentInputRestriction) {
                super.insertString(fb, offset, string, attr);
                return;
            }

            String filtered = removeRestrictedInputCharacters(string);
            if (filtered == null || filtered.isEmpty()) {
                return;
            }
            super.insertString(fb, offset, filtered, attr);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (offset < inputStart) {
                return;
            }
            
            // Prevent deletion beyond the input start boundary
            int documentLength = fb.getDocument().getLength();
            int userInputLength = documentLength - inputStart;
            
            if (userInputLength <= 0) {
                // No user input to delete
                return;
            }
            
            // Limit the deletion to not go beyond the input start
            int adjustedOffset = Math.max(offset, inputStart);
            int maxRemovable = documentLength - inputStart;
            int adjustedLength = Math.min(length, maxRemovable);
            
            if (adjustedLength <= 0) {
                return;
            }
            
            super.remove(fb, adjustedOffset, adjustedLength);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (offset < inputStart) {
                return;
            }

            if (bypassDocumentInputRestriction) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            String filtered = removeRestrictedInputCharacters(text);
            if (filtered == null || filtered.isEmpty()) {
                return;
            }
            super.replace(fb, offset, length, filtered, attrs);
        }
    }
}