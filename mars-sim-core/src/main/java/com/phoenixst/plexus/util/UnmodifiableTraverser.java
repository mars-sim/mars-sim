/*
 *  $Id: UnmodifiableTraverser.java,v 1.5 2005/10/03 15:20:46 rconner Exp $
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

import com.phoenixst.plexus.*;


/**
 *  An unmodifiable view of a <code>Traverser</code>.
 *
 *  @version    $Revision: 1.5 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class UnmodifiableTraverser
    implements Traverser
{

    private final Traverser delegate;


    /**
     *  Creates a new <code>UnmodifiableTraverser</code>.
     */
    public UnmodifiableTraverser( Traverser delegate )
    {
        super();
        this.delegate = delegate;
    }


    public boolean hasNext()
    {
        return delegate.hasNext();
    }


    public Object next()
    {
        return delegate.next();
    }


    public void remove()
    {
        throw new UnsupportedOperationException();
    }


    public Graph.Edge getEdge()
    {
        return delegate.getEdge();
    }


    public void removeEdge()
    {
        throw new UnsupportedOperationException();
    }

}
