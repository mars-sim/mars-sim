/*
 *  $Id: LoggingCollection.java,v 1.10 2006/06/05 20:32:49 rconner Exp $
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

import java.util.*;

import org.apache.log4j.*;


/**
 *  A <code>Collection</code> which wraps another to provide logging
 *  support.
 *
 *  @version    $Revision: 1.10 $
 *  @author     Ray A. Conner
 *
 *  @since      1.0
 */
public class LoggingCollection
    implements Collection
{

    /**
     *  The default logger.
     */
    private static final Logger DEFAULT_LOGGER = Logger.getLogger( LoggingCollection.class );

    /**
     *  The wrapped collection.
     */
    private final Collection delegate;

    /**
     *  The logger to use.
     */
    private final Logger logger;

    /**
     *  The level at which to log.
     */
    private final Level level;


    ////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////


    /**
     *  Creates a new <code>LoggingCollection</code> which
     *  logs to a category corresponding to this class at
     *  the DEBUG level.
     */
    public LoggingCollection( Collection delegate )
    {
        this( delegate, DEFAULT_LOGGER, Level.DEBUG );
    }


    /**
     *  Creates a new <code>LoggingCollection</code> which uses
     *  the specified log at the DEBUG level.
     */
    public LoggingCollection( Collection delegate, Logger logger )
    {
        this( delegate, logger, Level.DEBUG );
    }


    /**
     *  Creates a new <code>LoggingCollection</code>.
     */
    public LoggingCollection( Collection delegate, Logger logger, Level level )
    {
        this.delegate = delegate;
        this.logger = logger;
        this.level = level;
        if( delegate == null ) {
            throw new IllegalArgumentException( "Delegate Collection is null." );
        }
        if( logger == null ) {
            throw new IllegalArgumentException( "Logger is null." );
        }
        if( level == null ) {
            throw new IllegalArgumentException( "Level is null." );
        }
    }


    ////////////////////////////////////////
    // Protected methods
    ////////////////////////////////////////


    /**
     *  Provides access to internal state so it can be used
     *  by extensions of this class.
     */
    protected final Collection getDelegate()
    {
        return delegate;
    }


    /**
     *  Provides access to internal state so it can be used
     *  by extensions of this class.
     */
    protected final Logger getLogger()
    {
        return logger;
    }


    /**
     *  Provides access to internal state so it can be used
     *  by extensions of this class.
     */
    protected final Level getLevel()
    {
        return level;
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    public int size()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin size()" );
            int size = delegate.size();
            logger.log( level, "End size(), returns " + size );
            return size;
        }
        return delegate.size();
    }


    public boolean isEmpty()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin isEmpty()" );
            boolean isEmpty = delegate.isEmpty();
            logger.log( level, "End isEmpty(), returns " + isEmpty );
            return isEmpty;
        }
        return delegate.isEmpty();
    }


    public void clear()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin clear()" );
            delegate.clear();
            logger.log( level, "End clear()" );
            return;
        }
        delegate.clear();
    }


    public boolean add( Object object )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( object );
            logger.log( level, "Begin add( " + argString + " )" );
            boolean modified = delegate.add( object );
            logger.log( level, "End add( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.add( object );
    }


    public boolean remove( Object object )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( object );
            logger.log( level, "Begin remove( " + argString + " )" );
            boolean modified = delegate.remove( object );
            logger.log( level, "End remove( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.remove( object );
    }


    public boolean contains( Object object )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( object );
            logger.log( level, "Begin contains( " + argString + " )" );
            boolean contains = delegate.contains( object );
            logger.log( level, "End contains( " + argString + " ), returns " + contains );
            return contains;
        }
        return delegate.contains( object );
    }


    public Iterator iterator()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin iterator()" );
            Iterator iterator = delegate.iterator();
            logger.log( level, "End iterator(), returns " + iterator );
            return new LoggingIterator( iterator, logger, level );
        }
        // We still wrap the iterator since logging may actually
        // become enabled during iteration.
        return new LoggingIterator( delegate.iterator(), logger, level );
    }


    public boolean addAll( Collection collection )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( collection );
            logger.log( level, "Begin addAll( " + argString + " )" );
            boolean modified = delegate.addAll( collection );
            logger.log( level, "End addAll( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.addAll( collection );
    }


    public boolean containsAll( Collection collection )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( collection );
            logger.log( level, "Begin containsAll( " + argString + " )" );
            boolean containsAll = delegate.containsAll( collection );
            logger.log( level, "End containsAll( " + argString + " ), returns " + containsAll );
            return containsAll;
        }
        return delegate.containsAll( collection );
    }


    public boolean removeAll( Collection collection )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( collection );
            logger.log( level, "Begin removeAll( " + argString + " )" );
            boolean modified = delegate.removeAll( collection );
            logger.log( level, "End removeAll( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.removeAll( collection );
    }


    public boolean retainAll( Collection collection )
    {
        if( logger.isEnabledFor( level ) ) {
            String argString = String.valueOf( collection );
            logger.log( level, "Begin retainAll( " + argString + " )" );
            boolean modified = delegate.retainAll( collection );
            logger.log( level, "End retainAll( " + argString + " ), returns " + modified );
            return modified;
        }
        return delegate.retainAll( collection );
    }


    public Object[] toArray()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin toArray()" );
            Object[] array = delegate.toArray();
            logger.log( level, "End toArray(), returns " + array );
            return array;
        }
        return delegate.toArray();
    }


    public Object[] toArray( Object[] array )
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin toArray()" );
            Object[] returnedArray = delegate.toArray( array );
            logger.log( level, "End toArray(), returns " + returnedArray );
            return returnedArray;
        }
        return delegate.toArray( array );
    }


    ////////////////////////////////////////
    // Object
    ////////////////////////////////////////


    public String toString()
    {
        if( logger.isEnabledFor( level ) ) {
            logger.log( level, "Begin toString()" );
            String string = delegate.toString();
            logger.log( level, "End toString(), returns " + string );
            return string;
        }
        return delegate.toString();
    }

}
