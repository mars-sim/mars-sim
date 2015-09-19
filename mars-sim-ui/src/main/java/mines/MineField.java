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

import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Matthias Mann
 */
public class MineField {
    
    public static final int NUMBER_MASK = 15;
    public static final int MINE = 1 << 4;
    public static final int FLAG = 1 << 5;
    public static final int OPEN = 1 << 6;
    public static final int SIMOPEN = 1 << 7;
    
    private final Random random;
    private final MineFieldSize size;
    private final short[][] field;
    private boolean reset;
    private boolean gameOver;
    private boolean victory;
    private long startTime;
    private int gameTime;
    private int numFlags;
    
    public MineField(MineFieldSize size) {
        this.size = size;
        this.field = new short[size.height][size.width];
        this.reset = true;
        this.random = new Random();
    }

    public MineFieldSize getSize() {
        return size;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isVictory() {
        return victory;
    }
    
    public boolean isGameLost() {
        return gameOver && !victory;
    }
    
    public int getGameTime() {
        if(reset) {
            return 0;
        }
        if(gameOver) {
            return gameTime;
        }
        return (int)((System.currentTimeMillis() - startTime) / 1000);
    }

    public int getNumFlags() {
        return numFlags;
    }
    
    public void reset() {
        for(short[] row : field) {
            Arrays.fill(row, (byte)0);
        }
        reset = true;
        gameOver = false;
        victory = false;
        gameTime = 0;
        numFlags = 0;
    }
    
    public void generate(int startX, int startY) {
        int numPositions = size.width * size.height;
        int[] positions = new int[numPositions];
        for(int i=0 ; i<numPositions ; i++) {
            positions[i] = i;
        }
        
        for(int i=0 ; i<size.numMines ;) {
            int idx = random.nextInt(numPositions);
            int pos = positions[idx];
            positions[idx] = positions[--numPositions];
            
            int x = pos % size.width;
            int y = pos / size.width;
            
            if(Math.abs(x-startX) <= 1 && Math.abs(y-startY) <= 1) {
                continue;
            }
            
            field[y][x] |= MINE;
            i++;
        }
        
        for(int y=0 ; y<size.height ; y++) {
            for(int x=0 ; x<size.width ; x++) {
                if((field[y][x] & MINE) == 0) {
                    field[y][x] |= (byte)countNeighbors(x, y, MINE);
                }
            }
        }
        
        startTime = System.currentTimeMillis();
    }
    
    private int countNeighbors(int x, int y, int what) {
        int count = 0;
        for(int i=-1 ; i<=1 ; i++) {
            for(int j=-1 ; j<=1 ; j++) {
                if((getSafe(x+i, y+j) & what) == what) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private void setGameOver() {
        gameTime = getGameTime();
        gameOver = true;
    }
    
    private void checkVictory() {
        for(short[] row : field) {
            for(int value : row) {
                int masked = value & (FLAG|MINE|OPEN);
                if(masked != (FLAG|MINE) && masked != OPEN) {
                    return;
                }
            }
        }
        setGameOver();
        victory = true;
    }
    
    public boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < size.width && y < size.height;
    }
    
    public int get(int x, int y) {
        return field[y][x];
    }
    
    public int getSafe(int x, int y) {
        if(isValid(x, y)) {
            return field[y][x];
        }
        return 0;
    }
    
    public int getNumber(int x, int y) {
        return getSafe(x, y) & NUMBER_MASK;
    }
    
    public boolean isMine(int x, int y) {
        return (getSafe(x, y) & MINE) == MINE;
    }
    
    public boolean hasFlag(int x, int y) {
        return (getSafe(x, y) & FLAG) == FLAG;
    }
    
    public boolean isOpen(int x, int y) {
        return (getSafe(x, y) & OPEN) == OPEN;
    }
    
    public boolean isSimOpen(int x, int y) {
        return (getSafe(x, y) & SIMOPEN) == SIMOPEN;
    }
    
    public boolean checkOpenForNumberClick(int x, int y) {
        if(isValid(x, y)) {
            return (field[y][x] & (OPEN | FLAG)) == 0;
        }
        return false;
    }
    
    public void toggleFlag(int x, int y) {
        if(gameOver) {
            return;
        }
        if(hasFlag(x, y)) {
            field[y][x] &= ~FLAG;
            numFlags--;
        } else if(!isOpen(x, y)) {
            field[y][x] |= FLAG;
            numFlags++;
            checkVictory();
        }
    }
    
    public void setSimOpen(int x, int y) {
        if(gameOver) {
            return;
        }
        
        if(reset) {
            reset = false;
            generate(x, y);
        }
        
        int value = get(x, y);
        if((value & OPEN) != 0) {
            if((value & (FLAG|MINE)) != 0) {
                return;
            }
            if((value & NUMBER_MASK) > 0) {
                for(int i=-1 ; i<=1 ; i++) {
                    for(int j=-1 ; j<=1 ; j++) {
                        if(checkOpenForNumberClick(x+i, y+j)) {
                            field[y+j][x+i] |= SIMOPEN;
                        }
                    }
                }
            }
        } else {
            field[y][x] |= SIMOPEN;
        }
    }
    
    public void clearSimOpen(int x, int y) {
        for(int i=-1 ; i<=1 ; i++) {
            for(int j=-1 ; j<=1 ; j++) {
                if(isSimOpen(x+i, y+j)) {
                   field[y+j][x+i] &= ~SIMOPEN;
                }
            }
        }
    }
    
    public void open(int x, int y) {
        if(gameOver) {
            return;
        }
        
        if(reset) {
            reset = false;
            generate(x, y);
        }
        
        if(hasFlag(x, y)) {
            return;
        }
        
        if(isMine(x, y)) {
            field[y][x] |= OPEN;
            setGameOver();
            return;
        }
        
        Stack stack = new Stack();
        
        if(!isOpen(x, y)) {
            stack.push(x, y);
        } else {
            int number = getNumber(x, y);
            if(number == 0 || number != countNeighbors(x, y, FLAG)) {
                return;
            }
            
            for(int i=-1 ; i<=1 ; i++) {
                for(int j=-1 ; j<=1 ; j++) {
                    if(checkOpenForNumberClick(x+i, y+j)) {
                        stack.push(x+i, y+j);
                    }
                }
            }
        }
        
        while(!stack.isEmpty()) {
            x = stack.peekX();
            y = stack.peekY();
            stack.pop();
            
            field[y][x] |= OPEN;
            
            if(isMine(x, y)) {
                setGameOver();
            } else if(getNumber(x, y) == 0) {
                for(int i=-1 ; i<=1 ; i++) {
                    for(int j=-1 ; j<=1 ; j++) {
                        int xo = x + i;
                        int yo = y + j;

                        if(isValid(xo, yo)) {
                            if((get(xo, yo) & (FLAG|OPEN)) == 0) {
                                stack.push(xo, yo);
                            }
                        }
                    }
                }
            }
        }
        
        if(!gameOver) {
            checkVictory();
        }
    }
    
    static class Stack {
        private int[] arr;
        private int top;

        public Stack() {
            arr = new int[16];
        }
        
        public void push(int x, int y) {
            if(top == arr.length) {
                int[] tmp = new int[top*2];
                System.arraycopy(arr, 0, tmp, 0, top);
                arr = tmp;
            }
            arr[top++] = x | (y << 16);
        }
        
        public boolean isEmpty() {
            return top == 0;
        }
        
        public int peekX() {
            return arr[top-1] & 0xFFFF;
        }
        
        public int peekY() {
            return arr[top-1] >> 16;
        }
        
        public void pop() {
            top--;
        }
    }
}
