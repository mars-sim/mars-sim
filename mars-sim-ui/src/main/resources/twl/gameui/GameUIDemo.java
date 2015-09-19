/*
 * Copyright (c) 2008-2011, Matthias Mann
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
package gameui;

import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.RadialPopupMenu;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.WheelWidget;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.SimpleChangableListModel;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import java.util.ArrayList;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import test.TestUtils;

/**
 *
 * @author Matthias Mann
 */
public class GameUIDemo extends Widget {

    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
            Display.setTitle("TWL Game UI Demo");
            Display.setVSyncEnabled(true);

            LWJGLRenderer renderer = new LWJGLRenderer();
            GameUIDemo gameUI = new GameUIDemo();
            GUI gui = new GUI(gameUI, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    GameUIDemo.class.getResource("gameui.xml"), renderer);
            gui.applyTheme(theme);

            while(!Display.isCloseRequested() && !gameUI.quit) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                gui.update();
                Display.update();
                TestUtils.reduceInputLag();
            }

            gui.destroy();
            theme.destroy();
        } catch (Exception ex) {
            TestUtils.showErrMsg(ex);
        }
        Display.destroy();
    }

    private final ToggleButton[] actionButtons;
    private final ToggleButton btnPause;
    private final ToggleButton btnArmageddon;
    private final FPSCounter fpsCounter;
    private final Label lastSelectedRadialEntry;

    private final SimpleChangableListModel<String> digits;
    private final ArrayList<WheelWidget<String>> wheels;
    
    public boolean quit;

    private static final String[] ACTION_NAMES = {
        "pingu-digger",
        "pingu-miner",
        "pingu-basher",
        "pingu-climber",
        "pingu-floater",
        "pingu-bomber",
        "pingu-blocker",
        "pingu-bridger",
    };

    public GameUIDemo() {
        actionButtons = new ToggleButton[ACTION_NAMES.length];
        for(int i=0 ; i<ACTION_NAMES.length ; i++) {
            actionButtons[i] = new ToggleButton();
            actionButtons[i].setTheme(ACTION_NAMES[i]);
            add(actionButtons[i]);
        }

        btnPause = new ToggleButton();
        btnPause.setTheme("pause");
        add(btnPause);

        btnArmageddon = new ToggleButton();
        btnArmageddon.setTheme("armageddon");
        add(btnArmageddon);

        fpsCounter = new FPSCounter();
        add(fpsCounter);

        lastSelectedRadialEntry = new Label();
        lastSelectedRadialEntry.setText("Right click on the background");
        add(lastSelectedRadialEntry);
        
        digits = new SimpleChangableListModel<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        wheels = new ArrayList<WheelWidget<String>>();
        for(int i=0 ; i<4 ; i++) {
            WheelWidget<String> wheel = new WheelWidget<String>(digits);
            wheels.add(wheel);
            wheel.setCyclic(true);
            add(wheel);
        }
    }

    @Override
    protected void layout() {
        int x = 10;
        int y = 40;
        
        for(ToggleButton b : actionButtons) {
            b.setPosition(x, y);
            b.adjustSize();
            y += b.getHeight() + 5;
        }

        x = getInnerWidth() - 10;
        y = 10;

        btnPause.adjustSize();
        x -= btnPause.getWidth() + 5;
        btnPause.setPosition(x, y);

        btnArmageddon.adjustSize();
        x -= btnArmageddon.getWidth() + 5;
        btnArmageddon.setPosition(x, y);

        fpsCounter.adjustSize();
        fpsCounter.setPosition(
                getInnerWidth() - fpsCounter.getWidth(),
                getInnerHeight() - fpsCounter.getHeight());

        lastSelectedRadialEntry.adjustSize();
        lastSelectedRadialEntry.setPosition(
                getInnerWidth()/2 - lastSelectedRadialEntry.getWidth()/2,
                getInnerBottom() - lastSelectedRadialEntry.getHeight());
        
        int wheelsWidth = 0;
        for(WheelWidget<String> wheel : wheels) {
            wheel.adjustSize();
            wheelsWidth += wheel.getWidth();
        }
        x = getInnerX() + (getInnerWidth()-wheelsWidth)/2;
        y = getInnerY() + (getInnerHeight()-wheels.get(0).getHeight())/2;
        for(WheelWidget<String> wheel : wheels) {
            wheel.setPosition(x, y);
            x += wheel.getWidth();
        }
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
                break;
            case MOUSE_BTNDOWN:
                if(evt.getMouseButton() == Event.MOUSE_RBUTTON) {
                    return createRadialMenu().openPopup(evt);
                }
                break;
        }
        return evt.isMouseEventNoWheel();
    }

    RadialPopupMenu createRadialMenu() {
        RadialPopupMenu rpm = new RadialPopupMenu(this);
        for(int i=0 ; i<10 ; i++) {
            final int idx = i;
            rpm.addButton("star", new Runnable() {
                public void run() {
                    lastSelectedRadialEntry.setText("Selected " + idx);
                }
            });
        }
        return rpm;
    }
}
