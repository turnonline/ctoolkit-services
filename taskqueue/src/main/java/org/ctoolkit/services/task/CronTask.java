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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * A job standalone definition to be executed asynchronously in defined time (by a cron). CronTask represents a small,
 * discrete unit of work to be performed by the {@link TaskExecutor} in asynchronous manner.
 * <p>
 * The queue name should be known during implementation time, thus defined prior to execution.
 * <p>
 * <strong>Note: implementation must have a zero argument constructor!</strong>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class CronTask
        extends Task
{
    private static final long serialVersionUID = -6478709287944509148L;

    public static String CRON_NAMESPACE = "/cron/*";

    /**
     * Parameter map to pass params for job execution.
     */
    private Map<String, String[]> parameters = new HashMap<>();

    /**
     * Constructs cron task instance and sets the default queue name to be task added in.
     */
    protected CronTask()
    {
        super();
    }

    /**
     * Constructs cron task instance and sets the queue name to be task added in.
     *
     * @param queueName the queue name to be task added in.
     */
    protected CronTask( @Nonnull String queueName )
    {
        super( null, true, queueName );
    }

    /**
     * Creates cron task with given name prefix. Placed to the default queue.
     *
     * @param namePrefix the name prefix
     * @param makeUnique true to append unique ID to the task name prefix
     */
    public CronTask( @Nonnull String namePrefix, boolean makeUnique )
    {
        super( namePrefix, makeUnique );
    }

    /**
     * Creates cron task with specified name prefix and queue names.
     * Note, the unique ID will be applied only for non null name prefix.
     *
     * @param namePrefix the optional name prefix, {@code null} to be auto generated by App Engine
     * @param makeUnique true to append unique ID to the task name prefix
     * @param queueName  the queue name where task will be added
     */
    public CronTask( @Nullable String namePrefix, boolean makeUnique, @Nonnull String queueName )
    {
        super( namePrefix, makeUnique, queueName );
    }

    /**
     * Returns the map of task parameters.
     *
     * @return the map of task parameters
     */
    public Map<String, String[]> getParameters()
    {
        return parameters;
    }

    /**
     * Adds task parameter key value pair.
     *
     * @param name  the parameter key
     * @param value the parameter value
     */
    public void addParameter( String name, String[] value )
    {
        parameters.put( name, value );
    }

    /**
     * Returns the parameter value for the given key.
     *
     * @param name the parameter key
     * @return the parameter value for the given key
     */
    public String[] getParameter( String name )
    {
        return parameters.get( name );
    }
}
