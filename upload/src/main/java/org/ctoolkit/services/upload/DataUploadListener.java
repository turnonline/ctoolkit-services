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

package org.ctoolkit.services.upload;

import com.google.appengine.api.blobstore.BlobKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A data upload callback interface used for post processing once upload data has been uploaded.
 * In order to be notified about uploads bind <code>DataUploadListener</code> in guice module as following:
 * <pre>
 * Multibinder&#60;DataUploadListener&#62; multibinder = Multibinder.newSetBinder( binder(), DataUploadListener.class );
 * multibinder.addBinding().to( DataUploadCallbackImpl.class );
 * </pre>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface DataUploadListener
{
    /**
     * Called right after data upload has done.
     *
     * @param storageName the cloud storage blob full name in form '/gs/{bucket}/{name}'
     * @param blobKey     the App Engine blobstore key reference of the uploaded file in cloud storage
     * @param imageSize   the optional image size, -1 if not requested
     * @param servingUrl  the CDN static serving URL in case of the image data, otherwise null
     * @param customName  the optional custom name
     * @param contentType the content type provided in the HTTP header during upload
     * @param filename    the file name included in the HTTP header during upload
     * @param size        the size in bytes
     * @param md5Hash     the md5 hash
     */
    void onDataUpload( @Nonnull String storageName,
                       @Nonnull BlobKey blobKey,
                       int imageSize,
                       @Nullable String servingUrl,
                       @Nullable String customName,
                       @Nonnull String contentType,
                       @Nonnull String filename,
                       long size,
                       @Nonnull String md5Hash );
}
