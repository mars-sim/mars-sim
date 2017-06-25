/*
 *  $Id: DepthFirstForestView.java,v 1.15 2005/10/03 15:12:36 rconner Exp $
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
 *  portion of a <code>Graph</code>.
 *
 *  <P>This implementation tracks discovery time and finishing time,
 *  and can possibly answer a few structural questions about the
 *  underlying <code>Graph</code>.  Whether or not these questions can
 *  be answered depends upon whether the supplied
 *  <code>Traverser</code> predicate or factory is <em>direction
 *  agnostic</em>.  If at least one encountered edge can be traversed
 *  in only one direction, then many structural queries cannot be
 *  answered by this class, and will throw exceptions.  The only
 *  exception is in the case of self-loops; these may only be
 *  traversed in one direction with no ill effect.  These cases are
 *  documented in the appropriate methods.
 *
 *  <P>If the underlying <code>Graph</code> changes, this view may
 *  become invalid, but perhaps not detectably so.
 *
 *  @version    $Revision: 1.15 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DepthFirstForestView extends AbstractDepthFirstForestView
{

    /**
     *  The Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( DepthFirstForestView.class );

    /**
     *  The start (root) nodes for this depth-first forest.
     */
    private final List roots;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DepthFirstForestView</code>.
     */
    public DepthFirstForestView( Graph graph,
                                 Predicate traverserPredicate )
    {
        this( graph,
              new DefaultTraverserFactory( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new <code>DepthFirstForestView</code>.
     */
    public DepthFirstForestView( Graph graph,
                                 Transformer traverserFactory )
    {
        super( graph, traverserFactory, LOGGER );

        // Iterate over nodes, selecting roots from new ones.
        List rootList = new ArrayList();
        int time = 0;
        for( Iterator i = graph.nodes( null ).iterator(); i.hasNext(); ) {
            Object node = i.next();
            if( !hasProcessedNode( node ) ) {
                rootList.add( node );
                time = visitTree( node, time );
            }
        }

        // Create the list of roots which the user sees.
        roots = Collections.unmodifiableList( rootList );
    }


    ////////////////////////////////////////
    // OrientedForest
    ////////////////////////////////////////


    /**
     *  Returns a list of the root nodes for this depth-first
     *  traversal in the order encountered.
     *
     *  <P><b>Description copied from interface: {@link
     *  OrientedForest}</b><br> {@inheritDoc}
     */
    public Collection rootNodes()
    {
        return roots;
    }

}
