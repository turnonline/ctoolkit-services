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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Injector;
import com.googlecode.objectify.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * The task, a standalone job definition to be executed asynchronously. Task represents a small, discrete unit of work.
 *
 * <b>Vocabulary:</b>
 * <ul>
 * <li><b>Entity Key:</b> represents an identification of an entity to work with</li>
 * <li><b>Task Name:</b> a name of the job, composition of the name prefix and entity Key.
 * If name prefix is not defined, task name will be auto generated by the App Engine.
 * Task Name appears in the Google Cloud Console.</li>
 * <li><b>Make Unique:</b> as task name must be unique at least for 9 days, set this property {@code true}
 * to append an unique ID to customized task name. By default value is {@code true}</li>
 * <li><b>Queue name:</b> a queue defined by the execution environment where task will be placed in.</li>
 * </ul>
 *
 * <b>Note:</b> Queue, and task names must be a combination of one or more digits, letters a-z, underscores, and/or dashes,
 * satisfying the following regular expression:
 * [0-9a-zA-Z\-\_]+
 *
 * <b>Naming a task</b>
 * When you create a new task, App Engine assigns the task a unique name by default. However, you can assign your
 * own name to a task by using the name parameters (Task Name, Entity Key, Make Unique). An advantage of assigning
 * your own task names is that named tasks are de-duplicated, which means you can use task names to guarantee
 * that a task is only added once. De-duplication continues for 9 days after the task is completed or deleted.
 * <p>
 * Note that de-duplication logic introduces significant performance overhead, resulting in increased latencies and
 * potentially increased error rates associated with named tasks. These costs can be magnified significantly
 * if task names are sequential, such as with timestamps. So, if you assign your own names, we recommend
 * using a well-distributed prefix for task names, such as a hash of the contents.
 *
 * @param <T> the type of the entity key and related entity
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see TaskExecutor
 */
