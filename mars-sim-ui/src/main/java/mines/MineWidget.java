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

import de.matthiasmann.twl.CallbackWithReason;
import de.matthiasmann.twl.Event;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Label.CallbackReason;
import de.matthiasmann.twl.ThemeInfo;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.renderer.Image;

/**
 *
 * @author Matthias Mann
 */
public final class MineWidget extends Widget {
    
    public interface Callback {
        public void victory(int time);
    }
    
    private final Label gameOverLabel;
    private final Image images[];
    private final Image imagesLost[];
    
    private Label gameTimeLabel;
    private MineField mineField;
    private Callback callback;
    private int cellWidth = 1;
    private int cellHeight = 1;
    private int lastGameTime = -1;
    private int lastNumFlags = -1;
    
    public MineWidget() {
        this.gameOverLabel = new Label();
        this.images = new Image[256];
        this.imagesLost = new Image[256];
        
        gameOverLabel.setTheme("gameOverLabel");
        gameOverLabel.setVisible(false);
        gameOverLabel.addCallback(new CallbackWithReason<CallbackReason>() {
            public void callback(CallbackReason reason) {
                if(reason == CallbackReason.DOUBLE_CLICK) {
                    restart();
                }
            }
        });
        add(gameOverLabel);
    }

    public void setMineField(MineField mineField) {
        this.mineField = mineField;
        invalidateLayout();
    }

    public Label getGameTimeLabel() {
        return gameTimeLabel;
    }
    
    public void setGameTimeLabel(Label gameTimeLabel) {
        this.gameTimeLabel = gameTimeLabel;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public void restart() {
        if(mineField != null) {
            gameOverLabel.setVisible(false);
            mineField.reset();
            mouseDownX = -1;
            mouseDownY = -1;
        }
    }
    
    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        if(mineField != null) {
            minWidth = Math.max(minWidth, cellWidth*mineField.getSize().width);
        }
        return minWidth;
    }

    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        if(mineField != null) {
            minHeight = Math.max(minHeight, cellHeight*mineField.getSize().height);
        }
        return minHeight;
    }

    @Override
    protected void layout() {
        layoutChildFullInnerArea(gameOverLabel);
    }

    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        Image[] numberImages = new Image[16];
        for(int i=0 ; i<=8 ; i++) {
            numberImages[i] = themeInfo.getImage("number."+i);
        }
        Image flagImage = themeInfo.getImage("flag");
        Image wrongFlagImage = themeInfo.getImage("wrongFlag");
        Image mineImage = themeInfo.getImage("mine");
        Image closedImage = themeInfo.getImage("closed");
        Image boomImage = themeInfo.getImage("boom");
        
        for(int idx=0 ; idx<256 ; idx++) {
            Image img;
            
            if((idx & MineField.FLAG) != 0) {
                img = flagImage;
            } else if((idx & MineField.SIMOPEN) != 0) {
                img = numberImages[0];
            } else if((idx & MineField.OPEN) == 0) {
                img = closedImage;
            } else {
                img = numberImages[idx & MineField.NUMBER_MASK];
            }
            images[idx] = img;
            
            
            if((idx & MineField.FLAG) != 0) {
                if((idx & MineField.MINE) == 0) {
                    img = wrongFlagImage;
                } else {
                    img = flagImage;
                }
            } else if((idx & MineField.MINE) != 0) {
                if((idx & MineField.OPEN) != 0) {
                    img = boomImage;
                } else {
                    img = mineImage;
                }
            } else if((idx & MineField.OPEN) != 0) {
                img = numberImages[idx & MineField.NUMBER_MASK];
            } else {
                img = closedImage;
            }
            imagesLost[idx] = img;
        }
        
        if(flagImage != null) {
            cellWidth = flagImage.getWidth();
            cellHeight = flagImage.getHeight();
        } else {
            cellWidth = 1;
            cellHeight = 1;
        }
    }

    private int mouseDownX = -1;
    private int mouseDownY = -1;
    private int mouseDownBtn = -1;
    
    @Override
    protected boolean handleEvent(Event evt) {
        if(super.handleEvent(evt)) {
            return true;
        }
        
        int x = (evt.getMouseX() - getInnerX()) / cellWidth;
        int y = (evt.getMouseY() - getInnerY()) / cellHeight;
        
        if(mineField != null && !mineField.isGameOver()) {
            switch(evt.getType()) {
                case MOUSE_BTNDOWN:
                    if(mouseDownX >= 0) {
                        mineField.clearSimOpen(mouseDownX, mouseDownY);
                    }
                    if(mineField.isValid(x, y)) {
                        mouseDownX = x;
                        mouseDownY = y;
                        mouseDownBtn = evt.getMouseButton();
                        if(mouseDownBtn == Event.MOUSE_LBUTTON) {
                            mineField.setSimOpen(x, y);
                        }
                    } else {
                        mouseDownX = -1;
                    }
                    break;

                case MOUSE_BTNUP:
                    if(mouseDownX >= 0) {
                        mineField.clearSimOpen(mouseDownX, mouseDownY);
                    }
                    if(mineField.isValid(x, y) && mouseDownX == x && mouseDownY == y) {
                        switch(evt.getMouseButton()) {
                            case Event.MOUSE_LBUTTON:
                                mineField.open(x, y);
                                break;

                            case Event.MOUSE_RBUTTON:
                                mineField.toggleFlag(x, y);
                                break;
                        }
                    }
                    break;
                    
                case MOUSE_DRAGGED:
                    if(mouseDownX >= 0) {
                        if(mouseDownX != x || mouseDownY != y) {
                            mineField.clearSimOpen(mouseDownX, mouseDownY);
                        } else if(mouseDownBtn == Event.MOUSE_LBUTTON) {
                            mineField.setSimOpen(x, y);
                        }
                    }
                    break;
            }
            
            if(mineField.isGameOver()) {
                if(mineField.isVictory()) {
                    if(callback != null) {
                        callback.victory(mineField.getGameTime());
                    }
                    gameOverLabel.setText("Victory !");
                } else {
                    gameOverLabel.setText("Game Over");
                }
                gameOverLabel.setVisible(true);
            }
        }
        
        return evt.isMouseEventNoWheel();
    }

    @Override
    protected void paintWidget(GUI gui) {
        if(mineField != null) {
            final int width = mineField.getSize().width;
            final int height = mineField.getSize().height;
            final Image[] imageSet = mineField.isGameLost() ? imagesLost : images;
            
            int ry = getInnerY();
            for(int y=0 ; y<height ; y++,ry+=cellHeight) {
                int rx = getInnerX();
                for(int x=0 ; x<width ; x++,rx+=cellWidth) {
                    Image img = imageSet[mineField.get(x, y)];
                    
                    if(img != null) {
                        img.draw(null, rx, ry, cellWidth, cellHeight);
                    }
                }
            }
            
            updateGameTimeLabel();
        }
    }
    
    private void updateGameTimeLabel() {
        if(mineField != null && gameTimeLabel != null) {
            int gameTime = mineField.getGameTime();
            int numFlags = mineField.getNumFlags();
            if(gameTime != lastGameTime || numFlags != lastNumFlags) {
                lastGameTime = gameTime;
                lastNumFlags = numFlags;
                gameTimeLabel.setText(String.format("Time %d:%02d  Flags %d/%d",
                        gameTime / 60, gameTime % 60,
                        numFlags, mineField.getSize().numMines));
            }
        }
    }
}
