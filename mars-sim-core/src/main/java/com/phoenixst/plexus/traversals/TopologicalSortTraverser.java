/*
 *  $Id: TopologicalSortTraverser.java,v 1.6 2005/10/03 15:16:31 rconner Exp $
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

package com.phoenixst.plexus.traversals;

import java.util.*;

import org.apache.commons.collections.*;

import com.phoenixst.collections.FilteredIterator;
import com.phoenixst.plexus.*;
import com.phoenixst.plexus.util.DefaultTraverserFactory;


/**
 *  A <code>Traverser</code> which returns nodes in a topologically
 *  sorted order.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TopologicalSortTraverser
    implements Traverser
{

    /**
     *  The Traverser factory to use to create new Traversers.
     */
    private final Transformer traverserFactory;

    /**
     *  The iterator over the entire node set.
     */
    private final Iterator nodeIter;

    /**
     *  The set of nodes that have been visited.
     */
    private final Set nodeSet = new HashSet();

    /**
     *  The current DepthFirstTraverser used to accomplish the
     *  topological sorting.
     */
    private DepthFirstTraverser t = null;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>TopologicalSortTraverser</code>, where the
     *  <code>traverserPredicate</code> specifies adjacent nodes which
     *  should be returned <em>before</em> a given node.
     */
    public TopologicalSortTraverser( Graph graph,
                                     Predicate traverserPredicate )
    {
        this( graph.nodes( null ).iterator(),
              new DefaultTraverserFactory( graph, traverserPredicate ) );
    }


    /**
     *  Creates a new <code>TopologicalSortTraverser</code>, where the
     *  <code>traverserFactory</code> returns a <code>Traverser</code>
     *  over adjacent nodes which should be returned <em>before</em>
     *  the given node.
     */
    public TopologicalSortTraverser( Graph graph,
                                     Transformer traverserFactory )
    {
        this( graph.nodes( null ).iterator(),
              traverserFactory );
    }


    /**
     *  Creates a new <code>TopologicalSortTraverser</code>, where the
     *  <code>traverserFactory</code> returns a <code>Traverser</code>
     *  over adjacent nodes which should be returned <em>before</em>
     *  the given node.
     */
    public TopologicalSortTraverser( Iterator nodeIter,
                                     Transformer traverserFactory )
    {
        super();
        this.traverserFactory = traverserFactory;
        if( nodeIter == null ) {
            throw new IllegalArgumentException( "Node Iterator is null." );
        }
        if( traverserFactory == null ) {
            throw new IllegalArgumentException( "Traverser Factory is null." );
        }
        this.nodeIter = new FilteredIterator( nodeIter, new NotPresentPredicate( nodeSet ) );
    }


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public boolean hasNext()
    {
        return (t != null && t.hasNext()) || nodeIter.hasNext();
    }


    public Object next()
    {
        if( t == null || !t.hasNext() ) {
            if( !nodeIter.hasNext() ) {
                throw new NoSuchElementException();
            }
            t = new DepthFirstTraverser( nodeIter.next(), traverserFactory );
        }
        Object node = t.next();
        while( t.isDescending() ) {
            if( !nodeSet.add( node ) ) {
                t.prune();
            }
            node = t.next();
        }
        return node;
    }


    public void remove()
    {
        if( t == null ) {
            throw new IllegalStateException();
        }
        t.remove();
    }


    /**
     *  Returns the <code>Edge</code> which was traversed to get to
     *  the last node returned by {@link #next next()}, or
     *  <code>null</code> if no <code>Edge</code> was traversed.  Note
     *  that because the nodes of the underlying <code>Graph</code>
     *  are visited in no particular order by the internals of this
     *  algorithm, this method may return <code>null</code> even if
     *  some other adjacent node succeeds this one.
     *
     *  <P><b>Description copied from interface: {@link
     *  Traverser}</b><br> {@inheritDoc}
     */
    public Graph.Edge getEdge()
    {
        if( t == null ) {
            throw new IllegalStateException();
        }
        return t.getEdge();
    }


    public void removeEdge()
    {
        if( t == null ) {
            throw new IllegalStateException();
        }
        t.removeEdge();
    }


    ////////////////////////////////////////
    // Private class
    ////////////////////////////////////////


    private static class NotPresentPredicate
        implements Predicate
    {
        private final Collection collection;

        NotPresentPredicate( Collection collection )
        {
            super();
            this.collection = collection;
        }

        public boolean evaluate( Object object )
        {
            return !collection.contains( object );
        }
    }

}