@SuppressWarnings( "WeakerAccess" )
public abstract class Task<T>
        implements DeferredTask
{
    private static final long serialVersionUID = -3959632462820157604L;

    private static final Logger logger = LoggerFactory.getLogger( Task.class );

    @Inject
    private static Injector injector;

    @Inject
    private transient TaskExecutor executor;

    private Key<T> entityKey;

    private String namePrefix;

    private boolean makeUnique;

    private Task next;

    private SerializableFunction<Object, Boolean> function;

    private String queueName;

    private TaskOptions options;

    private Integer postponeFor;

    /**
     * Creates a task with an auto generated name (by App Engine). Placed to the default queue.
     */
    public Task()
    {
        // for null name prefix the task name will be generated by App Engine
        this( null, true, Queue.DEFAULT_QUEUE );
    }

    /**
     * Creates task with given name prefix incl. unique ID to be appended. Placed to the default queue.
     *
     * @param namePrefix the task name prefix
     */
    public Task( @Nonnull String namePrefix )
    {
        this( namePrefix, true );
    }

    /**
     * Creates task with given name prefix. Placed to the default queue.
     *
     * @param namePrefix the name prefix
     * @param makeUnique true to append unique ID to the task name prefix
     */
    public Task( @Nonnull String namePrefix, boolean makeUnique )
    {
        this( checkNotNull( namePrefix ), makeUnique, Queue.DEFAULT_QUEUE );
    }

    /**
     * Creates task with specified name prefix and queue names.
     * Note, the unique ID will be applied only for non null name prefix.
     *
     * @param namePrefix the optional name prefix, {@code null} to be auto generated by App Engine
     * @param makeUnique true to append unique ID to the task name prefix
     * @param queueName  the queue name where task will be added
     */
    public Task( @Nullable String namePrefix, boolean makeUnique, @Nonnull String queueName )
    {
        this.namePrefix = namePrefix;
        this.makeUnique = namePrefix != null && makeUnique;
        this.queueName = checkNotNull( queueName );
    }

    @VisibleForTesting
    public void setExecutor( TaskExecutor executor )
    {
        this.executor = executor;
    }

    /**
     * Returns the entity key that might be used to retrieve entity to work with.
     * See {@link #workWith()}.
     *
     * @return the entity key
     */
    public final Key<T> getEntityKey()
    {
        return entityKey;
    }

    /**
     * Sets the entity key.
     *
     * @param entityKey the entity key to be set
     */
    public final void setEntityKey( Key<T> entityKey )
    {
        this.entityKey = entityKey;
    }

    /**
     * Returns the task name prefix.
     *
     * @return the task name prefix
     * @see #getTaskName()
     */
    public final String getNamePrefix()
    {
        return namePrefix;
    }

    /**
     * Returns the task name as a composition of the non null name prefix and entity key.
     * Otherwise returns {@code null}.
     * <p>
     * Task Name that appears in the Google Cloud Console.
     *
     * @return the task name, or {@code null} to be auto generated
     */
    public final String getTaskName()
    {
        if ( namePrefix != null )
        {
            if ( entityKey == null )
            {
                return namePrefix;
            }
            else
            {
                Object identification;
                String name = entityKey.getName();

                if ( name != null )
                {
                    // this key has a name, not ID
                    identification = null;
                }
                else
                {
                    identification = entityKey.getId();
                }

                return namePrefix + "_" + entityKey.getKind() + ( identification == null ? "" : "_" + identification );
            }
        }

        return null;
    }

    /**
     * Returns a generic name of the task that is not necessarily unique, however might be useful for logging purpose.
     *
     * @return the generic task name
     */
    public String getGenericName()
    {
        String taskName = getTaskName();
        if ( Strings.isNullOrEmpty( taskName ) )
        {
            taskName = getClass().getSimpleName();

            if ( entityKey != null )
            {
                taskName = taskName + "_" + entityKey.getKind() + "_" + entityKey.getId();
            }
        }
        return taskName;
    }

    /**
     * Returns the boolean indication whether to append an unique ID to the task name.
     * If task name prefix is {@code null} this will return {@code false}.
     *
     * @return true to append an unique ID to the task name
     */
    public final boolean isMakeUnique()
    {
        return namePrefix != null && makeUnique;
    }

    /**
     * Sets the boolean indication whether to append an unique ID to the task name.
     *
     * @param unique true to append an unique ID to the task name
     * @return this task to chain configuration
     */
    public final Task makeUnique( boolean unique )
    {
        this.makeUnique = unique;
        return this;
    }

    /**
     * Returns the queue name where task will be added.
     *
     * @return the queue name
     */
    public final String getQueueName()
    {
        return queueName;
    }

    /**
     * Schedules the given task as the next one to execute once the this (parent) task has been successfully finished.
     * The root task is the first one to be executed.
     *
     * @param task        the task as the next one to execute
     * @param postponeFor the number of seconds to be added to current time for this,
     *                    that's a time when the task will be started. Max 30 days.
     * @return just added task to chain calls
     */
    private <S> Task<S> setNext( @Nonnull Task<S> task, int postponeFor )
    {
        this.next = checkNotNull( task );
        return task.postponeFor( postponeFor );
    }

    /**
     * Schedules the given task as the next one to execute once the this (parent) task has been successfully finished.
     * The root task is the first one to be executed.
     *
     * @param task    the task as the next one to execute
     * @param options the task configuration
     * @return just added task to chain calls
     */
    private <S> Task<S> setNext( @Nonnull Task<S> task, @Nullable TaskOptions options )
    {
        this.next = checkNotNull( task );
        return task.options( options );
    }

    /**
     * Returns a task that will be executed once this task has finished successfully.
     *
     * @return the next task to execute
     */
    public final Task<?> next()
    {
        return next;
    }

    /**
     * Adds task to be scheduled as a last one in the current chain.
     * The root task is the first one to be executed.
     *
     * @param task        the task to be scheduled
     * @param postponeFor the number of seconds to be added to current time for this,
     *                    that's a time when the task will be started. Max 30 days.
     * @return just added task to chain calls
     */
    public final <S> Task<S> addNext( @Nonnull Task<S> task, int postponeFor )
    {
        return leaf().setNext( task, postponeFor );
    }

    /**
     * Adds task to be scheduled as a last one in the current chain.
     * The root task is the first one to be executed.
     *
     * @param task the task to be scheduled
     * @return just added task to chain calls
     */
    public final <S> Task<S> addNext( @Nonnull Task<S> task )
    {
        return addNext( task, ( TaskOptions ) null );
    }

    /**
     * Adds task to be scheduled as a last one in the current chain.
     * The root task is the first one to be executed.
     *
     * @param task    the task to be scheduled
     * @param options the task configuration
     * @return just added task to chain calls
     */
    public final <S> Task<S> addNext( @Nonnull Task<S> task, @Nullable TaskOptions options )
    {
        return leaf().setNext( task, options );
    }

    /**
     * Adds task to be scheduled as a last one in the current chain.
     * The root task is the first one to be executed.
     *
     * @param task      the task to be scheduled
     * @param condition the functional interface to evaluate a condition whether to enqueue given task.
     *                  If {@code false}, this task will be skipped however a following task (if exist)
     *                  will be scheduled.
     * @return just added task to chain calls
     */
    public final <S> Task<S> addNext( @Nonnull Task<S> task, @Nullable SerializableFunction<S, Boolean> condition )
    {
        // function is being evaluated on its parent task before execution
        Task<?> leaf = leaf();
        //noinspection unchecked
        leaf.function = ( SerializableFunction<Object, Boolean> ) condition;
        return leaf.setNext( task, null );
    }

    /**
     * Adds task to be scheduled as a last one in the current chain.
     * The root task is the first one to be executed.
     *
     * @param task      the task to be scheduled
     * @param condition the functional interface to evaluate a condition whether to enqueue given task.
     *                  If {@code false}, this task will be skipped however a following task (if exist)
     *                  will be scheduled.
     * @param options   the task configuration
     * @return just added task to chain calls
     */
    public final <S> Task<S> addNext( @Nonnull Task<S> task,
                                      @Nullable TaskOptions options,
                                      @Nullable SerializableFunction<S, Boolean> condition )
    {
        // function is being evaluated on its parent task before execution
        Task<?> leaf = leaf();
        //noinspection unchecked
        leaf.function = ( SerializableFunction<Object, Boolean> ) condition;
        return leaf.setNext( task, options );
    }

    /**
     * Returns a boolean indication whether current task has a next task to be scheduled once this will be done.
     *
     * @return {@code true} if has next task
     */
    public final boolean hasNext()
    {
        return next != null;
    }

    /**
     * Traverses and returns a task to be scheduled as last (leaf) in the chain.
     * If there is no next task, it will return {@code this}.
     *
     * @return the leaf task
     */
    private Task<?> leaf()
    {
        Task task = this;
        while ( task.hasNext() )
        {
            task = task.next();
        }

        return task;
    }

    /**
     * Counts number of tasks to be executed starting from this task (including).
     *
     * @return the number of tasks to be executed
     */
    public int countTasks()
    {
        int count = 1;
        Task child = this;

        while ( child.hasNext() )
        {
            child = child.next();
            count++;
        }

        return count;
    }

    /**
     * Removes the next task from the actual instance to be queued if defined.
     * <p>
     * <strong>Note</strong>: call to this method will not persist this change back to the queue
     * and will take effect only for this instance. However once task will be successfully finished,
     * no next task is going to be scheduled as it will be removed as a whole by the task engine from the queue.
     *
     * @return {@code true} if there was a planned task but has been cleared
     */
    public final boolean clear()
    {
        boolean cleared = this.next != null;
        this.next = null;
        this.function = null;
        return cleared;
    }

    /**
     * Returns the configuration options of this task.
     *
     * @return the task options configuration, {@code null} if not set
     */
    public final TaskOptions getOptions()
    {
        return options;
    }

    /**
     * Sets the configuration options for this task.
     *
     * @param options the configuration options instance
     * @return this task to chain configuration
     */
    public final Task<T> options( @Nullable TaskOptions options )
    {
        this.options = options;
        return this;
    }

    /**
     * Returns the countdown of this task in seconds, the value to be used when task will be added to the queue.
     * If not defined, the task will be executed at some time in near future.
     *
     * @return the countdown in seconds, {@code null} if not set
     */
    public final Integer getPostponeFor()
    {
        return postponeFor;
    }

    /**
     * Sets the countdown of this task in seconds.
     *
     * @param countdown the countdown to be set in seconds
     * @return this task to chain configuration
     */
    public final Task<T> postponeFor( @Nullable Integer countdown )
    {
        this.postponeFor = countdown;
        return this;
    }

    /**
     * Returns the task executor service instance.
     *
     * @return the task executor
     */
    protected final TaskExecutor executor()
    {
        return executor;
    }

    /**
     * Returns the entity resolved by {@link #getEntityKey()}.
     * <p>Objectify instances are {@link com.googlecode.objectify.Objectify#cache(boolean)}
     * {@code true} by default.</p>
     * Override this method if target entity to work with comes from the customized storage (serialized etc).
     *
     * @return the entity the task will handle or {@code null} if not found
     */
    public T workWith()
    {
        return workWith( true );
    }

    /**
     * Returns the entity resolved by {@link #getEntityKey()}.
     *
     * @param errorMessage the specified detail message for {@code IllegalArgumentException}
     * @return the entity the task will handle or throws {@link IllegalArgumentException} if not found
     */
    public T workWith( String errorMessage )
    {
        return workWith( true, errorMessage );
    }

    /**
     * Returns the entity resolved by {@link #getEntityKey()}.
     *
     * @param cache        {@code true} to use (or not use) a 2nd-level memcache
     * @param errorMessage the specified detail message for {@code IllegalArgumentException}
     * @return the entity the task will handle or throws {@link IllegalArgumentException} if not found
     */
    public final T workWith( boolean cache, String errorMessage )
    {
        T t = workWith( cache );
        if ( t == null )
        {
            String key = "; Key: " + getEntityKey();
            String message = Strings.isNullOrEmpty( errorMessage ) ? "Entity not found" : errorMessage;
            throw new IllegalArgumentException( message + key );
        }
        return t;
    }

    /**
     * Returns the entity resolved by {@link #getEntityKey()}.
     *
     * @param cache {@code true} to use (or not use) a 2nd-level memcache
     * @return the entity the task will handle or {@code null} if not found
     * @see com.googlecode.objectify.annotation.Cache
     */
    public final T workWith( boolean cache )
    {
        Key<T> key = getEntityKey();
        checkNotNull( key, "Entity key is null" );
        return ofy().cache( cache ).load().key( key ).now();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( !( o instanceof Task ) ) return false;
        Task task = ( Task ) o;
        return Objects.equals( entityKey, task.entityKey ) &&
                Objects.equals( namePrefix, task.namePrefix ) &&
                Objects.equals( queueName, task.queueName );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( entityKey, namePrefix, queueName );
    }

    @Override
    public String toString()
    {
        return "Task{" +
                "entityKey=" + entityKey +
                ", namePrefix='" + namePrefix + '\'' +
                ", makeUnique=" + makeUnique +
                ", next=" + ( next != null ) +
                ", queueName='" + queueName + '\'' +
                ", options=" + ( options != null ) +
                ", postponeFor=" + postponeFor +
                '}';
    }

    /**
     * Evaluates recursively whether there is a next task to be scheduled.
     * If a next task's non {@code null} {@link Task#function} evaluates to {@code false} that task will be skipped.
     *
     * @return the next task to be scheduled for execution, or {@code null} if none
     */
    private <S> Task nextToSchedule( @Nonnull Task<S> task )
    {
        boolean scheduleNext;
        if ( task.hasNext() && task.function != null )
        {
            Object entity = task.next().workWith();
            scheduleNext = entity != null && task.function.apply( entity );

            if ( entity == null )
            {
                logger.info( "Entity not found for key: " + task.next().getEntityKey() );
            }
            else
            {
                logger.info( task.next().getGenericName() + "'s function has been evaluated with value: " + scheduleNext );
            }
        }
        else
        {
            scheduleNext = true;
        }

        Task next;
        if ( task.hasNext() && !scheduleNext )
        {
            logger.info( "The task with name '" + task.next().getGenericName() + "' is being skipped" );
            next = nextToSchedule( task.next() );
        }
        else
        {
            next = task.next();
        }
        return next;
    }

    @Override
    public final void run()
    {
        logger.info( "Task '" + getGenericName() + "' has been started." );
        injector.injectMembers( this );

        execute();

        Task scheduled = nextToSchedule( this );
        if ( scheduled == null )
        {
            return;
        }
        else
        {
            logger.info( "The task with name '"
                    + scheduled.getGenericName()
                    + "' has been scheduled as next" );
        }

        // once parent task has been successfully executed, enqueue the next task
        TaskOptions nextTaskOptions = scheduled.getOptions();
        if ( nextTaskOptions == null )
        {
            executor.schedule( scheduled );
        }
        else
        {
            executor.schedule( scheduled, nextTaskOptions );
        }
    }

    /**
     * The client implementation to be executed asynchronously.
     */
    protected abstract void execute();

    /**
     * Serializable {@link Function}.
     */
    @FunctionalInterface
    public interface SerializableFunction<T, R>
            extends Function<T, R>, Serializable
    {
    }
}
