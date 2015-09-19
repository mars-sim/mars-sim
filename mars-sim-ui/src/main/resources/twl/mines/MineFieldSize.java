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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Matthias Mann
 */
public final class MineFieldSize {
    
    public final int width;
    public final int height;
    public final int numMines;

    public MineFieldSize(int width, int height, int numMines) {
        checkUShort(width, "width");
        checkUShort(height, "height");
        checkUShort(numMines, "numMines");
        
        if(numMines == 0 || numMines > width*height/2) {
            throw new IllegalArgumentException("numMines");
        }
        
        this.width = width;
        this.height = height;
        this.numMines = numMines;
    }
    
    public static MineFieldSize read(DataInputStream dis) throws IOException {
        int width = dis.readUnsignedShort();
        int height = dis.readUnsignedShort();
        int numMines = dis.readUnsignedShort();
        return new MineFieldSize(width, height, numMines);
    }
    
    public void write(DataOutputStream dos) throws IOException {
        dos.writeShort((short)width);
        dos.writeShort((short)height);
        dos.writeShort((short)numMines);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + this.width;
        hash = 23 * hash + this.height;
        hash = 23 * hash + this.numMines;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MineFieldSize) {
            final MineFieldSize other = (MineFieldSize)obj;
            return (this.width == other.width) &&
                    (this.height == other.height) &&
                    (this.numMines == other.numMines);
        }
        return false;
    }

    private static void checkUShort(int value, String what) {
        if(value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException(what);
        }
    }
}
