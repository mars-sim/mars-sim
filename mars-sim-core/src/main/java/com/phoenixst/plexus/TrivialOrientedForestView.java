/*
 *  $Id: TrivialOrientedForestView.java,v 1.6 2005/10/03 15:24:00 rconner Exp $
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

import java.util.Collection;


/**
 *  A trivial {@link OrientedForest} view of a {@link Graph}, where
 *  there is no forest structure.  No {@link Graph.Edge Graph.Edges}
 *  are forest edges, and so all nodes are roots and leaves.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TrivialOrientedForestView
    implements GraphView,
               OrientedForest
{

    private final Graph graph;

    private final boolean isStrict;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>TrivialForestView</code> with strict node-
     *  and edge-checking semantics.
     */
    public TrivialOrientedForestView( Graph graph )
    {
        this( graph, true );
    }


    /**
     *  Creates a new <code>TrivialForestView</code> with the
     *  specified node- and edge-checking semantics.  If
     *  <code>isStrict</code> is <code>true</code>, passing in a node
     *  or edge into this view which is not contained in the
     *  underlying <code>Graph</code> will cause an exception to be
     *  thrown.
     */
    public TrivialOrientedForestView( Graph graph, boolean isStrict )
    {
        super();
        this.graph = graph;
        this.isStrict = isStrict;
    }


    ////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////


    private void checkNode( Object node )
    {
        if( isStrict && !graph.containsNode( node ) ) {
            throw new NoSuchNodeException( "Node is not in the graph: " + node );
        }
    }


    private void checkEdge( Graph.Edge edge )
    {
        if( isStrict && !graph.containsEdge( edge ) ) {
            throw new IllegalArgumentException( "Edge is not in the graph: " + edge );
        }
    }


    ////////////////////////////////////////
    // GraphView
    ////////////////////////////////////////


    public Graph getGraph()
    {
        return graph;
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    /**
     *  This implementation returns <code>null</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Object getParent( Object node )
    {
        checkNode( node );
        return null;
    }


    /**
     *  This implementation returns an empty <code>Traverser</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Traverser childTraverser( Object node )
    {
        checkNode( node );
        return GraphUtils.EMPTY_TRAVERSER;
    }


    /**
     *  This implementation returns <code>null</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Graph.Edge getParentEdge( Object node )
    {
        checkNode( node );
        return null;
    }


    /**
     *  This implementation returns <code>false</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public boolean isForestEdge( Graph.Edge edge )
    {
        checkEdge( edge );
        return false;
    }


    /**
     *  This implementation throws an
     *  <code>IllegalArgumentException</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Object getParentEndpoint( Graph.Edge edge )
    {
        throw new IllegalArgumentException( "Edge is not a forest edge: " + edge );
    }


    /**
     *  This implementation returns all nodes.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Collection rootNodes()
    {
        return graph.nodes( null );
    }


    /**
     *  This implementation returns the specified node.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Object getRoot( Object node )
    {
        checkNode( node );
        return node;
    }


    /**
     *  This implementation returns <code>true</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public boolean isLeaf( Object node )
    {
        checkNode( node );
        return true;
    }


    /**
     *  This implementation returns <code>true</code> if the specified
     *  nodes are equal, and <code>false</code> otherwise.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public boolean isAncestor( Object ancestor, Object descendant )
    {
        checkNode( ancestor );
        checkNode( descendant );
        return GraphUtils.equals( ancestor, descendant );
    }


    /**
     *  This implementation returns <code>aNode</code> if the
     *  specified nodes are equal, and <code>null</code> otherwise.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Object getLeastCommonAncestor( Object aNode, Object bNode )
    {
        checkNode( aNode );
        checkNode( bNode );
        return GraphUtils.equals( aNode, bNode )
            ? aNode
            : null;
    }


    /**
     *  This implementation returns <code>0</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public int getDepth( Object node )
    {
        checkNode( node );
        return 0;
    }


    /**
     *  This implementation returns <code>0</code>.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public int getHeight( Object node )
    {
        checkNode( node );
        return 0;
    }

}
