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
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
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
        String[] split = checkNotNull( fullName, "Furl name is mandatory" ).split( "/" );
        if ( split.length >= 4 && "gs".equals( split[1] ) )
        {
            String bucket = split[2];

            if ( Strings.isNullOrEmpty( bucket ) )
            {
                throw new IllegalArgumentException( "The bucket has no value: '" + fullName + "'" );
            }

            Joiner joiner = Joiner.on( "/" );
            String prefix = joiner.join( split[0], split[1], split[2] );
            String name = fullName.substring( prefix.length() + 1 );

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
        BucketName bn = parseBucketAndName( fullName );
        String bucket = bn.bucket;
        String name = bn.name;

        return BlobId.of( bucket, name );
    }

    @Override
    public String getFullStorageName( @Nonnull Blob blob )
    {
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
        BlobId blobId = BlobId.of( bucketName, blobName );
        BlobInfo blobInfo = BlobInfo.newBuilder( blobId ).setContentType( checkNotNull( contentType ) ).build();

        return storage.create( blobInfo, checkNotNull( data ) );
    }

    @Override
    public byte[] read( @Nonnull String fullName )
    {
        BlobId blobId = createBlobId( fullName );
        return storage.readAllBytes( blobId );
    }

    @Override
    public byte[] read( @Nonnull BlobId blobId )
    {
        return storage.readAllBytes( checkNotNull( blobId ) );
    }

    @Override
    public byte[] read( @Nonnull String bucketName, @Nonnull String blobName )
    {
        return storage.readAllBytes(
                checkNotNull( bucketName, "In order to read blob a bucket name must be provided." ),
                checkNotNull( blobName, "In order to read blob a blob name must be provided." ) );
    }

    @Override
    public boolean delete( @Nonnull BlobId blobId )
    {
        return storage.delete( checkNotNull( blobId ) );
    }

    @Override
    public boolean delete( @Nonnull String fullName )
    {
        BlobId blobId = createBlobId( fullName );
        return delete( blobId );
    }

    @Override
    public boolean delete( @Nonnull String bucketName, @Nonnull String blobName )
    {
        BlobId blobId = BlobId.of(
                checkNotNull( bucketName, "In order to delete blob a bucket name must be provided." ),
                checkNotNull( blobName, "In order to delete blob a file name must be provided." ) );

        return storage.delete( blobId );
    }

    @Override
    public String getSecureServingUrl( @Nonnull String fullName )
    {
        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withGoogleStorageFileName( checkNotNull( fullName ) )
                .crop( false )
                .secureUrl( true );

        return imagesService.getServingUrl( options );
    }

    @Override
    public String getSecureServingUrl( @Nonnull String fullName, int imageSize )
    {
        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withGoogleStorageFileName( checkNotNull( fullName ) )
                .imageSize( imageSize )
                .crop( false )
                .secureUrl( true );

        return imagesService.getServingUrl( options );
    }

    @Override
    public String getSecureServingUrl( @Nonnull BlobKey blobKey )
    {
        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withBlobKey( checkNotNull( blobKey ) )
                .crop( false )
                .secureUrl( true );

        return imagesService.getServingUrl( options );
    }

    @Override
    public String getSecureServingUrl( @Nonnull BlobKey blobKey, int imageSize )
    {
        ServingUrlOptions options;
        options = ServingUrlOptions.Builder.withBlobKey( checkNotNull( blobKey ) )
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
