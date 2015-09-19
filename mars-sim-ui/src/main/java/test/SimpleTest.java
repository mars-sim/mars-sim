/*
 * Copyright (c) 2008, Matthias Mann
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
package test;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import de.matthiasmann.twl.BoxLayout;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.DesktopArea;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.PopupWindow;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import de.matthiasmann.twl.model.PersistentIntegerModel;
import de.matthiasmann.twl.model.SimpleBooleanModel;
import de.matthiasmann.twleffects.lwjgl.LWJGLEffectsRenderer;
import java.io.IOException;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

/**
 * A simple test for TWL.
 *
 * @author Matthias Mann
 */
public class SimpleTest {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    static final String WITH_TITLE = "resizableframe-title";
    static final String WITHOUT_TITLE = "resizableframe";

    static class StyleItem {
        public final String theme;
        public final String name;

        public StyleItem(String theme, String name) {
            this.theme = theme;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public static void main(String[] arg) throws LWJGLException {
        SimpleTest test = new SimpleTest();
        test.run(new VideoMode(new DisplayMode(WIDTH, HEIGHT), false));
    }

    private static final String[] THEME_FILES = {
        "simple_demo.xml",
        "guiTheme.xml"
    };

    protected final DisplayMode desktopMode;
    protected boolean closeRequested;
    protected ThemeManager theme;
    protected LWJGLRenderer renderer;
    protected GUI gui;
    protected VideoSettings.CallbackReason vidDlgCloseReason;
    protected PersistentIntegerModel curThemeIdx;

    public SimpleTest() {
        desktopMode = Display.getDisplayMode();
        curThemeIdx = new PersistentIntegerModel(
                AppletPreferences.userNodeForPackage(SimpleTest.class),
                "currentThemeIndex", 0, THEME_FILES.length, 0);
    }

    private void loadTheme() throws IOException {
        renderer.syncViewportSize();
        System.out.println("width="+renderer.getWidth()+" height="+renderer.getHeight());

        long startTime = System.nanoTime();
        // NOTE: this code destroys the old theme manager (including it's cache context)
        // after loading the new theme with a new cache context.
        // This allows easy reloading of a theme for development.
        // If you want fast theme switching without reloading then use the existing
        // cache context for loading the new theme and don't destroy the old theme.
        ThemeManager newTheme = ThemeManager.createThemeManager(
            SimpleTest.class.getResource(THEME_FILES[curThemeIdx.getValue()]), renderer);
        long duration = System.nanoTime() - startTime;
        System.out.println("Loaded theme in " + (duration/1000) + " us");

        if(theme != null) {
            theme.destroy();
        }
        theme = newTheme;
        
        gui.setSize();
        gui.applyTheme(theme);
        gui.setBackground(theme.getImageNoWarning("gui.background"));
    }

    private void createDisplay(VideoMode mode) throws LWJGLException {
        Display.setTitle("TWL Examples");
        Display.setFullscreen(mode.fullscreen);
        Display.setDisplayMode(mode.mode);
        Display.create(new PixelFormat(0, 0, 0));
        Display.setVSyncEnabled(true);
    }

    @SuppressWarnings("SleepWhileInLoop")
    public void mainLoop(boolean isApplet) throws LWJGLException, IOException {
        final RootPane root = new RootPane();
        //renderer = new LWJGLRenderer();
        renderer = new LWJGLEffectsRenderer();
        renderer.setUseSWMouseCursors(true);
        gui = new GUI(root, renderer);

        loadTheme();

        WidgetsDemoDialog1 dlg1 = new WidgetsDemoDialog1();
        root.desk.add(dlg1);
        dlg1.adjustSize();
        dlg1.center(0.35f, 0.5f);

        GraphDemoDialog1 fMS = new GraphDemoDialog1();
        root.desk.add(fMS);
        fMS.adjustSize();
        fMS.center(1f, 0.8f);

        ScrollPaneDemoDialog1 fScroll = new ScrollPaneDemoDialog1();
        root.desk.add(fScroll);
        fScroll.adjustSize();
        fScroll.center(0f, 0f);
        fScroll.addCloseCallback();
        fScroll.centerScrollPane();

        TableDemoDialog1 fTable = new TableDemoDialog1();
        root.desk.add(fTable);
        fTable.adjustSize();
        fTable.center(0f, 0.5f);
        //fTable.addCloseCallback();

        PropertySheetDemoDialog fPropertySheet = new PropertySheetDemoDialog();
        fPropertySheet.setHardVisible(false);
        root.desk.add(fPropertySheet);
        fPropertySheet.setSize(400, 400);
        fPropertySheet.center(0f, 0.25f);
        fPropertySheet.addCloseCallback();

        TextAreaDemoDialog1 fInfo = new TextAreaDemoDialog1();
        root.desk.add(fInfo);
        fInfo.setSize(gui.getWidth()*2/3, gui.getHeight()*2/3);
        fInfo.center(0.5f, 0.5f);
        fInfo.addCloseCallback();

        TextAreaDemoDialog2 fTextAreaTest = new TextAreaDemoDialog2();
        fTextAreaTest.setHardVisible(false);
        root.desk.add(fTextAreaTest);
        fTextAreaTest.setSize(gui.getWidth()*2/3, gui.getHeight()*2/3);
        fTextAreaTest.center(0.5f, 0.5f);
        fTextAreaTest.addCloseCallback();

        ColorSelectorDemoDialog1 fCS = new ColorSelectorDemoDialog1();
        fCS.setHardVisible(false);
        root.desk.add(fCS);
        fCS.adjustSize();
        fCS.center(0.5f, 0.5f);
        fCS.addCloseCallback();

        final PopupWindow settingsDlg = new PopupWindow(root);
        final VideoSettings settings = new VideoSettings(
                AppletPreferences.userNodeForPackage(VideoSettings.class),
                desktopMode);
        settingsDlg.setTheme("settingdialog");
        settingsDlg.add(settings);
        settingsDlg.setCloseOnClickedOutside(false);
        settings.setTheme("settings");
        settings.addCallback(new CallbackWithReason<VideoSettings.CallbackReason>() {
            public void callback(VideoSettings.CallbackReason reason) {
                vidDlgCloseReason = reason;
                settingsDlg.closePopup();
            }
        });

        root.addButton("Exit", new Runnable() {
            public void run() {
                closeRequested = true;
            }
        });
        root.addButton("Info", "Shows TWL license", new ToggleFadeFrame(fInfo)).setTooltipContent(makeComplexTooltip());
        root.addButton("TA", "Shows a text area test", new ToggleFadeFrame(fTextAreaTest));
        if(!isApplet) {
            root.addButton("Settings", "Opens a dialog which might be used to change video settings", new Runnable() {
                public void run() {
                    settings.readSettings();
                    settingsDlg.openPopupCentered();
                }
            });
        }
        root.addButton("Toggle Theme", new Runnable() {
            @SuppressWarnings("CallToThreadDumpStack")
            public void run() {
                curThemeIdx.setValue((curThemeIdx.getValue() + 1) % THEME_FILES.length);
                try {
                    loadTheme();
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        root.addButton("ScrollPane", new ToggleFadeFrame(fScroll));
        root.addButton("Properties", new ToggleFadeFrame(fPropertySheet));
        root.addButton("Color", new ToggleFadeFrame(fCS));

        root.addButton("Game", new Runnable() {
            public void run() {
                BlockGame game = new BlockGame();
                game.setTheme("/blockgame");
                PopupWindow popup = new PopupWindow(root);
                popup.setTheme("settingdialog");
                popup.add(game);
                popup.openPopupCentered();
            }
        });

        fInfo.requestKeyboardFocus();

        while(!Display.isCloseRequested() && !closeRequested) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            gui.update();
            Display.update();

            if(root.reduceLag) {
                TestUtils.reduceInputLag();
            }

            if(!isApplet && vidDlgCloseReason == VideoSettings.CallbackReason.ACCEPT) {
                settings.storeSettings();
                VideoMode vm = settings.getSelectedVideoMode();
                gui.destroy();
                renderer.getActiveCacheContext().destroy();
                Display.destroy();
                createDisplay(vm);
                loadTheme();
            }
            vidDlgCloseReason = null;

            if(!Display.isActive()) {
                gui.clearKeyboardState();
                gui.clearMouseState();
            }
            
            if(!Display.isVisible()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private Object makeComplexTooltip() {
        HTMLTextAreaModel tam = new HTMLTextAreaModel();
        tam.setHtml("Hello <img src=\"twl-logo\" alt=\"logo\"/> World");
        TextArea ta = new TextArea(tam);
        ta.setTheme("/htmlTooltip");
        return ta;
    }

    public void run(VideoMode mode) {
        try {
            createDisplay(mode);

            mainLoop(false);
        } catch (Throwable ex) {
            TestUtils.showErrMsg(ex);
        }

        Display.destroy();
        System.exit(0);
    }

    static class RootPane extends Widget {
        final DesktopArea desk;
        final BoxLayout btnBox;
        final BoxLayout vsyncBox;
        boolean reduceLag = true;

        public RootPane() {
            setTheme("");
            
            desk = new DesktopArea();
            desk.setTheme("");

            btnBox = new BoxLayout(BoxLayout.Direction.HORIZONTAL);
            btnBox.setTheme("buttonBox");

            vsyncBox = new BoxLayout(BoxLayout.Direction.HORIZONTAL);
            vsyncBox.setTheme("buttonBox");

            final SimpleBooleanModel vsyncModel = new SimpleBooleanModel(true);
            vsyncModel.addCallback(new Runnable() {
                public void run() {
                    Display.setVSyncEnabled(vsyncModel.getValue());
                }
            });

            ToggleButton vsyncBtn = new ToggleButton(vsyncModel);
            vsyncBtn.setTheme("checkbox");
            Label l = new Label("VSync");
            l.setLabelFor(vsyncBtn);

            vsyncBox.add(l);
            vsyncBox.add(vsyncBtn);

            add(desk);
            add(btnBox);
            add(vsyncBox);
        }

        public Button addButton(String text, Runnable cb) {
            Button btn = new Button(text);
            btn.addCallback(cb);
            btnBox.add(btn);
            invalidateLayout();
            return btn;
        }

        public Button addButton(String text, String ttolTip, Runnable cb) {
            Button btn = addButton(text, cb);
            btn.setTooltipContent(ttolTip);
            return btn;
        }
        
        @Override
        protected void layout() {
            btnBox.adjustSize();
            btnBox.setPosition(0, getParent().getHeight() - btnBox.getHeight());
            desk.setSize(getParent().getWidth(), getParent().getHeight());
            vsyncBox.adjustSize();
            vsyncBox.setPosition(
                    getParent().getWidth() - vsyncBox.getWidth(),
                    getParent().getHeight() - vsyncBox.getHeight());
        }

        @Override
        protected void afterAddToGUI(GUI gui) {
            super.afterAddToGUI(gui);
            validateLayout();
        }

        @Override
        protected boolean handleEvent(Event evt) {
            if(evt.getType() == Event.Type.KEY_PRESSED &&
                    evt.getKeyCode() == Event.KEY_L &&
                    (evt.getModifiers() & Event.MODIFIER_CTRL) != 0 &&
                    (evt.getModifiers() & Event.MODIFIER_SHIFT) != 0) {
                reduceLag ^= true;
                System.out.println("reduceLag = " + reduceLag);
            }

            return super.handleEvent(evt);
        }

    }
}
