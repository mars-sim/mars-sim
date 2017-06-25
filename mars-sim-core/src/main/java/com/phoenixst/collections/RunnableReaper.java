/*
 *  $Id: RunnableReaper.java,v 1.1 2006/06/19 19:12:26 rconner Exp $
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

import java.lang.ref.*;
import java.util.*;


/**
 *  A <code>Runnable</code> which reaps {@link Reapable Reapables}.
 *
 *  @version    $Revision: 1.1 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class RunnableReaper
    implements Runnable,
               Reaper
{

    /**
     *  Default sleep time in milliseconds.
     */
    private static final long DEFAULT_SLEEP = 100;

    /**
     *  A single default Reaper which to be used when a structure
     *  doesn't mind sharing the reaping thread with other things.
     */
    public static final Reaper DEFAULT_INSTANCE = RunnableReaper.getInstance( "DefaultReaper" );


    private final long sleepTime;

    private final ReferenceQueue refQueue = new ReferenceQueue();


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>RunnableReaper</code> which sleeps for 0.1
     *  seconds after emptying its internal ReferenceQueue of waiting
     *  References and notifying the corresponding
     *  <code>Reapables</code>.  It is up to the user to create a
     *  thread to run this Reaper; alternatively, one of the static
     *  factory methods can be used.
     */
    public RunnableReaper()
    {
        this( DEFAULT_SLEEP );
    }


    /**
     *  Creates a new <code>RunnableReaper</code> which sleeps for the
     *  specified number of milliseconds after emptying its internal
     *  ReferenceQueue of waiting References and notifying the
     *  corresponding <code>Reapables</code>.  It is up to the user to
     *  create a thread to run this Reaper; alternatively, one of the
     *  static factory methods can be used.
     */
    public RunnableReaper( long sleepTime )
    {
        super();
        this.sleepTime = sleepTime;
    }


    ////////////////////////////////////////
    // Factory methods
    ////////////////////////////////////////


    /**
     *  Creates a new <code>RunnableReaper</code> which sleeps for 0.1
     *  seconds after emptying its internal ReferenceQueue of waiting
     *  References and notifying the corresponding
     *  <code>Reapables</code>.  A new thread of minimum priority to
     *  run the <code>RunnableReaper</code> is also created and
     *  started by this method.
     */
    public static Reaper getInstance( String name )
    {
        return getInstance( name, DEFAULT_SLEEP, Thread.MIN_PRIORITY );
    }


    /**
     *  Creates a new <code>RunnableReaper</code> which sleeps for the
     *  specified number of milliseconds after emptying its internal
     *  ReferenceQueue of waiting References and notifying the
     *  corresponding <code>Reapables</code>.  A new thread of the
     *  specified priority to run the <code>RunnableReaper</code> is
     *  also created and started by this method.
     */
    public static Reaper getInstance( String name,
                                      long sleepTime,
                                      int threadPriority )
    {
        RunnableReaper reaper = new RunnableReaper( sleepTime );
        Thread thread = new Thread( reaper, name );
        thread.setPriority( threadPriority );
        thread.setDaemon( true );
        thread.start();
        return reaper;
    }


    ////////////////////////////////////////
    // Reaper
    ////////////////////////////////////////


    public Reference createReference( Reapable reapable, Object referent )
    {
        return new ReferenceImpl( reapable, referent, refQueue );
    }


    ////////////////////////////////////////
    // Runnable
    ////////////////////////////////////////


    public void run()
    {
        Set reapableSet = new HashSet();

        while( true ) {

            // Block until we get the first element, then get
            // everything we can until the queue is empty.  We're
            // trying to process the references infrequently in large
            // chunks.
            Reference reference = null;
            try {
                reference = refQueue.remove();
            } catch( InterruptedException e ) {
                // do nothing
            }
            while( reference != null ) {
                reapableSet.add( ((ReferenceImpl) reference).reapable );
                reference = refQueue.poll();
            }

            // Reap each Reapable which has at least one reference
            // that has been reclaimed.
            for( Iterator i = reapableSet.iterator(); i.hasNext(); ) {
                ((Reapable) i.next()).reap();
            }
            reapableSet.clear();

            // Sleep for a little while.
            try {
                Thread.sleep( sleepTime );
            } catch( InterruptedException e ) {
                // do nothing
            }
        }
    }


    ////////////////////////////////////////
    // Private Reference class
    ////////////////////////////////////////


    private static class ReferenceImpl extends WeakReference
    {
        final Reapable reapable;

        ReferenceImpl( Reapable reapable, Object referent, ReferenceQueue refQueue )
        {
            super( referent, refQueue );
            this.reapable = reapable;
        }
    }

}
