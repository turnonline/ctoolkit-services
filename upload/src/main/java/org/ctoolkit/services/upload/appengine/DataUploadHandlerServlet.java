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

package org.ctoolkit.services.upload.appengine;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.common.base.Charsets;
import com.google.common.net.MediaType;
import org.ctoolkit.services.upload.DataUploadHandler;
import org.ctoolkit.services.upload.DataUploadListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The data upload handler servlet AppEngine implementation. The successfully uploaded data are stored
 * in to the Cloud Storage under given bucket name (provided name or the default one).
 * To make it work correctly serve this servlet with {@link DataUploadHandler#DATA_HANDLER_UPLOAD_URL}
 * <p/>
 * Successful result returns following JSON:
 * <ul>
 * <li>storageName</li>
 * <li>blobKey</li>
 * <li>servingUrl</li>
 * <li>customName</li>
 * </ul>
 * POST parameters to serve data upload
 * <ul>
 * <li>{@link DataUploadHandler#UPLOAD_NAME_FIELD_MARKER}</li>
 * <li>{@link DataUploadHandler#PARAMETER_IMAGE_SIZE}</li>
 * <li>{@link DataUploadHandler#PARAMETER_CUSTOM_NAME}</li>
 * </ul>
 * GET parameters, to get upload URL
 * <ul>
 * <li>{@link DataUploadHandler#PARAMETER_GCS_BUCKET_NAME}</li>
 * </ul>
 * <p/>
 * In order to be notified about uploads register {@link DataUploadListener} listener via guice, example in the javadoc.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class DataUploadHandlerServlet
        extends HttpServlet
        implements DataUploadHandler
{
    private static final long serialVersionUID = 1L;

    /**
     * Part of HTTP content type header.
     */
    private static final String MULTIPART = "multipart/";

    private final Logger log = LoggerFactory.getLogger( DataUploadHandlerServlet.class );

    private final BlobstoreService blobstoreService;

    private final ImagesService imageService;

    private final AppIdentityService appIdentityService;

    private final Set<DataUploadListener> listeners;

    @Inject
    public DataUploadHandlerServlet( BlobstoreService blobstoreService,
                                     ImagesService imageService,
                                     AppIdentityService appIdentityService,
                                     Set<DataUploadListener> listeners )
    {
        this.blobstoreService = blobstoreService;
        this.imageService = imageService;
        this.appIdentityService = appIdentityService;
        this.listeners = listeners;
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
    {
        if ( !isMultipartContent( request ) )
        {
            log.warn( "Wrong content type: " + request.getContentType() );
            return;
        }

        String gStorageName;
        String imageSize;
        String customName;

        //preferred method over getBlobInfos or getUploads because uploading files to Cloud Storage
        Map<String, List<FileInfo>> fileInfos = blobstoreService.getFileInfos( request );
        List<FileInfo> list = fileInfos.get( UPLOAD_NAME_FIELD_MARKER );

        if ( list == null )
        {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );

            String message = "Incorrect upload form 'name' field, requested: " + UPLOAD_NAME_FIELD_MARKER;
            message = message + " " + String.valueOf( fileInfos );

            throw new IllegalArgumentException( message );
        }

        FileInfo info = list.get( 0 );

        gStorageName = info.getGsObjectName();
        imageSize = request.getParameter( PARAMETER_IMAGE_SIZE );
        customName = request.getParameter( PARAMETER_CUSTOM_NAME );

        if ( log.isInfoEnabled() )
        {
            log.info( "Image size: " + imageSize );
            log.info( "Custom name: " + customName );
            log.info( String.valueOf( info ) );
        }

        try
        {
            if ( !Strings.isNullOrEmpty( gStorageName ) )
            {
                int size = -1;
                if ( !Strings.isNullOrEmpty( imageSize ) )
                {
                    try
                    {
                        size = Integer.parseInt( imageSize );
                    }
                    catch ( NumberFormatException e )
                    {
                        log.warn( "Parsing of the image size has failed: " + imageSize );
                    }
                }

                String servingUrl = null;

                if ( isAnyImageContentType( info ) )
                {
                    ServingUrlOptions options;
                    options = ServingUrlOptions.Builder.withGoogleStorageFileName( gStorageName );
                    options = options.crop( false ).secureUrl( true );
                    if ( size > 0 )
                    {
                        options = options.imageSize( size );
                    }

                    servingUrl = imageService.getServingUrl( options );
                }

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                JsonObject jsonEntry = new JsonObject();
                jsonEntry.addProperty( "storageName", gStorageName );

                if ( !Strings.isNullOrEmpty( servingUrl ) )
                {
                    jsonEntry.addProperty( "servingUrl", servingUrl );
                }

                if ( !Strings.isNullOrEmpty( customName ) )
                {
                    jsonEntry.addProperty( "customName", customName );
                }

                // blob key retrieval
                Map<String, List<BlobKey>> blobs = blobstoreService.getUploads( request );
                BlobKey blobKey = blobs.get( UPLOAD_NAME_FIELD_MARKER ).get( 0 );

                if ( blobKey == null )
                {
                    response.setStatus( HttpServletResponse.SC_BAD_REQUEST );

                    String message = "Blob key is null for fileInfos: " + String.valueOf( fileInfos );
                    throw new IllegalArgumentException( message );
                }

                jsonEntry.addProperty( "blobKey", blobKey.getKeyString() );

                // return blob key and serving URL as JSON
                gson.toJson( jsonEntry, response.getWriter() );

                // notify
                for ( DataUploadListener listener : listeners )
                {
                    try
                    {
                        listener.onDataUpload( gStorageName, blobKey, size, servingUrl, customName,
                                info.getContentType(), info.getFilename(), info.getSize(), info.getMd5Hash() );
                    }
                    catch ( Exception e )
                    {
                        log.warn( "Calling of the listeners has failed, ignoring ..", e );
                    }
                }

                MediaType jsonUtf8 = MediaType.JSON_UTF_8;

                response.setStatus( HttpServletResponse.SC_CREATED );
                response.setCharacterEncoding( jsonUtf8.charset().get().name() );
                response.setContentType( jsonUtf8.toString() );
            }
            else
            {
                log.warn( "No key has found!" );
                response.setStatus( HttpServletResponse.SC_NOT_FOUND );
            }
        }
        catch ( Exception e )
        {
            log.error( "An error has occurred during uploading of the data: " + gStorageName, e );
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
    {
        response.setCharacterEncoding( Charsets.UTF_8.displayName() );

        String bucketName;
        String providedBucketName = request.getParameter( PARAMETER_GCS_BUCKET_NAME );

        if ( Strings.isNullOrEmpty( providedBucketName ) )
        {
            bucketName = appIdentityService.getDefaultGcsBucketName();
        }
        else
        {
            bucketName = providedBucketName;
        }

        if ( log.isInfoEnabled() )
        {
            log.info( "GCS bucket name: " + bucketName );
        }

        UploadOptions uploadOptions = UploadOptions.Builder.withGoogleStorageBucketName( bucketName );

        String uploadUrl = blobstoreService.createUploadUrl( DATA_HANDLER_UPLOAD_URL, uploadOptions );

        if ( log.isInfoEnabled() )
        {
            log.info( "Upload URL: " + uploadUrl );
        }

        response.getWriter().write( uploadUrl );
    }

    /**
     * The utility method that determines whether the request contains multipart content.
     *
     * @param request The servlet request to be evaluated. Must be non-null.
     * @return <code>true</code> if the request is multipart;
     * <code>false</code> otherwise.
     */
    private boolean isMultipartContent( HttpServletRequest request )
    {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith( MULTIPART );
    }

    private boolean isAnyImageContentType( FileInfo info )
    {
        String contentType = info.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith( MediaType.ANY_IMAGE_TYPE.type() );
    }
}