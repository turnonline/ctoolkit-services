package org.ctoolkit.services.guice.appengine;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;
import org.ctoolkit.services.storage.DataUploadListener;

import javax.inject.Singleton;

/**
 * The ctoolkit services AppEngine guice module.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesAppEngineModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        Multibinder.newSetBinder( binder(), DataUploadListener.class );
    }

    @Provides
    @Singleton
    BlobstoreService provideBlobstoreService()
    {
        return BlobstoreServiceFactory.getBlobstoreService();
    }

    @Provides
    @Singleton
    ImagesService provideImagesService()
    {
        return ImagesServiceFactory.getImagesService();
    }

    @Provides
    @Singleton
    AppIdentityService provideAppIdentityService()
    {
        return AppIdentityServiceFactory.getAppIdentityService();
    }

    @Provides
    @Singleton
    CacheFactory provideCacheFactory() throws CacheException
    {
        return CacheManager.getInstance().getCacheFactory();
    }
}
