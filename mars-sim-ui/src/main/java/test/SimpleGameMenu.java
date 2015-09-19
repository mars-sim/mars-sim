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
package test;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author Matthias Mann
 */
public class SimpleGameMenu extends Widget {

    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
            Display.setTitle("TWL Simple Game Menu Demo");
            Display.setVSyncEnabled(true);

            LWJGLRenderer renderer = new LWJGLRenderer();
            SimpleGameMenu gameUI = new SimpleGameMenu();
            GUI gui = new GUI(gameUI, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    SimpleGameMenu.class.getResource("simpleGameMenu.xml"), renderer);
            gui.applyTheme(theme);

            while(!Display.isCloseRequested() && !gameUI.quit) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                gui.update();
                Display.update();
            }

            gui.destroy();
            theme.destroy();
        } catch (Exception ex) {
            TestUtils.showErrMsg(ex);
        }
        Display.destroy();
    }

    private final FPSCounter fpsCounter;
    private final Button[] buttons;

    public boolean quit;

    public SimpleGameMenu() {
        buttons = new Button[3];
        buttons[0] = new Button("Start Game");
        buttons[1] = new Button("Options");
        buttons[2] = new Button("Quit");

        for(int i=0 ; i<buttons.length ; i++) {
            add(buttons[i]);
        }
        
        fpsCounter = new FPSCounter();
        add(fpsCounter);
    }

    private static final int TITLE_HEIGHT = 200;
    private static final int BUTTON_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 50;

    @Override
    protected void layout() {
        int centerX = getInnerX() + getInnerWidth()/2;
        int distY = (getInnerHeight() - TITLE_HEIGHT) / (buttons.length + 1);

        for(int i=0 ; i<buttons.length ; i++) {
            buttons[i].setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
            buttons[i].setPosition(centerX - BUTTON_WIDTH/2,
                    TITLE_HEIGHT + (i+1)*distY - BUTTON_HEIGHT/2);
        }

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

}
