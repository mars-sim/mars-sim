/*
 *  $Id: DefaultOrientedForestView.java,v 1.16 2006/06/07 21:08:23 rconner Exp $
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

package com.phoenixst.plexus;

import java.io.*;
import java.util.Collection;

import org.apache.commons.collections.Predicate;


/**
 *  A default view implementation of the <code>OrientedForest</code>
 *  interface.
 *
 *  @version    $Revision: 1.16 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class DefaultOrientedForestView extends AbstractOrientedForest
    implements GraphView,
               Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The Graph of which this is a view.
     *
     *  @serial
     */
    private final Graph graph;

    /**
     *  The parent Predicate.
     *
     *  @serial
     */
    private final Predicate parentPredicate;

    /**
     *  The child Predicate.
     *
     *  @serial
     */
    private final Predicate childPredicate;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>DefaultOrientedForestView</code>.
     */
    public DefaultOrientedForestView( Graph graph,
                                      Predicate parentPredicate,
                                      Predicate childPredicate )
    {
        super();
        this.graph = graph;
        this.parentPredicate = parentPredicate;
        this.childPredicate = childPredicate;
        if( graph == null ) {
            throw new IllegalArgumentException( "Delegate Graph is null." );
        }
    }


    ////////////////////////////////////////
    // Serialization methods
    ////////////////////////////////////////


    private void readObject( ObjectInputStream in )
        throws ClassNotFoundException,
               IOException
    {
        in.defaultReadObject();
        if( graph == null ) {
            throw new InvalidObjectException( "Graph is null." );
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


    public Object getParent( Object node )
    {
        return graph.getAdjacentNode( node, parentPredicate );
    }


    public Graph.Edge getParentEdge( Object node )
    {
        return graph.getIncidentEdge( node, parentPredicate );
    }


    public Traverser childTraverser( Object node )
    {
        return graph.traverser( node, childPredicate );
    }


    public Collection rootNodes()
    {
        return graph.nodes( new RootPredicate( graph, parentPredicate ) );
    }


    public boolean isLeaf( Object node )
    {
        return graph.getIncidentEdge( node, childPredicate ) == null;
    }


    ////////////////////////////////////////
    // Private classes
    ////////////////////////////////////////


    private static class RootPredicate
        implements Predicate
    {
        private final Graph graph;
        private final Predicate parentPredicate;

        RootPredicate( Graph graph, Predicate parentPredicate )
        {
            super();
            this.graph = graph;
            this.parentPredicate = parentPredicate;
        }

        public boolean evaluate( Object object )
        {
            return graph.getIncidentEdge( object, parentPredicate ) == null;
        }
    }

}
