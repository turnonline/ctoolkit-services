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

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.testing.RemoteStorageHelper;
import com.google.common.io.ByteStreams;
import org.ctoolkit.services.storage.StorageService;
import org.ctoolkit.services.storage.appengine.BackendServiceTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.ctoolkit.services.storage.appengine.blob.TestStorageProvider.BUCKET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * The blob service tested against cloud storage.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class StorageServiceDbIT
        extends BackendServiceTestCase
{
    private static final Logger log = LoggerFactory.getLogger( StorageServiceDbIT.class );

    private static final String BLOB_NAME = "thismac";

    @Inject
    private StorageService tested;

    @Inject
    private Storage storage;

    @AfterClass
    private void after() throws ExecutionException, InterruptedException
    {
        if ( !RemoteStorageHelper.forceDelete( storage, BUCKET, 5, TimeUnit.SECONDS ) )
        {
            log.warn( "Deletion of bucket " + BUCKET + " timed out, bucket is not empty" );
        }
    }

    @Test
    public void readAllBytes() throws Exception
    {
        InputStream stream = StorageServiceDbIT.class.getResourceAsStream( "thismac.png" );
        byte[] dataInput = ByteStreams.toByteArray( stream );
        String contentType = "image/png";

        Blob info = tested.store( dataInput, contentType, BUCKET, BLOB_NAME );

        assertNotNull( info, "Blob info instance is being expected," );
        assertNotNull( info.getName() );
        assertNotNull( info.getBucket() );
        assertNotNull( info.getCreateTime() );
        assertEquals( info.getContentType(), contentType );

        byte[] served = tested.read( BUCKET, info.getName() );

        assertEquals( served, dataInput );
    }

    @Test
    public void delete() throws Exception
    {
        InputStream stream = StorageServiceDbIT.class.getResourceAsStream( "thismac.png" );
        byte[] dataInput = ByteStreams.toByteArray( stream );
        String contentType = "image/png";

        Blob info = tested.store( dataInput, contentType, BUCKET, BLOB_NAME );
        assertTrue( tested.delete( BUCKET, info.getName() ) );
    }
}