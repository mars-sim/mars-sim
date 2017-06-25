/*
 *  $Id: FalsePredicate.java,v 1.8 2006/06/07 16:33:29 rconner Exp $
 *
 *  Copyright (C) 1994-2006 by Phoenix Software Technologists,
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

import org.apache.commons.collections.Predicate;


/**
 *  A <code>Predicate</code> which always returns <code>false</code>.
 *
 *  <P>This is mostly equivalent to the class of the same name in
 *  Jakarta Commons-Collections 3.0.  This implementation fixes a
 *  serialization bug in the Jakarta version so that the singleton
 *  property is correctly preserved.  No equivalent exists in version
 *  2.1.
 *
 *  @version    $Revision: 1.8 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public final class FalsePredicate
    implements Predicate,
               java.io.Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  Singleton <code>FalsePredicate</code> instance.
     */
    public static final FalsePredicate INSTANCE = new FalsePredicate();


    /**
     *  Creates a new <code>FalsePredicate</code>.  Private to
     *  preserve singleton.
     */
    private FalsePredicate()
    {
        super();
    }


    /**
     *  Make sure that the singleton stays that way.
     */
    private Object readResolve()
    {
        return INSTANCE;
    }


    /**
     *  Returns <code>false</code>.
     */
    public boolean evaluate( Object object )
    {
        return false;
    }


    public String toString()
    {
        return "FALSE";
    }

}
