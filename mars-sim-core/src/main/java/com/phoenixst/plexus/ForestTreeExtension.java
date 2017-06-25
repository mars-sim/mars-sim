/*
 *  $Id: ForestTreeExtension.java,v 1.9 2005/10/03 15:24:00 rconner Exp $
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

package com.phoenixst.plexus;

import java.util.*;

import com.phoenixst.collections.Identifier;


/**
 *  A {@link RootedTree} which is formed by extending an {@link
 *  OrientedForest}, adding a dummy root node (and edges) which is the
 *  parent of the forest's root nodes.
 *
 *  @version    $Revision: 1.9 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class ForestTreeExtension
    implements RootedTree
{

    static final Object DUMMY_EDGE_OBJECT = new Identifier( "Dummy Edge" );

    private Object root;

    private final OrientedForest forest;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>ForestTreeExtension</code>.
     */
    public ForestTreeExtension( OrientedForest forest )
    {
        this( new Identifier( "Root" ), forest );
    }


    /**
     *  Creates a new <code>ForestTreeExtension</code>.  The specified
     *  forest should <em>not</em> contain the given root node.
     */
    public ForestTreeExtension( Object root,
                                OrientedForest forest )
    {
        super();
        this.root = root;
        this.forest = forest;
    }


    ////////////////////////////////////////
    // Accessor for private classes
    ////////////////////////////////////////


    OrientedForest getForest()
    {
        return forest;
    }


    ////////////////////////////////////////
    // Rooted
    ////////////////////////////////////////


    public Object getRoot()
    {
        return root;
    }


    /**
     *  Sets the root node, which must <em>not</em> be contained by
     *  the forest being extended, although this implementation does
     *  not check for that condition.
     */
    public void setRoot( Object root )
    {
        this.root = root;
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    public Object getParent( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            return null;
        }
        Graph.Edge edge = forest.getParentEdge( node );
        if( edge == null ) {
            return root;
        }
        return edge.getOtherEndpoint( node );
    }


    /**
     *  Returns a <code>Traverser</code> over the children of the
     *  specified node.  <code>Traversers</code> over the children of
     *  the root node are unmodifiable.
     */
    public Traverser childTraverser( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            return new RootTraverser( this );
        }
        return forest.childTraverser( node );
    }


    public Graph.Edge getParentEdge( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            return null;
        }
        Graph.Edge edge = forest.getParentEdge( node );
        if( edge == null ) {
            edge = new DummyEdge( this, node );
        }
        return edge;
    }


    public boolean isForestEdge( Graph.Edge edge )
    {
        if( edge instanceof DummyEdge ) {
            return ((DummyEdge) edge).isFromTree( this );
        }
        return forest.isForestEdge( edge );
    }


    public Object getParentEndpoint( Graph.Edge edge )
    {
        if( edge instanceof DummyEdge ) {
            if( !((DummyEdge) edge).isFromTree( this ) ) {
                throw new IllegalArgumentException( "Edge is not a tree edge: " + edge );
            }
            return root;
        }
        return forest.getParentEndpoint( edge );
    }


    public Collection rootNodes()
    {
        return Collections.singleton( root );
    }


    public Object getRoot( Object node )
    {
        if( !isTreeNode( node ) ) {
            throw new NoSuchNodeException( "Node is not in this tree: " + node );
        }
        return root;
    }


    public boolean isLeaf( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            return forest.rootNodes().isEmpty();
        }
        return forest.isLeaf( node );
    }


    public boolean isAncestor( Object ancestor, Object descendant )
    {
        if( GraphUtils.equals( root, ancestor ) ) {
            if( !GraphUtils.equals( root, descendant ) ) {
                // check for descendant being in forest at all.
                forest.getParent( descendant );
            }
            return true;
        }
        if( GraphUtils.equals( root, descendant ) ) {
            // check for ancestor being in forest at all.
            forest.getParent( ancestor );
            return false;
        }
        return forest.isAncestor( ancestor, descendant );
    }


    public Object getLeastCommonAncestor( Object aNode, Object bNode )
    {
        if( GraphUtils.equals( root, aNode ) ) {
            if( !GraphUtils.equals( root, bNode ) ) {
                // check for bNode being in forest at all.
                forest.getParent( bNode );
            }
            return root;
        }
        if( GraphUtils.equals( root, bNode ) ) {
            // check for aNode being in forest at all.
            forest.getParent( aNode );
            return root;
        }
        if( !GraphUtils.equals( forest.getRoot( aNode ),
                                forest.getRoot( bNode ) ) ) {
            return root;
        }
        return forest.getLeastCommonAncestor( aNode, bNode );
    }


    public int getDepth( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            return 0;
        }
        return forest.getDepth( node ) + 1;
    }


    public int getHeight( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            int height = -1;
            for( Iterator i = forest.rootNodes().iterator(); i.hasNext(); ) {
                height = Math.max( height, forest.getHeight( i.next() ) );
            }
            return height + 1;
        }
        return forest.getHeight( node );
    }


    ////////////////////////////////////////
    // RootedTree
    ////////////////////////////////////////


    /**
     *  If the specified node is not the root node, this method
     *  delegates to {@link OrientedForest#getParent} and returns
     *  <code>true</code> if a {@link NoSuchNodeException} is not
     *  thrown.  Otherwise, it returns <code>false</code>.
     */
    public boolean isTreeNode( Object node )
    {
        if( GraphUtils.equals( root, node ) ) {
            return true;
        }
        try {
            forest.getParent( node );
            return true;
        } catch( NoSuchNodeException e ) {
            return false;
        }
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private static class DummyEdge
        implements Graph.Edge
    {
        private final ForestTreeExtension tree;
        private final Object head;

        DummyEdge( ForestTreeExtension tree, Object head )
        {
            super();
            this.tree = tree;
            this.head = head;
        }

        /**
         *  Returns true if this edge is from the specified tree.
         */
        boolean isFromTree( ForestTreeExtension otherTree )
        {
            return otherTree == tree
                && otherTree.getForest().getDepth( head ) == 0;
        }

        public boolean isDirected()
        {
            return true;
        }

        public Object getUserObject()
        {
            return DUMMY_EDGE_OBJECT;
        }

        public void setUserObject( Object object )
        {
            throw new UnsupportedOperationException();
        }

        public Object getTail()
        {
            return tree.getRoot();
        }

        public Object getHead()
        {
            return head;
        }

        public Object getOtherEndpoint( Object node )
        {
            if( GraphUtils.equals( tree.getRoot(), node ) ) {
                return head;
            } else if( GraphUtils.equals( head, node ) ) {
                return tree.getRoot();
            } else {
                throw new IllegalArgumentException( "Edge is not incident on the node: " + node );
            }
        }

        public boolean equals( Object object )
        {
            if( this == object ) {
                return true;
            }
            if( !(object instanceof DummyEdge) ) {
                return false;
            }
            DummyEdge edge = (DummyEdge) object;
            return tree == edge.tree
                && GraphUtils.equals( head , edge.head );
        }

        public int hashCode()
        {
            return ((tree == null) ? 0 : tree.hashCode())
                ^ ((head == null) ? 0 : head.hashCode());
        }

        public String toString()
        {
            return GraphUtils.getTextValue( this, true ).toString();
        }
    }


    private static class RootTraverser
        implements Traverser
    {
        private final ForestTreeExtension tree;
        private final Iterator i;
        private Graph.Edge currentEdge = null;

        RootTraverser( ForestTreeExtension tree )
        {
            super();
            this.tree = tree;
            i = tree.getForest().rootNodes().iterator();
        }

        public boolean hasNext()
        {
            return i.hasNext();
        }

        public Object next()
        {
            Object node = i.next();
            currentEdge = new DummyEdge( tree, node );
            return node;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }
    }

}
