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

import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.util.Map;

/**
 * The set of convenient methods for App Engine Task Queue (Push Queues).
 * <p>
 * All task queue tasks are performed asynchronously. The application that creates the task is not notified
 * whether or not the task completed, or if it was successful. The task queue service provides a retry mechanism,
 * so if a task fails it can be retried a finite number of times.
 * <p>
 * Note: Although App Engine might appear to process tasks in the order in which they are enqueued,
 * it is normal for tasks to be executed in arbitrary order, so your implementation should not assume
 * that tasks are executed serially or in any other order.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see <a href="https://cloud.google.com/appengine/docs/standard/java/taskqueue">Task Queue Overview</a>
 */
public interface TaskExecutor
{
    /**
     * Enqueue task to be executed asynchronously at some time in near future.
     *
     * @param task the asynchronously runnable task
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle execute( Task task );

    /**
     * Enqueue task to be executed asynchronously with customized countdown.
     * Maximum countdown for a task	30 days from the current date and time.
     *
     * @param task        the asynchronously runnable task
     * @param postponeFor the number of seconds to be added to current time,
     *                    that's a time when the task will be started. Max 30 days.
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle execute( Task task, int postponeFor );

    /**
     * Enqueue task to be executed asynchronously at some time in near future.
     *
     * @param task    the asynchronously runnable task
     * @param options the task configuration
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle execute( Task task, TaskOptions options );

    /**
     * Enqueue cron task to be executed asynchronously registered under cronUri,
     * see {@link CronTaskRegistrar}.
     *
     * @param cronUri the URI under which is cron task registered
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle execute( String cronUri );

    /**
     * Enqueue cron task to be executed asynchronously registered under cronUri,
     * see {@link CronTaskRegistrar}.
     *
     * @param cronUri    the URI under which is cron task registered
     * @param parameters the additional parameters to be added to registered task
     * @return the task definition (given or computed) already in queue
     */
    TaskHandle execute( String cronUri, Map<String, String> parameters );
}
