/*
 *  $Id: PruningTraverser.java,v 1.3 2005/10/03 15:16:31 rconner Exp $
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

import com.phoenixst.plexus.Traverser;


/**
 *  A <code>Traverser</code> which allows its traversal to be modified
 *  by pruning.
 *
 *  @version    $Revision: 1.3 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface PruningTraverser extends Traverser
{

    /**
     *  Signals this <code>Traverser</code> to not explore beyond the
     *  last node returned by {@link #next() next()}.  This method can
     *  be called only once per call to <code>next()</code>.  After
     *  calling this method (and before calling <code>next()</code>
     *  again), {@link #remove() remove()}, {@link #getEdge() getEdge()},
     *  and {@link #removeEdge() removeEdge()} will all throw
     *  <code>IllegalStateExceptions</code>.
     *
     *  @throws IllegalStateException if <code>next()</code> has not
     *  yet been called, or <code>remove()</code>,
     *  <code>removeEdge</code>, or <code>prune()</code> has been
     *  called after the last call to <code>next()</code>.
     *
     *  @throws UnsupportedOperationException if this method is not
     *  supported by this <code>Traverser</code>.
     */
    public void prune();

}
