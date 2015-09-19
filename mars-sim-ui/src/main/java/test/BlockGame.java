/*
 * Copyright (c) 2008-2009, Matthias Mann
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

import de.matthiasmann.twl.Alignment;
import de.matthiasmann.twl.DialogLayout;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ParameterList;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Timer;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.Image;
import java.util.Arrays;
import java.util.Random;

/**
 * A simple BlockGame Demo
 *
 * @author Matthias Mann
 */
public class BlockGame extends DialogLayout {

    Image[] blockImages;
    int blockWidth;
    int blockHeight;

    Timer timer;
    int nextBlocksAvail;
    int curBlock;
    int curRotation;
    int nextBlock;
    int curX;
    int curY;

    int score;
    int rowsCompleted;
    int level;
    int speed;
    int tickCounter;

    int moveX;
    int moveY;
    int moveRot;

    private final int nextBlocks[];
    private final Grid playfield;
    private final Grid playfieldFixed;
    private final Grid nextBlockGrid;
    private final GridWidget playfieldWidget;
    private final GridWidget nextBlockWidget;
    private final Label scoreDisplay;
    private final Label levelDisplay;
    private final Random random;

    public BlockGame() {
        this.nextBlocks = new int[BLOCKS.length * 2];
        this.playfield = new Grid(10, 22);
        this.playfieldFixed = new Grid(10, 22);
        this.nextBlockGrid = new Grid(4, 4);
        this.playfieldWidget = new GridWidget(playfield, 2);
        this.nextBlockWidget = new GridWidget(nextBlockGrid, 0);
        this.scoreDisplay = new Label();
        this.levelDisplay = new Label();
        this.random = new Random();

        Label lScore = new Label("Score");
        lScore.setLabelFor(scoreDisplay);

        Label lLevel = new Label("Level");
        lLevel.setLabelFor(levelDisplay);

        setHorizontalGroup(createSequentialGroup()
                .addWidget(playfieldWidget)
                .addGroup(createParallelGroup()
                    .addWidget(nextBlockWidget, Alignment.RIGHT)
                    .addGroup(createSequentialGroup()
                        .addGroup(createParallelGroup()
                            .addWidget(lScore)
                            .addWidget(lLevel))
                        .addGroup(createParallelGroup()
                            .addWidget(scoreDisplay)
                            .addWidget(levelDisplay)))));
        
        setVerticalGroup(createParallelGroup()
                .addWidget(playfieldWidget)
                .addGroup(createSequentialGroup()
                    .addWidget(nextBlockWidget)
                    .addGroup(createParallelGroup()
                        .addWidget(lScore)
                        .addWidget(scoreDisplay))
                    .addGroup(createParallelGroup()
                        .addWidget(lLevel)
                        .addWidget(levelDisplay))
                    .addGap()));

        level = 1;

        nextBlock = genNextBlock();
        nextBlock();

        setCanAcceptKeyboardFocus(true);
        updateScore();
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        applyThemeGame(themeInfo);
        invalidateLayout();
    }

    protected void applyThemeGame(ThemeInfo themeInfo) {
        ParameterList plImages = themeInfo.getParameterList("blockImages");
        blockImages = new Image[plImages.getSize()];
        blockWidth = 0;
        blockHeight = 0;
        for(int i=0 ; i<plImages.getSize() ; i++) {
            Image img = plImages.getImage(i);
            blockImages[i] = img;
            if(img != null) {
                blockWidth = Math.max(blockWidth, img.getWidth());
                blockHeight = Math.max(blockHeight, img.getHeight());
            }
        }
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        timer = gui.createTimer();
        timer.setContinuous(true);
        timer.setCallback(new Runnable() {
            public void run() {
                gameTick();
            }
        });
        timer.setDelay(100);
        timer.start();
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        super.beforeRemoveFromGUI(gui);
        timer.stop();
        timer = null;
    }

    private int genNextBlock() {
        if(nextBlocksAvail == 0) {
            nextBlocksAvail = nextBlocks.length;
            for(int i=0 ; i<nextBlocksAvail ; i++) {
                nextBlocks[i] = i % BLOCKS.length;
            }
            for(int i=0 ; i<nextBlocksAvail ; i++) {
                int j = random.nextInt(nextBlocksAvail);
                int t = nextBlocks[i];
                nextBlocks[i] = nextBlocks[j];
                nextBlocks[j] = t;
            }
        }
        return nextBlocks[--nextBlocksAvail];
    }

    private void nextBlock() {
        curBlock = nextBlock;
        curX = 4;
        curY = 0;
        curRotation = 0;
        nextBlock = genNextBlock();
        nextBlockGrid.clear();
        nextBlockGrid.placeBlock(0, 0, BLOCKS[nextBlock][0], nextBlock+1);
        speed = Math.max(1, 11 - level);
    }

    private void updateScore() {
        scoreDisplay.setText(Integer.toString(score));
        levelDisplay.setText(Integer.toString(level));
        invalidateLayout();
    }

