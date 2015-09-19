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
package de.matthiasmann.twl.textarea;

import de.matthiasmann.twl.model.HasCallback;
import de.matthiasmann.twl.utils.MultiStringReader;
import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.utils.XMLParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * A simple XHTML parser.
 *
 * The following tags are supported:
 * <ul>
 *  <li>{@code a}<br/>Attributes: {@code href}</li>
 *  <li>{@code p}</li>
 *  <li>{@code br}</li>
 *  <li>{@code img}<br/>Attributes: {@code src}, {@code alt}<br/>Styles: {@code float}, {@code display}, {@code width}, {@code height}</li>
 *  <li>{@code span}</li>
 *  <li>{@code div}<br/>Styles: {@code background-image}, {@code float}, {@code width}<br/>Required styles for floating blocks: {@code width}<br/>Optional styles for floating blocks: {@code height}</li>
 *  <li>{@code ul}</li>
 *  <li>{@code li}<br/>Styles: {@code list-style-image}</li>
 *  <li>{@code button}<br/>Attributes: {@code name}, {@code value}<br/>Styles: {@code float}, {@code display}, {@code width}, {@code height}</li>
 *  <li>{@code table}<br/>Attributes: {@code cellspacing}, {@code cellpadding}<br/>Required styles: {@code width}</li>
 *  <li>{@code tr}<br/>Styles: {@code height}</li>
 *  <li>{@code td}<br/>Attributes: {@code colspan}<br/>Styles: {@code width}</li>
 * </ul>
 *
 * The following generic CSS attributes are supported:
 * <ul>
 *  <li>{@code font-family}<br/>References a font in the theme</li>
 *  <li>{@code text-align}</li>
 *  <li>{@code text-ident}</li>
 *  <li>{@code margin}</li>
 *  <li>{@code margin-top}</li>
 *  <li>{@code margin-left}</li>
 *  <li>{@code margin-right}</li>
 *  <li>{@code margin-bottom}</li>
 *  <li>{@code padding}</li>
 *  <li>{@code padding-top}</li>
 *  <li>{@code padding-left}</li>
 *  <li>{@code padding-right}</li>
 *  <li>{@code padding-bottom}</li>
 *  <li>{@code clear}</li>
 *  <li>{@code vertical-align}</li>
 *  <li>{@code white-space}<br/>Only {@code normal} and {@code pre}</li>
 * </ul>
 *
 * Numeric values must use on of the following units: {@code em}, {@code ex}, {@code px}, {@code %}
 *
 * @author Matthias Mann
 */
public class HTMLTextAreaModel extends HasCallback implements TextAreaModel {
    
    private final ArrayList<Element> elements;
    private final ArrayList<String> styleSheetLinks;
    private final HashMap<String, Element> idMap;
    private String title;

    private final ArrayList<Style> styleStack;
    private final StringBuilder sb;
    private final int[] startLength;

    private ContainerElement curContainer;

    /**
     * Creates a new {@code HTMLTextAreaModel} without content.
     */
    public HTMLTextAreaModel() {
        this.elements = new ArrayList<Element>();
        this.styleSheetLinks = new ArrayList<String>();
        this.idMap = new HashMap<String, Element>();
        this.styleStack = new ArrayList<Style>();
        this.sb = new StringBuilder();
        this.startLength = new int[2];
    }

    /**
     * Creates a new {@code HTMLTextAreaModel} and parses the given html.
     * @param html the HTML to parse
     * @see #setHtml(java.lang.String)
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public HTMLTextAreaModel(String html) {
        this();
        setHtml(html);
    }

    /**
     * Creates a new {@code HTMLTextAreaModel} and parses the content of the
     * given {@code Reader}.
     *
     * @see #parseXHTML(java.io.Reader)
     * @param r the reader to parse html from
     * @throws IOException if an error occured while reading
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public HTMLTextAreaModel(Reader r) throws IOException {
        this();
        parseXHTML(r);
    }

    /**
     * Sets the a html to parse.
     * 
     * @param html the html.
     */
    public void setHtml(String html) {
        Reader r;
        if(isXHTML(html)) {
            r = new StringReader(html);
        } else {
            r = new MultiStringReader("<html><body>", html, "</body></html>");
        }
        parseXHTML(r);
    }

