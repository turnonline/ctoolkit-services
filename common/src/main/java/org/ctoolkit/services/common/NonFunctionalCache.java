/*
 * Copyright (c) 2017 Comvai, s.r.o. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.ctoolkit.services.common;

import javax.cache.Cache;
import javax.cache.CacheEntry;
import javax.cache.CacheListener;
import javax.cache.CacheStatistics;
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
    public Map getAll( Collection keys )
    {
        return null;
    }

    @Override
    public void load( Object key )
    {
    }

    @Override
    public void loadAll( Collection keys )
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
