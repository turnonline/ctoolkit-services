package org.ctoolkit.services.common;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheEntry;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheListener;
import net.sf.jsr107cache.CacheStatistics;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Non functional cache implementation. Behaves like an empty cache.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
class NonFunctionalCache
        implements Cache
{
    @Override
    public boolean containsKey( Object key )
    {
        return false;
    }

    @Override
    public boolean containsValue( Object value )
    {
        return false;
    }

    @Override
    public Set entrySet()
    {
        return null;
    }

    @Override
    public boolean isEmpty()
    {
        return true;
    }

    @Override
    public Set keySet()
    {
        return null;
    }

    @Override
    public void putAll( Map t )
    {
    }

    @Override
    public int size()
    {
        return 0;
    }

    @Override
    public Collection values()
    {
        return null;
    }

    @Override
    public Object get( Object key )
    {
        return null;
    }

    @Override
    public Map getAll( Collection keys ) throws CacheException
    {
        return null;
    }

    @Override
    public void load( Object key ) throws CacheException
    {
    }

    @Override
    public void loadAll( Collection keys ) throws CacheException
    {
    }

    @Override
    public Object peek( Object key )
    {
        return null;
    }

    @Override
    public Object put( Object key, Object value )
    {
        return null;
    }

    @Override
    public CacheEntry getCacheEntry( Object key )
    {
        return null;
    }

    @Override
    public CacheStatistics getCacheStatistics()
    {
        return null;
    }

    @Override
    public Object remove( Object key )
    {
        return null;
    }

    @Override
    public void clear()
    {
    }

    @Override
    public void evict()
    {
    }

    @Override
    public void addListener( CacheListener listener )
    {
    }

    @Override
    public void removeListener( CacheListener listener )
    {
    }
}
