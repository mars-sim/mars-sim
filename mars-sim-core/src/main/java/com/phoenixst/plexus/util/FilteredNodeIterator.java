/*
 *  $Id: FilteredNodeIterator.java,v 1.8 2005/10/03 15:20:46 rconner Exp $
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

import java.util.Iterator;

import org.apache.commons.collections.Predicate;

import com.phoenixst.collections.FilteredIterator;
import com.phoenixst.plexus.Graph;


/**
 *  A simple filtered node <code>Iterator</code>.  Because this class
 *  must advance the underlying <code>Iterator</code> to function
 *  properly, implementing {@link #remove} may delegate to {@link
 *  Graph#removeNode Graph.removeNode( node )} in some situations.
 *
 *  @version    $Revision: 1.8 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class FilteredNodeIterator extends FilteredIterator
{

    private final Graph graph;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>FilteredNodeIterator</code> which will
     *  throw an <code>IllegalStateException</code> if
     *  <code>remove()</code> is called after <code>hasNext()</code>
     *  without an intervening call to <code>next()</code>.
     */
    public FilteredNodeIterator( Iterator nodeIter,
                                 Predicate nodePredicate )
    {
        this( null, nodeIter, nodePredicate );
    }


    /**
     *  Creates a new <code>FilteredNodeIterator</code> which will
     *  have {@link #remove()} delegate to {@link
     *  Graph#removeNode(Object) Graph.removeNode( node )} if
     *  necessary.  Depending upon the <code>Graph</code>
     *  implementation, this may invalidate this
     *  <code>Iterator</code>.
     */
    public FilteredNodeIterator( Graph graph,
                                 Iterator nodeIter,
                                 Predicate nodePredicate )
    {
        super( nodeIter, nodePredicate );
        this.graph = graph;
    }


    ////////////////////////////////////////
    // FilteredIterator
    ////////////////////////////////////////


    /**
     *  If the <code>Graph</code> specified in the constructor is not
     *  <code>null</code>, this implementation will delegate to {@link
     *  Graph#removeNode(Object) Graph.removeNode( node )} (which may
     *  invalidate this <code>Iterator</code>).  If the
     *  <code>Graph</code> specified in the constructor is
     *  <code>null</code>, or if the other constructor is used, this
     *  implementation throws an <code>IllegalStateException</code>.
     *
     *  <P><b>Description copied from class: {@link
     *  FilteredIterator}</b><br> {@inheritDoc}
     */
    protected void remove( Object object )
    {
        if( graph == null ) {
            super.remove( object );
        }
        graph.removeNode( object );
    }

}
