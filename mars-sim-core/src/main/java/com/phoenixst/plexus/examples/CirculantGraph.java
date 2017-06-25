/*
 *  $Id: CirculantGraph.java,v 1.13 2006/06/20 01:09:29 rconner Exp $
 *
 *  Copyright (C) 1994-2006 by Phoenix Software Technologists,
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

package com.phoenixst.plexus.examples;

import java.io.*;
import java.util.*;

import com.phoenixst.plexus.*;


/**
 *  A circulant <code>Graph</code>.  If the nodes are arranged in a
 *  circle, then each node is adjacent to a fixed number of nodes in
 *  either direction.
 *
 *  @version    $Revision: 1.13 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class CirculantGraph extends AbstractIntegerNodeGraph
{

    private static final long serialVersionUID = 2L;

    /**
     *  If the nodes are arranged in a circle, each node is adjacent
     *  to this many of the nearest nodes in either direction.
     *
     *  @serial
     */
    private final int d;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>CirculantGraph</code>.
     *
     *  @param n the number of nodes in the new
     *  <code>CirculantGraph</code>.
     *
     *  @param d each node is adjacent to this many of the nearest
     *  nodes in either direction.
     */
    public CirculantGraph( int n, int d )
    {
        super( n );
        this.d = d;
        if( n < 3 ) {
            throw new IllegalArgumentException( "A CirculantGraph must have at least 3 nodes: " + n );
        }
        if( d < 1 ) {
            throw new IllegalArgumentException( "The number of nearest nodes must be positive: " + d );
        }
        if( 2 * d >= n ) {
            throw new IllegalArgumentException( "The number of nearest nodes must less than half the graph: " + d );
        }
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        int n = getNodeSize();
        if( n < 3 ) {
            throw new InvalidObjectException( "A CirculantGraph must have at least 3 nodes: " + n );
        }
        if( d < 1 ) {
            throw new InvalidObjectException( "The number of nearest nodes must be positive: " + d );
        }
        if( 2 * d >= n ) {
            throw new InvalidObjectException( "The number of nearest nodes must less than half the graph: " + d );
        }
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    /**
     *  Returns the degree of <code>node</code>, defined as the number
     *  of edges incident on <code>node</code>.
     */
    public int degree( Object node )
    {
        checkNode( node );
        return 2 * d;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    final int getAdjSize()
    {
        return d;
    }


    protected Graph.Edge createEdge( int tailIndex, int headIndex )
    {
        int minIndex = Math.min( tailIndex, headIndex );
        int maxIndex = Math.max( tailIndex, headIndex );
        int diff = maxIndex - minIndex;
        if( diff == 0 ) {
            return null;
        } else if( diff <= d ) {
            return new EdgeImpl( minIndex, maxIndex, true );
        } else if( getNodeSize() - diff <= d ) {
            return new EdgeImpl( maxIndex, minIndex, true );
        } else {
            return null;
        }
    }


    protected Collection createEdgeCollection()
    {
        return new EdgeCollection();
    }


    protected Traverser createTraverser( int nodeIndex )
    {
        return new TraverserImpl( nodeIndex );
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Circulant Graph( " );
        s.append( getNodeSize() );
        s.append( ", " );
        s.append( d );
        s.append( " )" );
        return s.toString();
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private class EdgeCollection extends AbstractIntegerEdgeCollection
    {
        EdgeCollection()
        {
            super();
        }

        public int size()
        {
            return getNodeSize() * getAdjSize();
        }

        public Iterator iterator()
        {
            return new EdgeIterator();
        }
    }


    private class EdgeIterator
        implements Iterator
    {
        private int nodeIndex = 0;
        private int adjIndex = 0;

        EdgeIterator()
        {
            super();
        }

        public boolean hasNext()
        {
            return adjIndex < getAdjSize() || nodeIndex < getNodeSize() - 1;
        }

        public Object next()
        {
            adjIndex++;
            if( adjIndex > getAdjSize() ) {
                nodeIndex++;
                if( nodeIndex >= getNodeSize() ) {
                    throw new NoSuchElementException();
                }
                adjIndex = 1;
            }
            return createEdge( nodeIndex, (nodeIndex + adjIndex) % getNodeSize() );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


    private class TraverserImpl
        implements Traverser
    {
        private final int nodeIndex;
        private int i = -1;

        TraverserImpl( int nodeIndex )
        {
            super();
            this.nodeIndex = nodeIndex;
        }

        public boolean hasNext()
        {
            return i < 2 * getAdjSize();
        }

        public Object next()
        {
            i++;
            int adjSize = getAdjSize();
            if( i == adjSize ) {
                i++;
            }
            if( i > 2 * adjSize ) {
                throw new NoSuchElementException();
            }
            int n = getNodeSize();
            return new Integer( (nodeIndex + n - adjSize + i) % n );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            int adjSize = getAdjSize();
            if( i < 0 || i > 2 * adjSize ) {
                throw new IllegalStateException();
            }
            int n = getNodeSize();
            return createEdge( nodeIndex, (nodeIndex + n - adjSize + i) % n );
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }

}
