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
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.ctoolkit.services.storage.StorageService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.MessageFormat;
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
    private static final String STORAGE_NAME_PATTERN = "/gs/{0}/{1}";

    private final Storage storage;

    private final AppIdentityService appIdentityService;

    private final ImagesService imagesService;

    private final BlobstoreService blobstoreService;

    @Inject
    public StorageServiceBean( Storage storage,
                               AppIdentityService appIdentityService,
                               ImagesService imagesService,
                               BlobstoreService blobstoreService )
    {
        this.storage = storage;
        this.appIdentityService = appIdentityService;
        this.imagesService = imagesService;
        this.blobstoreService = blobstoreService;
    }

    /**
     * Parse bucket name and file name from the full storage name.
     *
     * @param fullName the cloud storage file full name
     * @return the bucket name and file name
     */
    private BucketName parseBucketAndName( @Nonnull String fullName )
    {
        checkNotNull( fullName );

        String[] split = fullName.split( "/" );
        if ( split.length == 4 && "gs".equals( split[1] ) )
        {
            String bucket = split[2];
            String name = split[3];

            return new BucketName( bucket, name );
        }
        else
        {
            String message = "The full storage name does not follow expected pattern '" + STORAGE_NAME_PATTERN
                    + "' for given argument: '" + fullName + "' Fill: {0} - bucket name and {1} file name.";

            throw new IllegalArgumentException( message );
        }
    }

    @Override
    public BlobId createBlobId( @Nonnull String fullName )
    {
        checkNotNull( fullName );

        BucketName bn = parseBucketAndName( fullName );
        String bucket = bn.bucket;
        String name = bn.name;

        return BlobId.of( bucket, name );
    }

    @Override
    public String getFullStorageName( @Nonnull Blob blob )
    {
        checkNotNull( blob );
        return MessageFormat.format( STORAGE_NAME_PATTERN, blob.getBucket(), blob.getName() );
    }

    @Override
    public BlobKey createBlobKey( @Nonnull String fullName )
    {
        // storage full name validation, in case of failure it throws IllegalArgumentException
        parseBucketAndName( fullName );
        return blobstoreService.createGsBlobKey( fullName );
    }

    @Override
    public BlobKey createBlobKey( @Nonnull Blob blob )
    {
        String fullName = this.getFullStorageName( blob );
        return blobstoreService.createGsBlobKey( fullName );
    }

    @Override
    public Blob store( @Nonnull byte[] data, @Nonnull String contentType )
    {
        return store( data, contentType, appIdentityService.getDefaultGcsBucketName(), UUID.randomUUID().toString() );
    }

    @Override
    public Blob store( @Nonnull byte[] data, @Nonnull String contentType, @Nonnull String blobName )
    {
        return store( data, contentType, appIdentityService.getDefaultGcsBucketName(), blobName );
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
    public byte[] readByFullStorageName( @Nonnull String fullName )
    {
        checkNotNull( fullName );

        BlobId blobId = createBlobId( fullName );
        return storage.readAllBytes( blobId );
    }

    @Override
    public byte[] read( @Nonnull BlobId blobId )
    {
        checkNotNull( blobId );
        return storage.readAllBytes( blobId );
    }

    @Override
    public byte[] read( @Nonnull String blobName )
    {
        checkNotNull( blobName );
        return read( appIdentityService.getDefaultGcsBucketName(), blobName );
    }

    @Override
    public byte[] read( @Nonnull String bucketName, @Nonnull String blobName )
    {
        checkNotNull( bucketName, "In order to read blob a bucket name must be provided." );
        checkNotNull( blobName, "In order to read blob a blob name must be provided." );

        return storage.readAllBytes( bucketName, blobName );
    }

    @Override
    public boolean delete( @Nonnull BlobId blobId )
    {
        checkNotNull( blobId );
        return storage.delete( blobId );
    }

    @Override
    public boolean delete( @Nonnull String blobName )
    {
        return delete( appIdentityService.getDefaultGcsBucketName(), blobName );
    }

    @Override
    public boolean delete( @Nonnull String bucketName, @Nonnull String blobName )
    {
        checkNotNull( bucketName, "In order to delete blob a bucket name must be provided." );
        checkNotNull( blobName, "In order to delete blob a file name must be provided." );

        BlobId blobId = BlobId.of( bucketName, blobName );

        return storage.delete( blobId );
    }

    @Override
    public String getSecureServingUrl( @Nonnull String fullName )
    {
        checkNotNull( fullName );

        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withGoogleStorageFileName( fullName )
                .crop( false )
                .secureUrl( true );

        return imagesService.getServingUrl( options );
    }

    @Override
    public String getSecureServingUrl( @Nonnull String fullName, int imageSize )
    {
        checkNotNull( fullName );

        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withGoogleStorageFileName( fullName )
                .imageSize( imageSize )
                .crop( false )
                .secureUrl( true );

        return imagesService.getServingUrl( options );
    }

    @Override
    public String getSecureServingUrl( @Nonnull BlobKey blobKey )
    {
        checkNotNull( blobKey );

        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withBlobKey( blobKey )
                .crop( false )
                .secureUrl( true );

        return imagesService.getServingUrl( options );
    }

    @Override
    public String getSecureServingUrl( @Nonnull BlobKey blobKey, int imageSize )
    {
        checkNotNull( blobKey );

        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withBlobKey( blobKey )
                .imageSize( imageSize )
                .crop( false )
                .secureUrl( true );

        return imagesService.getServingUrl( options );
    }

    private static class BucketName
    {
        String bucket;

        String name;

        BucketName( String bucket, String name )
        {
            this.bucket = bucket;
            this.name = name;
        }
    }
}
