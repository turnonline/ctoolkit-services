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

package org.ctoolkit.services.upload;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.UUID;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CACHE_CONTROL;
import static com.google.common.net.HttpHeaders.X_REQUESTED_WITH;
import static org.ctoolkit.services.storage.appengine.blob.StorageServiceBean.STORAGE_NAME_PATTERN;

/**
 * Servlet handling uploads in to Google Cloud Storage.
 * <p>
 * <strong>Example of the Guice module configuration:</strong>
 * <p>
 * {@code CloudStorageUploadServletMultipartConfig} represents a class that extends {@link CloudStorageUploadServlet}
 * annotated with customized {@code @MultipartConfig} configuration.
 * <pre>
 * public class MyServletModule
 *         extends ServletModule
 * {
 *    &#64;Override
 *    protected void configureServlets()
 *    {
 *        String pattern = "/api/billing/v1/storage-upload";
 *        serve( pattern ).with( CloudStorageUploadServletMultipartConfig.class );
 *
 *        filter( pattern ).through( CloudStorageUploadServlet.AccessControl.class );
 *    }
 * }
 * </pre>
 * Servlet call results in a JSON response with following structure:
 * <pre>
 * {
 *     "items": [
 *         {
 *             "fileName": "my.jpeg",
 *             "storageName": "/gs/my-default-bucket.appspot.com/uploads/nice.jpeg",
 *             "servingUrl": "https://lh3.googleusercontent.com/abc683.."
 *         }
 *     ]
 * }
 * </pre>
 * <strong>Note</strong>, if target servlet is running in non default service, enrich your
 * dispatch.yaml with following, for example:
 * <pre>
 *   - url: '*&#47;billing/v1/storage-upload/*'
 *     module: my-billing
 * </pre>
 */
