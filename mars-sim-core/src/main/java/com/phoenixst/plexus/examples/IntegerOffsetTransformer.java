/*
 *  $Id: IntegerOffsetTransformer.java,v 1.8 2005/10/03 15:14:43 rconner Exp $
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

package com.phoenixst.plexus.examples;

import com.phoenixst.collections.InvertibleTransformer;


/**
 *  An {@link InvertibleTransformer} which adds an offset to
 *  <code>Integers</code>.
 *
 *  @version    $Revision: 1.8 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class IntegerOffsetTransformer
    implements InvertibleTransformer,
               java.io.Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     *  The number to add to transform an Integer.
     *
     *  @serial
     */
    private final int offset;


    /**
     *  Creates a new <code>IntegerOffsetTransformer</code>.
     */
    public IntegerOffsetTransformer( int offset )
    {
        super();
        this.offset = offset;
    }


    public Object transform( Object object )
    {
        if( !(object instanceof Integer) ) {
            return object;
        }
        return new Integer( ((Integer) object).intValue() + offset );
    }


    public Object untransform( Object object )
    {
        if( !(object instanceof Integer) ) {
            return object;
        }
        return new Integer( ((Integer) object).intValue() - offset );
    }

}
