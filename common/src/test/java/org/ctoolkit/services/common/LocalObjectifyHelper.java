/*
 * Copyright (c) 2020 TurnOnline.biz s.r.o. All Rights Reserved.
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

package org.ctoolkit.services.common;

import com.google.appengine.tools.development.testing.LocalServiceTestConfig;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import javax.inject.Singleton;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Local Objectify Google Datastore helper, a configuration to initialize local datastore emulator for unit testing.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@Singleton
public class LocalObjectifyHelper
        implements LocalServiceTestConfig
{
    private final LocalDatastoreHelper lDatastoreHelper = LocalDatastoreHelper.create( 1.0 );

    private Closeable session;

    @Override
    public void setUp()
    {
        try
        {
            lDatastoreHelper.reset();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }

        Datastore datastore = lDatastoreHelper.getOptions().getService();
        ObjectifyService.init( new ObjectifyFactory( datastore ) );

        session = ObjectifyService.begin();
    }

    @Override
    public void tearDown()
    {
        try
        {
            session.close();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Starts the local Datastore emulator through {@code gcloud}.
     *
     * <p>Currently the emulator does not persist any state across runs.
     */
    public void start() throws IOException, InterruptedException
    {
        lDatastoreHelper.start();
    }

    /**
     * Stops the Datastore emulator.
     */
    public void stop() throws InterruptedException, TimeoutException, IOException
    {
        lDatastoreHelper.stop();
    }
}
