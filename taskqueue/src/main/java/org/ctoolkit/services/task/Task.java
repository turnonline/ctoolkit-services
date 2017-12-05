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

import javax.annotation.Nonnull;

/**
 * A job standalone definition to be executed asynchronously. Task represents a small, discrete unit of work to be
 * performed by the {@link TaskExecutor} in asynchronous manner.
 * <p>
 * Vocabulary:
 * <lo>
 * <li>Owner Id: ID of the owner in favor of whom will be a task executed.</li>
 * <li>Method: a name of the job.</li>
 * <li>Order: a number that represents the execution order. 0 means the first (highest priority).</li>
 * <li>Queue name: is a queue defined by the execution environment where task will be placed in.</li>
 * </lo>
 * <p>
 * Notice: Override {@link #equals(Object)} } and {@link #hashCode()} methods to make <code>Comparator</code> work
 * correctly and don't forget to call its super as well because <code>Task</code> properties must be taken into account.
 * <p>
 * Note: Queue, and task names must be a combination of one or more digits, letters a-z, underscores, and/or dashes,
 * satisfying the following regular expression:
 * [0-9a-zA-Z\-\_]+
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org>Aurel Medvegy</a>"
 * @see TaskExecutor
 */
public abstract class Task
        implements Comparable<Task>, DeferredTask
{
    private Long ownerId;

    private String method;

    private int order;

    private String queueName;

    Task()
    {
        // default constructor for serialization
    }

    /**
     * Creates task with given name and 'default' queue. No priority set.
     *
     * @param method the task name
     */
    public Task( String method )
    {
        this( method, "default", -1 );
    }

    /**
     * Creates task with given name 'default' queue and with specified priority.
     *
     * @param method the task name
     * @param order  the priority
     */
    public Task( String method, int order )
    {
        this( method, "default", order );
    }

    /**
     * Creates task. No priority set.
     *
     * @param method    the task name
     * @param queueName the queue name to be task added in
     */
    public Task( String method, String queueName )
    {
        this( method, queueName, -1 );
    }

    /**
     * Creates task with specified given params.
     *
     * @param method    the task name
     * @param queueName the queue name to be task added in
     * @param order     the order
     */
    public Task( String method, String queueName, int order )
    {
        this.method = method;
        this.queueName = queueName;
        this.order = order;
    }

    /**
     * Returns the task owner Id.
     *
     * @return the task owner Id
     */
    public Long getOwnerId()
    {
        return ownerId;
    }

    /**
     * Sets the task owner Id.
     *
     * @param ownerId the task owner Id to be set
     */
    public void setOwnerId( Long ownerId )
    {
        this.ownerId = ownerId;
    }

    /**
     * Returns the task name.
     *
     * @return the task name
     */
    public String getMethod()
    {
        return method;
    }

    /**
     * Returns the queue name to be task added in.
     *
     * @return the queue name
     */
    public String getQueueName()
    {
        return queueName;
    }

    /**
     * Returns the task order. The '-1' value means no order setup.
     *
     * @return the task order
     */
    public int getOrder()
    {
        return order;
    }

    /**
     * The method takes into account 'order', 'method' and #equals. It evaluates these values in that order.
     */
    @Override
    public int compareTo( @Nonnull Task task )
    {
        int thisOrder = this.order;
        int anotherOrder = task.getOrder();

        int o = thisOrder < anotherOrder ? -1 : ( thisOrder == anotherOrder ? 0 : 1 );

        int m = this.method.compareTo( task.getMethod() );

        if ( o == 0 && m == 0 )
        {
            return this.equals( task ) ? 0 : 1;
        }
        if ( o == 0 )
        {
            return m;
        }

        return o;
    }

    /**
     * The method takes into account 'order', 'method'
     */
    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( !( o instanceof Task ) ) return false;

        Task task = ( Task ) o;

        if ( order != task.order ) return false;
        //noinspection RedundantIfStatement
        if ( !method.equals( task.method ) ) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = method.hashCode();
        result = 31 * result + order;
        return result;
    }

    @Override
    public String toString()
    {
        return "Task{" +
                "ownerId=" + ownerId +
                ", method='" + method + '\'' +
                ", order=" + order +
                ", queueName='" + queueName + '\'' +
                '}';
    }

    @Override
    public abstract void run();
}
