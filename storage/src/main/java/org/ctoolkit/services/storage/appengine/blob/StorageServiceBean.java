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
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileMetadata;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import org.ctoolkit.services.storage.BlobInfo;
import org.ctoolkit.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Date;
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
    private static final Logger log = LoggerFactory.getLogger( StorageServiceBean.class );

    private static final GcsService gcsService = GcsServiceFactory.createGcsService();

    private AppIdentityService appIdentityService = AppIdentityServiceFactory.getAppIdentityService();

    public StorageServiceBean()
    {
    }

    @Override
    public BlobInfo store( @Nonnull byte[] data, @Nonnull String mimeType )
    {
        return store( data, mimeType, UUID.randomUUID().toString(), appIdentityService.getDefaultGcsBucketName() );
    }

    @Override
    public BlobInfo store( @Nonnull byte[] data,
                           @Nonnull String mimeType,
                           @Nonnull String fileName,
                           @Nonnull String bucketName )
    {
        checkNotNull( data );
        checkNotNull( mimeType );
        checkNotNull( fileName );
        checkNotNull( bucketName );

        BlobInfo info = null;

        try
        {
            GcsFilename gcsFileName = new GcsFilename( bucketName, fileName );

            GcsFileOptions fileOptions = new GcsFileOptions.Builder().mimeType( mimeType ).build();
            GcsOutputChannel outputChannel = gcsService.createOrReplace( gcsFileName, fileOptions );
            outputChannel.write( ByteBuffer.wrap( data ) );
            outputChannel.close();

            // populate metadata of the blob
            info = new BlobInfo();
            info.setFileName( gcsFileName.getObjectName() );
            info.setBucketName( gcsFileName.getBucketName() );
            info.setMimeType( fileOptions.getMimeType() );
            info.setLength( data.length );
            info.setLastModified( new Date() );
        }
        catch ( Exception e )
        {
            String builder = "Error has occurred while storing blob:" +
                    " Bucket name: " + bucketName +
                    " File name " + fileName +
                    " Mime Type: " + mimeType +
                    " Data length: " + data.length;

            log.error( builder, e );
        }

        return info;
    }

    private String getGcsFullName( String fileName, String bucketName )
    {
        return MessageFormat.format( "/gs/{0}/{1}", bucketName, fileName );
    }

    @Override
    public byte[] serve( @Nonnull String fileName )
    {
        return serve( fileName, appIdentityService.getDefaultGcsBucketName() );
    }

    @Override
    public byte[] serve( @Nonnull String fileName, @Nonnull String bucketName )
    {
        checkNotNull( bucketName, "In order to serve blob a bucket name must be provided." );
        checkNotNull( fileName, "In order to serve blob a file name must be provided." );

        BlobInfo metadata = getBlobInfo( fileName, bucketName );
        if ( metadata == null )
        {
            throw new IllegalArgumentException( "No GCS metadata has found fo bucket name '"
                    + bucketName + "'" + " file name '" + fileName + "'" );
        }

        byte[] data = null;

        try
        {
            GcsFilename gcsName = new GcsFilename( bucketName, fileName );
            long length = metadata.getLength();
            ByteBuffer result = ByteBuffer.allocate( Long.valueOf( length ).intValue() );

            GcsInputChannel readChannel = gcsService.openReadChannel( gcsName, 0 );
            readChannel.read( result );
            data = result.array();
        }
        catch ( IOException e )
        {
            log.error( "Error has occurred while deleting blob. Bucket name '"
                    + bucketName + "'" + " File name '" + fileName + "'", e );
        }

        return data;
    }

    @Override
    public boolean delete( @Nonnull String fileName )
    {
        return delete( fileName, appIdentityService.getDefaultGcsBucketName() );
    }

    @Override
    public boolean delete( @Nonnull String fileName, @Nonnull String bucketName )
    {
        checkNotNull( bucketName, "In order to delete blob a bucket name must be provided." );
        checkNotNull( fileName, "In order to delete blob a file name must be provided." );

        try
        {
            // delete blob object
            return gcsService.delete( new GcsFilename( bucketName, fileName ) );
        }
        catch ( IOException e )
        {
            log.error( "Error has occurred during blob deletion in storage. Bucket name '"
                    + bucketName + "'" + " File name '" + fileName + "'", e );

            return false;
        }
    }

    @Override
    public BlobInfo getBlobInfo( @Nonnull String fileName )
    {
        return getBlobInfo( fileName, appIdentityService.getDefaultGcsBucketName() );
    }

    @Override
    public BlobInfo getBlobInfo( @Nonnull String fileName, @Nonnull String bucketName )
    {
        checkNotNull( fileName, "In order to get metadata a file name must be provided." );
        checkNotNull( bucketName, "In order to get metadata a bucket name must be provided." );

        BlobInfo info = null;

        try
        {
            GcsFilename gcsName = new GcsFilename( bucketName, fileName );
            GcsFileMetadata metadata = gcsService.getMetadata( gcsName );

            if ( metadata != null )
            {
                info = new BlobInfo();
                info.setMimeType( metadata.getOptions().getMimeType() );
                info.setContentEncoding( metadata.getOptions().getContentEncoding() );
                info.setLastModified( metadata.getLastModified() );
                info.setBucketName( metadata.getFilename().getBucketName() );
                info.setFileName( metadata.getFilename().getObjectName() );
                info.setLength( metadata.getLength() );
                info.setEtag( metadata.getEtag() );
            }
        }
        catch ( IOException e )
        {
            log.error( "Error has occurred while getting GCS metadata. Bucket name '"
                    + bucketName + "'" + " File name '" + fileName + "'", e );
        }

        return info;
    }
}
