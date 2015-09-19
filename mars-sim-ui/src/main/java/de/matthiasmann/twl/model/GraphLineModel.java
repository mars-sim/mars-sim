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

/**
 * A generic line model for a graph.
 * 
 * @author Matthias Mann
 */
public interface GraphLineModel {
    
    /**
     * Returns the name of the visual style.
     * The style will be looked up in the theme data of the graph widget.
     * @return the name of the visual style.
     */
    public String getVisualStyleName();

    /**
     * Returns the number of points on the lines.
     * A line is only drawn if atleast one point is available.
     * @return the number of points on the lines
     */
    public int getNumPoints();

    /**
     * Returns the value of the desired point.
     * @param idx The index of the point. Will be &gt;= 0 and &lt; getNumPoints()
     * @return the value of the desired point.
     * @see #getNumPoints()
     */
    public float getPoint(int idx);

    /**
     * Returns the smallest value which should be used to scale the graph.
     * @return the smallest value which should be used to scale the graph
     */
    public float getMinValue();

    /**
     * Returns the largest value which should be used to scale the graph.
     * @return the largest value which should be used to scale the graph
     */
    public float getMaxValue();

}
