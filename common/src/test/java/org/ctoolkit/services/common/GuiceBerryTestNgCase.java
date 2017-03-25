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

package org.ctoolkit.services.common;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.common.testing.TearDown;
import com.google.guiceberry.testng.TestNgGuiceBerry;
import com.google.inject.name.Names;
import org.ctoolkit.services.guice.CtoolkitServicesAppEngineModule;
import org.ctoolkit.test.appengine.ServiceConfigModule;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class GuiceBerryTestNgCase
        extends ServiceConfigModule
{
    private TearDown toTearDown;

    public GuiceBerryTestNgCase()
    {
        construct( new LocalServiceTestHelper( new LocalMemcacheServiceTestConfig(),
                new LocalModulesServiceTestConfig(),
                new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage( 0 ) ) );
    }

    @BeforeMethod
    public void setUp( Method m )
    {
        // Make this the call to TestNgGuiceBerry.setUp as early as possible
        toTearDown = TestNgGuiceBerry.setUp( this, m, GuiceBerryTestNgCase.class );
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        // Make this the call to TestNgGuiceBerry.tearDown as late as possible
        toTearDown.tearDown();
    }

    @Override
    public void configureTestBinder()
    {
        // setting the SystemProperty.Environment.Value.Development
        System.setProperty( "com.google.appengine.runtime.environment", "Development" );
        install( new CtoolkitServicesAppEngineModule() );
        install( new CtoolkitCommonServicesModule() );

        PropertyConfig config = new PropertyConfig();
        config.setTestAppI( "localhost" );
        config.setProductionAppI( "localhostAsProd" );
        Names.bindProperties( binder(), config );
    }
}
