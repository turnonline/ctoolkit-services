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

package org.ctoolkit.services.storage.appengine.blob;

import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.testing.RemoteStorageHelper;

import javax.inject.Provider;

/**
 * The storage service instance provider configured against test Cloud Storage.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class TestStorageProvider
        implements Provider<Storage>
{
    static final String BUCKET = RemoteStorageHelper.generateBucketName();

    @Override
    public Storage get()
    {
        String projectId = System.getProperty( "cloud.storage.project.id" );
        RemoteStorageHelper helper = RemoteStorageHelper.create();
        Storage storage = helper.getOptions().toBuilder().setProjectId( projectId ).build().getService();
        storage.create( BucketInfo.of( BUCKET ) );

        return storage;
    }
}
