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
package de.matthiasmann.twl.model;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.AnimationState.StateKey;
import de.matthiasmann.twl.renderer.AttributedString;
import java.util.ArrayList;
import java.util.BitSet;

/**
 *
 * @author Matthias Mann
 */
public class StringAttributes implements AttributedString {

    private final CharSequence seq;
    private final AnimationState baseAnimState;
    private final ArrayList<Marker> markers;

    private int position;
    private int markerIdx;
    
    private StringAttributes(AnimationState baseAnimState, CharSequence seq) {
        if(seq == null) {
            throw new NullPointerException("seq");
        }
        
        this.seq = seq;
        this.baseAnimState = baseAnimState;
        this.markers = new ArrayList<Marker>();
    }

    public StringAttributes(String text, AnimationState baseAnimState) {
        this(baseAnimState, text);
    }

    public StringAttributes(String text) {
        this(text, null);
    }

    public StringAttributes(ObservableCharSequence cs, AnimationState baseAnimState) {
        this(baseAnimState, cs);

        cs.addCallback(new ObservableCharSequence.Callback() {
            public void charactersChanged(int start, int oldCount, int newCount) {
                if(start < 0) {
                    throw new IllegalArgumentException("start");
                }
                if(oldCount > 0) {
                    delete(start, oldCount);
                }
                if(newCount > 0) {
                    insert(start, newCount);
                }
            }
        });
    }

    public StringAttributes(ObservableCharSequence cs) {
        this(cs, null);
    }

    public char charAt(int index) {
        return seq.charAt(index);
    }

    public int length() {
        return seq.length();
    }

    public CharSequence subSequence(int start, int end) {
        return seq.subSequence(start, end);
    }

    @Override
    public String toString() {
        return seq.toString();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int pos) {
        if(pos < 0 || pos > seq.length()) {
            throw new IllegalArgumentException("pos");
        }
        this.position = pos;
        
        int idx = find(pos);
        if(idx >= 0) {
            this.markerIdx = idx;
        } else if(pos > lastMarkerPos()) {
            this.markerIdx = markers.size();
        } else {
            // select the marker to the left
            this.markerIdx = (idx & IDX_MASK) - 1;
        }
    }

    public int advance() {
        if(markerIdx+1 < markers.size()) {
            markerIdx++;
            position = markers.get(markerIdx).position;
        } else {
            position = seq.length();
        }
        return position;
    }

    public boolean getAnimationState(StateKey state) {
        if(markerIdx >= 0 && markerIdx < markers.size()) {
            Marker marker = markers.get(markerIdx);
            int bitIdx = state.getID() << 1;
            if(marker.get(bitIdx)) {
                return marker.get(bitIdx+1);
            }
        }
        if(baseAnimState != null) {
            return baseAnimState.getAnimationState(state);
        }
        return false;
    }

    public int getAnimationTime(StateKey state) {
        if(baseAnimState != null) {
            return baseAnimState.getAnimationTime(state);
        }
        return 0;
    }

    public boolean getShouldAnimateState(StateKey state) {
        if(baseAnimState != null) {
            return baseAnimState.getShouldAnimateState(state);
        }
        return false;
    }

    public void setAnimationState(StateKey key, int from, int end, boolean active) {
        if(key == null) {
            throw new NullPointerException("key");
        }
        if(from > end) {
            throw new IllegalArgumentException("negative range");
        }
        if(from < 0 || end > seq.length()) {
            throw new IllegalArgumentException("range outside of sequence");
        }
        if(from == end) {
            return;
        }
        int fromIdx = markerIndexAt(from);
        int endIdx = markerIndexAt(end);
        int bitIdx = key.getID() << 1;
        for(int i=fromIdx ; i<endIdx ; i++) {
            Marker m = markers.get(i);
            m.set(bitIdx);
            m.set(bitIdx+1, active);
        }
    }

    public void removeAnimationState(StateKey key, int from, int end) {
        if(key == null) {
            throw new NullPointerException("key");
        }
        if(from > end) {
            throw new IllegalArgumentException("negative range");
        }
        if(from < 0 || end > seq.length()) {
            throw new IllegalArgumentException("range outside of sequence");
        }
        if(from == end) {
            return;
        }
        int fromIdx = markerIndexAt(from);
        int endIdx = markerIndexAt(end);
        removeRange(fromIdx, endIdx, key);
    }

    public void removeAnimationState(StateKey key) {
        if(key == null) {
            throw new NullPointerException("key");
        }
        removeRange(0, markers.size(), key);
    }

    private void removeRange(int start, int end, StateKey key) {
        int bitIdx = key.getID() << 1;
        for(int i=start ; i<end ; i++) {
            markers.get(i).clear(bitIdx);
            markers.get(i).clear(bitIdx+1); // also clear the active bit for optimize
        }
    }

    public void clearAnimationStates() {
        markers.clear();
    }
    
    /**
     * Optimizes the internal representation.
     * This need O(n) time.
     */
    public void optimize() {
        if(markers.size() > 1) {
            Marker prev = markers.get(0);
            for(int i=1 ; i<markers.size() ;) {
                Marker cur = markers.get(i);
                if(prev.equals(cur)) {
                    markers.remove(i);
                } else {
                    prev = cur;
                    i++;
                }
            }
        }
    }

    void insert(int pos, int count) {
        int idx = find(pos) & IDX_MASK;
        for(int end=markers.size() ; idx<end ; idx++) {
            markers.get(idx).position += count;
        }
    }

    void delete(int pos, int count) {
        int startIdx = find(pos) & IDX_MASK;
        int removeIdx = startIdx;
        int end = markers.size();
        for(int idx=startIdx ; idx<end ; idx++) {
            Marker m = markers.get(idx);
            int newPos = m.position - count;
            if(newPos <= pos) {
                newPos = pos;
                removeIdx = idx;
            }
            m.position = newPos;
        }
        for(int idx=removeIdx ; idx>startIdx ;) {
            markers.remove(--idx);
        }
    }

    private int lastMarkerPos() {
        int numMarkers = markers.size();
        if(numMarkers > 0) {
            return markers.get(numMarkers-1).position;
        } else {
            return 0;
        }
    }
    
    private int markerIndexAt(int pos) {
        int idx = find(pos);
        if(idx < 0) {
            idx &= IDX_MASK;
            insertMarker(idx, pos);
        }
        return idx;
    }

    private void insertMarker(int idx, int pos) {
        Marker newMarker = new Marker();
        if(idx > 0) {
            Marker leftMarker = markers.get(idx - 1);
            assert leftMarker.position < pos;
            newMarker.or(leftMarker);
        }
        newMarker.position = pos;
        markers.add(idx, newMarker);
    }

    private static final int NOT_FOUND = Integer.MIN_VALUE;
    private static final int IDX_MASK  = Integer.MAX_VALUE;

    private int find(int pos) {
        int lo = 0;
        int hi = markers.size();
        while(lo < hi) {
            int mid = (lo + hi) >>> 1;
            int markerPos = markers.get(mid).position;
            if(pos < markerPos) {
                hi = mid;
            } else if(pos > markerPos) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }
        return lo | NOT_FOUND;
    }

    static class Marker extends BitSet {
        int position;
    }
}
