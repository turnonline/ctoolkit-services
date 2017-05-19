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

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Serialize;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Entity
public class TaskWaitingList
{
    @Id
    private Long id;

    @Serialize
    private SortedSet<Task> tasks;

    @Serialize
    private SortedSet<Task> queue;

    @Index
    private Date updatedDate;

    @Index
    private Date createdDate;

    TaskWaitingList()
    {
    }

    public TaskWaitingList( Task task )
    {
        this.id = task.getOwnerId();
        tasks = new TreeSet<>();
        queue = new TreeSet<>();
        tasks.add( task );
    }

    public Long getId()
    {
        return id;
    }

    public void add( Task task )
    {
        if ( !id.equals( task.getOwnerId() ) )
        {
            throw new IllegalArgumentException( task + " cannot be assigned to different owner. Owner: " + this );
        }
        tasks.add( task );
    }

    public SortedSet<Task> getTasks()
    {
        return tasks;
    }

    public SortedSet<Task> getQueue()
    {
        return queue;
    }

    public void queued()
    {
        queue.addAll( tasks );
        tasks.clear();
    }

    public void queued( Task task )
    {
        queue.add( task );
        tasks.remove( task );
    }

    /**
     * Return updated date.
     *
     * @return updated date
     */
    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    /**
     * Date of entity creation
     *
     * @return date of entity creation
     */
    public Date getCreatedDate()
    {
        return createdDate;
    }

    /**
     * Method is called before every update. If the entity is being updated first time the
     * <code>createdDate</code> will be set up otherwise <code>updatedDate</code> will be updated.
     */
    @OnSave
    public void onSave()
    {
        if ( id == null )
        {
            createdDate = new Date();
        }
        else
        {
            updatedDate = new Date();
        }
    }

    @Override
    public String toString()
    {
        return "TaskWaitingList{" + "id=" + id +
                ", updatedDate=" + updatedDate +
                ", createdDate=" + createdDate +
                '}';
    }
}
