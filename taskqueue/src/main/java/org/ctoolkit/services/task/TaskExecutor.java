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

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * The set of convenient methods for App Engine Task Queue (Push Queues).
 * <p>
 * In order to inject a service instance in your task implementation use transient instance.
 * Task will be serialized and requested objects injected right before the execution.
 * <p>
 * <b>For example:</b>
 * <pre>
 *
 * class MyOwnTask
 *        extends Task
 *  {
 *   &#64;Inject
 *   private transient MyService service;
 *   ..
 *  }
 *
 *  // In the guice module
 *  requestStaticInjection( MyOwnTask.class );
 * </pre>
 * All task queue tasks are performed asynchronously. The application that creates the task is not notified
 * whether or not the task completed, or if it was successful. The task queue service provides a retry mechanism,
 * so if a task fails it can be retried a finite number of times.
 * <p>
 * Note: Although App Engine might appear to process tasks in the order in which they are enqueued,
 * it is normal for tasks to be executed in arbitrary order, so your implementation should not assume
 * that tasks are executed serially or in any other order. If you need to make sure tasks are executed
 * in defined order, use task chaining:
 * <p>
 * <b>Task chaining example:</b>
 * <pre>
 *
 * Task first = new MyOwnTask().postponeFor( 10 );
 * Task second = new MySecondTask();
 *
 * first.setNext( second );
 *
 * // first task will be postponed by 10 seconds, second will be added to the queue once first ends successfully
 * executor.schedule( first );
 * </pre>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see <a href="https://cloud.google.com/appengine/docs/standard/java/taskqueue">Task Queue Overview</a>
 */
public interface TaskExecutor
{
    /**
     * Adds task to the queue to be executed asynchronously.
     *
     * @param task the asynchronously runnable task
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle schedule( @Nonnull Task task );

    /**
     * Adds all tasks to the same queue to be executed asynchronously.
     *
     * @param queueName the queue name where all tasks will be added.
     * @param task      the asynchronously runnable tasks
     * @return the task definition (given or computed) already in queue
     */
    List<TaskHandle> schedule( @Nonnull String queueName, @Nonnull Task... task );

    /**
     * Adds task to the queue to be executed asynchronously and sets the number of seconds delay
     * before execution of the task. Maximum countdown for a task is 30 days from the date and time
     * when task has been added to a queue.
     *
     * @param task        the asynchronously runnable task
     * @param postponeFor the number of seconds to be added to current time,
     *                    that's a time when the task will be started. Max 30 days.
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle schedule( @Nonnull Task task, int postponeFor );

    /**
     * Adds task to the queue to be executed asynchronously.
     *
     * @param task    the asynchronously runnable task
     * @param options the task configuration
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle schedule( @Nonnull Task task, @Nullable TaskOptions options );

    /**
     * Adds cron task to the queue to be executed asynchronously registered under cronUri,
     * see {@link CronTaskRegistrar}.
     *
     * @param cronUri the URI under which is cron task registered
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle schedule( @Nonnull String cronUri );

    /**
     * Adds cron task to the queue to be executed asynchronously registered under cronUri,
     * see {@link CronTaskRegistrar}.
     *
     * @param cronUri    the URI under which is cron task registered
     * @param parameters the additional parameters to be added to registered task
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle schedule( @Nonnull String cronUri, @Nullable Map<String, String> parameters );

    /**
     * Deletes a task from the default queue.
     *
     * @param taskName the name of the task to be deleted
     * @return true if the task was successfully deleted. False if the task was not found or was previously deleted.
     */
    boolean delete( @Nonnull String taskName );

    /**
     * Returns the associated {@link Queue} of the task.
     *
     * @param task the task instance
     * @return the associated queue
     */
    Queue getQueue( Task task );

    /**
     * Returns the {@link Queue} by name.
     * <p>
     * The returned Queue object may not necessarily refer to an existing queue.
     * Queues must be configured before they may be used.
     * Attempting to use a non-existing queue name may result in errors
     * at the point of use of the Queue object and not when calling getQueue(String).
     *
     * @param queueName the name of the queue to be returned
     * @return the queue
     */
    Queue getQueue( String queueName );
}
