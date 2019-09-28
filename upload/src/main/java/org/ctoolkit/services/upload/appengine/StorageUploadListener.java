/*
 * Copyright (c) 2019 Comvai, s.r.o. All Rights Reserved.
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

package org.ctoolkit.services.upload.appengine;

import com.google.cloud.storage.BlobInfo;
import com.google.common.base.MoreObjects;
import org.ctoolkit.services.storage.StorageService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;

/**
 * A data upload callback interface used for post processing once upload data has been uploaded.
 * In order to be notified about uploads bind <code>StorageUploadListener</code> in guice module as following:
 * <pre>
 * Multibinder&#60;StorageUploadListener&#62; multibinder = Multibinder.newSetBinder( binder(), StorageUploadListener.class );
 * multibinder.addBinding().to( MyStorageUploadCallbackImpl.class );
 * </pre>
 * {@link StorageService#GOOGLE_STORAGE_NAME_PATTERN}
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public interface StorageUploadListener
{
    /**
     * Called right after binary files has been uploaded into Cloud Storage.
     *
     * @param request   the current HTTP request
     * @param uploads   the list of metadata of uploaded files
     * @param accountId the account identification of the verified account
     */
    void onStorageUpload( @Nonnull HttpServletRequest request,
                          @Nonnull List<Metadata> uploads,
                          @Nullable Long accountId );

    @SuppressWarnings( "WeakerAccess" )
    class Metadata
            implements Serializable
    {
        private static final long serialVersionUID = 382970411903226244L;

        private BlobInfo blobInfo;

        private String fileName;

        private String relativePath;

        private String cloudStorageName;

        private String generalStorageName;

        private String servingUrl;

        private Metadata( BlobInfo blobInfo,
                          String fileName,
                          String relativePath,
                          String cloudStorageName,
                          String generalStorageName,
                          String servingUrl )
        {
            this.blobInfo = blobInfo;
            this.fileName = fileName;
            this.relativePath = relativePath;
            this.cloudStorageName = cloudStorageName;
            this.generalStorageName = generalStorageName;
            this.servingUrl = servingUrl;
        }

        /**
         * The Cloud Storage object metadata, a reference of the uploaded file in cloud storage.
         */
        public BlobInfo getBlobInfo()
        {
            return blobInfo;
        }

        /**
         * The file name of the uploaded binary in Cloud Storage.
         */
        public String getFileName()
        {
            return fileName;
        }

        /**
         * The path of the the uploaded binary relative to the Cloud Storage bucket.
         */
        public String getRelativePath()
        {
            return relativePath;
        }

        /**
         * The Cloud Storage binary full name in form {@link StorageService#GOOGLE_STORAGE_NAME_PATTERN}
         */
        public String getCloudStorageName()
        {
            return cloudStorageName;
        }

        /**
         * The binary unique and full storage name as it is publicly visible.
         */
        public String getGeneralStorageName()
        {
            return generalStorageName;
        }

        /**
         * The CDN static serving URL in case of the image data, otherwise {@code null}
         */
        public String getServingUrl()
        {
            return servingUrl;
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper( this )
                    .add( "blobInfo", blobInfo )
                    .add( "fileName", fileName )
                    .add( "cloudStorageName", cloudStorageName )
                    .add( "generalStorageName", generalStorageName )
                    .add( "servingUrl", servingUrl )
                    .toString();
        }

        public static class Builder
        {
            private BlobInfo blobInfo;

            private String fileName;

            private String path;

            private String cloudStorageName;

            private String generalStorageName;

            private String servingUrl;

            public Builder blobInfo( BlobInfo blobInfo )
            {
                this.blobInfo = blobInfo;
                return this;
            }

            public Builder fileName( String fileName )
            {
                this.fileName = fileName;
                return this;
            }

            public Builder relativePath( String relativePath )
            {
                this.path = relativePath;
                return this;
            }

            public Builder cloudStorageName( String cloudStorageName )
            {
                this.cloudStorageName = cloudStorageName;
                return this;
            }

            public Builder generalStorageName( String generalStorageName )
            {
                this.generalStorageName = generalStorageName;
                return this;
            }

            public Builder servingUrl( String servingUrl )
            {
                this.servingUrl = servingUrl;
                return this;
            }

            public Metadata build()
            {
                return new Metadata( blobInfo, fileName, path, cloudStorageName, generalStorageName, servingUrl );
            }
        }
    }
}
