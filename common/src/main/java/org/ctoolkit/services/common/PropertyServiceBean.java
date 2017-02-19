package org.ctoolkit.services.common;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Strings;
import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Property service implementation of {@link PropertyService} for AppEngine.
 * <p>
 * If no configuration instance {@link PropertyConfig} is being provided in module, default behavior is:
 * <ul>
 * <li>Development: {@link #isDevelopmentEnvironment()} returns true</li>
 * <li>AppEngine: {@link #isTestEnvironment()} returns true</li>
 * </ul>
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
class PropertyServiceBean
        implements PropertyService
{
    private static final Logger logger = LoggerFactory.getLogger( PropertyServiceBean.class );

    private static final String ENTITY_PROPERTY = "Property";

    private static final String ENTITY_PROPERTY_VALUE = "value";

    private static final String PROPERTY_CACHE_NAMESPACE = "ctoolkit_common_properties_cache_namespace";

    private final CacheFactory factory;

    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

    private Cache cache;

    private boolean isTestEnvironment = false;

    private boolean isProductionEnvironment = false;

    private boolean isDevelopmentEnvironment = false;

    @Inject
    PropertyServiceBean( CacheFactory factory, @Configuration Map<String, String> configuration )
    {
        this.factory = factory;
        cache = create( PROPERTY_CACHE_NAMESPACE );

        String productionAppId = configuration.get( "service.property.appId.production" );
        String testAppId = configuration.get( "service.property.appId.test" );

        if ( SystemProperty.environment.value() == SystemProperty.Environment.Value.Production )
        {
            // means running on the app engine environment under an applicationId
            isTestEnvironment = SystemProperty.applicationId.get().equals( testAppId );

            if ( Strings.isNullOrEmpty( testAppId ) )
            {
                // If testAppId is not being set and running on AppEngine, we assume it is TEST environment.
                // Unless overridden by production AppId configuration
                isTestEnvironment = true;
            }

            isProductionEnvironment = !isTestEnvironment;
            isDevelopmentEnvironment = false;
        }

        if ( SystemProperty.environment.value() == SystemProperty.Environment.Value.Production )
        {
            // means running on the app engine environment under an applicationId
            isProductionEnvironment = SystemProperty.applicationId.get().equals( productionAppId );
            isTestEnvironment = !isProductionEnvironment;
            isDevelopmentEnvironment = false;
        }

        if ( SystemProperty.environment.value() == SystemProperty.Environment.Value.Development )
        {
            // means running on the local app engine environment
            isDevelopmentEnvironment = true;
            isTestEnvironment = false;
            isProductionEnvironment = false;
        }

        logger.info( "Configured production environment: " + productionAppId );
        logger.info( "Configured test environment: " + testAppId );
        logger.info( "App Engine environment: " + SystemProperty.environment.value() );
        logger.info( "App Engine application ID: " + SystemProperty.applicationId.get() );
    }

    @Override
    public Cache create( @Nullable String namespace )
    {
        Map<String, String> properties = new HashMap<>();
        if ( !Strings.isNullOrEmpty( namespace ) )
        {
            properties.put( GCacheFactory.NAMESPACE, namespace );
        }

        return create( properties );
    }

    @Override
    public Cache create( @Nonnull Map config )
    {
        try
        {
            return factory.createCache( config );
        }
        catch ( CacheException e )
        {
            logger.error( "Error occurred during creating cache instance, providing "
                    + NonFunctionalCache.class.getSimpleName() + " instance instead.", e );
        }

        return new NonFunctionalCache();
    }

    @Override
    public String getString( String key )
    {
        return getProperty( key, StringConverter.instance() );
    }

    @Override
    public void setString( String key, String value )
    {
        put( key, value );
    }

    @Override
    public Double getDouble( String key )
    {
        return getProperty( key, DoubleConverter.instance() );
    }

    @Override
    public void setDouble( String key, Double value )
    {
        put( key, value );
    }

    @Override
    public Integer getInteger( String key )
    {
        return getProperty( key, IntegerConverter.instance() );
    }

    @Override
    public void setInteger( String key, Integer value )
    {
        put( key, value );
    }

    @Override
    public boolean isProductionEnvironment()
    {
        return isProductionEnvironment;
    }

    @Override
    public boolean isTestEnvironment()
    {
        return isTestEnvironment;
    }

    /**
     * This returns true as well if property is being overridden by
     * <code>System.setProperty( "com.google.appengine.runtime.environment", "Development" )</code>
     *
     * @return true if current application is running in test environment or has that property set to 'Development'
     */
    @Override
    public boolean isDevelopmentEnvironment()
    {
        return isDevelopmentEnvironment;
    }

    public String getAppSpotURL()
    {
        return getAppSpotURL( null );
    }

    public String getAppSpotURL( String prefix )
    {
        StringBuilder builder = new StringBuilder();

        builder.append( "https://" );
        if ( !Strings.isNullOrEmpty( prefix ) )
        {
            builder.append( prefix );
            builder.append( "-dot-" );
        }
        builder.append( SystemProperty.applicationId.get() );
        builder.append( ".appspot.com" );

        logger.info( "AppSpot URL: " + builder );

        return builder.toString();
    }

    /**
     * Return typed property value from cache. If property is not cached, method will load value from data-store
     * and than puts it in to cache.
     *
     * @param key       property key
     * @param converter property type converter
     * @param <T>       property value type
     * @return typed property value
     */
    @SuppressWarnings( value = "unchecked" )
    private <T> T getProperty( String key, Converter<T> converter )
    {
        Object object = cache.get( key );
        T property = null;

        // if property is cached return it
        if ( object != null )
        {
            property = ( T ) object;
        }
        // else try to get it from data-store and cache it
        else
        {
            Entity entity = get( key );
            if ( entity != null )
            {
                property = converter.convert( entity.getProperty( ENTITY_PROPERTY_VALUE ) );
                if ( property != null )
                {
                    cache.put( key, property );
                }
            }
        }

        return property;
    }

    /**
     * Get property from data-store. If property for specified key does not exists, returns <code>null</code>.
     *
     * @param key property key
     * @return entity containing property object
     */
    private Entity get( String key )
    {
        try
        {
            return datastoreService.get( KeyFactory.createKey( ENTITY_PROPERTY, key ) );
        }
        catch ( EntityNotFoundException e )
        {
            logger.warn( "Property entity not found for key: " + key );
            return null;
        }
    }

    /**
     * Put key-value pair to data-store, and cache it.
     *
     * @param key   property key
     * @param value property value
     */
    private void put( String key, Object value )
    {
        Entity property = get( key );
        if ( property == null )
        {
            property = new Entity( ENTITY_PROPERTY, key );
        }

        property.setProperty( ENTITY_PROPERTY_VALUE, value );

        // put property entity to data-store
        datastoreService.put( property );

        // cache property
        cache.put( key, value );
    }
}
