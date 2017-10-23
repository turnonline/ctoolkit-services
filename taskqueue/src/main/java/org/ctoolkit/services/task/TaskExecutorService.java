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

import java.util.Map;

/**
 * A service that executes submitted {@link Task} tasks. The Task may be buffered until certain conditions in
 * {@link Arrangement} will be met.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface TaskExecutorService
{
    /**
     * Executes the given command at some time in the future. The task's order value -1 causes putting the task into
     * the target queue immediately.
     *
     * @param task the runnable task
     */
    TaskHandle execute( Task task );

    /**
     * Executes the given command at some time in the future. Arrangement implementation may cause this task
     * will be buffered while it will be put in to the queue until certain conditions in Arrangement will be met.
     * The task's order value -1 causes putting the task into the target queue immediately.
     *
     * @param task        the runnable task
     * @param arrangement the object to better specify the task execution point
     */
    TaskHandle execute( Task task, Arrangement arrangement );

    TaskHandle execute( String cronUri );

    TaskHandle execute( String cronUri, Map<String, String> parameters );
}
