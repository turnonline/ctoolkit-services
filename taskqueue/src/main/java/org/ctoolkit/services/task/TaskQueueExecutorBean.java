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

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Google App Engine Task Queue (Push Queues) convenient methods implementation.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org>Aurel Medvegy</a>"
 */
@Singleton
class TaskQueueExecutorBean
        implements TaskExecutor
{
    private final Logger logger;

    private final Injector injector;

    private final Map<String, Class<? extends CronTask>> map = new HashMap<>();

    private ModulesService modulesService = ModulesServiceFactory.getModulesService();

    @Inject
    TaskQueueExecutorBean( Logger logger,
                           Injector injector )
    {
        this.logger = logger;
        this.injector = injector;
    }

    @Override
    public TaskHandle execute( Task task )
    {
        return execute( task, TaskOptions.Builder.withDefaults() );
    }

    @Override
    public TaskHandle execute( Task task, int postponeFor )
    {
        TaskOptions options = TaskOptions.Builder.withDefaults();
        options.etaMillis( System.currentTimeMillis() + postponeFor * 1000 );

        return execute( task, options );
    }

    @Override
    public final TaskHandle execute( Task task, TaskOptions options )
    {
        TaskHandle handler = addPayload( task, options );
        logger.info( "Task to execute: " + task );
        return handler;
    }

    public void register( CronTaskRegistrar registrar )
    {
        map.putAll( registrar.getClassMap() );
    }

    @Override
    public TaskHandle execute( String cronUri )
    {
        return execute( cronUri, null );
    }

    @Override
    public final TaskHandle execute( String cronUri, Map<String, String> parameters )
    {
        Class<? extends CronTask> clazz = map.get( cronUri );

        if ( parameters == null )
        {
            parameters = new HashMap<>();
        }

        if ( clazz == null )
        {
            if ( logger.isLoggable( Level.WARNING ) )
            {
                logger.warning( "No CronTask impl. class registered for cron URI: " + cronUri );
            }
            return null;
        }

        CronTask task = injector.getInstance( clazz );

        // TODO: From ServletRequest javadoc: The keys in the parameter map are of type String. The values in the parameter map are type of String array.
        for ( Map.Entry<String, String> params : parameters.entrySet() )
        {
            task.addParameter( params.getKey(), params.getValue() );
        }

        if ( logger.isLoggable( Level.INFO ) )
        {
            logger.info( "Creating cron: " + task );
        }

        return addPayload( task );
    }

    private TaskHandle addPayload( Task task, TaskOptions options )
    {
        Queue queue = getQueue( task );

        String module = modulesService.getCurrentModule();
        String version = modulesService.getCurrentVersion();
        String hostname = modulesService.getVersionHostname( module, version );

        // header added to make sure run against current module (even non default module)
        // see https://code.google.com/p/googleappengine/issues/detail?id=10457
        options.header( "Host", hostname );

        logger.info( "Enqueued in: " + task.getQueueName() + ",  module: " + module + ", version: " + version
                + ", Module hostname: " + hostname );

        return queue.add( options.payload( task ) );
    }

    private TaskHandle addPayload( CronTask task )
    {
        Queue queue = getQueue( task );

        TaskOptions options = TaskOptions.Builder.withDefaults();

        String module = modulesService.getCurrentModule();
        String version = modulesService.getCurrentVersion();
        String hostname = modulesService.getVersionHostname( module, version );

        // header added to make sure run against current module (even non default module)
        // see https://code.google.com/p/googleappengine/issues/detail?id=10457
        options.header( "Host", hostname );

        logger.info( "Enqueued in: " + task.getQueueName() + ",  module: " + module + ", version: " + version
                + ", Module hostname: " + hostname );

        return queue.add( options.payload( new CronTaskWrapper( task ) ) );
    }

    private Queue getQueue( Task task )
    {
        Queue queue;

        if ( "default".equalsIgnoreCase( task.getQueueName() ) )
        {
            queue = QueueFactory.getDefaultQueue();
        }
        else
        {
            queue = QueueFactory.getQueue( task.getQueueName() );
        }

        return queue;
    }

    private Queue getQueue( CronTask task )
    {
        Queue queue;

        if ( "default".equalsIgnoreCase( task.getQueueName() ) )
        {
            queue = QueueFactory.getDefaultQueue();
        }
        else
        {
            queue = QueueFactory.getQueue( task.getQueueName() );
        }

        return queue;
    }
}
