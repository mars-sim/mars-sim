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

import de.matthiasmann.twl.renderer.FontMapper;
import de.matthiasmann.twl.utils.StringList;
import de.matthiasmann.twl.utils.TextUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A style sheet.
 * 
 * <p>Each StyleSheet class contains the following rules:</p>
 * <pre>pre {
 *    white-space: pre
 *}</pre>
 * 
 * @author Matthias Mann
 */
public class StyleSheet implements StyleSheetResolver {

    static final Object NULL = new Object();
    
    private static final Selector PRE_SELECTOR = new Selector("pre", null, null, null, null);
    static {
        (PRE_SELECTOR.style = new CSSStyle()).put(StyleAttribute.PREFORMATTED, Boolean.TRUE);
        PRE_SELECTOR.score = 0x100;
    }
    
    private final ArrayList<Selector> rules;
    private final IdentityHashMap<Style, Object> cache;
    private ArrayList<AtRule> atrules;

    public StyleSheet() {
        this.rules = new ArrayList<Selector>();
        this.cache = new IdentityHashMap<Style, Object>();
        
        rules.add(PRE_SELECTOR);
    }

    public void parse(URL url) throws IOException {
        InputStream is = url.openStream();
        try {
            parse(new InputStreamReader(is, "UTF8"));
        } finally {
            is.close();
        }
    }

    public void parse(String style) throws IOException {
        parse(new StringReader(style));
    }
    
    public void parse(Reader r) throws IOException {
        Parser parser = new Parser(r);
        ArrayList<Selector> selectors = new ArrayList<Selector>();
        int what;
        while((what=parser.yylex()) != Parser.EOF) {
            if(what == Parser.ATRULE) {
                parser.expect(Parser.IDENT);
                AtRule atrule = new AtRule(parser.yytext());
                parser.expect(Parser.STYLE_BEGIN);
                
                while((what=parser.yylex()) != Parser.STYLE_END) {
                    if(what != Parser.IDENT) {
                        parser.unexpected();
                    }
                    String key = parser.yytext();
                    parser.expect(Parser.COLON);
                    what = parser.yylex();
                    if(what != Parser.SEMICOLON && what != Parser.STYLE_END) {
                        parser.unexpected();
                    }
                    String value = TextUtil.trim(parser.sb, 0);
                    try {
                        atrule.entries.put(key, value);
                    } catch (IllegalArgumentException ex) {
                    }
                    if(what == Parser.STYLE_END) {
                        break;
                    }
                }
                
                if(atrules == null) {
                    atrules = new ArrayList<AtRule>();
                }
                atrules.add(atrule);
                continue;
            }
            
            Selector selector = null;
            selectorloop: for(;;) {
                String element = null;
                String className = null;
                String pseudoClass = null;
                String id = null;
                parser.sawWhitespace = false;
                switch (what) {
                    default:
                        parser.unexpected();    // throws exception
                        // fall though will not happen but keeps compiler quite
                    case Parser.DOT:
                    case Parser.HASH:
                    case Parser.COLON:
                        break;
                    case Parser.IDENT:
                        element = parser.yytext();
                        // fall through
                    case Parser.STAR:
                        what = parser.yylex();
                        break;
                }
                while((what == Parser.DOT || what == Parser.HASH || what == Parser.COLON) && !parser.sawWhitespace) {
                    parser.expect(Parser.IDENT);
                    String text = parser.yytext();
                    if(what == Parser.DOT) {
                        className = text;
                    } else if(what == Parser.COLON) {
                        pseudoClass = text;
                    } else {
                        id = text;
                    }
                    what = parser.yylex();
                }
                selector = new Selector(element, className, id, pseudoClass, selector);
                switch (what) {
                    case Parser.GT:
                        selector.directChild = true;
                        what = parser.yylex();
                        break;
                    case Parser.COMMA:
                    case Parser.STYLE_BEGIN:
                        break selectorloop;
                }
            }
            
            // to ensure that the head of the selector matches the head of the
            // style and not skip ahead we use the directChild flag
            // this causes an offset of 1 for all scores which doesn't matter
            selector.directChild = true;
            selectors.add(selector);

            switch (what) {
                default:
                    parser.unexpected();    // throws exception
                    // fall though will not happen but keeps compiler quite
                    
                case Parser.STYLE_BEGIN: {
                    CSSStyle style = new CSSStyle();

                    while((what=parser.yylex()) != Parser.STYLE_END) {
                        if(what != Parser.IDENT) {
                            parser.unexpected();
                        }
                        String key = parser.yytext();
                        parser.expect(Parser.COLON);
                        what = parser.yylex();
                        if(what != Parser.SEMICOLON && what != Parser.STYLE_END) {
                            parser.unexpected();
                        }
                        String value = TextUtil.trim(parser.sb, 0);
                        try {
                            style.parseCSSAttribute(key, value);
                        } catch (IllegalArgumentException ex) {
                        }
                        if(what == Parser.STYLE_END) {
                            break;
                        }
                    }

                    for(int i=0,n=selectors.size() ; i<n ; i++) {
                        Style selectorStyle = style;
                        selector = selectors.get(i);
                        if(selector.pseudoClass != null) {
                            selectorStyle = transformStyle(style, selector.pseudoClass);
                        }
                        rules.add(selector);
                        int score = 0;
                        for(Selector s=selector ; s!=null ; s=s.tail) {
                            if(s.directChild) {
                                score += 0x1;
                            }
                            if(s.element != null) {
                                score += 0x100;
                            }
                            if(s.className != null) {
                                score += 0x10000;
                            }
                            if(s.id != null) {
                                score += 0x1000000;
                            }
                        }
                        // only needed on head
                        selector.score = score;
                        selector.style = selectorStyle;
                    }

                    selectors.clear();
                    break;
                }

                case Parser.COMMA:
                    break;
            }
        }
    }
    
