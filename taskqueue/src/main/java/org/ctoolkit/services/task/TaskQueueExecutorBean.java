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
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * GAE Task Queue implementation of {@link Executor}
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org>Aurel Medvegy</a>"
 */
@Singleton
class TaskQueueExecutorBean
        implements TaskExecutorService
{
    private final Logger logger;

    private final Injector injector;

    private final Map<String, Class<? extends CronTask>> map = new HashMap<String, Class<? extends CronTask>>();

    private ModulesService modulesService = ModulesServiceFactory.getModulesService();

    @Inject
    TaskQueueExecutorBean( Logger logger,
                           Injector injector )
    {
        this.logger = logger;
        this.injector = injector;
    }

    @Override
    public void execute( Task task )
    {
        execute( task, new Arrangement()
        {
            @Override
            public boolean done( Collection<Task> tasks )
            {
                return true;
            }
        } );
    }

    @Override
    public final void execute( Task task, Arrangement arrangement )
    {
        logger.info( "Input Task: " + task );

        TaskWaitingList tasks = ofy().load().type( TaskWaitingList.class ).id( task.getOwnerId() ).now();

        if ( tasks == null )
        {
            tasks = new TaskWaitingList( task );
        }
        else
        {
            tasks.add( task );
        }

        if ( task.getOrder() == -1 )
        {
            // if order == -1 -> no priority thus may be enqueued immediately
            logger.info( "Task to execute: " + task );
            addPayload( task );
            tasks.queued( task );
        }
        else if ( arrangement.done( tasks.getTasks() ) )
        {
            logger.info( "Tasks to execute: " + tasks.getTasks() );
            for ( Task t : tasks.getTasks() )
            {
                addPayload( t );
            }
            tasks.queued();
        }
        ofy().save().entity( tasks ).now();
    }

    public void register( CronTaskRegistrar registrar )
    {
        map.putAll( registrar.getClassMap() );
    }

    @Override
    public void execute( String cronUri )
    {
        execute( cronUri, null );
    }

    @Override
    public final void execute( String cronUri, Map<String, String> parameters )
    {
        Class<? extends CronTask> clazz = map.get( cronUri );

        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        if ( clazz == null )
        {
            if ( logger.isLoggable( Level.WARNING ) )
            {
                logger.warning( "No CronTask impl. class registered for cron URI: " + cronUri );
            }
            return;
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

        addPayload( task );
    }

    @Override
    public boolean isExecuting( Task[] tasks )
    {
        for ( Task taskToCheck : tasks )
        {
            if ( taskToCheck.getOwnerId() == null )
            { // no owner specified, continue to next one
                continue;
            }

            TaskWaitingList waitingList = ofy().load().type( TaskWaitingList.class ).id( taskToCheck.getOwnerId() ).now();
            if ( waitingList == null )
            { // no waiting list for task, continue to next one
                continue;
            }

            for ( Task taskInList : waitingList.getQueue() )
            {
                if ( taskInList.equals( taskToCheck ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void addPayload( Task task )
    {
        Queue queue = getQueue( task );

        TaskOptions options = TaskOptions.Builder.withDefaults();

        String module = modulesService.getCurrentModule();
        String version = modulesService.getCurrentVersion();
        String hostname = modulesService.getVersionHostname( module, version );

        // header added to make sure run against current module (even non default module)
        // see https://code.google.com/p/googleappengine/issues/detail?id=10457
        options.header( "Host", hostname );

        logger.info( "Enqueued in queue: " + task.getQueueName() + ",  module: " + module + ", version: " + version
                + ", Module hostname: " + hostname );

        queue.add( options.payload( new TaskWrapper( task, new TaskFinalizer() ) ) );
    }

    private void addPayload( CronTask task )
    {
        Queue queue = getQueue( task );

        TaskOptions options = TaskOptions.Builder.withDefaults();

        String module = modulesService.getCurrentModule();
        String version = modulesService.getCurrentVersion();
        String hostname = modulesService.getVersionHostname( module, version );

        // header added to make sure run against current module (even non default module)
        // see https://code.google.com/p/googleappengine/issues/detail?id=10457
        options.header( "Host", hostname );

        logger.info( "Enqueued in queue: " + task.getQueueName() + ",  module: " + module + ", version: " + version
                + ", Module hostname: " + hostname );

        queue.add( options.payload( new CronTaskWrapper( task ) ) );
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
