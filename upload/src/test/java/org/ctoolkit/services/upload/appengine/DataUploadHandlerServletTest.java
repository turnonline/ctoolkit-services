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
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.FileInfo;
import com.google.appengine.api.blobstore.UploadOptions;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.GsonBuilder;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import org.ctoolkit.services.storage.DataUploadHandler;
import org.ctoolkit.services.storage.DataUploadListener;
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
 * Testing of {@link DataUploadHandlerServlet}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class DataUploadHandlerServletTest
{

    private static final String KEY = "StorageKey-1239j";

    private static final String SERVING_URL = "https://www.ctoolkit.org/logo.png";

    private static final String UPLOAD_URL = "https://www.ctoolkit.org/_ah/upload";

    private static final String ARBITRARY_NAME = "my arbitrary xy name";

    @Tested
    private DataUploadHandlerServlet tested;

    @Injectable
    private BlobstoreService blobstoreService;

    @Injectable
    private ImagesService imageService;

    @Injectable
    private AppIdentityService appIdentityService;

    @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
    @Injectable
    private Set<DataUploadListener> listeners = new HashSet<>();

    @Mocked
    private DataUploadListener listener;

    @BeforeMethod
    public void setUp() throws Exception
    {
        listeners.add( listener );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doPostWrongFieldName( @Mocked final HttpServletRequest request,
                                      @Mocked final HttpServletResponse response,
                                      @Mocked final FileInfo fileInfo )
            throws Exception
    {
        final Map<String, List<FileInfo>> infos = new HashMap<>();
        infos.put( "__wrong_field_name", Collections.singletonList( fileInfo ) );

        new NonStrictExpectations()
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
    public void doPost( @Mocked final HttpServletRequest request,
                        @Mocked final HttpServletResponse response,
                        @Mocked final FileInfo fileInfo )
            throws Exception
    {
        final Map<String, List<FileInfo>> infos = new HashMap<>();
        final StringWriter writer = new StringWriter();
        final int reqImageSize = 125;
        final String ct = "image/jpg";

        infos.put( DataUploadHandler.UPLOAD_NAME_FIELD_MARKER, Collections.singletonList( fileInfo ) );

        new NonStrictExpectations()
        {
            {
                blobstoreService.getFileInfos( request );
                result = infos;

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

                listener.onDataUpload( KEY, reqImageSize, SERVING_URL, ARBITRARY_NAME, ct, "a", 9, "b" );
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
                result = KEY;
            }
        };

        tested.doPost( request, response );

        Gson gson = new GsonBuilder().create();
        JsonOutput output = gson.fromJson( writer.getBuffer().toString(), JsonOutput.class );

        assertEquals( KEY, output.key );
        assertEquals( SERVING_URL, output.servingUrl );
        assertEquals( ARBITRARY_NAME, output.customName );

        new Verifications()
        {
            {
                listener.onDataUpload( KEY, reqImageSize, SERVING_URL, ARBITRARY_NAME, ct, "a", 9, "b" );
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
    public void doGet( @Mocked final HttpServletRequest request,
                       @Mocked final HttpServletResponse response )
            throws Exception
    {
        final StringWriter writer = new StringWriter();

        new NonStrictExpectations()
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
    static class JsonOutput
    {
        private String key;

        private String servingUrl;

        private String customName;
    }
}