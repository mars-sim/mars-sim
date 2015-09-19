/*
 * Copyright (c) 2008-2012, Matthias Mann
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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An immutable single linked list of strings.
 * 
 * @author Matthias Mann
 */
public final class StringList implements Iterable<String> {
    private final String value;
    private final StringList next;

    /**
     * Constructs a string list with a single entry.
     * This is equivalent to {@code new StringList(value, null); }
     * 
     * @param value the string value
     * @throws NullPointerException if value is null
     */
    public StringList(String value) {
        this(value, null);
    }

    /**
     * Constructs a new head of a string list.
     * 
     * @param value the string value
     * @param next the rest/tail of the string list, can be null
     * @throws NullPointerException if value is null
     */
    public StringList(String value, StringList next) {
        if(value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
        this.next = next;
    }

    /**
     * Returns the next element in the string list, or null if this is the last
     * @return the next element in the string list, or null if this is the last
     */
    public StringList getNext() {
        return next;
    }

    /**
     * Returns the string value of this element, never null
     * @return the string value of this element, never null
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof StringList)) {
            return false;
        }
        final StringList that = (StringList)obj;
        return this.value.equals(that.value) &&
                (this.next == that.next || (this.next != null && this.next.equals(that.next)));
    }

    @Override
    public int hashCode() {
        int hash = value.hashCode();
        if(next != null) {
            hash = 67 * hash + next.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        if(next == null) {
            return value;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            for(StringList list=next ; list!=null ; list=list.next) {
                sb.append(", ").append(list.value);
            }
            return sb.toString();
        }
    }
    
    public Iterator<String> iterator() {
        return new I(this);
    }
    
    static class I implements Iterator<String> {
        private StringList list;

        I(StringList list) {
            this.list = list;
        }

        public boolean hasNext() {
            return list != null;
        }

        public String next() {
            if(list == null) {
                throw new NoSuchElementException();
            }
            String value = list.getValue();
            list = list.getNext();
            return value;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
