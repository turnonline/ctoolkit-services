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
 * The CtoolkiT Services - Google AppEngine standard task queue extension module.
 * In order to configure see {@link CronTaskRegistrar}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesTaskModule
        extends AbstractModule
{
    private TaskQueueExecutorBean executorBean;

    @Override
    protected void configure()
    {
        // bind cron task
        Multibinder.newSetBinder( binder(), CronTaskRegistrar.class );
    }

    @Provides
    @Singleton
    TaskExecutorService provideExecutorService( TaskQueueExecutorBean bean, Set<CronTaskRegistrar> registrar )
    {
        if ( executorBean != null )
        {
            return executorBean;
        }

        for ( CronTaskRegistrar next : registrar )
        {
            next.configure();
            bean.register( next );
        }

        executorBean = bean;

        return bean;
    }
}
