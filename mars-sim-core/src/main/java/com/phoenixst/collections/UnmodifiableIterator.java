/*
 *  $Id: UnmodifiableIterator.java,v 1.7 2005/10/03 15:11:54 rconner Exp $
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

package com.phoenixst.collections;

import java.util.Iterator;


/**
 *  An unmodifiable view of an <code>Iterator</code>.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0.  No equivalent exists in version
 *  2.1.
 *
 *  @version    $Revision: 1.7 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class UnmodifiableIterator
    implements Iterator
{

    private final Iterator delegate;


    /**
     *  Creates a new <code>UnmodifiableIterator</code>.
     */
    public UnmodifiableIterator( Iterator delegate )
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

}
