/*
 *  $Id: IncidentEdgeCollection.java,v 1.7 2005/10/03 15:20:46 rconner Exp $
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

package com.phoenixst.plexus.util;

import java.util.*;

import org.apache.commons.collections.Predicate;

import com.phoenixst.collections.*;
import com.phoenixst.plexus.*;


/**
 *  A <code>Collection</code> for <code>Graph.Edges</code> to help
 *  implement the {@link Graph#incidentEdges Graph.incidentEdges(
 *  Predicate )} method.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class IncidentEdgeCollection extends AbstractCollection
{

    private final Graph graph;

    private final Object baseNode;

    private final Predicate traverserPredicate;

    private final OrderedPair pair = new OrderedPair( null, null );


    /**
     *  Creates a new <code>IncidentEdgeCollection</code>.  If a
     *  <code>null</code> <code>Predicate</code> is passed to this
     *  constructor, {@link TruePredicate} is used internally.
     */
    public IncidentEdgeCollection( Graph graph,
                                   Object baseNode,
                                   Predicate traverserPredicate )
    {
        super();
        this.graph = graph;
        this.baseNode = baseNode;
        this.traverserPredicate = (traverserPredicate != null)
            ? traverserPredicate
            : TruePredicate.INSTANCE;

        if( !graph.containsNode( baseNode ) ) {
            throw new IllegalArgumentException( "Graph does not contain base node: " + baseNode );
        }
        pair.setFirst( baseNode );
    }


    /**
     *  This implementation delegates to {@link
     *  Graph#degree(Object,Predicate) Graph.degree( node, predicate
     *  )}.
     */
    public int size()
    {
        return graph.degree( baseNode, traverserPredicate );
    }


    /**
     *  This implementation delegates to {@link Graph#removeEdge
     *  Graph.removeEdge( Object )} if the specified object passes the
     *  <code>Predicate</code> specified by the constructor and is
     *  incident upon the <code>baseNode</code>.
     */
    public boolean remove( Object object )
    {
        if( !(object instanceof Graph.Edge) ) {
            return false;
        }
        Graph.Edge edge = (Graph.Edge) object;
        pair.setSecond( edge );
        return
            ( GraphUtils.equals( baseNode, edge.getTail() )
              || GraphUtils.equals( baseNode, edge.getHead() ) )
            && traverserPredicate.evaluate( pair )
            && graph.removeEdge( edge );
    }


    /**
     *  This implementation delegates to {@link Graph#containsEdge
     *  Graph.containsEdge( Object )} if the specified object passes
     *  the <code>Predicate</code> specified by the constructor and is
     *  incident upon the <code>baseNode</code>.
     */
    public boolean contains( Object object )
    {
        if( !(object instanceof Graph.Edge) ) {
            return false;
        }
        Graph.Edge edge = (Graph.Edge) object;
        pair.setSecond( edge );
        return
            ( GraphUtils.equals( baseNode, edge.getTail() )
              || GraphUtils.equals( baseNode, edge.getHead() ) )
            && traverserPredicate.evaluate( pair )
            && graph.containsEdge( edge );
    }


    /**
     *  This implementation returns a wrapper around {@link
     *  Graph#traverser(Object,Predicate) Graph.traverser( node,
     *  predicate )}.
     */
    public Iterator iterator()
    {
        return new TraverserEdgeIteratorAdapter( graph.traverser( baseNode, traverserPredicate ) );
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean addAll( Collection collection )
    {
        throw new UnsupportedOperationException();
    }

}
