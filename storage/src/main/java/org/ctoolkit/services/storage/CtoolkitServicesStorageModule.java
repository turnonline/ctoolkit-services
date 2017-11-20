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

package org.ctoolkit.services.storage;

import com.google.cloud.storage.Storage;
import com.google.inject.AbstractModule;
import org.ctoolkit.services.guice.CtoolkitServicesAppEngineModule;
import org.ctoolkit.services.storage.appengine.blob.StorageServiceBean;
import org.ctoolkit.services.storage.appengine.datastore.ObjectifyEntityExecutor;

/**
 * The ctoolkit services App Engine datastore and storage module.
 * Install this module if you need to inject one of the following services:
 * <ul>
 * <li>{@link EntityExecutor}</li>
 * <li>{@link StorageService}</li>
 * </ul>
 * The {@link CtoolkitServicesAppEngineModule} is being required by this module to be installed.
 * <p>
 * The {@link StorageService} implementation requires underlying {@link Storage} instance to be injected.
 * Either bind the {@link DefaultStorageProvider} or use your own configuration.
 * <pre>
 * {@code
 *      bind( Storage.class ).toProvider( DefaultStorageProvider.class ).in( Singleton.class );
 * }
 * </pre>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesStorageModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( EntityExecutor.class ).to( ObjectifyEntityExecutor.class );
        bind( StorageService.class ).to( StorageServiceBean.class );
    }
}
