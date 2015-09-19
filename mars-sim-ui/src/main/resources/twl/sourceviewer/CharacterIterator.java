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
package sourceviewer;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author Matthias Mann
 */
public final class CharacterIterator {

    public static final int EOF = -1;

    private final Reader r;
    
    private char[] buffer;
    private int bufferStart;
    private int pos;
    private int start;
    private int end;
    private int marker;
    private boolean skipCR;
    private boolean atEOF;

    public CharacterIterator(CharSequence cs) {
        int len = cs.length();
        this.r = null;
        this.buffer = new char[len];
        this.end = len;
        
        if(cs instanceof String) {
            ((String)cs).getChars(0, len, buffer, 0);
        } else {
            for(int i=0 ; i<len ; i++) {
                buffer[i] = cs.charAt(i);
            }
        }
    }
    
    public CharacterIterator(Reader r) {
        this.r = r;
        this.buffer = new char[4096];
        this.marker = -1;
    }

    public int length() {
        return pos - start;
    }

    public String getString() {
        return new String(buffer, start, length());
    }

    public void clear() {
        start = pos;
        marker = -1;
    }

    public int peek() {
        if(pos < end || refill()) {
            char ch = buffer[pos];
            if(ch == '\r') {
                if(skipCR) {
                    ++pos;
                    skipCR = false;
                    return peek();
                }
                ch = '\n';
            } else if(ch == '\n') {
                skipCR = true;
            }
            return ch;
        }
        atEOF = true;
        return EOF;
    }

    public void pushback() {
        if(pos > start && !atEOF) {
            pos--;
            marker = -1;
        }
    }
    
    public void advanceToEOL() {
        for(;;) {
            int ch = peek();
            if(ch < 0 || ch == '\n') {
                return;
            }
            pos++;
        }
    }

    public void advanceIdentifier() {
        while(Character.isJavaIdentifierPart(peek())) {
            pos++;
        }
    }

    public int next() {
        int ch = peek();
        if(ch >= 0) {
            pos++;
        }
        return ch;
    }

    public boolean check(String characters) {
        if(pos < end || refill()) {
            return characters.indexOf(buffer[pos]) >= 0;
        }
        return false;
    }

    public void setMarker(boolean pushback) {
        marker = pos;
        if(pushback && pos > start) {
            marker--;
        }
    }

    public boolean isMarkerAtStart() {
        return marker == start;
    }
    
    public void rewindToMarker() {
        if(marker >= start) {
            pos = marker;
            marker = -1;
        }
    }

    public boolean isKeyword(KeywordList list) {
        return marker >= 0 && list.isKeyword(buffer, marker, pos - marker);
    }

    public int getCurrentPosition() {
        return bufferStart + pos;
    }
    
    private void compact() {
        bufferStart += start;
        pos -= start;
        marker -= start;
        end -= start;
        if(pos > buffer.length*3/2) {
            char[] newBuffer = new char[buffer.length * 2];
            System.arraycopy(buffer, start, newBuffer, 0, end);
            buffer = newBuffer;
        } else if(end > 0) {
            System.arraycopy(buffer, start, buffer, 0, end);
        }
        start = 0;
    }

    private boolean refill() {
        if(r == null) {
            return false;
        }
        
        compact();

        try {
            int read = r.read(buffer, end, buffer.length - end);
            if(read <= 0) {
                return false;
            }
            end += read;
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
