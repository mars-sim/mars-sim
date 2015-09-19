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
package de.matthiasmann.twl;

import de.matthiasmann.twl.model.BooleanModel;

/**
 * A widget that slides in/out a selected edge of the screen.
 *
 * @author Matthias Mann
 */
public class PositionAnimatedPanel extends Widget {

    public enum Direction {
        TOP(0,-1),
        LEFT(-1,0),
        BOTTOM(0,1),
        RIGHT(1,0);
        
        final int x;
        final int y;
        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }
    };
    
    private final Widget animatedWidget;
    private MouseSensitiveRectangle rect;
    private Direction direction = Direction.TOP;
    private int moveSpeedIn;
    private int moveSpeedOut;
    private int auraSizeX;
    private int auraSizeY;
    private boolean forceVisible;
    private boolean forceJumps;

    private BooleanModel forceVisibleModel;
    private Runnable forceVisibleModelCallback;
    
    public PositionAnimatedPanel(Widget animatedWidget) {
        if(animatedWidget == null) {
            throw new NullPointerException("animatedWidget");
        }
        
        this.animatedWidget = animatedWidget;
        
        setClip(true);
        add(animatedWidget);
    }

    public Direction getHideDirection() {
        return direction;
    }

    public void setHideDirection(Direction direction) {
        if(direction == null) {
            throw new NullPointerException("direction");
        }
        this.direction = direction;
    }
    
    public int getMoveSpeedIn() {
        return moveSpeedIn;
    }

    public void setMoveSpeedIn(int moveSpeedIn) {
        this.moveSpeedIn = moveSpeedIn;
    }

    public int getMoveSpeedOut() {
        return moveSpeedOut;
    }

    public void setMoveSpeedOut(int moveSpeedOut) {
        this.moveSpeedOut = moveSpeedOut;
    }

    public int getAuraSizeX() {
        return auraSizeX;
    }

    public void setAuraSizeX(int auraSizeX) {
        this.auraSizeX = auraSizeX;
    }

    public int getAuraSizeY() {
        return auraSizeY;
    }

    public void setAuraSizeY(int auraSizeY) {
        this.auraSizeY = auraSizeY;
    }

    public boolean isForceVisible() {
        return forceVisible;
    }

    public void setForceVisible(boolean forceVisible) {
        this.forceVisible = forceVisible;
        if(forceVisibleModel != null) {
            forceVisibleModel.setValue(forceVisible);
        }
    }

    public boolean isForceVisibleJumps() {
        return forceJumps;
    }

    public void setForceVisibleJumps(boolean forceJumps) {
        this.forceJumps = forceJumps;
    }

    public BooleanModel getForceVisibleModel() {
        return forceVisibleModel;
    }

    public void setForceVisibleModel(BooleanModel forceVisibleModel) {
        if(this.forceVisibleModel != forceVisibleModel) {
            if(this.forceVisibleModel != null) {
                this.forceVisibleModel.removeCallback(forceVisibleModelCallback);
            }
            this.forceVisibleModel = forceVisibleModel;
            if(forceVisibleModel != null) {
                if(forceVisibleModelCallback == null) {
                    forceVisibleModelCallback = new ForceVisibleModelCallback();
                }
                forceVisibleModel.addCallback(forceVisibleModelCallback);
                syncWithForceVisibleModel();
            }
        }
    }

    public boolean isHidden() {
        final int x = animatedWidget.getX();
        final int y = animatedWidget.getY();
        return x == getInnerX() + direction.x*animatedWidget.getWidth() &&
                y == getInnerY() + direction.y*animatedWidget.getHeight();
    }

    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), animatedWidget.getMinWidth() + getBorderHorizontal());
    }

    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), animatedWidget.getMinHeight() + getBorderVertical());
    }

    @Override
    public int getPreferredInnerWidth() {
        return animatedWidget.getPreferredWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        return animatedWidget.getPreferredHeight();
    }
    
    @Override
    protected void applyTheme(ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        setHideDirection(themeInfo.getParameter("hidedirection", Direction.TOP));
        setMoveSpeedIn(themeInfo.getParameter("speed.in", 2));
        setMoveSpeedOut(themeInfo.getParameter("speed.out", 1));
        setAuraSizeX(themeInfo.getParameter("aura.width", 50));
        setAuraSizeY(themeInfo.getParameter("aura.height", 50));
        setForceVisibleJumps(themeInfo.getParameter("forceVisibleJumps", false));
    }

    @Override
    protected void afterAddToGUI(GUI gui) {
        super.afterAddToGUI(gui);
        rect = gui.createMouseSenitiveRectangle();
        setRectSize();
    }

    @Override
    protected void beforeRemoveFromGUI(GUI gui) {
        super.beforeRemoveFromGUI(gui);
        rect = null;
    }

    @Override
    protected void layout() {
        animatedWidget.setSize(getInnerWidth(), getInnerHeight());
        setRectSize();
    }

    @Override
    protected void positionChanged() {
        setRectSize();
    }

    @Override
    protected void paint(GUI gui) {
        if(rect != null) {
            int x = getInnerX();
            int y = getInnerY();
            boolean forceOpen = forceVisible || hasOpenPopups();
            if(forceOpen && forceJumps) {
                animatedWidget.setPosition(x, y);
            } else if(forceOpen || rect.isMouseOver()) {
                // in only needs the direction - not the distance
                animatedWidget.setPosition(
                        calcPosIn(animatedWidget.getX(), x, direction.x),
                        calcPosIn(animatedWidget.getY(), y, direction.y));
            } else {
                // out needs the exact distance
                animatedWidget.setPosition(
                        calcPosOut(animatedWidget.getX(), x, direction.x*animatedWidget.getWidth()),
                        calcPosOut(animatedWidget.getY(), y, direction.y*animatedWidget.getHeight()));
            }
        }
        super.paint(gui);
    }
    
    private void setRectSize() {
        if(rect != null) {
            rect.setXYWH(getX() - auraSizeX, getY() - auraSizeY,
                    getWidth() + 2*auraSizeX, getHeight() + 2*auraSizeY);
        }
    }
    
    private int calcPosIn(int cur, int org, int dir) {
        if(dir < 0) {
            return Math.min(org, cur + moveSpeedIn);
        } else {
            return Math.max(org, cur - moveSpeedIn);
        }
    }
    
    private int calcPosOut(int cur, int org, int dist) {
        if(dist < 0) {
            return Math.max(org+dist, cur - moveSpeedIn);
        } else {
            return Math.min(org+dist, cur + moveSpeedIn);
        }
    }
    
    void syncWithForceVisibleModel() {
        setForceVisible(forceVisibleModel.getValue());
    }
    
    class ForceVisibleModelCallback implements Runnable {
        ForceVisibleModelCallback() {
        }
        public void run() {
            syncWithForceVisibleModel();
        }
    }
}
