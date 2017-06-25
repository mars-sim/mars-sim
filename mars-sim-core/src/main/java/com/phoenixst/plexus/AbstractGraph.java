/*
 *  $Id: AbstractGraph.java,v 1.59 2005/10/03 15:24:00 rconner Exp $
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

package com.phoenixst.plexus;

import java.util.*;

import org.apache.commons.collections.Predicate;

import com.phoenixst.collections.*;
import com.phoenixst.plexus.util.*;


/**
 *  This class provides a skeletal implementation of the
 *  <code>Graph</code> interface, to minimize the effort required to
 *  implement this interface.  Any concrete extension of this class
 *  must override the following methods to provide a full
 *  implemnetation:
 *
 *  <UL>
 *    <LI>{@link #nodes() nodes()}
 *    <LI>{@link #edges() edges()}
 *    <LI>{@link #traverser(Object) traverser( node )}
 *  </UL>
 *
 *  <P>Alternately, an extension may override one or more of the
 *  following methods (which normally defer to those listed above) if
 *  doing so admits a more efficient solution.  In this case, the
 *  above methods should probably still be implemented correctly.
 *
 *  <UL>
 *    <LI>{@link #nodes(Predicate) nodes( nodePredicate )}
 *    <LI>{@link #edges(Predicate) edges( edgePredicate )}
 *    <LI>{@link #traverser(Object,Predicate) traverser( node, traverserPredicate )}
 *  </UL>
 *
 *  <P>Any modifiable concrete extensions of this class must also
 *  implement:
 *
 *  <UL>
 *    <LI>{@link #addNode(Object) addNode( node )}
 *    <LI>{@link #addEdge(Object,Object,Object,boolean) addEdge( object, tail, head, isDirected )}
 *    <LI>Modifying operations of the collection views and traversers
 *  </UL>
 *
 *  <P>The documentation for each non-abstract method in this class
 *  describes its implementation in detail. Each of these methods may
 *  be overridden if the graph being implemented admits a more
 *  efficient implementation.  Note that almost every method
 *  implementation here is written in terms of an iterator over the
 *  structure of this graph; these methods are all rather inefficient.
 *
 *  <P>The programmer should generally provide a void (no argument) and
 *  <code>Graph</code> constructor, as per the recommendation in the
 *  <code>Graph</code> interface specification.
 *
 *  @version    $Revision: 1.59 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public abstract class AbstractGraph
    implements Graph
{

    ////////////////////////////////////////
    // Constructor
    ////////////////////////////////////////


    /**
     *  Protected constructor, called implicitly by subclasses.
     */
    protected AbstractGraph()
    {
        super();
    }


    ////////////////////////////////////////
    // Methods to be overridden by subclasses.
    ////////////////////////////////////////


    /**
     *  Returns a <code>Collection</code> view of all the nodes
     *  in this <code>Graph</code>.  This method is only called
     *  by {@link #nodes(Predicate) nodes( Predicate )}.
     */
    protected abstract Collection nodes();


    /**
     *  Returns a <code>Collection</code> view of all the
     *  <code>Graph.Edges</code> in this <code>Graph</code>.
     *  This method is only called by {@link #edges(Predicate)
     *  edges( Predicate )}.
     */
    protected abstract Collection edges();


    /**
     *  Returns an unfiltered <code>Traverser</code> over those
     *  <code>Graph.Edges</code> incident to the specified node.
     *  This method is only called by {@link
     *  #traverser(Object,Predicate) traverser( node, Predicate )}.
     */
    protected abstract Traverser traverser( Object node );


    ////////////////////////////////////////
    // Graph
    ////////////////////////////////////////


    /**
     *  This implementation throws an
     *  <code>UnsupportedOperationException</code>.
     */
    public boolean addNode( Object node )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  This implementation iterates over the nodes in this graph
     *  looking for the specified element.  If it finds the element,
     *  it removes the element using using the {@link
     *  Iterator#remove()} operation.
     *
     *  <P>Note that this implementation will throw an
     *  <code>UnsupportedOperationException</code> if the iterator
     *  returned by this graph's <code>nodes( null ).iterator()</code>
     *  method does not implement the <code>remove</code> method and
     *  this graph contains the specified node.
     */
    public boolean removeNode( Object node )
    {
        if( node == null ) {
            for( Iterator i = nodes( null ).iterator(); i.hasNext(); ) {
                if( i.next() == null ) {
                    i.remove();
                    return true;
                }
            }
        } else {
            for( Iterator i = nodes( null ).iterator(); i.hasNext(); ) {
                if( node.equals( i.next() ) ) {
                    i.remove();
                    return true;
                }
            }
        }
        return false;
    }


    /**
     *  This implementation iterates over the nodes in this graph
     *  looking for the specified element.
     */
    public boolean containsNode( Object node )
    {
        if( node == null ) {
            for( Iterator i = nodes( null ).iterator(); i.hasNext(); ) {
                if( i.next() == null ) {
                    return true;
                }
            }
        } else {
            for( Iterator i = nodes( null ).iterator(); i.hasNext(); ) {
                if( node.equals( i.next() ) ) {
                    return true;
                }
            }
        }
        return false;

    }


    /**
     *  This implementation throws an
     *  <code>UnsupportedOperationException</code>.
     */
    public Graph.Edge addEdge( Object object, Object tail, Object head, boolean isDirected )
    {
        throw new UnsupportedOperationException();
    }


    /**
     *  This implementation traverses over the edges in this graph
     *  incident on the tail of the specified <code>edge</code>.  If
     *  it finds the element, it removes the element using using the
     *  {@link Traverser#remove()} operation.
     *
     *  <P>Note that this implementation will throw an
     *  <code>UnsupportedOperationException</code> if the traverser
     *  returned by this graph's {@link #traverser(Object,Predicate)
     *  traverser( node, predicate )} method does not implement the
     *  <code>removeEdge</code> method and this graph contains the
     *  specified edge.
     */
    public boolean removeEdge( Graph.Edge edge )
    {
        if( edge != null ) {
            Object tail = edge.getTail();
            if( !containsNode( tail ) ) {
                return false;
            }
            for( Traverser t = traverser( tail, null ); t.hasNext(); ) {
                t.next();
                if( edge.equals( t.getEdge() ) ) {
                    t.removeEdge();
                    return true;
                }
            }
        }
        return false;
    }


    /**
     *  This implementation traverses over the edges in this graph
     *  incident on the tail of the specified <code>edge</code>,
     *  looking for it and returning <code>true</code> if found.
     */
    public boolean containsEdge( Graph.Edge edge )
    {
        if( edge != null ) {
            Object tail = edge.getTail();
            if( !containsNode( tail ) ) {
                return false;
            }
            for( Traverser t = traverser( tail, null ); t.hasNext(); ) {
                t.next();
                if( edge.equals( t.getEdge() ) ) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     *  This implementation counts the number of elements accessed by
     *  this graph's {@link #traverser(Object,Predicate) traverser(
     *  node, null )} method, counting self-loops twice.
     */
    public int degree( Object node )
    {
        int count = 0;
        for( Traverser t = traverser( node, null ); t.hasNext(); ) {
            Object adjNode = t.next();
            if( (node == null) ? (adjNode == null) : node.equals( adjNode ) ) {
                count++;
            }
            count++;
        }
        return count;
    }


    /**
     *  This implementation counts the number of elements accessed by
     *  this graph's {@link #traverser(Object,Predicate) traverser(
     *  node, traverserPredicate )} method, without counting
     *  self-loops twice.
     */
    public int degree( Object node,
                       Predicate traverserPredicate )
    {
        int count = 0;
        for( Traverser t = traverser( node, traverserPredicate ); t.hasNext(); ) {
            t.next();
            count++;
        }
        return count;
    }


    /**
     *  This implementation delegates to {@link #nodes() nodes()},
     *  except for when the specified <code>nodePredicate</code> is
     *  either {@link FalsePredicate#INSTANCE} or an instance of
     *  {@link EqualPredicate}.  These two cases are optimized.
     */
    public Collection nodes( Predicate nodePredicate )
    {
        if( nodePredicate == null || nodePredicate == TruePredicate.INSTANCE ) {
            return nodes();
        } else if( nodePredicate == FalsePredicate.INSTANCE ) {
            return Collections.EMPTY_SET;
        } else if( nodePredicate instanceof EqualPredicate ) {
            Object testNode = ((EqualPredicate) nodePredicate).getTestObject();
            if( !containsNode( testNode ) ) {
                return Collections.EMPTY_SET;
            }
            return new SingletonNodeCollection( this, testNode );
        } else {
            return new FilteredCollection( nodes(), nodePredicate );
        }
    }


    /**
     *  This implementation delegates to {@link #edges() edges()},
     *  except for when the specified <code>edgePredicate</code> is
     *  either {@link FalsePredicate#INSTANCE} or an instance of
     *  {@link EqualPredicate}.  These two cases are optimized.
     */
    public Collection edges( Predicate edgePredicate )
    {
        if( edgePredicate == null || edgePredicate == TruePredicate.INSTANCE ) {
            return edges();
        } else if( edgePredicate == FalsePredicate.INSTANCE ) {
            return Collections.EMPTY_SET;
        } else if( edgePredicate instanceof EqualPredicate ) {
            Graph.Edge testEdge = (Graph.Edge) ((EqualPredicate) edgePredicate).getTestObject();
            if( !containsEdge( testEdge ) ) {
                return Collections.EMPTY_SET;
            }
            return new SingletonEdgeCollection( this, testEdge );
        } else {
            return new FilteredCollection( edges(), edgePredicate );
        }
    }


    /**
     *  This implementation returns a new {@link
     *  AdjacentNodeCollection}.
     */
    public Collection adjacentNodes( Object node,
                                     Predicate traverserPredicate )
    {
        return new AdjacentNodeCollection( this, node, traverserPredicate );
    }


    /**
     *  This implementation returns a new {@link
     *  IncidentEdgeCollection}.
     */
    public Collection incidentEdges( Object node,
                                     Predicate traverserPredicate )
    {
        return new IncidentEdgeCollection( this, node, traverserPredicate );
    }


    /**
     *  This implementation returns the first node accessed by {@link
     *  #nodes(Predicate)} if present, otherwise it returns
     *  <code>null</code>.
     */
    public Object getNode( Predicate nodePredicate )
    {
        Iterator i = nodes( nodePredicate ).iterator();
        return i.hasNext()
            ? i.next()
            : null;
    }


    /**
     *  This implementation returns the first <code>Edge</code>
     *  accessed by {@link #edges(Predicate)} if present, otherwise it
     *  returns <code>null</code>.
     */
    public Graph.Edge getEdge( Predicate edgePredicate )
    {
        Iterator i = edges( edgePredicate ).iterator();
        return i.hasNext()
            ? (Graph.Edge) i.next()
            : null;
    }


    /**
     *  This implementation returns the other endpoint of the
     *  <code>Edge</code> returned by {@link
     *  #getIncidentEdge(Object,Predicate)} if present, otherwise it
     *  returns <code>null</code>.
     */
    public Object getAdjacentNode( Object node,
                                   Predicate traverserPredicate )
    {
        Graph.Edge edge = getIncidentEdge( node, traverserPredicate );
        return (edge != null)
            ? edge.getOtherEndpoint( node )
            : null;
    }


    /**
     *  This implementation returns the first <code>Edge</code>
     *  accessed by {@link #incidentEdges(Object,Predicate)} if
     *  present, otherwise it returns <code>null</code>.
     */
    public Graph.Edge getIncidentEdge( Object node,
                                       Predicate traverserPredicate )
    {
        Traverser t = traverser( node, traverserPredicate );
        if( !t.hasNext() ) {
            return null;
        }
        t.next();
        return t.getEdge();
    }


    /**
     *  This implementation delegates to {@link #traverser(Object)
     *  traverser( node )}, except for when the specified
     *  <code>traverserPredicate</code> is either {@link
     *  FalsePredicate#INSTANCE} or an instance of {@link
     *  EqualsTraverserPredicate}.  These two cases are optimized.
     */
    public Traverser traverser( Object node,
                                Predicate traverserPredicate )
    {
        if( traverserPredicate == null || traverserPredicate == TruePredicate.INSTANCE ) {
            return traverser( node );
        } else if( traverserPredicate == FalsePredicate.INSTANCE ) {
            return GraphUtils.EMPTY_TRAVERSER;
        } else if( traverserPredicate instanceof EqualsTraverserPredicate ) {
            Graph.Edge testEdge = ((EqualsTraverserPredicate) traverserPredicate).getTestEdge();
            return ( ( GraphUtils.equals( node, testEdge.getTail() )
                       || GraphUtils.equals( node, testEdge.getHead() ) )
                     && containsEdge( testEdge ) )
                ? new SingletonTraverser( this, testEdge.getOtherEndpoint( node ), testEdge )
                : GraphUtils.EMPTY_TRAVERSER;
        } else {
            return new FilteredTraverser( traverser( node ), traverserPredicate );
        }
    }

}