    public int getNumAtRules() {
        return (atrules != null) ? atrules.size() : 0;
    }
    
    public AtRule getAtRule(int idx) {
        if(atrules == null) {
            throw new IndexOutOfBoundsException();
        }
        return atrules.get(idx);
    }
    
    public void registerFonts(FontMapper fontMapper, URL baseUrl) {
        if(atrules == null) {
            return;
        }
        for(AtRule atrule : atrules) {
            if("font-face".equals(atrule.name)) {
                String family = atrule.get("font-family");
                String src = atrule.get("src");
                
                if(family != null && src != null) {
                    StringList srcs = CSSStyle.parseList(src, 0);
                    for(; srcs != null ; srcs=srcs.getNext()) {
                        String url = CSSStyle.stripURL(srcs.getValue());
                        try {
                            fontMapper.registerFont(family, new URL(baseUrl, url));
                        } catch (IOException ex) {
                            Logger.getLogger(StyleSheet.class.getName()).log(Level.SEVERE,
                                    "Could not register font: " + url, ex);
                        }
                    }
                }
            }
        }
    }

    public void layoutFinished() {
        cache.clear();
    }

    public void startLayout() {
        cache.clear();
    }

    public Style resolve(Style style) {
        Object cacheData = cache.get(style);
        if(cacheData == null) {
            return resolveSlow(style);
        }
        if(cacheData == NULL) {
            return null;
        }
        return (Style)cacheData;
    }
    
    private Style resolveSlow(Style style) {
        Selector[] candidates = new Selector[rules.size()];
        int numCandidates = 0;
        
        // find all possible candidates
        for(int i=0,n=rules.size() ; i<n ; i++) {
            Selector selector = rules.get(i);
            if(matches(selector, style)) {
                candidates[numCandidates++] = selector;
            }
        }

        // sort according to rule priority - this needs a stable sort
        if(numCandidates > 1) {
            Arrays.sort(candidates, 0, numCandidates);
        }
        
        Style result = null;
        boolean copy = true;

        // merge all matching rules
        for(int i=0,n=numCandidates ; i<n ; i++) {
            Style ruleStyle = candidates[i].style;
            if(result == null) {
                result = ruleStyle;
            } else {
                if(copy) {
                    result = new Style(result);
                    copy = false;
                }
                result.putAll(ruleStyle);
            }
        }

        putIntoCache(style, result);
        return result;
    }
    
    private void putIntoCache(Style key, Style style) {
        cache.put(key, (style == null) ? NULL : style);
    }

    private boolean matches(Selector selector, Style style) {
        do {
            StyleSheetKey styleSheetKey = style.getStyleSheetKey();
            if(styleSheetKey != null) {
                if(selector.matches(styleSheetKey)) {
                    selector = selector.tail;
                    if(selector == null) {
                        return true;
                    }
                } else if(selector.directChild) {
                    return false;
                }
            }
            style = style.getParent();
        }while(style != null);
        return false;
    }

    private Style transformStyle(CSSStyle style, String pseudoClass) {
        Style result = new Style(style.getParent(), style.getStyleSheetKey());
        if("hover".equals(pseudoClass)) {
            result.put(StyleAttribute.COLOR_HOVER, style.getRaw(StyleAttribute.COLOR));
            result.put(StyleAttribute.BACKGROUND_COLOR_HOVER, style.getRaw(StyleAttribute.BACKGROUND_COLOR));
            result.put(StyleAttribute.TEXT_DECORATION_HOVER, style.getRaw(StyleAttribute.TEXT_DECORATION));
        }
        return result;
    }

    static class Selector extends StyleSheetKey implements Comparable<Selector> {
        final String pseudoClass;
        final Selector tail;
        boolean directChild;
        Style style;
        int score;

        Selector(String element, String className, String id, String pseudoClass, Selector tail) {
            super(element, className, id);
            this.pseudoClass = pseudoClass;
            this.tail = tail;
        }

        public int compareTo(Selector other) {
            return this.score - other.score;
        }
    }
    
    public static class AtRule implements Iterable<Map.Entry<String, String>> {
        final String name;
        final HashMap<String, String> entries;

        public AtRule(String name) {
            this.name = name;
            this.entries = new HashMap<String, String>();
        }

        public String getName() {
            return name;
        }

        public String get(String key) {
            return entries.get(key);
        }

        public Iterator<Map.Entry<String, String>> iterator() {
            return Collections.unmodifiableSet(entries.entrySet()).iterator();
        }
    }
}
