/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.mars_sim.msp.ui.swing.tool;

/*
import com.jme3.app.Applcation;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.util.JmeFormatter;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;

public class MarsViewer
extends ToolWindow
implements InternalFrameListener {

	private static final long serialVersionUID = 1L;

	// default logger.
	//private static Logger logger = Logger.getLogger(SettlementWindow.class.getName());

	public static final String NAME = Msg.getString("MarsViewer.title"); //$NON-NLS-1$

	public static final int HORIZONTAL = 600;
	public static final int VERTICAL = 600;


    private static JmeCanvasContext context;
    private static Canvas canvas;
    private static Applcation app;
    private static JInternalFrame frame;

    private static Container canvasPanel1, canvasPanel2;
    private static Container currentPanel;
    private static JTabbedPane tabbedPane;
    private MainDesktopPane desktop;


    public MarsViewer(MainDesktopPane desktop) {
        //super("Mars Viewer", false, true, false, true);
		super("Mars Viewer", desktop);

        this.desktop = desktop;

        frame = this;

		//setSize(new Dimension(512, 512));

		setResizable(false);

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		setPreferredSize(new Dimension(HORIZONTAL, VERTICAL));
		setMaximizable(true);

		setLocation(400, 400);

		//setContentPane();
		createJMECanvas();

		//desktop.add(this);

		//Dimension desktopSize = desktop.getParent().getSize();
	    //Dimension jInternalFrameSize = frame.getSize();
	    //int width = (desktopSize.width - jInternalFrameSize.width) / 2;
	    //int height = (desktopSize.height - jInternalFrameSize.height) / 2;
	    //setLocation(width, height);

		pack();
	    setVisible(true);

        System.out.println("done with JmeViewer's constructor");
    }


    private static void createTabs(){
        tabbedPane = new JTabbedPane();

        canvasPanel1 = new JPanel();
        canvasPanel1.setLayout(new BorderLayout());
        tabbedPane.addTab("jME3 Canvas 1", canvasPanel1);

        canvasPanel2 = new JPanel();
        canvasPanel2.setLayout(new BorderLayout());
        tabbedPane.addTab("jME3 Canvas 2", canvasPanel2);

        frame.getContentPane().add(tabbedPane);
        //setContentPane(tabbedPane);

        currentPanel = canvasPanel1;

        System.out.println("done with createTabs()");
    }

    private void createMenu(){
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu menuTortureMethods = new JMenu("Canvas Torture Methods");
        menuBar.add(menuTortureMethods);

        final JMenuItem itemRemoveCanvas = new JMenuItem("Remove Canvas");
        menuTortureMethods.add(itemRemoveCanvas);
        itemRemoveCanvas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (itemRemoveCanvas.getText().equals("Remove Canvas")){
                    currentPanel.remove(canvas);

                    itemRemoveCanvas.setText("Add Canvas");
                }else if (itemRemoveCanvas.getText().equals("Add Canvas")){
                    currentPanel.add(canvas, BorderLayout.CENTER);

                    itemRemoveCanvas.setText("Remove Canvas");
                }
            }
        });

        final JMenuItem itemHideCanvas = new JMenuItem("Hide Canvas");
        menuTortureMethods.add(itemHideCanvas);
        itemHideCanvas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (itemHideCanvas.getText().equals("Hide Canvas")){
                    canvas.setVisible(false);
                    itemHideCanvas.setText("Show Canvas");
                }else if (itemHideCanvas.getText().equals("Show Canvas")){
                    canvas.setVisible(true);
                    itemHideCanvas.setText("Hide Canvas");
                }
            }
        });

        final JMenuItem itemSwitchTab = new JMenuItem("Switch to tab #2");
        menuTortureMethods.add(itemSwitchTab);
        itemSwitchTab.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
               if (itemSwitchTab.getText().equals("Switch to tab #2")){
                   canvasPanel1.remove(canvas);
                   canvasPanel2.add(canvas, BorderLayout.CENTER);
                   currentPanel = canvasPanel2;
                   itemSwitchTab.setText("Switch to tab #1");
               }else if (itemSwitchTab.getText().equals("Switch to tab #1")){
                   canvasPanel2.remove(canvas);
                   canvasPanel1.add(canvas, BorderLayout.CENTER);
                   currentPanel = canvasPanel1;
                   itemSwitchTab.setText("Switch to tab #2");
               }
           }
        });
        
        
//
  
 
        JMenuItem itemSwitchLaf = new JMenuItem("Switch Look and Feel");
        menuTortureMethods.add(itemSwitchLaf);
        itemSwitchLaf.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Throwable t){
                    t.printStackTrace();
                }
                SwingUtilities.updateComponentTreeUI(frame);
                //frame.pack();
            }
        });

        JMenuItem itemSmallSize = new JMenuItem("Set size to (0, 0)");
        menuTortureMethods.add(itemSmallSize);
        itemSmallSize.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                Dimension preferred = frame.getPreferredSize();
                frame.setPreferredSize(new Dimension(0, 0));
                //frame.pack();
                frame.setVisible(true);
                frame.setPreferredSize(preferred);
            }
        });
//


        JMenuItem itemKillCanvas = new JMenuItem("Stop/Start Canvas");
        menuTortureMethods.add(itemKillCanvas);
        itemKillCanvas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentPanel.remove(canvas);
                app.stop(true);

                //createCanvas(appClass);
                createCanvas();

                currentPanel.add(canvas, BorderLayout.CENTER);
                //frame.pack();
                frame.setVisible(true);
                startApp();
            }
        });

        JMenuItem itemExit = new JMenuItem("Exit");
        menuTortureMethods.add(itemExit);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                frame.dispose();
                app.stop();
            }
        });
        System.out.println("done with createMenu()");
    }

    private void createFrame(){
        //frame = new JFrame("Test");
        //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //frame.addWindowListener(new WindowAdapter(){
        //    @Override
       //     public void windowClosed(WindowEvent e) {
        //        app.stop();
        //    }
        //});

        createTabs();
        createMenu();
        System.out.println("done with createFrame()");

    }

    public static void createCanvas() { //String appClass){

        AppSettings settings = new AppSettings(true);
        settings.setWidth(512);
        settings.setHeight(480);

		app = new MarsAssets();

        app.setPauseOnLostFocus(false);

        // turn off splash menu
        app.setSettings(settings);

        app.createCanvas();
        //app.startCanvas();

        // turn off splash menu
        //app.setShowSettings(false);

        context = (JmeCanvasContext) app.getContext();
        canvas = context.getCanvas();
        context.setSystemListener(app);
        canvas.setSize(settings.getWidth(), settings.getHeight());


//
        SwingCanvasTest canvasApplication = new SwingCanvasTest();
        canvasApplication.setSettings(settings);
        canvasApplication.createCanvas(); // create canvas!
        JmeCanvasContext ctx = (JmeCanvasContext) canvasApplication.getContext();
        ctx.setSystemListener(canvasApplication);
        Dimension dim = new Dimension(640, 480);
        ctx.getCanvas().setPreferredSize(dim);
//


        System.out.println("done with createCanvas()");
    }

    public static void startApp(){
        app.startCanvas();
        app.enqueue(new Callable<Void>(){
            public Void call(){
                if (app instanceof SimpleApplication){
                    SimpleApplication simpleApp = (SimpleApplication) app;
                    simpleApp.getFlyByCamera().setDragToRotate(true);
                }
                return null;
            }
        });
        System.out.println("done with startApp()");
    }

    public void createJMECanvas(){

    	//object.setFocusable(false);
    	//setPauseOnLostFocus(false);

        JmeFormatter formatter = new JmeFormatter();

        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);

        Logger.getLogger("").removeHandler(Logger.getLogger("").getHandlers()[0]);
        Logger.getLogger("").addHandler(consoleHandler);


        SwingUtilities.invokeLater(new Runnable(){
            public void run(){

                createCanvas();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }


            	JPopupMenu.setDefaultLightWeightPopupEnabled(false);

                createFrame();

                currentPanel.add(canvas, BorderLayout.CENTER);

                startApp();

                //frame.setVisible(true); // will crash
            }
        });


//

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                createCanvas();

            	JPopupMenu.setDefaultLightWeightPopupEnabled(false);

                createFrame();

                currentPanel.add(canvas, BorderLayout.CENTER);

                startApp();

            }
        });
//

        System.out.println("done with createJMECanvas()");
    }


	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void internalFrameClosing(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void internalFrameClosed(InternalFrameEvent e) {
		// TODO Auto-generated method stub
        frame.dispose();
        app.stop();
	}


	@Override
	public void internalFrameIconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void internalFrameDeiconified(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}


	@Override
	public void internalFrameDeactivated(InternalFrameEvent e) {
		// TODO Auto-generated method stub

	}

}
*/