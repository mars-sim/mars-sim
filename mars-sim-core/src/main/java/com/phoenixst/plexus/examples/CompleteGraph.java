/*
 *  $Id: CompleteGraph.java,v 1.29 2006/06/20 01:09:29 rconner Exp $
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

import org.apache.commons.collections.iterators.EmptyIterator;

import com.phoenixst.plexus.*;


/**
 *  A <code>Graph</code> containing a set of <code>Integer</code>
 *  nodes where there is an edge between every pair of nodes.
 *
 *  @version    $Revision: 1.29 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class CompleteGraph extends AbstractIntegerNodeGraph
{

    private static final long serialVersionUID = 2L;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>CompleteGraph</code>.
     */
    public CompleteGraph( int n )
    {
        super( n );
        if( n < 1 ) {
            throw new IllegalArgumentException( "A Complete Graph must have at least 1 node: " + n );
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
        if( getNodeSize() < 1 ) {
            throw new InvalidObjectException( "A Complete Graph must have at least 1 node: " + getNodeSize() );
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
        return getNodeSize() - 1;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    protected Graph.Edge createEdge( int tailIndex, int headIndex )
    {
        int minIndex = Math.min( tailIndex, headIndex );
        int maxIndex = Math.max( tailIndex, headIndex );
        if( minIndex == maxIndex ) {
            return null;
        }
        return new EdgeImpl( minIndex, maxIndex, true );
    }


    protected Collection createEdgeCollection()
    {
        return new EdgeCollection();
    }


    protected Traverser createTraverser( int nodeIndex )
    {
        return getNodeSize() < 2 ? GraphUtils.EMPTY_TRAVERSER : new TraverserImpl( nodeIndex );
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Complete( " );
        s.append( getNodeSize() );
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
            return (n * (n - 1)) / 2;
        }

        public Iterator iterator()
        {
            return getNodeSize() < 2 ? EmptyIterator.INSTANCE : new EdgeIterator();
        }
    }


    private class EdgeIterator
        implements Iterator
    {
        /*
         * Basically:
         *   for( int headIndex = 1; headIndex < n; headIndex++ )
         *      for( int tailIndex = 0; tailIndex < headIndex; tailIndex++ )
         */

        private int headIndex = 0;
        private int tailIndex = -1;

        EdgeIterator()
        {
            super();
        }

        public boolean hasNext()
        {
            int n = getNodeSize();
            return (headIndex < n)
                && !(headIndex == n - 1 && tailIndex == headIndex - 1);
        }

        public Object next()
        {
            tailIndex++;
            if( tailIndex >= headIndex ) {
                headIndex++;
                tailIndex = 0;
            }
            if( headIndex >= getNodeSize() ) {
                throw new NoSuchElementException();
            }
            return createEdge( tailIndex, headIndex );
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
            int k = i + 1;
            if( k == nodeIndex ) {
                k++;
            }
            return k < getNodeSize();
        }

        public Object next()
        {
            i++;
            if( i == nodeIndex ) {
                i++;
            }
            if( i >= getNodeSize() ) {
                throw new NoSuchElementException();
            }
            return new Integer( i );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( i == -1 || i >= getNodeSize() ) {
                throw new IllegalStateException();
            }
            return createEdge( nodeIndex, i );
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }

}
