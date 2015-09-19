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
package gameui;

import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.RadialPopupMenu;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import test.TestUtils;

/**
 * A simple game UI demo using DialogLayout
 *
 * @author Matthias Mann
 */
public class GameUIDemo2 extends DialogLayout {

    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
            Display.setTitle("TWL Game UI Demo");
            Display.setVSyncEnabled(true);

            LWJGLRenderer renderer = new LWJGLRenderer();
            GameUIDemo2 gameUI = new GameUIDemo2();
            GUI gui = new GUI(gameUI, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    GameUIDemo2.class.getResource("gameui.xml"), renderer);
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

    public GameUIDemo2() {
        actionButtons = new ToggleButton[ACTION_NAMES.length];
        for(int i=0 ; i<ACTION_NAMES.length ; i++) {
            actionButtons[i] = new ToggleButton();
            actionButtons[i].setTheme(ACTION_NAMES[i]);
        }

        btnPause = new ToggleButton();
        btnPause.setTheme("pause");

        btnArmageddon = new ToggleButton();
        btnArmageddon.setTheme("armageddon");

        fpsCounter = new FPSCounter();

        lastSelectedRadialEntry = new Label();
        lastSelectedRadialEntry.setText("Right click on the background");
        add(lastSelectedRadialEntry);

        // create the groups for the action buttons (aligned top left)
        Group actionButtonsH = createSequentialGroup()
                .addGap("actionButtonsLeft")
                .addGroup(createParallelGroup(actionButtons))
                .addGap();
        Group actionButtonsV = createSequentialGroup()
                .addGap("actionButtonsTop")
                .addWidgets(actionButtons)
                .addGap();

        // create the groups for the game control buttons (aligned top right)
        Group gameCtrlH = createSequentialGroup()
                .addGap()
                .addWidget(btnArmageddon)
                .addWidget(btnPause)
                .addGap("gameCtrlRight");
        Group gameCtrlV = createSequentialGroup()
                .addGap("gameCtrlTop")
                .addGroup(createParallelGroup(btnArmageddon, btnPause))
                .addGap();
        
        // create the groups for the status display (aligned bottom right)
        Group statusH = createSequentialGroup()
                .addGap()
                .addWidget(fpsCounter)
                .addGap("statusRight");
        Group statusV = createSequentialGroup()
                .addGap()
                .addWidget(fpsCounter)
                .addGap("statusBottom");

        // create the groups for the radial menu message display (aligned bottom center)
        Group radialMenuMessageH = createSequentialGroup()
                .addGap()
                .addWidget(lastSelectedRadialEntry)
                .addGap();
        Group radialMenuMessageV = createSequentialGroup()
                .addGap()
                .addWidget(lastSelectedRadialEntry)
                .addGap("statusBottom");

        // now overlay all groups
        setHorizontalGroup(createParallelGroup(actionButtonsH, gameCtrlH, statusH, radialMenuMessageH));
        setVerticalGroup(createParallelGroup(actionButtonsV, gameCtrlV, statusV, radialMenuMessageV));
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
