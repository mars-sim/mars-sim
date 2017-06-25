/*
 *  $Id: SingletonTraverser.java,v 1.9 2005/10/03 15:20:46 rconner Exp $
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

import java.util.NoSuchElementException;

import com.phoenixst.plexus.*;


/**
 *  A <code>Traverser</code> over a single <code>Edge</code>.  {@link
 *  #remove} and {@link #removeEdge} delegate to {@link
 *  Graph#removeNode Graph.removeNode( node )} and {@link
 *  Graph#removeEdge Graph.removeEdge( edge )} respectively.
 *
 *  <P>An {@link #SingletonTraverser(Object,Graph.Edge) alternate
 *  constructor} is provided to explicitly make the created
 *  <code>Traverser</code> unmodifiable.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class SingletonTraverser
    implements Traverser
{

    private final Graph graph;
    private final Object endpoint;
    private Graph.Edge edge;

    private boolean hasNext = true;
    private boolean hasEndpoint = true;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new unmodifiable <code>SingletonTraverser</code>.
     *  If this constructor is used, {@link #remove} and {@link
     *  #removeEdge} will throw
     *  <code>UnsupportedOperationExceptions</code>.
     *
     *  @param endpoint the endpoint of the edge over which the
     *  returned <code>Traverser</code> iterates.
     *
     *  @param edge the edge over which the returned
     *  <code>Traverser</code> iterates.
     */
    public SingletonTraverser( Object endpoint, Graph.Edge edge )
    {
        this( null, endpoint, edge );
    }


    /**
     *  Creates a new modifiable <code>SingletonTraverser</code>.
     *
     *  @param graph the graph containing the edge over which the
     *  returned <code>Traverser</code> iterates.
     *
     *  @param endpoint the endpoint of the edge over which the
     *  returned <code>Traverser</code> iterates.
     *
     *  @param edge the edge over which the returned
     *  <code>Traverser</code> iterates.
     */
    public SingletonTraverser( Graph graph, Object endpoint, Graph.Edge edge )
    {
        super();
        this.graph = graph;
        this.endpoint = endpoint;
        this.edge = edge;
    }


    ////////////////////////////////////////
    // Traverser
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
        return endpoint;
    }


    public void remove()
    {
        if( graph == null ) {
            throw new UnsupportedOperationException();
        }
        if( hasNext || !hasEndpoint ) {
            throw new IllegalStateException();
        }
        graph.removeNode( endpoint );
        hasEndpoint = false;
    }


    public Graph.Edge getEdge()
    {
        if( hasNext || !hasEndpoint ) {
            throw new IllegalStateException();
        }
        return edge;
    }


    public void removeEdge()
    {
        if( graph == null ) {
            throw new UnsupportedOperationException();
        }
        if( hasNext || edge == null || !hasEndpoint ) {
            throw new IllegalStateException();
        }
        graph.removeEdge( edge );
        edge = null;
    }

}