    /**
     * Reads HTML from the given {@code Reader}.
     *
     * @param r the reader to parse html from
     * @throws IOException if an error occured while reading
     * @see #setHtml(java.lang.String)
     * @deprecated use {@link #parseXHTML(java.io.Reader)}
     */
    public void readHTMLFromStream(Reader r) throws IOException {
        parseXHTML(r);
    }

    /**
     * Reads HTML from the given {@code URL}.
     *
     * @param url the URL to parse.
     * @throws IOException if an error occured while reading
     * @see #parseXHTML(java.io.Reader)
     */
    public void readHTMLFromURL(URL url) throws IOException {
        InputStream in = url.openStream();
        try {
            parseXHTML(new InputStreamReader(in, "UTF8"));
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(HTMLTextAreaModel.class.getName()).log(
                        Level.SEVERE, "Exception while closing InputStream", ex);
            }
        }
    }

    public Iterator<Element> iterator() {
        return elements.iterator();
    }

    /**
     * Returns all links to CSS style sheets
     * @return an Iterable containing all hrefs
     */
    public Iterable<String> getStyleSheetLinks() {
        return styleSheetLinks;
    }

    /**
     * Returns the title of this XHTML document or null if it has no title.
     * @return the title of this XHTML document or null if it has no title.
     */
    public String getTitle() {
        return title;
    }

    public Element getElementById(String id) {
        return idMap.get(id);
    }

    public void domModified() {
        doCallback();
    }

    /**
     * Parse a XHTML document. The root element must be &lt;html&gt;
     * @param reader the reader used to read the XHTML document.
     */
    public void parseXHTML(Reader reader) {
        this.elements.clear();
        this.styleSheetLinks.clear();
        this.idMap.clear();
        this.title = null;

        try {
            XmlPullParser xpp = XMLParser.createParser();
            xpp.setInput(reader);
            xpp.defineEntityReplacementText("nbsp", "\u00A0");
            xpp.require(XmlPullParser.START_DOCUMENT, null, null);
            xpp.nextTag();
            xpp.require(XmlPullParser.START_TAG, null, "html");

            styleStack.clear();
            styleStack.add(new Style(null, null));
            curContainer = null;
            sb.setLength(0);

            while(xpp.nextTag() != XmlPullParser.END_TAG) {
                xpp.require(XmlPullParser.START_TAG, null, null);
                String name = xpp.getName();
                if("head".equals(name)) {
                    parseHead(xpp);
                } else if("body".equals(name)) {
                    pushStyle(xpp);
                    BlockElement be = new BlockElement(getStyle());
                    elements.add(be);
                    parseContainer(xpp, be);
                }
            }
            
            parseMain(xpp);
            finishText();
        } catch(Throwable ex) {
             Logger.getLogger(HTMLTextAreaModel.class.getName()).log(Level.SEVERE, "Unable to parse XHTML document", ex);
        } finally {
            // data was modified
            doCallback();
        }
    }

    private void parseContainer(XmlPullParser xpp, ContainerElement container) throws XmlPullParserException, IOException {
        ContainerElement prevContainer = curContainer;
        curContainer = container;
        pushStyle(null);
        parseMain(xpp);
        popStyle();
        curContainer = prevContainer;
    }

    private void parseMain(XmlPullParser xpp) throws XmlPullParserException, IOException {
        int level = 1;
        int type;
        while(level > 0 && (type=xpp.nextToken()) != XmlPullParser.END_DOCUMENT) {
            switch(type) {
            case XmlPullParser.START_TAG: {
                String name = xpp.getName();
                if("head".equals(name)) {
                    parseHead(xpp);
                    break;
                }
                ++level;
                finishText();
                Style style = pushStyle(xpp);
                Element element;

                if("img".equals(name)) {
                    String src = TextUtil.notNull(xpp.getAttributeValue(null, "src"));
                    String alt = xpp.getAttributeValue(null, "alt");
                    element = new ImageElement(style, src, alt);
                } else if("p".equals(name)) {
                    ParagraphElement pe = new ParagraphElement(style);
                    parseContainer(xpp, pe);
                    element = pe;
                    --level;
                } else if("button".equals(name)) {
                    String btnName = TextUtil.notNull(xpp.getAttributeValue(null, "name"));
                    String btnParam = TextUtil.notNull(xpp.getAttributeValue(null, "value"));
                    element = new WidgetElement(style, btnName, btnParam);
                } else if("ul".equals(name)) {
                    ContainerElement ce = new ContainerElement(style);
                    parseContainer(xpp, ce);
                    element = ce;
                    --level;
                } else if("ol".equals(name)) {
                    element = parseOL(xpp, style);
                    --level;
                } else if("li".equals(name)) {
                    ListElement le = new ListElement(style);
                    parseContainer(xpp, le);
                    element = le;
                    --level;
                } else if("div".equals(name) || isHeading(name)) {
                    BlockElement be = new BlockElement(style);
                    parseContainer(xpp, be);
                    element = be;
                    --level;
                } else if("a".equals(name)) {
                    String href = xpp.getAttributeValue(null, "href");
                    if(href == null) {
                        break;
                    }
                    LinkElement le = new LinkElement(style, href);
                    parseContainer(xpp, le);
                    element = le;
                    --level;
                } else if("table".equals(name)) {
                    element = parseTable(xpp, style);
                    --level;
                } else if("br".equals(name)) {
                    element = new LineBreakElement(style);
                } else {
                    break;
                }

                curContainer.add(element);
                registerElement(element);
                break;
            }
            case XmlPullParser.END_TAG: {
                --level;
                finishText();
                popStyle();
                break;
            }
            case XmlPullParser.TEXT: {
                char[] buf = xpp.getTextCharacters(startLength);
                if(startLength[1] > 0) {
                    sb.append(buf, startLength[0], startLength[1]);
                }
                break;
            }
            case XmlPullParser.ENTITY_REF:
                sb.append(xpp.getText());
                break;
            }
        }
    }

    private void parseHead(XmlPullParser xpp) throws XmlPullParserException, IOException {
        int level = 1;
        while(level > 0) {
            switch (xpp.nextTag()) {
                case XmlPullParser.START_TAG: {
                    ++level;
                    String name = xpp.getName();
                    if("link".equals(name)) {
                        String linkhref = xpp.getAttributeValue(null, "href");
                        if("stylesheet".equals(xpp.getAttributeValue(null, "rel")) &&
                                "text/css".equals(xpp.getAttributeValue(null, "type")) &&
                                linkhref != null) {
                            styleSheetLinks.add(linkhref);
                        }
                    }
                    if("title".equals(name)) {
                        title = xpp.nextText();
                        --level;
                    }
                    break;
                }
                case XmlPullParser.END_TAG: {
                    --level;
                    break;
                }
            }
        }
    }

    private TableElement parseTable(XmlPullParser xpp, Style tableStyle) throws XmlPullParserException, IOException {
        ArrayList<TableCellElement> cells = new ArrayList<TableCellElement>();
        ArrayList<Style> rowStyles = new ArrayList<Style>();
        int numColumns = 0;
        int cellSpacing = parseInt(xpp, "cellspacing", 0);
        int cellPadding = parseInt(xpp, "cellpadding", 0);
        
        for(;;) {
            switch (xpp.nextTag()) {
                case XmlPullParser.START_TAG: {
                    pushStyle(xpp);
                    String name = xpp.getName();
                    if("td".equals(name) || "th".equals(name)) {
                        int colspan = parseInt(xpp, "colspan", 1);
                        TableCellElement cell = new TableCellElement(getStyle(), colspan);
                        parseContainer(xpp, cell);
                        registerElement(cell);

                        cells.add(cell);
                        for(int col=1 ; col<colspan ; col++) {
                            cells.add(null);
                        }
                    }
                    if("tr".equals(name)) {
                        rowStyles.add(getStyle());
                    }
                    break;
                }
                case XmlPullParser.END_TAG: {
                    popStyle();
                    String name = xpp.getName();
                    if("tr".equals(name)) {
                        if(numColumns == 0) {
                            numColumns = cells.size();
                        }
                    }
                    if("table".equals(name)) {
                        TableElement tableElement = new TableElement(tableStyle,
                                numColumns, rowStyles.size(), cellSpacing, cellPadding);
                        for(int row=0,idx=0 ; row<rowStyles.size() ; row++) {
                            tableElement.setRowStyle(row, rowStyles.get(row));
                            for(int col=0 ; col<numColumns && idx<cells.size() ; col++,idx++) {
                                TableCellElement cell = cells.get(idx);
                                tableElement.setCell(row, col, cell);
                            }
                        }
                        return tableElement;
                    }
                }
            }
        }
    }

    private OrderedListElement parseOL(XmlPullParser xpp, Style olStyle) throws XmlPullParserException, IOException {
        int start = parseInt(xpp, "start", 1);
        OrderedListElement ole = new OrderedListElement(olStyle, start);
        registerElement(ole);
        for(;;) {
            switch (xpp.nextTag()) {
                case XmlPullParser.START_TAG: {
                    pushStyle(xpp);
                    String name = xpp.getName();
                    if("li".equals(name)) {
                        ContainerElement ce = new ContainerElement(getStyle());
                        parseContainer(xpp, ce);
                        registerElement(ce);
                        ole.add(ce);
                    }
                    break;
                }
                case XmlPullParser.END_TAG: {
                    popStyle();
                    String name = xpp.getName();
                    if("ol".equals(name)) {
                        return ole;
                    }
                    break;
                }
            }
        }
    }

    private void registerElement(Element element) {
        StyleSheetKey styleSheetKey = element.getStyle().getStyleSheetKey();
        if(styleSheetKey != null) {
            String id = styleSheetKey.getId();
            if(id != null) {
                idMap.put(id, element);
            }
        }
    }

    private static int parseInt(XmlPullParser xpp, String attribute, int defaultValue) {
        String value = xpp.getAttributeValue(null, attribute);
        if(value != null) {
            try {
                return Integer.parseInt(value);
            } catch (IllegalArgumentException ignore) {
            }
        }
        return defaultValue;
    }

    private static boolean isXHTML(String doc) {
        if(doc.length() > 5 && doc.charAt(0) == '<') {
            return doc.startsWith("<?xml") || doc.startsWith("<!DOCTYPE") || doc.startsWith("<html>");
        }
        return false;
    }
    
    private boolean isHeading(String name) {
        return name.length() == 2 && name.charAt(0) == 'h' &&
                (name.charAt(1) >= '0' && name.charAt(1) <= '6');
    }
    
    private Style getStyle() {
        return styleStack.get(styleStack.size()-1);
    }

    private Style pushStyle(XmlPullParser xpp) {
        Style parent = getStyle();
        StyleSheetKey key = null;
        String style = null;
        
        if(xpp != null) {
            String className = xpp.getAttributeValue(null, "class");
            String element = xpp.getName();
            String id = xpp.getAttributeValue(null, "id");
            key = new StyleSheetKey(element, className, id);
            style = xpp.getAttributeValue(null, "style");
        }
        
        Style newStyle;

        if(style != null) {
            newStyle = new CSSStyle(parent, key, style);
        } else {
            newStyle = new Style(parent, key);
        }

        styleStack.add(newStyle);
        return newStyle;
    }

    private void popStyle() {
        int stackSize = styleStack.size();
        if(stackSize > 1) {
            styleStack.remove(stackSize-1);
        }
    }

    private void finishText() {
        if(sb.length() > 0) {
            Style style = getStyle();
            TextElement e = new TextElement(style, sb.toString());
            registerElement(e);
            curContainer.add(e);
            sb.setLength(0);
        }
    }
}
