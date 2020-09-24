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
import mockit.Mock;
import mockit.MockUp;
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
import java.util.HashSet;
import java.util.Set;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.ctoolkit.services.upload.appengine.CloudStorageUploadServlet.RESPONSE_IMAGE_SIZE;

/**
 * {@link CloudStorageUploadServlet} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CloudStorageUploadServletTest
{
    private static final String BUCKET = "test-bucket.appspot.com";

    private static final String FILE_NAME = "nícé.jpeg";

    @Tested
    private CloudStorageUploadServlet tested;

    @Injectable
    private Storage storage;

    @Injectable
    private AppIdentityService appIdentity;

    @Injectable
    private ImagesService imageService;

    @Injectable
    private Set<StorageUploadListener> listeners;

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
        listeners = new HashSet<>();
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
                response.setContentType( "text/plain; charset=utf-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.setStatus( HttpServletResponse.SC_CREATED );

                ServingUrlOptions suo;
                imageService.getServingUrl( suo = withCapture() );
                assertThat( suo ).isNotNull();

                ServingUrlOptions expected = ServingUrlOptions.Builder
                        .withGoogleStorageFileName( "/gs/test-bucket.appspot.com/132435/uploads/nice.jpeg" )
                        .crop( false )
                        .secureUrl( true );

                assertThat( suo ).isEqualTo( expected );

                BlobInfo blobInfo;
                storage.create( blobInfo = withCapture(), ( byte[] ) any );

                assertThat( blobInfo ).isNotNull();
                assertThat( blobInfo.getBucket() ).isEqualTo( BUCKET );
                assertThat( blobInfo.getName() ).isEqualTo( accountId + "/uploads/nice.jpeg" );
            }
        };

        String expectedResponse = fromFile( "upload-response.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_ImageCreatedWithImageSize() throws ServletException, IOException, JSONException
    {
        expectationsWithParts();

        new Expectations()
        {
            {
                request.getHeader( RESPONSE_IMAGE_SIZE );
                result = 200;
            }
        };

        // test call
        long accountId = 132435L;
        tested.upload( request, response, accountId );

        new Verifications()
        {
            {
                response.setStatus( HttpServletResponse.SC_CREATED );

                ServingUrlOptions suo;
                imageService.getServingUrl( suo = withCapture() );
                assertThat( suo ).isNotNull();

                ServingUrlOptions expected = ServingUrlOptions.Builder
                        .withGoogleStorageFileName( "/gs/test-bucket.appspot.com/132435/uploads/nice.jpeg" )
                        .crop( false )
                        .imageSize( 200 )
                        .secureUrl( true );

                assertThat( suo ).isEqualTo( expected );
            }
        };

        String expectedResponse = fromFile( "upload-response.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_ExcludingStorageNamePrefix() throws ServletException, IOException, JSONException
    {
        expectationsWithParts();

        new Expectations( tested )
        {
            {
                request.getHeader( RESPONSE_IMAGE_SIZE );
                result = 200;

                tested.storageNameInclPrefix();
                result = false;
            }
        };

        // test call
        long accountId = 132435L;
        tested.upload( request, response, accountId );

        new Verifications()
        {
            {
                response.setStatus( HttpServletResponse.SC_CREATED );

                ServingUrlOptions suo;
                imageService.getServingUrl( suo = withCapture() );
                assertThat( suo ).isNotNull();

                ServingUrlOptions expected = ServingUrlOptions.Builder
                        // internally we still need the storage name to be with '/gs/' prefix
                        .withGoogleStorageFileName( "/gs/test-bucket.appspot.com/132435/uploads/nice.jpeg" )
                        .crop( false )
                        .imageSize( 200 )
                        .secureUrl( true );

                assertThat( suo ).isEqualTo( expected );
            }
        };

        String expectedResponse = fromFile( "upload-response-excl-prefix.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_IncludingFileNameTimestampPostfix() throws ServletException, IOException, JSONException
    {
        expectationsWithParts();
        expectationsInclTimestamp();

        // test call
        long accountId = 4352L;
        tested.upload( request, response, accountId );

        new Verifications()
        {
            {
                response.setStatus( HttpServletResponse.SC_CREATED );

                ServingUrlOptions suo;
                imageService.getServingUrl( suo = withCapture() );
                assertThat( suo ).isNotNull();

                ServingUrlOptions expected = ServingUrlOptions.Builder
                        // internally we still need the storage name to be with '/gs/' prefix
                        .withGoogleStorageFileName( "/gs/test-bucket.appspot.com/4352/uploads/nice-1569564533490.jpeg" )
                        .crop( false )
                        .secureUrl( true );

                assertThat( suo ).isEqualTo( expected );
            }
        };

        String expectedResponse = fromFile( "upload-response-incl-timestamp.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_DirectoryInvalidRequest() throws ServletException, IOException
    {
        new Expectations( tested )
        {
            {
                tested.uploadDirectory( request );
                result = new IllegalArgumentException( "Invalid parameter" );
            }
        };

        long accountId = 132435L;
        tested.upload( request, response, accountId );

        new Verifications()
        {
            {
                response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
            }
        };
    }

    @Test
    public void upload_ImageCreatedWithInvalidImageSize() throws ServletException, IOException
    {
        expectationsWithParts();

        new Expectations()
        {
            {
                request.getHeader( RESPONSE_IMAGE_SIZE );
                result = "123a";
            }
        };

        // test call
        long accountId = 132435L;
        tested.upload( request, response, accountId );

        new Verifications()
        {
            {
                response.setStatus( HttpServletResponse.SC_CREATED );

                ServingUrlOptions suo;
                imageService.getServingUrl( suo = withCapture() );
                assertThat( suo ).isNotNull();

                ServingUrlOptions expected = ServingUrlOptions.Builder
                        .withGoogleStorageFileName( "/gs/test-bucket.appspot.com/132435/uploads/nice.jpeg" )
                        .crop( false )
                        .secureUrl( true );

                assertThat( suo ).isEqualTo( expected );
            }
        };
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
                response.setContentType( "text/plain; charset=utf-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.setStatus( HttpServletResponse.SC_CREATED );

                imageService.getServingUrl( ( ServingUrlOptions ) any );
                times = 0;

                BlobInfo blobInfo;
                //noinspection ConstantConditions
                storage.create( blobInfo = withCapture(), ( byte[] ) any );

                assertThat( blobInfo ).isNotNull();
                assertThat( blobInfo.getBucket() ).isEqualTo( BUCKET );
                assertThat( blobInfo.getName() ).isEqualTo( "uploads/nice.jpeg" );
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
                response.setContentType( "text/plain; charset=utf-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.setStatus( HttpServletResponse.SC_CREATED );

                BlobInfo blobInfo;
                storage.create( blobInfo = withCapture(), ( byte[] ) any );

                assertThat( blobInfo ).isNotNull();
                assertThat( blobInfo.getBucket() ).isEqualTo( BUCKET );
                assertThat( blobInfo.getName() ).isEqualTo( "uploads/nice.jpeg" );
            }
        };

        String expectedResponse = fromFile( "upload-response-min.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_ListenerRegistered() throws IOException, ServletException, JSONException
    {
        expectationsWithParts();

        new Expectations( tested )
        {
            {
                tested.storageNameInclPrefix();
                result = false;
            }
        };

        listeners.add( ( request, uploads, accountId ) -> {
            assertThat( uploads ).isNotNull();

            assertWithMessage( "Number of uploaded files" )
                    .that( uploads )
                    .hasSize( 1 );

            StorageUploadListener.Metadata metadata = uploads.get( 0 );

            assertWithMessage( "Upload metadata: Blob Info" )
                    .that( metadata.getBlobInfo() )
                    .isNotNull();

            assertWithMessage( "Upload metadata: Cloud Storage Name" )
                    .that( metadata.getCloudStorageName() )
                    .isEqualTo( "/gs/test-bucket.appspot.com/132435/uploads/nice.jpeg" );

            assertWithMessage( "Upload metadata: File Name" )
                    .that( metadata.getFileName() )
                    .isEqualTo( "nice.jpeg" );

            assertWithMessage( "Upload metadata: relative path to the file incl. file name" )
                    .that( metadata.getRelativePath() )
                    .isEqualTo( "132435/uploads/nice.jpeg" );

            assertWithMessage( "Upload metadata: General Storage Name" )
                    .that( metadata.getGeneralStorageName() )
                    .isEqualTo( "test-bucket.appspot.com/132435/uploads/nice.jpeg" );

            assertWithMessage( "Upload metadata: Serving URL" )
                    .that( metadata.getServingUrl() )
                    .isEqualTo( "https://cdn.google/abc683" );

            assertWithMessage( "Account ID" )
                    .that( accountId )
                    .isEqualTo( 132435L );
        } );

        // test call
        long accountId = 132435L;
        tested.upload( request, response, accountId );

        String expectedResponse = fromFile( "upload-response-excl-prefix.json" );
        JSONAssert.assertEquals( expectedResponse, writer.toString(), JSONCompareMode.LENIENT );
    }

    @Test
    public void upload_ErrorOnRegisteredListener() throws IOException, ServletException
    {
        expectationsWithParts();

        listeners.add( ( request, uploads, accountId ) -> {
            throw new RuntimeException( "Upload listener failure" );
        } );

        // test call
        tested.upload( request, response, null );

        new Verifications()
        {
            {
                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            }
        };
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

        assertWithMessage( "Image content-type" )
                .that( tested.isAnyImageContentType( part ) )
                .isTrue();
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

        assertWithMessage( "Image content-type" )
                .that( tested.isAnyImageContentType( part ) )
                .isTrue();
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

        assertWithMessage( "Image content-type" )
                .that( tested.isAnyImageContentType( part ) )
                .isTrue();
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

        assertWithMessage( "Image content-type" )
                .that( tested.isAnyImageContentType( part ) )
                .isTrue();
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

        assertWithMessage( "Image content-type" )
                .that( tested.isAnyImageContentType( part ) )
                .isFalse();
    }

    @Test
    public void fileNameInclTimestamp_UUID()
    {
        new Expectations()
        {
            {
                part.getSubmittedFileName();
                result = null;
            }
        };

        assertWithMessage( "UUID based file name expected" )
                .that( tested.fileName( part ) )
                .hasLength( 36 );
    }

    @Test
    public void fileNameInclTimestamp_SimpleName()
    {
        expectationsInclTimestamp( "nice.jpeg" );

        assertWithMessage( "File name incl. timestamp as postfix" )
                .that( tested.fileName( part ) )
                .isEqualTo( "nice-1569564533490.jpeg" );
    }

    @Test
    public void fileNameInclTimestamp_NameWithSpace()
    {
        expectationsInclTimestamp( "nice image.png" );

        assertWithMessage( "File name incl. timestamp as postfix" )
                .that( tested.fileName( part ) )
                .isEqualTo( "nice image-1569564533490.png" );
    }

    @Test
    public void fileNameInclTimestamp_CamelCaseName()
    {
        expectationsInclTimestamp( "Ugly Image.svg" );

        assertWithMessage( "File name incl. timestamp as postfix" )
                .that( tested.fileName( part ) )
                .isEqualTo( "Ugly Image-1569564533490.svg" );
    }

    @Test
    public void fileNameInclTimestamp_NameWithNumbers()
    {
        expectationsInclTimestamp( "Image123.bla" );

        assertWithMessage( "File name incl. timestamp as postfix" )
                .that( tested.fileName( part ) )
                .isEqualTo( "Image123-1569564533490.bla" );
    }

    @Test
    public void fileNameInclTimestamp_NameWhatever()
    {
        expectationsInclTimestamp( "Image-@.whatever" );

        assertWithMessage( "File name incl. timestamp as postfix" )
                .that( tested.fileName( part ) )
                .isEqualTo( "Image-@-1569564533490.whatever" );
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

    private void expectationsInclTimestamp()
    {
        expectationsInclTimestamp( FILE_NAME );
    }

    private void expectationsInclTimestamp( String fileName )
    {
        new Expectations( tested )
        {
            {
                tested.fileNameInclTimestamp();
                result = true;

                part.getSubmittedFileName();
                result = fileName;
            }
        };

        new MockUp<System>()
        {
            @Mock
            public long currentTimeMillis()
            {
                return 1569564533490L;
            }
        };
    }

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