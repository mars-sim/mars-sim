/*
 *  $Id: GraphView.java,v 1.1 2004/01/14 20:46:56 rconner Exp $
 *
 *  Copyright (C) 1994-2004 by Phoenix Software Technologists,
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


/**
 *  Implementations of this interface represent a "view" of a {@link
 *  Graph}, a particular way of looking at it.  Rather than extending
 *  the <code>Graph</code> interface excessively (rooted graph,
 *  forest, tree, rooted tree, etc.), we will instead place any
 *  additional functionality those cases might provide into separate
 *  <code>GraphView</code> objects which may be applied to any graph
 *  implementation.  This allows a single <code>Graph</code> to be
 *  viewed in multiple ways without the overhead of a
 *  <code>GraphWrapper</code> implementation.
 *
 *  @version    $Revision: 1.1 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public interface GraphView
{

    /**
     *  Returns the <code>Graph</code> of which this is a view.
     */
    public Graph getGraph();

}
