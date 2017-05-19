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

import java.util.HashMap;
import java.util.Map;

/**
 * Implement {@link #configure()} to register your own implementation of cron task classes with executor service
 * in order to be able execute scheduled task.
 * <p/>
 * As client you are required to bind your own implementation of <code>CronTaskRegistrar</code> in guice module as following:
 * <pre>
 * Multibinder<CronTask> registrar = Multibinder.newSetBinder( binder(), CronTask.class );
 * registrar.addBinding().to( CronTaskRegistration.class );
 * </pre>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class CronTaskRegistrar
{
    private final Map<String, Class<? extends CronTask>> map = new HashMap<String, Class<? extends CronTask>>();

    /**
     * Implement to register CronTask Classes.
     */
    public abstract void configure();

    /**
     * Register a CronTask.
     *
     * @param clazz the cron task class to register
     */
    public final void register( String cronUri, Class<? extends CronTask> clazz )
    {
        map.put( cronUri, clazz );
    }

    /**
     * Returns the map of cron task classes to be registered.
     *
     * @return the map of cron task classes to be registered
     */
    public final Map<String, Class<? extends CronTask>> getClassMap()
    {
        return map;
    }
}
