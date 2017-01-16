package org.ctoolkit.services.storage.appengine.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.common.collect.ImmutableMap;
import com.googlecode.objectify.Key;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The handler that manages saving and loading dynamic properties on top of AppEngine datastore.
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

    public Map<String, Object> load()
    {
        DatastoreService lowDb = DatastoreServiceFactory.getDatastoreService();
        List<String> propertyNames;
        Map<String, Object> map = new HashMap<>();

        try
        {
            Entity entity = lowDb.get( key.getRaw() );

            //noinspection unchecked
            propertyNames = ( List<String> ) entity.getProperty( DYN_PROPERTIES );
            if ( propertyNames == null )
            {
                // there is nothing to load
                return map;
            }

            for ( String property : propertyNames )
            {
                Object value = entity.getProperty( property );
                map.put( property, value );
            }
        }
        catch ( EntityNotFoundException e )
        {
            throw new IllegalArgumentException( e );
        }

        return map;
    }
}
