/*
 *  $Id: AbstractUnmodifiableCollection.java,v 1.6 2005/10/03 15:11:54 rconner Exp $
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

import java.util.*;


/**
 *  An extension of {@link AbstractCollection} in which all modifying
 *  operations explicitly throw exceptions.
 *
 *  @version    $Revision: 1.6 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public abstract class AbstractUnmodifiableCollection extends AbstractCollection
{

    /**
     *  Protected constructor, called implicitly by subclasses.
     */
    protected AbstractUnmodifiableCollection()
    {
        super();
    }


    /**
     *  This implementation counts the number of elements accessed by
     *  the <code>Iterator</code>.
     */
    public int size()
    {
        int size = 0;
        for( Iterator i = iterator(); i.hasNext(); ) {
            i.next();
            size++;
        }
        return size;
    }


    public boolean remove( Object object )
    {
        throw new UnsupportedOperationException();
    }


    public boolean addAll( Collection collection )
    {
        throw new UnsupportedOperationException();
    }


    public boolean removeAll( Collection collection )
    {
        throw new UnsupportedOperationException();
    }


    public boolean retainAll( Collection collection )
    {
        throw new UnsupportedOperationException();
    }


    public void clear()
    {
        throw new UnsupportedOperationException();
    }

}
