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
package de.matthiasmann.twl;

/**
 *
 * @author Matthias Mann
 */
public class SplitPane extends Widget {

    public enum Direction {
        HORIZONTAL("splitterHorizontal") {
            int get(int x, int y) {
                return x;
            }
            int getMinSize(Widget w) {
                return w.getMinWidth();
            }
            int getPrefSize(Widget w) {
                return w.getPreferredWidth();
            }
        },
        VERTICAL("splitterVertical") {
            int get(int x, int y) {
                return y;
            }
            int getMinSize(Widget w) {
                return w.getMinHeight();
            }
            int getPrefSize(Widget w) {
                return w.getPreferredHeight();
            }
        };

        final String splitterTheme;
        Direction(String splitterTheme) {
            this.splitterTheme = splitterTheme;
        }
        abstract int get(int x, int y);
        abstract int getMinSize(Widget w);
        abstract int getPrefSize(Widget w);
    }

    /**
     * Magic constant for {@link #setSplitPosition(int) } to keep both
     * widgets on equal size.
     */
    public static final int CENTER = -1;
    
    /**
     * Magic constant for {@link #setSplitPosition(int) } to keep the first
     * (or second widget when {@link #getReverseSplitPosition() } is active)
     * in it's minimum size.
     */
    public static final int MIN_SIZE = -2;
    
    /**
     * Magic constant for {@link #setSplitPosition(int) } to keep the first
     * (or second widget when {@link #getReverseSplitPosition() } is active)
     * in it's preferred size.
     */
    public static final int PREFERRED_SIZE = -3;
    
    private final DraggableButton splitter;
    private Direction direction;
    private int splitPosition = CENTER;
    private boolean reverseSplitPosition;
    private boolean respectMinSizes;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public SplitPane() {
        splitter = new DraggableButton();
        splitter.setCanAcceptKeyboardFocus(false);
        splitter.setListener(new DraggableButton.DragListener() {
            int initialPos;
            public void dragStarted() {
                initialPos = getEffectiveSplitPosition();
            }
            public void dragged(int deltaX, int deltaY) {
                SplitPane.this.dragged(initialPos, deltaX, deltaY);
            }
            public void dragStopped() {
            }
        });
        setDirection(Direction.HORIZONTAL);
        add(splitter);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if(direction == null) {
            throw new NullPointerException("direction");
        }
        this.direction = direction;
        splitter.setTheme(direction.splitterTheme);
    }

    public int getMaxSplitPosition() {
        return Math.max(0, direction.get(
                getInnerWidth() - splitter.getPreferredWidth(),
                getInnerHeight() - splitter.getPreferredHeight()));
    }
    
    public int getSplitPosition() {
        return splitPosition;
    }

    public void setSplitPosition(int pos) {
        if(pos < PREFERRED_SIZE) {
            throw new IllegalArgumentException("pos");
        }
        splitPosition = pos;
        invalidateLayoutLocally();
    }

    public boolean getReverseSplitPosition() {
        return reverseSplitPosition;
    }

    public void setReverseSplitPosition(boolean reverseSplitPosition) {
        if(this.reverseSplitPosition != reverseSplitPosition) {
            this.reverseSplitPosition = reverseSplitPosition;
            invalidateLayoutLocally();
        }
    }

    public boolean isRespectMinSizes() {
        return respectMinSizes;
    }

    public void setRespectMinSizes(boolean respectMinSizes) {
        if(this.respectMinSizes != respectMinSizes) {
            this.respectMinSizes = respectMinSizes;
            invalidateLayoutLocally();
        }
    }

    void dragged(int initialPos, int deltaX, int deltaY) {
        int delta = direction.get(deltaX, deltaY);
        if(reverseSplitPosition) {
            delta = -delta;
        }
        setSplitPosition(clamp(initialPos + delta));
    }

    @Override
    protected void childRemoved(Widget exChild) {
        super.childRemoved(exChild);
        if(exChild == splitter) {
            // add it back :)
            add(splitter);
        }
    }

    @Override
    protected void childAdded(Widget child) {
        super.childAdded(child);
        int numChildren = getNumChildren();
        if(numChildren > 0 && getChild(numChildren-1) != splitter) {
            // move splitter to the end (so that it renders on top)
            moveChild(getChildIndex(splitter), numChildren-1);
        }
    }


    @Override
    public int getMinWidth() {
        int min;
        if(direction == Direction.HORIZONTAL) {
            min = BoxLayout.computeMinWidthHorizontal(this, 0);
        } else {
            min = BoxLayout.computeMinWidthVertical(this);
        }
        return Math.max(super.getMinWidth(), min);
    }

