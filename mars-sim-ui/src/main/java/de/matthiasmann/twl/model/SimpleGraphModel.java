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
package de.matthiasmann.twl.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author Matthias Mann
 */
public class SimpleGraphModel implements GraphModel {

    private final ArrayList<GraphLineModel> lines;
    private boolean scaleLinesIndependant;

    public SimpleGraphModel() {
        lines = new ArrayList<GraphLineModel>();
    }

    public SimpleGraphModel(GraphLineModel ... lines) {
        this(Arrays.asList(lines));
    }

    public SimpleGraphModel(Collection<GraphLineModel> lines) {
        this.lines = new ArrayList<GraphLineModel>(lines);
    }

    public GraphLineModel getLine(int idx) {
        return lines.get(idx);
    }

    public int getNumLines() {
        return lines.size();
    }

    public boolean getScaleLinesIndependant() {
        return scaleLinesIndependant;
    }

    public void setScaleLinesIndependant(boolean scaleLinesIndependant) {
        this.scaleLinesIndependant = scaleLinesIndependant;
    }

    /**
     * Adds a new line at the end of the list
     * @param line the new line
     */
    public void addLine(GraphLineModel line) {
        insertLine(lines.size(), line);
    }

    /**
     * Inserts a new line before the specified index in the list
     * @param idx the index before which the new line will be inserted
     * @param line the new line
     * @throws NullPointerException if line is null
     * @throws IllegalArgumentException if the line is already part of this model
     */
    public void insertLine(int idx, GraphLineModel line) {
        if(line == null) {
            throw new NullPointerException("line");
        }
        if(indexOfLine(line) >= 0) {
            throw new IllegalArgumentException("line already added");
        }
        lines.add(idx, line);
    }

    /**
     * Returns the index of the specified line in this list or -1 if not found.
     * @param line the line to locate
     * @return the index or -1 if not found
     */
    public int indexOfLine(GraphLineModel line) {
        // do a manual search for object identity - not based on equals like lines.indexOf
        for(int i=0,n=lines.size() ; i<n ; i++) {
            if(lines.get(i) == line) {
                return  i;
            }
        }
        return -1;
    }

    /**
     * Removes the line at the specified index
     * @param idx the index of the line to remove
     * @return the line that was removed
     */
    public GraphLineModel removeLine(int idx) {
        return lines.remove(idx);
    }

}
