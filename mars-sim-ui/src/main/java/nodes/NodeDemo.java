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
package nodes;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import test.TestUtils;

/**
 * @author Matthias Mann
 */
public class NodeDemo {

    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(1024, 600));
            Display.create();
            Display.setTitle("TWL Node Demo");
            Display.setVSyncEnabled(true);

            NodeArea nodeArea = new NodeArea();
            ScrollPane scrollPane = new ScrollPane(nodeArea);
            scrollPane.setExpandContentSize(true);

            LWJGLRenderer renderer = new LWJGLRenderer();
            //renderer.setUseQuadsForLines(true);
            GUI gui = new GUI(scrollPane, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    NodeDemo.class.getResource("nodes.xml"), renderer);
            gui.applyTheme(theme);
            gui.update();
            
            Node nodeSource = nodeArea.addNode("Source");
            nodeSource.setPosition(50, 50);
            Pad nodeSourceColor = nodeSource.addPad("Color", false);
            Pad nodeSourceAlpha = nodeSource.addPad("Alpha", false);

            Node nodeSink = nodeArea.addNode("Sink");
            nodeSink.setPosition(350, 200);
            Pad nodeSinkColor = nodeSink.addPad("Color", true);

            nodeArea.addConnection(nodeSourceColor, nodeSinkColor);

            while(!Display.isCloseRequested()) {
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

}
