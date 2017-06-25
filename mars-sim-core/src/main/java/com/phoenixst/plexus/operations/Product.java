/*
 *  $Id: Product.java,v 1.61 2006/06/20 00:59:08 rconner Exp $
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

import com.phoenixst.collections.*;
import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.TraverserChain;


/**
 *  A <code>Graph</code> which is the product of two other
 *  <code>Graphs</code>.  The nodes are {@link OrderedPair} objects
 *  with exactly two elements, the first element being a node from the
 *  first graph and the second being a node from the second graph.
 *
 *  <P>If either wrapped <code>Graph</code> contains
 *  <code>Graph.Edges</code> which point to other
 *  <code>Graph.Edges</code>, the product will <strong>not</strong>
 *  reflect this.  The node and edge aspects of any such
 *  <code>Graph.Edge</code> will be distinct in the product.
 *
 *  @version    $Revision: 1.61 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class Product extends AbstractGraph
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
     *  Creates a new <code>Product</code> graph.
     *
     *  @param left the first (left) graph operand for the product
     *  operation.
     *
     *  @param right the second (right) graph operand for the product
     *  operation.
     */
    public Product( Graph left, Graph right )
    {
        super();
        this.left = left;
        this.right = right;
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
        nodeCollection = CartesianProduct.leftProduct( left.nodes( null ),
                                                       right.nodes( null ) );
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
    // Product Methods
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
        OrderedPair baseNode = (OrderedPair) node;
        Object leftNode = baseNode.getFirst();
        Object rightNode = baseNode.getSecond();
        return new TraverserChain( new TraverserImpl( left.traverser( leftNode, null ), rightNode, true ),
                                   new TraverserImpl( right.traverser( rightNode, null ), leftNode, false ) );
    }


    ////////////////////////////////////////
    // Graph Methods
    ////////////////////////////////////////


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Returns <code>true</code> if this <code>Graph</code> contains
     *  <code>node</code>.
     */
    public boolean containsNode( Object node )
    {
        if( !(node instanceof OrderedPair) ) {
            return false;
        }
        OrderedPair pair = (OrderedPair) node;
        return left.containsNode( pair.getFirst() )
            && right.containsNode( pair.getSecond() );
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeEdge( Graph.Edge edge )
    {
        throw new UnsupportedOperationException();
    }


    public boolean containsEdge( Graph.Edge edge )
    {
        if( !(edge instanceof EdgeImpl) ) {
            return false;
        }
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        if( edgeImpl.isLeftEdge ) {
            return right.containsNode( edgeImpl.node )
                && left.containsEdge( edgeImpl.edge );
        }
        return left.containsNode( edgeImpl.node )
            && right.containsEdge( edgeImpl.edge );
    }


    /**
     *  Returns the degree of <code>node</code>, defined as the number
     *  of edges incident on <code>node</code>, with self-loops
     *  counted twice.
     */
    public int degree( Object node )
    {
        OrderedPair baseNode = (OrderedPair) node;
        Object leftNode = baseNode.getFirst();
        Object rightNode = baseNode.getSecond();
        return left.degree( leftNode ) + right.degree( rightNode );
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Product[ " );
        s.append( left );
        s.append( ", " );
        s.append( right );
        s.append( " ]" );
        return s.toString();
    }


    ////////////////////////////////////////
    // Private Classes
    ////////////////////////////////////////


    /**
     *  An immutable <code>Graph.Edge</code> that wraps an edge from
     *  one of the argument graphs, but uses a tail and head from this
     *  graph.
     */
    private static class EdgeImpl
        implements Graph.Edge,
                   Serializable
    {
        private static final long serialVersionUID = 2L;

        /**
         *  The wrapped edge.
         *
         *  @serial
         */
        final Graph.Edge edge;

        /**
         *  The node in the operand graph that does not contain the
         *  wrapped edge.
         *
         *  @serial
         */
        final Object node;

        /**
         *  Whether the wrapped edge is from the left operand.
         *
         *  @serial
         */
        final boolean isLeftEdge;

        EdgeImpl( Graph.Edge edge,
                  Object node,
                  boolean isLeftEdge )
        {
            super();
            this.edge = edge;
            this.node = node;
            this.isLeftEdge = isLeftEdge;
        }

        private void readObject( ObjectInputStream in )
            throws ClassNotFoundException,
                   IOException
        {
            in.defaultReadObject();
            if( edge == null ) {
                throw new InvalidObjectException( "Wrapped Graph.Edge is null." );
            }
        }

        // Graph.Edge methods

        public boolean isDirected()
        {
            return edge.isDirected();
        }

        public Object getUserObject()
        {
            return edge.getUserObject();
        }

        public void setUserObject( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public Object getTail()
        {
            return isLeftEdge
                ? new OrderedPair( edge.getTail(), node )
                : new OrderedPair( node, edge.getTail() );
        }

        public Object getHead()
        {
            return isLeftEdge
                ? new OrderedPair( edge.getHead(), node )
                : new OrderedPair( node, edge.getHead() );
        }

        public Object getOtherEndpoint( Object otherNode )
        {
            Object tail = getTail();
            Object head = getHead();
            if( GraphUtils.equals( otherNode, tail ) ) {
                return head;
            } else if( GraphUtils.equals( otherNode, head ) ) {
                return tail;
            } else {
                throw new IllegalArgumentException( "Graph.Edge is not incident on the node: " + otherNode );
            }
        }

        // Other methods

        public boolean equals( Object object )
        {
            if( this == object ) {
                return true;
            }
            if( !(object instanceof EdgeImpl) ) {
                return false;
            }
            EdgeImpl edgeImpl = (EdgeImpl) object;
            return (isLeftEdge == edgeImpl.isLeftEdge)
                && edge.equals( edgeImpl.edge )
                && GraphUtils.equals( node, edgeImpl.node );
        }

        public int hashCode()
        {
            return (isLeftEdge ? 1231 : 1237)
                ^ edge.hashCode()
                ^ ((node == null) ? 0 : node.hashCode());
        }

        public String toString()
        {
            return GraphUtils.getTextValue( this, true ).toString();
        }
    }


    private static class TraverserImpl
        implements Traverser
    {
        private final Traverser t;
        private final Object otherNode;
        private final boolean isLeftEdge;

        TraverserImpl( Traverser t, Object otherNode, boolean isLeftEdge )
        {
            super();
            this.t = t;
            this.otherNode = otherNode;
            this.isLeftEdge = isLeftEdge;
        }

        public boolean hasNext()
        {
            return t.hasNext();
        }

        public Object next()
        {
            Object node = t.next();
            return isLeftEdge
                ? new OrderedPair( node, otherNode )
                : new OrderedPair( otherNode, node );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            return new EdgeImpl( t.getEdge(), otherNode, isLeftEdge );
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }


    private class EdgeCollection extends AbstractUnmodifiableCollection
    {
        // left edges X right nodes
        private final Collection leftProduct;
        // left nodes X right edges
        private final Collection rightProduct;

        EdgeCollection()
        {
            super();
            leftProduct = CartesianProduct.leftProduct( left.edges( null ),
                                                        right.nodes( null ) );
            rightProduct = CartesianProduct.rightProduct( left.nodes( null ),
                                                          right.edges( null ) );
        }

        public boolean contains( Object object )
        {
            return (object instanceof EdgeImpl)
                && containsEdge( (EdgeImpl) object );
        }

        public int size()
        {
            return leftProduct.size() + rightProduct.size();
        }

        public boolean isEmpty()
        {
            return leftProduct.isEmpty() && rightProduct.isEmpty();
        }

        public Iterator iterator()
        {
            return new IteratorChain( new EdgeIterator( leftProduct.iterator(), true ),
                                      new EdgeIterator( rightProduct.iterator(), false ) );
        }
    }


    private static class EdgeIterator
        implements Iterator
    {
        private final Iterator pairIter;
        private final boolean isLeftEdge;

        EdgeIterator( Iterator pairIter, boolean isLeftEdge )
        {
            super();
            this.pairIter = pairIter;
            this.isLeftEdge = isLeftEdge;
        }

        public boolean hasNext()
        {
            return pairIter.hasNext();
        }

        public Object next()
        {
            OrderedPair pair = (OrderedPair) pairIter.next();
            return isLeftEdge
                ? new EdgeImpl( (Graph.Edge) pair.getFirst(), pair.getSecond(), true )
                : new EdgeImpl( (Graph.Edge) pair.getSecond(), pair.getFirst(), false );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

}
