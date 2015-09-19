/*
 * Copyright (c) 2008-2009, Matthias Mann
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

import de.matthiasmann.twl.utils.XMLParser;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * A immutable InputMap class. It maps key strokes to action names.
 *
 * @author Matthias Mann
 */
public final class InputMap {

    private static final InputMap EMPTY_MAP = new InputMap(new KeyStroke[0]);
    
    private final KeyStroke[] keyStrokes;

    private InputMap(KeyStroke[] keyStrokes) {
        this.keyStrokes = keyStrokes;
    }

    /**
     * Maps the given key event to an action.
     * @param event the key event
     * @return the action or null if no mapping was found
     */
    public String mapEvent(Event event) {
        if(event.isKeyEvent()) {
            int mappedEventModifiers = KeyStroke.convertModifier(event);
            for(KeyStroke ks : keyStrokes) {
                if(ks.match(event, mappedEventModifiers)) {
                    return ks.getAction();
                }
            }
        }
        return null;
    }

    /**
     * Creates a new InputMap containing both the current and the new KeyStrokes.
     * If the new key strokes contain already mapped key strokes then the new mappings will replace the old mappings.
     *
     * @param newKeyStrokes the new key strokes.
     * @return the InputMap containing the resulting mapping
     */
    public InputMap addKeyStrokes(LinkedHashSet<KeyStroke> newKeyStrokes) {
        int size = newKeyStrokes.size();
        if(size == 0) {
            return this;
        }
        
        KeyStroke[] combined = new KeyStroke[keyStrokes.length + size];
        newKeyStrokes.toArray(combined);   // copy new key strokes
        for(KeyStroke ks : keyStrokes) {
            if(!newKeyStrokes.contains(ks)) {  // append old ones if they have not been replaced
                combined[size++] = ks;
            }
        }
        
        return new InputMap(shrink(combined, size));
    }

    /**
     * Creates a new InputMap containing both the current and the new KeyStrokes from another InputMap.
     * If the new key strokes contain already mapped key strokes then the new mappings will replace the old mappings.
     *
     * @param map the other InputMap containing the new key strokes.
     * @return the InputMap containing the resulting mapping
     */
    public InputMap addKeyStrokes(InputMap map) {
        if(map == this || map.keyStrokes.length == 0) {
            return this;
        }
        if(keyStrokes.length == 0) {
            return map;
        }
        return addKeyStrokes(new LinkedHashSet<KeyStroke>(Arrays.asList(map.keyStrokes)));
    }

    /**
     *
     * Creates a new InputMap containing both the current and the new KeyStroke.
     * If the specified key stroke is already mapped then the new mapping will replace the old mapping.
     *
     * @param keyStroke the new key stroke.
     * @return the InputMap containing the resulting mapping
     */
    public InputMap addKeyStroke(KeyStroke keyStroke) {
        LinkedHashSet<KeyStroke> newKeyStrokes = new LinkedHashSet<KeyStroke>(1, 1.0f);
        newKeyStrokes.add(keyStroke);
        return addKeyStrokes(newKeyStrokes);
    }

    /**
     * Remove key strokes from this mapping
     *
     * @param keyStrokes the key strokes to remove
     * @return the InputMap containing the resulting mapping
     */
    public InputMap removeKeyStrokes(Set<KeyStroke> keyStrokes) {
        if(keyStrokes.isEmpty()) {
            return this;
        }

        int size = 0;
        KeyStroke[] result = new KeyStroke[this.keyStrokes.length];
        for(KeyStroke ks : this.keyStrokes) {
            if(!keyStrokes.contains(ks)) {  // append old ones if it has not been removed
                result[size++] = ks;
            }
        }

        return new InputMap(shrink(result, size));
    }

    /**
     * Returns all key strokes in this InputMap.
     * @return all key strokes in this InputMap.
     */
    public KeyStroke[] getKeyStrokes() {
        return keyStrokes.clone();
    }