@SuppressWarnings( "UnstableApiUsage" )
public class CloudStorageUploadServlet
        extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger( CloudStorageUploadServlet.class );

    private static final int BUFFER_SIZE = 1024;

    private static final String __MULTIPART_CONFIG_ELEMENT = "org.eclipse.jetty.multipartConfig";

    private static final long serialVersionUID = -3618565065044722043L;

    private final Storage storage;

    private final AppIdentityService appIdentity;

    private final ImagesService imageService;

    private String bucketName;

    private String directory = "uploads";

    public CloudStorageUploadServlet( Storage storage,
                                      AppIdentityService appIdentity,
                                      ImagesService imageService )
    {
        this.storage = storage;
        this.appIdentity = appIdentity;
        this.imageService = imageService;
    }

    @Override
    public void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        //Workaround to support Guice Servlet 3 multipart https://github.com/google/guice/issues/898
        if ( "POST".equals( request.getMethod() ) )
        {
            request.setAttribute( __MULTIPART_CONFIG_ELEMENT, new MultipartConfigElement( "" ) );
        }
        super.service( request, response );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
    {
        Collection<Part> parts = request.getParts();
        LOGGER.info( "Parts to be stored: " + parts );

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson json = gsonBuilder.create();

        JsonObject root = new JsonObject();
        JsonArray items = new JsonArray();
        root.add( "items", items );
        JsonObject jsonEntry;

        for ( Part part : parts )
        {
            String filename = fileName( part );
            String fullPath = directory + "/" + filename;
            BlobInfo.Builder builder = BlobInfo.newBuilder( BlobId.of( getBucketName(), fullPath ) );
            builder.setContentType( part.getContentType() );

            BlobInfo blobInfo = builder.build();

            try
            {
                store( part, blobInfo );
            }
            catch ( Exception e )
            {
                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                return;
            }

            String gStorageName = MessageFormat.format(
                    STORAGE_NAME_PATTERN,
                    blobInfo.getBucket(),
                    blobInfo.getName() );

            jsonEntry = new JsonObject();
            jsonEntry.addProperty( "storageName", gStorageName );
            jsonEntry.addProperty( "fileName", filename );

            String servingUrl;

            if ( isAnyImageContentType( part ) )
            {
                ServingUrlOptions options = ServingUrlOptions.Builder
                        .withGoogleStorageFileName( gStorageName )
                        .crop( false )
                        .secureUrl( true );

                options.imageSize( 200 );

                try
                {
                    servingUrl = imageService.getServingUrl( options );
                    jsonEntry.addProperty( "servingUrl", servingUrl );
                }
                catch ( Exception e )
                {
                    // continue without 'servingUrl' property
                    LOGGER.error( "Get serving URL failure " + options, e );
                }
            }

            items.add( jsonEntry );
        }

        json.toJson( root, response.getWriter() );
        MediaType jsonUtf8 = MediaType.JSON_UTF_8;

        response.setStatus( HttpServletResponse.SC_CREATED );
        //noinspection OptionalGetWithoutIsPresent
        response.setCharacterEncoding( jsonUtf8.charset().get().name() );
        response.setContentType( jsonUtf8.toString() );
    }

    private void store( Part uploaded, BlobInfo blobInfo ) throws IOException
    {
        if ( uploaded.getSize() > 1_000_000 )
        {
            // 1MB or more it's recommended to write it in chunks via the blob's channel writer.
            try ( WriteChannel writer = storage.writer( blobInfo ) )
            {
                byte[] buffer = new byte[BUFFER_SIZE];
                try ( InputStream input = uploaded.getInputStream() )
                {
                    int limit;
                    while ( ( limit = input.read( buffer ) ) >= 0 )
                    {
                        try
                        {
                            writer.write( ByteBuffer.wrap( buffer, 0, limit ) );
                        }
                        catch ( Exception e )
                        {
                            LOGGER.error( blobInfo.toString(), e );
                        }
                    }
                }
            }
        }
        else
        {
            byte[] bytes = ByteStreams.toByteArray( uploaded.getInputStream() );
            storage.create( blobInfo, bytes );
        }
    }

    private String fileName( final Part part )
    {
        String fileName = part.getSubmittedFileName();
        if ( Strings.isNullOrEmpty( fileName ) )
        {
            fileName = UUID.randomUUID().toString();
        }
        return fileName;
    }

    /**
     * Returns the bucket name to be used as a target storage location.
     * By default it returns project default bucket name, override to be customized.
     *
     * @return the bucket name
     */
    protected String getBucketName()
    {
        if ( bucketName == null )
        {
            bucketName = appIdentity.getDefaultGcsBucketName();
        }
        return bucketName;
    }

    boolean isAnyImageContentType( Part info )
    {
        String contentType = info.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith( MediaType.ANY_IMAGE_TYPE.type() );
    }

    /**
     * These headers are added:
     * <ul>
     * <li>{@link HttpHeaders#ACCESS_CONTROL_ALLOW_ORIGIN}: *</li>
     * <li>{@link HttpHeaders#ACCESS_CONTROL_ALLOW_METHODS}: OPTIONS,POST</li>
     * <li>{@link HttpHeaders#ACCESS_CONTROL_ALLOW_HEADERS}: Cache-Control,X-Requested-With</li>
     * </ul>
     */
    @Singleton
    public static class AccessControl
            implements Filter
    {
        @Override
        public void init( FilterConfig filterConfig )
        {
        }

        @Override
        public void doFilter( ServletRequest request, ServletResponse servletResponse, FilterChain chain )
                throws IOException, ServletException
        {
            HttpServletResponse response = ( HttpServletResponse ) servletResponse;

            response.addHeader( ACCESS_CONTROL_ALLOW_ORIGIN, "*" );
            response.addHeader( ACCESS_CONTROL_ALLOW_METHODS, "OPTIONS,POST" );
            response.addHeader( ACCESS_CONTROL_ALLOW_HEADERS, CACHE_CONTROL + "," + X_REQUESTED_WITH );
            chain.doFilter( request, response );
        }

        @Override
        public void destroy()
        {
        }
    }
}
