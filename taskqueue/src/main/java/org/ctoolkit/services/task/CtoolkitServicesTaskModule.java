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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

import javax.inject.Singleton;
import java.util.Set;

/**
 * The CtoolkiT Services - Google App Engine standard task queue extension module.
 * Install this module if {@link TaskExecutor} service is needed in order to enqueue
 * an asynchronous {@link Task}.
 * <p>
 * If you need cron task (asynchronously executed in defined time) install
 * {@link CtoolkitServicesTaskServletModule} too and configure your cron task
 * implementation via {@link CronTaskRegistrar}
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesTaskModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        // bind cron task
        Multibinder.newSetBinder( binder(), CronTaskRegistrar.class );
    }

    @Provides
    @Singleton
    TaskExecutor provideExecutorService( TaskQueueExecutorBean executor, Set<CronTaskRegistrar> registrar )
    {
        for ( CronTaskRegistrar next : registrar )
        {
            next.configure();
            executor.register( next );
        }

        return executor;
    }
}
