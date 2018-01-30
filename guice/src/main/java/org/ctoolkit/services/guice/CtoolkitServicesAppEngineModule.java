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

package org.ctoolkit.services.guice;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import javax.inject.Singleton;

/**
 * The ctoolkit services guice module with App Engine's services providers.
 * This module is being required with all of the modules using following App Engine services:
 * <ul>
 * <li>{@link BlobstoreService}</li>
 * <li>{@link ImagesService}</li>
 * <li>{@link AppIdentityService}</li>
 * <li>{@link CacheFactory}</li>
 * </ul>
 * ..or bind it on your own.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesAppEngineModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
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
