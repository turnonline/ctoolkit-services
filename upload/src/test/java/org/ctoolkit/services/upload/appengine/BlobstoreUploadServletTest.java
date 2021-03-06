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
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.ctoolkit.services.upload.DataUploadHandler;
import org.ctoolkit.services.upload.DataUploadListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;

/**
 * Testing of {@link BlobstoreUploadServlet}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class BlobstoreUploadServletTest
{
    private static final String STORAGE_NAME = "StorageKey-1239j";

    private static final String BLOB_KEY = "blob-key-1239j";

    private static final String SERVING_URL = "https://www.ctoolkit.org/logo.png";

    private static final String UPLOAD_URL = "https://www.ctoolkit.org/_ah/upload";

    private static final String ARBITRARY_NAME = "my arbitrary xy name";

    @Tested
    private BlobstoreUploadServlet tested;

    @Injectable
    private BlobstoreService blobstoreService;

    @Injectable
    private ImagesService imageService;

    @Injectable
    private AppIdentityService appIdentityService;

    @Injectable
    private Set<DataUploadListener> listeners = new HashSet<>();

    @Mocked
    private DataUploadListener listener;

    @Mocked
    private HttpServletRequest request;

    @Mocked
    private HttpServletResponse response;

    @Mocked
    private FileInfo fileInfo;

    @Mocked
    private BlobKey blobKey;

    @BeforeMethod
    public void setUp()
    {
        listeners.add( listener );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doPostWrongFieldName()
    {
        final Map<String, List<FileInfo>> infos = new HashMap<>();
        infos.put( "__wrong_field_name", Collections.singletonList( fileInfo ) );

        new Expectations()
        {
            {
                request.getContentType();
                result = "MULTIpart/mixed";

                blobstoreService.getFileInfos( request );
                result = infos;
            }
        };

        tested.doPost( request, response );
    }

    @Test
    public void doPost()
            throws Exception
    {
        final Map<String, List<FileInfo>> infos = new HashMap<>();
        final StringWriter writer = new StringWriter();
        final int reqImageSize = 125;
        final String ct = "image/jpg";

        infos.put( DataUploadHandler.UPLOAD_NAME_FIELD_MARKER, Collections.singletonList( fileInfo ) );

        final Map<String, List<BlobKey>> blobs = new HashMap<>();
        blobs.put( DataUploadHandler.UPLOAD_NAME_FIELD_MARKER, Collections.singletonList( blobKey ) );

        new Expectations()
        {
            {
                blobstoreService.getFileInfos( request );
                result = infos;

                blobstoreService.getUploads( request );
                result = blobs;

                blobKey.getKeyString();
                result = BLOB_KEY;

                request.getParameter( DataUploadHandler.PARAMETER_IMAGE_SIZE );
                result = "125";

                request.getParameter( DataUploadHandler.PARAMETER_CUSTOM_NAME );
                result = ARBITRARY_NAME;

                request.getContentType();
                result = "MULTIpart/mixed";

                imageService.getServingUrl( ( ServingUrlOptions ) any );
                result = SERVING_URL;

                response.getWriter();
                result = new PrintWriter( writer );

                listener.onDataUpload( STORAGE_NAME, blobKey, reqImageSize, SERVING_URL, ARBITRARY_NAME, ct, "a", 9, "b" );
                result = new Exception( "This exception needs to be logged only as a WARNING." );

                fileInfo.getContentType();
                result = ct;

                fileInfo.getFilename();
                result = "a";

                fileInfo.getSize();
                result = 9L;

                fileInfo.getMd5Hash();
                result = "b";

                fileInfo.getGsObjectName();
                result = STORAGE_NAME;
            }
        };

        tested.doPost( request, response );

        Gson gson = new GsonBuilder().create();
        JsonOutput output = gson.fromJson( writer.getBuffer().toString(), JsonOutput.class );

        assertEquals( STORAGE_NAME, output.storageName );
        assertEquals( BLOB_KEY, output.blobKey );
        assertEquals( SERVING_URL, output.servingUrl );
        assertEquals( ARBITRARY_NAME, output.customName );

        new Verifications()
        {
            {
                listener.onDataUpload( STORAGE_NAME, blobKey, reqImageSize, SERVING_URL, ARBITRARY_NAME, ct, "a", 9, "b" );
                times = 1;

                response.setContentType( "application/json; charset=utf-8" );
                response.setCharacterEncoding( "UTF-8" );
                response.setStatus( HttpServletResponse.SC_CREATED );

                ServingUrlOptions suo;
                imageService.getServingUrl( suo = withCapture() );
                // no getters thus at least printed
                System.out.println( suo );
            }
        };
    }

    @Test
    public void doGet()
            throws Exception
    {
        final StringWriter writer = new StringWriter();

        new Expectations()
        {
            {
                blobstoreService.createUploadUrl( anyString, ( UploadOptions ) any );
                result = UPLOAD_URL;

                response.getWriter();
                result = new PrintWriter( writer );
            }
        };

        tested.doGet( request, response );

        assertEquals( UPLOAD_URL, writer.toString() );

        new Verifications()
        {
            {
                response.setCharacterEncoding( "UTF-8" );
            }
        };
    }

    @SuppressWarnings( "unused" )
    private static class JsonOutput
    {
        private String storageName;

        private String blobKey;

        private String servingUrl;

        private String customName;
    }
}