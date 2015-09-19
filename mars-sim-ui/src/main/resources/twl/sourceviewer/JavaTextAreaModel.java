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
package sourceviewer;

import de.matthiasmann.twl.model.HasCallback;
import de.matthiasmann.twl.textarea.Style;
import de.matthiasmann.twl.textarea.StyleAttribute;
import de.matthiasmann.twl.textarea.StyleSheetKey;
import de.matthiasmann.twl.textarea.TextAreaModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Iterator;

/**
 *
 * @author Matthias Mann
 */
public class JavaTextAreaModel extends HasCallback implements TextAreaModel {

    private final EnumMap<JavaScanner.Kind, Style> styles;
    private final Style normalStyle;
    private ContainerElement root;

    public JavaTextAreaModel() {
        this.styles = new EnumMap<JavaScanner.Kind, Style>(JavaScanner.Kind.class);
        this.normalStyle = new Style(null, new StyleSheetKey("pre", "code", null))
                .with(StyleAttribute.PREFORMATTED, true);
        styles.put(JavaScanner.Kind.NORMAL, normalStyle);
        styles.put(JavaScanner.Kind.COMMENT, new Style(normalStyle, new StyleSheetKey("span", "comment", null)));
        styles.put(JavaScanner.Kind.COMMENT_TAG, new Style(normalStyle, new StyleSheetKey("span", "commentTag", null)));
        styles.put(JavaScanner.Kind.STRING, new Style(normalStyle, new StyleSheetKey("span", "string", null)));
        styles.put(JavaScanner.Kind.KEYWORD, new Style(normalStyle, new StyleSheetKey("span", "keyword", null)));
    }

    public Iterator<Element> iterator() {
        return new IteratorImpl(root);
    }

    public void clear() {
        root = null;
        doCallback();
    }

    public void parse(Reader r, boolean withLineNumbers) {
        JavaScanner js = new JavaScanner(r);

        ContainerElement container;
        Style lineStyle;
        if(withLineNumbers) {
            container = new OrderedListElement(new Style(normalStyle, new StyleSheetKey("ol", "linenumbers", null)), 1);
            lineStyle = new Style(container.getStyle(), new StyleSheetKey("li", null, null));
        } else {
            container = new BlockElement(normalStyle);
            lineStyle = null;
        }
        ContainerElement line = null;
        TextElement newLine = new TextElement(normalStyle, "\n");

        JavaScanner.Kind kind;
        while((kind=js.scan()) != JavaScanner.Kind.EOF) {
            if(withLineNumbers && line == null) {
                line = new ContainerElement(lineStyle);
            }
            if(kind == JavaScanner.Kind.NEWLINE) {
                if(line != null) {
                    line.add(newLine);
                    container.add(line);
                    line = null;
                } else {
                    container.add(newLine);
                }
                continue;
            }
            TextElement textElement = new TextElement(styles.get(kind), js.getString());
            if(line != null) {
                line.add(textElement);
            } else {
                container.add(textElement);
            }
        }

        root = container;
        doCallback();
    }

    public void parse(InputStream is, String charsetName, boolean withLineNumbers) throws UnsupportedEncodingException {
        InputStreamReader isr = new InputStreamReader(is, charsetName);
        parse(isr, withLineNumbers);
    }

    public void parse(URL url, boolean withLineNumbers) throws IOException {
        InputStream is = url.openStream();
        try {
            parse(is, "UTF8", withLineNumbers);
        } finally {
            is.close();
        }
    }

    public void parse(File file, boolean withLineNumbers) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            parse(fis, "UTF8", withLineNumbers);
        } finally {
            fis.close();
        }
    }

    private static class IteratorImpl implements Iterator<Element> {
        Element e;
        public IteratorImpl(Element e) {
            this.e = e;
        }

        public boolean hasNext() {
            return e != null;
        }

        public Element next() {
            Element tmp = e;
            e = null;
            return tmp;
        }

        public void remove() {
        }
    }
}
