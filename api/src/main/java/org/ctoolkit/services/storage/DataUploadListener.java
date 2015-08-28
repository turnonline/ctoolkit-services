package org.ctoolkit.services.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A data upload callback interface used for post processing once upload data has been uploaded.
 * In order to be notified about uploads bind <code>DataUploadListener</code> in guice module as following:
 * <pre>
 * Multibinder<DataUploadListener> multibinder = Multibinder.newSetBinder( binder(), DataUploadListener.class );
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
     * @param key         the key, cloud storage name
     * @param imageSize   the optional image size, -1 if not requested
     * @param servingUrl  the serving CDN URL in case of the image data, otherwise null
     * @param customName  the optional custom name
     * @param contentType the content type provided in the HTTP header during upload
     * @param filename    the file name included in the HTTP header during upload
     * @param size        the size in bytes
     * @param md5Hash     the md5 hash
     */
    void onDataUpload( @Nonnull String key,
                       int imageSize,
                       @Nullable String servingUrl,
                       @Nullable String customName,
                       @Nonnull String contentType,
                       @Nonnull String filename,
                       long size,
                       @Nonnull String md5Hash );
}
