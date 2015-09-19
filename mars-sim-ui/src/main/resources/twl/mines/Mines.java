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
package mines;

import de.matthiasmann.twl.ActionMap.Action;
import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.ScrollPane.Fixed;
import de.matthiasmann.twl.SimpleDialog;
import de.matthiasmann.twl.Table;
import de.matthiasmann.twl.TableRowSelectionManager;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.model.TableSingleSelectionModel;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import mines.MineWidget.Callback;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import test.TestUtils;

/**
 *
 * @author Matthias Mann
 */
public class Mines extends Widget {
    
    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.create();
            Display.setTitle("TWL Mines");
            Display.setVSyncEnabled(true);

            LWJGLRenderer renderer = new LWJGLRenderer();
            Mines mines = new Mines();
            mines.getOrCreateActionMap().addMapping(mines);
            mines.startGame(16, 16, 40);
            GUI gui = new GUI(mines, renderer);

            ThemeManager theme = ThemeManager.createThemeManager(
                    Mines.class.getResource("mines.xml"), renderer);
            gui.applyTheme(theme);

            while(!Display.isCloseRequested() && !mines.quit) {
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

    private final FPSCounter fpsCounter;
    private final MineWidget mineWidget;
    private final Label gameTimeLabel;
    private final Button restartButton;
    private final Button highscoresButton;
    private final EditField nameEditField;
    private final Highscores highscores;
    private File highscoresFile;
    private MineField mineField;
    private boolean quit;

    public Mines() {
        fpsCounter = new FPSCounter();
        fpsCounter.setVisible(false);
        add(fpsCounter);
        
        mineWidget = new MineWidget();
        add(mineWidget);
        
        gameTimeLabel = new Label();
        gameTimeLabel.setTheme("gameTimeLabel");
        add(gameTimeLabel);
        
        nameEditField = new EditField();
        nameEditField.setTheme("nameEditField");
        try {
            nameEditField.setText(System.getProperty("user.name"));
        } catch(Exception ex) {
            // ignore
        }
        
        highscores = new Highscores();
        
        try {
            File folder = new File(new File(System.getProperty("user.home")), ".twl");
            folder.mkdirs();
            highscoresFile = new File(folder, "mines.highscores");
            highscores.read(highscoresFile);
        } catch(Exception ex) {
            getLogger().log(Level.WARNING, "Could not read highscore", ex);
        }
        
        mineWidget.setGameTimeLabel(gameTimeLabel);
        mineWidget.setCallback(new Callback() {
            public void victory(int time) {
                enterHighscore(time);
            }
        });
        
        highscoresButton = new Button();
        highscoresButton.setTheme("highscoresButton");
        highscoresButton.addCallback(new Runnable() {
            public void run() {
                showHighscores();
            }
        });
        add(highscoresButton);
        
        restartButton = new Button();
        restartButton.setTheme("restartButton");
        restartButton.addCallback(new Runnable() {
            public void run() {
                restart();
            }
        });
        add(restartButton);
    }

    @Override
    protected void layout() {
        mineWidget.adjustSize();
        mineWidget.setPosition(
                getInnerX() + (getInnerWidth()-mineWidget.getWidth())/2,
                getInnerY() + (getInnerHeight()-mineWidget.getHeight())/2);
        
        restartButton.adjustSize();
        restartButton.setPosition(mineWidget.getRight() - restartButton.getWidth(), mineWidget.getBottom());
        
        gameTimeLabel.setSize(mineWidget.getWidth() - restartButton.getWidth(), gameTimeLabel.getPreferredHeight());
        gameTimeLabel.setPosition(mineWidget.getX(), mineWidget.getBottom());
        
        highscoresButton.setSize(mineWidget.getWidth(), highscoresButton.getPreferredHeight());
        highscoresButton.setPosition(mineWidget.getX(), mineWidget.getY() - highscoresButton.getHeight());
        
        // fpsCounter is bottom right
        fpsCounter.adjustSize();
        fpsCounter.setPosition(
                getInnerWidth() - fpsCounter.getWidth(),
                getInnerHeight() - fpsCounter.getHeight());
    }
    
    public void startGame(int width, int height, int numMines) {
        MineFieldSize size = new MineFieldSize(width, height, numMines);
        mineField = new MineField(size);
        mineWidget.setMineField(mineField);
    }
    
    void enterHighscore(final int time) {
        final Date date = new Date();
        final MineFieldSize size = mineField.getSize();
        
        SimpleDialog dialog = new SimpleDialog();
        dialog.setTitle("Victory");
        dialog.setMessage(nameEditField);
        dialog.setTheme("enterNameDialog");
        dialog.setOkCallback(new Runnable() {
            public void run() {
                addToHighscore(size, time, date);
            }
        });
        dialog.showDialog(mineWidget);
    }
    
    void addToHighscore(MineFieldSize size, int time, Date date) {
        Highscores.Entry entry = new Highscores.Entry(time, nameEditField.getText(), date);
        int pos = highscores.addEntry(size, entry);
        if(highscoresFile != null) {
            try {
                highscores.secureWrite(highscoresFile);
            } catch(IOException ex) {
                getLogger().log(Level.WARNING, "Could not write highscores", ex);
            }
        }
        showHighscores(size, pos);
    }
    
    void showHighscores() {
        if(mineField != null) {
            showHighscores(mineField.getSize(), -1);
        }
    }
    
    private void showHighscores(MineFieldSize size, int row) {
        Table table = new Table(new HighscoreTableModel(highscores.getEntries(size)));
        table.setTheme("resultTable");
        TableSingleSelectionModel selModel = new TableSingleSelectionModel();
        table.setSelectionManager(new TableRowSelectionManager(selModel));
        
        ScrollPane pane = new ScrollPane(table);
        pane.setFixed(Fixed.HORIZONTAL);
        
        SimpleDialog dialog = new SimpleDialog();
        dialog.setTitle(String.format("%dx%d %d mines", size.width, size.height, size.numMines));
        dialog.setMessage(pane);
        dialog.setTheme("highscoreDialog");
        dialog.showDialog(this);
        
        if(row >= 0) {
            selModel.setSelection(row, row);
            table.scrollToRow(row);
        }
    }
    
    @Action
    public void restart() {
        mineWidget.restart();
    }
    
    @Action
    public void toggleFPS() {
        fpsCounter.setVisible(!fpsCounter.isVisible());
    }
    
    @Action
    public void quit() {
        quit = true;
    }
    
    private static Logger getLogger() {
        return Logger.getLogger(Mines.class.getName());
    }
}