    void gameTick() {
        if(++tickCounter > speed) {
            tickCounter = 0;
            moveY = 1;
        }
        
        int[] blockBits = BLOCKS[curBlock];

        if(moveRot != 0) {
            int nextRot = (curRotation + moveRot) % blockBits.length;
            if(playfieldFixed.canPlaceBlock(curX, curY, blockBits[nextRot])) {
                curRotation = nextRot;
            } else if(playfieldFixed.canPlaceBlock(curX+1, curY, blockBits[nextRot])) {//Tries to wall kick to the right
                curX++;
                curRotation = nextRot;
            } else if(playfieldFixed.canPlaceBlock(curX-1, curY, blockBits[nextRot])) {//Tries to wall kick to the left
                curX--;
                curRotation = nextRot;
            }
            moveX = 0;
        }

        if(moveX != 0 && playfieldFixed.canPlaceBlock(curX+moveX, curY, blockBits[curRotation])) {
            curX += moveX;
        }

        if(moveY > 0) {
            if(playfieldFixed.canPlaceBlock(curX, curY+moveY, blockBits[curRotation])) {
                curY += moveY;
            } else {
                playfieldFixed.placeBlock(curX, curY, blockBits[curRotation], curBlock+1);
                int rows = playfieldFixed.removeRows();
                if(rows > 0) {
                    rowsCompleted += rows;
                    level = 1 + rowsCompleted / 10;
                    score += rows*rows * level;
                    updateScore();
                }
                nextBlock();
            }
        }

        playfield.copyFrom(playfieldFixed);
        playfield.placeBlock(curX, curY, BLOCKS[curBlock][curRotation], curBlock+1);

        moveRot = 0;
        moveX = 0;
        moveY = 0;
    }

    @Override
    protected boolean handleEvent(Event evt) {
        if(super.handleEvent(evt)) {
            return true;
        }

        switch(evt.getType()) {
        case KEY_PRESSED:
            switch(evt.getKeyCode()) {
            case Event.KEY_UP:
                moveRot = 1;
                return true;
            case Event.KEY_LEFT:
                moveX = -1;
                return true;
            case Event.KEY_RIGHT:
                moveX = 1;
                return true;
            case Event.KEY_DOWN:
                moveY = 1;
                return true;
            }
            break;
        }

        return false;
    }

    static final int BLOCKS[][] = {
        {0x0F00, 0x4444},                   // ID
        {0x0330},                           // OD
        {0x0710, 0x3220, 0x0470, 0x2260},   // LD
        {0x0740, 0x2230, 0x0170, 0x6220},   // JD
        {0x0720, 0x2320, 0x0270, 0x2620},   // TD
        {0x0630, 0x1320},                   // SD
        {0x0360, 0x4620},                   // ZD
    };

    static class Grid {
        protected final int[] grid;
        protected final int width;
        protected final int height;

        public Grid(int width, int height) {
            this.grid = new int[height * width];
            this.width = width;
            this.height = height;
        }

        public void copyFrom(Grid src) {
            System.arraycopy(src.grid, 0, grid, 0, grid.length);
        }

        public void clear() {
            Arrays.fill(grid, 0);
        }

        public boolean canPlaceBlock(int x, int y, int block) {
            for(int iy=0 ; iy<4 ; iy++) {
                for(int ix=0 ; ix<4 ; ix++) {
                    int mask = 1 << (ix + 12 - 4*iy);
                    if((block & mask) != 0) {
                        if(y+iy < 0 || y+iy >= height || x+ix < 0 || x+ix >= width) {
                            return false;
                        }
                        int idx = x + ix + (y + iy) * width;
                        if(grid[idx] != 0) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        public void placeBlock(int x, int y, int block, int color) {
            for(int iy=0 ; iy<4 ; iy++) {
                for(int ix=0 ; ix<4 ; ix++) {
                    int mask = 1 << (ix + 12 - 4*iy);
                    if((block & mask) != 0) {
                        int idx = x + ix + (y + iy) * width;
                        grid[idx] = color;
                    }
                }
            }
        }

        public int removeRows() {
            int rdst = height;
            for(int rsrc=height ; rsrc-->0 ;) {
                boolean isFull = true;
                for(int c=0,idx=rsrc*width ; c<width ; c++) {
                    if(grid[idx++] == 0) {
                        isFull = false;
                        break;
                    }
                }

                if(!isFull) {
                    rdst--;
                    if(rsrc != rdst) {
                        System.arraycopy(grid, rsrc*width, grid, rdst*width, width);
                    }
                }
            }
            if(rdst > 0) {
                Arrays.fill(grid, 0, rdst*width, 0);
            }
            return rdst;
        }
    }

    class GridWidget extends Widget {
        private final Grid tg;
        private final int skipRows;

        public GridWidget(Grid tg, int skipRows) {
            this.tg = tg;
            this.skipRows = skipRows;
        }

        @Override
        protected void paintWidget(GUI gui) {
            if(blockImages != null) {
                int y = getInnerY();
                for(int r=skipRows ; r<tg.height ; r++) {
                    int idx = r * tg.width;
                    int x = getInnerX();
                    for(int c=0 ; c<tg.width ; c++) {
                        int color = tg.grid[idx++];
                        if(color < blockImages.length) {
                            Image img = blockImages[color];
                            if(img != null) {
                                img.draw(getAnimationState(), x, y);
                            }
                        }
                        x += blockWidth;
                    }
                    y += blockHeight;
                }
            }
        }

        @Override
        public int getPreferredInnerWidth() {
            return tg.width * blockWidth;
        }

        @Override
        public int getPreferredInnerHeight() {
            return (tg.height - skipRows) * blockHeight;
        }
    }
}
