/*
 *  $Id: TraverserEdgeIteratorAdapter.java,v 1.6 2005/10/03 15:20:46 rconner Exp $
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
 *  <code>Iterator</code> over the accessed <code>Edges</code>.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class TraverserEdgeIteratorAdapter
    implements Iterator
{

    private final Traverser t;


    /**
     *  Creates a new <code>TraverserEdgeIteratorAdapter</code>.
     */
    public TraverserEdgeIteratorAdapter( Traverser t )
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
        t.next();
        return t.getEdge();
    }


    public void remove()
    {
        t.removeEdge();
    }

}
