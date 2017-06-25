/*
 *  $Id: Join.java,v 1.59 2006/06/21 20:15:26 rconner Exp $
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

package com.phoenixst.plexus.operations;

import java.io.*;
import java.util.*;

import org.apache.commons.collections.iterators.EmptyIterator;

import com.phoenixst.collections.*;
import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.*;


/**
 *  A <code>Graph</code> which is the join of two other
 *  <code>Graphs</code> with disjoint node sets.  This implementation
 *  does not actually check that the node sets are disjoint, but will
 *  definitely behave strangely if they are not.  The new edges added
 *  to the the union of the two graphs may be directed or undirected as
 *  specified in the constructor.  These new edges never contain
 *  user-defined objects.
 *
 *  @version    $Revision: 1.59 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class Join extends AbstractGraph
    implements Serializable
{

    private static final long serialVersionUID = 2L;


    /**
     *  The left operand.
     *
     *  @serial
     */
    final Graph left;

    /**
     *  The right operand.
     *
     *  @serial
     */
    final Graph right;

    /**
     *  Whether or not the join edges are directed.
     *
     *  @serial
     */
    final boolean isDirected;

    /**
     *  All the nodes.
     */
    private transient Collection nodeCollection;

    /**
     *  All the edges.
     */
    private transient Collection edgeCollection;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>Join</code> graph.
     *
     *  @param left the first (left) graph operand for the join
     *  operation.
     *
     *  @param right the second (right) graph operand for the join
     *  operation.
     *
     *  @param isDirected whether or not the new edges added as part
     *  of the join operation are directed.  If <code>true</code>,
     *  nodes from the first operand are the tails of the edges.
     */
    public Join( Graph left, Graph right, boolean isDirected )
    {
        super();
        this.left = left;
        this.right = right;
        this.isDirected = isDirected;
        if( left == null ) {
            throw new IllegalArgumentException( "Left operand is null." );
        }
        if( right == null ) {
            throw new IllegalArgumentException( "Right operand is null." );
        }
        initialize();
    }


    ////////////////////////////////////////
    // Serialization and construction assistance methods
    ////////////////////////////////////////


    private void initialize()
    {
        Collection nodes = new CompositeCollection( left.nodes( null ), right.nodes( null ) );
        nodeCollection = Collections.unmodifiableCollection( nodes );
        edgeCollection = new EdgeCollection();
    }


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( left == null ) {
            throw new InvalidObjectException( "Left operand is null." );
        }
        if( right == null ) {
            throw new InvalidObjectException( "Right operand is null." );
        }
        initialize();
    }


    ////////////////////////////////////////
    // Join Methods
    ////////////////////////////////////////


    /**
     *
     */
    public Graph getLeftOperand()
    {
        return left;
    }


    /**
     *
     */
    public Graph getRightOperand()
    {
        return right;
    }


    ////////////////////////////////////////
    // AbstractGraph Methods
    ////////////////////////////////////////


    protected Collection nodes()
    {
        return nodeCollection;
    }


    protected Collection edges()
    {
        return edgeCollection;
    }


    protected Traverser traverser( Object node )
    {
        boolean isLeftNode = left.containsNode( node );
        Traverser sameTraverser;
        if( isLeftNode ) {
            sameTraverser = left.traverser( node, null );
        } else {
            sameTraverser = right.traverser( node, null );
        }
        return new TraverserChain( new UnmodifiableTraverser( sameTraverser ),
                                   new CrossTraverserImpl( node, isLeftNode ) );
    }


    ////////////////////////////////////////
    // Graph Methods
    ////////////////////////////////////////


    /**
     *  Returns <code>true</code> if this <code>Graph</code> contains
     *  <code>node</code>.
     */
    public boolean containsNode( Object node )
    {
        return left.containsNode( node ) || right.containsNode( node );
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  This implementation traverses over the edges in this graph
     *  incident on the tail of the specified <code>edge</code>,
     *  looking for it and returning <code>true</code> if found.
     */
    public boolean containsEdge( Graph.Edge edge )
    {
        if( edge == null ) {
            return false;
        }
        Object tail = edge.getTail();
        Object head = edge.getHead();

        if( left.containsNode( tail ) ) {
            if( right.containsNode( head ) ) {
                Graph.Edge testEdge = new DefaultSimpleEdge( tail, head, isDirected );
                if( testEdge.equals( edge ) ) {
                    return true;
                }
            }
            if( left.containsEdge( edge ) ) {
                return true;
            }
        }

        return right.containsNode( tail )
            && right.containsEdge( edge );
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeEdge( Graph.Edge edge )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Returns the degree of <code>node</code>, defined as the number
     *  of edges incident on <code>node</code>, with self-loops
     *  counted twice.
     */
    public int degree( Object node )
    {
        return left.containsNode( node )
            ? left.degree( node ) + right.nodes( null ).size()
            : right.degree( node ) + left.nodes( null ).size();
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Join[ " );
        s.append( left );
        s.append( ", " );
        s.append( right );
        s.append( ", " );
        s.append( isDirected ? "dir" : "undir" );
        s.append( " ]" );
        return s.toString();
    }


    ////////////////////////////////////////
    // Private Classes
    ////////////////////////////////////////


    /**
     *  This implementation iterates over the null edges between each
     *  possible pair of nodes, one from left and the other from right.
     */
    private class CrossEdgeIterator
        implements Iterator
    {
        private final Iterator pairIter;

        CrossEdgeIterator()
        {
            super();
            pairIter = CartesianProduct.leftIterator( left.nodes( null ), right.nodes( null ) );
        }

        public boolean hasNext()
        {
            return pairIter.hasNext();
        }

        public Object next()
        {
            OrderedPair pair = (OrderedPair) pairIter.next();
            return new DefaultSimpleEdge( pair.getFirst(), pair.getSecond(),
                                          isDirected );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


    /**
     *  This implementation iterates over the nodes in the graph other
     *  than the one containing the specified node, each of which is
     *  incident through a null edge.
     */
    private class CrossTraverserImpl
        implements Traverser
    {
        private final Object node;
        private final boolean isNodeTail;
        private final Iterator nodeIter;
        private Object currentNode;
        private boolean hasStarted = false;

        CrossTraverserImpl( Object node, boolean isLeftNode )
        {
            super();
            this.node = node;
            isNodeTail = isLeftNode;
            if( isNodeTail ) {
                nodeIter = right.nodes( null ).iterator();
            } else {
                nodeIter = left.nodes( null ).iterator();
            }
        }

        public boolean hasNext()
        {
            return nodeIter.hasNext();
        }

        public Object next()
        {
            currentNode = nodeIter.next();
            hasStarted = true;
            return currentNode;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( !hasStarted ) {
                throw new IllegalStateException();
            }
            return isNodeTail
                ? new DefaultSimpleEdge( node, currentNode, isDirected )
                : new DefaultSimpleEdge( currentNode, node, isDirected );
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }


    private class EdgeCollection extends AbstractEdgeCollection
    {
        EdgeCollection()
        {
            super( Join.this );
        }

        public int size()
        {
            return left.edges( null ).size()
                + right.edges( null ).size()
                + (left.nodes( null ).size() * right.nodes( null ).size());
        }

        public boolean isEmpty()
        {
            return left.edges( null ).isEmpty()
                && right.edges( null ).isEmpty()
                && (left.nodes( null ).isEmpty()
                    || right.nodes( null ).isEmpty());
        }

        public Iterator iterator()
        {
            return isEmpty()
                ? EmptyIterator.INSTANCE
                : new IteratorChain( new Iterator[]
                    {
                        new UnmodifiableIterator( left.edges( null ).iterator() ),
                        new UnmodifiableIterator( right.edges( null ).iterator() ),
                        new CrossEdgeIterator()
                    } );
        }
    }

}
