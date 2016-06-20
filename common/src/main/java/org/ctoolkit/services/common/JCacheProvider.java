package org.ctoolkit.services.common;

import net.sf.jsr107cache.Cache;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;

/**
 * The JCache provider to provide default cache instance by
 * {@link PropertyService#create(String)} with no namespace.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
class JCacheProvider
        implements Provider<Cache>
{
    private final PropertyService service;

    @Inject
    JCacheProvider( PropertyService service )
    {
        this.service = service;
    }

    @Override
    public Cache get()
    {
        return service.create( new HashMap<>() );
    }
}
