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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;

import javax.annotation.Nonnull;

/**
 * The storage service API as an extension to {@link Storage} with set of convenient methods.
 * If you need something else simply use directly {@link Storage} instance.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface StorageService
{
    /**
     * Parse and create {@link BlobId} instance as an unique blob document identification within cloud storage.
     *
     * @param fullName the cloud storage blob full name in form '/gs/{bucket}/{name}'
     * @return the unique blob document identification
     * @throws IllegalArgumentException if given argument does not follow expected pattern
     */
    BlobId createBlobId( @Nonnull String fullName );

    /**
     * Extract and return full storage name for given blob in form '/gs/{bucket}/{name}'.
     *
     * @param blob the cloud storage blob instance
     * @return the full storage name
     */
    String getFullStorageName( @Nonnull Blob blob );

    /**
     * Creates the App Engine blobstore key reference of the file referenced by cloud storage blob full name.
     *
     * @param fullName the cloud storage blob full name in form '/gs/{bucket}/{name}'
     * @return the App Engine blobstore key reference
     * @throws IllegalArgumentException in case of invalid format of the storage full name
     */
    BlobKey createBlobKey( @Nonnull String fullName );

    /**
     * Creates the App Engine blobstore key reference of the file represented by given blob.
     *
     * @param blob the cloud storage blob instance
     * @return the App Engine blobstore key reference
     */
    BlobKey createBlobKey( @Nonnull Blob blob );

    /**
     * Store data into storage and return cloud storage object.
     * The file name will be generated (UUID) and stored within default bucket container.
     *
     * @param data        the array of bytes of the blob document to be stored
     * @param contentType the media type of the data representation
     * @return the file record metadata
     */
    Blob store( @Nonnull byte[] data, @Nonnull String contentType );

    /**
     * Store data into storage under default bucket container and return cloud storage object.
     *
     * @param data        the array of bytes of the blob document to be stored
     * @param contentType the media type of the data representation
     * @param blobName    the intended name of the blob file in storage
     * @return the file record metadata
     */
    Blob store( @Nonnull byte[] data,
                @Nonnull String contentType,
                @Nonnull String blobName );

    /**
     * Store data into storage and return cloud storage object.
     *
     * @param data        the array of bytes of the blob document to be stored
     * @param contentType the media type of the data representation
     * @param bucketName  the name of the bucket, the specified or default container to hold given data
     * @param blobName    the intended name of the blob file in storage
     * @return the file record metadata
     */
    Blob store( @Nonnull byte[] data,
                @Nonnull String contentType,
                @Nonnull String bucketName,
                @Nonnull String blobName );

    /**
     * Returns the blob document from bucket container.
     *
     * @param fullName the full name of the blob stored in the cloud storage in form '/gs/{bucket}/{name}'
     * @return the array of bytes of the blob
     * @throws IllegalArgumentException in case of invalid format of the storage full name
     */
    byte[] readByFullStorageName( @Nonnull String fullName );

    /**
     * Returns the blob document from bucket container.
     *
     * @param blobId the unique blob identification to retrieve desired blob
     * @return the array of bytes of the blob
     */
    byte[] read( @Nonnull BlobId blobId );

    /**
     * Returns the blob document from default bucket container.
     *
     * @param blobName the name of the blob document in storage to get
     * @return the array of bytes of the blob
     */
    byte[] read( @Nonnull String blobName );

    /**
     * Returns the blob document.
     *
     * @param bucketName the name of the bucket container
     * @param blobName   the name of the blob document in storage to get
     * @return the array of bytes of the blob
     */
    byte[] read( @Nonnull String bucketName, @Nonnull String blobName );

    /**
     * Delete the blob document from bucket container.
     *
     * @param blobId the unique blob identification to delete desired blob
     * @return true if blob document has been deleted, false if not found
     */
    boolean delete( @Nonnull BlobId blobId );

    /**
     * Delete the blob document from default bucket container.
     *
     * @param blobName the name of the blob document in storage to delete
     * @return true if blob document has been deleted, false if not found
     */
    boolean delete( @Nonnull String blobName );

    /**
     * Delete the blob document.
     *
     * @param bucketName the name of the bucket container
     * @param blobName   the name of the blob document in storage to delete
     * @return true if blob document has been deleted, false if not found
     */
    boolean delete( @Nonnull String bucketName, @Nonnull String blobName );

    /**
     * Returns the secure (SSL) CDN static serving URL.
     * The name must represent image otherwise exception will be thrown.
     *
     * @param fullName the full name of the image stored in the cloud storage in form '/gs/{bucket}/{name}'
     * @return the CDN static URL
     */
    String getSecureServingUrl( @Nonnull String fullName );

    /**
     * Returns the secure (SSL) CDN static serving URL for specific image size.
     * The name must represent image otherwise exception will be thrown.
     *
     * @param fullName  the full name of the image stored in the cloud storage in form '/gs/{bucket}/{name}'
     * @param imageSize the desired image size between 0 and 1600 (including).
     * @return the CDN static URL
     */
    String getSecureServingUrl( @Nonnull String fullName, int imageSize );

    /**
     * Same as {@link #getSecureServingUrl(BlobKey, int)}  without image size argument.
     *
     * @param blobKey the App Engine blobstore key reference of the uploaded file in cloud storage
     * @return the CDN static URL
     */
    String getSecureServingUrl( @Nonnull BlobKey blobKey );

    /**
     * Returns the secure (SSL) CDN static serving URL for specific image size.
     * The name must represent image otherwise exception will be thrown.
     * <p>
     * If blob key {@link BlobKey} is available, call to retrieve serving URL
     * {@link com.google.appengine.api.images.ImagesService#getServingUrl(ServingUrlOptions)}
     * will save one remote call  {@link com.google.appengine.api.blobstore.BlobstoreService#createGsBlobKey(String)}
     * in order to create that {@link BlobKey} instance.
     *
     * @param blobKey   the App Engine blobstore key reference of the uploaded file in cloud storage
     * @param imageSize the desired image size between 0 and 1600 (including).
     * @return the CDN static URL
     */
    String getSecureServingUrl( @Nonnull BlobKey blobKey, int imageSize );
}
