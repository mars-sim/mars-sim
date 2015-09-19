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
package de.matthiasmann.twl.utils;

import java.util.Comparator;

/**
 * Natural sorting of string containing numbers
 * 
 * @author Matthias Mann
 */
public class NaturalSortComparator {

    public static final Comparator<String> stringComparator = new Comparator<String>() {
        public int compare(String n1, String n2) {
            return naturalCompare(n1, n2);
        }
    };
    public static final Comparator<String> stringPathComparator = new Comparator<String>() {
        public int compare(String n1, String n2) {
            return naturalCompareWithPaths(n1, n2);
        }
    };
    
    private static int findDiff(String s1, int idx1, String s2, int idx2) {
        int len = Math.min(s1.length() - idx1, s2.length() - idx2);
        for(int i=0 ; i<len ; i++) {
            char c1 = s1.charAt(idx1 + i);
            char c2 = s2.charAt(idx2 + i);
            if(c1 != c2) {
                if(Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                    return i;
                }
            }
        }
        return len;
    }

    private static int findNumberStart(String s, int i) {
        while(i > 0 && Character.isDigit(s.charAt(i-1))) {
            i--;
        }
        return i;
    }

    private static int findNumberEnd(String s, int i) {
        int len = s.length();
        while(i < len && Character.isDigit(s.charAt(i))) {
            i++;
        }
        return i;
    }

    public static int naturalCompareWithPaths(String n1, String n2) {
        int diffOffset = findDiff(n1, 0, n2, 0);
        int idx0 = n1.indexOf('/', diffOffset);
        int idx1 = n2.indexOf('/', diffOffset);
        if((idx0^idx1) < 0) {
            return idx0;
        }
        return naturalCompare(n1, n2, diffOffset, diffOffset);
    }
    
    public static int naturalCompare(String n1, String n2) {
        return naturalCompare(n1, n2, 0, 0);
    }
    
    private static int naturalCompare(String n1, String n2, int i1, int i2) {
        for(;;) {
            int diffOffset = findDiff(n1, i1, n2, i2);
            i1 += diffOffset;
            i2 += diffOffset;
            if(i1 == n1.length() || i2 == n2.length()) {
                return n1.length() - n2.length();
            }
            char c1 = n1.charAt(i1);
            char c2 = n2.charAt(i2);
            if(Character.isDigit(c1) || Character.isDigit(c2)) {
                int s1 = findNumberStart(n1, i1);
                int s2 = findNumberStart(n2, i2);
                if(Character.isDigit(n1.charAt(s1)) && Character.isDigit(n2.charAt(s2))) {
                    i1 = findNumberEnd(n1, s1 + 1);
                    i2 = findNumberEnd(n2, s2 + 1);
                    try {
                        long value1 = Long.parseLong(n1.substring(s1, i1), 10);
                        long value2 = Long.parseLong(n2.substring(s2, i2), 10);
                        if(value1 != value2) {
                            return Long.signum(value1 - value2);
                        }
                        continue;
                    } catch (NumberFormatException ex) {
                    }
                }
            }
            char cl1 = Character.toLowerCase(c1);
            char cl2 = Character.toLowerCase(c2);
            assert(cl1 != cl2); // findDiff should not stop for this case
            return cl1 - cl2;
        }
    }

    private NaturalSortComparator() {
    }
}
