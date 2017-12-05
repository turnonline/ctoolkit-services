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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A job standalone definition to be executed asynchronously in defined time (by a cron). CronTask represents a small,
 * discrete unit of work to be performed by the {@link TaskExecutor} in asynchronous manner.
 * <p>
 * The queue name should be known in implementation time, thus defined prior to execution.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class CronTask
        implements Runnable, Serializable
{
    public static String CRON_NAMESPACE = "/cron/*";

    private String queueName = "default";

    /**
     * Parameter map to pass params for job execution.
     */
    private Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Constructs cron task instance and sets the default queue name to be task added in.
     */
    protected CronTask()
    {
        this( "default" );
    }

    /**
     * Constructs cron task instance and sets the queue name to be task added in.
     *
     * @param queueName the queue name to be task added in.
     */
    protected CronTask( String queueName )
    {
        this.queueName = queueName;
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
     * Returns the map of task parameters.
     *
     * @return the map of task parameters
     */
    public Map<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * Adds task parameter key value pair.
     *
     * @param name  the parameter key
     * @param value the parameter value
     */
    public void addParameter( String name, String value )
    {
        parameters.put( name, value );
    }

    /**
     * Returns the parameter value for the given key.
     *
     * @param name the parameter key
     * @return the parameter value for the given key
     */
    public String getParameter( String name )
    {
        return parameters.get( name );
    }

    @Override
    public abstract void run();

    @Override
    public String toString()
    {
        return getClass().getName() + " {" +
                "queueName='" + queueName + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
