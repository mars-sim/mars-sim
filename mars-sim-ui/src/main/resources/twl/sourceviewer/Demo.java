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
package sourceviewer.demo;

import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TabbedPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.DefaultEditFieldModel;
import de.matthiasmann.twl.model.EditFieldModel;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.textarea.StyleSheet;
import de.matthiasmann.twl.theme.ThemeManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import sourceviewer.JavaTextAreaModel;
import sourceviewer.StringSyntaxHighlighter;
import test.TestUtils;

/**
 *
 * @author Matthias Mann
 */
public final class Demo extends DesktopArea {

    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(1200, 600));
            Display.create();
            Display.setTitle("TWL TextArea Demo");
            Display.setVSyncEnabled(true);

            LWJGLRenderer renderer = new LWJGLRenderer();
            Demo demo = new Demo();
            GUI gui = new GUI(demo, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    Demo.class.getResource("demo.xml"), renderer);
            gui.applyTheme(theme);

            while(!Display.isCloseRequested() && !demo.quit) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                gui.update();

                /**
                 * requires LWJGL 2.4 - for 2.3 just call Display.update()
                 */
                Display.update(false);
                GL11.glGetError();  // force sync with multi threaded GL driver
                Display.sync(60);   // ensure 60Hz even without vsync
                Display.processMessages();  // now process inputs
            }

            gui.destroy();
            theme.destroy();
        } catch (Exception ex) {
            TestUtils.showErrMsg(ex);
        }
        Display.destroy();
    }

    private final FPSCounter fpsCounter;
    private final ResizableFrame frame;
    private final TabbedPane tabbedPane;
    private final StyleSheet styleSheet;
    private final ArrayList<TabEntry> entrys;
    private final ToggleButton showLineNumbersBtn;
    private final ToggleButton scrollTabsBtn;

    public boolean quit;

    public Demo() {
        entrys = new ArrayList<TabEntry>();
        
        fpsCounter = new FPSCounter();
        add(fpsCounter);

        showLineNumbersBtn = new ToggleButton("Show line numbers");
        add(showLineNumbersBtn);

        scrollTabsBtn = new ToggleButton("Scroll tabs");
        scrollTabsBtn.setActive(true);
        add(scrollTabsBtn);

        tabbedPane = new TabbedPane();
        tabbedPane.setScrollTabs(scrollTabsBtn.isActive());

        frame = new ResizableFrame();
        frame.setTitle("Source code viewer");
        frame.add(tabbedPane);
        add(frame);

        frame.setSize(1100, 520);
        frame.setPosition(40, 20);
        
        styleSheet = new StyleSheet();

        try {
            styleSheet.parse(
                    "ol > li { padding-left: 5px; }" +
                    "pre {font-family: code }" +
                    ".comment    { font-family: codeComment }" +
                    ".commentTag { font-family: codeCommentTag }" +
                    ".string     { font-family: codeString  }" +
                    ".keyword    { font-family: codeKeyword }");
        } catch(IOException ex) {
            Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, "Can't parse style sheet", ex);
        }

        final String[] files = new String[] {
            "demo/Demo.java",
            "JavaScanner.java",
            "JavaTextAreaModel.java",
            "CharacterIterator.java",
            "KeywordList.java",
        };
        for(String path : files) {
            try {
                addSourceFile(path);
            } catch(IOException ex) {
                Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, "Can't open file: " + path, ex);
            }
        }

        parseFiles();

        showLineNumbersBtn.addCallback(new Runnable() {
            public void run() {
                parseFiles();
            }
        });
        scrollTabsBtn.addCallback(new Runnable() {
            public void run() {
                tabbedPane.setScrollTabs(scrollTabsBtn.isActive());
            }
        });
        
        EditFieldModel efm = new DefaultEditFieldModel();
        EditField ef = new EditField(null, efm);
        ef.setMultiLine(true);
        ef.setText("public class HelloWorld {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "}\n");
        
        StringSyntaxHighlighter ssh = new StringSyntaxHighlighter(efm, ef.getStringAttributes());
        ssh.registerCallback();
        
        ScrollPane sp = new ScrollPane(ef);
        sp.setFixed(ScrollPane.Fixed.HORIZONTAL);
        sp.setExpandContentSize(true);
        
        tabbedPane.addTab("Source editor", sp);
    }

    public void parseFiles() {
        boolean withLineNumbers = showLineNumbersBtn.isActive();
        for(TabEntry e : entrys) {
            try {
                e.parse(withLineNumbers);
            } catch(IOException ex) {
                Logger.getLogger(Demo.class.getName()).log(Level.SEVERE, "Can't parse file: " + e.url, ex);
            }
        }
    }

    public void addSourceFile(String path) throws IOException {
        URL ref = Demo.class.getResource("demo.xml");
        URL url = new URL(ref, "../../sourceviewer/".concat(path));
        if(url == null) {
            throw new FileNotFoundException(path);
        }
        
        JavaTextAreaModel jtam = new JavaTextAreaModel();
        entrys.add(new TabEntry(jtam, url));

        TextArea textArea = new TextArea(jtam);
        textArea.setTheme("textarea");
        textArea.setStyleClassResolver(styleSheet);

        ScrollPane sp = new ScrollPane(textArea);
        sp.setTheme("scrollpane");
        sp.setFixed(ScrollPane.Fixed.HORIZONTAL);
        sp.setExpandContentSize(true);

        tabbedPane.addTab(path, sp);
    }
    
    @Override
    protected void layout() {
        super.layout();

        // fpsCounter is bottom right
        fpsCounter.adjustSize();
        fpsCounter.setPosition(
                getInnerRight() - fpsCounter.getWidth(),
                getInnerBottom() - fpsCounter.getHeight());

        // showLineNumbersBtn is bottom left
        showLineNumbersBtn.adjustSize();
        showLineNumbersBtn.setPosition(
                getInnerX(),
                getInnerBottom() - showLineNumbersBtn.getHeight());

        scrollTabsBtn.adjustSize();
        scrollTabsBtn.setPosition(
                showLineNumbersBtn.getRight() + 5,
                getInnerBottom() - scrollTabsBtn.getHeight());
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if(super.handleEvent(evt)) {
            return true;
        }
        switch (evt.getType()) {
            case KEY_PRESSED:
                switch (evt.getKeyCode()) {
                    case Event.KEY_ESCAPE:
                        quit = true;
                        return true;
                }
        }
        return false;
    }

    static class TabEntry {
        final JavaTextAreaModel jtam;
        final URL url;

        public TabEntry(JavaTextAreaModel jtam, URL url) {
            this.jtam = jtam;
            this.url = url;
        }

        public void parse(boolean withLineNumbers) throws IOException {
            jtam.parse(url, withLineNumbers);
        }
    }
}
