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

/**
 * Check if a given text is a keyword
 *
 * @author Matthias Mann
 */
public class KeywordList {

    private final String[] keywords;
    private final int maxLength;

    /**
     * Constructs a keyword list from an sorted list of keywords (sorted on char codes)
     * @param keywords the list of keywords
     */
    public KeywordList(String ... keywords) {
        int len = 0;
        for(String kw : keywords) {
            len = Math.max(len, kw.length());
        }
        
        this.keywords = keywords;
        this.maxLength = len;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isKeyword(char[] buf, int start, int len) {
        if(len > maxLength) {
            return false;
        }
        int kwidx = 0;
        for(int chpos=0 ; chpos<len ; chpos++) {
            char c = buf[start + chpos];
            for(;;) {
                String kw = keywords[kwidx];
                if(chpos < kw.length()) {
                    char kwc = kw.charAt(chpos);
                    if(kwc == c) {
                        break;
                    }
                    if(kwc > c) {
                        return false;
                    }
                }
                if(++kwidx == keywords.length) {
                    return false;
                }
            }
        }
        return keywords[kwidx].length() == len;
    }
}
