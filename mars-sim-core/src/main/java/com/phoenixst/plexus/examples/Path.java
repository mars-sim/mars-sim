/*
 *  $Id: Path.java,v 1.29 2006/06/20 01:09:29 rconner Exp $
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

import org.apache.commons.collections.iterators.SingletonIterator;

import com.phoenixst.plexus.*;


/**
 *  A <code>Graph</code> containing a set of <code>Integer</code>
 *  nodes connected by a path of edges from the first node to the last
 *  one.
 *
 *  @version    $Revision: 1.29 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class Path extends AbstractIntegerNodeGraph
{

    private static final long serialVersionUID = 2L;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>Path</code>.
     */
    public Path( int n )
    {
        super( n );
        if( n < 2 ) {
            throw new IllegalArgumentException( "A Path must have at least 2 nodes: " + n );
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
        if( getNodeSize() < 2 ) {
            throw new InvalidObjectException( "A Path must have at least 2 nodes: " + getNodeSize() );
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
        int nodeIndex = checkNode( node );
        return (nodeIndex == 0 || nodeIndex == getNodeSize() - 1) ? 1 : 2;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    protected Graph.Edge createEdge( int tailIndex, int headIndex )
    {
        if( tailIndex + 1 == headIndex ) {
            return new EdgeImpl( tailIndex, headIndex, true );
        } else if( headIndex + 1 == tailIndex ) {
            return new EdgeImpl( headIndex, tailIndex, true );
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
        s.append( "Path( " );
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
            return getNodeSize() - 1;
        }

        public Iterator iterator()
        {
            return new EdgeIterator();
        }
    }


    private class EdgeIterator
        implements Iterator
    {
        private int i = 0;

        EdgeIterator()
        {
            super();
        }

        public boolean hasNext()
        {
            return i < getNodeSize() - 1;
        }

        public Object next()
        {
            i++;
            if( i >= getNodeSize() ) {
                throw new NoSuchElementException();
            }
            return createEdge( i - 1, i );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


    private class TraverserImpl
        implements Traverser
    {
        private final Object node;
        private final Iterator i;
        private Graph.Edge currentEdge;
        private boolean isCurrentValid = false;

        TraverserImpl( int nodeIndex )
        {
            super();
            this.node = new Integer( nodeIndex );
            int n = getNodeSize();
            if( nodeIndex == 0 ) {
                i = new SingletonIterator( createEdge( 0, 1 ) );
            } else if( nodeIndex == n - 1 ) {
                i = new SingletonIterator( createEdge( n - 2, n - 1 ) );
            } else {
                i = Arrays.asList( new Graph.Edge[] { createEdge( nodeIndex - 1, nodeIndex ),
                                                      createEdge( nodeIndex, nodeIndex + 1 ) } ).iterator();
            }
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public Object next()
        {
            currentEdge = (Graph.Edge) i.next();
            isCurrentValid = true;
            return currentEdge.getOtherEndpoint( node );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( !isCurrentValid ) {
                throw new IllegalStateException();
            }
            return currentEdge;
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }

}
