/*
 *  $Id: SingletonEdgeIterator.java,v 1.7 2005/10/03 15:20:46 rconner Exp $
 *
 *  Copyright (C) 1994-2005 by Phoenix Software Technologists,
 *  Inc. and others.  All rights reserved.
 *
 *  THIS PROGRAM AND DOCUMENTATION IS PROVIDED UNDER THE TERMS OF THE
 *  COMMON PUBLIC LICENSE ("AGREEMENT") WHICH ACCOMPANIES IT.  ANY
 *  USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THE AGREEMENT.
 *
 *  The license text can also be found at
 *    http://opensource.org/licenses/cpl.php
 */

package com.phoenixst.plexus.util;

import java.util.*;

import com.phoenixst.plexus.Graph;


/**
 *  An <code>Iterator</code> over a single <code>Edge</code>.  {@link
 *  #remove} delegates to {@link Graph#removeEdge Graph.removeEdge(
 *  edge )}.
 *
 *  <P>An {@link #SingletonEdgeIterator(Graph.Edge) alternate
 *  constructor} is provided to explicitly make the created
 *  <code>Iterator</code> unmodifiable.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class SingletonEdgeIterator
    implements Iterator
{

    private final Graph graph;
    private Graph.Edge edge;

    private boolean hasNext = true;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new unmodifiable <code>SingletonEdgeIterator</code>.
     *  If this constructor is used, {@link #remove} will throw an
     *  <code>UnsupportedOperationException</code>.
     *
     *  @param edge the edge over which the returned
     *  <code>Iterator</code> iterates.
     */
    public SingletonEdgeIterator( Graph.Edge edge )
    {
        this( null, edge );
    }


    /**
     *  Creates a new modifiable <code>SingletonEdgeIterator</code>.
     *
     *  @param graph the graph containing the edge over which the
     *  returned <code>Iterator</code> iterates.
     *
     *  @param edge the edge over which the returned
     *  <code>Iterator</code> iterates.
     */
    public SingletonEdgeIterator( Graph graph, Graph.Edge edge )
    {
        super();
        this.graph = graph;
        this.edge = edge;
    }


    ////////////////////////////////////////
    // Iterator
    ////////////////////////////////////////


    public boolean hasNext()
    {
        return hasNext;
    }


    public Object next()
    {
        if( !hasNext ) {
            throw new NoSuchElementException();
        }
        hasNext = false;
        return edge;
    }


    public void remove()
    {
        if( graph == null ) {
            throw new UnsupportedOperationException();
        }
        if( hasNext || edge == null ) {
            throw new IllegalStateException();
        }
        graph.removeEdge( edge );
        edge = null;
    }

}
