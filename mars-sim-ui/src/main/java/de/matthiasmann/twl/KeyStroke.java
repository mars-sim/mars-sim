/*
 * Copyright (c) 2008-2010, Matthias Mann
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Matthias Mann nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.TextUtil;
import java.util.Locale;

/**
 * A class to represent a key stroke and it's associated action.
 *
 * NOTE: equals() and hashCode() do NOT check the action.
 *
 * @author Matthias Mann
 */
public final class KeyStroke {

    private static final int SHIFT = 1;
    private static final int CTRL = 2;
    private static final int META = 4;
    private static final int ALT = 8;
    private static final int CMD = 20;  // special: CMD is LMETA so META is also set ...
    
    private final int modifier;
    private final int keyCode;
    private final char keyChar;
    private final String action;

    private KeyStroke(int modifier, int keyCode, char keyChar, String action) {
        this.modifier = modifier;
        this.keyCode = keyCode;
        this.keyChar = keyChar;
        this.action = action;
    }

    /**
     * Returns the action name for this key stroke
     * @return the action name
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the key stroke in parsable form
     * @return the key stroke
     * @see #parse(java.lang.String, java.lang.String)
     */
    public String getStroke() {
        StringBuilder sb = new StringBuilder();
        if((modifier & SHIFT) == SHIFT) {
            sb.append("shift ");
        }
        if((modifier & CTRL) == CTRL) {
            sb.append("ctrl ");
        }
        if((modifier & ALT) == ALT) {
            sb.append("alt ");
        }
        if((modifier & CMD) == CMD) {
            sb.append("cmd ");
        } else if((modifier & META) == META) {
            sb.append("meta ");
        }
        if(keyCode != Event.KEY_NONE) {
            sb.append(Event.getKeyNameForCode(keyCode));
        } else {
            sb.append("typed ").append(keyChar);
        }
        return sb.toString();
    }

    /**
     * Two KeyStroke objects are equal if the have the same key stroke, it does not compare the action.
     *
     * @param obj the other object to compare against
     * @return true if the other object is a KeyStroke and responds to the same input event
     * @see #getStroke()
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof KeyStroke) {
            final KeyStroke other = (KeyStroke)obj;
            return (this.modifier == other.modifier) &&
                    (this.keyCode == other.keyCode) &&
                    (this.keyChar == other.keyChar);
        }
        return false;
    }

    /**
     * Computes the hash code for this key stroke without the action.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.modifier;
        hash = 83 * hash + this.keyCode;
        hash = 83 * hash + this.keyChar;
        return hash;
    }

    /**
     * Parses a key stroke from string representation.<p>
     * The following syntax is supported:<ul>
     * <li>{@code <modifiers>* <keyName>}</li>
     * <li>{@code <modifiers>* typed <character>}</li>
     * </ul>
     * Thw folloiwng modifiers are supported;<ul>
     * <li>{@code ctrl}</li>
     * <li>{@code shift}</li>
     * <li>{@code meta}</li>
     * <li>{@code alt}</li>
     * <li>{@code cmd}</li>
     * </ul>
     * All matching is case insensitive.
     * 
     * @param stroke the key stroke
     * @param action the action to associate
     * @return the parsed KeyStroke
     * @throws IllegalArgumentException if the key stroke can't be parsed
     * @see Keyboard#getKeyIndex(java.lang.String)
     */
    public static KeyStroke parse(String stroke, String action) {
        if(stroke == null) {
            throw new NullPointerException("stroke");
        }
        if(action == null) {
            throw new NullPointerException("action");
        }

        int idx = TextUtil.skipSpaces(stroke, 0);
        int modifers = 0;
        char keyChar = Event.CHAR_NONE;
        int keyCode = Event.KEY_NONE;
        boolean typed = false;
        boolean end = false;

        while(idx < stroke.length()) {
            int endIdx = TextUtil.indexOf(stroke, ' ', idx);
            String part = stroke.substring(idx, endIdx);

            if(end) {
                throw new IllegalArgumentException("Unexpected: " + part);
            }
            
            if(typed) {
                if(part.length() != 1) {
                    throw new IllegalArgumentException("Expected single character after 'typed'");
                }
                keyChar = part.charAt(0);
                if(keyChar == Event.CHAR_NONE) {
                    throw new IllegalArgumentException("Unknown character: " + part);
                }
                end = true;
            } else if("ctrl".equalsIgnoreCase(part) || "control".equalsIgnoreCase(part)) {
                modifers |= CTRL;
            } else if("shift".equalsIgnoreCase(part)) {
                modifers |= SHIFT;
            } else if("meta".equalsIgnoreCase(part)) {
                modifers |= META;
            } else if("cmd".equalsIgnoreCase(part)) {
                modifers |= CMD;
            } else if("alt".equalsIgnoreCase(part)) {
                modifers |= ALT;
            } else if("typed".equalsIgnoreCase(part)) {
                typed = true;
            } else {
                keyCode = Event.getKeyCodeForName(part.toUpperCase(Locale.ENGLISH));
                if(keyCode == Event.KEY_NONE) {
                    throw new IllegalArgumentException("Unknown key: " + part);
                }
                end = true;
            }

            idx = TextUtil.skipSpaces(stroke, endIdx+1);
        }

        if(!end) {
            throw new IllegalArgumentException("Unexpected end of string");
        }

        return new KeyStroke(modifers, keyCode, keyChar, action);
    }

    /**
     * Creates a KeyStroke from the KEY_PRESSED event.
     *
     * @param event the input event
     * @param action the action to associate
     * @return the KeyStroke for this event and action
     * @throws IllegalArgumentException if the event is not a Type.KEY_PRESSED
     */
    public static KeyStroke fromEvent(Event event, String action) {
        if(event == null) {
            throw new NullPointerException("event");
        }
        if(action == null) {
            throw new NullPointerException("action");
        }
        if(event.getType() != Event.Type.KEY_PRESSED) {
            throw new IllegalArgumentException("Event is not a Type.KEY_PRESSED");
        }
        int modifiers = convertModifier(event);
        return new KeyStroke(modifiers, event.getKeyCode(), Event.CHAR_NONE, action);
    }

    boolean match(Event e, int mappedEventModifiers) {
        if(mappedEventModifiers != modifier) {
            return false;
        }
        if(keyCode != Event.KEY_NONE && keyCode != e.getKeyCode()) {
            return false;
        }
        if(keyChar != Event.CHAR_NONE && (!e.hasKeyChar() || keyChar != e.getKeyChar())) {
            return false;
        }
        return true;
    }

    static int convertModifier(Event event) {
        int eventModifiers = event.getModifiers();
        int modifiers = 0;
        if((eventModifiers & Event.MODIFIER_SHIFT) != 0) {
            modifiers |= SHIFT;
        }
        if((eventModifiers & Event.MODIFIER_CTRL) != 0) {
            modifiers |= CTRL;
        }
        if((eventModifiers & Event.MODIFIER_META) != 0) {
            modifiers |= META;
        }
        if((eventModifiers & Event.MODIFIER_LMETA) != 0) {
            modifiers |= CMD;
        }
        if((eventModifiers & Event.MODIFIER_ALT) != 0) {
            modifiers |= ALT;
        }
        return modifiers;
    }
}
