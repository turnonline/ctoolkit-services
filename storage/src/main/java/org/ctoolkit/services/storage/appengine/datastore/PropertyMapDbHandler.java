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

package org.ctoolkit.services.storage.appengine.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.collect.ImmutableMap;
import com.googlecode.objectify.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The handler that manages saving and loading dynamic properties on top of App Engine datastore.
 * All property values are saved as unindexed properties. There is one more specific property '{@link #DYN_PROPERTIES}'
 * to keep list of current property names for concrete entity.
 * <p>
 * It's not thread safe and it's intended to create a new instance for every query.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@SuppressWarnings( "WeakerAccess" )
public class PropertyMapDbHandler
{
    private static final String DYN_PROPERTIES = "dynProperties";

    private final Map<String, Object> map;

    private final Key key;

    /**
     * Constructor to prepare instance for load operation.
     *
     * @param key the source of the ID to retrieve target entity
     * @param <T> the entity type
     */
    public <T> PropertyMapDbHandler( @Nonnull Key<T> key )
    {
        checkNotNull( key );

        this.key = key;
        this.map = null;
    }

    /**
     * Constructor to prepare instance for save operation.
     *
     * @param key the source of the ID to retrieve target entity
     * @param map the map of property value pair to be saved
     * @param <T> the entity type
     */
    public <T> PropertyMapDbHandler( @Nonnull Key<T> key, @Nonnull Map<String, Object> map )
    {
        checkNotNull( key );
        checkNotNull( map );

        this.key = key;
        this.map = ImmutableMap.<String, Object>builder().putAll( map ).build();
    }

    public void save()
    {
        if ( map == null )
        {
            String msg = "Map cannot be null in order to save. The instance is being configured to load().";
            throw new IllegalArgumentException( msg );
        }

        DatastoreService lowDb = DatastoreServiceFactory.getDatastoreService();
        Set<String> newPropertyNames = map.keySet();
        List<String> savedPropertyNames;

        try
        {
            Entity entity = lowDb.get( key.getRaw() );

            //noinspection unchecked
            savedPropertyNames = ( List<String> ) entity.getProperty( DYN_PROPERTIES );
            if ( savedPropertyNames != null )
            {
                savedPropertyNames.removeAll( newPropertyNames );
                if ( !savedPropertyNames.isEmpty() )
                {
                    // this map contains list of already removed properties
                    for ( String property : savedPropertyNames )
                    {
                        entity.removeProperty( property );
                    }
                }
            }

            if ( newPropertyNames.isEmpty() && ( savedPropertyNames == null || savedPropertyNames.isEmpty() ) )
            {
                return;
            }

            entity.setUnindexedProperty( DYN_PROPERTIES, newPropertyNames );

            for ( String property : newPropertyNames )
            {
                Object value = map.get( property );
                entity.setUnindexedProperty( property, value );
            }

            lowDb.put( entity );
        }
        catch ( EntityNotFoundException e )
        {
            throw new IllegalArgumentException( e );
        }

    }

    /**
     * Loads map of values. The list of properties in the map are defined by value of property {@link #DYN_PROPERTIES}.
     * {@link #load(List)}
     *
     * @return the map of values
     */
    public Map<String, Object> load()
    {
        return load( null );
    }

    /**
     * Loads map of values for given list of property names. If {@code null} it will retrieve
     * saved {@link #DYN_PROPERTIES} list of names.
     *
     * @param propertyNames the list of property names to retrieve
     * @return the map of values
     */
    public Map<String, Object> load( @Nullable List<String> propertyNames )
    {
        DatastoreService lowDb = DatastoreServiceFactory.getDatastoreService();
        Map<String, Object> map = new HashMap<>();

        try
        {
            Entity entity = lowDb.get( key.getRaw() );

            if ( propertyNames == null )
            {
                //noinspection unchecked
                propertyNames = ( List<String> ) entity.getProperty( DYN_PROPERTIES );
                if ( propertyNames == null )
                {
                    // there is nothing to load
                    return map;
                }
            }

            for ( String property : propertyNames )
            {
                Object value = entity.getProperty( property );
                if ( value != null )
                {
                    map.put( property, value );
                }
            }
        }
        catch ( EntityNotFoundException e )
        {
            throw new IllegalArgumentException( e );
        }

        return map;
    }
}
