/*
 *  $Id: DepthFirstTreeView.java,v 1.21 2005/10/03 15:12:36 rconner Exp $
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

package com.phoenixst.plexus.algorithms;

import java.util.*;

import org.apache.commons.collections.*;
import org.apache.log4j.Logger;

import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.DefaultTraverserFactory;


/**
 *  A constructive (<strong>not</strong> lazy) depth-first tree for a
 *  portion of a <code>Graph</code>.  As described in the {@link
 *  RootedTree} class docs, all methods in this view which take a
 *  node argument will throw a <code>NoSuchNodeException</code> if
 *  given a node which is not a descendant of the root node.
 *
 *  <P>This implementation tracks discovery time and finishing time,
 *  and can possibly answer a few structural questions about the
 *  portion of the underlying <code>Graph</code> reachable from the
 *  specified start node.  Whether or not these questions can be
 *  answered depends upon whether the supplied <code>Traverser</code>
 *  predicate or factory is <em>direction agnostic</em>.  If at least
 *  one encountered edge can be traversed in only one direction, then
 *  many structural queries cannot be answered by this class, and will
 *  throw exceptions.  The only exception is in the case of
 *  self-loops; these may only be traversed in one direction with no
 *  ill effect.  These cases are documented in the appropriate
 *  methods.
 *
 *  <P>If the underlying <code>Graph</code> changes, this view may
 *  become invalid, but perhaps not detectably so.
 *
 *  @version    $Revision: 1.21 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DepthFirstTreeView extends AbstractDepthFirstForestView
    implements RootedTree
{

    /**
     *  The Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( DepthFirstTreeView.class );

    /**
     *  The start (root) node for this depth-first tree.
     */
    private final Object startNode;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DepthFirstTreeView</code> starting at
     *  the specified node.
     */
    public DepthFirstTreeView( Object startNode,
                               Graph graph,
                               Predicate traverserPredicate )
    {
        this( startNode,
              graph,
              new DefaultTraverserFactory( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new <code>DepthFirstTreeView</code> starting at
     *  the specified node.
     */
    public DepthFirstTreeView( Object startNode,
                               Graph graph,
                               Transformer traverserFactory )
    {
        super( graph, traverserFactory, LOGGER );
        this.startNode = startNode;
        visitTree( startNode, 0 );
    }


    ////////////////////////////////////////
    // Rooted
    ////////////////////////////////////////


    public Object getRoot()
    {
        return startNode;
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public void setRoot( Object root )
    {
        throw new UnsupportedOperationException();
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    public Collection rootNodes()
    {
        return Collections.singleton( startNode );
    }


    public Object getRoot( Object node )
    {
        if( !hasProcessedNode( node ) ) {
            throw new NoSuchNodeException( NODE_NOT_PRESENT_MESSAGE + node );
        }
        return startNode;
    }


    ////////////////////////////////////////
    // RootedTree
    ////////////////////////////////////////


    public boolean isTreeNode( Object node )
    {
        return hasProcessedNode( node );
    }

}