    /**
     * Returns an empty input mapping
     * @return an empty input mapping
     */
    public static InputMap empty() {
        return EMPTY_MAP;
    }
    
    /**
     * Parses a stand alone &lt;inputMapDef&gt; XML file
     *
     * @param url the URL ton the XML file
     * @return the parsed key strokes
     * @throws IOException if an IO related error occured
     */
    public static InputMap parse(URL url) throws IOException {
        try {
            XMLParser xmlp = new XMLParser(url);
            try {
                xmlp.require(XmlPullParser.START_DOCUMENT, null, null);
                xmlp.nextTag();
                xmlp.require(XmlPullParser.START_TAG, null, "inputMapDef");
                xmlp.nextTag();
                LinkedHashSet<KeyStroke> keyStrokes = parseBody(xmlp);
                xmlp.require(XmlPullParser.END_TAG, null, "inputMapDef");
                return new InputMap(keyStrokes.toArray(new KeyStroke[keyStrokes.size()]));
            } finally {
                xmlp.close();
            }
        } catch(XmlPullParserException ex) {
            throw (IOException)(new IOException("Can't parse XML").initCause(ex));
        }
    }

    /**
     * Writes this input map into a XML file which can be parsed by {@link #parse(java.net.URL)}.
     * The encoding is UTF8
     *
     * @param os the output where the XML will be written to
     * @throws IOException if an IO error occured
     * @see #parse(java.net.URL) 
     */
    public void writeXML(OutputStream os) throws IOException {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlSerializer serializer = factory.newSerializer();
            serializer.setOutput(os, "UTF8");
            serializer.startDocument("UTF8", Boolean.TRUE);
            serializer.text("\n");
            serializer.startTag(null, "inputMapDef");
            for(KeyStroke ks : keyStrokes) {
                serializer.text("\n    ");
                serializer.startTag(null, "action");
                serializer.attribute(null, "name", ks.getAction());
                serializer.text(ks.getStroke());
                serializer.endTag(null, "action");
            }
            serializer.text("\n");
            serializer.endTag(null, "inputMapDef");
            serializer.endDocument();
        } catch(XmlPullParserException ex) {
            throw (IOException)(new IOException("Can't generate XML").initCause(ex));
        }
    }

    /**
     * Parses the child elemets of the current XML tag as input map.
     * This method is only public so that it can be called from ThemeManager.
     *
     * @param xmlp the XML parser
     * @return the found key strokes
     * @throws XmlPullParserException if a parser error occured
     * @throws IOException if an IO error occured
     */
    public static LinkedHashSet<KeyStroke> parseBody(XMLParser xmlp) throws XmlPullParserException, IOException {
        LinkedHashSet<KeyStroke> newStrokes = new LinkedHashSet<KeyStroke>();
        while(!xmlp.isEndTag()) {
            xmlp.require(XmlPullParser.START_TAG, null, "action");
            String name = xmlp.getAttributeNotNull("name");
            String key = xmlp.nextText();
            try {
                KeyStroke ks = KeyStroke.parse(key, name);
                if(!newStrokes.add(ks)) {
                    Logger.getLogger(InputMap.class.getName()).log(Level.WARNING, "Duplicate key stroke: {0}", ks.getStroke());
                }
            } catch (IllegalArgumentException ex) {
                throw xmlp.error("can't parse Keystroke", ex);
            }
            xmlp.require(XmlPullParser.END_TAG, null, "action");
            xmlp.nextTag();
        }
        return newStrokes;
    }
    
    private static KeyStroke[] shrink(KeyStroke[] keyStrokes, int size) {
        if(size != keyStrokes.length) {
            KeyStroke[] tmp = new KeyStroke[size];
            System.arraycopy(keyStrokes, 0, tmp, 0, size);
            keyStrokes = tmp;
        }
        return keyStrokes;
    }
}
