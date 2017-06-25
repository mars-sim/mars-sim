/*
 *  $Id: EmptyGraph.java,v 1.25 2005/10/03 15:14:43 rconner Exp $
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

package com.phoenixst.plexus.examples;

import java.util.*;

import org.apache.commons.collections.Predicate;

import com.phoenixst.plexus.*;


/**
 *  A <code>Graph</code> containing a set of <code>Integer</code>
 *  nodes and no edges.
 *
 *  @version    $Revision: 1.25 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class EmptyGraph extends AbstractIntegerNodeGraph
{

    private static final long serialVersionUID = 2L;


    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Creates a new <code>EmptyGraph</code>.
     */
    public EmptyGraph( int n )
    {
        super( n );
    }


    ////////////////////////////////////////
    // Graph methods
    ////////////////////////////////////////


    /**
     *  Returns <code>false</code>.
     */
    public boolean containsEdge( Graph.Edge edge )
    {
        return false;
    }


    /**
     *  Returns <code>0</code>.
     */
    public int degree( Object node )
    {
        checkNode( node );
        return 0;
    }


    /**
     *  Returns <code>0</code>.
     */
    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        checkNode( node );
        return 0;
    }


    /**
     *  Returns an empty <code>Collection</code>.
     */
    public Collection edges( Predicate edgePredicate )
    {
        return Collections.EMPTY_SET;
    }


    /**
     *  Returns an empty <code>Collection</code>.
     */
    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        checkNode( node );
        return Collections.EMPTY_SET;
    }


    /**
     *  Returns an empty <code>Collection</code>.
     */
    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        checkNode( node );
        return Collections.EMPTY_SET;
    }


    /**
     *  Returns <code>null</code>.
     */
    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        return null;
    }


    /**
     *  Returns <code>null</code>.
     */
    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        checkNode( node );
        return null;
    }


    /**
     *  Returns <code>null</code>.
     */
    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        checkNode( node );
        return null;
    }


    /**
     *  Returns an empty <code>Traverser</code>.
     */
    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        checkNode( node );
        return GraphUtils.EMPTY_TRAVERSER;
    }


    ////////////////////////////////////////
    // Other methods
    ////////////////////////////////////////


    protected Graph.Edge createEdge( int tailIndex, int headIndex )
    {
        return null;
    }


    protected Collection createEdgeCollection()
    {
        return Collections.EMPTY_SET;
    }


    protected Traverser createTraverser( int nodeIndex )
    {
        return GraphUtils.EMPTY_TRAVERSER;
    }


    public String toString()
    {
        StringBuilder s = new StringBuilder();
        s.append( "Empty( " );
        s.append( getNodeSize() );
        s.append( " )" );
        return s.toString();
    }

}
