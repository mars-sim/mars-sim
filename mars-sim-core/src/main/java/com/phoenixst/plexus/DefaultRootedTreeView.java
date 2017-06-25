/*
 *  $Id: DefaultRootedTreeView.java,v 1.18 2005/10/03 15:24:00 rconner Exp $
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

import org.apache.commons.collections.Predicate;


/**
 *  A default view implementation of the <code>RootedTree</code>
 *  interface.
 *
 *  @version    $Revision: 1.18 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultRootedTreeView extends DefaultOrientedForestView
    implements RootedTree
{

    private static final long serialVersionUID = 2L;

    /**
     *  The root node.
     *
     *  @serial
     */
    private Object root = null;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultRootedTreeView</code>.
     */
    public DefaultRootedTreeView( Graph graph,
                                  Predicate parentPredicate,
                                  Predicate childPredicate )
    {
        super( graph, parentPredicate, childPredicate );
    }


    /**
     *  Creates a new <code>DefaultRootedTreeView</code>.
     */
    public DefaultRootedTreeView( Graph graph,
                                  Object root,
                                  Predicate parentPredicate,
                                  Predicate childPredicate )
    {
        super( graph, parentPredicate, childPredicate );
        this.root = root;
        if( !graph.containsNode( root ) ) {
            throw new NoSuchNodeException( "Root node is not in the graph: " + root );
        }
    }


    ////////////////////////////////////////
    // RootedTree
    ////////////////////////////////////////


    public boolean isTreeNode( Object node )
    {
        Graph.Edge edge = getParentEdge( node );
        while( edge != null ) {
            node = edge.getOtherEndpoint( node );
            edge = getParentEdge( node );
        }
        return GraphUtils.equals( root, node );
    }


    ////////////////////////////////////////
    // Rooted
    ////////////////////////////////////////


    /**
     *  Gets the root node.
     */
    public Object getRoot()
    {
        return root;
    }


    /**
     *  Sets the root node, which must already be present in the
     *  <code>Graph</code>.
     */
    public void setRoot( Object root )
    {
        if( !getGraph().containsNode( root ) ) {
            throw new NoSuchNodeException( "Root node is not in the graph: " + root );
        }
        this.root = root;
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    public Collection rootNodes()
    {
        if( !getGraph().containsNode( root ) ) {
            return Collections.EMPTY_SET;
        }
        return Collections.singleton( root );
    }


    public Object getRoot( Object node )
    {
        if( !isTreeNode( node ) ) {
            throw new NoSuchNodeException( "Node is not in this tree: " + node );
        }
        return root;
    }


    /**
     *  Gets the depth of the specified node.  If the specified node
     *  is not a descendant of the root node, this method will throw a
     *  <code>NoSuchNodeException</code>.
     */
    public int getDepth( Object node )
    {
        int depth = 0;
        Graph.Edge edge = getParentEdge( node );
        while( edge != null ) {
            node = edge.getOtherEndpoint( node );
            edge = getParentEdge( node );
            depth++;
        }
        if( !GraphUtils.equals( root, node ) ) {
            throw new NoSuchNodeException( "Node is not in this tree: " + node );
        }
        return depth;
    }

}
