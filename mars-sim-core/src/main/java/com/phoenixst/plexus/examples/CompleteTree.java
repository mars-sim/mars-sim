/*
 *  $Id: CompleteTree.java,v 1.32 2006/06/20 01:09:29 rconner Exp $
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
 *  A <code>Graph</code> which is a complete tree.
 *
 *  @version    $Revision: 1.32 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class CompleteTree extends AbstractIntegerNodeGraph
{

    private static final long serialVersionUID = 2L;


    /*
     *  A complete tree of height H with K children per internal node
     *  has:
     *
     *    (K^H - 1) / (K - 1)     internal nodes
     *    (K^H)                   leaf nodes
     *    (K^(H+1) - 1) / (K - 1) total nodes
     *
     *  The numInternalNodes field is calculated, and used as a
     *  dividing point in node indices.  Node 0 is the root, nodes 1
     *  through (numInternalNodes-1) are the rest of the internal
     *  nodes, and all nodes >= numInternalNodes are leaves.
     *
     *  For a given node of index I, other related nodes (if
     *  appropriate) are:
     *
     *     parent index = (I - 1) / K
     *     child indices = (I * K) + 1 through (I * K) + K
     */

    /**
     *  The height.
     *
     *  @serial
     */
    private final int height;

    /**
     *  The number of children for each internal node.
     *
     *  @serial
     */
    private final int numChildren;

    /**
     *  The number of internal nodes.
     */
    private transient int numInternalNodes;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>CompleteTree</code>.  A tree of height 0
     *  is just a single root node (numChildren is ignored in this
     *  case).
     */
    public CompleteTree( int height, int numChildren )
    {
        super( getNodeSize( height, numChildren ) );
        this.height = height;
        this.numChildren = numChildren;
        if( height < 0 ) {
            throw new IllegalArgumentException( "A CompleteTree must have height at least 0: " + height );
        }
        if( numChildren < 1 ) {
            throw new IllegalArgumentException( "A CompleteTree must have at least 1 child per internal node: " + numChildren );
        }

        if( numChildren == 1 ) {
            numInternalNodes = height;
        } else {
            int numNodes = 1;
            for( int i = 0; i < height; i++ ) {
                numNodes *= numChildren;
            }
            numInternalNodes = (numNodes - 1) / (numChildren - 1);
        }
    }


    private static int getNodeSize( int height, int numChildren )
    {
        if( numChildren == 1 ) {
            return height + 1;
        }
        int numNodes = 1;
        for( int i = 0; i < height; i++ ) {
            numNodes *= numChildren;
        }
        return (numNodes * numChildren - 1) / (numChildren - 1);
    }


    ////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( height < 0 ) {
            throw new InvalidObjectException( "A CompleteTree must have height at least 0: " + height );
        }
        if( numChildren < 1 ) {
            throw new InvalidObjectException( "A CompleteTree must have at least 1 child per internal node: " + numChildren );
        }

        int numNodes = 1;
        if( numChildren == 1 ) {
            numInternalNodes = height;
            numNodes = height + 1;
        } else {
            numNodes = 1;
            for( int i = 0; i < height; i++ ) {
                numNodes *= numChildren;
            }
            numInternalNodes = (numNodes - 1) / (numChildren - 1);
            numNodes = (numNodes * numChildren - 1) / (numChildren - 1);
        }

        if( numNodes != getNodeSize() ) {
            throw new InvalidObjectException( "Calculated number of nodes does not match." );
        }
    }


    ////////////////////////////////////////
    // CompleteTree Methods
    ////////////////////////////////////////


    /**
     *  Gets the height of this <code>CompleteTree</code>.
     */
    public final int getHeight()
    {
        return height;
    }


    /**
     *  Gets the number of children of each internal node in this
     *  <code>CompleteTree</code>.
     */
    public final int getNumChildren()
    {
        return numChildren;
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
        if( nodeIndex == 0 ) {
            return ( height == 0 )
                ? 0
                : numChildren;
        } else if( nodeIndex < numInternalNodes ) {
            return numChildren + 1;
        } else {
            return 1;
        }
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    protected Graph.Edge createEdge( int tailIndex, int headIndex )
    {
        if( tailIndex < headIndex ) {
            if( tailIndex == (headIndex - 1) / numChildren ) {
                return new EdgeImpl( tailIndex, headIndex, true );
            }
        } else if( tailIndex > headIndex ) {
            if( headIndex == (tailIndex - 1) / numChildren ) {
                return new EdgeImpl( headIndex, tailIndex, true );
            }
        }
        return null;
    }


    protected Collection createEdgeCollection()
    {
        return new EdgeCollection();
    }


    protected Traverser createTraverser( int nodeIndex )
    {
        if( height == 0 ) {
            return GraphUtils.EMPTY_TRAVERSER;
        } else if( nodeIndex == 0 ) {
            return new RootTraverser();
        } else if( nodeIndex < numInternalNodes ) {
            return new InternalTraverser( nodeIndex );
        } else {
            int tailIndex = (nodeIndex - 1) / numChildren;
            return GraphUtils.singletonTraverser( this,
                                                  new Integer( tailIndex ),
                                                  createEdge( tailIndex, nodeIndex ) );
        }
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Complete Tree( " );
        s.append( height );
        s.append( ", " );
        s.append( numChildren );
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
            return getHeight() == 0 ? EmptyIterator.INSTANCE : new EdgeIterator();
        }
    }


    /**
     *
     */
    private class EdgeIterator
        implements Iterator
    {
        private int headIndex = 0;

        EdgeIterator()
        {
            super();
        }

        public boolean hasNext()
        {
            return headIndex < getNodeSize() - 1;
        }

        public Object next()
        {
            headIndex++;
            if( headIndex >= getNodeSize() ) {
                throw new NoSuchElementException();
            }
            return createEdge( (headIndex - 1) / getNumChildren(), headIndex );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }


    /**
     *
     */
    private class RootTraverser
        implements Traverser
    {
        /*
         *  We don't have to check for height==0 here, since the empty
         *  case is caught in the adjacentTraverser() method.
         */

        private int headIndex = 0;

        RootTraverser()
        {
            super();
        }

        public boolean hasNext()
        {
            return headIndex < getNumChildren();
        }

        public Object next()
        {
            if( headIndex >= getNumChildren() ) {
                throw new NoSuchElementException();
            }
            headIndex++;
            return new Integer( headIndex );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( headIndex == 0 || headIndex > getNumChildren() ) {
                throw new IllegalStateException();
            }
            return createEdge( 0, headIndex );
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }


    /**
     *
     */
    private class InternalTraverser
        implements Traverser
    {
        /*
         *  When i is -1, we haven't started yet
         *  When i is 0, traverse the parent edge
         *  When i is 1->#children, traverse child edges
         */

        private final int nodeIndex;
        private final int parentIndex;
        private final int baseChildIndex;
        private int i = -1;

        InternalTraverser( int nodeIndex )
        {
            super();
            this.nodeIndex = nodeIndex;
            parentIndex = (nodeIndex - 1) / getNumChildren();
            baseChildIndex = nodeIndex * getNumChildren();
        }

        public boolean hasNext()
        {
            return i < getNumChildren();
        }

        public Object next()
        {
            if( i >= getNumChildren() ) {
                throw new NoSuchElementException();
            }
            i++;
            return ( i == 0 )
                ? new Integer( parentIndex )
                : new Integer( baseChildIndex + i );
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        public Graph.Edge getEdge()
        {
            if( i == -1 || i > getNumChildren() ) {
                throw new IllegalStateException();
            }
            return ( i == 0 )
                ? createEdge( parentIndex, nodeIndex )
                : createEdge( nodeIndex, baseChildIndex + i );
        }

        public void removeEdge()
        {
            throw new UnsupportedOperationException();
        }
    }

}
