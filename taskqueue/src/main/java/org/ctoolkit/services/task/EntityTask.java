/*
 * Copyright (c) 2018 Comvai, s.r.o. All Rights Reserved.
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

import com.googlecode.objectify.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * The extension of the {@link Task} with dedicated convenient method {@link #execute(Object)}
 * executed within transaction. The task might be retired if no entity will be found once task has started
 * as in the meantime entity could be removed with another process etc.
 * In this case warning will be logged.
 * <p>
 * <strong>Dedicated task lasting for seconds, not minutes</strong>
 * </p>
 * <p>
 * Transactions have a maximum duration of 60 seconds with a 10 second idle expiration time after 30 seconds.
 * </p>
 *
 * @param <T> the type of the entity that task will work with
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class EntityTask<T>
        extends Task<T>
{
    private static final long serialVersionUID = -2500873815267224035L;

    private final static Logger logger = LoggerFactory.getLogger( EntityTask.class );

    /**
     * Creates a task with an auto generated name (by App Engine). Placed to the default queue.
     *
     * @param key the entity key to be used to retrieve entity to work with
     */
    public EntityTask( @Nonnull Key<T> key )
    {
        super();
        setEntityKey( checkNotNull( key ) );
    }

    /**
     * Creates a task with an auto generated name (by App Engine). Placed to the default queue.
     *
     * @param entity this task will work with this entity asynchronously
     */
    public EntityTask( @Nonnull T entity )
    {
        super();
        setEntityKey( Key.create( entity ) );
    }

    /**
     * Creates task with given name prefix incl. unique ID to be appended. Placed to the default queue.
     *
     * @param key        the entity key to be used to retrieve entity to work with
     * @param namePrefix the task name prefix
     */
    public EntityTask( @Nonnull Key<T> key, @Nonnull String namePrefix )
    {
        super( namePrefix );
        setEntityKey( checkNotNull( key ) );
    }

    /**
     * Creates task with given name prefix incl. unique ID to be appended. Placed to the default queue.
     *
     * @param entity     this task will work with this entity asynchronously
     * @param namePrefix the task name prefix
     */
    public EntityTask( @Nonnull T entity, @Nonnull String namePrefix )
    {
        super( namePrefix );
        setEntityKey( Key.create( entity ) );
    }

    /**
     * Creates task with given name prefix. Placed to the default queue.
     *
     * @param key        the entity key to be used to retrieve entity to work with
     * @param namePrefix the name prefix
     * @param makeUnique true to append unique ID to the task name prefix
     */
    public EntityTask( @Nonnull Key<T> key, @Nonnull String namePrefix, boolean makeUnique )
    {
        super( namePrefix, makeUnique );
        setEntityKey( checkNotNull( key ) );
    }

    /**
     * Creates task with given name prefix. Placed to the default queue.
     *
     * @param entity     this task will work with this entity asynchronously
     * @param namePrefix the name prefix
     * @param makeUnique true to append unique ID to the task name prefix
     */
    public EntityTask( @Nonnull T entity, @Nonnull String namePrefix, boolean makeUnique )
    {
        super( namePrefix, makeUnique );
        setEntityKey( Key.create( entity ) );
    }

    /**
     * Creates task with specified name prefix and queue names.
     * Note, the unique ID will be applied only for non null name prefix.
     *
     * @param key        the entity key to be used to retrieve entity to work with
     * @param namePrefix the optional name prefix, {@code null} to be auto generated by App Engine
     * @param makeUnique true to append unique ID to the task name prefix
     * @param queueName  the queue name where task will be added
     */
    public EntityTask( @Nonnull Key<T> key,
                       @Nullable String namePrefix,
                       boolean makeUnique,
                       @Nonnull String queueName )
    {
        super( namePrefix, makeUnique, queueName );
        setEntityKey( checkNotNull( key ) );
    }

    /**
     * Creates task with specified name prefix and queue names.
     * Note, the unique ID will be applied only for non null name prefix.
     *
     * @param entity     this task will work with this entity asynchronously
     * @param namePrefix the optional name prefix, {@code null} to be auto generated by App Engine
     * @param makeUnique true to append unique ID to the task name prefix
     * @param queueName  the queue name where task will be added
     */
    public EntityTask( @Nonnull T entity,
                       @Nullable String namePrefix,
                       boolean makeUnique,
                       @Nonnull String queueName )
    {
        super( namePrefix, makeUnique, queueName );
        setEntityKey( Key.create( entity ) );
    }

    @Override
    public final void execute()
    {
        ofy().transact( () -> {
            T entity = workWith();
            if ( entity == null )
            {
                logger.warn( "No Entity has been found for specified key '" + getEntityKey()
                        + "'. Task '" + getTaskName() + "' has been retired." );
                return;
            }

            execute( entity );
        } );
    }

    /**
     * The client implementation to be executed asynchronously.
     *
     * @param entity the entity instance for key {@link #getEntityKey()}
     */
    protected abstract void execute( @Nonnull T entity );
}
