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

import com.google.appengine.api.taskqueue.DeferredTask;

/**
 * Task wrapper to inherit from {@link DeferredTask}. Intended for implementation purpose, not public.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
class TaskWrapper
        implements DeferredTask
{
    private static final long serialVersionUID = 7931451935955414168L;

    private final Task runnable;

    private final TaskFinalizer taskFinalizer;

    TaskWrapper( Task runnable, TaskFinalizer taskFinalizer )
    {
        this.runnable = runnable;
        this.taskFinalizer = taskFinalizer;
    }

    @Override
    public void run()
    {
        runnable.run();

        // finalize task
        taskFinalizer.finalize( runnable );
    }
}
