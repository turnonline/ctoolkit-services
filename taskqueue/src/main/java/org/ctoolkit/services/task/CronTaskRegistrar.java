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
 * <strong>It's recommended to prefix URI with '/cron' as it's an accepted declaration
 * for security-constraint within web.xml</strong>
 * <p>
 * Implement {@link #configure()} to register your own implementation of cron task classes with executor service
 * in order to be able execute scheduled task.
 * <p>
 * As client you are required to register your {@link CronTask} implementation and bind your registration
 * {@link CronTaskRegistrar} in guice module as following:
 * <pre>
 *  public class MyCronTaskRegistration
 *          extends CronTaskRegistrar
 *  {
 *      {@literal @}Override
 *      public void configure()
 *      {
 *          register( "/cron/my-own-cron-task", MyOwnCronTask.class );
 *      }
 *  }
 *
 *  // finally add your registration binding in your application guice module
 *  Multibinder&#60;CronTaskRegistrar&#62; registrar = Multibinder.newSetBinder( binder(), CronTaskRegistrar.class );
 *  registrar.addBinding().to( MyCronTaskRegistration.class );
 * </pre>
 * The cron task scheduling definition takes place in cron.xml
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see <a href="https://cloud.google.com/appengine/docs/standard/java/config/cron">Scheduling Tasks With Cron for Java</a>
 */
public abstract class CronTaskRegistrar
{
    private final Map<String, Class<? extends CronTask>> map = new HashMap<>();

    /**
     * Implement to register CronTask Classes.
     */
    public abstract void configure();

    /**
     * Register a CronTask.
     *
     * @param cronUri the relative path as cron identification starting with '/cron/..'
     * @param clazz   the cron task class to register
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
