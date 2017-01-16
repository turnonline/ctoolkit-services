package org.ctoolkit.services.storage.appengine.datastore;

import com.google.inject.AbstractModule;
import org.ctoolkit.services.storage.EntityExecutor;

/**
 * The ctoolkit services AppEngine datastore module.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesAppEngineStorageModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( EntityExecutor.class ).to( ObjectifyEntityExecutor.class );
    }
}
