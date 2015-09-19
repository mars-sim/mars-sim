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
package textarea;

import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Rect;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Timer;
import de.matthiasmann.twl.ValueAdjusterInt;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.model.SimpleIntegerModel;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.textarea.StyleAttribute;
import de.matthiasmann.twl.textarea.StyleSheet;
import de.matthiasmann.twl.textarea.TextAreaModel;
import de.matthiasmann.twl.textarea.Value;
import de.matthiasmann.twl.theme.ThemeManager;
import de.matthiasmann.twl.utils.TextUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import test.TestUtils;

/**
 *
 * @author Matthias Mann
 */
public class TextAreaDemo extends DesktopArea {

    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
            Display.setTitle("TWL TextArea Demo");
            Display.setVSyncEnabled(true);

            LWJGLRenderer renderer = new LWJGLRenderer();
            TextAreaDemo demo = new TextAreaDemo();
            GUI gui = new GUI(demo, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    TextAreaDemo.class.getResource("demo.xml"), renderer);
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
    private final TextFrame textFrame;

    public boolean quit;

    public TextAreaDemo() {
        fpsCounter = new FPSCounter();
        add(fpsCounter);

        textFrame = new TextFrame();
        add(textFrame);

        textFrame.setSize(600, 500);
        textFrame.setPosition(40, 20);
    }

    @Override
    protected void layout() {
        super.layout();

        // fpsCounter is bottom right
        fpsCounter.adjustSize();
        fpsCounter.setPosition(
                getInnerWidth() - fpsCounter.getWidth(),
                getInnerHeight() - fpsCounter.getHeight());
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

    static class TextFrame extends ResizableFrame {
        private final HTMLTextAreaModel textAreaModel;
        private final TextArea textArea;
        private final ScrollPane scrollPane;
        private Timer timer;
        private int size;
        private int dir;

        private static final int MIN_SIZE = 128;
        private static final int MAX_SIZE = 256;

        public TextFrame() {
            setTitle("Text");

            this.textAreaModel = new HTMLTextAreaModel();
            this.textArea = new TextArea(textAreaModel);

            readFile("demo.html");

            textArea.addCallback(new TextArea.Callback() {
                public void handleLinkClicked(String href) {
                    if(href.startsWith("javascript:")) {
                        handleAction(href.substring(11));
                    } else if(href.startsWith("#")) {
                        TextAreaModel.Element ankor = textAreaModel.getElementById(href.substring(1));
                        if(ankor != null) {
                            Rect rect = textArea.getElementRect(ankor);
                            if(rect != null) {
                                scrollPane.setScrollPositionY(rect.getY());
                            }
                        }
                    } else {
                        readFile(href);
                    }
                }
            });

            ValueAdjusterInt vai = new ValueAdjusterInt(new SimpleIntegerModel(0, 100, 50));
            vai.setTooltipContent("Select a nice value");
            textArea.registerWidget("niceValueSlider", vai);
            
            scrollPane = new ScrollPane(textArea);
            scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);

            add(scrollPane);
        }

        @Override
        protected void afterAddToGUI(GUI gui) {
            super.afterAddToGUI(gui);
            timer = gui.createTimer();
            timer.setDelay(16);
            timer.setContinuous(true);
            timer.setCallback(new Runnable() {
                public void run() {
                    animate();
                }
            });
        }

        @Override
        protected void beforeRemoveFromGUI(GUI gui) {
            super.beforeRemoveFromGUI(gui);
            timer.stop();
            timer = null;
        }

        void readFile(String name) {
            try {
                textAreaModel.readHTMLFromURL(TextAreaDemo.class.getResource(name));

                StyleSheet styleSheet = new StyleSheet();
                for(String styleSheetLink : textAreaModel.getStyleSheetLinks()) {
                    try {
                        styleSheet.parse(TextAreaDemo.class.getResource(styleSheetLink));
                    } catch(IOException ex) {
                        Logger.getLogger(TextAreaDemo.class.getName()).log(Level.SEVERE,
                                "Can't parse style sheet: " + styleSheetLink, ex);
                    }
                }
                textArea.setStyleClassResolver(styleSheet);
                
                setTitle(TextUtil.notNull(textAreaModel.getTitle()));

                size = MIN_SIZE;
                dir = -4;
            } catch(IOException ex) {
                Logger.getLogger(TextAreaDemo.class.getName()).log(Level.SEVERE, "Can't read HTML: " + name, ex);
            }
        }

        void handleAction(String what) {
            if("zoomImage()".equals(what)) {
                if(timer != null && !timer.isRunning()) {
                    dir = -dir;
                    timer.start();
                }
            }
        }

        void animate() {
            size = Math.max(MIN_SIZE, Math.min(MAX_SIZE, size + dir));
            if(size == MIN_SIZE || size == MAX_SIZE) {
                timer.stop();
            }

            TextAreaModel.Element e = textAreaModel.getElementById("portrait");
            if(e != null) {
                e.setStyle(e.getStyle().with(StyleAttribute.WIDTH, new Value(size, Value.Unit.PX)));
                textAreaModel.domModified();
            }
        }
    }
}
