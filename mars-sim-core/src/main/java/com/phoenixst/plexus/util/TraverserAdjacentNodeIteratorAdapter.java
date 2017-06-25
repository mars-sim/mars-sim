/*
 *  $Id: TraverserAdjacentNodeIteratorAdapter.java,v 1.4 2005/10/03 15:20:46 rconner Exp $
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

import com.phoenixst.plexus.Traverser;


/**
 *  This class wraps a {@link Traverser}, presenting an
 *  <code>Iterator</code> over adjacent nodes.  This differs from a
 *  <code>TraverserNodeIteratorAdapter</code> in that {@link #remove}
 *  delegates to {@link Traverser#removeEdge}.
 *
 *  @version    $Revision: 1.4 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TraverserAdjacentNodeIteratorAdapter
    implements Iterator
{

    private final Traverser t;


    /**
     *  Creates a new <code>TraverserAdjacentNodeIteratorAdapter</code>.
     */
    public TraverserAdjacentNodeIteratorAdapter( Traverser t )
    {
        super();
        this.t = t;
    }


    public boolean hasNext()
    {
        return t.hasNext();
    }


    public Object next()
    {
        return t.next();
    }


    public void remove()
    {
        t.removeEdge();
    }

}
