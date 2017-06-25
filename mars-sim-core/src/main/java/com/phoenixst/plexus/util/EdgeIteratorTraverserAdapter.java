/*
 *  $Id: EdgeIteratorTraverserAdapter.java,v 1.6 2005/10/03 15:20:46 rconner Exp $
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

import java.util.Iterator;

import com.phoenixst.plexus.*;


/**
 *  This class wraps an <code>Iterator</code> over <code>Edge</code>
 *  objects, presenting a {@link Traverser}.  It is assumed that each
 *  <code>Edge</code> is incident to the base node.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class EdgeIteratorTraverserAdapter
    implements Traverser
{

    private final Graph graph;
    private final Object baseNode;
    private final Iterator edgeIter;

    private Object currentNode = null;
    private Graph.Edge currentEdge = null;
    private boolean isCurrentNodeValid = false;


    /**
     *  Creates a new unmodifiable
     *  <code>EdgeIteratorTraverserAdapter</code>.  If this
     *  constructor is used, {@link #remove} and {@link #removeEdge}
     *  will throw <code>UnsupportedOperationExceptions</code>.
     */
    public EdgeIteratorTraverserAdapter( Object baseNode,
                                         Iterator edgeIter )
    {
        this( null, baseNode, edgeIter );
    }


    /**
     *  Creates a new <code>EdgeIteratorTraverserAdapter</code>.
     */
    public EdgeIteratorTraverserAdapter( Graph graph,
                                         Object baseNode,
                                         Iterator edgeIter )
    {
        super();
        this.graph = graph;
        this.baseNode = baseNode;
        this.edgeIter = edgeIter;
    }


    public boolean hasNext()
    {
        return edgeIter.hasNext();
    }


    public Object next()
    {
        currentEdge = (Graph.Edge) edgeIter.next();
        currentNode = currentEdge.getOtherEndpoint( baseNode );
        isCurrentNodeValid = true;
        return currentNode;
    }


    public void remove()
    {
        if( graph == null ) {
            throw new UnsupportedOperationException();
        }
        if( !isCurrentNodeValid ) {
            throw new IllegalStateException();
        }
        graph.removeNode( currentNode );
        currentEdge = null;
        isCurrentNodeValid = false;
    }


    public Graph.Edge getEdge()
    {
        if( currentEdge == null ) {
            throw new IllegalStateException();
        }
        return currentEdge;
    }


    public void removeEdge()
    {
        if( graph == null ) {
            throw new UnsupportedOperationException();
        }
        if( currentEdge == null ) {
            throw new IllegalStateException();
        }
        graph.removeEdge( currentEdge );
        currentEdge = null;
    }

}
