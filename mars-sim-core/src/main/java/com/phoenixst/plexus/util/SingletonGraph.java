/*
 *  $Id: SingletonGraph.java,v 1.22 2006/06/07 20:25:53 rconner Exp $
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

package com.phoenixst.plexus.util;

import java.util.*;

import org.apache.commons.collections.Predicate;

import com.phoenixst.plexus.*;


/**
 *  An unmodifiable <code>Graph</code> which contains a single node
 *  and no edges.
 *
 *  @version    $Revision: 1.22 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class SingletonGraph
    implements Graph,
               java.io.Serializable
{

    private static final long serialVersionUID = 1L;


    /**
     *  The node.
     *
     *  @serial
     */
    private final Object singleNode;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>SingletonGraph</code>.
     */
    public SingletonGraph( Object node )
    {
        super();
        singleNode = node;
    }


    ////////////////////////////////////////
    // Private check method
    ////////////////////////////////////////


    private void checkNode( Object node )
    {
        if( !GraphUtils.equals( singleNode, node ) ) {
            throw new NoSuchNodeException( "Node is not in this graph: " + node );
        }
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean addNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    public boolean containsNode( Object node )
    {
        return GraphUtils.equals( singleNode, node );
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public Graph.Edge addEdge( Object object,
                               Object tail,
                               Object head,
                               boolean isDirected )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean removeEdge( Graph.Edge edge )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  Returns <code>false</code>.
     */
    public boolean containsEdge( Graph.Edge edge )
    {
        return false;
    }


    public int degree( Object node )
    {
        checkNode( node );
        return 0;
    }


    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        checkNode( node );
        return 0;
    }


    public Collection nodes( Predicate nodePredicate )
    {
        return ( nodePredicate == null || nodePredicate.evaluate( singleNode ) )
            ? Collections.singleton( singleNode )
            : Collections.EMPTY_SET;
    }


    /**
     *  Returns an empty collection.
     */
    public Collection edges( Predicate edgePredicate )
    {
        return Collections.EMPTY_SET;
    }


    /**
     *  Returns an empty collection.
     */
    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        checkNode( node );
        return Collections.EMPTY_SET;
    }


    /**
     *  Returns an empty collection.
     */
    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        checkNode( node );
        return Collections.EMPTY_SET;
    }


    public Object getNode( Predicate nodePredicate )
    {
        return ( nodePredicate == null || nodePredicate.evaluate( singleNode ) )
            ? singleNode
            : null;
    }


    /**
     *  Returns <code>null</code>.
     */
    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        return null;
    }


    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        checkNode( node );
        return null;
    }


    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        checkNode( node );
        return null;
    }


    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        checkNode( node );
        return GraphUtils.EMPTY_TRAVERSER;
    }

}
