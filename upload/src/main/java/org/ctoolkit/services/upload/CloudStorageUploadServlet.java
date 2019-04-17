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
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

public class CloudStorageUploadServlet
        extends HttpServlet
{
    private static final Logger LOGGER = LoggerFactory.getLogger( CloudStorageUploadServlet.class );

    private static final int BUFFER_SIZE = 1024;

    private static final String __MULTIPART_CONFIG_ELEMENT = "org.eclipse.jetty.multipartConfig";

    private static final long serialVersionUID = -3618565065044722043L;

    private final Storage storage;

    private final AppIdentityService appIdentity;

    private String bucketName;

    private String directory = "Uploads";

    public CloudStorageUploadServlet( Storage storage, AppIdentityService appIdentity )
    {
        this.storage = storage;
        this.appIdentity = appIdentity;
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

        for ( Part part : parts )
        {
            String filename = fileName( part );
            BlobInfo.Builder builder = BlobInfo.newBuilder( BlobId.of( getBucketName(), filename ) );
            builder.setContentType( part.getContentType() );

            store( part, builder.build() );
        }
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
            @SuppressWarnings( "UnstableApiUsage" )
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
        return directory + "/" + fileName;
    }

    private String getBucketName()
    {
        if ( bucketName == null )
        {
            bucketName = appIdentity.getDefaultGcsBucketName();
        }
        return bucketName;
    }
}
