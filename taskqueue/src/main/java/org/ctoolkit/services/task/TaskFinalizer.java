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

import com.google.inject.Injector;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
class TaskFinalizer
        implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Inject
    private static Injector injector;

    @Inject
    private transient Logger logger;

    public void finalize( Task taskToFinalize )
    {
        injector.injectMembers( this );

        logger.info( "Starting to finalize task: " + taskToFinalize );

        TaskWaitingList taskList = ofy().load().type( TaskWaitingList.class ).id( taskToFinalize.getOwnerId() ).now();
        if ( taskList != null )
        {
            Iterator<Task> taskIterator = taskList.getQueue().iterator();
            while ( taskIterator.hasNext() )
            {
                Task task = taskIterator.next();
                if ( task.equals( taskToFinalize ) )
                {
                    taskIterator.remove(); // remove task from buffer
                }
            }

            // if there is no tasks in task list, removes it
            if ( taskList.getTasks().isEmpty() )
            {
                ofy().delete().entity( taskList ).now();
            }
            // merge task - only current executing task will be removed
            else
            {
                ofy().save().entity( taskList ).now();
            }
        }

        logger.info( "Finalization of task: " + taskToFinalize + " has been finished" );
    }
}
