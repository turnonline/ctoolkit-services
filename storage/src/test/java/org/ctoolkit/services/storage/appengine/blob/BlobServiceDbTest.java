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

package org.ctoolkit.services.storage.appengine.blob;

import com.google.common.io.ByteStreams;
import org.ctoolkit.services.storage.BlobInfo;
import org.ctoolkit.services.storage.BlobService;
import org.ctoolkit.services.storage.appengine.GuiceBerryTestNgCase;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.InputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * The blob service tested with storage.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class BlobServiceDbTest
        extends GuiceBerryTestNgCase
{
    @Inject
    private BlobService tested;

    @Test
    public void storeAndServe() throws Exception
    {
        InputStream stream = BlobServiceDbTest.class.getResourceAsStream( "thismac.png" );
        byte[] dataInput = ByteStreams.toByteArray( stream );
        String mimeType = "image/png";

        BlobInfo info = tested.store( dataInput, mimeType );

        assertNotNull( info, "Blob info instance is being expected," );
        assertNotNull( info.getFileName() );
        assertNotNull( info.getBucketName() );
        assertNotNull( info.getLength() );
        assertNotNull( info.getLastModified() );
        assertEquals( info.getMimeType(), mimeType );

        byte[] served = tested.serve( info.getFileName() );

        assertEquals( served, dataInput );
    }

    @Test
    public void delete() throws Exception
    {
        InputStream stream = BlobServiceDbTest.class.getResourceAsStream( "thismac.png" );
        byte[] dataInput = ByteStreams.toByteArray( stream );
        String mimeType = "image/png";

        BlobInfo info = tested.store( dataInput, mimeType );
        assertTrue( tested.delete( info.getFileName() ) );
    }

    @Test
    public void blobInfo() throws Exception
    {
        InputStream stream = BlobServiceDbTest.class.getResourceAsStream( "thismac.png" );
        byte[] dataInput = ByteStreams.toByteArray( stream );
        String mimeType = "image/png";

        BlobInfo info = tested.store( dataInput, mimeType );

        BlobInfo metadata = tested.getBlobInfo( info.getFileName(), info.getBucketName() );
        assertEquals( metadata.getMimeType(), info.getMimeType() );
        assertEquals( metadata.getContentEncoding(), info.getContentEncoding() );
        assertEquals( metadata.getBucketName(), info.getBucketName() );
        assertEquals( metadata.getFileName(), info.getFileName() );
        assertEquals( metadata.getLength(), info.getLength() );
        assertEquals( metadata.getEtag(), info.getEtag() );
        assertNotNull( metadata.getLastModified() );
    }
}