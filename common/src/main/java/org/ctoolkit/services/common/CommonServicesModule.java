package org.ctoolkit.services.common;

import com.google.inject.AbstractModule;
import net.sf.jsr107cache.Cache;

import javax.inject.Singleton;

/**
 * The common services guice module.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CommonServicesModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( PropertyService.class ).to( PropertyServiceBean.class ).in( Singleton.class );
        bind( Cache.class ).toProvider( JCacheProvider.class ).in( Singleton.class );
    }
}
