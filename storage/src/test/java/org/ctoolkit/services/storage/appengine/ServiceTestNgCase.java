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

package org.ctoolkit.services.storage.appengine;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.ObjectifyService;
import org.ctoolkit.services.storage.appengine.objectify.Child2LevelEntity;
import org.ctoolkit.services.storage.appengine.objectify.ChildEntity;
import org.ctoolkit.services.storage.appengine.objectify.FakeEntity;
import org.ctoolkit.services.storage.appengine.objectify.ParentEntity;
import org.ctoolkit.services.storage.appengine.objectify.ParentFakeEntity;
import org.ctoolkit.services.storage.appengine.objectify.SiblingChildEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.Closeable;
import java.lang.reflect.Method;

/**
 * The base class for App Engine backend services local testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class ServiceTestNgCase
{
    private LocalServiceTestHelper helper = new LocalServiceTestHelper( new LocalMemcacheServiceTestConfig(),
            new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage( 0 ) );

    private Closeable session;

    @BeforeMethod
    public void setUp( Method m )
    {
        SystemProperty.environment.set( "Development" );

        helper.setUp();
        session = ObjectifyService.begin();

        ObjectifyService.register( FakeEntity.class );
        ObjectifyService.register( ParentFakeEntity.class );
        ObjectifyService.register( ParentEntity.class );
        ObjectifyService.register( ChildEntity.class );
        ObjectifyService.register( SiblingChildEntity.class );
        ObjectifyService.register( Child2LevelEntity.class );
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        session.close();
        helper.tearDown();
    }
}