    @Override
    public int getMinHeight() {
        int min;
        if(direction == Direction.HORIZONTAL) {
            min = BoxLayout.computeMinHeightHorizontal(this);
        } else {
            min = BoxLayout.computeMinHeightVertical(this, 0);
        }
        return Math.max(super.getMinHeight(), min);
    }

    @Override
    public int getPreferredInnerWidth() {
        if(direction == Direction.HORIZONTAL) {
            return BoxLayout.computePreferredWidthHorizontal(this, 0);
        } else {
            return BoxLayout.computePreferredWidthVertical(this);
        }
    }

    @Override
    public int getPreferredInnerHeight() {
        if(direction == Direction.HORIZONTAL) {
            return BoxLayout.computePreferredHeightHorizontal(this);
        } else {
            return BoxLayout.computePreferredHeightVertical(this, 0);
        }
    }

    @Override
    protected void layout() {
        Widget a = null;
        Widget b = null;
        for(int i=0 ; i<getNumChildren() ; ++i) {
            Widget w = getChild(i);
            if(w != splitter) {
                if(a == null) {
                    a = w;
                } else {
                    b = w;
                    break;
                }
            }
        }

        int innerX = getInnerX();
        int innerY = getInnerY();
        int splitPos = getEffectiveSplitPosition();

        if(reverseSplitPosition) {
            splitPos = getMaxSplitPosition() - splitPos;
        }

        switch(direction) {
        case HORIZONTAL:
            int innerHeight = getInnerHeight();
            splitter.setPosition(innerX+splitPos, innerY);
            splitter.setSize(splitter.getPreferredWidth(), innerHeight);
            if(a != null) {
                a.setPosition(innerX, innerY);
                a.setSize(splitPos, innerHeight);
            }
            if(b != null) {
                b.setPosition(splitter.getRight(), innerY);
                b.setSize(Math.max(0, getInnerRight()-splitter.getRight()), innerHeight);
            }
            break;

        case VERTICAL:
            int innerWidth = getInnerWidth();
            splitter.setPosition(innerX, innerY+splitPos);
            splitter.setSize(innerWidth, splitter.getPreferredHeight());
            if(a != null) {
                a.setPosition(innerX, innerY);
                a.setSize(innerWidth, splitPos);
            }
            if(b != null) {
                b.setPosition(innerX, splitter.getBottom());
                b.setSize(innerWidth, Math.max(0, getInnerBottom()-splitter.getBottom()));
            }
            break;
        }
    }

    int getEffectiveSplitPosition() {
        final int maxSplitPosition = getMaxSplitPosition();
        
        int pos = splitPosition;
        switch(pos) {
            case CENTER:
                pos = maxSplitPosition/2;
                break;
            case MIN_SIZE: {
                Widget w = getPrimaryWidget();
                if(w != null) {
                    pos = direction.getMinSize(w);
                } else {
                    pos = maxSplitPosition/2;
                }
                break;
            }
            case PREFERRED_SIZE: {
                Widget w = getPrimaryWidget();
                if(w != null) {
                    pos = direction.getPrefSize(w);
                } else {
                    pos = maxSplitPosition/2;
                }
                break;
            }
        }
        
        int minValue = 0;
        int maxValue = maxSplitPosition;

        if(respectMinSizes) {
            Widget a = null;
            Widget b = null;
            for(int i=0 ; i<getNumChildren() ; ++i) {
                Widget w = getChild(i);
                if(w != splitter) {
                    if(a == null) {
                        a = w;
                    } else {
                        b = w;
                        break;
                    }
                }
            }
            
            int aMinSize = (a != null) ? direction.getMinSize(a) : 0;
            int bMinSize = (b != null) ? direction.getMinSize(b) : 0;
            
            if(reverseSplitPosition) {
                minValue = bMinSize;
                maxValue = Math.max(0, maxSplitPosition - aMinSize);
            } else {
                minValue = aMinSize;
                maxValue = Math.max(0, maxSplitPosition - bMinSize);
            }
        }
        
        return Math.max(minValue, Math.min(maxValue, pos));
    }
    
    private Widget getPrimaryWidget() {
        int idx = reverseSplitPosition ? 1 : 0;
        if(getNumChildren() > idx) {
            return getChild(idx);
        }
        return null;
    }

    private int clamp(int pos) {
        return Math.max(0, Math.min(getMaxSplitPosition(), pos));
    }
}
