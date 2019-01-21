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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Google App Engine Task Queue (Push Queues) convenient methods implementation.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org>Aurel Medvegy</a>"
 */
@Singleton
class TaskQueueExecutorBean
        implements TaskExecutor
{
    private static final Logger logger = LoggerFactory.getLogger( TaskQueueExecutorBean.class );

    private final Injector injector;

    private final Map<String, Class<? extends CronTask>> map = new HashMap<>();

    private final ModulesService modulesService;

    @Inject
    TaskQueueExecutorBean( Injector injector, ModulesService modulesService )
    {
        this.injector = injector;
        this.modulesService = modulesService;
    }

    @Override
    public TaskHandle schedule( @Nonnull Task task )
    {
        return schedule( task, TaskOptions.Builder.withDefaults() );
    }

    @Override
    public List<TaskHandle> schedule( @Nonnull String queueName, @Nonnull Task... tasks )
    {
        checkNotNull( queueName );
        checkNotNull( tasks );

        if ( tasks.length == 0 )
        {
            logger.info( "Nothing to schedule, task array is empty." );
            return new ArrayList<>();
        }

        List<TaskOptions> options = new ArrayList<>();

        for ( Task next : tasks )
        {
            if ( next == null )
            {
                throw new NullPointerException( "Any of the Task cannot be null!" );
            }
            options.add( populateTaskOptions( next, next.getOptions() ) );
        }

        Queue queue = getQueue( queueName );
        return queue.add( options );
    }

    @Override
    public TaskHandle schedule( @Nonnull Task task, int postponeFor )
    {
        checkNotNull( task );

        task.postponeFor( postponeFor );
        return schedule( task, TaskOptions.Builder.withDefaults() );
    }

    @Override
    public final TaskHandle schedule( @Nonnull Task task, @Nullable TaskOptions options )
    {
        checkNotNull( task );

        Queue queue = getQueue( task );
        TaskOptions ready = populateTaskOptions( task, options );
        TaskHandle handler = queue.add( ready );

        logger.info( "Task to execute: " + task );
        return handler;
    }

    public void register( @Nonnull CronTaskRegistrar registrar )
    {
        checkNotNull( registrar );
        map.putAll( registrar.getClassMap() );
    }

    @Override
    public TaskHandle schedule( @Nonnull String cronUri )
    {
        return schedule( cronUri, ( Map<String, String> ) null );
    }

    @Override
    public final TaskHandle schedule( @Nonnull String cronUri, @Nullable Map<String, String> parameters )
    {
        checkNotNull( cronUri );

        Class<? extends CronTask> clazz = map.get( cronUri );

        if ( parameters == null )
        {
            parameters = new HashMap<>();
        }

        if ( clazz == null )
        {
            logger.warn( "No CronTask impl. class registered for cron URI: " + cronUri );
            return null;
        }

        CronTask task = injector.getInstance( clazz );

        for ( Map.Entry<String, String> params : parameters.entrySet() )
        {
            task.addParameter( params.getKey(), params.getValue() );
        }

        logger.info( "Creating cron: " + task );

        return addPayload( task );
    }

    @Override
    public boolean delete( @Nonnull String taskName )
    {
        return false;
    }

    private TaskOptions populateTaskOptions( @Nonnull Task task, @Nullable TaskOptions options )
    {
        checkNotNull( task );

        if ( options == null )
        {
            options = TaskOptions.Builder.withDefaults();
        }

        Integer countdown = task.getPostponeFor();
        Long eta = options.getEtaMillis();

        // Set only if there is no eta value, the TaskOptions value has preference
        if ( countdown != null && countdown > 0 && eta == null )
        {
            // Calculates the final date in milliseconds TaskOptions#etaMillis for given relative value in seconds.
            options.etaMillis( System.currentTimeMillis() + countdown * 1000 );
        }

        String optionsTaskName = options.getTaskName();

        // Set name defined by task only if it's not already defined by options, the TaskOptions value has preference
        if ( optionsTaskName == null )
        {
            String taskName = task.getTaskName();
            if ( taskName != null )
            {
                if ( task.isMakeUnique() )
                {
                    // append unique ID to make sure task name will be unique
                    taskName = taskName + "_" + allocateId();
                }
                options.taskName( taskName );
            }
        }

        String service = modulesService.getCurrentModule();
        String version = modulesService.getCurrentVersion();
        String hostname = modulesService.getVersionHostname( service, version );

        // header added to make sure run against current module (even non default module)
        // see https://code.google.com/p/googleappengine/issues/detail?id=10457
        options.header( "Host", hostname );

        logger.info( "Queue: " + task.getQueueName() + ",  service: " + service + ", version: " + version
                + ", Service hostname: " + hostname );

        return options.payload( task );
    }

    @VisibleForTesting
    long allocateId()
    {
        return ofy().factory().allocateId( TaskUniqueIdGenerator.class ).getId();
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

    @Override
    public Queue getQueue( Task task )
    {
        Queue queue;

        if ( DEFAULT_QUEUE.equalsIgnoreCase( task.getQueueName() ) )
        {
            queue = QueueFactory.getDefaultQueue();
        }
        else
        {
            queue = QueueFactory.getQueue( task.getQueueName() );
        }

        return queue;
    }

    @Override
    public Queue getQueue( String queueName )
    {
        return QueueFactory.getQueue( queueName );
    }

    private Queue getQueue( CronTask task )
    {
        Queue queue;

        if ( DEFAULT_QUEUE.equalsIgnoreCase( task.getQueueName() ) )
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
