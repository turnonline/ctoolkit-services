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

package org.ctoolkit.services.datastore;

import com.google.appengine.api.utils.SystemProperty;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.testing.LocalDatastoreHelper;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import org.ctoolkit.services.datastore.objectify.Child2LevelEntity;
import org.ctoolkit.services.datastore.objectify.ChildEntity;
import org.ctoolkit.services.datastore.objectify.FakeEntity;
import org.ctoolkit.services.datastore.objectify.ParentEntity;
import org.ctoolkit.services.datastore.objectify.ParentFakeEntity;
import org.ctoolkit.services.datastore.objectify.SiblingChildEntity;
import org.ctoolkit.services.storage.EntityLongIdentityHasherTestEntity;
import org.ctoolkit.services.storage.EntityStringIdentityHasherTestEntity;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * The base class for App Engine backend services local testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class BackendServiceTestCase
{
    private Closeable session;

    @BeforeSuite
    public void beforeAll( ITestContext context ) throws IOException, InterruptedException
    {
        LocalDatastoreHelper helper = get( context );
        helper.start();

        SystemProperty.environment.set( "Development" );
    }

    @BeforeMethod
    public void before( ITestContext context ) throws IOException
    {
        LocalDatastoreHelper helper = get( context );
        helper.reset();
        Datastore datastore = helper.getOptions().getService();
        ObjectifyService.init( new ObjectifyFactory( datastore ) );

        session = ObjectifyService.begin();

        ObjectifyService.register( FakeEntity.class );
        ObjectifyService.register( ParentFakeEntity.class );
        ObjectifyService.register( ParentEntity.class );
        ObjectifyService.register( ChildEntity.class );
        ObjectifyService.register( SiblingChildEntity.class );
        ObjectifyService.register( Child2LevelEntity.class );
        ObjectifyService.register( EntityLongIdentityHasherTestEntity.class );
        ObjectifyService.register( EntityLongIdentityHasherTestEntity.MyHashCode.class );
        ObjectifyService.register( EntityStringIdentityHasherTestEntity.class );
        ObjectifyService.register( EntityStringIdentityHasherTestEntity.MyHashCode.class );
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        session.close();
    }

    @AfterSuite
    public void stop( ITestContext context ) throws InterruptedException, TimeoutException, IOException
    {
        LocalDatastoreHelper helper = get( context );
        helper.stop();
    }

    private LocalDatastoreHelper get( ITestContext context )
    {
        LocalDatastoreHelper helper = ( LocalDatastoreHelper ) context.getAttribute( LocalDatastoreHelper.class.getName() );
        if ( helper == null )
        {
            helper = LocalDatastoreHelper.create( 1.0 );
            context.setAttribute( LocalDatastoreHelper.class.getName(), helper );
        }
        return helper;
    }
}
