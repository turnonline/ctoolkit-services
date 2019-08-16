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

import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Injector;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * {@link Task} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class TaskTest
{
    @Tested
    private Task<TestModel> tested;

    @Injectable
    private Injector injector;

    @Injectable
    private TaskExecutor executor;

    @Test
    public void chainingNextNoOptions()
    {
        FakeTask next = new FakeTask();
        tested.addNext( next );

        tested.run();

        assertNull( next.getPostponeFor() );
        verificationsNoOptions( next );
    }

    @Test
    public void chainingNextRemovedByFirstTask()
    {
        TaskOptions options = TaskOptions.Builder.withDefaults();
        FakeTask next = new FakeTask();
        tested.addNext( next );

        assertTrue( tested.clear() );
        tested.run();

        new Verifications()
        {
            {
                //noinspection ConstantConditions
                executor.schedule( ( Task ) any );
                times = 0;

                executor.schedule( ( Task ) any, options );
                times = 0;
            }
        };
    }

    @Test
    public void countTasks()
    {
        FakeTask second = new FakeTask();
        FakeTask third = new FakeTask();
        FakeTask last = new FakeTask();

        tested.addNext( second ).addNext( third ).addNext( last );

        assertThat( tested.countTasks() ).isEqualTo( 4 );
    }

    @Test
    public void chainingNextWithOptions()
    {
        FakeTask next = new FakeTask();
        TaskOptions options = TaskOptions.Builder.withDefaults();
        tested.addNext( next ).options( options );

        tested.run();

        assertNull( next.getPostponeFor() );
        verificationsWithOptions( next, options );
    }

    @Test
    public void chainingNextWithOptionsAsArgument()
    {
        FakeTask next = new FakeTask();
        TaskOptions options = TaskOptions.Builder.withDefaults();
        tested.addNext( next, options );

        tested.run();

        assertNull( next.getPostponeFor() );
        verificationsWithOptions( next, options );
    }

    @Test
    public void chainingNextWithPostponeFor()
    {
        FakeTask next = new FakeTask();
        tested.addNext( next ).postponeFor( 20 );

        tested.run();

        assertEquals( next.getPostponeFor(), Integer.valueOf( 20 ) );
        verificationsNoOptions( next );
    }

    @Test
    public void chainingNextWithPostponeForAsArgument()
    {
        FakeTask next = new FakeTask();
        tested.addNext( next, 10 );

        tested.run();

        assertEquals( next.getPostponeFor(), Integer.valueOf( 10 ) );
        verificationsNoOptions( next );
    }

    @Test
    public void addNext_FunctionTrue()
    {
        final Task<TestModel> next = new FakeTask( "Second task" );
        tested.addNext( next, TestModel::isChanged );

        new Expectations( tested )
        {
            {
                next.workWith();
                result = new TestModel();

                tested.getTaskName();
                result = "First task";
            }
        };

        tested.run();

        verificationsNoOptions( next );
    }

    @Test
    public void addNext_FunctionFalse()
    {
        Task<TestModel> next = new FakeTask( "Second task" );
        tested.addNext( next, TestModel::isChanged );

        new Expectations( tested )
        {
            {
                next.workWith();
                result = new TestModel( false );

                tested.getTaskName();
                result = "Parent task";
            }
        };

        tested.run();

        new Verifications()
        {
            {
                //noinspection ConstantConditions
                executor.schedule( ( Task ) any );
                times = 0;

                //noinspection ConstantConditions
                executor.schedule( ( Task ) any, ( TaskOptions ) any );
                times = 0;
            }
        };
    }

    @Test
    public void addNext_FunctionFalseButOneMoreTask()
    {
        Task<TestModel> next = new FakeTask( "Second task" );
        Task<?> last = new FakeTask( "Last task" );

        tested.addNext( next, TestModel::isChanged ).addNext( last );

        new Expectations( tested )
        {
            {
                next.workWith();
                result = new TestModel( false );

                tested.getTaskName();
                result = "First task";
            }
        };

        tested.run();

        new Verifications()
        {
            {
                executor.schedule( next );
                times = 0;

                executor.schedule( next, ( TaskOptions ) any );
                times = 0;

                executor.schedule( last );
                times = 1;

                executor.schedule( last, ( TaskOptions ) any );
                times = 0;
            }
        };
    }

    @Test
    public void addNext_MultipleTasksExecuteLastOnly()
    {
        Task<TestModel> second = new FakeTask( "Second task" );
        Task<Test2Model> third = new Fake2Task( "Third task" );

        tested.addNext( second, TaskOptions.Builder.withDefaults(), TestModel::isChanged )
                .addNext( third, Test2Model::isChanged );

        Task<Test3Model> fourth = new Fake3Task( "Fourth task" );
        tested.addNext( fourth, Test3Model::isChanged );

        Task<Test4Model> last = new Fake4Task( "Last task" );
        tested.addNext( last, TaskOptions.Builder.withDefaults(), Test4Model::isChanged );

        new Expectations( second, third, fourth, last )
        {
            {
                second.workWith();
                result = new TestModel( false );

                third.workWith();
                result = new Test2Model( false );

                fourth.workWith();
                result = new Test3Model( false );

                last.workWith();
                result = new Test4Model( true );
            }
        };

        tested.run();

        new Verifications()
        {
            {
                executor.schedule( second );
                times = 0;

                executor.schedule( second, ( TaskOptions ) any );
                times = 0;

                executor.schedule( third );
                times = 0;

                executor.schedule( third, ( TaskOptions ) any );
                times = 0;

                executor.schedule( fourth );
                times = 0;

                executor.schedule( fourth, ( TaskOptions ) any );
                times = 0;

                executor.schedule( last );
                times = 0;

                executor.schedule( last, ( TaskOptions ) any );
                times = 1;
            }
        };
    }

    @Test
    public void workWith_WithErrorMessageButOk()
    {
        new Expectations( tested )
        {
            {
                tested.workWith( true );
                result = new TestModel( false );
            }
        };

        assertThat( tested.workWith( "Not found" ) ).isNotNull();
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void workWith_EntityNotFound()
    {
        new Expectations( tested )
        {
            {
                tested.workWith( true );
                result = null;
            }
        };

        tested.workWith( "Not found" );
    }

    private void verificationsWithOptions( final Task next, final TaskOptions options )
    {
        new Verifications()
        {
            {
                //noinspection ConstantConditions
                executor.schedule( ( Task ) any );
                times = 0;

                executor.schedule( next, options );
                times = 1;
            }
        };
    }

    private void verificationsNoOptions( final Task next )
    {
        new Verifications()
        {
            {
                executor.schedule( next );
                times = 1;

                //noinspection ConstantConditions
                executor.schedule( ( Task ) any, ( TaskOptions ) any );
                times = 0;
            }
        };
    }
}