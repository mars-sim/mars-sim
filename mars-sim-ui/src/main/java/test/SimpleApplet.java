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

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;

/**
 *
 * @author Matthias Mann
 */
public class SimpleApplet extends Applet {

    /** The Canvas where the LWJGL Display is added */
    Canvas display_parent;
    /** Thread which runs the main game loop */
    Thread gameThread;

    SimpleTest simpleTest;

    public void startLWJGL() {
        gameThread = new Thread() {
            @Override
            public void run() {
                try {
                    Display.setParent(display_parent);
                    Display.setVSyncEnabled(true);
                    Display.create();
                    gameLoop();
                    Display.destroy();
                } catch (LWJGLException e) {
                    e.printStackTrace();
                }
            }
        };
        gameThread.start();
    }

    /**
     * Tell game loop to stop running, after which the LWJGL Display will be destoryed.
     * The main thread will wait for the Display.destroy() to complete
     */
    private void stopLWJGL() {
        if(simpleTest != null) {
            simpleTest.closeRequested = true;
        }
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    /**
     * Applet Destroy method will remove the canvas, before canvas is destroyed it will notify
     * stopLWJGL() to stop main game loop and to destroy the Display
     */
    @Override
    public void destroy() {
        remove(display_parent);
        super.destroy();
        System.out.println("Clear up");
    }

    /**
     * initialise applet by adding a canvas to it, this canvas will start the LWJGL Display and game loop
     * in another thread. It will also stop the game loop and destroy the display on canvas removal when
     * applet is destroyed.
     */
    @Override
    public void init() {
        setLayout(new BorderLayout());
        try {
            display_parent = new Canvas() {
                @Override
                public final void addNotify() {
                    super.addNotify();
                    startLWJGL();
                }
                @Override
                public final void removeNotify() {
                    stopLWJGL();
                    super.removeNotify();
                }
            };
            display_parent.setSize(getWidth(), getHeight());
            add(display_parent);
            display_parent.setFocusable(true);
            display_parent.requestFocus();
            display_parent.setIgnoreRepaint(true);
            //setResizable(true);
            setVisible(true);
        } catch (Exception e) {
            System.err.println(e);
            throw new RuntimeException("Unable to create display");
        }
    }

    public void gameLoop() {
        try {
            simpleTest = new SimpleTest();
            simpleTest.mainLoop(true);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            pw.flush();
            Sys.alert("Error", sw.toString());
        }
    }
}
