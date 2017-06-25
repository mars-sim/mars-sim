/*
 *  $Id: Traverser.java,v 1.13 2005/10/03 15:24:00 rconner Exp $
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

import java.util.Iterator;


/**
 *  An interface for traversing through nodes in a {@link Graph}.  An
 *  edge is followed to reach every visited node, or at least most of
 *  them.  For example, the start node of a breadth first search is
 *  not reached through an edge.
 *
 *  <P>The {@link #hasNext() hasNext()} method is not consistent with
 *  that defined by {@link Iterator}.  Since {@link #remove() remove()}
 *  will generally also remove a number of edges as a side-effect,
 *  <code>hasNext()</code> may return <code>true</code> before a call
 *  to <code>remove()</code>, but return <code>false</code>
 *  afterwards.  Since the idiom is to call <code>hasNext()</code>
 *  just before calling <code>next()</code>, this is not normally an
 *  issue.
 *
 *  <P>Note that a traverser does not necessarily move from one node
 *  to the next, following a chain of edges; that is, it is not
 *  generally a linear path.
 *
 *  <P>In addition, unlike the typical node iterator, the nodes
 *  returned by {@link #next() next()} are not necessarily distinct.
 *  This is because a traverser follows edges according to some
 *  criteria, and a traversal may touch the same node more than once.
 *  This is trivially true in multigraphs and some traversals will
 *  reach the same node twice if the graph contains cycles.
 *
 *  @version    $Revision: 1.13 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface Traverser extends Iterator
{

    /**
     *  Returns the <code>Edge</code> which was traversed to get to
     *  the last node returned by {@link #next() next()}, or
     *  <code>null</code> if no <code>Edge</code> was traversed.  This
     *  call can be made only if {@link #remove() remove()} or {@link
     *  #removeEdge removeEdge()} has not been called after the last
     *  call to <code>next()</code>.
     *
     *  @return The <code>Edge</code> which was traversed to get to
     *  the last node returned by <code>next()</code>, or
     *  <code>null</code> if no <code>Edge</code> was traversed.
     *
     *  @throws IllegalStateException if <code>next()</code> has not
     *  yet been called, or <code>remove()</code> or
     *  <code>removeEdge()</code> has been called after the last call
     *  to <code>next()</code>.
     */
    public Graph.Edge getEdge();


    /**
     *  Removes from the underlying {@link Graph} the
     *  <code>Edge</code> that would be returned by {@link #getEdge()
     *  getEdge()} (optional operation).  If no <code>Edge</code> was
     *  traversed (as in the root of a breadth-first search), this
     *  method throws a <code>IllegalStateException</code>.  This
     *  method can be called only once per call to {@link #next()
     *  next()}.  The behavior of a traverser is unspecified if the
     *  underlying graph structure is modified while the traversal is
     *  in progress in any way other than by calling this method or
     *  {@link #remove() remove()}.
     *
     *  @throws IllegalStateException if <code>next()</code> has not
     *  yet been called, or <code>remove()</code> or
     *  <code>removeEdge</code> has been called after the last call to
     *  <code>next()</code>, or no <code>Edge</code> was traversed to
     *  reach the last node returned by <code>next()</code>.
     *
     *  @throws UnsupportedOperationException if this method is not
     *  supported by this <code>Traverser</code>.
     */
    public void removeEdge();

}
