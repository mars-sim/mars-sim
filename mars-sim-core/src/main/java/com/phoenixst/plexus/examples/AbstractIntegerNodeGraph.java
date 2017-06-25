/*
 *  $Id: AbstractIntegerNodeGraph.java,v 1.8 2006/06/20 01:09:29 rconner Exp $
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

import com.phoenixst.collections.AbstractUnmodifiableCollection;
import com.phoenixst.plexus.*;


/**
 *  An unmodifiable graph where the nodes are <code>Integers</code>
 *  from zero to a specified number (exclusive) and the edges do not
 *  contain user-defined objects.  This is mainly useful for
 *  implementing special-case graphs that do not actually need to
 *  explicitly store their structure.
 *
 *  <P>This implementation is simple.  Any extension must also be
 *  simple to support on-the-fly <code>Graph.Edge</code> creation.  To
 *  fully implement an extension of this class, the programmer must
 *  provide implementations for the following methods:
 *
 *  <UL>
 *    <LI>{@link #createEdge(int,int) createEdge( tailIndex, headIndex )}</LI>
 *    <LI>{@link #createEdgeCollection()}</LI>
 *    <LI>{@link #createTraverser(int) createTraverser( nodeIndex )}</LI>
 *  </UL>
 *
 *  <P>In addition, it is recommended that the programmer override
 *  {@link #degree(Object) degree( node )} (when applicable) since its
 *  default implementation inefficiently depends upon other
 *  iterator-returning methods.
 *
 *  @version    $Revision: 1.8 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public abstract class AbstractIntegerNodeGraph extends AbstractGraph
    implements Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The number of nodes in this graph.
     *
     *  @serial
     */
    private final int n;

    /**
     *  The collection of nodes.
     */
    private transient Collection allNodes;

    /**
     *  The collection of edges.
     */
    private transient Collection allEdges;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>AbstractIntegerNodeGraph</code>.
     */
    protected AbstractIntegerNodeGraph( int n )
    {
        super();
        this.n = n;
        if( n < 0 ) {
            throw new IllegalArgumentException( "Graph must have at least zero nodes." );
        }
        initialize();
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    private void initialize()
    {
        allNodes = new NodeCollection( n );
        allEdges = createEdgeCollection();
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( n < 0 ) {
            throw new InvalidObjectException( "N must be non-negative: " + n );
        }
        initialize();
    }


    ////////////////////////////////////////
    // AbstractGraph methods
    ////////////////////////////////////////


    protected final Collection nodes()
    {
        return allNodes;
    }


    protected final Collection edges()
    {
        return allEdges;
    }


    protected final Traverser traverser( Object node )
    {
        return createTraverser( checkNode( node ) );
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public final boolean removeNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Returns <code>true</code> if this <code>Graph</code> contains
     *  the specified node.
     */
    public final boolean containsNode( Object node )
    {
        return getNodeIndex( node ) != -1;
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public final boolean removeEdge( Graph.Edge edge )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Returns <code>true</code> if this <code>Graph</code> contains
     *  the specified <code>Graph.Edge</code>.  This implementation
     *  tests the specified edge for equality with the one returned by
     *  {@link #createEdge createEdge( tailIndex, headIndex )} with
     *  the same tail and head.
     */
    public boolean containsEdge( Graph.Edge edge )
    {
        if( edge == null ) {
            return false;
        }
        int tailIndex = getNodeIndex( edge.getTail() );
        int headIndex = getNodeIndex( edge.getHead() );
        return tailIndex != -1
            && headIndex != -1
            && edge.equals( createEdge( tailIndex, headIndex ) );
    }


    ////////////////////////////////////////
    // Methods for subclasses
    ////////////////////////////////////////


    /**
     *  Returns the number of nodes in this
     *  <code>AbstractIntegerNodeGraph</code>.
     */
    protected final int getNodeSize()
    {
        return n;
    }


    /**
     *  Returns the specified node as a primitive <code>int</code>
     *  from <code>0</code> to the number of nodes in this graph
     *  (exclusive) if the specified node is in this graph.
     *  Otherwise, returns <code>-1</code>.
     */
    protected final int getNodeIndex( Object node )
    {
        if( !(node instanceof Integer) ) {
            return -1;
        }
        int value = ((Integer) node).intValue();
        return ( value >= 0 && value < n ) ? value : -1;
    }


    /**
     *  Returns the specified node as a primitive <code>int</code>
     *  from <code>0</code> to the number of nodes in this graph
     *  (exclusive) if the specified node is in this graph.
     *  Otherwise, throws an <code>NoSuchNodeException</code>.
     */
    protected final int checkNode( Object node )
    {
        int nodeIndex = getNodeIndex( node );
        if( nodeIndex == -1 ) {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }
        return nodeIndex;
    }


    /**
     *  If an edge exists between the specified indices, create and
     *  return it.  If not, return <code>null</code>.  The indices are
     *  assumed to represent valid nodes for this <code>Graph</code>.
     */
    protected abstract Graph.Edge createEdge( int tailIndex, int headIndex );


    /**
     *  Creates the (single) collection of edges for this instance.
     */
    protected abstract Collection createEdgeCollection();


    /**
     *  Creates a traverser with no filtering; assumes that the node
     *  index is valid.
     */
    protected abstract Traverser createTraverser( int nodeIndex );


    ////////////////////////////////////////
    // Protected Graph.Edge implementation
    ////////////////////////////////////////


    /**
     *  Protected <code>Graph.Edge</code> implementation.
     */
    protected static class EdgeImpl
        implements Graph.Edge,
                   Serializable
    {
        private static final long serialVersionUID = 2L;

        /**
         *  The tail.
         *
         *  @serial
         */
        private final int tail;

        /**
         *  The head.
         *
         *  @serial
         */
        private final int head;

        /**
         *  Whether or not this edge is directed.
         *
         *  @serial
         */
        private final boolean directed;

        protected EdgeImpl( int tail, int head, boolean directed )
        {
            super();
            this.tail = tail;
            this.head = head;
            this.directed = directed;
        }

        private void readObject( ObjectInputStream in )
            throws ClassNotFoundException,
                   IOException
        {
            in.defaultReadObject();
            if( tail < 0 ) {
                throw new InvalidObjectException( "Tail is invalid: " + tail );
            }
            if( head < 0  ) {
                throw new InvalidObjectException( "Head is invalid: " + head );
            }
        }

        // Graph.Edge

        public boolean isDirected()
        {
            return directed;
        }

        public Object getUserObject()
        {
            return null;
        }

        public void setUserObject( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public Object getTail()
        {
            return new Integer( tail );
        }

        public Object getHead()
        {
            return new Integer( head );
        }

        public Object getOtherEndpoint( Object node )
        {
            if( !(node instanceof Integer ) ) {
                throw new IllegalArgumentException( "Graph.Edge is not incident on the node: " + node );
            }
            int nodeValue = ((Integer) node).intValue();
            if( nodeValue == tail ) {
                return new Integer( head );
            } else if( nodeValue == head ) {
                return new Integer( tail );
            } else {
                throw new IllegalArgumentException( "Graph.Edge is not incident on the node: " + node );
            }
        }

        public boolean equals( Object object )
        {
            if( this == object ) {
                return true;
            }
            if( !(object instanceof EdgeImpl) ) {
                return false;
            }
            EdgeImpl edge = (EdgeImpl) object;
            return (directed == edge.directed)
                && ( (edge.tail == tail && edge.head == head)
                     || (!directed && edge.tail == head && edge.head == tail) );
        }

        public int hashCode()
        {
            return tail ^ head;
        }

        public String toString()
        {
            StringBuilder s = new StringBuilder();
            s.append( tail );
            s.append( directed ? " -> " : " -- " );
            s.append( head );
            return s.toString();
        }
    }


    ////////////////////////////////////////
    // Collection view classes
    ////////////////////////////////////////


    private static class NodeCollection extends AbstractUnmodifiableCollection
    {
        private final int n;

        NodeCollection( int n )
        {
            super();
            this.n = n;
        }

        public int size()
        {
            return n;
        }

        public boolean isEmpty()
        {
            return n == 0;
        }

        public boolean contains( Object object )
        {
            if( !(object instanceof Integer) ) {
                return false;
            }
            int value = ((Integer) object).intValue();
            return value >= 0 && value < n;
        }

        public Iterator iterator()
        {
            return new NodeIterator( n );
        }

        private static class NodeIterator
            implements Iterator
        {
            private final int n;
            private int i = 0;

            NodeIterator( int n )
            {
                super();
                this.n = n;
            }

            public boolean hasNext()
            {
                return i < n;
            }

            public Object next()
            {
                if( i >= n ) {
                    throw new NoSuchElementException();
                }
                return new Integer( i++ );
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        }
    }


    protected abstract class AbstractIntegerEdgeCollection extends AbstractUnmodifiableCollection
    {
        protected AbstractIntegerEdgeCollection()
        {
            super();
        }

        public boolean contains( Object object )
        {
            return (object instanceof Graph.Edge)
                && containsEdge( (Graph.Edge) object );
        }
    }

}
