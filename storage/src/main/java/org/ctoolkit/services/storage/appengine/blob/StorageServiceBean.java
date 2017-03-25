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
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.ctoolkit.services.storage.StorageService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Google Cloud Storage service API implementation.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class StorageServiceBean
        implements StorageService
{
    private final Storage storage;

    private final AppIdentityService appIdentityService;

    @Inject
    public StorageServiceBean( Storage storage, AppIdentityService appIdentityService )
    {
        this.storage = storage;
        this.appIdentityService = appIdentityService;
    }

    @Override
    public Blob store( @Nonnull byte[] data, @Nonnull String contentType )
    {
        return store( data, contentType, appIdentityService.getDefaultGcsBucketName(), UUID.randomUUID().toString() );
    }

    @Override
    public Blob store( @Nonnull byte[] data,
                       @Nonnull String contentType,
                       @Nonnull String bucketName,
                       @Nonnull String blobName )
    {
        checkNotNull( data );
        checkNotNull( contentType );
        checkNotNull( blobName );
        checkNotNull( bucketName );

        BlobId blobId = BlobId.of( bucketName, blobName );
        BlobInfo blobInfo = BlobInfo.newBuilder( blobId ).setContentType( contentType ).build();

        return storage.create( blobInfo, data );
    }

    @Override
    public byte[] readAllBytes( @Nonnull String blobName )
    {
        checkNotNull( blobName );
        return readAllBytes( blobName, appIdentityService.getDefaultGcsBucketName() );
    }

    @Override
    public byte[] readAllBytes( @Nonnull String bucketName, @Nonnull String blobName )
    {
        checkNotNull( bucketName, "In order to read blob a bucket name must be provided." );
        checkNotNull( blobName, "In order to read blob a blob name must be provided." );

        return storage.readAllBytes( bucketName, blobName );
    }

    @Override
    public boolean delete( @Nonnull String blobName )
    {
        return delete( blobName, appIdentityService.getDefaultGcsBucketName() );
    }

    @Override
    public boolean delete( @Nonnull String bucketName, @Nonnull String blobName )
    {
        checkNotNull( bucketName, "In order to delete blob a bucket name must be provided." );
        checkNotNull( blobName, "In order to delete blob a file name must be provided." );

        BlobId blobId = BlobId.of( bucketName, blobName );

        return storage.delete( blobId );
    }
}
