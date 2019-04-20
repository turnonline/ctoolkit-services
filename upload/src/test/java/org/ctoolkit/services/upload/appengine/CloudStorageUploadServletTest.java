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

import com.google.api.client.util.Charsets;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.common.io.CharStreams;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import static com.google.common.truth.Truth.assertThat;

/**
 * {@link CloudStorageUploadServlet} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CloudStorageUploadServletTest
{
    private static final String BUCKET = "test-bucket.appspot.com";

    private static final String FILE_NAME = "nice.jpeg";

    @Tested
    private CloudStorageUploadServlet tested;

    @Injectable
    private Storage storage;

    @Injectable
    private AppIdentityService appIdentity;

    @Injectable
    private ImagesService imageService;

    @Mocked
    private HttpServletRequest request;

    @Mocked
    private HttpServletResponse response;

    @Mocked
    private Part part;

    private StringWriter writer;

    @BeforeMethod
    public void before()
    {
        writer = new StringWriter();
    }

    @Test
    public void upload_ImageCreated() throws ServletException, IOException, JSONException
    {
        expectationsWithParts();

        // test call
        long accountId = 132435L;
        tested.upload( request, response, accountId );

        new Verifications()
        {
            {
                response.setContentType( "application/json; charset=utf-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.setStatus( HttpServletResponse.SC_CREATED );

                ServingUrlOptions suo;
                imageService.getServingUrl( suo = withCapture() );
                assertThat( suo ).isNotNull();

                BlobInfo blobInfo;
                storage.create( blobInfo = withCapture(), ( byte[] ) any );

                assertThat( blobInfo ).isNotNull();
                assertThat( blobInfo.getBucket() ).isEqualTo( BUCKET );
                assertThat( blobInfo.getName() ).isEqualTo( accountId + "/uploads/" + FILE_NAME );
            }
        };

        String expectedResponse = fromFile( "upload-response.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_CreatedWithUnknownContentType() throws ServletException, IOException, JSONException
    {
        // no content-type
        expectationsWithParts( null );

        // test call
        tested.doPost( request, response );

        new Verifications()
        {
            {
                response.setContentType( "application/json; charset=utf-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.setStatus( HttpServletResponse.SC_CREATED );

                imageService.getServingUrl( ( ServingUrlOptions ) any );
                times = 0;

                BlobInfo blobInfo;
                //noinspection ConstantConditions
                storage.create( blobInfo = withCapture(), ( byte[] ) any );

                assertThat( blobInfo ).isNotNull();
                assertThat( blobInfo.getBucket() ).isEqualTo( BUCKET );
                assertThat( blobInfo.getName() ).isEqualTo( "uploads/" + FILE_NAME );
            }
        };

        String expectedResponse = fromFile( "upload-response-min.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_StorageFailure() throws ServletException, IOException
    {
        expectationsWithParts();

        new Expectations()
        {
            {
                //noinspection ConstantConditions
                storage.create( ( BlobInfo ) any, ( byte[] ) any );
                result = new RuntimeException();
            }
        };

        // test call
        tested.doPost( request, response );

        new Verifications()
        {
            {
                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            }
        };
    }

    @Test
    public void upload_ImageCreatedServeUrlFailure() throws ServletException, IOException, JSONException
    {
        expectationsWithParts();

        new Expectations()
        {
            {
                imageService.getServingUrl( ( ServingUrlOptions ) any );
                result = new RuntimeException();
            }
        };

        // test call
        tested.doPost( request, response );

        new Verifications()
        {
            {
                response.setContentType( "application/json; charset=utf-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.setStatus( HttpServletResponse.SC_CREATED );

                BlobInfo blobInfo;
                storage.create( blobInfo = withCapture(), ( byte[] ) any );

                assertThat( blobInfo ).isNotNull();
                assertThat( blobInfo.getBucket() ).isEqualTo( BUCKET );
                assertThat( blobInfo.getName() ).isEqualTo( "uploads/" + FILE_NAME );
            }
        };

        String expectedResponse = fromFile( "upload-response-min.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void isAnyImageContentType_Jpeg()
    {
        new Expectations()
        {
            {
                part.getContentType();
                result = "image/jpeg";
            }
        };

        assertThat( tested.isAnyImageContentType( part ) ).named( "Image content-type" ).isTrue();
    }

    @Test
    public void isAnyImageContentType_Gif()
    {
        new Expectations()
        {
            {
                part.getContentType();
                result = "image/gif";
            }
        };

        assertThat( tested.isAnyImageContentType( part ) ).named( "Image content-type" ).isTrue();
    }

    @Test
    public void isAnyImageContentType_Png()
    {
        new Expectations()
        {
            {
                part.getContentType();
                result = "image/png";
            }
        };

        assertThat( tested.isAnyImageContentType( part ) ).named( "Image content-type" ).isTrue();
    }

    @Test
    public void isAnyImageContentType_Tiff()
    {
        new Expectations()
        {
            {
                part.getContentType();
                result = "image/tiff";
            }
        };

        assertThat( tested.isAnyImageContentType( part ) ).named( "Image content-type" ).isTrue();
    }

    @Test
    public void isAnyImageContentType_NonImage()
    {
        new Expectations()
        {
            {
                part.getContentType();
                result = "application/msword";
            }
        };

        assertThat( tested.isAnyImageContentType( part ) ).named( "Image content-type" ).isFalse();
    }

    private void expectationsWithParts() throws IOException, ServletException
    {
        expectationsWithParts( "image/jpeg" );
    }

    private void expectationsWithParts( String contentType )
            throws IOException, ServletException
    {
        Collection<Part> parts = new ArrayList<>();
        parts.add( part );

        new Expectations()
        {
            {
                part.getInputStream();
                result = new ByteArrayInputStream( new byte[0] );

                part.getContentType();
                result = contentType;

                part.getSubmittedFileName();
                result = FILE_NAME;

                request.getParts();
                result = parts;

                appIdentity.getDefaultGcsBucketName();
                result = BUCKET;

                response.getWriter();
                result = new PrintWriter( writer );
                minTimes = 0;

                imageService.getServingUrl( ( ServingUrlOptions ) any );
                result = "https://cdn.google/abc683";
                minTimes = 0;
            }
        };
    }

    @SuppressWarnings( "UnstableApiUsage" )
    private String fromFile( String fileName ) throws IOException
    {
        InputStream stream = this.getClass().getResourceAsStream( fileName );
        if ( stream == null )
        {
            String msg = fileName + " file has not been found in " + this.getClass().getPackage() + ".";
            throw new IllegalArgumentException( msg );
        }
        return CharStreams.toString( new InputStreamReader( stream, Charsets.UTF_8 ) );
    }
}