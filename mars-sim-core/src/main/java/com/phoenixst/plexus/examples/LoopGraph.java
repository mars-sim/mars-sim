/*
 *  $Id: LoopGraph.java,v 1.13 2006/06/20 01:09:29 rconner Exp $
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
 *  A loop <code>Graph</code>.
 *
 *  @version    $Revision: 1.13 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class LoopGraph extends AbstractIntegerNodeGraph
{

    private static final long serialVersionUID = 2L;

    /**
     *  @serial
     */
    private final int offset;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>LoopGraph</code>.
     */
    public LoopGraph( int n, int offset )
    {
        super( n );
        this.offset = offset;
        if( n < 3 ) {
            throw new IllegalArgumentException( "A LoopGraph must have at least 3 nodes: " + n );
        }
        if( offset < 1 ) {
            throw new IllegalArgumentException( "Offset must be positive: " + offset );
        }
        if( 2 * offset > n ) {
            throw new IllegalArgumentException( "Offset can be at most half the graph: " + offset );
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
            throw new InvalidObjectException( "A LoopGraph must have at least 3 nodes: " + n );
        }
        if( offset < 1 ) {
            throw new InvalidObjectException( "Offset must be positive: " + offset );
        }
        if( 2 * offset > n ) {
            throw new InvalidObjectException( "Offset can be at most half the graph: " + offset );
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
        return (2 * offset == getNodeSize())
            ? 1
            : 2;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    final int getOffset()
    {
        return offset;
    }


    protected Graph.Edge createEdge( int tailIndex, int headIndex )
    {
        int minIndex = Math.min( tailIndex, headIndex );
        int maxIndex = Math.max( tailIndex, headIndex );
        int diff = maxIndex - minIndex;
        if( diff == offset ) {
            return new EdgeImpl( minIndex, maxIndex, true );
        } else if( getNodeSize() - diff == offset ) {
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
        s.append( "Loop Graph( " );
        s.append( getNodeSize() );
        s.append( ", " );
        s.append( offset );
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
            int n = getNodeSize();
            return (2 * getOffset() == n)
                ? n / 2
                : n;
        }

        public Iterator iterator()
        {
            return new EdgeIterator();
        }
    }


    private class EdgeIterator
        implements Iterator
    {
        private int i;

        EdgeIterator()
        {
            super();
            i = (2 * getOffset() == getNodeSize())
                ? getOffset() - 1
                : -1;
        }

        public boolean hasNext()
        {
            return i < getNodeSize() - 1;
        }

        public Object next()
        {
            i++;
            int n = getNodeSize();
            if( i >= n ) {
                throw new NoSuchElementException();
            }
            return createEdge( i, (i + getOffset()) % n );
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
        private final int node1;
        private final int node2;
        private int i;

        TraverserImpl( int nodeIndex )
        {
            super();
            this.nodeIndex = nodeIndex;
            int n = getNodeSize();
            int off = getOffset();
            node1 = (nodeIndex + n - off) % n;
            node2 = (nodeIndex + off) % n;
            i = (2 * off == n)
                ? 1
                : 0;
        }

        public boolean hasNext()
        {
            return i < 2;
        }

        public Object next()
        {
            i++;
            if( i == 1 ) {
                return new Integer( node1 );
            } else if( i == 2 ) {
                return new Integer( node2 );
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( i == 1 ) {
                return createEdge( nodeIndex, node1 );
            } else if( i == 2 ) {
                return createEdge( nodeIndex, node2 );
            } else {
                throw new IllegalStateException();
            }
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }

}
