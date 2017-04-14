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

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.ImagesService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * The storage service convenient methods unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class StorageServiceBeanTest
{
    @Tested
    private StorageServiceBean tested;

    @Injectable
    private Storage storage;

    @Injectable
    private AppIdentityService appIdentityService;

    @Injectable
    private ImagesService imagesService;

    @Injectable
    private BlobstoreService blobstoreService;

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void createBlobIdWrongArg() throws Exception
    {
        String fullName = "/gs/fileName";
        tested.createBlobId( fullName );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void createBlobIdWrongPrefix() throws Exception
    {
        String fullName = "/haha/bucketName-12/fileName-23";
        tested.createBlobId( fullName );
    }

    @Test
    public void createBlobId() throws Exception
    {
        String fullName = "/gs/bucketName-12/fileName-23";
        BlobId blobId = tested.createBlobId( fullName );

        assertNotNull( blobId );
        assertEquals( blobId.getBucket(), "bucketName-12" );
        assertEquals( blobId.getName(), "fileName-23" );
    }

    @Test
    public void getFullStorageName( @Mocked final Blob blob ) throws Exception
    {
        new Expectations()
        {
            {
                blob.getBucket();
                result = "bucketName-42";

                blob.getName();
                result = "fileName-53";
            }
        };

        String fullStorageName = tested.getFullStorageName( blob );
        assertEquals( fullStorageName, "/gs/bucketName-42/fileName-53" );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void createBlobKeyWrongPrefix() throws Exception
    {
        String fullName = "/haha/bucketName-12/fileName-23";
        tested.createBlobKey( fullName );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void createBlobKeyWrongArg() throws Exception
    {
        String fullName = "/gs/fileName";
        tested.createBlobKey( fullName );
    }

    @Test
    public void createBlobKey() throws Exception
    {
        final String fullName = "/gs/bucketName-12/file/Name-23";
        tested.createBlobKey( fullName );

        new Verifications()
        {
            {
                blobstoreService.createGsBlobKey( fullName );
            }
        };
    }

    @Test
    public void createBlobKeyFromBlob( @Mocked final Blob blob ) throws Exception
    {
        new Expectations()
        {
            {
                blob.getBucket();
                result = "bucketName-12";

                blob.getName();
                result = "file/Name-23/AbV";
            }
        };

        tested.createBlobKey( blob );

        new Verifications()
        {
            {
                blobstoreService.createGsBlobKey( "/gs/bucketName-12/file/Name-23/AbV" );
            }
        };
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void readByFullStorageNameWrongArg() throws Exception
    {
        String fullName = "/gs/fileName";
        tested.read( fullName );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void readByFullStorageNameWrongPrefix() throws Exception
    {
        String fullName = "/haha/bucketName-12/fileName-23";
        tested.read( fullName );
    }

    @Test
    public void readByFullStorageName() throws Exception
    {
        final String fullName = "/gs/bucketName-12/fileName-23";
        tested.read( fullName );

        new Verifications()
        {
            {
                BlobId blobId;
                storage.readAllBytes( blobId = withCapture() );
                assertEquals( blobId.getBucket(), "bucketName-12" );
                assertEquals( blobId.getName(), "fileName-23" );
            }
        };
    }
}