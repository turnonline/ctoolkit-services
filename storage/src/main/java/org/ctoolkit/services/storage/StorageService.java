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

import com.google.cloud.storage.Blob;

import javax.annotation.Nonnull;

/**
 * The service API to store and manage large objects (blobs) up to TBs in size.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface StorageService
{
    /**
     * Store data into storage and return related metadata.
     * The file name will be generated and stored within default bucket container.
     *
     * @param data        the array of bytes of the file (blob document) to be stored
     * @param contentType the media type of the data representation
     * @return the file record metadata
     */
    Blob store( @Nonnull byte[] data, @Nonnull String contentType );

    /**
     * Store data into storage and return related metadata.
     *
     * @param data        the array of bytes of the file (blob document) to be stored
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
     * Return the file (blob document) from default bucket container.
     *
     * @param blobName the name of the file (blob document) in storage to get
     * @return the array of bytes of the file
     */
    byte[] readAllBytes( @Nonnull String blobName );

    /**
     * Return the file (blob document).
     *
     * @param bucketName the name of the bucket container
     * @param blobName   the name of the file (blob document) in storage to get
     * @return the array of bytes of the file
     */
    byte[] readAllBytes( @Nonnull String bucketName, @Nonnull String blobName );

    /**
     * Delete the file (blob document) from default bucket container.
     *
     * @param blobName the name of the file (blob document) in storage to delete
     * @return true if file (blob document) has been deleted, false if not found
     */
    boolean delete( @Nonnull String blobName );

    /**
     * Delete the file (blob document).
     *
     * @param bucketName the name of the bucket container
     * @param blobName   the name of the file (blob document) in storage to delete
     * @return true if file (blob document) has been deleted, false if not found
     */
    boolean delete( @Nonnull String bucketName, @Nonnull String blobName );
}
