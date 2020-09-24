/*
 * Copyright (c) 2018 Comvai, s.r.o. All Rights Reserved.
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

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * The common test case for all integration tests requiring App Engine services to be available within unit test.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@Guice( modules = TestModule.class )
public class BackendServiceTestCase
{
    protected LocalServiceTestHelper helper;

    private LocalObjectifyHelper ofyHelper;

    @Inject
    public void setLocalDatastoreHelper( LocalObjectifyHelper loh )
    {
        this.ofyHelper = loh;

        helper = new LocalServiceTestHelper( new LocalMemcacheServiceTestConfig(),
                new LocalModulesServiceTestConfig(),
                new LocalURLFetchServiceTestConfig(),
                ofyHelper );
    }

    @BeforeMethod
    public void beforeMethod()
    {
        helper.setUp();
    }

    @AfterMethod
    public void afterMethod()
    {
        helper.tearDown();
    }

    @BeforeSuite
    public void start() throws IOException, InterruptedException
    {
        SystemProperty.environment.set( "Development" );
        ofyHelper.start();
    }

    @AfterSuite
    public void stop() throws InterruptedException, TimeoutException, IOException
    {
        ofyHelper.stop();
    }
}
