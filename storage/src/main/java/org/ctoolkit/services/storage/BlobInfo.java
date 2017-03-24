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

import java.util.Date;

/**
 * The single blob (file, document, image) record metadata.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class BlobInfo
{
    private String mimeType;

    private String contentEncoding;

    private Date lastModified;

    private String fileName;

    private String bucketName;

    private long length;

    private String etag;

    /**
     * Returns the media type of blob data representation (Content-Type).
     *
     * @return the media type
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * Sets the media type of blob data representation (Content-Type).
     *
     * @param mimeType the media type to be set
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.1.5">specification</a>
     */
    public void setMimeType( String mimeType )
    {
        this.mimeType = mimeType;
    }

    /**
     * Returns the content encoding of the blob.
     *
     * @return the content encoding
     */
    public String getContentEncoding()
    {
        return contentEncoding;
    }

    /**
     * Sets the content encoding of the blob.
     *
     * @param encoding the the content encoding to be set
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-3.1.2.2">specification</a>
     */
    public void setContentEncoding( String encoding )
    {
        this.contentEncoding = encoding;
    }

    /**
     * Returns the last modified date of the blob in storage.
     *
     * @return the last modified date
     */
    public Date getLastModified()
    {
        return lastModified;
    }

    /**
     * Sets the last modified date of the blob in storage.
     *
     * @param lastModified the last modified date to be set
     */
    public void setLastModified( Date lastModified )
    {
        this.lastModified = lastModified;
    }

    /**
     * Returns the name of the blob in storage.
     *
     * @return the name of the blob
     */
    public String getFileName()
    {
        return fileName;
    }

    /**
     * Sets the name of the blob in storage.
     *
     * @param fileName the name of the blob to be set
     */
    public void setFileName( String fileName )
    {
        this.fileName = fileName;
    }

    /**
     * Returns the name of the bucket, the container to hold data.
     *
     * @return the name of the bucket
     */
    public String getBucketName()
    {
        return bucketName;
    }

    /**
     * Sets the name of the bucket.
     *
     * @param bucketName the name of the bucket to be set
     */
    public void setBucketName( String bucketName )
    {
        this.bucketName = bucketName;
    }

    /**
     * Returns the data length of the blob.
     *
     * @return the data length
     */
    public long getLength()
    {
        return length;
    }

    /**
     * Sets the data length of the blob.
     *
     * @param length the data length to be set
     */
    public void setLength( long length )
    {
        this.length = length;
    }

    /**
     * Returns the ETag value.
     * <p>
     * A blob-tag differentiating between multiple representations of the same blob.
     * For non null values can be quickly compared to determine whether two representations
     * of a blob are the same or not.
     *
     * @return the etag value
     */
    public String getEtag()
    {
        return etag;
    }

    /**
     * Sets the ETag value.
     *
     * @param etag the etag value to be set
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">ETag specification</a>
     */
    public void setEtag( String etag )
    {
        this.etag = etag;
    }

    @Override
    public String toString()
    {
        return "BlobInfo{" +
                "mimeType='" + mimeType + '\'' +
                ", contentEncoding='" + contentEncoding + '\'' +
                ", lastModified=" + lastModified +
                ", fileName='" + fileName + '\'' +
                ", bucketName='" + bucketName + '\'' +
                ", length=" + length +
                ", etag='" + etag + '\'' +
                '}';
    }
}
