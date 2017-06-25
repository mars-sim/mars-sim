/*
 *  $Id: AdjacentNodeCollection.java,v 1.7 2005/10/03 15:20:46 rconner Exp $
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

import com.phoenixst.collections.TruePredicate;
import com.phoenixst.plexus.*;


/**
 *  A <code>Collection</code> for nodes to help implement the {@link
 *  Graph#adjacentNodes Graph.adjacentNodes( Predicate )} method.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class AdjacentNodeCollection extends AbstractCollection
{

    private final Graph graph;

    private final Object baseNode;

    private final Predicate traverserPredicate;


    /**
     *  Creates a new <code>AdjacentNodeCollection</code>.  If a
     *  <code>null</code> <code>Predicate</code> is passed to this
     *  constructor, {@link TruePredicate} is used internally.
     */
    public AdjacentNodeCollection( Graph graph,
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
     *  This implementation uses {@link
     *  Graph#traverser(Object,Predicate) Graph.traverser( node,
     *  predicate )} and removes the <code>Graph.Edge</code> to the
     *  node, if found.
     */
    public boolean remove( Object object )
    {
        for( Traverser t = graph.traverser( baseNode, traverserPredicate ); t.hasNext(); ) {
            if( GraphUtils.equals( object, t.next() ) ) {
                t.removeEdge();
                return true;
            }
        }
        return false;
    }


    /**
     *  This implementation uses {@link
     *  Graph#traverser(Object,Predicate) Graph.traverser( node,
     *  predicate )} and returns <code>true</code>, if found.
     */
    public boolean contains( Object object )
    {
        for( Iterator i = graph.traverser( baseNode, traverserPredicate ); i.hasNext(); ) {
            if( GraphUtils.equals( object, i.next() ) ) {
                return true;
            }
        }
        return false;
    }


    /**
     *  This implementation returns a wrapper around {@link
     *  Graph#traverser(Object,Predicate) Graph.traverser( node,
     *  predicate )}.
     */
    public Iterator iterator()
    {
        return new TraverserAdjacentNodeIteratorAdapter( graph.traverser( baseNode, traverserPredicate ) );
    }


    /**
     *  Throws an <code>UnsupportedOperationException</code>.
     */
    public boolean addAll( Collection collection )
    {
        throw new UnsupportedOperationException();
    }

}
