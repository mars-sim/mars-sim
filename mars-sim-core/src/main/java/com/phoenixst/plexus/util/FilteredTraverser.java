/*
 *  $Id: FilteredTraverser.java,v 1.19 2005/10/03 15:20:46 rconner Exp $
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

import java.util.NoSuchElementException;

import org.apache.commons.collections.Predicate;

import com.phoenixst.collections.OrderedPair;
import com.phoenixst.plexus.*;


/**
 *  A simple filtered <code>Traverser</code>.  Because this class must
 *  advance the underlying <code>Traverser</code> to function
 *  properly, {@link #remove} and {@link #removeEdge} may delegate to
 *  {@link Graph#removeNode Graph.removeNode( node )} and {@link
 *  Graph#removeEdge Graph.removeEdge( edge )} in some situations.
 *
 *  @version    $Revision: 1.19 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class FilteredTraverser
    implements Traverser
{

    private final Graph graph;
    private final Traverser delegate;
    private final Predicate traverserPredicate;

    private Object currentNode = null;
    private Object nextNode = null;
    private Graph.Edge currentEdge = null;
    private Graph.Edge nextEdge = null;

    private boolean isCurrentValid = false;
    private boolean isNextValid = false;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>FilteredTraverser</code> which will throw
     *  an <code>IllegalStateException</code> if <code>remove()</code>
     *  or <code>removeEdge()</code> is called after
     *  <code>hasNext()</code> without an intervening call to
     *  <code>next()</code>.
     */
    public FilteredTraverser( Traverser delegate,
                              Predicate traverserPredicate )
    {
        this( null, delegate, traverserPredicate );
    }


    /**
     *  Creates a new <code>FilteredTraverser</code> which will have
     *  {@link #remove()} and {@link #removeEdge()} delegate to {@link
     *  Graph#removeNode Graph.removeNode( node )} and {@link
     *  Graph#removeEdge Graph.removeEdge( edge )} if necessary.
     *  Depending upon the <code>Graph</code> implementation, this may
     *  invalidate this <code>Traverser</code>.
     */
    public FilteredTraverser( Graph graph,
                              Traverser delegate,
                              Predicate traverserPredicate )
    {
        super();
        this.graph = graph;
        this.delegate = delegate;
        this.traverserPredicate = traverserPredicate;
    }


    ////////////////////////////////////////
    // Traverser
    ////////////////////////////////////////


    public boolean hasNext()
    {
        if( !isNextValid ) {
            OrderedPair pair = new OrderedPair();
            while( delegate.hasNext() ) {
                Object node = delegate.next();
                Graph.Edge edge = delegate.getEdge();
                pair.setFirst( edge.getOtherEndpoint( node ) );
                pair.setSecond( edge );
                if( traverserPredicate.evaluate( pair ) ) {
                    nextNode = node;
                    nextEdge = edge;
                    isNextValid = true;
                    break;
                }
            }
        }
        return isNextValid;
    }


    public Object next()
    {
        if( !hasNext() ) {
            throw new NoSuchElementException();
        }
        currentNode = nextNode;
        currentEdge = nextEdge;
        isCurrentValid = true;
        isNextValid = false;
        return currentNode;
    }


    public void remove()
    {
        if( !isCurrentValid ) {
            throw new IllegalStateException();
        }
        if( !isNextValid ) {
            delegate.remove();
        } else {
            if( graph == null ) {
                throw new IllegalStateException( "The remove() method cannot be called after hasNext()"
                                                 + " without an intervening call to next()." );
            }
            graph.removeNode( currentNode );
        }
        isCurrentValid = false;
    }


    public Graph.Edge getEdge()
    {
        if( !isCurrentValid ) {
            throw new IllegalStateException();
        }
        return currentEdge;
    }


    public void removeEdge()
    {
        if( !isCurrentValid ) {
            throw new IllegalStateException();
        }
        if( !isNextValid ) {
            delegate.removeEdge();
        } else {
            if( graph == null ) {
                throw new IllegalStateException( "The removeEdge() method cannot be called after hasNext()"
                                                 + " without an intervening call to next()." );
            }
            graph.removeEdge( currentEdge );
        }
        isCurrentValid = false;
    }

}
