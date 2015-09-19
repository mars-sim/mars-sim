/*
 * Copyright (c) 2008-2013, Matthias Mann
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
package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.Border;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.utils.AbstractMathInterpreter;
import de.matthiasmann.twl.utils.StateExpression;
import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.utils.XMLParser;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A helper class to make XML parsing easier
 *
 * @author Matthias Mann
 */
final class ParserUtil {

    private ParserUtil() {
    }

    static void checkNameNotEmpty(final String name, XMLParser xmlp) throws XmlPullParserException {
        if(name == null) {
            throw xmlp.error("missing 'name' on '" + xmlp.getName() + "'");
        }
        if(name.length() == 0) {
            throw xmlp.error("empty name not allowed");
        }
        if("none".equals(name)) {
            throw xmlp.error("can't use reserved name \"none\"");
        }
        if(name.indexOf('*') >= 0) {
            throw xmlp.error("'*' is not allowed in names");
        }
        if(name.indexOf('/') >= 0) {
            throw xmlp.error("'/' is not allowed in names");
        }
    }

    static Border parseBorderFromAttribute(XMLParser xmlp, String attribute) throws XmlPullParserException {
        String value = xmlp.getAttributeValue(null, attribute);
        if(value == null) {
            return null;
        }
        return parseBorder(xmlp, value);
    }

    static Border parseBorder(XMLParser xmlp, String value) throws XmlPullParserException {
        try {
            int values[] = TextUtil.parseIntArray(value);
            switch(values.length) {
            case 1:
                return new Border(values[0]);
            case 2:
                return new Border(values[0], values[1]);
            case 4:
                return new Border(values[0], values[1], values[2], values[3]);
            default:
                throw xmlp.error("Unsupported border format");
            }
        } catch (NumberFormatException ex) {
            throw xmlp.error("Unable to parse border size", ex);
        }
    }

    static Color parseColorFromAttribute(XMLParser xmlp, String attribute, ParameterMapImpl constants, Color defaultColor) throws XmlPullParserException {
        String value = xmlp.getAttributeValue(null, attribute);
        if(value == null) {
            return defaultColor;
        }
        return parseColor(xmlp, value, constants);
    }

    static Color parseColor(XMLParser xmlp, String value, ParameterMapImpl constants) throws XmlPullParserException {
        try {
            Color color = Color.parserColor(value);
            if(color == null && constants != null) {
                color = constants.getParameterValue(value, false, Color.class);
            }
            if(color == null) {
                throw xmlp.error("Unknown color name: " + value);
            }
            return color;
        } catch(NumberFormatException ex) {
            throw xmlp.error("unable to parse color code", ex);
        }
    }
    
    static String appendDot(String name) {
        int len = name.length();
        if(len > 0 && name.charAt(len-1) != '.') {
            name = name.concat(".");
        }
        return name;
    }

    static int[] parseIntArrayFromAttribute(XMLParser xmlp, String attribute) throws XmlPullParserException {
        try {
            String value = xmlp.getAttributeNotNull(attribute);
            return TextUtil.parseIntArray(value);
        } catch(NumberFormatException ex) {
            throw xmlp.error("Unable to parse", ex);
        }
    }

    static int parseIntExpressionFromAttribute(XMLParser xmlp, String attribute, int defaultValue, AbstractMathInterpreter interpreter) throws XmlPullParserException {
        try {
            String value = xmlp.getAttributeValue(null, attribute);
            if(value == null) {
                return defaultValue;
            }
            if(TextUtil.isInteger(value)) {
                return Integer.parseInt(value);
            }
            Number n = interpreter.execute(value);
            if(!(n instanceof Integer)) {
                if(n.intValue() != n.doubleValue()) {
                    throw xmlp.error("Not an integer");
                }
            }
            return n.intValue();
        } catch(NumberFormatException ex) {
            throw xmlp.error("Unable to parse", ex);
        } catch(ParseException ex) {
            throw xmlp.error("Unable to parse", ex);
        }
    }
    
    static<V> SortedMap<String,V> find(SortedMap<String,V> map, String baseName) {
        return map.subMap(baseName, baseName.concat("\uFFFF"));
    }

    static<V> Map<String,V> resolve(SortedMap<String,V> map, String ref, String name, V mapToNull) {
        name = ParserUtil.appendDot(name);
        int refLen = ref.length() - 1;
        ref = ref.substring(0, refLen);

        SortedMap<String,V> matched = find(map, ref);
        if(matched.isEmpty()) {
            return matched;
        }

        HashMap<String, V> result = new HashMap<String, V>();
        for(Map.Entry<String, V> texEntry : matched.entrySet()) {
            String entryName = texEntry.getKey();
            assert entryName.startsWith(ref);
            V value = texEntry.getValue();
            if(value == mapToNull) {
                value = null;
            }
            result.put(name.concat(entryName.substring(refLen)), value);
        }

        return result;
    }
    
    static StateExpression parseCondition(XMLParser xmlp) throws XmlPullParserException {
        String expression = xmlp.getAttributeValue(null, "if");
        boolean negate = expression == null;
        if(expression == null) {
            expression = xmlp.getAttributeValue(null, "unless");
        }
        if(expression != null) {
            try {
                return StateExpression.parse(expression, negate);
            } catch(ParseException ex) {
                throw xmlp.error("Unable to parse condition", ex);
            }
        }
        return null;
    }

}
