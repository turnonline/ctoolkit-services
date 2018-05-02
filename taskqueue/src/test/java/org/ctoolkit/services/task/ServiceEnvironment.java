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

package org.ctoolkit.services.task;

import com.google.appengine.tools.development.testing.LocalAppIdentityServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalCapabilitiesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalFileServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalImagesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalLogServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalRdbmsServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalRobotEnabledAppIdentityServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalSearchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalSocketServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalStubbyServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.ctoolkit.test.appengine.ServiceConfigModule;

/**
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class ServiceEnvironment
        extends ServiceConfigModule
{
    public ServiceEnvironment()
    {
        construct( new LocalServiceTestHelper(
                new LocalAppIdentityServiceTestConfig(),
                new LocalBlobstoreServiceTestConfig(),
                new LocalCapabilitiesServiceTestConfig(),
                new LocalDatastoreServiceTestConfig(),
                new LocalFileServiceTestConfig(),
                new LocalImagesServiceTestConfig(),
                new LocalLogServiceTestConfig(),
                new LocalMailServiceTestConfig(),
                new LocalMemcacheServiceTestConfig(),
                new LocalModulesServiceTestConfig(),
                new LocalRdbmsServiceTestConfig(),
                new LocalRobotEnabledAppIdentityServiceTestConfig(),
                new LocalSearchServiceTestConfig(),
                new LocalSocketServiceTestConfig(),
                new LocalStubbyServiceTestConfig(),
                new LocalTaskQueueTestConfig(),
                new LocalURLFetchServiceTestConfig(),
                new LocalUserServiceTestConfig()
        ) );
    }

    @Override
    public void configureTestBinder()
    {
        install( new CtoolkitServicesTaskModule() );
        install( new CtoolkitServicesTaskServletModule() );
    }
}